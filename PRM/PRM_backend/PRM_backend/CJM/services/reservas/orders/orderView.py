# -*- coding: utf-8 -*
import itertools

from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.orders.Order import Order
from CJM.entidades.reservas.orders.PastTransactionKey import PastTransactionKey
from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.entidades.reservas.orders.PersonConsumptionByMoney import PersonConsumptionByMoney
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.Moneda import Moneda
from CJM.entidades.eventos.Compra import Compra
from CJM.entidades.eventos.Devolucion import Devolucion
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.services.reservas.reservaPersonaView import \
    get_reservation_and_person_reservation_on_namespace_with_permission
from CJM.services.validations import validate_amount, validate_person_reservation_keys_list, validate_id_skus_list, \
    validate_money, ORDERS_INVALID_ORDERS_ERROR_CODE, ORDERS_INVALID_ORDER_TIME_ERROR_CODE, \
    ORDERS_INVALID_AMOUNT_CONSUMED_ERROR_CODE, ORDERS_INVALID_MISSING_AMOUNT_ERROR_CODE, \
    ORDERS_INVALID_EMPTY_CONSUMPTIONS_ERROR_CODE, ORDERS_INVALID_MONEY_CONSUMED_ERROR_CODE, \
    validate_ids_currencies_list, ORDERS_INVALID_MISSING_MONEY_ERROR_CODE, ORDER_DOES_NOT_EXISTS_ERROR_CODE, \
    validate_reservation_keys_list
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError, EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_datetime, validate_id_location, validate_list_exists
from tests.errorDefinitions.errorConstants import ORDERS_INVALID_AMOUNT_CONSUMPTIONS_CODE, \
    ORDERS_INVALID_MONEY_CONSUMPTIONS_CODE

ORDERS_VIEW_NAME = "person-orders"
app = Blueprint(ORDERS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/person-orders/',
           methods=['POST'], strict_slashes=False)
@with_json_body(with_extra_entities=True)
def create_orders(id_client):
    """
    Procesa una lista de ordenes y las agrega a la persona correspondiente
        Parametros esperados:
            id-location: int opcional, id de la ubicación donde se hicieron los consumos
            orders: Lista de ordenes por cantidad consumidas. Cada orden debe tener:
                order-time: Fecha y hora de la orden
                id-reservation: Id de la reserva correspondiente a la orden
                id-person-reservation: Id de la reserva de persona correspondiente a la orden
                money-consumptions: Lista de consumos de la orden. Cada consumo debe tener:
                    id-sku: Id del sku consumido
                    amount-consumed: Cantidad consumida
                    money-consumed: Cantidad de dinero consumida para el sku
                amount-consumptions: Lista de consumos de la orden. Cada consumo debe tener:
                    id-sku: Id del sku consumido
                    amount-consumed: Cantidad consumida
                    missing-amount: Cantidad de items consumidos que no estaban incluidos en el plan/topoffs
    :param id_client: id del cliente asociado
    :return: Lista de ordenes creadas
    """
    id_client = validate_id_client(id_client)

    id_original_location = request.json.get(Order.LOCATION_ID_NAME)

    def create_orders_on_namespace(id_current_client):
        """
        Procesa la lista y crea las entidades correspondientes
        NO se ejecuta dentro de una transacción para evitar problemas cuando la lista tenga más de 25 paquetes diferentes,
        el código debe revisar que los datos son correctos antes de crear cualquier entidad
        :param id_current_client:
        :return:
        """
        id_location = id_original_location
        if id_location is not None:
            id_location = validate_id_location(id_location)

        orders = request.json.get(Order.ORDERS_NAME)
        orders = validate_list_exists(orders, Order.ORDERS_NAME, allow_empty_list=True,
                                      internal_code=ORDERS_INVALID_ORDERS_ERROR_CODE)

        currencies_names = _check_currencies_names(orders)
        person_reservations = _check_and_get_person_reservations(orders)

        orders = _check_orders_and_consumptions_data(orders)

        orders_amount_consumptions_list = [order_data.get(Order.AMOUNT_CONSUMPTIONS_NAME) for order_data in orders]
        orders_money_consumptions_list = [order_data.get(Order.MONEY_CONSUMPTIONS_NAME) for order_data in orders]
        _check_skus_ids_from_consumptions_list(orders_amount_consumptions_list)
        _check_skus_ids_from_consumptions_list(orders_money_consumptions_list)

        (all_orders, new_orders) = _create_orders(id_current_client, id_location, orders, person_reservations,
                                                  currencies_names)

        amount_consumptions_entities, money_consumptions_entities = _create_consumptions_and_timeline_events_for_all_orders(id_current_client,
                                                                                                                            new_orders,
                                                                                                                            all_orders,
                                                                                                                            orders_amount_consumptions_list,
                                                                                                                            orders_money_consumptions_list,
                                                                                                                            person_reservations)
        return [new_orders, list(amount_consumptions_entities), list(money_consumptions_entities)]

    return on_client_namespace(id_client, create_orders_on_namespace,
                               view=ORDERS_VIEW_NAME,
                               action=Role.CREATE_ACTION,
                               id_location=id_original_location)


