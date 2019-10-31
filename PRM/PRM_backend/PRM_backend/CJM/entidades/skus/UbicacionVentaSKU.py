# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.skus.AsociacionUbicacionVentaSKUoUbicacion import AsociacionUbicacionVentaSKUoUbicacion
from CJM.entidades.skus.SKU import SKU
from commons.entidades.locations.Ubicacion import Ubicacion


class UbicacionVentaSKU(ndb.Model):
    PRICE_NAME = "price"
    SKU_ID_NAME = SKU.ID_SKU_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idSku = ndb.IntegerProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)
    precio = ndb.FloatProperty()

    def to_dict(self):
        fields_dict = dict()
        fields_dict[UbicacionVentaSKU.SKU_ID_NAME] = self.idSku
        fields_dict[UbicacionVentaSKU.LOCATION_ID_NAME] = self.idUbicacion
        fields_dict[UbicacionVentaSKU.PRICE_NAME] = self.precio
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create(cls, id_sku, id_location, price):
        new_location_sales = UbicacionVentaSKU(idSku=id_sku,
                                               idUbicacion=id_location,
                                               precio=price)
        new_location_sales.put()
        return new_location_sales

    @classmethod
    def get_skus_by_location(cls, id_location):
        sales_locations = UbicacionVentaSKU.query(UbicacionVentaSKU.idUbicacion == id_location)
        skus = ndb.get_multi([ndb.Key(SKU, sales_location.idSku) for sales_location in sales_locations])
        return [AsociacionUbicacionVentaSKUoUbicacion(sale_location, sku, as_sku=True)
                for (sale_location, sku) in zip(sales_locations, skus)]

    @classmethod
    def get_locations_by_sku(cls, id_sku):
        sales_locations = UbicacionVentaSKU.query(UbicacionVentaSKU.idSku == id_sku)
        locations = ndb.get_multi(
            [ndb.Key(Ubicacion, sales_location.idUbicacion) for sales_location in sales_locations])
        return [AsociacionUbicacionVentaSKUoUbicacion(sale_location, location, as_sku=False)
                for (sale_location, location) in zip(sales_locations, locations)]

    @classmethod
    def get_by_sku_and_location(cls, id_sku, id_location):
        sales_location = UbicacionVentaSKU.query(ndb.AND(UbicacionVentaSKU.idSku == id_sku,
                                                         UbicacionVentaSKU.idUbicacion == id_location)).get()
        return sales_location
