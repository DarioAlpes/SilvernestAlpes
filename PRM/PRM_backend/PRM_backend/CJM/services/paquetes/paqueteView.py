# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.paquetes.Acceso import Acceso
from CJM.entidades.paquetes.ConsumoCantidad import ConsumoCantidad
from CJM.entidades.paquetes.ConsumoDinero import ConsumoDinero
from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.paquetes.ReglaPrecioPaquete import ReglaPrecioPaquete
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.services.validations import validate_money, \
    PACKAGE_INVALID_NAME_ERROR_CODE, PACKAGE_INVALID_PRICE_ERROR_CODE, PACKAGE_INVALID_DESCRIPTION_ERROR_CODE, \
    PACKAGE_INVALID_RESTRICTED_CONSUMPTION_ERROR_CODE, PACKAGE_INVALID_VALID_FROM_ERROR_CODE, \
    PACKAGE_INVALID_VALID_THROUGH_ERROR_CODE, \
    PACKAGE_INVALID_RANGE_OF_DATES_FROM_GREATER_THAN_THROUGH_ERROR_CODE, PACKAGE_INVALID_BASE_TIME_ERROR_CODE, \
    validate_percentage, PACKAGE_INVALID_TAX_RATE_ERROR_CODE, PACKAGE_INVALID_EXTERNAL_CODE_ERROR_CODE, \
    PACKAGE_INVALID_AVAILABLE_FOR_SALE_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError, EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_string_not_empty, validate_bool_not_empty, \
    validate_id_location, DEFAULT_DATE_FORMAT, validate_datetime