def _check_currencies_names(orders):
    # Check order entities
    currencies_names = [order_data.get(Order.CURRENCY_NAME)
                        for order_data in orders]
    currencies_names = [Moneda.DEFAULT_CURRENCY_NAME if currency_name is None else currency_name
                        for currency_name in currencies_names]
    currencies_names = validate_ids_currencies_list(currencies_names,
                                                    Order.CURRENCY_NAME,
                                                    allow_empty_list=True)
    return currencies_names


def _check_and_get_person_reservations(orders):
    reservations_keys = [Reserva.get_key_from_id(order_data.get(Order.RESERVATION_ID_NAME))
                         for order_data in orders]
    validate_reservation_keys_list(reservations_keys,
                                   Order.RESERVATION_ID_NAME,
                                   allow_empty_list=True,
                                   get_objects=False)

    person_reservations_keys = [ReservaPersona.get_key_from_id(order_data.get(Order.PERSON_RESERVATION_ID_NAME))
                                for order_data in orders]
    person_reservations = validate_person_reservation_keys_list(person_reservations_keys,
                                                                Order.PERSON_RESERVATION_ID_NAME,
                                                                allow_empty_list=True,
                                                                get_objects=True)
    return person_reservations


def _check_orders_and_consumptions_data(orders):
    for order_data in orders:
        order_time = order_data.get(Order.ORDER_TIME_NAME)
        order_data[Order.ORDER_TIME_NAME] = validate_datetime(order_time, Order.ORDER_TIME_NAME,
                                                              allow_none=False,
                                                              internal_code=ORDERS_INVALID_ORDER_TIME_ERROR_CODE)

        order_data[Order.AMOUNT_CONSUMPTIONS_NAME] = _check_amount_consumptions_list(order_data.get(Order.AMOUNT_CONSUMPTIONS_NAME))
        order_data[Order.MONEY_CONSUMPTIONS_NAME] = _check_money_consumptions_list(order_data.get(Order.MONEY_CONSUMPTIONS_NAME))

        if len(order_data[Order.AMOUNT_CONSUMPTIONS_NAME]) == 0 and len(order_data[Order.MONEY_CONSUMPTIONS_NAME]) == 0:
            raise ValidationError(u"A non-empty list was expected for field {0} or field {1}."
                                  .format(Order.AMOUNT_CONSUMPTIONS_NAME, Order.MONEY_CONSUMPTIONS_NAME),
                                  internal_code=ORDERS_INVALID_EMPTY_CONSUMPTIONS_ERROR_CODE)
    return orders


