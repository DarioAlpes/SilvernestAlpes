# -*- coding: utf-8 -*
from google.appengine.api.namespace_manager import namespace_manager
from google.appengine.ext import ndb

from commons.entidades.logs.SuccessLog import SuccessLog
from flask_login.utils import login_required

from flask import request, Blueprint
from flask_login.utils import login_user, logout_user

from commons.entidades.users import Role
from commons.entidades.users.Usuario import Usuario
from commons.excepciones.apiexceptions import UnauthorizedError
from commons.utils import with_json_body, with_json_bodyless
from commons.utils import on_client_namespace
from commons.validations import validate_password, validate_id_client, validate_id_user, \
    validate_role, validate_id_locations_list, USER_INVALID_USERNAME_ERROR_CODE, validate_username, \
    validate_string_not_empty
from main import login_limit
from flask import session

USERS_VIEW_NAME = "users"

app = Blueprint(USERS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/users/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_user(id_client):
    """
    Crea un usuario en el namespace del cliente id_client
        Parametros esperados:
            username: str
            password: str
            role: str
            ids-locations: int[] opcional, ids de las ubicaciones en las que aplica el rol. Si se omite se supone
            que aplica en todas las ubicaciones
    :param id_client: id del cliente asociado
    :return: Usuario creado
    """
    id_client = validate_id_client(id_client)

    def create_user_on_namespace(id_current_client):
        username = request.json.get(Usuario.USERNAME_NAME)
        username = validate_username(username, Usuario.USERNAME_NAME,
                                     internal_code=USER_INVALID_USERNAME_ERROR_CODE)
        password = request.json.get(Usuario.PASSWORD_NAME)
        password = validate_password(password, username, Usuario.PASSWORD_NAME)

        role = request.json.get(Usuario.ROLE_NAME)
        role = validate_role(role, Usuario.ROLE_NAME)

        ids_locations = request.json.get(Usuario.LOCATIONS_IDS_NAME)
        if ids_locations is not None:
            ids_locations = validate_id_locations_list(ids_locations, Usuario.LOCATIONS_IDS_NAME,
                                                       allow_empty_list=False)
        roles_and_applicable_locations = {role: ids_locations}
        return Usuario.create(id_current_client, username, password, roles_and_applicable_locations)

    return on_client_namespace(id_client, create_user_on_namespace, action=Role.CREATE_ACTION, view=USERS_VIEW_NAME)


@app.route('/clients/<int:id_client>/users/<string:username>/', methods=['PATCH'], strict_slashes=False)
@with_json_body
def change_password(id_client, username):
    """
    Cambia la contraseña de un usuario en el namespace del cliente id_client
        Parametros esperados:
            password: str
    :param id_client: id del cliente asociado
    :param username: username del usuario
    :return: Usuario modificado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def change_password_on_namespace(id_current_client):
        new_username = validate_id_user(username)

        password = request.json.get(Usuario.PASSWORD_NAME)
        password = validate_password(password, new_username, Usuario.PASSWORD_NAME)

        return Usuario.change_password(new_username, password)

    user = Usuario.load_current_user()
    secured = user is None or user.key.id() != username or user.idCliente != id_client
    return on_client_namespace(id_client, change_password_on_namespace, action=Role.UPDATE_ACTION, view=USERS_VIEW_NAME,
                               secured=secured)


@app.route('/clients/<int:id_client>/users/<string:username>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_user(id_client, username):
    """
    Elimina el usuario dado en el namespace del cliente id_client
    :param id_client: id del cliente asociado
    :param username: username del usuario
    :return: Usuario eliminado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def create_user_on_namespace(id_current_client):
        new_username = validate_id_user(username)
        return Usuario.delete(new_username)

    return on_client_namespace(id_client, create_user_on_namespace, action=Role.DELETE_ACTION, view=USERS_VIEW_NAME)


@app.route('/clients/<int:id_client>/users/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_users(id_client):
    """
    Da la lista de usuarios del cliente con id dado
    :param id_client: id del cliente a consultar
    :return: Lista de usuarios del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_users_on_namespace(id_current_client):
        return Usuario.list()

    return on_client_namespace(id_client, list_users_on_namespace, action=Role.READ_ACTION, view=USERS_VIEW_NAME)


@app.route('/clients/<int:id_client>/users/<string:username>/logout/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def logout_client_user(id_client, username):
    @login_required
    def logout_user_on_namespace(id_current_client):
        user = Usuario.load_current_user(apply_timeout=False)
        if user is None or user.username != username or user.idCliente != id_current_client:
            raise UnauthorizedError(u"Invalid username")
        else:
            logout_user()
            return Usuario.get_by_id(username)

    return on_client_namespace(id_client, logout_user_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/users/<string:username>/login/', methods=['POST'], strict_slashes=False)
@with_json_body
@login_limit
def authenticate_user(id_client, username):
    id_client = validate_id_client(id_client)

    password = request.json.get(Usuario.PASSWORD_NAME)
    password = validate_string_not_empty(password, Usuario.PASSWORD_NAME)

    # noinspection PyUnusedLocal
    def authenticate_user_on_namespace(id_current_client):
        user = Usuario.get_by_id(username)
        if user is None or not user.validate_password(password):
            raise UnauthorizedError(u"Invalid credentials")
        else:
            login_user(user)
            session.permanent = True
            return user

    return on_client_namespace(id_client, authenticate_user_on_namespace, secured=False)


@app.route('/users/<string:username>/login/', methods=['POST'], strict_slashes=False)
@with_json_body
@login_limit
def authenticate_global_user(username):
    password = request.json.get(Usuario.PASSWORD_NAME)
    user = Usuario.get_by_id(username)
    if user is None or not user.validate_password(password):
        raise UnauthorizedError(u"Invalid credentials")
    else:
        login_user(user)
        session.permanent = True
        return user


@app.route('/users/<string:username>/logout/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
@login_required
def logout_global_user(username):
    user = Usuario.load_current_user(apply_timeout=False)
    if user is None or user.username != username or user.idCliente is not None:
        raise UnauthorizedError(u"Invalid username")
    else:
        logout_user()
        return Usuario.get_by_id(username)


@app.route('/users/<string:username>/', methods=['PATCH'], strict_slashes=False)
@with_json_body
def change_global_user_password(username):
    """
    Cambia la contraseña de un usuario global
        Parametros esperados:
            password: str
    :param username: username del usuario
    :return: Usuario modificado
    """
    user = Usuario.load_current_user()

    if user is None or user.username != username or user.idCliente is not None:
        raise UnauthorizedError()
    else:
        password = request.json.get(Usuario.PASSWORD_NAME)
        password = validate_password(password, username, Usuario.PASSWORD_NAME)

        return Usuario.change_password(username, password)


def load_user_from_user_token(username):
    try:
        user_parts = username.split('$')
        id_client = int(user_parts[0])
        if id_client > 0:
            # noinspection PyUnusedLocal
            def get_user_on_namespace(id_current_client):
                return Usuario.get_by_id(user_parts[1])
            return on_client_namespace(id_client, get_user_on_namespace, secured=False)
        else:
            return Usuario.get_by_id(user_parts[1])
    except ValueError:
        return None


@ndb.tasklet
def get_username_key_async(id_client, entity_key):
    if entity_key is not None:
        previous_namespace = namespace_manager.get_namespace()
        namespace_manager.set_namespace("")
        user_key = yield SuccessLog.get_user_for_creation_by_client_and_entity_key_async(id_client, entity_key)
        namespace_manager.set_namespace(previous_namespace)
    else:
        user_key = None
    if user_key is None or user_key.user_key is None:
        username = SuccessLog.ANONYMOUS
    else:
        username = user_key.user_key.id()
    raise ndb.Return(username)
