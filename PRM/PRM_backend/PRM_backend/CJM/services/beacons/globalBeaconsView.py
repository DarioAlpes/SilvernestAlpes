# -*- coding: utf-8 -*
from flask import Blueprint

from CJM.entidades.beacons.GlobalBeacon import GlobalBeacon
from commons.utils import with_json_body, with_json_bodyless

app = Blueprint("global-beacons", __name__)


@app.route('/static-beacons/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_static_global_beacon():
    """
    Crea un beacon estatico en el namespace global
        Parametros esperados:
    :return: beacon creado
    """
    return GlobalBeacon.create_static()


@app.route('/static-beacons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_static_global_beacons():
    """
    Da la lista de beacons estaticos globales
    :return: Lista de beacons estaticos globales
    """
    return GlobalBeacon.list_static()


@app.route('/mobile-beacons/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_mobile_global_beacon():
    """
    Crea un beacon móvil en el namespace global
        Parametros esperados:
    :return: beacon creado
    """
    return GlobalBeacon.create_mobile()


@app.route('/mobile-beacons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_mobile_global_beacons():
    """
    Da la lista de beacons estaticos globales
    :return: Lista de beacons estaticos globales
    """
    return GlobalBeacon.list_mobile()


@app.route('/beacons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_all_global_beacons():
    """
    Da la lista de beacons globales
    :return: Lista de beacons globales
    """
    return GlobalBeacon.list()
