# -*- coding: utf-8 -*


from CJM.entidades.Moneda import Moneda
from CJM.entidades.beacons.ClientBeacon import ClientBeacon
from CJM.entidades.beacons.GlobalBeacon import GlobalBeacon
from CJM.entidades.eventos.Accion import Accion
from CJM.entidades.eventos.Compra import Compra
from CJM.entidades.eventos.Devolucion import Devolucion
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.Feedback import Feedback
from CJM.entidades.eventos.Pedido import Pedido
from CJM.entidades.eventos.Visita import Visita
from CJM.entidades.persons.Persona import Persona
from CJM.entidades.reservas.EventoSocial import EventoSocial
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from CJM.entidades.skus.SKU import SKU
from commons.excepciones.apiexceptions import ValidationError
from commons.validations import validate_string_not_empty, validate_model_id, raise_field_does_not_exists_error, \
    validate_list_exists, validate_model_id_list, validate_model_keys_list

GENDER_MALE = u"male"
GENDER_FEMALE = u"female"
VALID_GENDERS = {GENDER_MALE, GENDER_FEMALE}
GENDERS_ABBREVIATIONS = {gender[0]: gender for gender in VALID_GENDERS}

CC_DOCUMENT_TYPE = "CC"
NIT_DOCUMENT_TYPE = "NIT"
TI_DOCUMENT_TYPE = "TI"
CE_DOCUMENT_TYPE = "CE"
PASSPORT_DOCUMENT_TYPE = "PA"
REGISTRO_CIVIL_DOCUMENT_TYPE = "RC"
NUIP_DOCUMENT_TYPE = "NUIP"
CARNE_DIPLOMATICO_DOCUMENT_TYPE = "CD"
NO_DOCUMENT_DOCUMENT_TYPE = "NO_DOCUMENT"

VALID_DOCUMENTS = {CC_DOCUMENT_TYPE,
                   NIT_DOCUMENT_TYPE,
                   TI_DOCUMENT_TYPE,
                   CE_DOCUMENT_TYPE,
                   PASSPORT_DOCUMENT_TYPE,
                   REGISTRO_CIVIL_DOCUMENT_TYPE,
                   NUIP_DOCUMENT_TYPE,
                   CARNE_DIPLOMATICO_DOCUMENT_TYPE,
                   NO_DOCUMENT_DOCUMENT_TYPE}
VALID_PERSON_TYPES = {"ADULTO", "NIÑO", "BEBE", "ADULTO MAYOR"}

AGE_GROUP_PROPERTY_NAME = "age-group"
CATEGORY_PROPERTY_NAME = "category"
VALID_PRICE_PROPERTIES = {AGE_GROUP_PROPERTY_NAME, CATEGORY_PROPERTY_NAME}

INFANT_AGE_GROUP = "INFANT"
KID_AGE_GROUP = "KID"
ADULT_AGE_GROUP = "ADULT"
VALID_AGE_GROUPS = {INFANT_AGE_GROUP, KID_AGE_GROUP, ADULT_AGE_GROUP}

PARENT_RELATIONSHIP = u"PARENT"
CHILD_RELATIONSHIP = u"CHILD"
SPOUSE_RELATIONSHIP = u"SPOUSE"
SIBLING_RELATIONSHIP = u"SIBLING"
UNKNOWN_RELATIONSHIP = u"UNKNOWN"
RELATIONSHIPS_SYMMETRY = {PARENT_RELATIONSHIP: CHILD_RELATIONSHIP,
                          CHILD_RELATIONSHIP: PARENT_RELATIONSHIP,
                          SPOUSE_RELATIONSHIP: SPOUSE_RELATIONSHIP,
                          SIBLING_RELATIONSHIP: SIBLING_RELATIONSHIP,
                          UNKNOWN_RELATIONSHIP: UNKNOWN_RELATIONSHIP}

COTIZANTE_AFFILIATION = "COTIZANTE"
BENEFICIARIO_AFFILIATION = "BENEFICIARIO"

VALID_AFFILIATIONS = {COTIZANTE_AFFILIATION, BENEFICIARIO_AFFILIATION}

A_CATEGORY = "A"
B_CATEGORY = "B"
C_CATEGORY = "C"
D_CATEGORY = "D"
VALID_CATEGORIES = {A_CATEGORY, B_CATEGORY, C_CATEGORY, D_CATEGORY}
VALID_EAN_FORMATS = {"EAN-13": 13, "UPC-A": 12}

