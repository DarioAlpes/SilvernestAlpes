# -*- coding: utf-8 -*
from CJM.entidades.paquetes.ConsumoCantidad import ConsumoCantidad
from CJM.entidades.paquetes.Paquete import Paquete
from CJM.services.paquetes.paqueteView import get_active_package_for_edit, check_permissions_by_package_location
from CJM.services.validations import validate_amount, validate_id_sku, validate_id_sku_category, \
    AMOUNT_CONSUMPTION_SKU_AND_CATEGORY_ARE_EXCLUSIVE_ERROR_CODE, \
    AMOUNT_CONSUMPTION_SKU_AND_CATEGORY_NOT_SENT_ERROR_CODE, AMOUNT_CONSUMPTION_INVALID_AMOUNT_INCLUDED_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client
from flask import request, Blueprint

PACKAGE_AMOUNT_CONSUMPTIONS_VIEW_NAME = "package-amount-consumptions"
app = Blueprint(PACKAGE_AMOUNT_CONSUMPTIONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/amount-consumptions/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_package_amount_consumption(id_client, id_package):
    """
    Crea un consumo por cantidad y lo asocia al paquete con id dado en el namespace del cliente id_client
        Parametros esperados:
            Solo uno de los siguientes:
                id-sku: int id del sku incluido
                id-sku-category: int id de la categoria de skus incluidos
            amount-included: int con la cantidad incluidoa de items
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Consumo por cantidad creado
    """
    def create_package_amount_consumption_on_namespace(id_current_client, package, id_sku, id_sku_category,
                                                       amount_included):
        return ConsumoCantidad.create(id_current_client, package, id_sku, id_sku_category, amount_included)

    return _get_and_validate_amount_consumption_json_params(id_client,
                                                            id_package,
                                                            create_package_amount_consumption_on_namespace,
                                                            Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/amount-consumptions/', methods=['GET'],
           strict_slashes=False)
@with_json_bodyless
def list_amount_consumptions(id_client, id_package):
    """
    Lista los consumos por cantidad del paquete con id dado del cliente correspondiente.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Lista de consumos por cantidad del paquete
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_amount_consumptions_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)
        return ConsumoCantidad.list_amount_consumptions_by_package(package)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION,
                                          PACKAGE_AMOUNT_CONSUMPTIONS_VIEW_NAME)
    return on_client_namespace(id_client, list_amount_consumptions_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/amount-consumptions/<int:id_consumption>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_amount_consumption(id_client, id_package, id_consumption):
    """
    Da el consumo por cantidad con id dado.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_consumption: id del consumo asociado
    :return: Consumo por cantidad con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_amount_consumption_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)

        return ConsumoCantidad.get_by_package(package, id_consumption)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION,
                                          PACKAGE_AMOUNT_CONSUMPTIONS_VIEW_NAME)
    return on_client_namespace(id_client, get_amount_consumption_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/amount-consumptions/<int:id_consumption>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def edit_package_amount_consumption(id_client, id_package, id_consumption):
    """
    Actualiza el consumo por cantidadcon id dado
        Parametros esperados:
            Solo uno de los siguientes:
                id-sku: int id del sku incluido
                id-sku-category: int id de la categoria de skus incluidos
            amount-included: int con la cantidad incluidoa de items
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_consumption: id del consumo asociado
    :return: Consumo por cantidad editado
    """

    # noinspection PyUnusedLocal
    def edit_package_amount_consumption_on_namespace(id_current_client, package, id_sku, id_sku_category,
                                                     amount_included):
        return ConsumoCantidad.update(id_consumption, package, id_sku, id_sku_category, amount_included)

    return _get_and_validate_amount_consumption_json_params(id_client,
                                                            id_package,
                                                            edit_package_amount_consumption_on_namespace,
                                                            Role.UPDATE_ACTION)


def _get_and_validate_amount_consumption_json_params(id_client, id_package, on_namespace_callback, action):
    id_client = validate_id_client(id_client)

    def _get_and_validate_amount_consumption_json_params_on_namespace(id_current_client):
        amount_included = request.json.get(ConsumoCantidad.AMOUNT_INCLUDED_NAME)
        amount_included = validate_amount(amount_included, ConsumoCantidad.AMOUNT_INCLUDED_NAME,
                                          internal_code=AMOUNT_CONSUMPTION_INVALID_AMOUNT_INCLUDED_ERROR_CODE)

        id_sku = request.json.get(ConsumoCantidad.SKU_ID_NAME)
        if id_sku is not None:
            id_sku = validate_id_sku(id_sku, ConsumoCantidad.SKU_ID_NAME)

        id_sku_category = request.json.get(ConsumoCantidad.SKU_CATEGORY_ID_NAME)
        if id_sku_category is not None:
            id_sku_category = validate_id_sku_category(id_sku_category, ConsumoCantidad.SKU_CATEGORY_ID_NAME)

        if id_sku is not None and id_sku_category is not None:
            raise ValidationError(u"The fields {0} and {1} are exclusive."
                                  .format(ConsumoCantidad.SKU_ID_NAME,
                                          ConsumoCantidad.SKU_CATEGORY_ID_NAME),
                                  internal_code=AMOUNT_CONSUMPTION_SKU_AND_CATEGORY_ARE_EXCLUSIVE_ERROR_CODE)

        if id_sku is None and id_sku_category is None:
            raise ValidationError(u"Expected one of the  following fields: [{0}, {1}]."
                                  .format(ConsumoCantidad.SKU_ID_NAME,
                                          ConsumoCantidad.SKU_CATEGORY_ID_NAME),
                                  internal_code=AMOUNT_CONSUMPTION_SKU_AND_CATEGORY_NOT_SENT_ERROR_CODE)

        package = get_active_package_for_edit(id_package)

        return on_namespace_callback(id_current_client, package, id_sku, id_sku_category, amount_included)

    check_permissions_by_package_location(id_client, id_package, action, PACKAGE_AMOUNT_CONSUMPTIONS_VIEW_NAME)
    return on_client_namespace(id_client, _get_and_validate_amount_consumption_json_params_on_namespace, secured=False)
