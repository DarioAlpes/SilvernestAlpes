# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.Feedback import Feedback
from CJM.services.validations import validate_id_feedback, validate_id_person, validate_score, validate_id_sku
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from flask import request, Blueprint

from commons.validations import validate_id_client, validate_datetime, validate_id_location, validate_string_not_empty

app = Blueprint("feedbacks", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/feedbacks/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_feedback(id_client, id_person):
    """
    Crea un feedback para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            text: str
            score: int entre 0-100
            id-location: int
            id-sku: int
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :return: feedback creado
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    text = request.json.get(Feedback.TEXT_NAME)
    text = validate_string_not_empty(text, Feedback.TEXT_NAME)

    score = request.json.get(Feedback.SCORE_NAME)
    score = validate_score(score, Feedback.SCORE_NAME)

    def create_feedback_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_location = request.json.get(Feedback.UBICACION_ID_NAME)
        id_location = validate_id_location(id_location, Feedback.UBICACION_ID_NAME)

        id_sku = request.json.get(Feedback.SKU_ID_NAME)
        id_sku = validate_id_sku(id_sku, Feedback.SKU_ID_NAME)

        return Feedback.create(id_current_client, id_current_person, initial_time, text, score, id_location, id_sku)

    return on_client_namespace(id_client, create_feedback_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/feedbacks/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_feedbacks_by_person(id_client, id_person):
    """
    Da la lista de feedbacks de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de feedbacks de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_feedbacks_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Feedback.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_feedbacks_by_person_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/feedbacks/<int:id_feedback>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_feedback(id_client, id_person, id_feedback):
    """
    Da el feedback con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_feedback: id del feedback asociada.
    :return: feedback con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_feedback_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_feedback = validate_id_feedback(id_feedback)

        return Feedback.get_by_id_for_person(id_current_feedback, id_current_person, u"Feedback")

    return on_client_namespace(id_client, get_feedback_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/feedbacks/<int:id_feedback>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_feedback(id_client, id_person, id_feedback):
    """
    Elimina el feedback con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_feedback: id del feedback a eliminar.
    :return: feedback eliminado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_feedback_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_feedback = validate_id_feedback(id_feedback)

        return Feedback.delete_by_id_for_person(id_current_feedback, id_current_person, u"Feedback")

    return on_client_namespace(id_client, get_feedback_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/feedbacks/<int:id_feedback>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_feedback(id_client, id_person, id_feedback):
    """
    Actualiza el feedback con id dado para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            text: str
            score: int entre 0-100
            id-location: int
            id-sku: int
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :param id_feedback: id del feedback a actualizar.
    :return: feedback actualizado
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    text = request.json.get(Feedback.TEXT_NAME)
    text = validate_string_not_empty(text, Feedback.TEXT_NAME)

    score = request.json.get(Feedback.SCORE_NAME)
    score = validate_score(score, Feedback.SCORE_NAME)

    # noinspection PyUnusedLocal
    def update_feedback_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_feedback = validate_id_feedback(id_feedback)

        id_location = request.json.get(Feedback.UBICACION_ID_NAME)
        id_location = validate_id_location(id_location, Feedback.UBICACION_ID_NAME)

        id_sku = request.json.get(Feedback.SKU_ID_NAME)
        id_sku = validate_id_sku(id_sku, Feedback.SKU_ID_NAME)

        return Feedback.update(id_current_person, id_current_feedback, initial_time, text, score, id_location, id_sku)

    return on_client_namespace(id_client, update_feedback_on_namespace)
