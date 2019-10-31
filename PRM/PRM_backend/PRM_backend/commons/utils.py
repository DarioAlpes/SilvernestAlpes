# -*- coding: utf-8 -*
import logging
import time as time_measure
from functools import wraps

from google.appengine.ext.db import Error

from flask import request
import json
from flask_login.utils import login_required
from google.appengine.api import namespace_manager
from google.appengine.api.datastore_errors import Error

from commons.entidades.users import Role
from commons.entidades.users.Role import does_user_has_permission
from commons.excepciones.apiexceptions import ValidationError, InvalidDataFormat, UnauthorizedError
from commons.jsonUtils import to_json

CLIENT_NAMESPACE_NAME_PREFIX = "cliente"


def optional_argument_decorator(func):
    def wrapped_decorator(*args, **kwargs):
        if len(args) == 1 and callable(args[0]):
            return func(args[0])
        else:
            def real_decorator(decoratee):
                return func(decoratee, **kwargs)
        return real_decorator
    return wrapped_decorator


@optional_argument_decorator
def with_json_body(func, with_extra_entities=False):
    """
    Envuelve una función que recibe un request POST en formato JSON y produce un resultado que se debe servir como JSON
    :param func: Función a envolver
    :param with_extra_entities: Indica si la respuesta trae una lista en la que cada elemento es uno o más entidades 
    de diferente tipo. Por ejemplo en el caso de ordenes de persona traera 3 listas, la lista de consumos por dinero, 
    cantidad y las ordenes mismas. Se usa para crear un log diferente por cada tipo de entidad. La(s) entidad(es)
    a retornar en la respuesta deben estar en la primera posición de la lista
    :return: Función que revisa que el request venga en formato json, llama la función func y transforma el resultado a
    JSON
    :raises InvalidDataFormat: La función devuelta genera esta excepción si request.json es None
    """
    @wraps(func)
    def func_wrapper(*args, **kwargs):
        if request.get_json() is None:
            raise InvalidDataFormat(expected_format="JSON")
        else:
            payload = json.dumps(request.get_json())
            return _call_and_log(payload, func, with_extra_entities, *args, **kwargs)
    return func_wrapper


def _call_and_log(payload, func, with_extra_entities, *args, **kwargs):
    from commons.entidades.users.Usuario import Usuario
    from commons.entidades.logs.SuccessLog import SuccessLog
    initial_all = time_measure.time()
    user_before = Usuario.load_current_user()
    id_client = kwargs.get("id_client", None)
    if payload is None or "/users" in request.url:
        payload = ""
    SuccessLog.log_request_values(id_client, user_before, request.method, request.url, payload)
    initial_code = time_measure.time()
    logging.info("Tiempo log petición: " + str(initial_code - initial_all))
    result = func(*args, **kwargs)
    final_code = time_measure.time()
    logging.info("Tiempo código: " + str(final_code - initial_code))
    user_after = Usuario.load_current_user()
    if not with_extra_entities:
        result = [result]
    json_result = None
    for current_result in result:
        current_json_result = to_json(current_result)
        if json_result is None:
            json_result = current_json_result

        if "/secret-keys" in request.url:
            current_json_result = "{}"
        try:
            SuccessLog.create(current_result, current_json_result, id_client, user_before, user_after, request.method,
                              request.url, payload)
        except Error:
            pass
    final_all = time_measure.time()
    logging.info("Tiempo to json y log: " + str(final_all - final_code))
    return json_result


@optional_argument_decorator
def with_json_bodyless(func, with_extra_entities=False):
    """
    Envuelve una función que recibe un request GET y produce un resultado que se debe servir como JSON
    :param func: Función a envolver
    :param with_extra_entities: Indica si la respuesta trae una lista en la que cada elemento es uno o más entidades 
    de diferente tipo. Por ejemplo en el caso de ordenes de persona traera 3 listas, la lista de consumos por dinero, 
    cantidad y las ordenes mismas. Se usa para crear un log diferente por cada tipo de entidad. La(s) entidad(es)
    a retornar en la respuesta deben estar en la primera posición de la lista
    :return: Función que llama la función func y transforma el resultado a JSON
    """
    @wraps(func)
    def func_wrapper(*args, **kwargs):
        return _call_and_log(None, func, with_extra_entities, *args, **kwargs)
    return func_wrapper


