# -*- coding: utf-8 -*
from CJM.entidades.reservas.EntidadHijaReservaPersona import EntidadHijaReservaPersona
from google.appengine.ext import ndb

from CJM.entidades.skus.SKU import SKU
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.entidades.users.Usuario import Usuario


class Order(EntidadHijaReservaPersona):
    ORDER_TIME_NAME = "order-time"
    ORDERS_NAME = "orders"
    AMOUNT_CONSUMPTIONS_NAME = "amount-consumptions"
    MONEY_CONSUMPTIONS_NAME = "money-consumptions"
    ID_ORDER_NAME = "id-order"
    CURRENCY_NAME = "currency"

    SKU_ID_NAME = SKU.ID_SKU_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idCliente = ndb.IntegerProperty()
    idUbicacionOrden = ndb.IntegerProperty(indexed=True)
    idEvento = ndb.IntegerProperty(indexed=True)
    tiempoOrden = ndb.DateTimeProperty(indexed=True)
    moneda = ndb.StringProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, currency, order_time, id_location):
        new_order = cls.create_without_put(id_client, id_reservation, id_person_reservation, currency, order_time, id_location)
        new_order.put()
        return new_order

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, currency, order_time, id_location):
        new_order = Order(idCliente=id_client,
                          idReservaPersona=id_person_reservation,
                          idReserva=id_reservation,
                          idUbicacionOrden=id_location,
                          moneda=currency,
                          tiempoOrden=order_time,
                          usuario=Usuario.get_current_username(),
                          eliminada=False)
        return new_order

    def clone(self, id_reservation, id_person_reservation, username):
        return Order(idCliente=self.idCliente,
                     idReservaPersona=id_person_reservation,
                     idReserva=id_reservation,
                     idUbicacionOrden=self.idUbicacionOrden,
                     moneda=self.moneda,
                     tiempoOrden=self.tiempoOrden,
                     eliminada=self.eliminada,
                     usuario=username)

    def associate_timeline_purchase(self, timeline_purchase):
        self.idEvento = timeline_purchase.key.id()

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(Order, self).to_dict()
        fields_dict[Order.LOCATION_ID_NAME] = self.idUbicacionOrden
        fields_dict[Order.CURRENCY_NAME] = self.moneda
        fields_dict[Order.ORDER_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoOrden)
        return fields_dict
