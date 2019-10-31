# -*- coding: utf-8 -*
from CJM.entidades.sensores.Sensor import Sensor
from flask import request, Blueprint
from google.appengine.api.taskqueue import taskqueue

from CJM.entidades.lecturas.LogLectura import LogLectura
from commons.utils import with_json_body, on_client_namespace
from commons.validations import validate_id_client, validate_list_exists
from CJM.services.validations import validate_id_reading_log

app = Blueprint('readings', __name__)


@app.route('/clients/<int:id_client>/sensors/<string:id_sensor>/readings/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_readings_from_mobile_sensor(id_client, id_sensor):
    """
    Crea las lecturas recibidas por parámetro en el namespace del cliente id_client asociandolas al sensor dado
        Parametros esperados:
            readings: Lista con:
                id-beacon: int
                reading-time: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
    :param id_client: id del cliente asociado
    :param id_sensor: id del sensor asociado
    :returns Lista de log de lectura creados
    """
    id_client = validate_id_client(id_client)
    sensor = Sensor.get_sensor_by_id(id_sensor)
    sensor.check_id_client(id_client, id_sensor)

    # noinspection PyUnusedLocal
    def create_readings_from_mobile_sensor_on_namespace(id_current_client):

        readings = request.json.get(LogLectura.READINGS_NAME)
        readings = validate_list_exists(readings, LogLectura.READINGS_NAME, allow_empty_list=False)

        return LogLectura.create_readings(id_sensor, readings)

    reading_logs = on_client_namespace(id_client, create_readings_from_mobile_sensor_on_namespace)

    url_template = '/clients/{0}/readings/{{0}}/events/'.format(id_client)

    tasks = [taskqueue.Task(url=url_template.format(log.idInterno), method='POST',
                            headers={"Content-type": "application/json"}, payload="{}")
             for log in reading_logs]

    taskqueue.Queue(LogLectura.READINGS_QUEUE_NAME).add(tasks)
    return reading_logs


@app.route('/clients/<int:id_client>/readings/<int:id_log>/events/', methods=['POST'], strict_slashes=False)
@with_json_body
def process_log(id_client, id_log):
    """
    Crea los eventos asociados al log dado en el namespace del cliente dado
        Parametros esperados:
    :param id_client: id del cliente asociado
    :param id_log: id del log a procesar
    :return: Lista de eventos creados
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_log_by_id_on_namespace(id_current_client):
        id_current_log = validate_id_reading_log(id_log)
        return LogLectura.get_by_id(id_current_log)

    reading_log = on_client_namespace(id_client, get_log_by_id_on_namespace)
    sensor = Sensor.get_sensor_by_id(reading_log.idSensor)
    sensor.check_id_client(id_client, reading_log.idSensor)

    def create_readings_from_mobile_sensor_on_namespace(id_current_client):
        id_current_log = validate_id_reading_log(id_log)

        return LogLectura.process_reading(id_current_log, id_current_client, sensor)

    return on_client_namespace(id_client, create_readings_from_mobile_sensor_on_namespace)