CURRENCY_DOES_NOT_EXISTS_ERROR_CODE = 7
SKU_CATEGORY_DOES_NOT_EXISTS_ERROR_CODE = 8
SKU_DOES_NOT_EXISTS_ERROR_CODE = 9
SKU_IMAGE_DOES_NOT_EXISTS_ERROR_CODE = 10
SALE_LOCATION_DOES_NOT_EXISTS_ERROR_CODE = 11
SKU_ON_SALE_DOES_NOT_EXISTS_ERROR_CODE = 12
PERSON_DOES_NOT_EXISTS_ERROR_CODE = 13
PERSON_IMAGE_DOES_NOT_EXISTS_ERROR_CODE = 14
SOCIAL_EVENT_DOES_NOT_EXISTS_ERROR_CODE = 15
PACKAGE_DOES_NOT_EXISTS_ERROR_CODE = 16
ACCESS_DOES_NOT_EXISTS_ERROR_CODE = 17
AMOUNT_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE = 18
MONEY_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE = 19
RESERVATION_DOES_NOT_EXISTS_ERROR_CODE = 20
PERSON_RESERVATION_DOES_NOT_EXISTS_ERROR_CODE = 21
ACCESS_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE = 22
AMOUNT_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE = 23
MONEY_TOPOFF_DOES_NOT_EXISTS_ERROR_CODE = 24
PERSON_ACCESS_DOES_NOT_EXISTS_ERROR_CODE = 25
PERSON_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE = 26
SUPPORTED_TAG_DOES_NOT_EXISTS_ERROR_CODE = 27
CLIENT_TAG_DOES_NOT_EXISTS_ERROR_CODE = 28
ORDER_DOES_NOT_EXISTS_ERROR_CODE = 29
GLOBAL_SENSOR_DOES_NOT_EXISTS_ERROR_CODE = 31
CLIENT_SENSOR_DOES_NOT_EXISTS_ERROR_CODE = 32
PERSONS_RELATIONSHIP_DOES_NOT_EXISTS_ERROR_CODE = 33
PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_ERROR_CODE = 34


CURRENCY_INVALID_NAME_ERROR_CODE = 601
CURRENCY_ALREADY_EXISTS_ERROR_CODE = 602

SKU_CATEGORY_INVALID_NAME_ERROR_CODE = 701
SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE = 702

SKU_INVALID_NAME_ERROR_CODE = 801
SKU_INVALID_MEASURE_ERROR_CODE = 802
SKU_INVALID_COST_ERROR_CODE = 803
SKU_INVALID_EAN_CODE_ERROR_CODE = 804
SKU_IMAGE_ALREADY_EXISTS_ERROR_CODE = 805
SKU_INVALID_TAX_RATE_ERROR_CODE = 806
SKU_INVALID_EXTERNAL_CODE_ERROR_CODE = 807

SALE_LOCATION_INVALID_PRICE_ERROR_CODE = 901
SALE_LOCATION_ALREADY_EXISTS_ERROR_CODE = 902

SKU_ON_SALE_INVALID_PRICE_ERROR_CODE = 1001
SKU_ON_SALE_ALREADY_EXISTS_ERROR_CODE = 1002

PERSON_INVALID_CITY_ERROR_CODE = 1101
PERSON_INVALID_COMPANY_ERROR_CODE = 1102
PERSON_INVALID_PROFESSION_ERROR_CODE = 1103
PERSON_INVALID_NATIONALITY_ERROR_CODE = 1104
PERSON_INVALID_AFFILIATION_ERROR_CODE = 1105
PERSON_INVALID_CATEGORY_ERROR_CODE = 1106
PERSON_INVALID_MAIL_ERROR_CODE = 1108
PERSON_INVALID_FIRST_NAME_ERROR_CODE = 1109
PERSON_INVALID_LAST_NAME_ERROR_CODE = 1110
PERSON_INVALID_DOCUMENT_TYPE_ERROR_CODE = 1111
PERSON_INVALID_DOCUMENT_NUMBER_ERROR_CODE = 1112
PERSON_INVALID_GENDER_ERROR_CODE = 1113
PERSON_INVALID_BIRTHDATE_ERROR_CODE = 1114
PERSON_IMAGE_ALREADY_EXISTS_ERROR_CODE = 1115
PERSON_DUPLICATED_DOCUMENT_ERROR_CODE = 1116

