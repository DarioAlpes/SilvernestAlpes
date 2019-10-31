# -*- coding: utf-8 -*
from commons.excepciones.apiexceptions import ValidationError
from commons.validations import validate_string_not_empty


class SensorType(object):
    STRING_MOBILE = "MOBILE"
    STRING_STATIC = "STATIC"

    VALID_SENSOR_TYPES = {STRING_MOBILE, STRING_STATIC}

    @staticmethod
    def validate_sensor_type(sensor_type, field_name, internal_code):
        corrected_sensor_type = validate_string_not_empty(sensor_type, field_name, internal_code=internal_code)
        corrected_sensor_type = corrected_sensor_type.upper()
        if corrected_sensor_type not in SensorType.VALID_SENSOR_TYPES:
            raise ValidationError(u"Invalid value [{0}] for field {1}.".format(sensor_type,
                                                                                 field_name),
                                  internal_code=internal_code)
        return corrected_sensor_type
