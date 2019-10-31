# -*- coding: utf-8 -*+
from CJM.entidades.reservas.ActivacionesReservaPersona import ActivacionesReservaPersona
from CJM.entidades.reservas.AccessTopoff import AccessTopoff
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.EventoSocial import EventoSocial
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.balance.ReservationsBalanceCalculator import ReservationsBalanceCalculator
from CJM.entidades.reservas.orders.PersonAccess import PersonAccess
from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.entidades.reservas.orders.PersonConsumptionByMoney import PersonConsumptionByMoney
from commons.entidades.Cliente import Cliente
from flask import request, Blueprint

from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.persons.Persona import Persona
from CJM.entidades.reservas.ReservaPersona import ReservaPersona, UserActivations
from CJM.services.paquetes.packagePriceRuleView import get_base_price_and_tax_rate_from_package
from CJM.services.paquetes.paqueteView import check_permissions_by_package_location
from CJM.services.validations import validate_id_person, validate_document_type, \
    validate_document_number, PERSON_DOES_NOT_EXISTS_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_ACTIVE_ERROR_CODE, PERSON_RESERVATION_TRYING_TO_ACTIVATE_UNPAID_RESERVATION_ERROR_CODE, \
    DELETE_HOLDER_PERSON_RESERVATION_ERROR_CODE, \
    DELETE_ACTIVE_PERSON_RESERVATION_ERROR_CODE, DELETE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE, \
    DELETE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_DOCUMENT_TYPE_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_DOCUMENT_NUMBER_ERROR_CODE, DELETE_PERSON_RESERVATION_WITH_TOPOFFS_ERROR_CODE, \
    UPDATE_ACTIVE_PERSON_RESERVATION_ERROR_CODE, UPDATE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE, \
    UPDATE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE, UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_INITIAL_DATE_ERROR_CODE, PERSON_RESERVATION_INVALID_FINAL_DATE_ERROR_CODE, \
    PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FINAL_ERROR_CODE, \
    PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FROM_ERROR_CODE, \
    PERSON_RESERVATION_RANGE_OF_DATES_THROUGH_GREATER_THAN_FINAL_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_RANGE_OF_DATES_INITIAL_SMALLER_THAN_EVENT_INITIAL_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_RANGE_OF_DATES_FINAL_BIGGER_THAN_EVENT_FINAL_ERROR_CODE, \
    PERSON_RESERVATION_INVALID_BASE_TIME_ERROR_CODE, \
    PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_INITIAL_TIME_ERROR_CODE, \
    PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_FINAL_TIME_ERROR_CODE, \
    PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_ERROR_CODE, NO_DOCUMENT_DOCUMENT_TYPE, \
    PERSON_RESERVATION_TRYING_TO_ACTIVATE_ALREADY_ACTIVE_PERSON_RESERVATION_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError, EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_bool_not_empty, validate_datetime, DEFAULT_DATETIME_FORMAT, \
    COMPENSAR_RESERVATIONS_SERVICE_NAME

AVAILABLE_FUNDS_VIEW_NAME = "available-funds"
PERSON_RESERVATION_BALANCE_VIEW_NAME = "person-reservation-balance"
PERSON_RESERVATIONS_VIEW_NAME = "person-reservation"
PERSON_RESERVATIONS_ACTIVATIONS_VIEW_NAME = "persons-reservations-activations"
PERSON_RESERVATIONS_KEYS_VIEW_NAME = "person-reservation-keys"
app = Blueprint(PERSON_RESERVATIONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/persons-reservations/',
           methods=['POST'], strict_slashes=False)