SOCIAL_EVENT_INVALID_NAME_ERROR_CODE = 1202
SOCIAL_EVENT_INVALID_DESCRIPTION_ERROR_CODE = 1203
SOCIAL_EVENT_INVALID_INITIAL_DATE_ERROR_CODE = 1204
SOCIAL_EVENT_INVALID_FINAL_DATE_ERROR_CODE = 1205
SOCIAL_EVENT_INVALID_RANGE_OF_DATES_ERROR_CODE = 1206
SOCIAL_EVENT_INVALID_COMPANY_ERROR_CODE = 1207
SOCIAL_EVENT_INVALID_DOCUMENT_NUMBER_ERROR_CODE = 1208
SOCIAL_EVENT_INVALID_DOCUMENT_TYPE_ERROR_CODE = 1209

PACKAGE_INVALID_NAME_ERROR_CODE = 1301
PACKAGE_INVALID_PRICE_ERROR_CODE = 1302
PACKAGE_INVALID_DESCRIPTION_ERROR_CODE = 1303
PACKAGE_INVALID_RESTRICTED_CONSUMPTION_ERROR_CODE = 1304
PACKAGE_INVALID_VALID_FROM_ERROR_CODE = 1305
PACKAGE_INVALID_VALID_THROUGH_ERROR_CODE = 1306
PACKAGE_INVALID_DURATION_ERROR_CODE = 1307
PACKAGE_INVALID_RANGE_OF_DATES_FROM_GREATER_THAN_THROUGH_ERROR_CODE = 1308
PACKAGE_INVALID_TAX_RATE_ERROR_CODE = 1311
PACKAGE_INVALID_EXTERNAL_CODE_ERROR_CODE = 1312
PACKAGE_INVALID_AVAILABLE_FOR_SALE_ERROR_CODE = 1313

ACCESS_INVALID_AMOUNT_INCLUDED_ERROR_CODE = 1401

AMOUNT_CONSUMPTION_SKU_AND_CATEGORY_ARE_EXCLUSIVE_ERROR_CODE = 1501
AMOUNT_CONSUMPTION_SKU_AND_CATEGORY_NOT_SENT_ERROR_CODE = 1502
AMOUNT_CONSUMPTION_INVALID_AMOUNT_INCLUDED_ERROR_CODE = 1503

MONEY_CONSUMPTION_INVALID_MONEY_INCLUDED_ERROR_CODE = 1601

RESERVATION_INVALID_PAYMENT_ERROR_CODE = 1703
RESERVATION_INVALID_TRANSACTION_NUMBER_ERROR_CODE = 1710
RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_ERROR_CODE = 1711
UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE = 1714
UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE = 1715
UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE = 1716
UPDATE_RESERVATION_WITH_TOPOFFS_ERROR_CODE = 1717

DELETE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_ERROR_CODE = 1801
DELETE_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE = 1802
DELETE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE = 1803
DELETE_RESERVATION_WITH_TOPOFFS_ERROR_CODE = 1804

RESERVATION_INVALID_RESERVATION_NUMBER_ERROR_CODE = 1901

RESERVATION_INVALID_DOCUMENT_TYPE_ERROR_CODE = 2001
RESERVATION_INVALID_DOCUMENT_NUMBER_ERROR_CODE = 2002
RESERVATION_INVALID_BASE_TIME_ERROR_CODE = 2003
RESERVATION_INVALID_INITIAL_TIME_ERROR_CODE = 2004
RESERVATION_INVALID_FINAL_TIME_ERROR_CODE = 2005
RESERVATION_INVALID_INCLUDE_CHILDREN_ERROR_CODE = 2006

PERSON_RESERVATION_INVALID_ACTIVE_ERROR_CODE = 2106
PERSON_RESERVATION_TRYING_TO_ACTIVATE_UNPAID_RESERVATION_ERROR_CODE = 2107
UPDATE_ACTIVE_PERSON_RESERVATION_ERROR_CODE = 2109
UPDATE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE = 2110
UPDATE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE = 2111
UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_ERROR_CODE = 2112
PERSON_RESERVATION_INVALID_INITIAL_DATE_ERROR_CODE = 2113
PERSON_RESERVATION_INVALID_FINAL_DATE_ERROR_CODE = 2114
PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FINAL_ERROR_CODE = 2115
PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FROM_ERROR_CODE = 2116
PERSON_RESERVATION_RANGE_OF_DATES_THROUGH_GREATER_THAN_FINAL_ERROR_CODE = 2117
PERSON_RESERVATION_INVALID_RANGE_OF_DATES_INITIAL_SMALLER_THAN_EVENT_INITIAL_ERROR_CODE = 2118
PERSON_RESERVATION_INVALID_RANGE_OF_DATES_FINAL_BIGGER_THAN_EVENT_FINAL_ERROR_CODE = 2119
PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_ERROR_CODE = 2120
PERSON_RESERVATION_TRYING_TO_ACTIVATE_ALREADY_ACTIVE_PERSON_RESERVATION_ERROR_CODE = 2121

