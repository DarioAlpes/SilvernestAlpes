# -*- coding: utf-8 -*
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.ReporteVisita import ReporteVisita
from CJM.entidades.eventos.ReporteVisitaPorTipo import ReporteVisitaPorTipo
from CJM.entidades.eventos.Visita import Visita
from CJM.entidades.persons.Persona import Persona
from CJM.services.validations import validate_id_visit, validate_id_person
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from commons.validations import validate_id_client, validate_datetime, validate_id_location, validate_location_type

app = Blueprint("visitas", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/visits/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_visit(id_client, id_person):
    """
    Crea una visita para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            final-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-location: int
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :return: visita creada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    final_time = request.json.get(Visita.FINAL_TIME_NAME)
    final_time = validate_datetime(final_time, Visita.FINAL_TIME_NAME)

    def create_visit_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_location = request.json.get(Visita.UBICACION_ID_NAME)
        id_location = validate_id_location(id_location, Visita.UBICACION_ID_NAME)

        return Visita.create(id_current_client, id_current_person, initial_time, final_time, id_location)

    return on_client_namespace(id_client, create_visit_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/visits/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_visits_by_person(id_client, id_person):
    """
    Da la lista de visitas de la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociado.
    :return: Lista de visitas de la persona dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_visits_by_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        return Visita.list_by_person(id_current_person)

    return on_client_namespace(id_client, list_visits_by_person_on_namespace)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/visits-per-category/', methods=['GET'],
           strict_slashes=False)
@with_json_bodyless
def get_visits_report(id_client, id_location):
    """
    Da el reporte de visitas por categoría de las visitas a la ubicación con id dado en un rango de fechas
        Parametros esperados en el query string:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite no se
            filtra por fecha inicial
            final-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite no se
            filtra por fecha final
    :param id_client: id del cliente asociado.
    :param id_location: id de la ubicación a reportar asociado.
    :return: Reporte de visitas de la ubicación dada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_visits_by_person_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)

        initial_time = request.args.get(ReporteVisita.INITIAL_TIME_NAME, None)
        if initial_time is not None:
            initial_time = validate_datetime(initial_time, ReporteVisita.INITIAL_TIME_NAME)

        final_time = request.args.get(ReporteVisita.FINAL_TIME_NAME, None)
        if final_time is not None:
            final_time = validate_datetime(final_time, ReporteVisita.FINAL_TIME_NAME)

        return get_report_for_location(id_current_location, initial_time, final_time)

    return on_client_namespace(id_client, list_visits_by_person_on_namespace)


def get_report_for_location(id_location, initial_time, final_time):
    visits = Visita.list_by_location_and_datetimes(id_location, initial_time, final_time)
    ids_persons = {ndb.Key(Persona, visit.idPersona) for visit in visits}
    persons = ndb.get_multi(ids_persons)

    report = ReporteVisita()
    for person in persons:
        report.add_visit(person)

    return report


@app.route('/clients/<int:id_client>/visits-per-category/', methods=['GET'],
           strict_slashes=False)
@with_json_bodyless
def get_visits_by_type_report(id_client):
    """
    Da el reporte de visitas por categoría de las visitas de las ubicaciones con tipo dado en un rango de fechas
        Parametros esperados en el query string:
            type: str con el tipo de ubicación a consultar
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite no se
            filtra por fecha inicial
            final-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite no se
            filtra por fecha final
    :param id_client: id del cliente asociado.
    :return: Reporte de visitas de la ubicación dada
    """
    id_client = validate_id_client(id_client)
    location_type = request.args.get(ReporteVisitaPorTipo.UBICACION_TYPE_NAME, None)
    if location_type is not None:
        location_type = validate_location_type(location_type, ReporteVisitaPorTipo.UBICACION_TYPE_NAME)

    # noinspection PyUnusedLocal
    def get_visits_by_type_report_on_namespace(id_current_client):
        initial_time = request.args.get(ReporteVisita.INITIAL_TIME_NAME, None)
        if initial_time is not None:
            initial_time = validate_datetime(initial_time, ReporteVisita.INITIAL_TIME_NAME)

        final_time = request.args.get(ReporteVisita.FINAL_TIME_NAME, None)
        if final_time is not None:
            final_time = validate_datetime(final_time, ReporteVisita.FINAL_TIME_NAME)

        if location_type is None:
            locations = Ubicacion.list()
        else:
            locations = Ubicacion.list_by_type(location_type)

        report = ReporteVisitaPorTipo()
        for location in locations:
            report.add_report_for_location(location.key.id(), location.nombre,
                                           get_report_for_location(location.key.id(), initial_time, final_time))

        return report.to_list()

    return on_client_namespace(id_client, get_visits_by_type_report_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/visits/<int:id_visit>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_visit(id_client, id_person, id_visit):
    """
    Da la visita con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_visit: id de la visita asociada.
    :return: visita con id dado para la persona dada para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_visit_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_visit = validate_id_visit(id_visit)

        return Visita.get_by_id_for_person(id_current_visit, id_current_person, u"Visit")

    return on_client_namespace(id_client, get_visit_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/visits/<int:id_visit>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_visit(id_client, id_person, id_visit):
    """
    Elimina la visita con id dado para la persona dada para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_person: id de la persona asociada.
    :param id_visit: id de la visita a eliminar.
    :return: visita eliminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_visit_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_current_visit = validate_id_visit(id_visit)

        return Visita.delete_by_id_for_person(id_current_visit, id_current_person, u"Visit")

    return on_client_namespace(id_client, get_visit_on_namespace)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/visits/<int:id_visit>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_visit(id_client, id_person, id_visit):
    """
    Actualiza la visita con id dado para la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            initial-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            final-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"] opcional. Si se omite se supone
            el tiempo actual del servidor
            id-location: int
    :param id_client: id del cliente asociado
    :param id_person: id de la persona asociada
    :param id_visit: id de la visita a actualizar
    :return: visita actualizada
    """
    id_client = validate_id_client(id_client)

    initial_time = request.json.get(Evento.INITIAL_TIME_NAME)
    initial_time = validate_datetime(initial_time, Evento.INITIAL_TIME_NAME)

    final_time = request.json.get(Visita.FINAL_TIME_NAME)
    final_time = validate_datetime(final_time, Visita.FINAL_TIME_NAME)

    # noinspection PyUnusedLocal
    def create_visit_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)

        id_location = request.json.get(Visita.UBICACION_ID_NAME)
        id_location = validate_id_location(id_location, Visita.UBICACION_ID_NAME)

        id_current_visit = validate_id_visit(id_visit)

        return Visita.update(id_current_person, id_current_visit, initial_time, final_time, id_location)

    return on_client_namespace(id_client, create_visit_on_namespace)