def _check_amount_consumptions_list(amount_consumptions):
    validate_list_exists(amount_consumptions, Order.AMOUNT_CONSUMPTIONS_NAME, allow_empty_list=True,
                         internal_code=ORDERS_INVALID_AMOUNT_CONSUMPTIONS_CODE)
    for amount_consumption_data in amount_consumptions:
        amount_consumed = amount_consumption_data.get(PersonConsumptionByAmount.AMOUNT_CONSUMED_NAME)
        amount_consumption_data[PersonConsumptionByAmount.AMOUNT_CONSUMED_NAME] = validate_amount(amount_consumed,
                                                                                                  PersonConsumptionByAmount.AMOUNT_CONSUMED_NAME,
                                                                                                  internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_ERROR_CODE)

        missing_amount = amount_consumption_data.get(PersonConsumptionByAmount.MISSING_AMOUNT_NAME)
        amount_consumption_data[PersonConsumptionByAmount.MISSING_AMOUNT_NAME] = validate_amount(missing_amount,
                                                                                                 PersonConsumptionByAmount.MISSING_AMOUNT_NAME,
                                                                                                 allow_zero=True,
                                                                                                 internal_code=ORDERS_INVALID_MISSING_AMOUNT_ERROR_CODE)
    return amount_consumptions


def _check_money_consumptions_list(money_consumptions):
    validate_list_exists(money_consumptions, Order.MONEY_CONSUMPTIONS_NAME, allow_empty_list=True,
                         internal_code=ORDERS_INVALID_MONEY_CONSUMPTIONS_CODE)

    for money_consumption_data in money_consumptions:
        amount_consumed = money_consumption_data.get(PersonConsumptionByMoney.AMOUNT_CONSUMED_NAME)
        money_consumption_data[PersonConsumptionByMoney.AMOUNT_CONSUMED_NAME] = validate_amount(amount_consumed,
                                                                                                PersonConsumptionByMoney.AMOUNT_CONSUMED_NAME,
                                                                                                internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_ERROR_CODE)

        money_consumed = money_consumption_data.get(PersonConsumptionByMoney.MONEY_CONSUMED_NAME)
        money_consumption_data[PersonConsumptionByMoney.MONEY_CONSUMED_NAME] = validate_money(money_consumed,
                                                                                              PersonConsumptionByMoney.MONEY_CONSUMED_NAME,
                                                                                              internal_code=ORDERS_INVALID_MONEY_CONSUMED_ERROR_CODE)

        missing_money = money_consumption_data.get(PersonConsumptionByMoney.MISSING_MONEY_NAME)
        money_consumption_data[PersonConsumptionByMoney.MISSING_MONEY_NAME] = validate_money(missing_money,
                                                                                             PersonConsumptionByMoney.MISSING_MONEY_NAME,
                                                                                             allow_zero=True,
                                                                                             internal_code=ORDERS_INVALID_MISSING_MONEY_ERROR_CODE)
    return money_consumptions


def _create_consumptions_and_timeline_events_for_all_orders(id_client,
                                                            new_orders,
                                                            all_orders,
                                                            orders_amount_consumptions_list,
                                                            orders_money_consumptions_list,
                                                            person_reservations):
    amount_consumptions_entities = []
    money_consumptions_entities = []
    for index, order in enumerate(all_orders):
        if order is not None:
            new_amount_consumptions, new_money_consumptions = _create_consumptions_and_timeline_event_for_order(id_client,
                                                                                                                order,
                                                                                                                orders_amount_consumptions_list[index],
                                                                                                                orders_money_consumptions_list[index],
                                                                                                                person_reservations[index])
            amount_consumptions_entities = itertools.chain(amount_consumptions_entities, new_amount_consumptions)
            money_consumptions_entities = itertools.chain(money_consumptions_entities, new_money_consumptions)
    amount_consumptions_entities = list(amount_consumptions_entities)
    money_consumptions_entities = list(money_consumptions_entities)
    ndb.put_multi(itertools.chain(new_orders, amount_consumptions_entities, money_consumptions_entities))
    return amount_consumptions_entities, money_consumptions_entities


def _create_consumptions_and_timeline_event_for_order(id_client, order, amount_consumptions, money_consumptions,
                                                      person_reservation):
    (amount_consumptions_entities, amount_ids_skus, amount_amounts, amount_money) = \
        _create_amount_consumptions(id_client, order, amount_consumptions)

    (money_consumptions_entities, money_ids_skus, money_amounts, money_money) = \
        _create_money_consumptions(id_client, order, money_consumptions)

    purchase = Compra.create(id_client,
                             person_reservation.idPersona,
                             order.tiempoOrden,
                             amount_ids_skus + money_ids_skus,
                             amount_money + money_money,
                             amount_amounts + money_amounts)

    order.associate_timeline_purchase(purchase)
    return amount_consumptions_entities, money_consumptions_entities


