# -*- coding: utf-8 -*

import itertools

from CJM.entidades.reservas.AccessTopoff import AccessTopoff
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.entidades.reservas.orders.Order import Order
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.reports.entities_by_user.UserEntities import UserEntities
from CJM.entidades.reservas.orders.PersonAccess import PersonAccess
from CJM.entidades.reservas.orders.PersonConsumptionByMoney import PersonConsumptionByMoney
from CJM.services.validations import ENTITIES_PER_USER_INVALID_KIND_ERROR_CODE, \
    ENTITIES_PER_USER_INVALID_INITIAL_TIME_ERROR_CODE, ENTITIES_PER_USER_INVALID_FINAL_TIME_ERROR_CODE, \
    ENTITIES_PER_USER_INVALID_INCLUDE_DELETED_ERROR_CODE
from commons.entidades.logs.SuccessLog import SuccessLog
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless
from commons.validations import validate_id_client, validate_datetime, validate_string_not_empty, \
    validate_bool_not_empty

ENTITIES_BY_KIND_PER_USER_VIEW_NAME = "entities-by-kind-per-user"
app = Blueprint(ENTITIES_BY_KIND_PER_USER_VIEW_NAME, __name__)

RESERVATIONS_KIND_NAME = "reservations"
PERSONS_RESERVATIONS_KIND_NAME = "persons-reservations"
MONEY_TOPOFFS_KIND_NAME = "money-topoffs"
AMOUNT_TOPOFFS_KIND_NAME = "amount-topoffs"
ACCESS_TOPOFFS_KIND_NAME = "access-topoffs"
PERSON_ACCESSES_KIND_NAME = "person-accesses"
PERSON_ORDERS_KIND_NAME = "person-orders"
PERSON_AMOUNT_CONSUMPTIONS_KIND_NAME = "person-amount-consumptions"
PERSON_MONEY_CONSUMPTIONS_KIND_NAME = "person-money-consumptions"

ALLOWED_KINDS = {RESERVATIONS_KIND_NAME: Reserva,
                 PERSONS_RESERVATIONS_KIND_NAME: ReservaPersona,
                 MONEY_TOPOFFS_KIND_NAME: MoneyTopoff,
                 AMOUNT_TOPOFFS_KIND_NAME: AmountTopoff,
                 ACCESS_TOPOFFS_KIND_NAME: AccessTopoff,
                 PERSON_ACCESSES_KIND_NAME: PersonAccess,
                 PERSON_ORDERS_KIND_NAME: Order,
                 PERSON_AMOUNT_CONSUMPTIONS_KIND_NAME: PersonConsumptionByAmount,
                 PERSON_MONEY_CONSUMPTIONS_KIND_NAME: PersonConsumptionByMoney}
ALLOWED_KINDS_NAMES = {kind_name: ALLOWED_KINDS[kind_name].__name__ for kind_name in ALLOWED_KINDS}

METHODS_CREATE_ENTITIES = {"POST"}


@app.route('/clients/<int:id_client>/entities-per-user/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_entities_per_user(id_client):
    """
    Da la lista de transacciones por usuario
        Parametros esperados en el query string:
            kind: str
            initial-time: Fecha y hora inicial a filtrar, opcional
            final-time: Fecha y hora inicial a filtrar, opcional
    :param id_client: id del cliente asociado
    :return: Lista de transacciones por usuario
    """
    id_client = validate_id_client(id_client)
    kind_name = request.args.get(UserEntities.KIND_NAME)

    kind_name = validate_string_not_empty(kind_name, UserEntities.KIND_NAME,
                                          internal_code=ENTITIES_PER_USER_INVALID_KIND_ERROR_CODE)
    if kind_name not in ALLOWED_KINDS_NAMES:
        raise ValidationError(u"Invalid value for field {0}, expected one of the following values: {1}"
                              .format(UserEntities.KIND_NAME,
                                      ALLOWED_KINDS_NAMES.keys()),
                              internal_code=ENTITIES_PER_USER_INVALID_KIND_ERROR_CODE)
    kind = ALLOWED_KINDS_NAMES[kind_name]

    include_deleted = request.args.get(UserEntities.INCLUDE_DELETED_NAME)
    if include_deleted is not None:
        include_deleted = validate_bool_not_empty(include_deleted, UserEntities.INCLUDE_DELETED_NAME, allow_string=True,
                                                  internal_code=ENTITIES_PER_USER_INVALID_INCLUDE_DELETED_ERROR_CODE)
    else:
        include_deleted = False

    initial_time = request.args.get(UserEntities.INITIAL_TIME_NAME)
    if initial_time is not None:
        initial_time = validate_datetime(initial_time, UserEntities.INITIAL_TIME_NAME, allow_none=False,
                                         internal_code=ENTITIES_PER_USER_INVALID_INITIAL_TIME_ERROR_CODE)

    final_time = request.args.get(UserEntities.FINAL_TIME_NAME)
    if final_time is not None:
        final_time = validate_datetime(final_time, UserEntities.FINAL_TIME_NAME, allow_none=False,
                                       internal_code=ENTITIES_PER_USER_INVALID_FINAL_TIME_ERROR_CODE)
    logs = SuccessLog.get_by_client_and_methods_and_between_dates(id_client, [kind], METHODS_CREATE_ENTITIES,
                                                                  initial_time, final_time).fetch()

    def get_entities_per_user_on_namespace(_):
        entities_by_user = dict()
        all_keys_entities = []
        for log in logs:
            all_keys_entities = itertools.chain(all_keys_entities, log.entities_keys)

        all_entities = ndb.get_multi(all_keys_entities)
        current_index = 0
        for log in logs:
            if log.user_key not in entities_by_user:
                entities_by_user[log.user_key] = UserEntities(log.user_key)
            final_index = current_index + len(log.entities_keys)
            entities_by_user[log.user_key].add_entities(log, itertools.islice(all_entities, current_index, final_index),
                                                        include_deleted)
            current_index = final_index
        return entities_by_user.values()

    return on_client_namespace(id_client, get_entities_per_user_on_namespace,
                               action=Role.READ_ACTION,
                               view=ENTITIES_BY_KIND_PER_USER_VIEW_NAME)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
