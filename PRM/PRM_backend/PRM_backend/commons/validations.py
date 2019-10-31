# -*- coding: utf-8 -*
import re
from datetime import datetime

from google.appengine.ext import ndb

import pytz
from google.appengine.api.datastore_errors import Error

from commons.entidades.Cliente import Cliente
from commons.entidades.locations.LocationTag import LocationTag
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.entidades.optionalities.FieldOptionality import FieldOptionality
from commons.entidades.users import Role
from commons.entidades.users.Usuario import Usuario
from commons.excepciones.apiexceptions import ValidationError, EntityDoesNotExists

DATETIME_FORMATS = ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
EMAIL_REGEX = re.compile(r"[^@]+@[^@]+\.[^@]+")
DEFAULT_DATETIME_FORMAT = DATETIME_FORMATS[0]

DATE_FORMATS = ["%Y%m%d", "%B %d, %Y"]
DEFAULT_DATE_FORMAT = DATE_FORMATS[0]

MIN_PASSWORD_LENGTH = 8

CLIENT_DOES_NOT_EXISTS_ERROR_CODE = 1
LOCATION_TAG_DOES_NOT_EXISTS_ERROR_CODE = 2
LOCATION_DOES_NOT_EXISTS_ERROR_CODE = 3
LOCATION_IMAGE_DOES_NOT_EXISTS_ERROR_CODE = 4
OPTIONALITY_FIELD_DOES_NOT_EXISTS_ERROR_CODE = 5
USER_DOES_NOT_EXISTS_ERROR_CODE = 6

CLIENT_INVALID_NAME_ERROR_CODE = 101
CLIENT_INVALID_REQUIRES_LOGIN_ERROR_CODE = 102
CLIENT_INVALID_EXTERNAL_PERSON_SERVICE_ERROR_CODE = 103
CLIENT_INVALID_EXTERNAL_RESERVATIONS_SERVICE_ERROR_CODE = 104

LOCATION_TAG_INVALID_NAME_ERROR_CODE = 201
LOCATION_TAG_ALREADY_EXISTS_ERROR_CODE = 202

LOCATION_INVALID_NAME_ERROR_CODE = 301
LOCATION_INVALID_TYPE_ERROR_CODE = 302
LOCATION_INVALID_ACTIVE_ERROR_CODE = 303
LOCATION_INVALID_DESCRIPTION_ERROR_CODE = 304
LOCATION_INVALID_WEB_ERROR_CODE = 305
LOCATION_INVALID_PHONE_ERROR_CODE = 306
LOCATION_INVALID_ADDRESS_ERROR_CODE = 307
LOCATION_INVALID_MAIL_ERROR_CODE = 308
LOCATION_INVALID_SUBTYPE_ERROR_CODE = 309
LOCATION_INVALID_LATITUDE_ERROR_CODE = 310
LOCATION_INVALID_LONGITUDE_ERROR_CODE = 311
LOCATION_IMAGE_ALREADY_EXISTS_ERROR_CODE = 312
LOCATION_INVALID_TYPE_RELATIONSHIP_ERROR_CODE = 313
LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE = 314

OPTIONALITY_INVALID_APPLICABLE_TYPES_ERROR_CODE = 401
OPTIONALITY_INVALID_OPTIONALITY_ERROR_CODE = 402
OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE = 403
OPTIONALITY_CAN_NOT_BE_CHANGED_ERROR_CODE = 404

USER_INVALID_USERNAME_ERROR_CODE = 501
USER_INVALID_PASSWORD_ERROR_CODE = 502
USER_INVALID_ROLE_ERROR_CODE = 503
USER_ALREADY_EXISTS_ERROR_CODE = 504

COMPENSAR_PERSON_SERVICE_NAME = "COMPENSAR"
VALID_EXTERNAL_PERSON_SERVICES = {COMPENSAR_PERSON_SERVICE_NAME}

