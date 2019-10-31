# -*- coding: utf-8 -*
from CJM.entidades.skus.UbicacionVentaSKU import UbicacionVentaSKU
from CJM.services.validations import validate_id_sku, validate_money, SALE_LOCATION_DOES_NOT_EXISTS_ERROR_CODE, \
    SALE_LOCATION_INVALID_PRICE_ERROR_CODE, SALE_LOCATION_ALREADY_EXISTS_ERROR_CODE, \
    SKU_ON_SALE_DOES_NOT_EXISTS_ERROR_CODE, SKU_ON_SALE_INVALID_PRICE_ERROR_CODE, SKU_ON_SALE_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityAlreadyExists, EntityDoesNotExists
from commons.utils import with_json_bodyless, with_json_body, on_client_namespace
from commons.validations import validate_id_client, validate_id_location
from flask import request, Blueprint

SKUS_SALES_VIEW_NAME = "sku-sales-locations"
app = Blueprint(SKUS_SALES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/sales-locations/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_sku_location_sale_by_sku(id_client, id_sku):
    """
    Crea una ubicación de venta del sku dado en la ubicación dada en el namespace del cliente id_client
        Parametros esperados:
            id-location: int
            price: float
    :param id_client: id del cliente asociado
    :param id_sku: id del sku asociado
    :return: Ubicación de venta creada
    """
    id_location = request.json.get(UbicacionVentaSKU.LOCATION_ID_NAME)

    return _create_sku_location(id_client, id_sku, id_location, u"Sale location[{0}]".format(id_location),
                                invalid_price_internal_code=SALE_LOCATION_INVALID_PRICE_ERROR_CODE,
                                entity_already_exists_internal_code=SALE_LOCATION_ALREADY_EXISTS_ERROR_CODE)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/skus-on-sale/', methods=['POST'],
           strict_slashes=False)
@with_json_body
def create_sku_location_sale_by_location(id_client, id_location):
    """
    Crea una ubicación de venta del sku dado en la ubicación dada en el namespace del cliente id_client
        Parametros esperados:
            id-sku: int
            price: float
    :param id_client: id del cliente asociado
    :param id_location: id del sku asociado
    :return: Ubicación de venta creada
    """
    id_sku = request.json.get(UbicacionVentaSKU.SKU_ID_NAME)

    return _create_sku_location(id_client, id_sku, id_location, u"Sku On Sale[{0}]".format(id_sku),
                                invalid_price_internal_code=SKU_ON_SALE_INVALID_PRICE_ERROR_CODE,
                                entity_already_exists_internal_code=SKU_ON_SALE_ALREADY_EXISTS_ERROR_CODE)


def _create_sku_location(id_client, id_sku, id_location, entity_name, invalid_price_internal_code=None,
                         entity_already_exists_internal_code=None):
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def _create_sku_location_on_namespace(id_current_client):
        price = request.json.get(UbicacionVentaSKU.PRICE_NAME)
        price = validate_money(price, UbicacionVentaSKU.PRICE_NAME, internal_code=invalid_price_internal_code)

        id_current_sku = validate_id_sku(id_sku)
        id_current_location = validate_id_location(id_location)
        if UbicacionVentaSKU.get_by_sku_and_location(id_current_sku, id_current_location) is not None:
            raise EntityAlreadyExists(entity_name, internal_code=entity_already_exists_internal_code)
        return UbicacionVentaSKU.create(id_current_sku, id_current_location, price)

    return on_client_namespace(id_client, _create_sku_location_on_namespace,
                               action=Role.CREATE_ACTION,
                               view=SKUS_SALES_VIEW_NAME,
                               id_location=id_location)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/sales-locations/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_sku_sales_locations(id_client, id_sku):
    """
    Da la lista de ubicaciones donde el sku esta a la venta.
    :param id_client: Id del cliente asociado.
    :param id_sku: id del sku asociado
    :return: lista de ubicaciones donde el sku esta a la venta.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_sku_sales_locations_on_namespace(id_current_client):
        id_current_sku = validate_id_sku(id_sku)
        return UbicacionVentaSKU.get_locations_by_sku(id_current_sku)

    return on_client_namespace(id_client, list_sku_sales_locations_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKUS_SALES_VIEW_NAME)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/skus-on-sale/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_skus_on_sale_by_location(id_client, id_location):
    """
    Da la lista de skus a la venta en la ubicación dada.
    :param id_client: Id del cliente asociado.
    :param id_location: id del sku asociado
    :return: lista de skus a la venta en la ubicación dada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_skus_on_sale_by_location_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        return UbicacionVentaSKU.get_skus_by_location(id_current_location)

    return on_client_namespace(id_client, list_skus_on_sale_by_location_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKUS_SALES_VIEW_NAME,
                               id_location=id_location)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/skus-on-sale/<int:id_sku>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_sku_on_sale_by_location(id_client, id_location, id_sku):
    """
    Elimina el sku con id dadod de la ubicación con id dado.
    :param id_client: Id del cliente asociado.
    :param id_location: id del sku asociado
    :param id_sku: id del sku a eliminar
    :return: sku en venta eliminado
    """
    return _delete_sku_location(id_client, id_location, id_sku, u"Sku On Sale[{0}]".format(id_sku),
                                internal_code=SKU_ON_SALE_DOES_NOT_EXISTS_ERROR_CODE)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/sales-locations/<int:id_location>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_sales_location_by_sku(id_client, id_sku, id_location):
    """
    Elimina la ubicación como punto de venta con id dado del sku con id dado.
    :param id_client: Id del cliente asociado.
    :param id_sku: id del sku a eliminar
    :param id_location: id del sku asociado
    :return: punto de venta eliminado
    """
    return _delete_sku_location(id_client, id_location, id_sku, u"Sale location[{0}]".format(id_location),
                                internal_code=SALE_LOCATION_DOES_NOT_EXISTS_ERROR_CODE)


def _delete_sku_location(id_client, id_location, id_sku, entity_name, internal_code=None):
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def _delete_sku_location_on_namespace(id_current_client):
        id_current_sku = validate_id_sku(id_sku)
        id_current_location = validate_id_location(id_location)
        sku_on_sale = UbicacionVentaSKU.get_by_sku_and_location(id_current_sku, id_current_location)
        if sku_on_sale is None:
            raise EntityDoesNotExists(entity_name, internal_code=internal_code)
        sku_on_sale.key.delete()
        return sku_on_sale

    return on_client_namespace(id_client, _delete_sku_location_on_namespace,
                               action=Role.DELETE_ACTION,
                               view=SKUS_SALES_VIEW_NAME,
                               id_location=id_location)
