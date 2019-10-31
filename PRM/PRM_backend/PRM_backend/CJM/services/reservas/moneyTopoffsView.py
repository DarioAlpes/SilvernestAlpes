# -*- coding: utf-8 -*
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.orders.PastTransactionKey import PastTransactionKey
from commons.excepciones.apiexceptions import EntityDoesNotExists
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.Moneda import Moneda
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.services.reservas.reservaPersonaView import \
    get_reservation_and_person_reservation_on_namespace_with_permission
from CJM.services.validations import validate_money, validate_transaction_number, \
    MONEY_TOPOFF_INVALID_MONEY_ERROR_CODE, MONEY_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE, \
    MONEY_TOPOFF_INVALID_TOPOFFS_ERROR_CODE, \
    validate_person_reservation_keys_list, validate_ids_currencies_list, MONEY_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE, \
    MONEY_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE, validate_reservation_keys_list
from commons.entidades.users import Role
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_list_exists, validate_datetime

MONEY_TOPOFFS_VIEW_NAME = "money-topoffs"
app = Blueprint(MONEY_TOPOFFS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/money-topoffs/',
           methods=['POST'], strict_slashes=False)
@with_json_body
def create_money_topoffs(id_client):
    """
    Procesa una lista de topoffs por dinero y los agrega a la persona correspondiente
        Parametros esperados:
            topoffs: Lista de topoffs a crear. Cada topoff debe tener:
                topoff-time: Fecha y hora de la orden
                money: float, dinero incluido en el topoff
                currency: str opcional, si se omite se supone COP. Representa el tipo de moneda incluida en el topoff
                transaction-number: str
                id-reservation: Id de la reserva correspondiente a la orden
                id-person-reservation: Id de la reserva de persona correspondiente a la orden
    :param id_client: id del cliente asociado
    :return: Lista de topoffs creados
    """
    id_client = validate_id_client(id_client)

    def create_money_topoffs_on_namespace(id_current_client):
        topoffs = request.json.get(MoneyTopoff.TOPOFFS_NAME)
        topoffs = validate_list_exists(topoffs, MoneyTopoff.TOPOFFS_NAME, allow_empty_list=True,
                                       internal_code=MONEY_TOPOFF_INVALID_TOPOFFS_ERROR_CODE)

        reservations_keys = [Reserva.get_key_from_id(topoff_data.get(MoneyTopoff.RESERVATION_ID_NAME))
                             for topoff_data in topoffs]
        validate_reservation_keys_list(reservations_keys,
                                       MoneyTopoff.RESERVATION_ID_NAME,
                                       allow_empty_list=True,
                                       get_objects=False)

        person_reservations_keys = [ReservaPersona.get_key_from_id(topoff_data.get(MoneyTopoff.PERSON_RESERVATION_ID_NAME))
                                    for topoff_data in topoffs]
        person_reservations = validate_person_reservation_keys_list(person_reservations_keys,
                                                                    MoneyTopoff.PERSON_RESERVATION_ID_NAME,
                                                                    allow_empty_list=True,
                                                                    get_objects=True)

        currencies_names = [topoff_data.get(MoneyTopoff.CURRENCY_NAME)
                            for topoff_data in topoffs]
        currencies_names = [Moneda.DEFAULT_CURRENCY_NAME if currency_name is None else currency_name
                            for currency_name in currencies_names]
        currencies_names = validate_ids_currencies_list(currencies_names,
                                                        MoneyTopoff.CURRENCY_NAME,
                                                        allow_empty_list=True)

        for index, topoff_data in enumerate(topoffs):
            topoff_time = topoff_data.get(MoneyTopoff.TOPOFF_TIME_NAME)
            topoff_data[MoneyTopoff.TOPOFF_TIME_NAME] = validate_datetime(topoff_time,
                                                                          MoneyTopoff.TOPOFF_TIME_NAME,
                                                                          allow_none=False,
                                                                          internal_code=MONEY_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE)

            transaction_number = topoff_data.get(MoneyTopoff.TRANSACTION_NUMBER_NAME)
            topoff_data[MoneyTopoff.TRANSACTION_NUMBER_NAME] = validate_transaction_number(transaction_number,
                                                                                           MoneyTopoff.TRANSACTION_NUMBER_NAME,
                                                                                           internal_code=MONEY_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE)

            money = topoff_data.get(MoneyTopoff.MONEY_NAME)
            topoff_data[MoneyTopoff.MONEY_NAME] = validate_money(money, MoneyTopoff.MONEY_NAME,
                                                                 internal_code=MONEY_TOPOFF_INVALID_MONEY_ERROR_CODE)

        transactions_keys = [PastTransactionKey.get_key_from_fields(topoff_data.get(MoneyTopoff.RESERVATION_ID_NAME),
                                                                    topoff_data.get(MoneyTopoff.PERSON_RESERVATION_ID_NAME),
                                                                    topoff_data[MoneyTopoff.TOPOFF_TIME_NAME],
                                                                    PastTransactionKey.MONEY_TOPOFF_NAME)
                             for topoff_data in topoffs]
        previous_transactions = ndb.get_multi(transactions_keys)

        topoffs_entities = []
        already_created_keys = set()
        transactions_entities = []
        for index, topoff_data in enumerate(topoffs):
            if previous_transactions[index] is None and transactions_keys[index] not in already_created_keys:
                person_reservation = person_reservations[index]
                currency = currencies_names[index]
                money = topoff_data.get(MoneyTopoff.MONEY_NAME)
                topoff_time = topoff_data.get(MoneyTopoff.TOPOFF_TIME_NAME)
                transaction_number = topoff_data.get(MoneyTopoff.TRANSACTION_NUMBER_NAME)
                topoffs_entities.append(MoneyTopoff.create_without_put(id_current_client, person_reservation.idReserva,
                                                                       person_reservation.key.id(), money,
                                                                       currency, transaction_number, topoff_time))
                transactions_entities.append(PastTransactionKey.create_without_put_from_key(transactions_keys[index]))
                already_created_keys.add(transactions_keys[index])

        ndb.put_multi(topoffs_entities + transactions_entities)
        return topoffs_entities

    return on_client_namespace(id_client, create_money_topoffs_on_namespace, action=Role.CREATE_ACTION,
                               view=MONEY_TOPOFFS_VIEW_NAME)


