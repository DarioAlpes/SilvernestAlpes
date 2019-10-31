# -*- coding: utf-8 -*
from CJM.entidades.reservas.ConsumoOrden import ConsumoOrden
from google.appengine.ext import ndb

from commons.entidades.users.Usuario import Usuario


class PersonConsumptionByAmount(ConsumoOrden):
    MISSING_AMOUNT_NAME = "missing-amount"
    CURRENCY_NAME = "currency"

    cantidadFaltante = ndb.IntegerProperty()
    posicionOrden = ndb.IntegerProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, id_order, id_sku, amount_consumed,
               missing_amount, order_time, order_position):
        new_person_consumption = cls.create_without_put(id_client, id_reservation, id_person_reservation, id_order,
                                                        id_sku, amount_consumed, missing_amount, order_time,
                                                        order_position)
        new_person_consumption.put()
        return new_person_consumption

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, id_order, id_sku, amount_consumed,
                           missing_amount, order_time, order_position):
        new_person_consumption = PersonConsumptionByAmount(idCliente=id_client,
                                                           idOrden=id_order,
                                                           idReservaPersona=id_person_reservation,
                                                           idReserva=id_reservation,
                                                           idSKUConsumido=id_sku,
                                                           cantidadConsumida=amount_consumed,
                                                           cantidadFaltante=missing_amount,
                                                           posicionOrden=order_position,
                                                           tiempoConsumo=order_time,
                                                           usuario=Usuario.get_current_username(),
                                                           eliminada=False)
        return new_person_consumption

    def clone(self, id_reservation, id_person_reservation, id_order, username):
        return PersonConsumptionByAmount(idCliente=self.idCliente,
                                         idOrden=id_order,
                                         idReservaPersona=id_person_reservation,
                                         idReserva=id_reservation,
                                         idSKUConsumido=self.idSKUConsumido,
                                         cantidadConsumida=self.cantidadConsumida,
                                         cantidadFaltante=self.cantidadFaltante,
                                         posicionOrden=self.posicionOrden,
                                         tiempoConsumo=self.tiempoConsumo,
                                         eliminada=self.eliminada,
                                         usuario=username)

    @classmethod
    def list_by_id_reservation_sorted_without_fetch(cls, id_reservation):
        return super(PersonConsumptionByAmount, cls).list_by_id_reservation_sorted_without_fetch(id_reservation).order(cls.idOrden,
                                                                                                                       cls.posicionOrden)

    @classmethod
    def list_by_ids_person_reservation_sorted_without_fetch(cls, id_reservation, id_person_reservation):
        return super(PersonConsumptionByAmount, cls).list_by_ids_person_reservation_sorted_without_fetch(id_reservation, id_person_reservation).order(cls.idOrden,
                                                                                                                                                      cls.posicionOrden)

    def to_dict(self):
        fields_dict = super(PersonConsumptionByAmount, self).to_dict()
        fields_dict[self.MISSING_AMOUNT_NAME] = self.cantidadFaltante
        return fields_dict

    @classmethod
    def get_consumed_amount_by_ids_skus(cls, id_reservation, id_person_reservation):
        amount_consumed = dict()
        consumptions = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).filter(cls.idSKUConsumido != None)

        for consumption in consumptions:
            amount_consumed[consumption.idSKUConsumido] = \
                amount_consumed.get(consumption.idSKUConsumido, 0) + consumption.cantidadConsumida

        return amount_consumed
