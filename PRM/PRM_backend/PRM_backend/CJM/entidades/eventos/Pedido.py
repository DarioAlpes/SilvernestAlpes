# -*- coding: utf-8 -*
import json

from google.appengine.ext import ndb

from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.TipoEvento import TipoEvento
from CJM.entidades.skus.SKU import SKU
from commons.entidades.Generador import Generador
from commons.utils import on_client_namespace


class Pedido(Evento):
    AMOUNTS_NAME = "amounts"
    TOTAL_AMOUNT_NAME = "total-amount"
    SKU_IDS_NAME = u"{0}s".format(SKU.ID_SKU_NAME)

    idSkus = ndb.IntegerProperty(repeated=True)
    cantidades = ndb.IntegerProperty(repeated=True)
    cantidadTotal = ndb.IntegerProperty()

    @classmethod
    def create(cls, id_client, id_person, initial_time, id_skus, amounts):
        id_event = Generador.get_next_event_id()
        new_purchase = Pedido(key=ndb.Key(Pedido, id_event),
                              idInterno=id_event,
                              idCliente=id_client,
                              idPersona=id_person,
                              tipo=TipoEvento.EVENT_TYPE_ORDER,
                              tiempoInicio=initial_time,
                              idSkus=id_skus,
                              cantidades=amounts,
                              cantidadTotal=sum(amounts))
        new_purchase.put()
        return new_purchase

    @classmethod
    def update(cls, id_person, id_order, initial_time, id_skus, amounts):
        purchase = Pedido.get_by_id_for_person(id_order, id_person, u"Order")
        purchase.tiempoInicio = initial_time
        purchase.idSkus = id_skus
        purchase.cantidades = amounts
        purchase.cantidadTotal = sum(amounts)
        purchase.put()
        return purchase

    def to_dict(self):
        fields_dict = super(Pedido, self).to_dict()
        fields_dict[self.SKU_IDS_NAME] = self.idSkus
        fields_dict[self.AMOUNTS_NAME] = self.cantidades
        fields_dict[self.TOTAL_AMOUNT_NAME] = self.cantidadTotal
        return fields_dict

    def get_description(self):
        # noinspection PyUnusedLocal
        def get_description_on_namespace(id_current_client):
            return ','.join(SKU.get_by_id(skuId).nombre for skuId in self.idSkus)

        return on_client_namespace(self.idCliente, get_description_on_namespace)

    def get_number(self):
        return self.cantidadTotal
