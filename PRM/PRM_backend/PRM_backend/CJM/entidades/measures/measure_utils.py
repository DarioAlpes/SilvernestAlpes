from commons.excepciones.apiexceptions import ValidationError


def extract_first_from_list(value, field_name, error_code=None):
    if isinstance(value, list):
        if len(value) > 0:
            return value[0]
        else:
            raise ValidationError(u"Field {0} can not be empty.".format(field_name), error_code=error_code)
    else:
        return value


def parse_to_int(value, field_name, error_code=None):
    try:
        return int(value)
    except Exception:
        raise ValidationError(u"Field {0} must be an int.".format(field_name), error_code=error_code)