COMPENSAR_RESERVATIONS_SERVICE_NAME = "COMPENSAR"
VALID_EXTERNAL_RESERVATIONS_SERVICES = {COMPENSAR_RESERVATIONS_SERVICE_NAME}


def validate_by_optionality_and_function(optionalities, field, entity_type, field_name, validation_function,
                                         allow_empty_list=None, internal_code=None):
    is_applicable_type = optionalities[field_name].subtipos is None \
                         or len(optionalities[field_name].subtipos) == 0 \
                         or entity_type in optionalities[field_name].subtipos
    if optionalities[field_name].opcionalidad == FieldOptionality.MANDATORY_NAME:
        if is_applicable_type:
            field = _evaluate_validation_function(validation_function, field, field_name, allow_empty_list, internal_code)
        else:
            field = None
    elif optionalities[field_name].opcionalidad == FieldOptionality.OPTIONAL_NAME:
        if field is not None and is_applicable_type:
            field = _evaluate_validation_function(validation_function, field, field_name, allow_empty_list, internal_code)
        elif optionalities[field_name].valorPorDefecto is not None:
            field = optionalities[field_name].parse_default_value()
        else:
            field = None
    elif optionalities[field_name].opcionalidad == FieldOptionality.FORBIDDEN_NAME:
        if field is not None:
            raise ValidationError(u"The field {0} is forbidden.".format(field_name))
    return field


def _evaluate_validation_function(validation_function, field, field_name, allow_empty_list, internal_code):
    kargs = dict()
    if allow_empty_list is not None:
        kargs['allow_empty_list'] = allow_empty_list
    if internal_code is not None:
        kargs['internal_code'] = internal_code
    return validation_function(field, field_name, **kargs)


def validate_string_not_empty(string_input, field_name=None, message=None, error_code=None, internal_code=None):
    """
    Valida que el string dado por parámetro no se vacío. Si es vacío genera una excepción de tipo ValidationError con
    el siguiente mensaje:
        message: si el parámetro message es suministrado.
        "El valor de <name> no puede ser vacío.": si el parámetro message no es suministrado pero el parámetro name si.
        "El string no puede ser vacío.": en otro caso.
    :param string_input: string a validar
    :param field_name: nombre del campo. Es opcional.
    :param message: mensaje a mostrar si falla la validación. Es opcional.
    :param error_code: código de error http.
    :param internal_code: código de error interno.
    :returns: string_input si la validación fue exitosa
    ;raises: ValidationError si la valdiación fue fallida
    """
    if string_input is not None and not isinstance(string_input, basestring):
        raise ValidationError(u"The field {0} must be a string.".format(field_name), error_code, internal_code)
    else:
        if string_input is not None:
            string_input = string_input.strip()
        if string_input is None or string_input == "":
            raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
        else:
            return string_input


def validate_username(string_input, field_name, message=None, error_code=None, internal_code=None):
    string_input = validate_string_not_empty(string_input, field_name, message, error_code, internal_code)
    if '$' in string_input:
        raise ValidationError(u"The field {0} can not contain $".format(field_name), internal_code=internal_code)
    return string_input


def validate_web_url(web_url, field_name=None, message=None, error_code=None, internal_code=None):
    return validate_string_not_empty(web_url, field_name, message, error_code, internal_code)


def validate_phone(phone, field_name=None, message=None, error_code=None, internal_code=None):
    return validate_string_not_empty(phone, field_name, message, error_code, internal_code)


def validate_mail(email, field_name, message=None, internal_code=None):
    email = validate_string_not_empty(email, field_name, message, internal_code=internal_code)
    if not EMAIL_REGEX.match(email):
        raise ValidationError(u"Invalid mail for field {0}.".format(field_name), internal_code=internal_code)
    return email


def validate_external_person_service(external_person_service, field_name, message=None, internal_code=None):
    external_person_service = validate_string_not_empty(external_person_service, field_name, message,
                                                        internal_code=internal_code)
    external_person_service = external_person_service.upper()
    if external_person_service not in VALID_EXTERNAL_PERSON_SERVICES:
        raise ValidationError(u"Invalid value for field {0}, expected one of the following values: {1}."
                              .format(field_name,
                                      VALID_EXTERNAL_PERSON_SERVICES),
                              internal_code=internal_code)
    return external_person_service


