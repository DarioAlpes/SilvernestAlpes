# -*- coding: utf-8 -*
import json

from CJM.entidades.reservas.ConsumoOrden import ConsumoOrden
from google.appengine.ext import ndb

from commons.entidades.users.Usuario import Usuario


class PersonConsumptionByMoney(ConsumoOrden):
    MONEY_CONSUMED_NAME = "money-consumed"
    MISSING_MONEY_NAME = "missing-money"
    CURRENCY_NAME = "currency"

    dineroConsumido = ndb.FloatProperty()
    dineroFaltante = ndb.FloatProperty()
    moneda = ndb.StringProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, id_order, id_sku, amount_consumed, money_consumed,
               missing_money, order_currency, order_time):
        new_person_consumption = cls.create_without_put(id_client, id_reservation, id_person_reservation, id_order,
                                                        id_sku, amount_consumed, money_consumed, missing_money,
                                                        order_currency, order_time)
        new_person_consumption.put()
        return new_person_consumption

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, id_order, id_sku, amount_consumed,
                           money_consumed, missing_money, order_currency, order_time):
        new_person_consumption = PersonConsumptionByMoney(idCliente=id_client,
                                                          idOrden=id_order,
                                                          idReservaPersona=id_person_reservation,
                                                          idReserva=id_reservation,
                                                          idSKUConsumido=id_sku,
                                                          cantidadConsumida=amount_consumed,
                                                          dineroConsumido=money_consumed,
                                                          dineroFaltante=missing_money,
                                                          moneda=order_currency,
                                                          tiempoConsumo=order_time,
                                                          usuario=Usuario.get_current_username(),
                                                          eliminada=False)
        return new_person_consumption

    def clone(self, id_reservation, id_person_reservation, id_order, username):
        return PersonConsumptionByMoney(idCliente=self.idCliente,
                                        idOrden=id_order,
                                        idReservaPersona=id_person_reservation,
                                        idReserva=id_reservation,
                                        idSKUConsumido=self.idSKUConsumido,
                                        cantidadConsumida=self.cantidadConsumida,
                                        dineroConsumido=self.dineroConsumido,
                                        dineroFaltante=self.dineroFaltante,
                                        moneda=self.moneda,
                                        tiempoConsumo=self.tiempoConsumo,
                                        eliminada=self.eliminada,
                                        usuario=username)

    def to_dict(self):
        fields_dict = super(PersonConsumptionByMoney, self).to_dict()
        fields_dict[PersonConsumptionByMoney.MONEY_CONSUMED_NAME] = self.dineroConsumido
        fields_dict[PersonConsumptionByMoney.MISSING_MONEY_NAME] = self.dineroFaltante
        fields_dict[PersonConsumptionByMoney.CURRENCY_NAME] = self.moneda
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def get_consumed_money_by_currency(cls, id_reservation, id_person_reservation):
        money_consumed = dict()
        consumptions = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).filter(cls.moneda != None)

        for consumption in consumptions:
            money_consumed[consumption.moneda] = \
                money_consumed.get(consumption.moneda, 0) + consumption.dineroConsumido

        return money_consumed