DELETE_ACTIVE_PERSON_RESERVATION_ERROR_CODE = 2201
DELETE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_ERROR_CODE = 2202
DELETE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_ERROR_CODE = 2203
DELETE_HOLDER_PERSON_RESERVATION_ERROR_CODE = 2204
DELETE_PERSON_RESERVATION_WITH_TOPOFFS_ERROR_CODE = 2205

PERSON_RESERVATION_INVALID_DOCUMENT_TYPE_ERROR_CODE = 2301
PERSON_RESERVATION_INVALID_DOCUMENT_NUMBER_ERROR_CODE = 2302
PERSON_RESERVATION_INVALID_BASE_TIME_ERROR_CODE = 2303

ACCESS_TOPOFF_INVALID_AMOUNT_ERROR_CODE = 2401
ACCESS_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE = 2402
ACCESS_TOPOFF_INACTIVE_PERSON_RESERVATION_ERROR_CODE = 2403
ACCESS_TOPOFF_INVALID_TOPOFFS_ERROR_CODE = 2404
ACCESS_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE = 2405

AMOUNT_TOPOFF_INVALID_AMOUNT_ERROR_CODE = 2501
AMOUNT_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE = 2502
AMOUNT_TOPOFF_INACTIVE_PERSON_RESERVATION_ERROR_CODE = 2503
AMOUNT_TOPOFF_SKU_AND_CATEGORY_ARE_EXCLUSIVE_ERROR_CODE = 2504
AMOUNT_TOPOFF_SKU_AND_CATEGORY_NOT_SENT_ERROR_CODE = 2505
AMOUNT_TOPOFF_INVALID_TOPOFFS_ERROR_CODE = 2506
AMOUNT_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE = 2507

MONEY_TOPOFF_INVALID_MONEY_ERROR_CODE = 2601
MONEY_TOPOFF_INVALID_TRANSACTION_NUMBER_ERROR_CODE = 2602
MONEY_TOPOFF_INACTIVE_PERSON_RESERVATION_ERROR_CODE = 2603
MONEY_TOPOFF_INVALID_TOPOFFS_ERROR_CODE = 2604
MONEY_TOPOFF_INVALID_TOPOFF_TIME_ERROR_CODE = 2605

SUPPORTED_TAGS_INVALID_NAME_ERROR_CODE = 2901
SUPPORTED_TAGS_INVALID_TOTAL_SIZE_ERROR_CODE = 2902
SUPPORTED_TAGS_ALREADY_EXISTS_ERROR_CODE = 2903

CLIENT_TAGS_INVALID_NAME_ERROR_CODE = 3001
CLIENT_TAGS_ALREADY_EXISTS_ERROR_CODE = 3002

ORDERS_INVALID_ORDER_TIME_ERROR_CODE = 3101
ORDERS_INVALID_MISSING_MONEY_ERROR_CODE = 3102
ORDERS_INVALID_MONEY_CONSUMPTIONS_ERROR_CODE = 3103
ORDERS_INVALID_ORDERS_ERROR_CODE = 3104
ORDERS_INVALID_MONEY_CONSUMED_ERROR_CODE = 3105
ORDERS_INVALID_AMOUNT_CONSUMED_ERROR_CODE = 3106
ORDERS_INVALID_MISSING_AMOUNT_ERROR_CODE = 3107
ORDERS_INVALID_AMOUNT_CONSUMPTIONS_ERROR_CODE = 3108
ORDERS_INVALID_EMPTY_CONSUMPTIONS_ERROR_CODE = 3109

PERSON_ACCESS_INVALID_ACCESS_TIME_ERROR_CODE = 3301
PERSON_ACCESS_INVALID_ACCESSES_ERROR_CODE = 3302

