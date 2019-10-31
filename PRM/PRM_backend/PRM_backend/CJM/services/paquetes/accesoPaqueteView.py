# -*- coding: utf-8 -*
from CJM.entidades.paquetes.Acceso import Acceso
from CJM.entidades.paquetes.Paquete import Paquete
from CJM.services.paquetes.paqueteView import get_active_package_for_edit, check_permissions_by_package_location
from CJM.services.validations import validate_amount, ACCESS_INVALID_AMOUNT_INCLUDED_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_id_location
from flask import request, Blueprint

PACKAGE_ACCESSES_VIEW_NAME = "package-accesses"
app = Blueprint(PACKAGE_ACCESSES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/accesses/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_package_access(id_client, id_package):
    """
    Crea un acceso y lo asocia al paquete con id dado en el namespace del cliente id_client
        Parametros esperados:
            id-location: int
            amount-included: int opcional, si no se envia se supone 0 (= ilimitado)
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Acceso creado
    """
    def create_package_access_on_namespace(id_current_client, package, id_location, amount_included):
        return Acceso.create(id_current_client, package, id_location, amount_included)

    check_permissions_by_package_location(id_client, id_package, Role.CREATE_ACTION, PACKAGE_ACCESSES_VIEW_NAME)
    return _get_and_validate_access_json_params(id_client,
                                                id_package,
                                                create_package_access_on_namespace,
                                                Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/accesses/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_accesses(id_client, id_package):
    """
    Lista los accesos del paquete con id dado del cliente correspondiente.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Lista de accesos del paquete
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_consumptions_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)
        return Acceso.list_accesses_by_package(package)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION, PACKAGE_ACCESSES_VIEW_NAME)
    return on_client_namespace(id_client, list_consumptions_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/accesses/<int:id_access>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_access(id_client, id_package, id_access):
    """
    Da el acceso con id dado.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_access: id del acceso asociado
    :return: Acceso con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_access_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)

        return Acceso.get_by_package(package, id_access)

    check_permission_by_access_locations(id_client, id_package, id_access, Role.READ_ACTION, PACKAGE_ACCESSES_VIEW_NAME)
    return on_client_namespace(id_client, get_access_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/accesses/<int:id_access>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_package_access(id_client, id_package, id_access):
    """
    Actualiza el acceso con id dado para el paquete con cliente dado en el cliente con id dado
        Parametros esperados:
            id-location: int
            amount-included: int opcional, si no se envia se supone 0 (= ilimitado)
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_access: id del acceso asociado
    :return: Acceso actualizado
    """

    # noinspection PyUnusedLocal
    def update_package_access_on_namespace(id_current_client, package, id_location, amount_included):
        return Acceso.update(id_access, package, id_location, amount_included)

    check_permission_by_access_locations(id_client, id_package, id_access, Role.UPDATE_ACTION,
                                         PACKAGE_ACCESSES_VIEW_NAME)
    return _get_and_validate_access_json_params(id_client,
                                                id_package,
                                                update_package_access_on_namespace,
                                                Role.UPDATE_ACTION)


def _get_and_validate_access_json_params(id_client, id_package, on_namespace_callback, action):
    id_client = validate_id_client(id_client)

    original_id_location = request.json.get(Acceso.LOCATION_ID_NAME)

    def _get_and_validate_access_json_params_on_namespace(id_current_client):
        amount_included = request.json.get(Acceso.AMOUNT_INCLUDED_NAME)

        if amount_included is not None and amount_included != 0:
            amount_included = validate_amount(amount_included, Acceso.AMOUNT_INCLUDED_NAME,
                                              internal_code=ACCESS_INVALID_AMOUNT_INCLUDED_ERROR_CODE)
        else:
            amount_included = 0
        id_location = validate_id_location(original_id_location, Acceso.LOCATION_ID_NAME)

        package = get_active_package_for_edit(id_package)

        return on_namespace_callback(id_current_client, package, id_location, amount_included)

    return on_client_namespace(id_client, _get_and_validate_access_json_params_on_namespace,
                               action=action,
                               view=PACKAGE_ACCESSES_VIEW_NAME,
                               id_location=original_id_location)


def check_permission_by_access_locations(id_client, id_package, id_access, action, view):
    check_permissions_by_package_location(id_client, id_package, action, view)

    # noinspection PyUnusedLocal
    def check_access_permision_on_namespace(id_current_client):
        try:
            package = Paquete.get_active_package_by_id(id_package)
            access = Acceso.get_by_package(package, id_access)
            id_location = access.idUbicacion
        except EntityDoesNotExists:
            id_location = None

        # Revisar permisos sobre ubicación del paquete
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            action=action,
                            view=view,
                            id_location=id_location)

    on_client_namespace(id_client, check_access_permision_on_namespace, secured=False)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
