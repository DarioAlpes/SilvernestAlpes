# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.beacons.BeaconType import BeaconType
from CJM.entidades.beacons.ClientBeacon import ClientBeacon
from CJM.entidades.beacons.GlobalBeacon import GlobalBeacon
from CJM.services.validations import validate_major, validate_minor, validate_id_person, validate_id_client_beacon,\
    validate_id_global_beacon
from commons.entidades.Generador import Generador
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_body, with_json_bodyless
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_id_location
from google.appengine.ext import ndb

app = Blueprint("client-beacons", __name__)


@app.route('/clients/<int:id_client>/beacons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_all_client_beacons(id_client):
    """
    Da la lista de beacons del cliente con id dado
    :param id_client: id del cliente asociado
    :return: Lista de beacons del cliente con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_all_client_beacons_on_namespace(id_current_client):
        return ClientBeacon.list()

    return on_client_namespace(id_client, list_all_client_beacons_on_namespace)


@app.route('/clients/<int:id_client>/static-beacons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_client_static_beacons(id_client):
    """
    Da la lista de beacons estaticos del cliente con id dado
    :param id_client: id del cliente asociado
    :return: Lista de beacons estaticos del cliente con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_client_static_beacons_on_namespace(id_current_client):
        return ClientBeacon.list_static()

    return on_client_namespace(id_client, list_client_static_beacons_on_namespace)


@app.route('/clients/<int:id_client>/mobile-beacons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_client_mobile_beacons(id_client):
    """
    Da la lista de beacons moviles del cliente con id dado
    :param id_client: id del cliente asociado
    :return: Lista de beacons moviles del cliente con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_client_mobile_beacons_on_namespace(id_current_client):
        return ClientBeacon.list_mobile()

    return on_client_namespace(id_client, list_client_mobile_beacons_on_namespace)


@app.route('/clients/<int:id_client>/static-beacons/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_static_beacon_for_client(id_client):
    """
    Crea un beacon estatico en el namespace del cliente id_client
        Parametros esperados:
            major: int mayor o igual a 0
            minor: int mayor o igual a 0
            id-location: int
            id-global-beacon: int opcional. Id del beacon global a asignar, si no viene se crea un nuevo beacon,
                            si viene se asigna el beacon correspondiente

    :param id_client: id del cliente asociado
    :return: beacon creado
    """
    id_client = validate_id_client(id_client)

    @ndb.transactional(xg=True)
    def create_static_beacon_for_client_on_transaction():
        major = request.json.get(ClientBeacon.MAJOR_NAME)
        major = validate_major(major, ClientBeacon.MAJOR_NAME)

        minor = request.json.get(ClientBeacon.MINOR_NAME)
        minor = validate_minor(minor, ClientBeacon.MINOR_NAME)

        id_global_beacon = request.json.get(ClientBeacon.GLOBAL_BEACON_ID_NAME)
        is_global = id_global_beacon is not None
        id_beacon = None

        if is_global:
            id_beacon = validate_id_global_beacon(id_global_beacon)
            GlobalBeacon.register_client(id_beacon, id_client, BeaconType.STATIC_BEACON_TYPE)
        else:
            id_beacon = Generador.get_next_beacon_id()

        def create_static_beacon_on_namespace(id_current_client):
            id_location = request.json.get(ClientBeacon.UBICACION_ID_NAME)
            id_location = validate_id_location(id_location, ClientBeacon.UBICACION_ID_NAME)

            return ClientBeacon.create_static(id_beacon, id_current_client, id_location, major, minor, is_global)

        return on_client_namespace(id_client, create_static_beacon_on_namespace, secured=False)

    on_client_namespace(id_client, _dummy_function_for_login_check)
    return create_static_beacon_for_client_on_transaction()


@app.route('/clients/<int:id_client>/static-beacons/<int:id_beacon>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def unregister_static_beacon_for_client(id_client, id_beacon):
    """
    Elimina el beacon  estatico con id dado del namespace del cliente. Si es un beacon global lo marca como no asignado
    en el namespace global
    :param id_client: id del cliente asociado
    :param id_beacon: id del beacon a eliminar
    :return: beacon eliminado
    """
    id_client = validate_id_client(id_client)

    @ndb.transactional(xg=True)
    def unregister_static_beacon_for_client_on_transaction():
        # noinspection PyUnusedLocal
        def unregister_static_beacon_on_namespace(id_current_client):
            id_current_beacon = validate_id_client_beacon(id_beacon)
            beacon_to_modify = ClientBeacon.get_by_id(id_current_beacon)

            if beacon_to_modify.tipo != BeaconType.STATIC_BEACON_TYPE:
                raise ValidationError(u"Expected static Beacon, got {0} Beacon."
                                      .format(BeaconType.beacon_type_to_string(beacon_to_modify.tipo)))
            beacon_to_modify.key.delete()

            return beacon_to_modify

        result = on_client_namespace(id_client, unregister_static_beacon_on_namespace, secured=False)

        if result.esGlobal:
            GlobalBeacon.unregister_client(id_beacon)

        return result

    on_client_namespace(id_client, _dummy_function_for_login_check)
    return unregister_static_beacon_for_client_on_transaction()


@app.route('/clients/<int:id_client>/mobile-beacons/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_mobile_beacon_for_client(id_client):
    """
    Crea un beacon móvil en el namespace del cliente id_client
        Parametros esperados:
            major: int mayor o igual a 0
            minor: int mayor o igual a 0
            id-global-beacon: int opcional. Id del beacon global a asignar, si no viene se crea un nuevo beacon,
                            si viene se asigna el beacon correspondiente
    :param id_client: id del cliente asociado
    :return: beacon creado
    """
    id_client = validate_id_client(id_client)

    @ndb.transactional(xg=True)
    def create_mobile_beacon_for_client_on_transaction():
        major = request.json.get(ClientBeacon.MAJOR_NAME)
        major = validate_major(major, ClientBeacon.MAJOR_NAME)

        minor = request.json.get(ClientBeacon.MINOR_NAME)
        minor = validate_minor(minor, ClientBeacon.MINOR_NAME)

        id_global_beacon = request.json.get(ClientBeacon.GLOBAL_BEACON_ID_NAME)
        is_global = id_global_beacon is not None
        id_beacon = None

        if is_global:
            id_beacon = validate_id_global_beacon(id_global_beacon)
            GlobalBeacon.register_client(id_beacon, id_client, BeaconType.MOBILE_BEACON_TYPE)
        else:
            id_beacon = Generador.get_next_beacon_id()

        def create_mobile_beacon_on_namespace(id_current_client):
            return ClientBeacon.create_mobile(id_beacon, id_current_client, major, minor, is_global)

        return on_client_namespace(id_client, create_mobile_beacon_on_namespace, secured=False)

    on_client_namespace(id_client, _dummy_function_for_login_check)
    return create_mobile_beacon_for_client_on_transaction()


@app.route('/clients/<int:id_client>/mobile-beacons/<int:id_beacon>/', methods=['PATCH'], strict_slashes=False)
@with_json_body
def register_mobile_beacon_for_client(id_client, id_beacon):
    """
    Asigna una persona al beacon correspondiente del cliente correspondiente
        Parametros esperados:
            id-person: int
    :param id_client: id del cliente asociado
    :param id_beacon: id del beacon a registrar
    :return: beacon actualizado
    """
    id_client = validate_id_client(id_client)

    @ndb.transactional(xg=True)
    def register_mobile_beacon_for_client_on_transaction():
        def register_mobile_beacon_on_namespace(id_current_client):
            id_person = request.json.get(ClientBeacon.PERSON_ID_NAME)
            id_person = validate_id_person(id_person, ClientBeacon.PERSON_ID_NAME)

            id_current_beacon = validate_id_client_beacon(id_beacon)

            beacon_to_modify = ClientBeacon.get_by_id(id_current_beacon)

            if beacon_to_modify.tipo != BeaconType.MOBILE_BEACON_TYPE:
                raise ValidationError(u"Expected mobile Beacon, got {0} Beacon."
                                      .format(BeaconType.beacon_type_to_string(beacon_to_modify.tipo)))

            return ClientBeacon.register_person(id_current_client, id_person, id_current_beacon)

        return on_client_namespace(id_client, register_mobile_beacon_on_namespace, secured=False)

    on_client_namespace(id_client, _dummy_function_for_login_check)
    return register_mobile_beacon_for_client_on_transaction()


@app.route('/clients/<int:id_client>/mobile-beacons/<int:id_beacon>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def unregister_mobile_beacon_for_client(id_client, id_beacon):
    """
    Elimina el beacon  móvil con id dado del namespace del cliente. Si es un beacon global lo marca como no asignado
    en el namespace global
    :param id_client: id del cliente asociado
    :param id_beacon: id del beacon a eliminar
    :return: beacon eliminado
    """
    id_client = validate_id_client(id_client)

    @ndb.transactional(xg=True)
    def unregister_mobile_beacon_for_client_on_transaction():
        # noinspection PyUnusedLocal
        def unregister_mobile_beacon_on_namespace(id_current_client):
            id_current_beacon = validate_id_client_beacon(id_beacon)
            beacon_to_modify = ClientBeacon.get_by_id(id_current_beacon)

            if beacon_to_modify.tipo != BeaconType.MOBILE_BEACON_TYPE:
                raise ValidationError(u"Expected a mobile Beacon, got a {0} Beacon."
                                      .format(BeaconType.beacon_type_to_string(beacon_to_modify.tipo)))
            beacon_to_modify.key.delete()

            return beacon_to_modify

        result = on_client_namespace(id_client, unregister_mobile_beacon_on_namespace, secured=False)

        if result.esGlobal:
            GlobalBeacon.unregister_client(id_beacon)

        return result

    on_client_namespace(id_client, _dummy_function_for_login_check)
    return unregister_mobile_beacon_for_client_on_transaction()


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