def validate_external_reservations_service(external_reservations_service, field_name, message=None,
                                           internal_code=None):
    external_reservations_service = validate_string_not_empty(external_reservations_service, field_name, message,
                                                              internal_code=internal_code)
    external_reservations_service = external_reservations_service.upper()
    if external_reservations_service not in VALID_EXTERNAL_RESERVATIONS_SERVICES:
        raise ValidationError(u"Invalid value for field {0}, expected one of the following values: {1}."
                              .format(field_name,
                                      VALID_EXTERNAL_RESERVATIONS_SERVICES),
                              internal_code=internal_code)
    return external_reservations_service


def validate_address(address, field_name=None, message=None, error_code=None, internal_code=None):
    return validate_string_not_empty(address, field_name, message, error_code, internal_code)


def validate_latitude(latitude, field_name, message=None, error_code=None, internal_code=None):
    if latitude is None:
        raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
    else:
        if isinstance(latitude, int):
            latitude = float(latitude)
        elif not isinstance(latitude, float):
            raise ValidationError(u"Expected a float value, got {0}.".format(latitude), internal_code=internal_code)
        if latitude < -90:
            raise ValidationError(u"Expected a value bigger or equal than -90, got {0}.".format(latitude),
                                  internal_code=internal_code)
        elif latitude > 90:
            raise ValidationError(u"Expected a value smaller or equal than 90, got {0}.".format(latitude),
                                  internal_code=internal_code)
        else:
            return latitude


def validate_longitude(longitude, field_name, message=None, error_code=None, internal_code=None):
    if longitude is None:
        raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
    else:
        if isinstance(longitude, int):
            longitude = float(longitude)
        elif not isinstance(longitude, float):
            raise ValidationError(u"Expected a float value, got {0}.".format(longitude))
        if longitude < -180:
            raise ValidationError(u"Expected a value bigger or equal than -180, got {0}.".format(longitude),
                                  internal_code=internal_code)
        elif longitude > 180:
            raise ValidationError(u"Expected a value smaller or equal than 180, got {0}.".format(longitude),
                                  internal_code=internal_code)
        else:
            return longitude


def validate_bool_not_empty(boolean_input, field_name=None, allow_string=False, message=None,
                            error_code=None, internal_code=None):
    if boolean_input is None:
        raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
    elif not isinstance(boolean_input, bool):
        if allow_string:
            boolean_input = validate_string_not_empty(boolean_input, field_name, internal_code=internal_code)
            if boolean_input == "true":
                return True
            elif boolean_input == "false":
                return False
            else:
                raise ValidationError(u"Expected a boolean value, got {0}.".format(boolean_input), error_code,
                                      internal_code)
        else:
            raise ValidationError(u"Expected a boolean value, got {0}.".format(boolean_input), error_code,
                                  internal_code)
    else:
        return boolean_input


def validate_int_not_empty(int_input, field_name=None, message=None, error_code=None, internal_code=None):
    if int_input is None:
        raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
    elif not isinstance(int_input, int):
        raise ValidationError(u"Expected a int value, got {0}.".format(int_input), error_code, internal_code)
    else:
        return int_input


def validate_float_not_empty(float_input, field_name=None, message=None, error_code=None, internal_code=None):
    if float_input is None:
        raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
    elif isinstance(float_input, int):
        float_input = float(float_input)
    if not isinstance(float_input, float):
        raise ValidationError(u"Expected a float value, got {0}.".format(float_input), error_code, internal_code)
    else:
        return float_input


def validate_string_list_exists(list_input, field_name, allow_empty_list=True, message=None):
    list_input = validate_list_exists(list_input, field_name, allow_empty_list, message)
    return [validate_string_not_empty(item, field_name, message) for item in list_input]


