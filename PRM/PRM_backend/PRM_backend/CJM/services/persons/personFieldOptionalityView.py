# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.services.persons.personaView import PERSONS_VIEW_NAME
from commons.entidades.optionalities.FieldOptionality import FieldOptionality
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_string_not_empty, \
    OPTIONALITY_FIELD_DOES_NOT_EXISTS_ERROR_CODE, OPTIONALITY_CAN_NOT_BE_CHANGED_ERROR_CODE, \
    OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE, OPTIONALITY_INVALID_APPLICABLE_TYPES_ERROR_CODE

PERSON_OPTIONALITIES_VIEW_NAME = "person-fields-optionality"
app = Blueprint(PERSON_OPTIONALITIES_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/person-fields-optionalities/<string:field_name>/', methods=['PUT'],
           strict_slashes=False)
@with_json_body
def change_optionality_for_person_field(id_client, field_name):
    """
    Cambia la opcionalidad asociada al campo de personas con el nombre dado
        Parametros esperados:
            optionality: str en MANDATORY, OPTIONAL y FORBIDDEN
            default-value: tipo variable según el tipo de campo a modificar, opcional. Se supone None si se omite. Solo
            se espera si optionality es OPTIONAL
            applicable-types: Prohibido, no aplica para personas
    :param id_client: id del cliente asociado
    :param field_name: nombre del campo a modificar
    :return: Opcionalidad asociada al campo de personas con el nombre dado
    """
    id_client = validate_id_client(id_client)

    optionality = request.json.get(FieldOptionality.OPTIONALITY_NAME)
    optionality = FieldOptionality.validate_optionality(optionality)

    applicable_types = request.json.get(FieldOptionality.APPLICABLE_TYPES_NAME)
    if applicable_types is not None:
        raise ValidationError(u"The field {0} is only allowed for person fields."
                              .format(FieldOptionality.APPLICABLE_TYPES_NAME),
                              internal_code=OPTIONALITY_INVALID_APPLICABLE_TYPES_ERROR_CODE)
    else:
        applicable_types = []

    default_value = request.json.get(FieldOptionality.DEFAULT_VALUE_NAME)
    if not optionality == FieldOptionality.OPTIONAL_NAME:
        if default_value is not None:
            raise ValidationError(u"The field {0} is only allowed for {1} fields."
                                  .format(FieldOptionality.DEFAULT_VALUE_NAME,
                                          FieldOptionality.OPTIONAL_NAME),
                                  internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)

    # noinspection PyUnusedLocal
    def change_optionality_for_location_field_on_namespace(id_current_client):
        previous_optionality = FieldOptionality.get_by_view_and_name(PERSONS_VIEW_NAME, field_name)
        if previous_optionality is None:
            raise ValidationError(u"Could not find the field {0}.".format(field_name), error_code=404,
                                  internal_code=OPTIONALITY_FIELD_DOES_NOT_EXISTS_ERROR_CODE)

        if not previous_optionality.permitirCambio:
            raise ValidationError(u"The optionality of field {0} can not be changed.".format(field_name),
                                  internal_code=OPTIONALITY_CAN_NOT_BE_CHANGED_ERROR_CODE)

        if default_value is not None:
            if not isinstance(default_value, unicode):
                # noinspection PyCompatibility
                corrected_default_value = unicode(str(previous_optionality.validate_default_value(default_value)),
                                                  "utf-8")
            else:
                corrected_default_value = validate_string_not_empty(default_value, FieldOptionality.DEFAULT_VALUE_NAME,
                                                                    internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)
        else:
            corrected_default_value = None

        previous_optionality.opcionalidad = optionality
        previous_optionality.subtipos = applicable_types
        previous_optionality.valorPorDefecto = corrected_default_value
        previous_optionality.put()
        return previous_optionality

    return on_client_namespace(id_client, change_optionality_for_location_field_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=PERSON_OPTIONALITIES_VIEW_NAME)


@app.route('/clients/<int:id_client>/person-fields-optionalities/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_person_fields_optionality(id_client):
    """
    Da la lista de opcionalidades de los campos de persona
    :param id_client: id del cliente a consultar
    :return: Lista de opcionalidades de los campos de persona
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_person_fields_optionality_on_namespace(id_current_client):
        return FieldOptionality.list_by_view(PERSONS_VIEW_NAME)

    return on_client_namespace(id_client, list_person_fields_optionality_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSON_OPTIONALITIES_VIEW_NAME)
