# -*- coding: utf-8 -*
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.orders.PastTransactionKey import PastTransactionKey
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.services.reservas.reservaPersonaView import \
    get_reservation_and_person_reservation_on_namespace_with_permission
from CJM.services.validations import validate_amount, \
    validate_transaction_number, AMOUNT_TOPOFF_INVALID_AMOUNT_ERROR_CODE, \
    AMOUNT_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE, \
    AMOUNT_TOPOFF_SKU_AND_CATEGORY_ARE_EXCLUSIVE_ERROR_CODE, AMOUNT_TOPOFF_SKU_AND_CATEGORY_NOT_SENT_ERROR_CODE, \
    AMOUNT_TOPOFF_INVALID_TOPOFFS_ERROR_CODE, validate_person_reservation_keys_list, validate_id_skus_list, \
    validate_id_skus_categories_list, AMOUNT_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE, \
    AMOUNT_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE, validate_reservation_keys_list
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError, EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_list_exists, validate_datetime

AMOUNT_TOPOFFS_VIEW_NAME = "amount-topoffs"
app = Blueprint(AMOUNT_TOPOFFS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/amount-topoffs/',
           methods=['POST'], strict_slashes=False)
@with_json_body
def create_amount_topoffs(id_client):
    """
    Procesa una lista de topoffs por cantidad y las agrega a la persona correspondiente
        Parametros esperados:
            topoffs: Lista de topoffs a crear. Cada topoff debe tener:
                topoff-time: Fecha y hora de la orden
                amount: int opcional, si no se envia se supone 0 (= ilimitado)
                transaction-number: str
                Solo uno de los siguientes:
                    id-sku: int id del sku incluido
                    id-sku-category: int id de la categoria de skus incluidos
                id-reservation: Id de la reserva correspondiente a la orden
                id-person-reservation: Id de la reserva de persona correspondiente a la orden
    :param id_client: id del cliente asociado
    :return: Lista de topoffs creados
    """
    id_client = validate_id_client(id_client)

    def create_amount_topoffs_on_namespace(id_current_client):
        topoffs = request.json.get(AmountTopoff.TOPOFFS_NAME)
        topoffs = validate_list_exists(topoffs, AmountTopoff.TOPOFFS_NAME, allow_empty_list=True,
                                       internal_code=AMOUNT_TOPOFF_INVALID_TOPOFFS_ERROR_CODE)

        reservations_keys = [Reserva.get_key_from_id(topoff_data.get(AmountTopoff.RESERVATION_ID_NAME))
                             for topoff_data in topoffs]
        validate_reservation_keys_list(reservations_keys,
                                       AmountTopoff.RESERVATION_ID_NAME,
                                       allow_empty_list=True,
                                       get_objects=False)

        person_reservations_keys = [ReservaPersona.get_key_from_id(topoff_data.get(AmountTopoff.PERSON_RESERVATION_ID_NAME))
                                    for topoff_data in topoffs]
        person_reservations = validate_person_reservation_keys_list(person_reservations_keys,
                                                                    AmountTopoff.PERSON_RESERVATION_ID_NAME,
                                                                    allow_empty_list=True,
                                                                    get_objects=True)

        sku_ids = [topoff_data.get(AmountTopoff.SKU_ID_NAME)
                   for topoff_data in topoffs
                   if topoff_data.get(AmountTopoff.SKU_ID_NAME) is not None]
        validate_id_skus_list(sku_ids, AmountTopoff.SKU_ID_NAME, allow_empty_list=True)

        sku_categories_ids = [topoff_data.get(AmountTopoff.SKU_CATEGORY_ID_NAME)
                              for topoff_data in topoffs
                              if topoff_data.get(AmountTopoff.SKU_CATEGORY_ID_NAME) is not None]
        validate_id_skus_categories_list(sku_categories_ids, AmountTopoff.SKU_CATEGORY_ID_NAME, allow_empty_list=True)

        for index, topoff_data in enumerate(topoffs):
            topoff_time = topoff_data.get(AmountTopoff.TOPOFF_TIME_NAME)
            topoff_data[AmountTopoff.TOPOFF_TIME_NAME] = validate_datetime(topoff_time,
                                                                           AmountTopoff.TOPOFF_TIME_NAME,
                                                                           allow_none=False,
                                                                           internal_code=AMOUNT_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE)

            transaction_number = topoff_data.get(AmountTopoff.TRANSACTION_NUMBER_NAME)
            topoff_data[AmountTopoff.TRANSACTION_NUMBER_NAME] = validate_transaction_number(transaction_number,
                                                                                            AmountTopoff.TRANSACTION_NUMBER_NAME,
                                                                                            internal_code=AMOUNT_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE)

            amount = topoff_data.get(AmountTopoff.AMOUNT_NAME)
            topoff_data[AmountTopoff.AMOUNT_NAME] = validate_amount(amount,
                                                                    AmountTopoff.AMOUNT_NAME,
                                                                    internal_code=AMOUNT_TOPOFF_INVALID_AMOUNT_ERROR_CODE)

            id_sku = topoff_data.get(AmountTopoff.SKU_ID_NAME)
            id_sku_category = topoff_data.get(AmountTopoff.SKU_CATEGORY_ID_NAME)

            if id_sku is not None and id_sku_category is not None:
                raise ValidationError(u"The fields {0} and {1} are exclusive.".format(AmountTopoff.SKU_ID_NAME,
                                                                                      AmountTopoff.SKU_CATEGORY_ID_NAME),
                                      internal_code=AMOUNT_TOPOFF_SKU_AND_CATEGORY_ARE_EXCLUSIVE_ERROR_CODE)

            if id_sku is None and id_sku_category is None:
                raise ValidationError(u"Expected one of the following fields: [{0}, {1}].".format(AmountTopoff.SKU_ID_NAME,
                                                                                                  AmountTopoff.SKU_CATEGORY_ID_NAME),
                                      internal_code=AMOUNT_TOPOFF_SKU_AND_CATEGORY_NOT_SENT_ERROR_CODE)

        transactions_keys = [PastTransactionKey.get_key_from_fields(topoff_data.get(AmountTopoff.RESERVATION_ID_NAME),
                                                                    topoff_data.get(AmountTopoff.PERSON_RESERVATION_ID_NAME),
                                                                    topoff_data[AmountTopoff.TOPOFF_TIME_NAME],
                                                                    PastTransactionKey.AMOUNT_TOPOFF_NAME)
                             for topoff_data in topoffs]
        previous_transactions = ndb.get_multi(transactions_keys)

        topoffs_entities = []
        already_created_keys = set()
        transactions_entities = []
        for index, topoff_data in enumerate(topoffs):
            if previous_transactions[index] is None and transactions_keys[index] not in already_created_keys:
                person_reservation = person_reservations[index]
                amount = topoff_data.get(AmountTopoff.AMOUNT_NAME)
                topoff_time = topoff_data.get(AmountTopoff.TOPOFF_TIME_NAME)
                transaction_number = topoff_data.get(AmountTopoff.TRANSACTION_NUMBER_NAME)
                id_sku = topoff_data.get(AmountTopoff.SKU_ID_NAME)
                id_sku_category = topoff_data.get(AmountTopoff.SKU_CATEGORY_ID_NAME)
                topoffs_entities.append(AmountTopoff.create_without_put(id_current_client, person_reservation.idReserva,
                                                                        person_reservation.key.id(), amount,
                                                                        id_sku, id_sku_category, transaction_number,
                                                                        topoff_time))
                transactions_entities.append(PastTransactionKey.create_without_put_from_key(transactions_keys[index]))
                already_created_keys.add(transactions_keys[index])

        ndb.put_multi(topoffs_entities + transactions_entities)
        return topoffs_entities

    return on_client_namespace(id_client, create_amount_topoffs_on_namespace, action=Role.CREATE_ACTION,
                               view=AMOUNT_TOPOFFS_VIEW_NAME)