def validate_list_exists(list_input, field_name, allow_empty_list=True, message=None, error_code=None,
                         internal_code=None):
    if list_input is None:
        raise_field_does_not_exists_error(field_name, message, error_code, internal_code)
    elif not isinstance(list_input, list):
        raise ValidationError(u"A list was expected for field {0}.".format(field_name), error_code, internal_code)
    elif (not allow_empty_list) and len(list_input) == 0:
        raise ValidationError(u"A non-empty list was expected for field {0}.".format(field_name), error_code,
                              internal_code)
    else:
        return list_input


def validate_datetime(datetime_input, field_name, message=None, allow_none=True, internal_code=None):
    if allow_none and datetime_input is None:
        tz = pytz.timezone('America/Bogota')
        datetime_input = datetime.now(tz).strftime(DEFAULT_DATETIME_FORMAT)
    datetime_input = validate_string_not_empty(datetime_input, field_name, message, internal_code=internal_code)
    for datetime_format in DATETIME_FORMATS:
        try:
            return datetime.strptime(datetime_input, datetime_format)
        except ValueError:
            pass
    raise ValidationError(u"The datetime {0} is invalid for field {1}. One of the following formats was expected {2}."
                          .format(datetime_input, field_name, str(DATETIME_FORMATS)),
                          internal_code=internal_code)


def validate_date(date_input, field_name, message=None, allow_none=False, internal_code=None):
    if allow_none and date_input is None:
        tz = pytz.timezone('America/Bogota')
        date_input = datetime.now(tz).date().strftime(DEFAULT_DATE_FORMAT)
    date_input = validate_string_not_empty(date_input, field_name, message, internal_code=internal_code)
    for date_format in DATE_FORMATS:
        try:
            return datetime.strptime(date_input, date_format).date()
        except ValueError:
            pass
    raise ValidationError(u"The date {0} is invalid for field {1}. One of the following formats was expected  {2}."
                          .format(date_input, field_name, str(DATE_FORMATS)), internal_code=internal_code)


def raise_field_does_not_exists_error(field_name, message, error_code=None, internal_code=None):
    if message is not None:
        raise ValidationError(message, error_code, internal_code)
    elif field_name is not None:
        raise ValidationError(u"The value of field {0} can not be empty.".format(field_name), error_code, internal_code)
    else:
        raise ValidationError(u"The field can not be empty.", error_code, internal_code)


def validate_password(password, username, name="password", message=None):
    password = validate_string_not_empty(password, name, message, internal_code=USER_INVALID_PASSWORD_ERROR_CODE)
    if len(password) < MIN_PASSWORD_LENGTH:
        raise ValidationError(u"The password must be at least {0} characters long.".format(MIN_PASSWORD_LENGTH),
                              internal_code=USER_INVALID_PASSWORD_ERROR_CODE)
    if not any(char.isalpha() for char in password):
        raise ValidationError(u"The password must contain at least one letter.",
                              internal_code=USER_INVALID_PASSWORD_ERROR_CODE)
    if not any(char.isdigit() for char in password):
        raise ValidationError(u"The password must contain at least one number.",
                              internal_code=USER_INVALID_PASSWORD_ERROR_CODE)
    if password.startswith(username):
        raise ValidationError(u"The password can not start with the username.",
                              internal_code=USER_INVALID_PASSWORD_ERROR_CODE)
    return password


def validate_role(role, name, message=None):
    role = validate_string_not_empty(role, name, message, internal_code=USER_INVALID_ROLE_ERROR_CODE)
    return Role.validate_role_is_valid(role, name)


def validate_location_type(location_type, field_name, message=None, internal_code=None):
    if internal_code is None:
        internal_code = LOCATION_INVALID_TYPE_ERROR_CODE
    location_type = validate_string_not_empty(location_type, field_name, message,
                                              internal_code=internal_code)
    return TipoUbicacion.is_valid(location_type, field_name, internal_code)


