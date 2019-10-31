# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.skus.SKU import SKU
from CJM.services.validations import validate_id_sku, validate_money, validate_ean_code, validate_id_sku_category, \
    SKU_INVALID_NAME_ERROR_CODE, SKU_INVALID_MEASURE_ERROR_CODE, SKU_INVALID_COST_ERROR_CODE, \
    SKU_INVALID_EAN_CODE_ERROR_CODE, validate_percentage, SKU_INVALID_TAX_RATE_ERROR_CODE, \
    SKU_INVALID_EXTERNAL_CODE_ERROR_CODE
from commons.entidades.users import Role
from commons.utils import with_json_body, on_client_namespace, with_json_bodyless
from commons.validations import validate_id_client, validate_string_not_empty

SKUS_VIEW_NAME = "sku"
app = Blueprint(SKUS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/skus/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_sku(id_client):
    """
    Crea un sku en el namespace del cliente id_client
        Parametros esperados:
            name: str
            measure: str
            cost: float
            tax-rate: float
            id-category: int
            external-code: str
            ean-code:str opcional
    :param id_client: id del cliente asociado
    :return: sku creado
    """
    # noinspection PyUnusedLocal
    def create_sku_on_namespace(id_current_client, name, measure, cost, ean_code, id_sku_category, tax_rate,
                                external_code):
        return SKU.create(name, measure, cost, ean_code, id_sku_category, tax_rate, external_code)

    return _get_and_validate_sku_json_params(id_client, create_sku_on_namespace,
                                             action=Role.CREATE_ACTION,
                                             secured=True)


@app.route('/clients/<int:id_client>/skus/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_skus(id_client):
    """
    Da la lista de skus del cliente con id dado.
    :param id_client: Id del cliente asociado.
    :return: Lista de skus del cliente dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_skus_on_namespace(id_current_client):
        return SKU.list()

    return on_client_namespace(id_client, list_skus_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKUS_VIEW_NAME)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_sku(id_client, id_sku):
    """
    Da el sku con id dado para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_sku: id del sku buscado.
    :return: sku con id dado para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_sku_on_namespace(id_current_client):
        current_id_sku = validate_id_sku(id_sku)
        return SKU.get_by_id(current_id_sku)

    return on_client_namespace(id_client, get_sku_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKUS_VIEW_NAME)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/', methods=['PUT'], strict_slashes=False)
@with_json_body
def update_sku(id_client, id_sku):
    """
    Actualiza el sku con id dado en el namespace del cliente id_client
        Parametros esperados:
            name: str
            measure: str
            cost: float
            id-category: int
            external-code: str
            ean-code:str opcional
            tax-rate: float
    :param id_client: id del cliente asociado
    :param id_sku: id del sku a actualizar.
    :return: sku actualizado
    """
    # noinspection PyUnusedLocal
    def update_sku_on_namespace(id_current_client, name, measure, cost, ean_code, id_sku_category, tax_rate,
                                external_code):
        current_id_sku = validate_id_sku(id_sku)
        return SKU.update(current_id_sku, name, measure, cost, ean_code, id_sku_category, tax_rate, external_code)

    return _get_and_validate_sku_json_params(id_client, update_sku_on_namespace,
                                             action=Role.UPDATE_ACTION,
                                             secured=True)


def _get_and_validate_sku_json_params(id_client, on_namespace_callback, action, secured=True):
    def _get_and_validate_sku_json_params_on_namespace(id_current_client):
        name = request.json.get(SKU.SKU_NAME_NAME)
        name = validate_string_not_empty(name, SKU.SKU_NAME_NAME, internal_code=SKU_INVALID_NAME_ERROR_CODE)

        measure = request.json.get(SKU.UNIT_OF_MEASURE_NAME)
        measure = validate_string_not_empty(measure, SKU.UNIT_OF_MEASURE_NAME, internal_code=SKU_INVALID_MEASURE_ERROR_CODE)

        cost = request.json.get(SKU.COST_NAME)
        cost = validate_money(cost, SKU.COST_NAME, internal_code=SKU_INVALID_COST_ERROR_CODE)

        ean_code = request.json.get(SKU.EAN_CODE_NAME)
        if ean_code is not None:
            ean_code = validate_ean_code(ean_code, SKU.EAN_CODE_NAME, internal_code=SKU_INVALID_EAN_CODE_ERROR_CODE)

        id_sku_category = request.json.get(SKU.SKU_CATEGORY_ID_NAME)
        id_sku_category = validate_id_sku_category(id_sku_category, SKU.SKU_CATEGORY_ID_NAME)

        tax_rate = request.json.get(SKU.TAX_RATE_NAME)
        tax_rate = validate_percentage(tax_rate, SKU.TAX_RATE_NAME,
                                       internal_code=SKU_INVALID_TAX_RATE_ERROR_CODE)
        external_code = request.json.get(SKU.EXTERNAL_CODE_NAME)
        external_code = validate_string_not_empty(external_code, SKU.EXTERNAL_CODE_NAME,
                                                  internal_code=SKU_INVALID_EXTERNAL_CODE_ERROR_CODE)

        return on_namespace_callback(id_current_client, name, measure, cost, ean_code, id_sku_category, tax_rate,
                                     external_code)

    if secured:
        return on_client_namespace(id_client, _get_and_validate_sku_json_params_on_namespace,
                                   action=action,
                                   view=SKUS_VIEW_NAME)
    else:
        return on_client_namespace(id_client, _get_and_validate_sku_json_params_on_namespace,
                                   secured=False)