GLOBAL_SENSOR_INVALID_ID_ERROR_CODE = 3401
GLOBAL_SENSOR_INVALID_TYPE_ERROR_CODE = 3402
GLOBAL_SENSOR_ALREADY_EXISTS_ERROR_CODE = 3403

CLIENT_SENSOR_ALREADY_EXISTS_ERROR_CODE = 3501
CLIENT_SENSOR_ALREADY_ASSIGNED_ERROR_CODE = 3502
CLIENT_SENSOR_PERSON_ON_STATIC_ERROR_CODE = 3503
CLIENT_SENSOR_LOCATION_ON_MOBILE_ERROR_CODE = 3504
CLIENT_SENSOR_INVALID_ACTIVE_ERROR_CODE = 3505
CLIENT_SENSOR_INVALID_SYNCED_ERROR_CODE = 3506

TRANSACTIONS_PER_USER_INVALID_INITIAL_TIME_ERROR_CODE = 3601
TRANSACTIONS_PER_USER_INVALID_FINAL_TIME_ERROR_CODE = 3602

CONSUMPTIONS_PER_SKU_INVALID_INITIAL_TIME_ERROR_CODE = 3701
CONSUMPTIONS_PER_SKU_INVALID_FINAL_TIME_ERROR_CODE = 3702

PACKAGE_INVALID_BASE_TIME_ERROR_CODE = 3801

PERSONS_RELATIONSHIPS_INVALID_RELATIONSHIPS_ERROR_CODE = 3901
PERSONS_RELATIONSHIP_ALREADY_EXIST_ERROR_CODE = 3902

PACKAGE_PRICE_RULE_INVALID_BASE_PRICE_ERROR_CODE = 4001
PACKAGE_PRICE_RULE_INVALID_TAX_RATE_ERROR_CODE = 4002
PACKAGE_PRICE_RULE_INVALID_RULES_ERROR_CODE = 4003
PACKAGE_PRICE_RULE_INVALID_PROPERTY_ERROR_CODE = 4004
PACKAGE_PRICE_RULE_INVALID_VALUE_ERROR_CODE = 4005
PACKAGE_PRICE_RULE_INVALID_DUPLICATED_RULE_ERROR_CODE = 4006

ENTITIES_PER_USER_INVALID_KIND_ERROR_CODE = 4101
ENTITIES_PER_USER_INVALID_INITIAL_TIME_ERROR_CODE = 4102
ENTITIES_PER_USER_INVALID_FINAL_TIME_ERROR_CODE = 4103
ENTITIES_PER_USER_INVALID_INCLUDE_DELETED_ERROR_CODE = 4104

PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_INITIAL_TIME_ERROR_CODE = 4201
PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_FINAL_TIME_ERROR_CODE = 4202

ERROR_QUERYING_ORBITA_ERROR_CODE = 5001


def validate_person_affiliation(person_affiliation, field_name, message=None):
    person_affiliation = validate_string_not_empty(person_affiliation, field_name, message,
                                                   internal_code=PERSON_INVALID_AFFILIATION_ERROR_CODE)
    person_affiliation = person_affiliation.upper()
    if person_affiliation not in VALID_AFFILIATIONS:
        raise ValidationError(u"Invalid person affiliation for field {0}, "
                              u"expected one of the following values {1}."
                              .format(field_name, str(VALID_AFFILIATIONS)),
                              internal_code=PERSON_INVALID_AFFILIATION_ERROR_CODE)
    return person_affiliation


def validate_person_category(person_category, field_name, message=None,
                             internal_code=PERSON_INVALID_CATEGORY_ERROR_CODE):
    person_category = validate_string_not_empty(person_category, field_name, message,
                                                internal_code=internal_code)
    person_category = person_category.upper()
    if person_category not in VALID_CATEGORIES:
        raise ValidationError(u"Invalid person category for field {0}, "
                              u"expected one of the following values {1}."
                              .format(field_name, str(VALID_CATEGORIES)),
                              internal_code=internal_code)
    return person_category


def validate_person_relationship(relationship, field_name, message=None):
    relationship = validate_string_not_empty(relationship, field_name, message,
                                             internal_code=PERSONS_RELATIONSHIPS_INVALID_RELATIONSHIPS_ERROR_CODE)
    relationship = relationship.upper()
    if relationship not in RELATIONSHIPS_SYMMETRY:
        raise ValidationError(u"Invalid relationship for field {0}, "
                              u"expected one of the following values {1}."
                              .format(field_name, str(RELATIONSHIPS_SYMMETRY.keys())),
                              internal_code=PERSONS_RELATIONSHIPS_INVALID_RELATIONSHIPS_ERROR_CODE)
    return relationship


