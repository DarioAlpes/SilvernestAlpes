# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.Compra import Compra
from CJM.services.validations import validate_id_person, validate_id_skus_list,\
    validate_price_list, validate_amount_list, validate_id_purchase
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from flask import request, Blueprint

from commons.validations import validate_id_client, validate_datetime

app = Blueprint("compras", __name__)


# TODO borrar función, solo para el demo
@app.route('/demoapi/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def create_demo_purchase():
    id_client = 10

    initial_time = validate_datetime("19900101010101", Evento.INITIAL_TIME_NAME)

    prices = [10000]

    amounts = [1]

    def create_purchase_on_namespace(id_current_client):
        id_current_person = 1

        id_skus = [11]

        return Compra.create(id_current_client, id_current_person, initial_time, id_skus, prices, amounts)

    return on_client_namespace(id_client, create_purchase_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/purchases/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_purchase(id_client, id_person):
    """
    Crea una compra para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-skus: lista de ints con los ids de los sku, o int que se interpreta como lista de tamaño 1
            prices: lista de floats con los precios, o float que se interpreta como lista de tamaño 1
            amounts: lista de ints con las cantidades, o int que se interpreta como lista de tamaño 1
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :return: compra creada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    prices = request.json.get(Compra.PRICES_NAME)
    if prices is not None and not isinstance(prices, list):
        prices = [prices]
    prices = validate_price_list(prices, Compra.PRICES_NAME)

    prices_length = len(prices)

    amounts = request.json.get(Compra.AMOUNTS_NAME)
    if amounts is not None and not isinstance(amounts, list):
        amounts = [amounts]
    amounts = validate_amount_list(amounts, Compra.AMOUNTS_NAME)

    if prices_length != len(amounts):
        raise ValidationError(u"The lists {0} y {1} must have the same length.".
                              format(Compra.PRICES_NAME, Compra.AMOUNTS_NAME))

    def create_purchase_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_skus = request.json.get(Compra.SKU_IDS_NAME)
        if id_skus is not None and not isinstance(id_skus, list):
            id_skus = [id_skus]
        id_skus = validate_id_skus_list(id_skus, Compra.SKU_IDS_NAME)

        if prices_length != len(id_skus):
            raise ValidationError(u"The lists {0} y {1} must have the same length.".
                                  format(Compra.PRICES_NAME, Compra.SKU_IDS_NAME))

        return Compra.create(id_current_client, id_current_person, initial_time, id_skus, prices, amounts)

    return on_client_namespace(id_client, create_purchase_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/purchases/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_purchases_by_person(id_client, id_person):
    """
    Da la lista de compras de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de compras de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_purchases_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Compra.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_purchases_by_person_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/purchases/<int:id_purchase>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_purchase(id_client, id_person, id_purchase):
    """
    Da la compra con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_purchase: id de la compra asociada.
    :return: compra con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_purchase_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_purchase = validate_id_purchase(id_purchase)

        return Compra.get_by_id_for_person(id_current_purchase, id_current_person, u"Purchase")

    return on_client_namespace(id_client, get_purchase_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/purchases/<int:id_purchase>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_purchase(id_client, id_person, id_purchase):
    """
    Elimina la compra con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_purchase: id de la compra a eliminar.
    :return: compra eliminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_purchase_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_purchase = validate_id_purchase(id_purchase)

        return Compra.delete_by_id_for_person(id_current_purchase, id_current_person, u"Purchase")

    return on_client_namespace(id_client, get_purchase_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/purchases/<int:id_purchase>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_purchase(id_client, id_person, id_purchase):
    """
    Actualiza la compra con id dado para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-skus: lista de ints con los ids de los sku, o int que se interpreta como lista de tamaño 1
            prices: lista de floats con los precios, o float que se interpreta como lista de tamaño 1
            amounts: lista de ints con las cantidades, o int que se interpreta como lista de tamaño 1
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :param id_purchase: id de la compra a actualizar.
    :return: compra creada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    prices = request.json.get(Compra.PRICES_NAME)
    if prices is not None and not isinstance(prices, list):
        prices = [prices]
    prices = validate_price_list(prices, Compra.PRICES_NAME)

    prices_length = len(prices)

    amounts = request.json.get(Compra.AMOUNTS_NAME)
    if amounts is not None and not isinstance(amounts, list):
        amounts = [amounts]
    amounts = validate_amount_list(amounts, Compra.AMOUNTS_NAME)

    if prices_length != len(amounts):
        raise ValidationError(u"The lists {0} y {1} must have the same length.".
                              format(Compra.PRICES_NAME, Compra.AMOUNTS_NAME))

    # noinspection PyUnusedLocal
    def update_purchase_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_purchase = validate_id_purchase(id_purchase)

        id_skus = request.json.get(Compra.SKU_IDS_NAME)
        if id_skus is not None and not isinstance(id_skus, list):
            id_skus = [id_skus]
        id_skus = validate_id_skus_list(id_skus, Compra.SKU_IDS_NAME)

        if prices_length != len(id_skus):
            raise ValidationError(u"The lists {0} y {1} must have the same length.".
                                  format(Compra.PRICES_NAME, Compra.SKU_IDS_NAME))

        return Compra.update(id_current_person, id_current_purchase, initial_time, id_skus, prices, amounts)

    return on_client_namespace(id_client, update_purchase_on_namespace)
