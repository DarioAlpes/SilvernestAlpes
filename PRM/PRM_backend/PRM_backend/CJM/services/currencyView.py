# -*- coding: utf-8 -*
from CJM.entidades.Moneda import Moneda
from CJM.services.validations import CURRENCY_DOES_NOT_EXISTS_ERROR_CODE, CURRENCY_INVALID_NAME_ERROR_CODE, \
    CURRENCY_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists, EntityAlreadyExists
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_string_not_empty
from flask import request, Blueprint

CURRENCIES_VIEW_NAME = "currency"
app = Blueprint(CURRENCIES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/currencies/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_currency(id_client):
    """
    Crea una moneda en el namespace del cliente id_client
        Parametros esperados:
            name: str
    :param id_client: id del cliente asociado
    :return: Moneda creada
    """
    id_client = validate_id_client(id_client)

    def create_currency_on_namespace(id_current_client):
        name = request.json.get(Moneda.MONEY_NAME_NAME)
        name = validate_string_not_empty(name, Moneda.MONEY_NAME_NAME, internal_code=CURRENCY_INVALID_NAME_ERROR_CODE)

        if Moneda.get_by_id(name) is not None:
            raise EntityAlreadyExists(u"Currency[{0}]".format(name),
                                      internal_code=CURRENCY_ALREADY_EXISTS_ERROR_CODE)
        return Moneda.create(id_current_client, name)

    return on_client_namespace(id_client, create_currency_on_namespace,
                               action=Role.CREATE_ACTION,
                               view=CURRENCIES_VIEW_NAME)


@app.route('/clients/<int:id_client>/currencies/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_currencies(id_client):
    """
    Da la lista de monedas del cliente con id dado
    :param id_client: id del cliente a consultar
    :return: Lista de monedas del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_currencies_on_namespace(id_current_client):
        return Moneda.list()

    return on_client_namespace(id_client, list_currencies_on_namespace,
                               action=Role.READ_ACTION,
                               view=CURRENCIES_VIEW_NAME)


@app.route('/clients/<int:id_client>/currencies/<int:id_currency>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_currency_by_id(id_client, id_currency):
    """
    Da la moneda con id dado del cliente con id dado
    :param id_client: id del cliente a consultar
    :param id_currency: id de la moneda a consultar
    :return: Moneda con id dado del cliente con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_currency_by_id_on_namespace(id_current_client):
        currency = Moneda.get_by_internal_id(id_currency)
        if currency is None:
            raise EntityDoesNotExists(u"Currency[{0}]".format(id_currency),
                                      internal_code=CURRENCY_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return currency

    return on_client_namespace(id_client, get_currency_by_id_on_namespace,
                               action=Role.READ_ACTION,
                               view=CURRENCIES_VIEW_NAME)