def validate_transaction_number(transaction_number, field_name, message=None, internal_code=None):
    return validate_string_not_empty(transaction_number, field_name, message, internal_code=internal_code)


def validate_document_type(document_type, field_name, message=None, internal_code=None):
    document_type = validate_string_not_empty(document_type, field_name, message,
                                              internal_code=internal_code)
    document_type = document_type.upper()
    if document_type not in VALID_DOCUMENTS:
        raise ValidationError(u"Invalid document type for field {0}, "
                              u"expected one of the following values {1}."
                              .format(field_name, str(VALID_DOCUMENTS)),
                              internal_code=internal_code)
    return document_type


def validate_document_number(document_number, field_name, message=None, internal_code=None):
    return validate_string_not_empty(document_number, field_name, message, internal_code=internal_code)


def validate_gender(gender, field_name, message=None, internal_code=None):
    gender = validate_string_not_empty(gender, field_name, message, internal_code=internal_code)
    gender = gender.lower()
    if gender not in VALID_GENDERS:
        if gender not in GENDERS_ABBREVIATIONS:
            raise ValidationError(u"Invalid gender for field {0}, expected one of the following values {1}."
                                  .format(field_name, str(VALID_GENDERS)), internal_code=internal_code)
        else:
            return GENDERS_ABBREVIATIONS[gender]
    else:
        return gender


def validate_score(score, field_name, message=None):
    if score is None:
        raise_field_does_not_exists_error(field_name, message)
    elif not isinstance(score, int):
        raise ValidationError(u"The field {0} must be an integer.".format(field_name))
    elif score < 0 or score > 100:
        raise ValidationError(u"Invalid value for field {0}. Expected a number in the following range [0,100]."
                              .format(field_name))
    else:
        return score


def validate_amount(amount, field_name, message=None, internal_code=None, allow_zero=False):
    if amount is None:
        raise_field_does_not_exists_error(field_name, message, internal_code=internal_code)
    elif not isinstance(amount, int):
        raise ValidationError(u"The field {0} must be an integer.".format(field_name), internal_code=internal_code)
    elif amount < 0 or (amount == 0 and not allow_zero):
        if allow_zero:
            raise ValidationError(u"Invalid value for field {0}. Expected a number greater or equal than 0."
                                  .format(field_name), internal_code=internal_code)
        else:
            raise ValidationError(u"Invalid value for field {0}. Expected a number greater than 0."
                                  .format(field_name), internal_code=internal_code)
    else:
        return amount


def validate_capacity(capacity, field_name, allow_none_and_zero=True, message=None, internal_code=None):
    if capacity is None:
        if allow_none_and_zero:
            return 0
        else:
            raise_field_does_not_exists_error(field_name, message, internal_code=internal_code)
    elif not isinstance(capacity, int):
        raise ValidationError(u"The field {0} must be an integer.".format(field_name), internal_code=internal_code)
    elif capacity < 0 or (capacity == 0 and not allow_none_and_zero):
        if allow_none_and_zero:
            raise ValidationError(u"Invalid value for field {0}. Expected a number greater or equal than 0."
                                  .format(field_name), internal_code=internal_code)
        else:
            raise ValidationError(u"Invalid value for field {0}. Expected a number greater than 0."
                                  .format(field_name), internal_code=internal_code)
    else:
        return capacity


def validate_amount_list(amount_list, field_name, allow_empty_list=False, message=None):
    validate_list_exists(amount_list, field_name, allow_empty_list, message)
    return [validate_amount(amount, field_name, message) for amount in amount_list]


def validate_money(price, field_name, message=None, allow_zero=False, internal_code=None):
    if price is None:
        raise_field_does_not_exists_error(field_name, message, internal_code=internal_code)
    elif isinstance(price, int):
        price = float(price)
    if not isinstance(price, float):
        raise ValidationError(u"The field {0} must be a decimal.".format(field_name), internal_code=internal_code)
    elif not allow_zero and price <= 0:
        raise ValidationError(u"Invalid value for field {0}. Expected a number greater than 0."
                              .format(field_name), internal_code=internal_code)
    elif price < 0:
        raise ValidationError(u"Invalid value for field {0}. Expected a number greater or equal than 0."
                              .format(field_name), internal_code=internal_code)
    else:
        return price


