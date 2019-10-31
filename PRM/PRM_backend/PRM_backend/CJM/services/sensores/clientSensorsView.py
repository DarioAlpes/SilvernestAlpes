# -*- coding: utf-8 -*
from commons.entidades.users import Role
from flask import request, Blueprint

from CJM.entidades.sensores.Sensor import Sensor
from CJM.services.validations import validate_id_person, CLIENT_SENSOR_INVALID_ACTIVE_ERROR_CODE, \
    CLIENT_SENSOR_INVALID_SYNCED_ERROR_CODE
from commons.excepciones.apiexceptions import APIException
from commons.utils import with_json_body, with_json_bodyless, validate_user_logged_in_is_global_user
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_id_location, validate_bool_not_empty

app = Blueprint("client-sensors", __name__)

CLIENT_SENSORS_VIEW_NAME = "client-sensors"


@app.route('/clients/<int:id_client>/sensors/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_all_client_sensors(id_client):
    """
    Da la lista de sensores del cliente con id dado
    :param id_client: id del cliente asociado
    :return: Lista de sensores del cliente con id dado
    """
    id_client = validate_id_client(id_client)

    on_client_namespace(id_client, _dummy_function_for_login_check, action=Role.READ_ACTION,
                        view=CLIENT_SENSORS_VIEW_NAME)

    return Sensor.list_by_id_client(id_client)


@app.route('/clients/<int:id_client>/sensors/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_sensor_for_client(id_client):
    """
    Crea un sensor en el namespace del cliente id_client
        Parametros esperados:
            id: int. Id del sensor global a asignar al cliente
            id-location: int opcional, solo para sensores estaticos. Ubicación del sensor
            id-person: int opcional, solo para sensores moviles. Persona asociada al sensor
    :param id_client: id del cliente asociado
    :return: sensor creado
    """
    validate_user_logged_in_is_global_user(CLIENT_SENSORS_VIEW_NAME, Role.DELETE_ACTION)
    id_client = validate_id_client(id_client)

    id_sensor = request.json.get(Sensor.ID_NAME)
    sensor = Sensor.get_sensor_by_id(id_sensor)

    def create_sensor_on_namespace(id_current_client):
        id_location = request.json.get(Sensor.LOCATION_ID_NAME)
        if id_location is not None:
            id_location = validate_id_location(id_location, Sensor.LOCATION_ID_NAME)
        id_person = request.json.get(Sensor.PERSON_ID_NAME)
        if id_person is not None:
            id_person = validate_id_person(id_person, Sensor.PERSON_ID_NAME)

        sensor.assign_client(id_current_client)
        sensor.assign_location(id_location)
        sensor.assign_person(id_person)

    on_client_namespace(id_client, create_sensor_on_namespace, secured=False)
    sensor.put()
    return sensor


@app.route('/clients/<int:id_client>/sensors/<string:id_sensor>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_sensor_for_client(id_client, id_sensor):
    """
    Da el sensor con id dado para el cliente id_client
    :param id_client: id del cliente asociado
    :param id_sensor: id del sensor asociado
    :return: sensor con id dado
    """
    return get_sensor_by_id_client_and_id_sensor(id_client, id_sensor, Role.READ_ACTION)


@app.route('/clients/<int:id_client>/sensors/<string:id_sensor>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_sensor_for_client(id_client, id_sensor):
    """
    Elimina el sensor con id dado para el cliente id_client
    :param id_client: id del cliente asociado
    :param id_sensor: id del sensor asociado
    :return: sensor eliminado
    """
    validate_user_logged_in_is_global_user(CLIENT_SENSORS_VIEW_NAME, Role.DELETE_ACTION)
    sensor = get_sensor_by_id_client_and_id_sensor(id_client, id_sensor, Role.DELETE_ACTION)
    original_sensor = sensor.clone()
    sensor.remove_client()
    sensor.put()
    return original_sensor


@app.route('/clients/<int:id_client>/sensors/<string:id_sensor>/', methods=['PATCH'], strict_slashes=False)
@with_json_body
def activate_or_deactivate_sensor_for_client(id_client, id_sensor):
    """
    Parametros esperados:
        active: bool. True si se esta activando el sensor, false en caso contrario
    :param id_client: id del cliente asociado
    :param id_sensor: id del sensor asociado
    :return: sensor con id dado
    """
    sensor = get_sensor_by_id_client_and_id_sensor(id_client, id_sensor, Role.UPDATE_ACTION)

    active = request.json.get(Sensor.ACTIVE_NAME)
    active = validate_bool_not_empty(active, Sensor.ACTIVE_NAME, internal_code=CLIENT_SENSOR_INVALID_ACTIVE_ERROR_CODE)

    synced = request.json.get(Sensor.SYNCED_NAME)
    synced = validate_bool_not_empty(synced, Sensor.SYNCED_NAME, internal_code=CLIENT_SENSOR_INVALID_SYNCED_ERROR_CODE)

    # Importa el orden, si se desactiva y sincroniza al tiempo es importante que la fecha de sincronizacion sea mayor
    # o igual a la fecha de desactivación
    sensor.change_active_status(active)
    sensor.register_synced(synced)
    sensor.put()
    return sensor


def get_sensor_by_id_client_and_id_sensor(id_client, id_sensor, action):
    id_client = validate_id_client(id_client)

    error = None
    try:
        sensor = Sensor.get_sensor_by_id(id_sensor)
        id_location = sensor.idUbicacion
    except APIException as error:
        id_location = None
        sensor = None

    on_client_namespace(id_client, _dummy_function_for_login_check, action=action,
                        view=CLIENT_SENSORS_VIEW_NAME, id_location=id_location)

    if error is not None:
        raise error

    sensor.check_id_client(id_client, id_sensor)

    return sensor


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
