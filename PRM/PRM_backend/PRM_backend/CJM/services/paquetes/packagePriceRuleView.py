# -*- coding: utf-8 -*
from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.paquetes.ReglaPrecioPaquete import ReglaPrecioPaquete
from CJM.entidades.persons.Persona import Persona
from CJM.services.paquetes.paqueteView import get_active_package_for_edit, check_permissions_by_package_location
from CJM.services.validations import validate_money, validate_percentage, validate_price_rule_property_name, \
    AGE_GROUP_PROPERTY_NAME, validate_age_group, CATEGORY_PROPERTY_NAME, validate_person_category, \
    PACKAGE_PRICE_RULE_INVALID_BASE_PRICE_ERROR_CODE, PACKAGE_PRICE_RULE_INVALID_TAX_RATE_ERROR_CODE,\
    PACKAGE_PRICE_RULE_INVALID_RULES_ERROR_CODE, PACKAGE_PRICE_RULE_INVALID_PROPERTY_ERROR_CODE, \
    PACKAGE_PRICE_RULE_INVALID_VALUE_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_list_exists
from flask import request, Blueprint

PACKAGE_PRICE_RULES_VIEW_NAME = "package-price-rules"
app = Blueprint(PACKAGE_PRICE_RULES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/price-rules/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_price_rule(id_client, id_package):
    """
    Crea una regla de precio del paquete y lo asocia al paquete con id dado en el namespace del cliente id_client
        Parametros esperados:
            base-price: float
            tax-rate: float
            rules: arreglo. Cada item debe tener:
                property: str 'age-group' o 'category'
                value: str Un valor válido para la propiedad 'property' (INFANT, KID, ADULT para 'age-group') (A, B, C, D, o null para 'category')
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Regla creada
    """

    # noinspection PyUnusedLocal
    def create_price_rule_on_namespace(id_current_client, package, price, tax_rate, rules):
        return ReglaPrecioPaquete.create(package, tax_rate, price, rules)

    return _get_and_validate_price_rule_json_params(id_client,
                                                    id_package,
                                                    create_price_rule_on_namespace,
                                                    Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/price-rules/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_price_rules(id_client, id_package):
    """
    Lista las reglas de precios del paquete con id dado del cliente correspondiente.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :return: Lista de reglas del paquete
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_price_rules_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)
        return ReglaPrecioPaquete.list_by_package(package)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION, PACKAGE_PRICE_RULES_VIEW_NAME)
    return on_client_namespace(id_client, list_price_rules_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/price-rules/<int:id_rule>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_rule(id_client, id_package, id_rule):
    """
    Da la regla con id dado.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_rule: id de la regla asociado
    :return: Regla con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_rule_on_namespace(id_current_client):
        package = Paquete.get_active_package_by_id(id_package)

        return ReglaPrecioPaquete.get_by_package(package, id_rule)

    check_permissions_by_package_location(id_client, id_package, Role.READ_ACTION, PACKAGE_PRICE_RULES_VIEW_NAME)
    return on_client_namespace(id_client, get_rule_on_namespace, secured=False)


@app.route('/clients/<int:id_client>/packages/<int:id_package>/price-rules/<int:id_rule>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_rule(id_client, id_package, id_rule):
    """
    Elimina la regla con id dado.
    :param id_client: id del cliente asociado
    :param id_package: id del paquete asociado
    :param id_rule: id de la regla asociado
    :return: Regla eliminada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_rule_on_namespace(id_current_client):
        package = get_active_package_for_edit(id_package)

        rule = ReglaPrecioPaquete.get_by_package(package, id_rule)
        rule.delete()
        return rule

    check_permissions_by_package_location(id_client, id_package, Role.DELETE_ACTION, PACKAGE_PRICE_RULES_VIEW_NAME)
    return on_client_namespace(id_client, delete_rule_on_namespace, secured=False)


def _get_and_validate_price_rule_json_params(id_client, id_package, on_namespace_callback, action):
    id_client = validate_id_client(id_client)

    def _get_and_validate_price_rule_json_params_on_namespace(id_current_client):
        price = request.json.get(ReglaPrecioPaquete.BASE_PRICE_NAME)
        price = validate_money(price, ReglaPrecioPaquete.BASE_PRICE_NAME, allow_zero=True,
                               internal_code=PACKAGE_PRICE_RULE_INVALID_BASE_PRICE_ERROR_CODE)

        tax_rate = request.json.get(ReglaPrecioPaquete.TAX_RATE_NAME)
        tax_rate = validate_percentage(tax_rate, ReglaPrecioPaquete.TAX_RATE_NAME, allow_zero=True,
                                       internal_code=PACKAGE_PRICE_RULE_INVALID_TAX_RATE_ERROR_CODE)

        rules = request.json.get(ReglaPrecioPaquete.RULES_NAME)
        rules = validate_list_exists(rules, ReglaPrecioPaquete.RULES_NAME, allow_empty_list=False,
                                     internal_code=PACKAGE_PRICE_RULE_INVALID_RULES_ERROR_CODE)

        for rule in rules:
            rule[ReglaPrecioPaquete.PROPERTY_NAME] = validate_price_rule_property_name(rule[ReglaPrecioPaquete.PROPERTY_NAME],
                                                                                       ReglaPrecioPaquete.PROPERTY_NAME,
                                                                                       internal_code=PACKAGE_PRICE_RULE_INVALID_PROPERTY_ERROR_CODE)
            if rule[ReglaPrecioPaquete.PROPERTY_NAME] == AGE_GROUP_PROPERTY_NAME:
                rule[ReglaPrecioPaquete.VALUE_NAME] = validate_age_group(rule[ReglaPrecioPaquete.VALUE_NAME],
                                                                         ReglaPrecioPaquete.VALUE_NAME,
                                                                         internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_ERROR_CODE)
            elif rule[ReglaPrecioPaquete.PROPERTY_NAME] == CATEGORY_PROPERTY_NAME:
                if rule[ReglaPrecioPaquete.VALUE_NAME] is not None:
                    rule[ReglaPrecioPaquete.VALUE_NAME] = validate_person_category(rule[ReglaPrecioPaquete.VALUE_NAME],
                                                                                   ReglaPrecioPaquete.VALUE_NAME,
                                                                                   internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_ERROR_CODE)
            else:
                # No debe pasar, validate_price_rule_property_name debe fallar antes si iba a caer acá
                raise ValidationError(u"Invalid {0} value ({1})".format(ReglaPrecioPaquete.PROPERTY_NAME,
                                                                        rule[ReglaPrecioPaquete.PROPERTY_NAME]),
                                      internal_code=PACKAGE_PRICE_RULE_INVALID_PROPERTY_ERROR_CODE)

        package = get_active_package_for_edit(id_package)

        ReglaPrecioPaquete.check_if_already_exists_overlapping_rule(package, rules)

        return on_namespace_callback(id_current_client, package, price, tax_rate, rules)

    check_permissions_by_package_location(id_client, id_package, action, PACKAGE_PRICE_RULES_VIEW_NAME)
    return on_client_namespace(id_client, _get_and_validate_price_rule_json_params_on_namespace, secured=False)


def get_base_price_and_tax_rate_from_package(id_person, package):
    person = Persona.get_by_id(id_person)
    if person is not None:
        category = person.categoria
        age_group = person.get_age_group()
        rule = ReglaPrecioPaquete.get_by_package_and_fields_values(package, category, age_group)
        if rule is not None:
            return rule.get_base_price_and_tax_rate()
    return package.get_base_price_and_tax_rate()