def validate_percentage(percentage, field_name, message=None, internal_code=None, allow_zero=False):
    percentage = validate_money(percentage, field_name, message, allow_zero=allow_zero, internal_code=internal_code)
    if percentage >= 100:
        raise ValidationError(u"Invalid value for field {0}. Expected a number smaller than 100."
                              .format(field_name), internal_code=internal_code)
    else:
        return percentage


def validate_reservation_number(reservation_number, field_name, message=None, parse_to_int=True, internal_code=None):
    validate_string_not_empty(reservation_number, field_name, message, internal_code=internal_code)
    if not reservation_number.startswith(Reserva.RESERVATION_NUMBER_PREFIX):
        raise ValidationError(u"Invalid value [{0}] for field {1}. Expected a string starting with {2}."
                              .format(reservation_number,
                                      field_name,
                                      Reserva.RESERVATION_NUMBER_PREFIX),
                              internal_code=internal_code)
    else:
        int_part_reservation_number = reservation_number[len(Reserva.RESERVATION_NUMBER_PREFIX):]
        if len(int_part_reservation_number) == 0:
            raise ValidationError(u"Invalid value [{0}] for field {1}. Expected a number after {2}."
                                  .format(reservation_number,
                                          field_name,
                                          Reserva.RESERVATION_NUMBER_PREFIX),
                                  internal_code=internal_code)
        try:
            int_reservation_number = int(int_part_reservation_number)
            if parse_to_int:
                return int_reservation_number
            else:
                return reservation_number
        except ValueError:
            raise ValidationError(u"Invalid value [{0}] for field {1}. Expected a number after {2}."
                                  .format(reservation_number,
                                          field_name,
                                          Reserva.RESERVATION_NUMBER_PREFIX),
                                  internal_code=internal_code)


def validate_currency_name(currency_name, field_name, message=None):
    return validate_model_id(Moneda, currency_name, field_name, u"Currency", message,
                             CURRENCY_DOES_NOT_EXISTS_ERROR_CODE)


def validate_duration(duration, field_name, message=None, internal_code=None):
    if duration is None:
        raise_field_does_not_exists_error(field_name, message, internal_code=internal_code)
    if not isinstance(duration, int):
        raise ValidationError(u"The field {0} must be an int.".format(field_name), internal_code=internal_code)
    elif duration <= 0:
        raise ValidationError(u"Invalid value for field {0}. Expected a number greater than 0."
                              .format(field_name), internal_code=internal_code)
    else:
        return duration


def validate_ean_code(ean_code, field_name, message=None, internal_code=None):
    ean_code = validate_string_not_empty(ean_code, field_name, message, internal_code=internal_code)
    actual_len = len(ean_code)
    is_digit = ean_code.isdigit()
    for ean_format, expected_len in VALID_EAN_FORMATS.items():
        if actual_len == expected_len and is_digit:
            return ean_code
    if message is None:
        message = u"Invalid value {0} for field {1}. Expected a valid {2} code.".format(ean_code, field_name,
                                                                                        u"/".join(VALID_EAN_FORMATS))
    raise ValidationError(message, internal_code=internal_code)


def validate_major(major, field_name, message=None):
    if major is None:
        raise_field_does_not_exists_error(field_name, message)
    elif not isinstance(major, int):
        raise ValidationError(u"The field {0} must be an integer.".format(field_name))
    elif major < 0:
        raise ValidationError(u"Invalid value for field {0}. Expected a number greater or equal to 0."
                              .format(field_name))
    else:
        return major


def validate_minor(minor, field_name, message=None):
    if minor is None:
        raise_field_does_not_exists_error(field_name, message)
    elif not isinstance(minor, int):
        raise ValidationError(u"The field {0} must be an integer.".format(field_name))
    elif minor < 0:
        raise ValidationError(u"Invalid value for field {0}. Expected a number greater or equal to 0."
                              .format(field_name))
    else:
        return minor


def validate_price_rule_property_name(property_name, field_name, message=None, internal_code=None):
    property_name = validate_string_not_empty(property_name, field_name, message, internal_code=internal_code)
    property_name = property_name.lower()
    if property_name not in VALID_PRICE_PROPERTIES:
        raise ValidationError(u"Invalid value for field {0}, "
                              u"expected one of the following values {1}."
                              .format(field_name, str(VALID_PRICE_PROPERTIES)),
                              internal_code=internal_code)
    return property_name


