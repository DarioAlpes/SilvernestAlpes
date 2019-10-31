# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.Accion import Accion
from CJM.services.validations import validate_id_action, validate_id_person, validate_amount
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from flask import request, Blueprint

from commons.validations import validate_id_client, validate_datetime, validate_string_not_empty

app = Blueprint("acciones", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/actions/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_action(id_client, id_person):
    """
    Crea una acción para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            name: str
            amount: int mayor a 0
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :return: acción creada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    name = request.json.get(Accion.ACTION_NAME_NAME)
    name = validate_string_not_empty(name, Accion.ACTION_NAME_NAME)

    amount = request.json.get(Accion.AMOUNT_NAME)
    amount = validate_amount(amount, Accion.AMOUNT_NAME)

    def create_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Accion.create(id_current_client, id_current_person, initial_time, name, amount)

    return on_client_namespace(id_client, create_action_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/actions/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_actions_by_person(id_client, id_person):
    """
    Da la lista de acciones de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de acciones de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_actions_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Accion.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_actions_by_person_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/actions/<int:id_action>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_action(id_client, id_person, id_action):
    """
    Da la acción con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_action: id de la acción asociada.
    :return: acción con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_action = validate_id_action(id_action)

        return Accion.get_by_id_for_person(id_current_action, id_current_person, u"Action")

    return on_client_namespace(id_client, get_action_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/actions/<int:id_action>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_action(id_client, id_person, id_action):
    """
    Elimina la acción con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_action: id de la acción a eliminar.
    :return: acción celiminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_action = validate_id_action(id_action)

        return Accion.delete_by_id_for_person(id_current_action, id_current_person, u"Action")

    return on_client_namespace(id_client, get_action_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/actions/<int:id_action>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_action(id_client, id_person, id_action):
    """
    Actualiza la acción con id dado para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            name: str
            amount: int mayor a 0
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :param id_action: id de la acción a actualizar.
    :return: acción creada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    name = request.json.get(Accion.ACTION_NAME_NAME)
    name = validate_string_not_empty(name, Accion.ACTION_NAME_NAME)

    amount = request.json.get(Accion.AMOUNT_NAME)
    amount = validate_amount(amount, Accion.AMOUNT_NAME)

    # noinspection PyUnusedLocal
    def create_action_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_action = validate_id_action(id_action)

        return Accion.update(id_current_person, id_current_action, initial_time, name, amount)

    return on_client_namespace(id_client, create_action_on_namespace)