PACKAGES_VIEW_NAME = "packages"
app = Blueprint(PACKAGES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/packages/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_package(id_client):
    """
    Crea un paquete en el namespace del cliente id_client
        Parametros esperados:
            name: str
            base-price: float
            tax-rate: float
            description: str
            restricted-consumption: bool
            valid-from: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
            valid-through: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
            id-social-event: int opcional
            id-location: int
            external-code: str
            available-for-sale: bool, opcional (true por defecto)
    :param id_client: id del cliente asociado
    :return: Paquete creado
    """
    def create_package_on_namespace(id_current_client, name, price, tax_rate, description, restricted_consumption,
                                    valid_from, valid_through, id_location, external_code, available_for_sale):

        return Paquete.create(id_current_client, name, price, tax_rate, description, restricted_consumption, valid_from,
                              valid_through, id_location, external_code, available_for_sale)

    return _get_and_validate_package_json_params(id_client, create_package_on_namespace, Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/packages/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_active_packages(id_client):
    """
    Lista los paquetes del cliente correspondiente.
    :param id_client: id del cliente asociado
    :return: Lista de paquetes del cliente
        Parametros esperados:
            base-time: str opcional en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_active_packages_on_namespace(id_current_client):
        base_time = request.args.get(Paquete.BASE_TIME_NAME)
        if base_time is not None:
            base_time = validate_datetime(base_time, Paquete.BASE_TIME_NAME,
                                          internal_code=PACKAGE_INVALID_BASE_TIME_ERROR_CODE)
        return Paquete.list_active_packages_with_base_time(base_time)

    return on_client_namespace(id_client, list_active_packages_on_namespace,
                               action=Role.READ_ACTION,
                               view=PACKAGES_VIEW_NAME)


@app.route('/clients/<int:id_client>/all-packages/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_all_packages(id_client):
    """
    Lista los paquetes del cliente correspondiente.
    :param id_client: id del cliente asociado
    :return: Lista de paquetes del cliente
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_all_packages_on_namespace(id_current_client):
        return Paquete.list()

    return on_client_namespace(id_client, list_all_packages_on_namespace,
                               action=Role.READ_ACTION,
                               view=PACKAGES_VIEW_NAME)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_package(id_client, id_package):
    """
    Da el paquete con id dado.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Paquete con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_package_on_namespace(id_current_client):
        return Paquete.get_active_package_by_id(id_package)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION, PACKAGES_VIEW_NAME)
    return on_client_namespace(id_client, get_package_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/', methods=['PUT'], strict_slashes=False)
@with_json_body
def update_package(id_client, id_package):
    """
    Actualiza el paquete con id dado.
        Parametros esperados:
            name: str
            base-price: float
            tax-rate: float
            description: str
            restricted-consumption: bool
            valid-from: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
            valid-through: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
            id-location: int
            external-code: str
            available-for-sale: bool, opcional (true por defecto)
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Paquete actualizado
    """
    def update_package_on_namespace(id_current_client, name, price, tax_rate, description, restricted_consumption, valid_from,
                                    valid_through, id_location, external_code, available_for_sale):
        package = get_active_package_for_edit(id_package)
        package.update(id_current_client, name, price, tax_rate, description, restricted_consumption, valid_from,
                       valid_through, id_location, external_code, available_for_sale)

        return package

    check_permissions_by_package_location(id_client, id_package, Role.UPDATE_ACTION, PACKAGES_VIEW_NAME)
    return _get_and_validate_package_json_params(id_client, update_package_on_namespace, Role.UPDATE_ACTION)


def get_active_package_for_edit(id_package):
    """
    Consulta el paquete activo con id dado. Si ya existen reservas asociadas a ese paquete crea un nuevo paquete
    identico para su edición. El objectivo es mantener los paquetes que se vendieron con anterioridad
    :param id_package:
    :return:
    """
    package = Paquete.get_active_package_by_id(id_package)
    create_new_package = ReservaPersona.get_number_person_reservations_specific_package(package) > 0
    if create_new_package:
        new_package = Paquete.clone_package_for_edit(package)
        Acceso.clone_package_accesses(package, new_package)
        ConsumoCantidad.clone_package_amount_consumptions(package, new_package)
        ConsumoDinero.clone_package_money_consumptions(package, new_package)
        ReglaPrecioPaquete.clone_package_rules(package, new_package)
        package = new_package
    return package


def check_permissions_by_package_location(id_client, id_package, action, view):
    # noinspection PyUnusedLocal
    def check_active_package_permision_on_namespace(id_current_client):
        try:
            package = Paquete.get_active_package_by_id(id_package)
            id_location = package.idUbicacion
        except EntityDoesNotExists:
            id_location = None

        # Revisar permisos sobre ubicación del paquete
        on_client_namespace(id_client, _dummy_function_for_login_check,
                            action=action,
                            view=view,
                            id_location=id_location)

    return on_client_namespace(id_client, check_active_package_permision_on_namespace, secured=False)


def _get_and_validate_package_json_params(id_client, on_namespace_callback, action):
    id_client = validate_id_client(id_client)

    original_id_location = request.json.get(Paquete.LOCATION_ID_NAME)

    def _get_and_validate_package_json_params_on_namespace(id_current_client):
        name = request.json.get(Paquete.PACKAGE_NAME_NAME)
        name = validate_string_not_empty(name, Paquete.PACKAGE_NAME_NAME, internal_code=PACKAGE_INVALID_NAME_ERROR_CODE)

        price = request.json.get(Paquete.BASE_PRICE_NAME)
        price = validate_money(price, Paquete.BASE_PRICE_NAME, allow_zero=True,
                               internal_code=PACKAGE_INVALID_PRICE_ERROR_CODE)

        tax_rate = request.json.get(Paquete.TAX_RATE_NAME)
        tax_rate = validate_percentage(tax_rate, Paquete.TAX_RATE_NAME, allow_zero=True,
                                       internal_code=PACKAGE_INVALID_TAX_RATE_ERROR_CODE)

        description = request.json.get(Paquete.DESCRIPTION_NAME)
        description = validate_string_not_empty(description, Paquete.DESCRIPTION_NAME,
                                                internal_code=PACKAGE_INVALID_DESCRIPTION_ERROR_CODE)

        external_code = request.json.get(Paquete.EXTERNAL_CODE_NAME)
        if external_code is not None:
            external_code = validate_string_not_empty(external_code, Paquete.EXTERNAL_CODE_NAME,
                                                      internal_code=PACKAGE_INVALID_EXTERNAL_CODE_ERROR_CODE)

        available_for_sale = request.json.get(Paquete.AVAILABLE_FOR_SALE)
        if available_for_sale is None:
            available_for_sale = True
        else:
            available_for_sale = validate_bool_not_empty(available_for_sale, Paquete.AVAILABLE_FOR_SALE,
                                                         internal_code=PACKAGE_INVALID_AVAILABLE_FOR_SALE_ERROR_CODE)

        restricted_consumption = request.json.get(Paquete.RESTRICTED_CONSUMPTION_NAME)
        restricted_consumption = validate_bool_not_empty(restricted_consumption, Paquete.RESTRICTED_CONSUMPTION_NAME,
                                                         internal_code=PACKAGE_INVALID_RESTRICTED_CONSUMPTION_ERROR_CODE)

        valid_from = request.json.get(Paquete.VALID_FROM_NAME)
        valid_from = validate_datetime(valid_from, Paquete.VALID_FROM_NAME,
                                       internal_code=PACKAGE_INVALID_VALID_FROM_ERROR_CODE, allow_none=False)

        valid_through = request.json.get(Paquete.VALID_THROUGH_NAME)
        valid_through = validate_datetime(valid_through, Paquete.VALID_THROUGH_NAME,
                                          internal_code=PACKAGE_INVALID_VALID_THROUGH_ERROR_CODE, allow_none=False)

        if valid_from > valid_through:
            raise ValidationError(u"The value of {0} [{1}] should be greater or equals than the value of {2} [{3}]."
                                  .format(Paquete.VALID_THROUGH_NAME, valid_through.strftime(DEFAULT_DATE_FORMAT),
                                          Paquete.VALID_FROM_NAME, valid_from.strftime(DEFAULT_DATE_FORMAT)),
                                  internal_code=PACKAGE_INVALID_RANGE_OF_DATES_FROM_GREATER_THAN_THROUGH_ERROR_CODE)

        id_location = validate_id_location(original_id_location)

        return on_namespace_callback(id_current_client, name, price, tax_rate, description, restricted_consumption,
                                     valid_from, valid_through, id_location, external_code, available_for_sale)

    return on_client_namespace(id_client, _get_and_validate_package_json_params_on_namespace,
                               action=action,
                               view=PACKAGES_VIEW_NAME,
                               id_location=original_id_location)


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