def _create_amount_consumptions(id_client, order, amount_consumptions):
    consumptions_entities = []
    ids_skus = []
    amounts = []
    money = []
    for index, amount_consumption_data in enumerate(amount_consumptions):
        id_sku = amount_consumption_data.get(PersonConsumptionByAmount.SKU_ID_NAME)
        ids_skus.append(id_sku)

        amount_consumed = amount_consumption_data.get(PersonConsumptionByAmount.AMOUNT_CONSUMED_NAME)
        amounts.append(amount_consumed)

        money.append(0)

        missing_amount = amount_consumption_data.get(PersonConsumptionByAmount.MISSING_AMOUNT_NAME)
        consumptions_entities.append(PersonConsumptionByAmount.create_without_put(id_client,
                                                                                  order.idReserva,
                                                                                  order.idReservaPersona,
                                                                                  order.key.id(),
                                                                                  id_sku,
                                                                                  amount_consumed,
                                                                                  missing_amount,
                                                                                  order.tiempoOrden,
                                                                                  index))
    return consumptions_entities, ids_skus, amounts, money


def _create_money_consumptions(id_client, order, money_consumptions):
    consumptions_entities = []
    ids_skus = []
    amounts = []
    money = []

    for money_consumption_data in money_consumptions:
        id_sku = money_consumption_data.get(PersonConsumptionByMoney.SKU_ID_NAME)
        ids_skus.append(id_sku)

        amount_consumed = money_consumption_data.get(PersonConsumptionByMoney.AMOUNT_CONSUMED_NAME)
        amounts.append(amount_consumed)

        money_consumed = money_consumption_data.get(PersonConsumptionByMoney.MONEY_CONSUMED_NAME)
        money.append(money_consumed/amount_consumed)

        missing_money = money_consumption_data.get(PersonConsumptionByMoney.MISSING_MONEY_NAME)

        consumptions_entities.append(PersonConsumptionByMoney.create_without_put(id_client,
                                                                                 order.idReserva,
                                                                                 order.idReservaPersona,
                                                                                 order.key.id(),
                                                                                 id_sku,
                                                                                 amount_consumed,
                                                                                 money_consumed,
                                                                                 missing_money,
                                                                                 order.moneda,
                                                                                 order.tiempoOrden))
    return consumptions_entities, ids_skus, amounts, money


def _check_skus_ids_from_consumptions_list(consumptions_list):
    amount_skus_ids = [consumption.get(PersonConsumptionByAmount.SKU_ID_NAME)
                       for consumptions in consumptions_list for consumption in consumptions]
    validate_id_skus_list(amount_skus_ids, PersonConsumptionByAmount.SKU_ID_NAME, allow_empty_list=True)


