# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.services.validations import SUPPORTED_TAGS_INVALID_TOTAL_SIZE_ERROR_CODE
from commons.entidades.Generador import Generador
from commons.excepciones.apiexceptions import ValidationError


class SupportedTag(ndb.Model):
    _ID_SIZE = 8
    _DATE_SIZE = 8
    _SIZE_SIZE = 4
    _AMOUNT_SIZE = 4
    _MONEY_SIZE = 8
    _BOOLEAN_SIZE = 1

    _NUMBER_IDS_HEADER = 2
    _NUMBER_DATES_HEADER = 2
    _NUMBER_SIZES_HEADER = 4
    _NUMBER_OF_BOOLEANS = 1

    _MIN_HEADER_SIZE = _ID_SIZE * _NUMBER_IDS_HEADER + _BOOLEAN_SIZE * _NUMBER_OF_BOOLEANS
    _HEADER_SIZE = _MIN_HEADER_SIZE + _DATE_SIZE * _NUMBER_DATES_HEADER + _SIZE_SIZE * _NUMBER_SIZES_HEADER

    _AMOUNT_SKU_CONSUMPTION_SIZE = _ID_SIZE + _AMOUNT_SIZE
    _AMOUNT_SKU_CATEGORY_CONSUMPTION_SIZE = _ID_SIZE + _AMOUNT_SIZE
    _MONEY_CONSUMPTION_SIZE = _ID_SIZE + _MONEY_SIZE
    _ACCESS_SIZE = _ID_SIZE + _AMOUNT_SIZE

    TAG_NAME_NAME = "name"
    ID_NAME = "id"
    TOTAL_SIZE_NAME = "total-size"
    HEADER_SIZE_NAME = "header-size"
    MIN_HEADER_SIZE_NAME = "min-header-size"
    AVAILABLE_SIZE_NAME = "available-size"
    NUMBER_SKU_CONSUMPTIONS_NAME = "max-amount-sku-consumptions"
    NUMBER_SKU_CATEGORY_CONSUMPTIONS_NAME = "max-amount-sku-category-consumptions"
    NUMBER_MONEY_CONSUMPTIONS_NAME = "max-money-consumptions"
    NUMBER_ACCESSES_NAME = "max-accesses"
    SKU_CONSUMPTIONS_SIZE_NAME = "amount-sku-consumption-size"
    SKU_CATEGORY_CONSUMPTIONS_SIZE_NAME = "amount-sku-category-consumption-size"
    MONEY_CONSUMPTIONS_SIZE_NAME = "money-consumption-size"
    ACCESSES_SIZE_NAME = "access-size"

    idInterno = ndb.IntegerProperty(indexed=True)
    tamanio = ndb.IntegerProperty(indexed=True)

    # noinspection PyUnresolvedReferences,PyTypeChecker
    def to_dict(self):
        fields_dict = dict()
        fields_dict[SupportedTag.TAG_NAME_NAME] = self.key.id()
        fields_dict[SupportedTag.ID_NAME] = self.idInterno
        fields_dict[SupportedTag.TOTAL_SIZE_NAME] = self.tamanio

        fields_dict[SupportedTag.HEADER_SIZE_NAME] = self._HEADER_SIZE
        fields_dict[SupportedTag.MIN_HEADER_SIZE_NAME] = self._MIN_HEADER_SIZE
        fields_dict[SupportedTag.SKU_CONSUMPTIONS_SIZE_NAME] = self._AMOUNT_SKU_CONSUMPTION_SIZE
        fields_dict[SupportedTag.SKU_CATEGORY_CONSUMPTIONS_SIZE_NAME] = self._AMOUNT_SKU_CATEGORY_CONSUMPTION_SIZE
        fields_dict[SupportedTag.MONEY_CONSUMPTIONS_SIZE_NAME] = self._MONEY_CONSUMPTION_SIZE
        fields_dict[SupportedTag.ACCESSES_SIZE_NAME] = self._ACCESS_SIZE

        size_minus_header = max(0, self.tamanio - self._HEADER_SIZE)
        fields_dict[SupportedTag.AVAILABLE_SIZE_NAME] = size_minus_header
        fields_dict[SupportedTag.NUMBER_SKU_CONSUMPTIONS_NAME] = size_minus_header // self._AMOUNT_SKU_CONSUMPTION_SIZE
        fields_dict[SupportedTag.NUMBER_SKU_CATEGORY_CONSUMPTIONS_NAME] = size_minus_header // self._AMOUNT_SKU_CATEGORY_CONSUMPTION_SIZE
        fields_dict[SupportedTag.NUMBER_MONEY_CONSUMPTIONS_NAME] = size_minus_header // self._MONEY_CONSUMPTION_SIZE
        fields_dict[SupportedTag.NUMBER_ACCESSES_NAME] = size_minus_header // self._ACCESS_SIZE
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create(cls, name, size):
        internal_id = Generador.get_next_supported_tag_id()
        new_tag = SupportedTag(
            key=ndb.Key(SupportedTag, name),
            idInterno=internal_id,
            tamanio=size
        )
        new_tag.put()
        return new_tag

    @classmethod
    def list(cls):
        return cls.query().fetch()

    @classmethod
    def get_by_internal_id(cls, id_tag):
        try:
            return SupportedTag.query(SupportedTag.idInterno == id_tag).get()
        except ValueError:
            return None

    @classmethod
    def validate_size(cls, size):
        if size < cls._MIN_HEADER_SIZE:
            raise ValidationError(u"The value of field {0} [{1}] must be greater or equals than {2}."
                                  .format(cls.TOTAL_SIZE_NAME,
                                          size,
                                          cls._MIN_HEADER_SIZE),
                                  internal_code=SUPPORTED_TAGS_INVALID_TOTAL_SIZE_ERROR_CODE)
