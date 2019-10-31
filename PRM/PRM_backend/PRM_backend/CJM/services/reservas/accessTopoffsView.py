# -*- coding: utf-8 -*
from CJM.entidades.reservas.AccessTopoff import AccessTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.orders.PastTransactionKey import PastTransactionKey
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.services.reservas.reservaPersonaView import \
    get_reservation_and_person_reservation_on_namespace_with_permission
from CJM.services.validations import validate_amount, validate_transaction_number, \
    ACCESS_TOPOFF_INVALID_AMOUNT_ERROR_CODE, ACCESS_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE, \
    ACCESS_TOPOFF_INVALID_TOPOFFS_ERROR_CODE, \
    validate_person_reservation_keys_list, ACCESS_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE, \
    ACCESS_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE, validate_reservation_keys_list
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_list_exists, \
    validate_id_locations_list, validate_datetime

ACCESS_TOPOFFS_VIEW_NAME = "access-topoffs"
app = Blueprint(ACCESS_TOPOFFS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/access-topoffs/',
           methods=['POST'], strict_slashes=False)
@with_json_body
def create_access_topoffs(id_client):
    """
    Procesa una lista de topoffs de accesos y las agrega a la persona correspondiente
        Parametros esperados:
            topoffs: Lista de topoffs a crear. Cada topoff debe tener:
                topoff-time: Fecha y hora de la orden
                amount: int opcional, si no se envia se supone 0 (= ilimitado)
                transaction-number: str
                id-location: int id de la ubicación asociada al acceso
                id-reservation: Id de la reserva correspondiente a la orden
                id-person-reservation: Id de la reserva de persona correspondiente a la orden
    :param id_client: id del cliente asociado
    :return: Lista de topoffs creados
    """
    id_client = validate_id_client(id_client)

    def create_access_topoffs_on_namespace(id_current_client):
        """
        Procesa la lista y crea las entidades correspondientes
        NO se ejecuta dentro de una transacción para evitar problemas cuando la lista tenga más de 25 paquetes diferentes,
        el código debe revisar que los datos son correctos antes de crear cualquier entidad
        :param id_current_client:
        :return:
        """
        topoffs = request.json.get(AccessTopoff.TOPOFFS_NAME)
        topoffs = validate_list_exists(topoffs, AccessTopoff.TOPOFFS_NAME, allow_empty_list=True,
                                       internal_code=ACCESS_TOPOFF_INVALID_TOPOFFS_ERROR_CODE)

        reservations_keys = [Reserva.get_key_from_id(topoff_data.get(AccessTopoff.RESERVATION_ID_NAME))
                             for topoff_data in topoffs]
        validate_reservation_keys_list(reservations_keys,
                                       AccessTopoff.RESERVATION_ID_NAME,
                                       allow_empty_list=True,
                                       get_objects=False)

        person_reservations_keys = [ReservaPersona.get_key_from_id(topoff_data.get(AccessTopoff.PERSON_RESERVATION_ID_NAME))
                                    for topoff_data in topoffs]
        person_reservations = validate_person_reservation_keys_list(person_reservations_keys,
                                                                    AccessTopoff.PERSON_RESERVATION_ID_NAME,
                                                                    allow_empty_list=True,
                                                                    get_objects=True)

        locations_ids = [topoff_data.get(AccessTopoff.LOCATION_ID_NAME)
                         for topoff_data in topoffs]
        validate_id_locations_list(locations_ids, AccessTopoff.LOCATION_ID_NAME, allow_empty_list=True)

        for index, topoff_data in enumerate(topoffs):
            topoff_time = topoff_data.get(AccessTopoff.TOPOFF_TIME_NAME)
            topoff_data[AccessTopoff.TOPOFF_TIME_NAME] = validate_datetime(topoff_time,
                                                                           AccessTopoff.TOPOFF_TIME_NAME,
                                                                           allow_none=False,
                                                                           internal_code=ACCESS_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE)

            transaction_number = topoff_data.get(AccessTopoff.TRANSACTION_NUMBER_NAME)
            topoff_data[AccessTopoff.TRANSACTION_NUMBER_NAME] = validate_transaction_number(transaction_number,
                                                                                            AccessTopoff.TRANSACTION_NUMBER_NAME,
                                                                                            internal_code=ACCESS_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE)

            amount = topoff_data.get(AccessTopoff.AMOUNT_NAME)
            if amount is None:
                amount = 0
            topoff_data[AccessTopoff.AMOUNT_NAME] = validate_amount(amount,
                                                                    AccessTopoff.AMOUNT_NAME,
                                                                    internal_code=ACCESS_TOPOFF_INVALID_AMOUNT_ERROR_CODE,
                                                                    allow_zero=True)

        transactions_keys = [PastTransactionKey.get_key_from_fields(topoff_data.get(AccessTopoff.RESERVATION_ID_NAME),
                                                                    topoff_data.get(AccessTopoff.PERSON_RESERVATION_ID_NAME),
                                                                    topoff_data[AccessTopoff.TOPOFF_TIME_NAME],
                                                                    PastTransactionKey.ACCESS_TOPOFF_NAME)
                             for topoff_data in topoffs]
        previous_transactions = ndb.get_multi(transactions_keys)

        topoffs_entities = []
        already_created_keys = set()
        transactions_entities = []
        for index, topoff_data in enumerate(topoffs):
            if previous_transactions[index] is None and transactions_keys[index] not in already_created_keys:
                person_reservation = person_reservations[index]
                amount = topoff_data.get(AccessTopoff.AMOUNT_NAME)
                topoff_time = topoff_data.get(AccessTopoff.TOPOFF_TIME_NAME)
                transaction_number = topoff_data.get(AccessTopoff.TRANSACTION_NUMBER_NAME)
                id_location = locations_ids[index]
                topoffs_entities.append(AccessTopoff.create_without_put(id_current_client, person_reservation.idReserva,
                                                                        person_reservation.key.id(), amount,
                                                                        id_location, transaction_number, topoff_time))
                transactions_entities.append(PastTransactionKey.create_without_put_from_key(transactions_keys[index]))
                already_created_keys.add(transactions_keys[index])

        ndb.put_multi(topoffs_entities + transactions_entities)
        return topoffs_entities

    return on_client_namespace(id_client, create_access_topoffs_on_namespace,
                               view=ACCESS_TOPOFFS_VIEW_NAME,
                               action=Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/access-topoffs/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_access_topoffs(id_client):
    """
    Lista los topoffs de accesos del cliente dado
    :param id_client: id del cliente asociado
    :return: Lista de topoffs de accesos del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_access_topoffs_on_namespace(id_current_client):
        return AccessTopoff.list()
    return on_client_namespace(id_client,
                               list_access_topoffs_on_namespace,
                               action=Role.READ_ACTION,
                               view=ACCESS_TOPOFFS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/access-topoffs/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_access_topoffs_by_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Lista los topoffs de accesos existentes para la reserva dada.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Topoffs existentes para la reserva dada.
    """
    id_client = validate_id_client(id_client)

    def list_access_topoffs_by_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              ACCESS_TOPOFFS_VIEW_NAME)

        return AccessTopoff.list_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id())

    return on_client_namespace(id_client, list_access_topoffs_by_person_reservation_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/access-topoffs/<int:id_topoff>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_access_topoff(id_client, id_reservation, id_person_reservation, id_topoff):
    """
    Da el topoff de acceso de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_topoff: id del topoff a consultar
    :return: Topoff de acceso con id dado
    """
    id_client = validate_id_client(id_client)

    def get_access_topoff_on_namespace(id_current_client):
        reservation, person_reservation, topoff = get_reservation_person_reservation_and_access_topoff_with_permissions(id_current_client,
                                                                                                                        id_reservation,
                                                                                                                        id_person_reservation,
                                                                                                                        id_topoff,
                                                                                                                        Role.READ_ACTION,
                                                                                                                        ACCESS_TOPOFFS_VIEW_NAME)

        return topoff

    return on_client_namespace(id_client, get_access_topoff_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/access-topoffs/<int:id_topoff>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_access_topoff(id_client, id_reservation, id_person_reservation, id_topoff):
    """
    Elimina el topoff de acceso de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_topoff: id del topoff a eliminar
    :return: Topoff de acceso eliminado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_access_topoff_on_namespace(id_current_client):
        reservation, person_reservation, topoff = get_reservation_person_reservation_and_access_topoff_with_permissions(id_current_client,
                                                                                                                        id_reservation,
                                                                                                                        id_person_reservation,
                                                                                                                        id_topoff,
                                                                                                                        Role.DELETE_ACTION,
                                                                                                                        ACCESS_TOPOFFS_VIEW_NAME)

        transaction_key = PastTransactionKey.get_key_from_fields(topoff.idReserva,
                                                                 topoff.idReservacionPersona,
                                                                 topoff.tiempoTopoff,
                                                                 PastTransactionKey.ACCESS_TOPOFF_NAME)

        topoff.mark_as_deleted()
        topoff.put()
        transaction_key.delete()
        return topoff

    return on_client_namespace(id_client, delete_access_topoff_on_namespace, secured=False)


def get_reservation_person_reservation_and_access_topoff_with_permissions(id_client, id_reservation,
                                                                          id_person_reservation, id_topoff, action,
                                                                          view):

    reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_client,
                                                                                                          id_reservation,
                                                                                                          id_person_reservation,
                                                                                                          action,
                                                                                                          view)
    topoff = AccessTopoff.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_topoff)
    if topoff is not None:
        # Only return if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            id_location=topoff.idUbicacion,
                            action=action,
                            view=view)
        return reservation, person_reservation, topoff
    else:
        # Only raise a validation error if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            action=action,
                            view=view)
        raise EntityDoesNotExists(u"Access topoff[{0}]".format(id_topoff),
                                  internal_code=ACCESS_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