@app.route('/clients/<int:id_client>/amount-topoffs/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_amount_topoffs(id_client):
    """
    Lista los topoffs por cantidad asociados al cliente dado
    :param id_client: id del cliente asociado
    :return: Lista de topoffs por cantidad del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_amount_topoffs_on_namespace(id_current_client):
        return AmountTopoff.list()

    return on_client_namespace(id_client, list_amount_topoffs_on_namespace, action=Role.READ_ACTION,
                               view=AMOUNT_TOPOFFS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/amount-topoffs/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_amount_topoffs_by_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Lista los topoffs por cantidad existentes para la reserva dada.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Topoffs existentes para la reserva dada.
    """
    id_client = validate_id_client(id_client)

    def list_amount_topoffs_by_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              AMOUNT_TOPOFFS_VIEW_NAME)

        return AmountTopoff.list_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id())

    return on_client_namespace(id_client, list_amount_topoffs_by_person_reservation_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/amount-topoffs/<int:id_topoff>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_amount_topoff(id_client, id_reservation, id_person_reservation, id_topoff):
    """
    Da el topoff de cantidad de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_topoff: id del topoff a consultar
    :return: Topoff de cantidad con id dado
    """
    id_client = validate_id_client(id_client)

    def get_amount_topoff_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              AMOUNT_TOPOFFS_VIEW_NAME)

        topoff = AmountTopoff.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_topoff)
        if topoff is None:
            raise EntityDoesNotExists(u"Amount topoff[{0}]".format(id_topoff),
                                      internal_code=AMOUNT_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return topoff

    return on_client_namespace(id_client, get_amount_topoff_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/amount-topoffs/<int:id_topoff>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_amount_topoff(id_client, id_reservation, id_person_reservation, id_topoff):
    """
    Elimina el topoff de cantidad de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_topoff: id del topoff a eliminar
    :return: Topoff de cantidad eliminado
    """
    id_client = validate_id_client(id_client)

    def delete_amount_topoff_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.DELETE_ACTION,
                                                                                                              AMOUNT_TOPOFFS_VIEW_NAME)

        topoff = AmountTopoff.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_topoff)
        if topoff is None:
            raise EntityDoesNotExists(u"Amount topoff[{0}]".format(id_topoff),
                                      internal_code=AMOUNT_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE)
        transaction_key = PastTransactionKey.get_key_from_fields(topoff.idReserva,
                                                                 topoff.idReservacionPersona,
                                                                 topoff.tiempoTopoff,
                                                                 PastTransactionKey.AMOUNT_TOPOFF_NAME)

        topoff.mark_as_deleted()
        topoff.put()
        transaction_key.delete()
        return topoff

    return on_client_namespace(id_client, delete_amount_topoff_on_namespace, secured=False)
