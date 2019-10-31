# -*- coding: utf-8 -*
from CJM.entidades.Moneda import Moneda
from CJM.entidades.paquetes.ConsumoDinero import ConsumoDinero
from CJM.entidades.paquetes.Paquete import Paquete
from CJM.services.paquetes.paqueteView import get_active_package_for_edit, check_permissions_by_package_location
from CJM.services.validations import validate_money, validate_currency_name, \
    MONEY_CONSUMPTION_INVALID_MONEY_INCLUDED_ERROR_CODE
from commons.entidades.users import Role
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client
from flask import request, Blueprint

PACKAGE_MONEY_CONSUMPTIONS_VIEW_NAME = "package-money-consumptions"
app = Blueprint(PACKAGE_MONEY_CONSUMPTIONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/money-consumptions/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_package_money_consumption(id_client, id_package):
    """
    Crea un consumo de dinero y lo asocia al paquete con id dado en el namespace del cliente id_client
        Parametros esperados:
            money-included: float
            currency: str opcional, si se omite se supone COP. Representa el tipo de moneda incluida en el paquete
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Consumo de dinero creado
    """

    def create_package_money_consumption_on_namespace(id_current_client, package, money_included, currency):
        return ConsumoDinero.create(id_current_client, package, money_included, currency)

    return _get_and_validate_money_consumption_json_params(id_client,
                                                           id_package,
                                                           create_package_money_consumption_on_namespace,
                                                           Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/money-consumptions/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_money_consumptions(id_client, id_package):
    """
    Lista los consumos por dinero del paquete con id dado del cliente correspondiente.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Lista de consumos por dinero del paquete
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_money_consumptions_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)
        return ConsumoDinero.list_money_consumptions_by_package(package)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION,
                                          PACKAGE_MONEY_CONSUMPTIONS_VIEW_NAME)
    return on_client_namespace(id_client, list_money_consumptions_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/money-consumptions/<int:id_consumption>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_money_consumption(id_client, id_package, id_consumption):
    """
    Da el consumo por dinero con id dado.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_consumption: id del consumo asociado
    :return: Consumo por dinero con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_money_consumption_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)

        return ConsumoDinero.get_by_package(package, id_consumption)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION,
                                          PACKAGE_MONEY_CONSUMPTIONS_VIEW_NAME)
    return on_client_namespace(id_client, get_money_consumption_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/money-consumptions/<int:id_consumption>/',
           methods=['PUT'], strict_slashes=False)
@with_json_body
def update_package_money_consumption(id_client, id_package, id_consumption):
    """
    Actualiza el consumo por dinero con id dado del paquete con id dado
        Parametros esperados:
            money-included: float
            currency: str opcional, si se omite se supone COP. Representa el tipo de moneda incluida en el paquete
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_consumption: id del consumo asociado
    :return: Consumo de dinero actualizado
    """

    # noinspection PyUnusedLocal
    def update_package_money_consumption_on_namespace(id_current_client, package, money_included, currency):
        return ConsumoDinero.update(id_consumption, package, money_included, currency)

    return _get_and_validate_money_consumption_json_params(id_client,
                                                           id_package,
                                                           update_package_money_consumption_on_namespace,
                                                           Role.CREATE_ACTION)


def _get_and_validate_money_consumption_json_params(id_client, id_package, on_namespace_callback, action):
    id_client = validate_id_client(id_client)

    def _get_and_validate_money_consumption_json_params_on_namespace(id_current_client):
        money_included = request.json.get(ConsumoDinero.MONEY_INCLUDED_NAME)
        money_included = validate_money(money_included, ConsumoDinero.MONEY_INCLUDED_NAME,
                                        internal_code=MONEY_CONSUMPTION_INVALID_MONEY_INCLUDED_ERROR_CODE)

        currency = request.json.get(ConsumoDinero.CURRENCY_NAME)
        if currency is None:
            currency = Moneda.DEFAULT_CURRENCY_NAME
        else:
            currency = validate_currency_name(currency, ConsumoDinero.CURRENCY_NAME)

        package = get_active_package_for_edit(id_package)

        return on_namespace_callback(id_current_client, package, money_included, currency)

    check_permissions_by_package_location(id_client, id_package, action, PACKAGE_MONEY_CONSUMPTIONS_VIEW_NAME)
    return on_client_namespace(id_client, _get_and_validate_money_consumption_json_params_on_namespace, secured=False)
