# -*- coding: utf-8 -*
import json
from functools import reduce
from operator import mul

from google.appengine.ext import ndb

from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.TipoEvento import TipoEvento
from CJM.entidades.skus.SKU import SKU
from commons.entidades.Generador import Generador
from commons.utils import on_client_namespace


class Compra(Evento):
    PRICES_NAME = "prices"
    AMOUNTS_NAME = "amounts"
    TOTAL_PRICE_NAME = "total-price"
    TOTAL_AMOUNT_NAME = "total-amount"
    SKU_IDS_NAME = u"{0}s".format(SKU.ID_SKU_NAME)

    idSkus = ndb.IntegerProperty(repeated=True)
    precios = ndb.FloatProperty(repeated=True)
    cantidades = ndb.IntegerProperty(repeated=True)
    precioTotal = ndb.FloatProperty()
    cantidadTotal = ndb.IntegerProperty()

    @classmethod
    def create(cls, id_client, id_person, initial_time, id_skus, prices, amounts):
        id_event = Generador.get_next_event_id()
        new_purchase = Compra(key=ndb.Key(Compra, id_event),
                              idInterno=id_event,
                              idCliente=id_client,
                              idPersona=id_person,
                              tipo=TipoEvento.EVENT_TYPE_PURCHASE,
                              tiempoInicio=initial_time,
                              idSkus=id_skus,
                              precios=prices,
                              cantidades=amounts,
                              precioTotal=sum(reduce(mul, data) for data in zip(prices, amounts)),
                              cantidadTotal=sum(amounts))
        new_purchase.put()
        return new_purchase

    @classmethod
    def update(cls, id_person, id_purchase, initial_time, id_skus, prices, amounts):
        purchase = Compra.get_by_id_for_person(id_purchase, id_person, u"Purchase")
        purchase.tiempoInicio = initial_time
        purchase.idSkus = id_skus
        purchase.precios = prices
        purchase.cantidades = amounts
        purchase.precioTotal = sum(reduce(mul, data) for data in zip(prices, amounts))
        purchase.cantidadTotal = sum(amounts)
        purchase.put()
        return purchase

    def to_dict(self):
        fields_dict = super(Compra, self).to_dict()
        fields_dict[self.SKU_IDS_NAME] = self.idSkus
        fields_dict[self.PRICES_NAME] = self.precios
        fields_dict[self.AMOUNTS_NAME] = self.cantidades
        fields_dict[self.TOTAL_PRICE_NAME] = self.precioTotal
        fields_dict[self.TOTAL_AMOUNT_NAME] = self.cantidadTotal
        return fields_dict

    def get_description(self):
        # noinspection PyUnusedLocal
        def get_description_on_namespace(id_current_client):
            return u','.join(SKU.get_by_id(skuId).nombre for skuId in self.idSkus)

        return on_client_namespace(self.idCliente, get_description_on_namespace)

    def get_number(self):
        return self.cantidadTotal

    @staticmethod
    def get_purchases_of_sku_by_person_between_dates(id_person, id_sku, initial_time, final_time):
        query_skus = ndb.AND(Evento.idPersona == id_person, Compra.idSkus == id_sku)
        if initial_time is not None:
            query_skus = ndb.AND(query_skus, Evento.tiempoInicio >= initial_time)
        if final_time is not None:
            query_skus = ndb.AND(query_skus, Evento.tiempoInicio <= final_time)
        return Compra.query(query_skus).fetch()
