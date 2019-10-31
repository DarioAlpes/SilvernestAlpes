# -*- coding: utf-8 -*
from google.appengine.ext import ndb
import logging
import time as time_measure
from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.persons.Persona import Persona
from CJM.entidades.reservas.AccessTopoff import AccessTopoff
from CJM.entidades.reservas.ActivacionesReservaPersona import ActivacionesReservaPersona
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.ReservaPersona import ReservaPersona, UserActivations
from CJM.entidades.reservas.balance.ReservationsBalanceCalculator import ReservationsBalanceCalculator
from CJM.entidades.reservas.orders.Order import Order
from CJM.entidades.reservas.orders.PersonAccess import PersonAccess
from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.entidades.reservas.orders.PersonConsumptionByMoney import PersonConsumptionByMoney
from CJM.services.reservas import PERSONS_RESERVATIONS_KIND_NAME, get_map_function_from_entity_to_entity_with_user, \
    ACCESS_TOPOFFS_KIND_NAME, AMOUNT_TOPOFFS_KIND_NAME, MONEY_TOPOFFS_KIND_NAME, PERSON_ACCESSES_KIND_NAME, \
    PERSON_ORDERS_KIND_NAME, PERSON_KIND_NAME, PERSON_AMOUNT_CONSUMPTIONS_KIND_NAME, PERSON_MONEY_CONSUMPTIONS_KIND_NAME
from CJM.services.validations import validate_money, validate_document_number, validate_document_type, \
    validate_reservation_number, RESERVATION_DOES_NOT_EXISTS_ERROR_CODE, RESERVATION_INVALID_PAYMENT_ERROR_CODE, \
    DELETE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE, DELETE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE, \
    DELETE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE, RESERVATION_INVALID_RESERVATION_NUMBER_ERROR_CODE, \
    RESERVATION_INVALID_DOCUMENT_TYPE_ERROR_CODE, RESERVATION_INVALID_DOCUMENT_NUMBER_ERROR_CODE, \
    RESERVATION_INVALID_TRANSACTION_NUMBER_ERROR_CODE, \
    RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_ERROR_CODE, validate_transaction_number, \
    DELETE_RESERVATION_WITH_TOPOFFS_ERROR_CODE, UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE, \
    UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE, UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE, \
    UPDATE_RESERVATION_WITH_TOPOFFS_ERROR_CODE, RESERVATION_INVALID_BASE_TIME_ERROR_CODE, validate_id_social_event, \
    RESERVATION_INVALID_INITIAL_TIME_ERROR_CODE, RESERVATION_INVALID_FINAL_TIME_ERROR_CODE, \
    RESERVATION_INVALID_INCLUDE_CHILDREN_ERROR_CODE
from commons.entidades.Cliente import Cliente
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError, EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_datetime, validate_bool_not_empty
from flask import request, Blueprint

