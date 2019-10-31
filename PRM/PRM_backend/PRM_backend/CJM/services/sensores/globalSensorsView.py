# -*- coding: utf-8 -*
from CJM.entidades.sensores.SensorType import SensorType
from CJM.services.validations import GLOBAL_SENSOR_INVALID_ID_ERROR_CODE, GLOBAL_SENSOR_INVALID_TYPE_ERROR_CODE, \
    GLOBAL_SENSOR_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityAlreadyExists
from commons.validations import validate_string_not_empty
from flask import request, Blueprint

from CJM.entidades.sensores.Sensor import Sensor
from commons.utils import with_json_body, with_json_bodyless, validate_user_logged_in_is_global_user

GLOBAL_SENSORS_VIEW_NAME = "global-sensors"
app = Blueprint("global-sensors", __name__)


@app.route('/sensors/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_static_global_sensor():
    """
    Crea un sensor estatico en el namespace global
        Parametros esperados:
            unique-id: str, id unico del dispositivo
            type: str en ["MOBILE", "STATIC"], tipo de sensor
    :return: sensor creado
    """
    validate_user_logged_in_is_global_user(GLOBAL_SENSORS_VIEW_NAME, Role.CREATE_ACTION)

    sensor_id = request.json.get(Sensor.ID_NAME)
    sensor_id = validate_string_not_empty(sensor_id, Sensor.ID_NAME,
                                          internal_code=GLOBAL_SENSOR_INVALID_ID_ERROR_CODE)

    sensor_type = request.json.get(Sensor.TYPE_NAME)
    sensor_type = SensorType.validate_sensor_type(sensor_type, Sensor.TYPE_NAME,
                                                  GLOBAL_SENSOR_INVALID_TYPE_ERROR_CODE)

    if Sensor.get_by_id(sensor_id) is not None:
        raise EntityAlreadyExists(u"Global Sensor [{0}]".format(sensor_id),
                                  internal_code=GLOBAL_SENSOR_ALREADY_EXISTS_ERROR_CODE)
    return Sensor.create(sensor_id, sensor_type)


@app.route('/sensors/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_all_global_sensors():
    """
    Da la lista de sensores globales
    :return: Lista de sensores globales
    """
    validate_user_logged_in_is_global_user(GLOBAL_SENSORS_VIEW_NAME, Role.READ_ACTION)
    return Sensor.list()


@app.route('/sensors/<string:id_sensor>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_sensor(id_sensor):
    """
    Da el sensor con id dado
    :param id_sensor: id del sensor a recuperar
    :return: sensor con id dado
    """
    validate_user_logged_in_is_global_user(GLOBAL_SENSORS_VIEW_NAME, Role.READ_ACTION)
    return Sensor.get_sensor_by_id(id_sensor)


@app.route('/sensors/<string:id_sensor>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_sensor(id_sensor):
    """
    Da el sensor con id dado
    :param id_sensor: id del sensor a recuperar
    :return: sensor con id dado
    """
    validate_user_logged_in_is_global_user(GLOBAL_SENSORS_VIEW_NAME, Role.DELETE_ACTION)
    return Sensor.delete_sensor_by_id(id_sensor)
