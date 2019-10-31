# -*- coding: utf-8 -*
from CJM.entidades.reservas.BaseTopoff import BaseTopoff
from google.appengine.ext import ndb

from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from CJM.entidades.skus.SKU import SKU
from commons.entidades.users.Usuario import Usuario


class AmountTopoff(BaseTopoff):
    AMOUNT_NAME = "amount"

    SKU_ID_NAME = SKU.ID_SKU_NAME
    SKU_CATEGORY_ID_NAME = CategoriaSKU.ID_SKU_CATEGORY_NAME

    cantidadIncluida = ndb.IntegerProperty()
    idSku = ndb.IntegerProperty(indexed=True)
    idCategoriaSku = ndb.IntegerProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, amount, id_sku, id_sku_category,
               transaction_number, topoff_time, username=None):
        new_topoff = cls.create_without_put(id_client, id_reservation, id_person_reservation, amount, id_sku,
                                            id_sku_category, transaction_number, topoff_time, username)
        new_topoff.put()
        return new_topoff

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, amount, id_sku, id_sku_category,
                           transaction_number, topoff_time, username=None):
        if username is None:
            username = Usuario.get_current_username()
        new_topoff = AmountTopoff(idCliente=id_client,
                                  idReservacionPersona=id_person_reservation,
                                  idReserva=id_reservation,
                                  cantidadIncluida=amount,
                                  idSku=id_sku,
                                  idCategoriaSku=id_sku_category,
                                  numeroTransaccion=transaction_number,
                                  tiempoTopoff=topoff_time,
                                  usuario=username,
                                  eliminada=False)
        return new_topoff

    def clone(self, id_reservation, id_person_reservation, username):
        return AmountTopoff(idCliente=self.idCliente,
                            idReservacionPersona=id_person_reservation,
                            idReserva=id_reservation,
                            cantidadIncluida=self.cantidadIncluida,
                            idSku=self.idSku,
                            idCategoriaSku=self.idCategoriaSku,
                            numeroTransaccion=self.numeroTransaccion,
                            tiempoTopoff=self.tiempoTopoff,
                            eliminada=self.eliminada,
                            usuario=username)

    def to_dict(self):
        fields_dict = super(AmountTopoff, self).to_dict()
        fields_dict[AmountTopoff.AMOUNT_NAME] = self.cantidadIncluida
        fields_dict[AmountTopoff.SKU_ID_NAME] = self.idSku
        fields_dict[AmountTopoff.SKU_CATEGORY_ID_NAME] = self.idCategoriaSku
        return fields_dict

    @classmethod
    def get_available_amounts_by_ids_categories(cls, id_reservation, id_person_reservation):
        amounts_available = dict()
        topoffs = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).filter(cls.idCategoriaSku != None)

        for topoff in topoffs:
            amounts_available[topoff.idCategoriaSku] = \
                amounts_available.get(topoff.idCategoriaSku, 0) + topoff.cantidadIncluida

        return amounts_available

    @classmethod
    def get_available_amounts_by_ids_skus(cls, id_reservation, id_person_reservation):
        amounts_available = dict()
        topoffs = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).filter(cls.idSku != None)

        for topoff in topoffs:
            amounts_available[topoff.idSku] = \
                amounts_available.get(topoff.idSku, 0) + topoff.cantidadIncluida

        return amounts_available
