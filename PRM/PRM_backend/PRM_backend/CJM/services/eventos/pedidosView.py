# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.Pedido import Pedido
from CJM.services.validations import validate_id_order, validate_id_person, validate_id_skus_list,\
    validate_amount_list
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from flask import request, Blueprint

from commons.validations import validate_id_client, validate_datetime

app = Blueprint("orders", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/orders/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_order(id_client, id_person):
    """
    Crea un pedido para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-skus: lista de ints con los ids de los sku, o int que se interpreta como lista de tamaño 1
            amounts: lista de ints con las cantidades, o int que se interpreta como lista de tamaño 1
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :return: pedido creado
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    amounts = request.json.get(Pedido.AMOUNTS_NAME)
    if amounts is not None and not isinstance(amounts, list):
        amounts = [amounts]
    amounts = validate_amount_list(amounts, Pedido.AMOUNTS_NAME)

    amounts_length = len(amounts)

    def create_order_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_skus = request.json.get(Pedido.SKU_IDS_NAME)
        if id_skus is not None and not isinstance(id_skus, list):
            id_skus = [id_skus]
        id_skus = validate_id_skus_list(id_skus, Pedido.SKU_IDS_NAME)

        if amounts_length != len(id_skus):
            raise ValidationError(u"The lists {0} y {1} must have the same length.".
                                  format(Pedido.AMOUNTS_NAME, Pedido.SKU_IDS_NAME))

        return Pedido.create(id_current_client, id_current_person, initial_time, id_skus, amounts)

    return on_client_namespace(id_client, create_order_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/orders/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_orders_by_person(id_client, id_person):
    """
    Da la lista de pedidos de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de pedidos de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_orders_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Pedido.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_orders_by_person_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/orders/<int:id_order>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_order(id_client, id_person, id_order):
    """
    Da el pedido con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_order: id del pedido asociado.
    :return: pedido con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_order_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_order = validate_id_order(id_order)

        return Pedido.get_by_id_for_person(id_current_order, id_current_person, u"Order")

    return on_client_namespace(id_client, get_order_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/orders/<int:id_order>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_order(id_client, id_person, id_order):
    """
    Elimina el pedido con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_order: id del pedido a eliminar.
    :return: pedido eliminado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_order_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_order = validate_id_order(id_order)

        return Pedido.delete_by_id_for_person(id_current_order, id_current_person, u"Order")

    return on_client_namespace(id_client, get_order_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/orders/<int:id_order>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_order(id_client, id_person, id_order):
    """
    Actualiza el pedido con id dado para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-skus: lista de ints con los ids de los sku, o int que se interpreta como lista de tamaño 1
            prices: lista de floats con los precios, o float que se interpreta como lista de tamaño 1
            amounts: lista de ints con las cantidades, o int que se interpreta como lista de tamaño 1
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :param id_order: id del pedido a actualizar.
    :return: pedido actualizado
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    amounts = request.json.get(Pedido.AMOUNTS_NAME)
    if amounts is not None and not isinstance(amounts, list):
        amounts = [amounts]
    amounts = validate_amount_list(amounts, Pedido.AMOUNTS_NAME)

    amounts_length = len(amounts)

    # noinspection PyUnusedLocal
    def update_order_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_skus = request.json.get(Pedido.SKU_IDS_NAME)
        if id_skus is not None and not isinstance(id_skus, list):
            id_skus = [id_skus]
        id_skus = validate_id_skus_list(id_skus, Pedido.SKU_IDS_NAME)

        if amounts_length != len(id_skus):
            raise ValidationError(u"The lists {0} y {1} must have the same length.".
                                  format(Pedido.AMOUNTS_NAME, Pedido.SKU_IDS_NAME))

        id_current_order = validate_id_order(id_order)

        return Pedido.update(id_current_person, id_current_order, initial_time, id_skus, amounts)

    return on_client_namespace(id_client, update_order_on_namespace)
