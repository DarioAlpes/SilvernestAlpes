# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.services.validations import validate_id_event, validate_id_person
from commons.utils import on_client_namespace, with_json_bodyless
from flask import Blueprint

from commons.validations import validate_id_client

app = Blueprint("eventos", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/events/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_events_by_person(id_client, id_person):
    """
    Da la lista de eventos de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de eventos de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_events_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Evento.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_events_by_person_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/events/<int:id_event>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_event(id_client, id_person, id_event):
    """
    Da el evento con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_event: id del evento asociado.
    :return: evento con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_event = validate_id_event(id_event)

        return Evento.get_by_id_for_person(id_current_event, id_current_person, u"Event")

    return on_client_namespace(id_client, get_action_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/events/<int:id_event>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_event(id_client, id_person, id_event):
    """
    Elimina el evento con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_event: id del evento a eliminar.
    :return: evento eliminado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_event = validate_id_event(id_event)

        return Evento.delete_by_id_for_person(id_current_event, id_current_person, u"Event")

    return on_client_namespace(id_client, get_action_on_namespace)
