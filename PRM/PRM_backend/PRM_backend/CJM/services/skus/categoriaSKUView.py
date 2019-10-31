# -*- coding: utf-8 -*
from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from CJM.services.validations import validate_id_sku_category, SKU_CATEGORY_INVALID_NAME_ERROR_CODE
from commons.entidades.users import Role
from commons.utils import with_json_bodyless, with_json_body, on_client_namespace
from commons.validations import validate_id_client, validate_string_not_empty
from flask import request, Blueprint
SKU_CATEGORIES_VIEW_NAME = "sku-categories"

app = Blueprint(SKU_CATEGORIES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/sku-categories/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_sku_category(id_client):
    """
    Crea una categoría de sku en el namespace del cliente id_client
        Parametros esperados:
            name: str
            id-parent-category: int opcional
    :param id_client: id del cliente asociado
    :return: Categoría creada
    """

    # noinspection PyUnusedLocal
    def create_sku_category_namespace(id_current_client, name, id_parent):
        return CategoriaSKU.create(name, id_parent)

    return _get_and_validate_sku_category_json_params(id_client, create_sku_category_namespace,
                                                      action=Role.CREATE_ACTION,
                                                      secured=True)


@app.route('/clients/<int:id_client>/sku-categories/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_sku_categories(id_client):
    """
    Da la lista de categorías de skus del cliente con id dado.
    :param id_client: Id del cliente asociado.
    :return: Lista de categorías de skus del cliente dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_sku_categories_on_namespace(id_current_client):
        return CategoriaSKU.list()

    return on_client_namespace(id_client, list_sku_categories_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKU_CATEGORIES_VIEW_NAME)


@app.route('/clients/<int:id_client>/sku-categories/<int:id_sku_category>/children/', methods=['GET'],
           strict_slashes=False)
@with_json_bodyless
def list_sku_categories_by_parent_id(id_client, id_sku_category):
    """
    Da la lista de categorias de sku hijo de la ubicación con id dado para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_sku_category: id de la categoria asociada.
    :return: Lista de categorias de sku hijo de la ubicación con id dado para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_sku_categories_by_parent_id_on_namespace(id_current_client):
        id_parent_category = validate_id_sku_category(id_sku_category)
        return CategoriaSKU.list_by_parent_id(id_parent_category)

    return on_client_namespace(id_client, list_sku_categories_by_parent_id_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKU_CATEGORIES_VIEW_NAME)


@app.route('/clients/<int:id_client>/sku-categories/<int:id_sku_category>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_sku_category(id_client, id_sku_category):
    """
    Da la categoría de SKU con id dado para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_sku_category: id de la categoría de sku buscada.
    :return: categoría de sku con id dado para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_sku_category_on_namespace(id_current_client):
        id_current_category = validate_id_sku_category(id_sku_category)
        return CategoriaSKU.get_by_id(id_current_category)

    return on_client_namespace(id_client, get_sku_category_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKU_CATEGORIES_VIEW_NAME)


@app.route('/clients/<int:id_client>/sku-categories/<int:id_sku_category>/', methods=['PUT'], strict_slashes=False)
@with_json_body
def update_sku_category(id_client, id_sku_category):
    """
    Actualzia la categoría de sku con id dado en el namespace del cliente id_client
        Parametros esperados:
            name: str
            id-parent-category: int opcional
    :param id_client: id del cliente asociado
    :param id_sku_category: id de la categoría de sku a actualizar
    :return: Categoría creada
    """

    # noinspection PyUnusedLocal
    def update_sku_category_namespace(id_current_client, name, id_parent):
        id_current_category = validate_id_sku_category(id_sku_category)
        return CategoriaSKU.update(id_current_category, name, id_parent)

    return _get_and_validate_sku_category_json_params(id_client, update_sku_category_namespace,
                                                      action=Role.UPDATE_ACTION,
                                                      secured=True)


def _get_and_validate_sku_category_json_params(id_client, on_namespace_callback, action, secured=True):

    id_client = validate_id_client(id_client)

    def _get_and_validate_sku_category_json_paramson_namespace(id_current_client):
        name = request.json.get(CategoriaSKU.SKU_CATEGORY_NAME_NAME)
        name = validate_string_not_empty(name, CategoriaSKU.SKU_CATEGORY_NAME_NAME,
                                         internal_code=SKU_CATEGORY_INVALID_NAME_ERROR_CODE)
        id_parent = request.json.get(CategoriaSKU.PARENT_NAME)
        if id_parent is not None:
            id_parent = validate_id_sku_category(id_parent)

        return on_namespace_callback(id_current_client, name, id_parent)

    if secured:
        return on_client_namespace(id_client, _get_and_validate_sku_category_json_paramson_namespace,
                                   action=action,
                                   view=SKU_CATEGORIES_VIEW_NAME)
    else:
        return on_client_namespace(id_client, _get_and_validate_sku_category_json_paramson_namespace,
                                   secured=False)
