# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from commons.entidades.Generador import Generador


class SKU(ndb.Model):
    IDS_SKUS_NAME = "ids-skus"
    SKU_NAME_NAME = "name"
    ID_NAME = "id"
    ID_SKU_NAME = "id-sku"
    UNIT_OF_MEASURE_NAME = "measure"
    COST_NAME = "cost"
    TAX_RATE_NAME = "tax-rate"
    EAN_CODE_NAME = "ean-code"
    IMAGE_KEY_NAME = "image-key"
    EXTERNAL_CODE_NAME = "external-code"
    SKU_CATEGORY_ID_NAME = CategoriaSKU.ID_SKU_CATEGORY_NAME

    idInterno = ndb.IntegerProperty()
    nombre = ndb.StringProperty()
    medida = ndb.StringProperty()
    costo = ndb.FloatProperty()
    impuesto = ndb.FloatProperty(indexed=True)
    codigoEAN = ndb.StringProperty()
    idCategoriaSku = ndb.IntegerProperty(indexed=True)
    codigoExterno = ndb.StringProperty(indexed=True)
    blobKey = ndb.BlobKeyProperty()

    def to_dict(self):
        fields_dict = dict()
        fields_dict[SKU.ID_NAME] = self.key.id()
        fields_dict[SKU.SKU_NAME_NAME] = self.nombre
        fields_dict[SKU.UNIT_OF_MEASURE_NAME] = self.medida
        fields_dict[SKU.COST_NAME] = self.costo
        fields_dict[SKU.TAX_RATE_NAME] = self.impuesto
        fields_dict[SKU.SKU_CATEGORY_ID_NAME] = self.idCategoriaSku
        fields_dict[SKU.EAN_CODE_NAME] = self.codigoEAN
        fields_dict[SKU.EXTERNAL_CODE_NAME] = self.codigoExterno
        if self.blobKey is None:
            str_blob_key = None
        else:
            str_blob_key = repr(self.blobKey)
        fields_dict[SKU.IMAGE_KEY_NAME] = str_blob_key
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create(cls, name, measure, cost, ean_code, id_sku_category, tax_rate, external_code):
        id_sku = Generador.get_next_sku_id()
        new_sku = SKU(
            key=ndb.Key(SKU, id_sku),
            idInterno=id_sku,
            nombre=name,
            medida=measure,
            costo=cost,
            impuesto=tax_rate,
            idCategoriaSku=id_sku_category,
            codigoExterno=external_code
        )
        if ean_code is not None:
            new_sku.codigoEAN = ean_code
        new_sku.put()
        return new_sku

    @classmethod
    def list(cls):
        return SKU.query().fetch()

    @classmethod
    def update(cls, current_id_sku, name, measure, cost, ean_code, id_sku_category, tax_rate, external_code):
        sku = SKU.get_by_id(current_id_sku)
        sku.nombre = name
        sku.medida = measure
        sku.costo = cost
        sku.impuesto = tax_rate
        sku.codigoEAN = ean_code
        sku.idCategoriaSku = id_sku_category
        sku.codigoExterno = external_code
        sku.put()
        return sku

    @classmethod
    def get_by_external_code_async(cls, external_code):
        return cls.query(cls.codigoExterno == external_code).get_async()
