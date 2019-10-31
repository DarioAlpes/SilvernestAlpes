# -*- coding: utf-8 -*
from CJM.entidades.measures.Measure import Measure
from CJM.services.validations import validate_id_person
from commons.excepciones.apiexceptions import EntityDoesNotExists
from commons.utils import on_client_namespace, with_json_bodyless
from flask import request, Blueprint
from CJM.entidades.measures import PersonMeasures

from commons.validations import validate_id_client

app = Blueprint("person_measures", __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/measures/<string:id_measure>/', methods=['GET'],
           strict_slashes=False)
@with_json_bodyless
def calculate_measure(id_client, id_person, id_measure):
    id_client = validate_id_client(id_client)

    id_measure_normalized = id_measure.replace("-", "_")

    measure = getattr(PersonMeasures, id_measure_normalized, None)

    if measure is None or (not hasattr(measure, '__call__')):
        raise EntityDoesNotExists(u"Measure[{0}]".format(id_measure))

    # noinspection PyUnusedLocal
    def calculate_measure_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        return Measure(measure(id_current_person, **request.args))

    return on_client_namespace(id_client, calculate_measure_on_namespace)