RESERVATIONS_VIEW_NAME = "reservation"
RESERVATION_BALANCE_VIEW_NAME = "reservation-balance"
app = Blueprint(RESERVATIONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/reservations/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_package_reservation_from_json(id_client):
    """
    Crea una reserva en el namespace del cliente id_client
        Parametros esperados:
            number-persons: int opcional, se supone 1 si se omite
            payment: float opcional, se supone 0 si se omite
            transaction-number: str, opcional
            id-social-event: int opcional
    :param id_client: id del cliente asociado
    :return: Reserva creada
    """
    def create_package_reservation_from_json_on_namespace(id_current_client, payment,
                                                          transaction_number, id_event):
        return Reserva.create(id_current_client, payment, transaction_number, id_event)

    return _get_and_validate_reservation_params(create_package_reservation_from_json_on_namespace, id_client,
                                                Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/reservations-by-document/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_reservations_by_document(id_client):
    """
    Lista las reservas del paquete apra las que existe una reserva de persona de la persona dada
    del cliente correspondiente.
        Parametros esperados en el query string:
            document-type: str en {"CC", "TI", "CE"}
            document-number: str
            base-time: str opcional en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
    :param id_client: id del cliente asociado
    :return: Lista de reservas de personas del paquete
    """
    from CJM.services.reservas.reservaPersonaView import get_person_reservations_by_client_and_document
    id_client = validate_id_client(id_client)
    client = Cliente.get_by_id(id_client)

    # noinspection PyUnusedLocal
    def list_reservations_by_document_on_namespace(id_current_client):
        document_type = request.args.get(Persona.DOCUMENT_TYPE_NAME)
        document_type = validate_document_type(document_type, Persona.DOCUMENT_TYPE_NAME,
                                               internal_code=RESERVATION_INVALID_DOCUMENT_TYPE_ERROR_CODE)

        document_number = request.args.get(Persona.DOCUMENT_NUMBER_NAME)
        document_number = validate_document_number(document_number, Persona.DOCUMENT_NUMBER_NAME,
                                                   internal_code=RESERVATION_INVALID_DOCUMENT_NUMBER_ERROR_CODE)

        base_time = request.args.get(Paquete.BASE_TIME_NAME)
        if base_time is not None:
            base_time = validate_datetime(base_time, Paquete.BASE_TIME_NAME,
                                          internal_code=RESERVATION_INVALID_BASE_TIME_ERROR_CODE)

        person_reservations = get_person_reservations_by_client_and_document(client, document_type, document_number,
                                                                             base_time)

        reservations_keys = {Reserva.get_key_from_person_reservation(person_reservation)
                             for person_reservation in person_reservations}
        reservations = ndb.get_multi(reservations_keys)
        return [reservation for reservation in reservations
                if reservation is not None and not reservation.eliminada]

    return on_client_namespace(id_client, list_reservations_by_document_on_namespace,
                               view=RESERVATIONS_VIEW_NAME,
                               action=Role.READ_ACTION)


@app.route('/clients/<int:id_client>/reservation-by-number/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_reservation_by_number(id_client):
    """
    Lista las reservas del paquete apra las que existe una reserva de persona de la persona dada
    del cliente correspondiente.
        Parametros esperados en el query string:
            reservation-number: str en en formato #Rxxxx (el número de reserva de una reserva)
    :param id_client: id del cliente asociado
    :return: Lista de reservas de personas del paquete
    """
    id_client = validate_id_client(id_client)
    reservation_number = request.args.get(Reserva.RESERVATION_NUMBER_NAME)
    reservation_number = validate_reservation_number(reservation_number, Reserva.RESERVATION_NUMBER_NAME,
                                                     parse_to_int=True,
                                                     internal_code=RESERVATION_INVALID_RESERVATION_NUMBER_ERROR_CODE)

    # noinspection PyUnusedLocal
    def get_reservation_by_number_on_namespace(id_current_client):
        reservation = Reserva.get_by_reservation_number(reservation_number)

        if reservation is None:
            on_client_namespace(id_client, _dummy_function_for_login_check,
                                action=Role.READ_ACTION,
                                view=RESERVATIONS_VIEW_NAME)
            raise EntityDoesNotExists(u"Reservation[{0}:{1}]".format(Reserva.RESERVATION_NUMBER_NAME,
                                                                     reservation_number),
                                      internal_code=RESERVATION_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return reservation

    return on_client_namespace(id_client, get_reservation_by_number_on_namespace,
                               action=Role.READ_ACTION,
                               view=RESERVATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_reservations(id_client):
    """
    Lista las reservas del cliente correspondiente.
    :param id_client: id del cliente asociado
    :return: Lista de reservas del paquete
    """
    id_client = validate_id_client(id_client)
    initial_time = request.args.get(Reserva.INITIAL_TIME_NAME)
    if initial_time is not None:
        initial_time = validate_datetime(initial_time, Reserva.INITIAL_TIME_NAME,
                                         internal_code=RESERVATION_INVALID_INITIAL_TIME_ERROR_CODE)
    final_time = request.args.get(Reserva.FINAL_TIME_NAME)
    if final_time is not None:
        final_time = validate_datetime(final_time, Reserva.FINAL_TIME_NAME,
                                       internal_code=RESERVATION_INVALID_FINAL_TIME_ERROR_CODE)

    include_children = request.args.get(Reserva.INCLUDE_CHILDREN_NAME)
    if include_children is not None:
        include_children = validate_bool_not_empty(include_children, Reserva.INCLUDE_CHILDREN_NAME, allow_string=True,
                                                   internal_code=RESERVATION_INVALID_INCLUDE_CHILDREN_ERROR_CODE)
    else:
        include_children = False

    # noinspection PyUnusedLocal
    def list_reservations_on_namespace(id_current_client):
        if initial_time is None and final_time is None:
            reservations_keys = Reserva.query().fetch(keys_only=True)
        else:
            person_reservations = ReservaPersona.list_by_range_of_times(initial_time, final_time)
            reservations_keys = {Reserva.get_key_from_person_reservation(person_reservation)
                                 for person_reservation in person_reservations}
        return get_reservation_from_reservation_keys_based_on_include_children(reservations_keys,
                                                                               include_children)
    return on_client_namespace(id_client, list_reservations_on_namespace,
                               action=Role.READ_ACTION,
                               view=RESERVATIONS_VIEW_NAME)


def _get_reservations_map_function_based_on_include_children(include_children):
    reservation_map_to_children_function = get_map_function_from_entity_to_entity_with_user({PERSONS_RESERVATIONS_KIND_NAME},
                                                                                            set())

    mapper_person_reservations = get_map_function_from_entity_to_entity_with_user({ACCESS_TOPOFFS_KIND_NAME,
                                                                                   AMOUNT_TOPOFFS_KIND_NAME,
                                                                                   MONEY_TOPOFFS_KIND_NAME,
                                                                                   PERSON_ACCESSES_KIND_NAME,
                                                                                   PERSON_ORDERS_KIND_NAME,
                                                                                   UserActivations.ACTIVATIONS_NAME,
                                                                                   UserActivations.DEACTIVATIONS_NAME},
                                                                                  {PERSON_KIND_NAME})

    mapper_orders = get_map_function_from_entity_to_entity_with_user({PERSON_AMOUNT_CONSUMPTIONS_KIND_NAME,
                                                                      PERSON_MONEY_CONSUMPTIONS_KIND_NAME},
                                                                     set())

    def map_reservation_with_children_if_required(reservation):
        if reservation is not None and not reservation.eliminada:
            if include_children:
                reservation_with_children = reservation_map_to_children_function(reservation).get_result()

                person_reservations = ReservaPersona.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                person_reservations_activations = ActivacionesReservaPersona.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                accesses = PersonAccess.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                access_topoffs = AccessTopoff.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                money_topoffs = MoneyTopoff.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                amount_topoffs = AmountTopoff.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                orders = Order.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                amount_consumptions = PersonConsumptionByAmount.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()
                money_consumptions = PersonConsumptionByMoney.list_by_id_reservation_ordered_by_parents_without_fetch(reservation.key.id()).fetch_async()

                person_reservations_with_children = _map_person_reservations_with_childrens(mapper_orders,
                                                                                            mapper_person_reservations,
                                                                                            person_reservations,
                                                                                            person_reservations_activations,
                                                                                            accesses,
                                                                                            orders,
                                                                                            amount_consumptions,
                                                                                            money_consumptions,
                                                                                            access_topoffs,
                                                                                            money_topoffs,
                                                                                            amount_topoffs)

                reservation_with_children.set_children(PERSONS_RESERVATIONS_KIND_NAME,
                                                       person_reservations_with_children)
                return reservation_with_children
            else:
                return reservation
        else:
            return None
    return map_reservation_with_children_if_required


def _map_person_reservations_with_childrens(mapper_orders, mapper_person_reservations, person_reservations,
                                            person_reservations_activations, accesses, orders,
                                            amount_consumptions, money_consumptions, access_topoffs, money_topoffs,
                                            amount_topoffs):
    person_reservations = iter(person_reservations.get_result())
    all_person_reservations_activations = iter(person_reservations_activations.get_result())
    all_accesses = iter(accesses.get_result())
    all_orders = iter(orders.get_result())
    all_amount_consumptions = iter(amount_consumptions.get_result())
    all_money_consumptions = iter(money_consumptions.get_result())
    all_access_topoffs = iter(access_topoffs.get_result())
    all_money_topoffs = iter(money_topoffs.get_result())
    all_amount_topoffs = iter(amount_topoffs.get_result())
    current_access = None
    current_order = None
    current_amount_consumption = None
    current_money_consumption = None
    current_activation = None
    current_amount_topoff = None
    current_access_topoff = None
    current_money_topoff = None
    person_reservations_with_children = []
    for person_reservation in person_reservations:
        person_async = Persona.get_by_id_async(person_reservation.idPersona)
        person_reservation_with_children_async = mapper_person_reservations(person_reservation)
        id_person_reservation = person_reservation.key.id()
        current_activation, activations, deactivations = _get_next_activation_and_current_lists_from_person_reservation(current_activation,
                                                                                                                        all_person_reservations_activations,
                                                                                                                        id_person_reservation)
        current_access, accesses = _get_next_entity_with_person_reservation_and_current_list_from_person_reservation(None,
                                                                                                                     current_access,
                                                                                                                     all_accesses,
                                                                                                                     id_person_reservation)
        current_access_topoff, access_topoffs = _get_next_topoff_and_current_list_from_person_reservation(current_access_topoff,
                                                                                                          all_access_topoffs,
                                                                                                          id_person_reservation)
        current_money_topoff, money_topoffs = _get_next_topoff_and_current_list_from_person_reservation(current_money_topoff,
                                                                                                        all_money_topoffs,
                                                                                                        id_person_reservation)
        current_amount_topoff, amount_topoffs = _get_next_topoff_and_current_list_from_person_reservation(current_amount_topoff,
                                                                                                          all_amount_topoffs,
                                                                                                          id_person_reservation)

        current_order, current_amount_consumption, current_money_consumption, orders = _get_next_order_and_current_list_from_person_reservation(mapper_orders,
                                                                                                                                                current_order,
                                                                                                                                                current_amount_consumption,
                                                                                                                                                current_money_consumption,
                                                                                                                                                all_orders,
                                                                                                                                                all_amount_consumptions,
                                                                                                                                                all_money_consumptions,
                                                                                                                                                id_person_reservation)
        person_reservation_with_children = person_reservation_with_children_async.get_result()
        person_reservation_with_children.set_children(UserActivations.ACTIVATIONS_NAME, activations)
        person_reservation_with_children.set_children(UserActivations.DEACTIVATIONS_NAME, deactivations)
        person_reservation_with_children.set_children(PERSON_ACCESSES_KIND_NAME,
                                                      [access for access in accesses])
        person_reservation_with_children.set_children(PERSON_ORDERS_KIND_NAME,
                                                      [order for order in orders])
        person_reservation_with_children.set_children(ACCESS_TOPOFFS_KIND_NAME,
                                                      [topoff for topoff in access_topoffs])
        person_reservation_with_children.set_children(MONEY_TOPOFFS_KIND_NAME,
                                                      [topoff for topoff in money_topoffs])
        person_reservation_with_children.set_children(AMOUNT_TOPOFFS_KIND_NAME,
                                                      [topoff for topoff in amount_topoffs])
        person_reservation_with_children.set_single_child(PERSON_KIND_NAME, person_async.get_result())
        person_reservations_with_children.append(person_reservation_with_children)
    return person_reservations_with_children


def _get_next_order_and_current_list_from_person_reservation(mapper_orders, current_order, current_amount_consumption,
                                                             current_money_consumption, all_orders,
                                                             all_amount_consumptions, all_money_consumptions,
                                                             id_person_reservation):

    current_order, orders_async = _get_next_entity_with_person_reservation_and_current_list_from_person_reservation(mapper_orders,
                                                                                                                    current_order,
                                                                                                                    all_orders,
                                                                                                                    id_person_reservation)

    if current_amount_consumption is None:
        current_amount_consumption = next(all_amount_consumptions, None)

    if current_money_consumption is None:
        current_money_consumption = next(all_money_consumptions, None)

    # Avanzar iterador hasta llegar a o superar el id de la reserva persona actual
    while current_amount_consumption is not None and current_amount_consumption.idReservaPersona < id_person_reservation:
        current_amount_consumption = next(all_amount_consumptions, None)

    # Avanzar iterador hasta llegar a o superar el id de la reserva persona actual
    while current_money_consumption is not None and current_money_consumption.idReservaPersona < id_person_reservation:
        current_money_consumption = next(all_money_consumptions, None)

    orders = []
    for order_async in orders_async:
        order_with_children = order_async.get_result()
        id_order = order_with_children.entity.key.id()
        current_amount_consumption, amount_consumptions = _get_next_entity_with_order_and_current_list_from_order(current_amount_consumption,
                                                                                                                  all_amount_consumptions,
                                                                                                                  id_order)
        current_money_consumption, money_consumptions = _get_next_entity_with_order_and_current_list_from_order(current_money_consumption,
                                                                                                                all_money_consumptions,
                                                                                                                id_order)
        order_with_children.set_children(PERSON_AMOUNT_CONSUMPTIONS_KIND_NAME,
                                         [consumption for consumption in amount_consumptions])
        order_with_children.set_children(PERSON_MONEY_CONSUMPTIONS_KIND_NAME,
                                         [consumption for consumption in money_consumptions])
        orders.append(order_with_children)

    return current_order, current_amount_consumption, current_amount_consumption, orders


def _get_next_activation_and_current_lists_from_person_reservation(current_activation,
                                                                   all_person_reservations_activations,
                                                                   id_person_reservation):
    activations = []
    deactivations = []
    if current_activation is None:
        current_activation = next(all_person_reservations_activations, None)
    # Avanzar iterador hasta llegar a o superar el id de la reserva persona actual
    while current_activation is not None and current_activation.idReservaPersona < id_person_reservation:
        current_activation = next(all_person_reservations_activations, None)

    while current_activation is not None and current_activation.idReservaPersona == id_person_reservation:
        if current_activation.esActivacion:
            activations.append(current_activation)
        else:
            deactivations.append(current_activation)
        current_activation = next(all_person_reservations_activations, None)
    return current_activation, activations, deactivations


def _get_next_entity_with_order_and_current_list_from_order(current_entity, all_entity_generator, id_order):
    entities = []
    if current_entity is None:
        current_entity = next(all_entity_generator, None)
    # Avanzar iterador hasta llegar a o superar el id de la orden
    while current_entity is not None and current_entity.idOrden < id_order:
        current_entity = next(all_entity_generator, None)

    while current_entity is not None and current_entity.idOrden == id_order:
        entities.append(current_entity)
        current_entity = next(all_entity_generator, None)
    return current_entity, entities


def _get_next_entity_with_person_reservation_and_current_list_from_person_reservation(mapper_leaf_entities,
                                                                                      current_entity,
                                                                                      all_entity_generator,
                                                                                      id_person_reservation):
    entities = []
    if current_entity is None:
        current_entity = next(all_entity_generator, None)
    # Avanzar iterador hasta llegar a o superar el id de la reserva persona actual
    while current_entity is not None and current_entity.idReservaPersona < id_person_reservation:
        current_entity = next(all_entity_generator, None)

    while current_entity is not None and current_entity.idReservaPersona == id_person_reservation:
        if mapper_leaf_entities is None:
            entities.append(current_entity)
        else:
            entities.append(mapper_leaf_entities(current_entity))
        current_entity = next(all_entity_generator, None)
    return current_entity, entities


def _get_next_topoff_and_current_list_from_person_reservation(current_topoff, all_topoffs_generator,
                                                              id_person_reservation):
    topoffs = []
    if current_topoff is None:
        current_topoff = next(all_topoffs_generator, None)
    # Avanzar iterador hasta llegar a o superar el id de la reserva persona actual
    while current_topoff is not None and current_topoff.idReservacionPersona < id_person_reservation:
        current_topoff = next(all_topoffs_generator, None)

    while current_topoff is not None and current_topoff.idReservacionPersona == id_person_reservation:
        topoffs.append(current_topoff)
        current_topoff = next(all_topoffs_generator, None)
    return current_topoff, topoffs


def get_reservation_from_reservation_keys_based_on_include_children(reservations_keys, include_children):
    initial_all = time_measure.time()
    map_function = _get_reservations_map_function_based_on_include_children(include_children)
    reservations_async = [reservation_key.get_async() for reservation_key in reservations_keys]

    @ndb.tasklet
    def get_and_map_reservation_from_async(reservation_async):
        reservation = reservation_async.get_result()
        reservation_mapped = map_function(reservation)
        raise ndb.Return(reservation_mapped)
    reservations = map(get_and_map_reservation_from_async, reservations_async)
    reservations_sync = (reservation.get_result() for reservation in reservations)
    reservations_filtered = (reservation for reservation in reservations_sync if reservation is not None)
    final_all = time_measure.time()
    logging.info("Tiempo maps: " + str(final_all - initial_all))
    return reservations_filtered


def _get_person_by_person_reservation_key_from_async_lists(person_reservations_async_lists):
    persons_async_dict = dict()
    for person_reservation_async_list in person_reservations_async_lists:
        person_reservation_list = person_reservation_async_list.get_result()
        for person_reservation in person_reservation_list:
            persons_async_dict[person_reservation.key] = Persona.get_by_id_async(person_reservation.idPersona)
    return persons_async_dict


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_reservation(id_client, id_reservation):
    """
    Da la reserva con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociado
    :return: Reserva con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_reservation_on_namespace(id_current_client):
        return Reserva.try_get_by_id(id_reservation)

    return on_client_namespace(id_client, get_reservation_on_namespace,
                               action=Role.READ_ACTION,
                               view=RESERVATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_reservation(id_client, id_reservation):
    """
    Actualiza la reserva con id dado con los parámetros dados.
        Parametros esperados:
            number-persons: int opcional, se supone 1 si se omite
            payment: float opcional, se supone 0 si se omite
            transaction-number: str, opcional
            id-social-event: int opcional
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva a actualizar
    :return: Reserva actualizada
    """
    from CJM.services.reservas.reservaPersonaView import check_if_it_is_possible_to_change_person_reservation

    # noinspection PyUnusedLocal
    def update_package_reservation_from_validated_parameters(current_id_client, payment, transaction_number, id_event):

        reservation = Reserva.try_get_by_id(id_reservation)

        person_reservations = ReservaPersona.list_by_id_reservation(reservation.key.id())
        number_of_person_reservations = len(person_reservations)

        for person_reservation in person_reservations:
            check_if_it_is_possible_to_change_person_reservation(person_reservation,
                                                                 internal_code_active_reservations=UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE,
                                                                 internal_code_consumed_accesses=UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE,
                                                                 internal_code_consumed_consumptions=UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE,
                                                                 internal_code_topoffs_exists=UPDATE_RESERVATION_WITH_TOPOFFS_ERROR_CODE)

        reservation.update(payment, transaction_number, id_event)
        return reservation

    return _get_and_validate_reservation_params(update_package_reservation_from_validated_parameters, id_client,
                                                Role.UPDATE_ACTION)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_reservation(id_client, id_reservation):
    """
    Elimina la reserva con id dado y todas las reservas de persona asociadas.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva a eliminar
    :return: Reserva eliminada
    """
    from CJM.services.reservas.reservaPersonaView import check_if_it_is_possible_to_change_person_reservation
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_reservation_on_namespace(id_current_client):
        reservation = Reserva.try_get_by_id(id_reservation)
        entities_to_delete = [reservation]
        person_reservations = ReservaPersona.list_by_id_reservation(reservation.key.id())
        for person_reservation in person_reservations:
            check_if_it_is_possible_to_change_person_reservation(person_reservation,
                                                                 internal_code_active_reservations=DELETE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE,
                                                                 internal_code_consumed_accesses=DELETE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE,
                                                                 internal_code_consumed_consumptions=DELETE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE,
                                                                 internal_code_topoffs_exists=DELETE_RESERVATION_WITH_TOPOFFS_ERROR_CODE)
            entities_to_delete.append(person_reservation)

        for entity in entities_to_delete:
            entity.mark_as_deleted()
        ndb.put_multi(entities_to_delete)
        return reservation

    return on_client_namespace(id_client, delete_reservation_on_namespace,
                               action=Role.DELETE_ACTION,
                               view=RESERVATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/',
           methods=['PATCH'], strict_slashes=False)
@with_json_body
def change_package_reservation_payment(id_client, id_reservation):
    """
    Crambia el valor pagado de la reserva con id dado por el recibido por parámetro. Permite marcar reservas pagadas
    como no pagadas.
        Parametros esperados:
            payment: float opcional, se supone 0 si se omite
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :return: Reserva creada
    """
    from CJM.services.reservas.reservaPersonaView import check_if_it_is_possible_to_change_person_reservation

    payment = request.json.get(Reserva.PAYMENT_NAME)
    transaction_number = request.json.get(Reserva.TRANSACTION_NUMBER_NAME)

    payment, transaction_number = _validate_and_return_payment_info(payment, transaction_number)

    # noinspection PyUnusedLocal
    def change_package_reservation_payment_on_namespace(id_current_client):
        reservation = Reserva.try_get_by_id(id_reservation)
        person_reservations = ReservaPersona.list_by_id_reservation(reservation.key.id())
        for person_reservation in person_reservations:
            check_if_it_is_possible_to_change_person_reservation(person_reservation,
                                                                 internal_code_active_reservations=UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE,
                                                                 internal_code_consumed_accesses=UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE,
                                                                 internal_code_consumed_consumptions=UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE,
                                                                 internal_code_topoffs_exists=UPDATE_RESERVATION_WITH_TOPOFFS_ERROR_CODE)
        reservation.update_payment_info(payment, transaction_number)
        return reservation

    return on_client_namespace(id_client, change_package_reservation_payment_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=RESERVATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/balance/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_reservation_balance(id_client, id_reservation):
    """
    Da el balance de la reserva. Corresponde a la lista de balances de las reservas de persona hijo.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :return: Balance de la reserva
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_reservation_on_namespace(id_current_client):
        reservation = Reserva.try_get_by_id(id_reservation)
        balances = ReservationsBalanceCalculator.calculate_funds_for_reservation(reservation,
                                                                                 is_balance=True)

        return balances.get_balances_as_list()
    return on_client_namespace(id_client, get_reservation_on_namespace,
                               action=Role.READ_ACTION,
                               view=RESERVATION_BALANCE_VIEW_NAME)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass


def _get_and_validate_reservation_params(callback_function, id_client, action):
    id_client = validate_id_client(id_client)

    def _get_and_validate_reservation_params_on_namespace(id_current_client):
        payment = request.json.get(Reserva.PAYMENT_NAME)
        transaction_number = request.json.get(Reserva.TRANSACTION_NUMBER_NAME)
        payment, transaction_number = _validate_and_return_payment_info(payment, transaction_number)

        id_social_event = request.json.get(Reserva.SOCIAL_EVENT_ID_NAME)
        if id_social_event is not None:
            validate_id_social_event(id_social_event)

        return callback_function(id_current_client, payment, transaction_number, id_social_event)

    return on_client_namespace(id_client, _get_and_validate_reservation_params_on_namespace,
                               action=action,
                               view=RESERVATIONS_VIEW_NAME)


def _validate_and_return_payment_info(payment, transaction_number):
    if payment is None:
        payment = 0
    else:
        payment = validate_money(payment, Reserva.PAYMENT_NAME, allow_zero=True,
                                 internal_code=RESERVATION_INVALID_PAYMENT_ERROR_CODE)

    if transaction_number is not None:
        transaction_number = validate_transaction_number(transaction_number, Reserva.TRANSACTION_NUMBER_NAME,
                                                         internal_code=RESERVATION_INVALID_TRANSACTION_NUMBER_ERROR_CODE)

    if transaction_number is None and payment != 0:
        raise ValidationError(u"The field {0} must 0 if field {1} is null.".format(Reserva.PAYMENT_NAME,
                                                                                   Reserva.TRANSACTION_NUMBER_NAME),
                              internal_code=RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_ERROR_CODE)

    return payment, transaction_number
