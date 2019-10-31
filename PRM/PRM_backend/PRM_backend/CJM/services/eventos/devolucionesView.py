# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.Devolucion import Devolucion
from CJM.services.validations import validate_id_person, validate_id_refund
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from flask import request, Blueprint

from commons.validations import validate_id_client, validate_datetime

app = Blueprint("devoluciones", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/refunds/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_refund(id_client, id_person):
    """
    Crea una devolución para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-event: int. Id del evento original que se esta devolviendo
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :return: devolución creada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    def create_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        id_event = request.json.get(Devolucion.EVENT_ID_NAME)
        Evento.get_by_id_for_person(id_event, id_current_person, u"Event")

        if len(Devolucion.get_previous_event_devolutions(id_current_person, id_event)) > 0:
            raise ValidationError(u"The event with id {0} was already refunded.".format(id_event))

        return Devolucion.create(id_current_client, id_current_person, initial_time, id_event)

    return on_client_namespace(id_client, create_action_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/refunds/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_refunds_by_person(id_client, id_person):
    """
    Da la lista de devoluciones de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de devoluciones de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_actions_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Devolucion.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_actions_by_person_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/refunds/<int:id_refund>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_refund(id_client, id_person, id_refund):
    """
    Da la devolución con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_refund: id de la devolución asociada.
    :return: devolución con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_refund = validate_id_refund(id_refund)

        return Devolucion.get_by_id_for_person(id_current_refund, id_current_person, u"Refund")

    return on_client_namespace(id_client, get_action_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/refunds/<int:id_refund>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_refund(id_client, id_person, id_refund):
    """
    Elimina la devolución con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_refund: id de la devolución a eliminar.
    :return: devolución eliminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_refund_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_refund = validate_id_refund(id_refund)

        return Devolucion.delete_by_id_for_person(id_current_refund, id_current_person, u"Refund")

    return on_client_namespace(id_client, delete_refund_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/refunds/<int:id_refund>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_refund(id_client, id_person, id_refund):
    """
    Actualiza la devolución con id dado para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-event: int. Id del evento original que se esta devolviendo
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :param id_refund: id de la devolución a actualizar.
    :return: devolución actualizada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    # noinspection PyUnusedLocal
    def update_refund_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        current_refund = Devolucion.get_by_id_for_person(id_refund, id_current_person, u"Refund")
        id_event = request.json.get(Devolucion.EVENT_ID_NAME)
        Evento.get_by_id_for_person(id_event, id_current_person, u"Event")

        refunds = Devolucion.get_previous_event_devolutions(id_current_person, id_event)
        if len(refunds) > 0:
            if len(refunds) > 1 or current_refund.idInterno != refunds[0].idInterno:
                raise ValidationError(u"The event with id {0} was already refunded.".format(id_event))

        return Devolucion.update(id_current_person, id_refund, initial_time, id_event)

    return on_client_namespace(id_client, update_refund_on_namespace)