@app.route('/clients/<int:id_client>/person-orders/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_orders(id_client):
    """
    Lista las ordenes existentes.
    :param id_client: id del cliente asociado
    :return: Lista de ordenes existentes
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_amount_orders_on_namespace(id_current_client):
        return Order.list()
    return on_client_namespace(id_client,
                               list_amount_orders_on_namespace,
                               action=Role.READ_ACTION,
                               view=ORDERS_VIEW_NAME)


def _create_orders(id_client, id_location, orders, person_reservations, currencies_names):
    transactions_keys = [PastTransactionKey.get_key_from_fields(order_data.get(Order.RESERVATION_ID_NAME),
                                                                order_data.get(Order.PERSON_RESERVATION_ID_NAME),
                                                                order_data[Order.ORDER_TIME_NAME],
                                                                PastTransactionKey.ORDER_NAME)
                         for order_data in orders]
    previous_transactions = ndb.get_multi(transactions_keys)

    already_created_keys = set()
    new_orders = []
    all_orders = []
    transactions_entities = []
    for index, order_data in enumerate(orders):
        if previous_transactions[index] is None and transactions_keys[index] not in already_created_keys:
            order_time = order_data.get(Order.ORDER_TIME_NAME)
            person_reservation = person_reservations[index]
            currency = currencies_names[index]

            order = Order.create_without_put(id_client, person_reservation.idReserva, person_reservation.key.id(),
                                             currency, order_time, id_location)
            new_orders.append(order)
            all_orders.append(order)
            transactions_entities.append(PastTransactionKey.create_without_put_from_key(transactions_keys[index]))
            already_created_keys.add(transactions_keys[index])
        else:
            all_orders.append(None)
    ndb.put_multi(new_orders + transactions_entities)
    return all_orders, new_orders


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-orders/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_amount_orders_by_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Lista las ordenes existentes para la reserva de persona dada.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Lista de ordenes existentes para la reserva de persona dada
    """
    id_client = validate_id_client(id_client)

    def list_orders_by_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              ORDERS_VIEW_NAME)
        return Order.list_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id())

    return on_client_namespace(id_client,
                               list_orders_by_person_reservation_on_namespace,
                               secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-orders/<int:id_order>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_order(id_client, id_reservation, id_person_reservation, id_order):
    """
    Da la orden con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_order: id de la orden a consultar
    :return: Orden con id dado
    """
    id_client = validate_id_client(id_client)

    def get_order_on_namespace(id_current_client):
        reservation, person_reservation, order = get_reservation_person_reservation_and_order_with_permissions(id_current_client,
                                                                                                               id_reservation,
                                                                                                               id_person_reservation,
                                                                                                               id_order,
                                                                                                               Role.READ_ACTION,
                                                                                                               ORDERS_VIEW_NAME)

        return order

    return on_client_namespace(id_client, get_order_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-orders/<int:id_order>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_order(id_client, id_reservation, id_person_reservation, id_order):
    """
    Elimina la orden con id dado.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :param id_order: id de la orden a eliminar
    :return: Orden eliminada
    """
    id_client = validate_id_client(id_client)

    def delete_order_on_namespace(id_current_client):
        reservation, person_reservation, order = get_reservation_person_reservation_and_order_with_permissions(id_current_client,
                                                                                                               id_reservation,
                                                                                                               id_person_reservation,
                                                                                                               id_order,
                                                                                                               Role.DELETE_ACTION,
                                                                                                               ORDERS_VIEW_NAME)

        amount_consumptions = PersonConsumptionByAmount.list_by_ids_order(order.idReserva, order.idReservaPersona,
                                                                          order.key.id())
        money_consumptions = PersonConsumptionByMoney.list_by_ids_order(order.idReserva, order.idReservaPersona,
                                                                        order.key.id())

        order.mark_as_deleted()

        for amount_consumption in amount_consumptions:
            amount_consumption.mark_as_deleted()

        for money_consumption in money_consumptions:
            money_consumption.mark_as_deleted()

        ndb.put_multi(amount_consumptions + money_consumptions + [order])

        PastTransactionKey.get_key_from_fields(order.idReserva,
                                               order.idReservaPersona,
                                               order.tiempoOrden,
                                               PastTransactionKey.ORDER_NAME).delete()

        initial_time = validate_datetime(datetime_input=None, field_name=Devolucion.INITIAL_TIME_NAME, allow_none=True)
        Devolucion.create(id_current_client, person_reservation.idPersona, initial_time, order.idEvento)
        return order

    return on_client_namespace(id_client, delete_order_on_namespace, secured=False)


def get_reservation_person_reservation_and_order_with_permissions(id_client, id_reservation, id_person_reservation,
                                                                  id_order, action, view):
    reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_client,
                                                                                                          id_reservation,
                                                                                                          id_person_reservation,
                                                                                                          action,
                                                                                                          view)

    order = Order.get_by_ids(person_reservation.idReserva, person_reservation.key.id(), id_order)
    if order is not None:
        # Only return if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            id_location=order.idUbicacionOrden,
                            action=action,
                            view=view)
        return reservation, person_reservation, order
    else:
        # Only raise a validation error if the user has the correct permission
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            action=action,
                            view=view)
        raise EntityDoesNotExists(u"Order[{0}]".format(id_order),
                                  internal_code=ORDER_DOES_NOT_EXISTS_ERROR_CODE)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
