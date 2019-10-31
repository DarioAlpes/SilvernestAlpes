# -*- coding: utf-8 -*
from commons.entidades.ClientKey import ClientKey
from flask import request, Blueprint

from commons.entidades.Cliente import Cliente
from commons.entidades.users import Role
from commons.utils import with_json_body, with_json_bodyless, validate_user_logged_in_is_global_user, \
    on_client_namespace
from commons.validations import validate_string_not_empty, validate_id_client, validate_bool_not_empty, \
    CLIENT_INVALID_NAME_ERROR_CODE, CLIENT_INVALID_REQUIRES_LOGIN_ERROR_CODE, validate_external_person_service, \
    CLIENT_INVALID_EXTERNAL_PERSON_SERVICE_ERROR_CODE, CLIENT_INVALID_EXTERNAL_RESERVATIONS_SERVICE_ERROR_CODE

CLIENT_KEYS_VIEW_NAME = "client-keys"
CLIENTS_VIEW_NAME = "clients"
app = Blueprint("clients", __name__)

REQUIRE_AUTH = False


def enable_auth():
    global REQUIRE_AUTH
    REQUIRE_AUTH = True


@app.route('/clients/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_client():
    """
    Crea un cliente en el namespace global
        Parametros esperados:
            name: str
            requires-login: bool, opcional (False por defecto).
            Indica si los servicios de este cliente requieren login.
            external-person-service: str, opciona. Nombre del servicio externo a usar para obtener datos de personas.
            external-reservations-service: str, opciona. Nombre del servicio externo a usar para obtener datos de
            reservas.
    :return: Cliente creado
    """
    validate_user_logged_in_is_global_user(CLIENTS_VIEW_NAME, Role.CREATE_ACTION)

    def create_client_with_params(client_name, requires_login, external_person_service, external_reservations_service):
        return Cliente.create(client_name, requires_login, external_person_service, external_reservations_service)

    return _get_and_validate_client_json_params(create_client_with_params)


@app.route('/clients/<int:id_client>/', methods=['PUT'], strict_slashes=False)
@with_json_body
def update_client(id_client):
    """
    Actualiza un cliente en el namespace global
        Parametros esperados:
            name: str
    :return: Cliente actualizado
    """
    validate_user_logged_in_is_global_user(CLIENTS_VIEW_NAME, Role.UPDATE_ACTION)
    id_client = validate_id_client(id_client)

    def update_client_with_params(client_name, requires_login, external_person_service, external_reservations_service):
        return Cliente.update(id_client, client_name, requires_login, external_person_service,
                              external_reservations_service)

    return _get_and_validate_client_json_params(update_client_with_params)


@app.route('/clients/<int:id_client>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_client(id_client):
    """
    Elimina un cliente en el namespace global
    :return: Cliente eliminado
    """
    validate_user_logged_in_is_global_user(CLIENTS_VIEW_NAME, Role.DELETE_ACTION)
    id_client = validate_id_client(id_client)
    return Cliente.delete(id_client)


@app.route('/clients/<int:id_client>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_client(id_client):
    """
    Da el cliente con id dado.
    :return: Cliente con id dado.
    """
    validate_user_logged_in_is_global_user(CLIENTS_VIEW_NAME, Role.READ_ACTION)
    id_client = validate_id_client(id_client)
    return Cliente.get_by_id(id_client)


@app.route('/_ah/warmup/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def warmup():
    """
    Prepara el app para warmup
    :return:
    """
    return "{}"


@app.route('/_ah/start/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def start():
    """
    Prepara el app para empezar
    :return:
    """
    return "{}"


@app.route('/_ah/stop/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def stop():
    """
    Prepara el app para parar
    :return:
    """
    return "{}"


@app.route('/clients/<int:id_client>/secret-keys/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_client_keys(id_client):
    """
    Da las llaves secretas del cliente. En este momento solo retorna una lista con un único campo, tag-key
    :return: Llaves secretas del cliente. En este momento solo retorna una lista con un único campo, tag-key
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_client_keys_on_namespace(id_current_client):
        key_lists = ClientKey.list()
        if len(key_lists) == 0:
            key_lists = [ClientKey.create()]
        return key_lists

    return on_client_namespace(id_client, get_client_keys_on_namespace, action=Role.READ_ACTION,
                               view=CLIENT_KEYS_VIEW_NAME)


@app.route('/clients/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_clients():
    """
    Da la lista de todos los clientes.
    :return: Lista de clientes.
    """
    validate_user_logged_in_is_global_user(CLIENTS_VIEW_NAME, Role.READ_ACTION)
    return Cliente.list()


def _get_and_validate_client_json_params(callback):
    client_name = request.get_json().get(Cliente.CLIENT_NAME_NAME)
    client_name = validate_string_not_empty(client_name,
                                            field_name=Cliente.CLIENT_NAME_NAME,
                                            internal_code=CLIENT_INVALID_NAME_ERROR_CODE)
    requires_login = request.get_json().get(Cliente.CLIENT_REQUIRES_LOGIN_NAME)
    if requires_login is None:
        requires_login = False
    else:
        requires_login = validate_bool_not_empty(requires_login, Cliente.CLIENT_REQUIRES_LOGIN_NAME,
                                                 internal_code=CLIENT_INVALID_REQUIRES_LOGIN_ERROR_CODE)
    external_person_service = request.get_json().get(Cliente.EXTERNAL_PERSON_SERVICE_NAME)
    if external_person_service is not None:
        external_person_service = validate_external_person_service(external_person_service,
                                                                   Cliente.EXTERNAL_PERSON_SERVICE_NAME,
                                                                   internal_code=CLIENT_INVALID_EXTERNAL_PERSON_SERVICE_ERROR_CODE)

    external_reservations_service = request.get_json().get(Cliente.EXTERNAL_RESERVATIONS_SERVICE_NAME)
    if external_reservations_service is not None:
        external_reservations_service = validate_external_person_service(external_reservations_service,
                                                                         Cliente.EXTERNAL_RESERVATIONS_SERVICE_NAME,
                                                                         internal_code=CLIENT_INVALID_EXTERNAL_RESERVATIONS_SERVICE_ERROR_CODE)
    return callback(client_name, requires_login, external_person_service, external_reservations_service)