def validate_location_subtype(location_subtype, field_name, message=None):
    location_subtype = validate_string_not_empty(location_subtype, field_name, message,
                                                 internal_code=LOCATION_INVALID_SUBTYPE_ERROR_CODE)
    return TipoUbicacion.is_valid_subtype(location_subtype, field_name)


def validate_id_client(id_client, message=None):
    return validate_model_id(Cliente, id_client, Cliente.ID_NAME, "Client", message, CLIENT_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_user(id_user, message=None):
    return validate_model_id(Usuario, id_user, Usuario.USERNAME_NAME, "User", message, USER_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_location(id_location, message=None):
    return validate_model_id(Ubicacion, id_location, Ubicacion.ID_NAME, "Location", message,
                             LOCATION_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_locations_list(id_locations_list, field_name, allow_empty_list=False, message=None):
    return validate_model_id_list(Ubicacion, id_locations_list, field_name, "Location", allow_empty_list, message,
                                  LOCATION_DOES_NOT_EXISTS_ERROR_CODE)


def validate_id_location_tags_list(id_location_tags_list, field_name, allow_empty_list=False, message=None):
    return validate_model_id_list(LocationTag, id_location_tags_list, field_name, "Location Tag", allow_empty_list,
                                  message, LOCATION_TAG_DOES_NOT_EXISTS_ERROR_CODE)


ERROR_CODE_ENTITY_DOESNT_EXIST = 404


def validate_model_id(entity_class, entity_id, field_name, entity_name, message, internal_code=None):
    if entity_id is None:
        field_name = u"{0}[{1}]".format(entity_name, field_name)
        raise_field_does_not_exists_error(field_name, message, ERROR_CODE_ENTITY_DOESNT_EXIST, internal_code)
    elif isinstance(entity_id, str) or isinstance(entity_id, unicode):
        entity_id = validate_string_not_empty(entity_id, field_name, message, ERROR_CODE_ENTITY_DOESNT_EXIST,
                                              internal_code)
    try:
        entity = entity_class.get_by_id(entity_id)
    except (ValueError, Error):
        entity = None
    if entity is None or not isinstance(entity, entity_class):
        raise EntityDoesNotExists(u"{0}[{1}]".format(entity_name, entity_id), internal_code)
    else:
        return entity_id


def validate_model_id_list(entity_class, entities_id_list, field_name, entity_name, allow_empty_list=False,
                           message=None, internal_code=None):
    entities_id_list = validate_list_exists(entities_id_list, field_name, allow_empty_list, message,
                                            ERROR_CODE_ENTITY_DOESNT_EXIST, internal_code)
    keys_list = [ndb.Key(entity_class, entity_id) for entity_id in entities_id_list]
    validate_model_keys_list(keys_list, field_name, entity_name, allow_empty_list, message, internal_code)
    return entities_id_list


def validate_model_keys_list(keys_list, field_name, entity_name, allow_empty_list=False,
                             message=None, internal_code=None, get_objects=False):
    keys_list = validate_list_exists(keys_list, field_name, allow_empty_list, message,
                                     ERROR_CODE_ENTITY_DOESNT_EXIST, internal_code)
    try:
        entities_list = ndb.get_multi(keys_list)
    except (ValueError, Error):
        entities_list = [None]

    for (entity, entity_key) in zip(entities_list, keys_list):
        if entity is None:
            raise EntityDoesNotExists(u"{0}[{1}]".format(entity_name, entity_key.id()), internal_code)

    if get_objects:
        return entities_list
    else:
        return keys_list


def parse_datetime_to_string_on_default_format(datetime_to_parse):
    if datetime_to_parse is not None:
        return datetime_to_parse.strftime(DEFAULT_DATETIME_FORMAT)
    else:
        return None


def parse_date_to_string_on_default_format(date_to_parse):
    if date_to_parse is not None:
        return date_to_parse.strftime(DEFAULT_DATE_FORMAT)
    else:
        return None