@app.route('/clients/<int:id_client>/money-topoffs/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_money_topoffs(id_client):
    """
    Lista los topoffs por dinero asociados al cliente dado.
    :param id_client: id del cliente asociado
    :return: Lista de topoffs dinero del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_money_topoffs_on_namespace(id_current_client):
        return MoneyTopoff.list()

    return on_client_namespace(id_client, list_money_topoffs_on_namespace, action=Role.READ_ACTION,
                               view=MONEY_TOPOFFS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/money-topoffs/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_money_topoffs_by_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Lista los topoffs por dinero existentes para la reserva dada.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Topoffs existentes para la reserva dada.
    """
    id_client = validate_id_client(id_client)

    def list_money_topoffs_by_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              MONEY_TOPOFFS_VIEW_NAME)

        return MoneyTopoff.list_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id())

    return on_client_namespace(id_client, list_money_topoffs_by_person_reservation_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/money-topoffs/<int:id_topoff>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_money_topoff(id_client, id_reservation, id_person_reservation, id_topoff):
    """
    Da el topoff de dinero de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_topoff: id del topoff a consultar
    :return: Topoff de dinero con id dado
    """
    id_client = validate_id_client(id_client)

    def get_money_topoff_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              MONEY_TOPOFFS_VIEW_NAME)

        topoff = MoneyTopoff.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_topoff)
        if topoff is None:
            raise EntityDoesNotExists(u"Money topoff[{0}]".format(id_topoff),
                                      internal_code=MONEY_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return topoff

    return on_client_namespace(id_client, get_money_topoff_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/money-topoffs/<int:id_topoff>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_money_topoff(id_client, id_reservation, id_person_reservation, id_topoff):
    """
    Elimina el topoff de dinero de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_topoff: id del topoff a eliminar
    :return: Topoff de dinero eliminado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_money_topoff_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.DELETE_ACTION,
                                                                                                              MONEY_TOPOFFS_VIEW_NAME)

        topoff = MoneyTopoff.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_topoff)
        if topoff is None:
            raise EntityDoesNotExists(u"Money topoff[{0}]".format(id_topoff),
                                      internal_code=MONEY_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE)

        transaction_key = PastTransactionKey.get_key_from_fields(topoff.idReserva,
                                                                 topoff.idReservacionPersona,
                                                                 topoff.tiempoTopoff,
                                                                 PastTransactionKey.MONEY_TOPOFF_NAME)

        topoff.mark_as_deleted()
        topoff.put()
        transaction_key.delete()
        return topoff

    return on_client_namespace(id_client, delete_money_topoff_on_namespace, secured=False)