def on_client_namespace(id_client, func, *args, **kwargs):
    """
    Cambia el namespace usado por la función por el namespace usado por el cliente con id dado e invoca la función dada
    por parámetro. AL terminar la invocación retorna al namespace original.
    :param id_client: Id del cliente a usar.
    :param func: Funcion a invocar.
    :return: El resultado de inovar la función en el namespace del cliente dado.
    """
    secured = kwargs.pop("secured", True)
    action = kwargs.pop("action", None)
    view = kwargs.pop("view", None)
    id_location = kwargs.pop("id_location", None)
    client = None
    user = None
    user_is_global_admin = False
    if secured:
        previous_namespace = namespace_manager.get_namespace()
        namespace_manager.set_namespace("")
        try:
            from commons.entidades.Cliente import Cliente
            client = Cliente.get_by_id(id_client)
            from commons.entidades.users.Usuario import Usuario
            from commons.entidades.users.Role import is_global_admin
            user = Usuario.load_current_user()
            if user is not None:
                user_is_global_admin = is_global_admin(user)
        except (ValueError, Error) as error:
            client = None
        finally:
            namespace_manager.set_namespace(previous_namespace)

        if client is None:
            raise UnauthorizedError(u"Securing functions outside a client namespace is not supported.")

    def func_wrapper():
        return func(id_client, *args, **kwargs)

    @login_required
    def func_wrapper_with_login():
        if does_user_has_permission(user, id_client, action, view, id_location, user_is_global_admin):
            return func(id_client, *args, **kwargs)
        else:
            raise UnauthorizedError()

    previous_namespace = namespace_manager.get_namespace()
    try:
        namespace_manager.set_namespace('cliente' + str(id_client))
        if secured and client.requiereLogin:
            return func_wrapper_with_login()
        else:
            return func_wrapper()
    finally:
        namespace_manager.set_namespace(previous_namespace)


def validate_user_logged_in_is_global_user(view, action):
    from commons.services.clienteView import REQUIRE_AUTH
    from commons.entidades.users.Usuario import Usuario
    user = Usuario.load_current_user()
    user_is_global_admin = Role.is_global_admin(user)

    @login_required
    def check_permission():
        if not Role.does_user_has_permission(user,
                                             None,
                                             action,
                                             view,
                                             None,
                                             user_is_global_admin):
            raise UnauthorizedError()
    if REQUIRE_AUTH:
        check_permission()


def not_null(menssage):
    def real_not_null(func):
        @wraps(func)
        def func_wrapper():
            ret = func()
            if ret is None:
                raise ValidationError(menssage)
            else:
                return ret
        return func_wrapper
    return real_not_null


def update_descendants_key(hierarchy_type, entity_to_update, internal_code):
    children = hierarchy_type.list_by_parent_id(entity_to_update.key.id())
    child_nodes_ids = dict()
    nodes_by_id = dict()
    unmarked_nodes = set()
    # Transformar en grafo
    for node in children:
        nodes_by_id[node.key.id()] = node
        unmarked_nodes.add(node.key.id())
        if node.idPadre not in child_nodes_ids:
            child_nodes_ids[node.idPadre] = set()
        child_nodes_ids[node.idPadre].add(node.key.id())

    # Obtener orden topologico
    order = []
    temporaly_marked_nodes = set()
    while len(unmarked_nodes) > 0:
        node_id = unmarked_nodes.pop()
        unmarked_nodes.add(node_id)
        _visit_for_topo_sort(hierarchy_type,
                             node_id,
                             child_nodes_ids,
                             temporaly_marked_nodes,
                             unmarked_nodes,
                             order,
                             internal_code)

    # Actualizar llaves en orden topologico inverso
    for node_id in reversed(order):
        node = nodes_by_id[node_id]
        if entity_to_update.key.id() == node_id:
            parent = None
        else:
            parent = nodes_by_id[node.idPadre]
        node.update_key_by_parent(parent)
    return children


def _visit_for_topo_sort(hierarchy_type, node_id, child_nodes_ids, temporaly_marked_nodes, unmarked_nodes, order,
                         internal_code):
    if node_id in temporaly_marked_nodes:
        raise ValidationError(u"There is a cicle on the relationship",
                              internal_code=internal_code)
    if node_id in unmarked_nodes:
        temporaly_marked_nodes.add(node_id)
        for child_id in child_nodes_ids.get(node_id, set()):
            _visit_for_topo_sort(hierarchy_type,
                                 child_id,
                                 child_nodes_ids,
                                 temporaly_marked_nodes,
                                 unmarked_nodes,
                                 order,
                                 internal_code)
        unmarked_nodes.remove(node_id)
        temporaly_marked_nodes.remove(node_id)
        order.append(node_id)
