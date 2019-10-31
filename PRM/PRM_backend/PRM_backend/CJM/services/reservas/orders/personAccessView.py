# -*- coding: utf-8 -*
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.orders.PastTransactionKey import PastTransactionKey
from CJM.entidades.reservas.orders.PersonAccess import PersonAccess
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.eventos.Devolucion import Devolucion
from CJM.entidades.eventos.Visita import Visita
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.services.reservas.reservaPersonaView import \
    get_reservation_and_person_reservation_on_namespace_with_permission
from CJM.services.validations import PERSON_ACCESS_INVALID_ACCESSES_ERROR_CODE, \
    PERSON_ACCESS_INVALID_ACCESS_TIME_ERROR_CODE, validate_person_reservation_keys_list, \
    PERSON_ACCESS_DOES_NOT_EXISTS_ERROR_CODE, validate_reservation_keys_list
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_id_location, validate_datetime, validate_list_exists

PERSON_ACCESSES_VIEW_NAME = "person-access"
app = Blueprint(PERSON_ACCESSES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/person-accesses/',
           methods=['POST'], strict_slashes=False)
@with_json_body
def create_person_accesses(id_client):
    """
    Procesa una lista de accesos y las agrega a la persona correspondiente
        Parametros esperados:
            id-location: int, id de la ubicación donde se hicieron los accesos
            accesses: Lista de accesos consumidos. Cada acceso debe tener:
                access-time: Fecha y hora de la orden
                id-reservation: Id de la reserva correspondiente a la orden
                id-person-reservation: Id de la reserva de persona correspondiente a la orden
    :param id_client: id del cliente asociado
    :return: Lista de accesos creados
    """
    id_client = validate_id_client(id_client)

    id_original_location = request.json.get(PersonAccess.LOCATION_ID_NAME)

    def create_person_accesses_on_namespace(id_current_client):
        """
        Procesa la lista y crea las entidades correspondientes
        NO se ejecuta dentro de una transacción para evitar problemas cuando la lista tenga más de 25 paquetes diferentes,
        el código debe revisar que los datos son correctos antes de crear cualquier entidad
        :param id_current_client:
        :return:
        """
        # Check global fields
        id_location = id_original_location
        id_location = validate_id_location(id_location)

        accesses = request.json.get(PersonAccess.ACCESSES_NAME)
        accesses = validate_list_exists(accesses, PersonAccess.ACCESSES_NAME, allow_empty_list=True,
                                        internal_code=PERSON_ACCESS_INVALID_ACCESSES_ERROR_CODE)

        reservations_keys = [Reserva.get_key_from_id(access_data.get(PersonAccess.RESERVATION_ID_NAME))
                             for access_data in accesses]
        validate_reservation_keys_list(reservations_keys,
                                       PersonAccess.RESERVATION_ID_NAME,
                                       allow_empty_list=True,
                                       get_objects=False)

        person_reservations_keys = [ReservaPersona.get_key_from_id(access_data.get(PersonAccess.PERSON_RESERVATION_ID_NAME))
                                    for access_data in accesses]
        person_reservations = validate_person_reservation_keys_list(person_reservations_keys,
                                                                    PersonAccess.PERSON_RESERVATION_ID_NAME,
                                                                    allow_empty_list=True,
                                                                    get_objects=True)

        for index, access_data in enumerate(accesses):
            access_time = access_data.get(PersonAccess.ACCESS_TIME_NAME)
            access_data[PersonAccess.ACCESS_TIME_NAME] = validate_datetime(access_time,
                                                                           PersonAccess.ACCESSES_NAME,
                                                                           allow_none=False,
                                                                           internal_code=PERSON_ACCESS_INVALID_ACCESS_TIME_ERROR_CODE)
        transactions_keys = [PastTransactionKey.get_key_from_fields(access_data.get(PersonAccess.RESERVATION_ID_NAME),
                                                                    access_data.get(PersonAccess.PERSON_RESERVATION_ID_NAME),
                                                                    access_data[PersonAccess.ACCESS_TIME_NAME],
                                                                    PastTransactionKey.ACCESS_NAME)
                             for access_data in accesses]
        previous_transactions = ndb.get_multi(transactions_keys)

        already_created_keys = set()
        accesses_entities = []
        transactions_entities = []
        # Create consumptions
        for index, access_data in enumerate(accesses):
            if previous_transactions[index] is None and transactions_keys[index] not in already_created_keys:
                access_time = access_data.get(PersonAccess.ACCESS_TIME_NAME)

                person_reservation = person_reservations[index]
                access = PersonAccess.create_without_put(id_current_client,
                                                         person_reservation.idReserva,
                                                         person_reservation.key.id(),
                                                         id_location,
                                                         access_time)

                accesses_entities.append(access)
                transactions_entities.append(PastTransactionKey.create_without_put_from_key(transactions_keys[index]))
                already_created_keys.add(transactions_keys[index])

                visit = Visita.create(id_client, person_reservation.idPersona, access_time, access_time, id_location)
                access.associate_visit(visit)

        ndb.put_multi(accesses_entities + transactions_entities)
        return accesses_entities

    return on_client_namespace(id_client, create_person_accesses_on_namespace,
                               view=PERSON_ACCESSES_VIEW_NAME,
                               action=Role.CREATE_ACTION,
                               id_location=id_original_location)


