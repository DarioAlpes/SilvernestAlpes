# -*- coding: utf-8 -*
from commons.excepciones.apiexceptions import ValidationError


class TipoUbicacion(object):

    COUNTRY = 'COUNTRY'
    REGION = 'REGION'
    CITY = 'CITY'
    POI = 'POI'
    PROPERTY = 'PROPERTY'
    ZONE = 'ZONE'
    AREA = 'AREA'
    TOUCHPOINT = 'TOUCHPOINT'

    LIST_VALID_LOCATIONS_TYPES = [COUNTRY, REGION, CITY, POI, PROPERTY, ZONE, AREA, TOUCHPOINT]
    LIST_DEFAULT_LOCATIONS_TYPES_WITH_WEB = [COUNTRY, REGION, CITY, POI, PROPERTY]
    LIST_DEFAULT_LOCATIONS_TYPES_WITH_PHONE = [POI, PROPERTY, ZONE, AREA]
    LIST_DEFAULT_LOCATIONS_TYPES_WITH_ADDRESS = [POI, PROPERTY, ZONE, AREA]
    LIST_DEFAULT_LOCATIONS_TYPES_WITH_MAIL = [POI, PROPERTY, ZONE, AREA]
    LIST_DEFAULT_LOCATIONS_TYPES_WITH_TAGS = [POI]
    LIST_DEFAULT_LOCATIONS_TYPES_WITH_SUBTYPE = [TOUCHPOINT]

    CASHLESS_POS = u"CASHLESS POS"
    POS = u"POS"
    KIOSK = u"KIOSK"
    AP = u"AP"
    RESTRICTED_AP = u"RESTRICTED AP"
    WIRELESS_AP = u"WIRELESS AP"
    LIST_VALID_LOCATION_SUBTYPES = [CASHLESS_POS, POS, KIOSK, AP, RESTRICTED_AP, WIRELESS_AP]

    @classmethod
    def requires_subtype(cls, location_type):
        return location_type in cls.LIST_DEFAULT_LOCATIONS_TYPES_WITH_SUBTYPE

    @classmethod
    def requires_tags(cls, location_type):
        return location_type in cls.LIST_DEFAULT_LOCATIONS_TYPES_WITH_TAGS

    @classmethod
    def list(cls):
        return cls.LIST_VALID_LOCATIONS_TYPES

    @classmethod
    def is_valid_subtype(cls, subtype, field_name):
        from commons.validations import LOCATION_INVALID_SUBTYPE_ERROR_CODE
        subtype = subtype.upper()
        if subtype not in cls.LIST_VALID_LOCATION_SUBTYPES:
            raise ValidationError(u"Invalid location subtype for field {0}, "
                                  u"expected one of the following values {1}."
                                  .format(field_name, str(cls.LIST_VALID_LOCATION_SUBTYPES)),
                                  internal_code=LOCATION_INVALID_SUBTYPE_ERROR_CODE)
        else:
            return subtype

    @classmethod
    def is_valid(cls, tipo, field_name, internal_code=None):
        if internal_code is None:
            from commons.validations import LOCATION_INVALID_TYPE_ERROR_CODE
            internal_code = LOCATION_INVALID_TYPE_ERROR_CODE
        tipo = tipo.upper()
        if tipo not in cls.LIST_VALID_LOCATIONS_TYPES:
            raise ValidationError(u"Invalid location type for field {0}, "
                                  u"expected one of the following values {1}."
                                  .format(field_name, str(cls.LIST_VALID_LOCATIONS_TYPES)),
                                  internal_code=internal_code)
        else:
            return tipo

    @classmethod
    def is_valid_relation(cls, parent_type, tipo):
        parent_position = cls.LIST_VALID_LOCATIONS_TYPES.index(parent_type)
        child_position = cls.LIST_VALID_LOCATIONS_TYPES.index(tipo)
        if child_position <= parent_position:
            from commons.validations import LOCATION_INVALID_TYPE_RELATIONSHIP_ERROR_CODE
            raise ValidationError(u"Can not add a location of type {0} as child "
                                  u"of a location of type {1}".format(tipo, parent_type),
                                  internal_code=LOCATION_INVALID_TYPE_RELATIONSHIP_ERROR_CODE)
