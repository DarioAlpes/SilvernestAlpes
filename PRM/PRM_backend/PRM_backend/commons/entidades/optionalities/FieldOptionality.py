# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from commons.entidades.Cliente import Cliente
from commons.excepciones.apiexceptions import ValidationError


class FieldOptionality(ndb.Model):
    MANDATORY_NAME = u"MANDATORY"
    OPTIONAL_NAME = u"OPTIONAL"
    FORBIDDEN_NAME = u"FORBIDDEN"

    VALID_OPTIONALITIES = {MANDATORY_NAME, OPTIONAL_NAME, FORBIDDEN_NAME}

    BOOLEAN_TYPE = u"bool"
    STRING_TYPE = u"str"
    DATE_TYPE = u"date"
    FLOAT_TYPE = u"float"
    INT_TYPE = u"int"
    STRING_ARRAY_TYPE = u"str[]"

    FIELD_NAME_NAME = u"field"
    OPTIONALITY_NAME = u"optionality"
    APPLICABLE_TYPES_NAME = u"applicable-types"
    DEFAULT_VALUE_NAME = u"default-value"
    VIEW_NAME = u"view"

    CLIENT_ID_NAME = Cliente.ID_CLIENT_NAME
    idCliente = ndb.IntegerProperty()
    permitirCambio = ndb.BooleanProperty(indexed=True)
    campo = ndb.StringProperty(indexed=True)
    vista = ndb.StringProperty(indexed=True)
    opcionalidad = ndb.StringProperty(indexed=True)
    subtipos = ndb.StringProperty(indexed=True, repeated=True)
    tipo = ndb.StringProperty(indexed=True)
    valorPorDefecto = ndb.StringProperty()

    # noinspection PyTypeChecker
    def to_dict(self):
        fields_dict = dict()
        fields_dict[FieldOptionality.CLIENT_ID_NAME] = self.idCliente
        fields_dict[FieldOptionality.VIEW_NAME] = self.vista
        fields_dict[FieldOptionality.FIELD_NAME_NAME] = self.campo
        fields_dict[FieldOptionality.OPTIONALITY_NAME] = self.opcionalidad
        if self.subtipos is not None and len(self.subtipos) > 0:
            fields_dict[FieldOptionality.APPLICABLE_TYPES_NAME] = self.subtipos
        if self.valorPorDefecto is not None:
            fields_dict[FieldOptionality.DEFAULT_VALUE_NAME] = self.parse_default_value()
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create(cls, id_client, view, field_name, optionality, field_type, applicable_subtypes=None, default_value=None,
               allow_change=True):
        field_optionality = cls.create_without_put(id_client, view, field_name, optionality, field_type,
                                                   applicable_subtypes, default_value, allow_change)
        field_optionality.put()
        return field_optionality

    @classmethod
    def create_without_put(cls, id_client, view, field_name, optionality, field_type, applicable_subtypes=None,
                           default_value=None, allow_change=True):
        if applicable_subtypes is None:
            applicable_subtypes = []
        str_default_value = None
        if default_value is not None:
            str_default_value = str(default_value)
        field_optionality = FieldOptionality(
            vista=view,
            campo=field_name,
            idCliente=id_client,
            opcionalidad=optionality,
            tipo=field_type,
            valorPorDefecto=str_default_value,
            permitirCambio=allow_change,
            subtipos=applicable_subtypes
        )
        return field_optionality

    @classmethod
    def list(cls):
        return FieldOptionality.query().fetch()

    @classmethod
    def list_by_view(cls, view_name):
        return FieldOptionality.query(FieldOptionality.vista == view_name).fetch()

    @classmethod
    def list_by_view_as_dict(cls, view_name):
        optionalities = cls.list_by_view(view_name)
        return {optionality.campo: optionality for optionality in optionalities}

    def parse_default_value(self):
        if self.tipo == self.BOOLEAN_TYPE:
            return bool(self.valorPorDefecto)
        elif self.tipo == self.STRING_TYPE:
            return self.valorPorDefecto
        elif self.tipo == self.INT_TYPE:
            return int(self.valorPorDefecto)
        elif self.tipo == self.FLOAT_TYPE:
            return float(self.valorPorDefecto)
        else:
            raise ValidationError(u"The default value is not supported for fields of type {0}".format(self.tipo))

    @classmethod
    def validate_optionality(cls, optionality):
        from commons.validations import validate_string_not_empty, OPTIONALITY_INVALID_OPTIONALITY_ERROR_CODE
        optionality = validate_string_not_empty(optionality, cls.OPTIONALITY_NAME,
                                                internal_code=OPTIONALITY_INVALID_OPTIONALITY_ERROR_CODE)
        optionality = optionality.upper()
        if optionality not in cls.VALID_OPTIONALITIES:
            raise ValidationError(u"Invalid value for field {0}, expected one of the following values: {1}."
                                  .format(cls.OPTIONALITY_NAME, cls.VALID_OPTIONALITIES),
                                  internal_code=OPTIONALITY_INVALID_OPTIONALITY_ERROR_CODE)
        return optionality

    @classmethod
    def get_by_view_and_name(cls, view_name, field_name):
        try:
            return FieldOptionality.query(ndb.AND(FieldOptionality.campo == field_name,
                                                  FieldOptionality.vista == view_name)).get()
        except ValueError:
            return None

    def validate_default_value(self, default_value):
        from commons.validations import validate_string_not_empty, validate_bool_not_empty, validate_int_not_empty, \
            validate_float_not_empty, OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE
        if self.tipo == self.BOOLEAN_TYPE:
            return validate_bool_not_empty(default_value, self.DEFAULT_VALUE_NAME,
                                           internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)
        elif self.tipo == self.STRING_TYPE:
            return validate_string_not_empty(default_value, self.DEFAULT_VALUE_NAME,
                                             internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)
        elif self.tipo == self.INT_TYPE:
            return validate_int_not_empty(default_value, self.DEFAULT_VALUE_NAME,
                                          internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)
        elif self.tipo == self.FLOAT_TYPE:
            return validate_float_not_empty(default_value, self.DEFAULT_VALUE_NAME,
                                            internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)
        else:
            raise ValidationError(u"The default value is not supported for fields of type {0}".format(self.tipo),
                                  internal_code=OPTIONALITY_INVALID_DEFAULT_VALUE_ERROR_CODE)
