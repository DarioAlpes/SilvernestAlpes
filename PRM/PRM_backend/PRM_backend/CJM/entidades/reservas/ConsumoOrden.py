# -*- coding: utf-8 -*
from CJM.entidades.reservas.EntidadHijaReservaPersona import EntidadHijaReservaPersona
from google.appengine.ext import ndb
from CJM.entidades.reservas.orders.Order import Order
from CJM.entidades.skus.SKU import SKU


class ConsumoOrden(EntidadHijaReservaPersona):
    ORDER_ID_NAME = Order.ID_ORDER_NAME
    SKU_ID_NAME = SKU.ID_SKU_NAME

    CONSUMPTION_TIME_NAME = "consumption-time"
    AMOUNT_CONSUMED_NAME = "amount-consumed"

    idOrden = ndb.IntegerProperty(indexed=True)
    tiempoConsumo = ndb.DateTimeProperty(indexed=True)
    cantidadConsumida = ndb.IntegerProperty()
    idSKUConsumido = ndb.IntegerProperty(indexed=True)

    @classmethod
    def list_by_ids_order_without_fetch(cls, id_reservation, id_person_reservation, id_order):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).filter(cls.idOrden == id_order)

    @classmethod
    def list_by_ids_order(cls, id_reservation, id_person_reservation, id_order):
        return cls.list_by_ids_order_without_fetch(id_reservation, id_person_reservation, id_order).fetch()

    @classmethod
    def list_by_id_reservation_sorted_without_fetch(cls, id_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).order(cls.tiempoConsumo)

    @classmethod
    def list_by_ids_person_reservation_sorted_without_fetch(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).order(cls.tiempoConsumo)

    @classmethod
    def list_by_id_reservation_ordered_by_parents_without_fetch(cls, id_reservation):
        return super(ConsumoOrden, cls).list_by_id_reservation_ordered_by_parents_without_fetch(id_reservation).order(cls.idOrden)

    @classmethod
    def get_by_order_ids(cls, id_reservation, id_person_reservation, id_order, id_entity):
        entity = cls.get_by_id(id_entity)
        if entity is None or entity.eliminada or entity.idReserva != id_reservation or entity.idReservaPersona != id_person_reservation or entity.idOrden != id_order:
            return None
        else:
            return entity

    @classmethod
    def list_between_dates(cls, initial_time, final_time):
        query = cls.list_without_fetch()
        if initial_time is not None:
            query = query.filter(cls.tiempoConsumo >= initial_time)
        if final_time is not None:
            query = query.filter(cls.tiempoConsumo <= final_time)
        return query.fetch()

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(ConsumoOrden, self).to_dict()
        fields_dict[self.SKU_ID_NAME] = self.idSKUConsumido
        fields_dict[self.ORDER_ID_NAME] = self.idOrden
        fields_dict[self.AMOUNT_CONSUMED_NAME] = self.cantidadConsumida
        fields_dict[self.CONSUMPTION_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoConsumo)
        return fields_dict