def validate_age_group(age_group, field_name, message=None, internal_code=None):
    age_group = validate_string_not_empty(age_group, field_name, message, internal_code=internal_code)
    age_group = age_group.upper()
    if age_group not in VALID_AGE_GROUPS:
        raise ValidationError(u"Invalid value for field {0}, "
                              u"expected one of the following values {1}."
                              .format(field_name, str(VALID_AGE_GROUPS)),
                              internal_code=internal_code)
    return age_group


def validate_price_list(price_list, field_name, allow_empty_list=False, message=None):
    validate_list_exists(price_list, field_name, allow_empty_list, message)
    return [validate_money(price, field_name, message) for price in price_list]


def validate_id_person(id_person, message=None):
    return validate_model_id(Persona, id_person, Persona.ID_NAME, "Person", message, PERSON_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_client_beacon(id_beacon, message=None):
    return validate_model_id(ClientBeacon, id_beacon, ClientBeacon.ID_NAME, "Beacon", message)


def validate_id_global_beacon(id_beacon, message=None):
    return validate_model_id(GlobalBeacon, id_beacon, GlobalBeacon.ID_NAME, "Beacon", message)


def validate_id_reading_log(id_log, message=None):
    from CJM.entidades.lecturas.LogLectura import LogLectura
    return validate_model_id(LogLectura, id_log, LogLectura.ID_NAME, "Reading Log", message)


def validate_id_sku(id_sku, message=None):
    return validate_model_id(SKU, id_sku, SKU.ID_NAME, "SKU", message, SKU_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_skus_list(id_skus_list, field_name, allow_empty_list=False, message=None):
    return validate_model_id_list(SKU, id_skus_list, field_name, "SKU", allow_empty_list, message,
                                  internal_code=SKU_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_skus_categories_list(id_skus_categories_list, field_name, allow_empty_list=False, message=None):
    return validate_model_id_list(CategoriaSKU, id_skus_categories_list, field_name, "SKU Category",
                                  allow_empty_list, message, internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_visit(id_visit, message=None):
    return validate_model_id(Visita, id_visit, Visita.ID_NAME, "Visit", message)


def validate_id_feedback(id_feedback, message=None):
    return validate_model_id(Feedback, id_feedback, Feedback.ID_NAME, "Feedback", message)


def validate_id_purchase(id_purchase, message=None):
    return validate_model_id(Compra, id_purchase, Compra.ID_NAME, "Purchase", message)


def validate_id_action(id_action, message=None):
    return validate_model_id(Accion, id_action, Accion.ID_NAME, "Action", message)


def validate_id_refund(id_refund, message=None):
    return validate_model_id(Devolucion, id_refund, Devolucion.ID_NAME, "Refund", message)


def validate_id_order(id_order, message=None):
    return validate_model_id(Pedido, id_order, Pedido.ID_NAME, "Order", message)


def validate_id_event(id_event, message=None):
    return validate_model_id(Evento, id_event, Evento.ID_NAME, "Event", message)


def validate_id_social_event(id_social_event, message=None):
    return validate_model_id(EventoSocial, id_social_event, EventoSocial.ID_NAME, "Social Event", message,
                             SOCIAL_EVENT_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_sku_category(id_sku_category, message=None):
    return validate_model_id(CategoriaSKU, id_sku_category, CategoriaSKU.ID_NAME, "SKU Category", message,
                             SKU_CATEGORY_DOES_NOT_EXISTS_ERROR_CODE)


def validate_ids_currencies_list(ids_currencies, field_name, allow_empty_list=False, message=None):
    return validate_model_id_list(Moneda, ids_currencies, field_name, "Currency", allow_empty_list,
                                  message, CURRENCY_DOES_NOT_EXISTS_ERROR_CODE)


def validate_reservation_keys_list(person_reservations_keys, field_name, allow_empty_list=False, message=None,
                                   get_objects=False):
    return validate_model_keys_list(person_reservations_keys, field_name, "Reservation", allow_empty_list,
                                    message, PERSON_RESERVATION_DOES_NOT_EXISTS_ERROR_CODE, get_objects)


def validate_person_reservation_keys_list(person_reservations_keys, field_name, allow_empty_list=False, message=None,
                                          get_objects=False):
    return validate_model_keys_list(person_reservations_keys, field_name, "Person Reservation", allow_empty_list,
                                    message, PERSON_RESERVATION_DOES_NOT_EXISTS_ERROR_CODE, get_objects)
