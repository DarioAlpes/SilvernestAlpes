# -*- coding: utf-8 -*
import json


class AsociacionUbicacionVentaSKUoUbicacion(object):

    def __init__(self, sale_location, entity, as_sku):
        self.sale_location = sale_location
        self.entity = entity
        self.as_sku = as_sku

    def to_dict(self):
        from CJM.entidades.skus.UbicacionVentaSKU import UbicacionVentaSKU
        fields_dict = self.entity.to_dict()
        fields_dict[UbicacionVentaSKU.PRICE_NAME] = self.sale_location.precio
        if self.as_sku:
            fields_dict[UbicacionVentaSKU.LOCATION_ID_NAME] = self.sale_location.idUbicacion
        else:
            fields_dict[UbicacionVentaSKU.SKU_ID_NAME] = self.sale_location.idSku
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