@app.route('/clients/<int:id_client>/person-accesses/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_person_accesses(id_client):
    """
    Lista los acceso de persona existentes.
    :param id_client: id del cliente asociado
    :return: Lista de accesos de persona existentes
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_amount_orders_on_namespace(id_current_client):
        return PersonAccess.list()
    return on_client_namespace(id_client,
                               list_amount_orders_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSON_ACCESSES_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-accesses/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_person_accesses_by_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Lista los accesos de persona existentes para la reserva dada.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Accesos de persona existentes para la reserva dada.
    """
    id_client = validate_id_client(id_client)

    def list_person_accesses_by_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              PERSON_ACCESSES_VIEW_NAME)

        return PersonAccess.list_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id())

    return on_client_namespace(id_client, list_person_accesses_by_person_reservation_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-accesses/<int:id_person_access>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_person_access(id_client, id_reservation, id_person_reservation, id_person_access):
    """
    Da el acceso de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_person_access: id del acceso de persona a consultar
    :return: Acceso de persona con id dado
    """
    id_client = validate_id_client(id_client)

    def get_person_access_on_namespace(id_current_client):
        reservation, person_reservation, access = get_reservation_person_reservation_and_person_access_with_permissions(id_current_client,
                                                                                                                        id_reservation,
                                                                                                                        id_person_reservation,
                                                                                                                        id_person_access,
                                                                                                                        Role.READ_ACTION,
                                                                                                                        PERSON_ACCESSES_VIEW_NAME)

        return access

    return on_client_namespace(id_client, get_person_access_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-accesses/<int:id_person_access>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_person_access(id_client, id_reservation, id_person_reservation, id_person_access):
    """
    Elimina el acceso de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_person_access: id del conusmo a consultar
    :return: Acceso de persona eliminado
    """
    id_client = validate_id_client(id_client)

    def delete_person_access_on_namespace(id_current_client):
        reservation, person_reservation, access = get_reservation_person_reservation_and_person_access_with_permissions(id_current_client,
                                                                                                                        id_reservation,
                                                                                                                        id_person_reservation,
                                                                                                                        id_person_access,
                                                                                                                        Role.DELETE_ACTION,
                                                                                                                        PERSON_ACCESSES_VIEW_NAME)
        access.mark_as_deleted()
        access.put()

        PastTransactionKey.get_key_from_fields(access.idReserva,
                                               access.idReservaPersona,
                                               access.tiempoAcceso,
                                               PastTransactionKey.ACCESS_NAME).delete()

        initial_time = validate_datetime(datetime_input=None, field_name=Visita.INITIAL_TIME_NAME, allow_none=True)
        Devolucion.create(id_current_client, person_reservation.idPersona, initial_time, access.idEvento)
        return access

    return on_client_namespace(id_client, delete_person_access_on_namespace, secured=False)


def get_reservation_person_reservation_and_person_access_with_permissions(id_client, id_reservation,
                                                                          id_person_reservation, id_access, action,
                                                                          view):

    reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_client,
                                                                                                          id_reservation,
                                                                                                          id_person_reservation,
                                                                                                          action,
                                                                                                          view)
    access = PersonAccess.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_access)
    if access is not None:
        # Only return if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            id_location=access.idUbicacionAcceso,
                            action=action,
                            view=view)
        return reservation, person_reservation, access
    else:
        # Only raise a validation error if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            action=action,
                            view=view)
        raise EntityDoesNotExists(u"Person Access[{0}]".format(id_access),
                                  internal_code=PERSON_ACCESS_DOES_NOT_EXISTS_ERROR_CODE)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