@with_json_body
def create_person_reservation(id_client, id_reservation):
    """
    Crea una reserva de una persona y la asocia al paquete y reserva con ids dados en el namespace del cliente id_client
        Parametros esperados:
            id-person: int opcional, se crea una nueva persona si se omite
            id-package: int
            initial-date: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] obligatorio
            final-date: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] obligatorio
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :return: Reserva de persona creada
    """
    id_client = validate_id_client(id_client)

    def create_person_reservation_on_namespace(id_current_client, id_person, package, reservation,
                                               initial_date, final_date):
        current_number_of_persons_reservations = ReservaPersona.count_by_id_reservation(reservation.key.id())
        _check_there_are_no_person_reservations_for_person_on_dates(id_person, initial_date, final_date)
        is_holder = current_number_of_persons_reservations == 0
        base_price, tax_rate = get_base_price_and_tax_rate_from_package(id_person, package)

        return ReservaPersona.create(id_current_client, reservation.key.id(), package, id_person, is_holder, base_price,
                                     tax_rate, initial_date, final_date)

    return _get_and_validate_person_reservation_json_params(id_client,
                                                            id_reservation,
                                                            create_person_reservation_on_namespace,
                                                            Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/persons-reservations-by-document/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_person_reservations_by_document(id_client):
    """
    Lista las reservas de personas de la persona con documento del cliente correspondiente.
        Parametros esperados en el query string:
            document-type: str en {"CC", "TI", "CE"}
            document-number: str
    :param id_client: id del cliente asociado
    :return: Lista de reservas de personas de la persona con documento dado del cliente correspondiente
    """
    id_client = validate_id_client(id_client)
    client = Cliente.get_by_id(id_client)
    document_type = request.args.get(Persona.DOCUMENT_TYPE_NAME)
    document_type = validate_document_type(document_type, Persona.DOCUMENT_TYPE_NAME,
                                           internal_code=PERSON_RESERVATION_INVALID_DOCUMENT_TYPE_ERROR_CODE)

    document_number = request.args.get(Persona.DOCUMENT_NUMBER_NAME)
    document_number = validate_document_number(document_number, Persona.DOCUMENT_NUMBER_NAME,
                                               internal_code=PERSON_RESERVATION_INVALID_DOCUMENT_NUMBER_ERROR_CODE)

    base_time = request.args.get(Paquete.BASE_TIME_NAME)
    if base_time is not None:
        base_time = validate_datetime(base_time, Paquete.BASE_TIME_NAME,
                                      internal_code=PERSON_RESERVATION_INVALID_BASE_TIME_ERROR_CODE)

    # noinspection PyUnusedLocal
    def list_person_reservations_by_document_on_namespace(id_current_client):
        return get_person_reservations_by_client_and_document(client, document_type, document_number, base_time)

    return on_client_namespace(id_client, list_person_reservations_by_document_on_namespace,
                               view=PERSON_RESERVATIONS_VIEW_NAME,
                               action=Role.READ_ACTION)


def get_person_reservations_by_client_and_document(client, document_type, document_number, base_time):
    from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
    error = None
    try:
        person_reservations = _get_local_person_reservations_by_client_and_document(client, document_type,
                                                                                    document_number, base_time)
    except EntityDoesNotExists as e:
        person_reservations = []
        error = e
    if len(person_reservations) == 0 and client.servicoReservasExterno == COMPENSAR_RESERVATIONS_SERVICE_NAME and not has_person_reservations_by_document_number(document_number, base_time):
        if not has_person_reservations_by_document_number(document_number, base_time):
            error = None
            # Se debe usar a nivel de modulo para permitir mockearla en pruebas
            queryReservationsFromCompensarService.query_compensars_external_reservations_service(client.key.id(),
                                                                                                 document_number)
            person_reservations = _get_local_person_reservations_by_client_and_document(client, document_type,
                                                                                        document_number, base_time)
    if error is not None:
        raise error
    return person_reservations


def has_person_reservations_by_document_number(document_number, base_time):
    persons = Persona.list_by_document_number_async(document_number).get_result()
    person_reservations_async_lists = [ReservaPersona.list_by_id_person_async(person.key.id()) for person in persons]
    for person_reservations_async_list in person_reservations_async_lists:
        person_reservations_list = person_reservations_async_list.get_result()
        if base_time is not None:
            person_reservations_list = [person_reservation for person_reservation in person_reservations_list
                                        if person_reservation.fechaFinal >= base_time]
        if len(person_reservations_list) > 0:
            return True
    return False


def _get_local_person_reservations_by_client_and_document(client, document_type, document_number, base_time):
    person_without_type_document_async = None
    person_with_type_async = Persona.get_person_by_document_async(document_type, document_number)
    if client.servicoReservasExterno == COMPENSAR_RESERVATIONS_SERVICE_NAME and document_type != NO_DOCUMENT_DOCUMENT_TYPE:
        person_without_type_document_async = Persona.get_person_by_document_async(NO_DOCUMENT_DOCUMENT_TYPE, document_number)
    person_with_type = person_with_type_async.get_result()
    person_without_type = None
    if person_without_type_document_async is not None:
        person_without_type = person_without_type_document_async.get_result()
    if person_with_type is None and person_without_type is None:
        raise EntityDoesNotExists(u"Person[{0}={1}, {2}={3}]".format(Persona.DOCUMENT_TYPE_NAME,
                                                                     document_type,
                                                                     Persona.DOCUMENT_NUMBER_NAME,
                                                                     document_number),
                                  internal_code=PERSON_DOES_NOT_EXISTS_ERROR_CODE)

    person_reservations_with_type_async = None
    person_reservations_without_type_async = None
    if person_with_type is not None:
        person_reservations_with_type_async = ReservaPersona.list_by_id_person_async(person_with_type.key.id())
    if person_without_type is not None:
        person_reservations_without_type_async = ReservaPersona.list_by_id_person_async(person_without_type.key.id())

    person_reservations = []
    if person_reservations_with_type_async is not None:
        person_reservations = person_reservations_with_type_async.get_result()
        if base_time is not None:
            person_reservations = [person_reservation for person_reservation in person_reservations
                                   if person_reservation.fechaFinal >= base_time]
    if len(person_reservations) == 0 and person_reservations_without_type_async is not None:
        person_reservations = person_reservations_without_type_async.get_result()
        if base_time is not None:
            person_reservations = [person_reservation for person_reservation in person_reservations
                                   if person_reservation.fechaFinal >= base_time]

    return person_reservations


@app.route('/clients/<int:id_client>/persons-reservations-activations-per-user/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_person_reservations_activations(id_client):
    """
    Da las activaciones y desactivaciones realizadas por cada usuario.
    :param id_client: id del cliente asociado
           initial-time: Fecha y hora inicial a filtrar, opcional
           final-time: Fecha y hora inicial a filtrar, opcional
    :return: Lista de activaciones y desactivaciones realizadas por cada usuario.
    """
    id_client = validate_id_client(id_client)

    initial_time = request.args.get(ActivacionesReservaPersona.INITIAL_TIME_NAME)
    if initial_time is not None:
        initial_time = validate_datetime(initial_time, ActivacionesReservaPersona.INITIAL_TIME_NAME, allow_none=False,
                                         internal_code=PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_INITIAL_TIME_ERROR_CODE)

    final_time = request.args.get(ActivacionesReservaPersona.FINAL_TIME_NAME)
    if final_time is not None:
        final_time = validate_datetime(final_time, ActivacionesReservaPersona.FINAL_TIME_NAME, allow_none=False,
                                       internal_code=PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_FINAL_TIME_ERROR_CODE)

    # noinspection PyUnusedLocal
    def list_person_reservations_activations_on_namespace(id_current_client):
        activations = ActivacionesReservaPersona.list_by_times(initial_time, final_time)
        activations_by_user = dict()
        for activation in activations:
            if activation.usuario not in activations_by_user:
                activations_by_user[activation.usuario] = UserActivations(activation.usuario)
            activations_by_user[activation.usuario].add_activation(activation)
        return activations_by_user.values()

    return on_client_namespace(id_client, list_person_reservations_activations_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSON_RESERVATIONS_ACTIVATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_person_reservations_by_reservation(id_client, id_reservation):
    """
    Da las reserva de persona hijas de la reserva con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :return: Reserva de persona con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_person_reservations_by_reservation_on_namespace(id_current_client):
        reservation = Reserva.try_get_by_id(id_reservation)
        return ReservaPersona.list_by_id_reservation(reservation.key.id())

    return on_client_namespace(id_client, list_person_reservations_by_reservation_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSON_RESERVATIONS_VIEW_NAME)


def get_reservation_and_person_reservation_on_namespace_with_permission(id_client, id_reservation, id_person_reservation,
                                                                        action, view):
    person_reservation = None
    reservation = None
    validation_error = None
    try:
        reservation = Reserva.try_get_by_id(id_reservation)
        person_reservation = ReservaPersona.try_get_by_id_reservation(reservation.key.id(), id_person_reservation)
    except ValidationError as validation_error:
        pass
    if person_reservation is not None:
        # Only return if the user has the correct permission
        check_permissions_by_package_location(id_client, person_reservation.idPaqueteCompartido,
                                              action, view)
        return reservation, person_reservation
    else:
        # Only raise a validation error if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            action=action,
                            view=view)
        raise validation_error


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Da la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Reserva de persona con id dado
    """
    id_client = validate_id_client(id_client)

    def get_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              PERSON_RESERVATIONS_VIEW_NAME)
        return person_reservation

    return on_client_namespace(id_client, get_reservation_on_namespace, secured=False)


def check_if_it_is_possible_to_change_person_reservation(person_reservation,
                                                         internal_code_active_reservations=None,
                                                         internal_code_consumed_accesses=None,
                                                         internal_code_consumed_consumptions=None,
                                                         internal_code_topoffs_exists=None):
    if person_reservation.activo:
        raise ValidationError(u"Can not change an active person reservation.",
                              internal_code=internal_code_active_reservations)

    if PersonAccess.count_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id()) > 0:
        raise ValidationError(u"Can not change a person reservation with consumed accesses.",
                              internal_code=internal_code_consumed_accesses)

    if PersonConsumptionByAmount.count_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id()) > 0:
        raise ValidationError(u"Can not change a person reservation with amount consumptions.",
                              internal_code=internal_code_consumed_consumptions)

    if PersonConsumptionByMoney.count_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id()) > 0:
        raise ValidationError(u"Can not change a person reservation with money consumptions.",
                              internal_code=internal_code_consumed_consumptions)

    if AccessTopoff.count_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id()) > 0:
        raise ValidationError(u"Can not change a person reservation with topoffs.",
                              internal_code=internal_code_topoffs_exists)

    if AmountTopoff.count_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id()) > 0:
        raise ValidationError(u"Can not change a person reservation with topoffs.",
                              internal_code=internal_code_topoffs_exists)

    if MoneyTopoff.count_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id()) > 0:
        raise ValidationError(u"Can not change a person reservation with topoffs.",
                              internal_code=internal_code_topoffs_exists)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Elimina la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona a eliminar
    :return: Reserva de persona eliminada
    """
    id_client = validate_id_client(id_client)

    def delete_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.DELETE_ACTION,
                                                                                                              PERSON_RESERVATIONS_VIEW_NAME)
        if person_reservation.is_holder():
            raise ValidationError(u"Can not delete a Person Reservation of a holder, "
                                  u"must delete the associated Reservation instead.",
                                  internal_code=DELETE_HOLDER_PERSON_RESERVATION_ERROR_CODE)
        check_if_it_is_possible_to_change_person_reservation(person_reservation,
                                                             internal_code_active_reservations=DELETE_ACTIVE_PERSON_RESERVATION_ERROR_CODE,
                                                             internal_code_consumed_accesses=DELETE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE,
                                                             internal_code_consumed_consumptions=DELETE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE,
                                                             internal_code_topoffs_exists=DELETE_PERSON_RESERVATION_WITH_TOPOFFS_ERROR_CODE)

        person_reservation.mark_as_deleted()
        person_reservation.put()
        return person_reservation

    return on_client_namespace(id_client, delete_person_reservation_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Actualiza la reserva de persona con id dado con los parametros dados.
            id-person: int opcional, se crea una nueva persona si se omite
            initial-date: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] obligatorio
            final-date: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] obligatorio
            active: bool. True si se quiere activar el paquete, false si se quiere desactivar
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona a actualizar
    :return: Reserva de persona actualizada
    """
    id_client = validate_id_client(id_client)

    def update_person_reservation_on_namespace(id_current_client, id_person, package, _,
                                               initial_date, final_date):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.UPDATE_ACTION,
                                                                                                              PERSON_RESERVATIONS_VIEW_NAME)

        active = _get_and_validate_active_field(reservation)
        _check_there_are_no_person_reservations_for_person_on_dates(id_person, initial_date, final_date,
                                                                    person_reservation)
        check_if_it_is_possible_to_change_person_reservation(person_reservation,
                                                             internal_code_active_reservations=UPDATE_ACTIVE_PERSON_RESERVATION_ERROR_CODE,
                                                             internal_code_consumed_accesses=UPDATE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE,
                                                             internal_code_consumed_consumptions=UPDATE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE,
                                                             internal_code_topoffs_exists=UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_ERROR_CODE)

        base_price, tax_rate = get_base_price_and_tax_rate_from_package(id_person, package)

        person_reservation.update(package, id_person, active, base_price, tax_rate, initial_date, final_date)
        return person_reservation

    return _get_and_validate_person_reservation_json_params(id_client,
                                                            id_reservation,
                                                            update_person_reservation_on_namespace,
                                                            Role.UPDATE_ACTION)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/available-funds/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_available_funds(id_client, id_reservation, id_person_reservation):
    """
    Da una lista con los fondos disponibles de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Lista de fondos disponibles de la reserva
    """
    id_client = validate_id_client(id_client)

    def get_available_funds_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              AVAILABLE_FUNDS_VIEW_NAME)
        package = person_reservation.try_get_package()
        balances = ReservationsBalanceCalculator.calculate_funds_for_person_reservation(package, reservation,
                                                                                        person_reservation,
                                                                                        is_balance=False)

        return balances.get_balance_for_person_reservation(person_reservation).get_balance_details_as_list()
    return on_client_namespace(id_client, get_available_funds_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/balance/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_balance(id_client, id_reservation, id_person_reservation):
    """
    Da una lista con los saldos de la reserva de persona con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Lista de saldos de la reserva
    """
    id_client = validate_id_client(id_client)

    def get_available_funds_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              PERSON_RESERVATION_BALANCE_VIEW_NAME)
        package = person_reservation.try_get_package()
        balances = ReservationsBalanceCalculator.calculate_funds_for_person_reservation(package, reservation,
                                                                                        person_reservation,
                                                                                        is_balance=True)

        return balances.get_balance_for_person_reservation(person_reservation).get_balance_details_as_list()
    return on_client_namespace(id_client, get_available_funds_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/',
           methods=['PATCH'], strict_slashes=False)
@with_json_body
def activate_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Activa o desactiva la reserva de persona con id dado.
        Parametros esperados:
            active: bool. True si se quiere activar el paquete, false si no
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Reserva de persona con id dado modificada
    """
    id_client = validate_id_client(id_client)

    def get_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.UPDATE_ACTION,
                                                                                                              PERSON_RESERVATIONS_VIEW_NAME)

        active = _get_and_validate_active_field(reservation)

        if active and person_reservation.activo:
            raise ValidationError(u"Can not activate an already active person reservation.",
                                  internal_code=PERSON_RESERVATION_TRYING_TO_ACTIVATE_ALREADY_ACTIVE_PERSON_RESERVATION_ERROR_CODE)

        return person_reservation.change_active_status(active)

    return on_client_namespace(id_client, get_reservation_on_namespace, secured=False)


def _get_and_validate_person_reservation_json_params(id_client, id_reservation, on_namespace_callback, action):
    id_client = validate_id_client(id_client)
    id_package = request.json.get(ReservaPersona.PACKAGE_ID_NAME)

    def _get_and_validate_person_reservation_json_params_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)
        reservation = Reserva.try_get_by_id(id_reservation)

        id_person = request.json.get(ReservaPersona.PERSON_ID_NAME)
        if id_person is None:
            person = Persona.create_phantom_person(id_current_client)
            id_person = person.key.id()
        else:
            id_person = validate_id_person(id_person)

        initial_date = request.json.get(ReservaPersona.INITIAL_DATE_NAME)
        initial_date = validate_datetime(initial_date, ReservaPersona.INITIAL_DATE_NAME, allow_none=False,
                                         internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_ERROR_CODE)

        final_date = request.json.get(ReservaPersona.FINAL_DATE_NAME)
        final_date = validate_datetime(final_date, ReservaPersona.FINAL_DATE_NAME, allow_none=False,
                                       internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_ERROR_CODE)

        if initial_date > final_date:
            raise ValidationError(u"The value of {0} [{1}] should be greater or equals than the value of {2} [{3}]."
                                  .format(ReservaPersona.FINAL_DATE_NAME, final_date.strftime(DEFAULT_DATETIME_FORMAT),
                                          ReservaPersona.INITIAL_DATE_NAME, initial_date.strftime(DEFAULT_DATETIME_FORMAT)),
                                  internal_code=PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FINAL_ERROR_CODE)

        if initial_date < package.validoDesde:
            raise ValidationError(u"The value of {0} [{1}] should be greater or equals "
                                  u"than the value of {2} field {3} [{4}]."
                                  .format(ReservaPersona.INITIAL_DATE_NAME, initial_date.strftime(DEFAULT_DATETIME_FORMAT),
                                          u"Package", Paquete.VALID_FROM_NAME,
                                          package.validoDesde.strftime(DEFAULT_DATETIME_FORMAT)),
                                  internal_code=PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FROM_ERROR_CODE)

        if final_date > package.validoHasta:
            raise ValidationError(u"The value of {0} [{1}] should be smaller or equals "
                                  u"than the value of {2} field {3} [{4}]."
                                  .format(ReservaPersona.FINAL_DATE_NAME, final_date.strftime(DEFAULT_DATETIME_FORMAT),
                                          u"Package", Paquete.VALID_THROUGH_NAME,
                                          package.validoHasta.strftime(DEFAULT_DATETIME_FORMAT)),
                                  internal_code=PERSON_RESERVATION_RANGE_OF_DATES_THROUGH_GREATER_THAN_FINAL_ERROR_CODE)

        if reservation.idEventoSocial is not None:
            social_event = EventoSocial.get_by_id(reservation.idEventoSocial)
            if social_event is not None:
                if initial_date < social_event.fechaInicial:
                    raise ValidationError(u"The value of {0} [{1}] should be greater or equals "
                                          u"than the value of {2} field {3} [{4}]."
                                          .format(ReservaPersona.INITIAL_DATE_NAME, initial_date.strftime(DEFAULT_DATETIME_FORMAT),
                                                  u"Social Event", EventoSocial.INITIAL_DATE_NAME,
                                                  social_event.fechaInicial.strftime(DEFAULT_DATETIME_FORMAT)),
                                          internal_code=PERSON_RESERVATION_INVALID_RANGE_OF_DATES_INITIAL_SMALLER_THAN_EVENT_INITIAL_ERROR_CODE)
                elif final_date > social_event.fechaFinal:
                    raise ValidationError(u"The value of {0} [{1}] should be smaller or equals "
                                          u"than the value of {2} field {3} [{4}]."
                                          .format(ReservaPersona.FINAL_DATE_NAME, final_date.strftime(DEFAULT_DATETIME_FORMAT),
                                                  u"Social Event", EventoSocial.FINAL_DATE_NAME,
                                                  social_event.fechaFinal.strftime(DEFAULT_DATETIME_FORMAT)),
                                          internal_code=PERSON_RESERVATION_INVALID_RANGE_OF_DATES_FINAL_BIGGER_THAN_EVENT_FINAL_ERROR_CODE)

        return on_namespace_callback(id_current_client, id_person, package, reservation, initial_date, final_date)

    check_permissions_by_package_location(id_client, id_package, action, PERSON_RESERVATIONS_VIEW_NAME)
    return on_client_namespace(id_client, _get_and_validate_person_reservation_json_params_on_namespace, secured=False)


def _get_and_validate_active_field(reservation):
    active = request.json.get(ReservaPersona.ACTIVE_NAME)
    active = validate_bool_not_empty(active, ReservaPersona.ACTIVE_NAME,
                                     internal_code=PERSON_RESERVATION_INVALID_ACTIVE_ERROR_CODE)
    if active and not reservation.is_paid():
        raise ValidationError(u"Can not activate unpaid reservation.",
                              internal_code=PERSON_RESERVATION_TRYING_TO_ACTIVATE_UNPAID_RESERVATION_ERROR_CODE)
    return active


def _check_there_are_no_person_reservations_for_person_on_dates(id_person, initial_date, final_date,
                                                                current_person_reservation=None):
    previous_reservations = ReservaPersona.list_by_range_of_times_and_id_person(id_person, initial_date, final_date)
    if current_person_reservation is not None:
        previous_reservations = [previous_reservation for previous_reservation in previous_reservations
                                 if previous_reservation.key != current_person_reservation.key]
    if len(previous_reservations) > 0:
        raise ValidationError(u"There is already a person reservation for person with id {0} in the given dates"
                              .format(id_person),
                              internal_code=PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_ERROR_CODE)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
