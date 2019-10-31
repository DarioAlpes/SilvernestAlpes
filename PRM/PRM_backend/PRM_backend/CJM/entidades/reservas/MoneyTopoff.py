# -*- coding: utf-8 -*
from CJM.entidades.reservas.BaseTopoff import BaseTopoff
from CJM.entidades.reservas.Reserva import Reserva
from google.appengine.ext import ndb

from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from commons.entidades.users.Usuario import Usuario


class MoneyTopoff(BaseTopoff):
    MONEY_NAME = "money"
    CURRENCY_NAME = "currency"

    PERSON_RESERVATION_ID_NAME = ReservaPersona.ID_PERSON_RESERVATION_NAME
    RESERVATION_ID_NAME = Reserva.ID_RESERVATION_NAME

    dineroIncluido = ndb.FloatProperty(indexed=True)
    moneda = ndb.StringProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, money, currency, transaction_number, topoff_time):
        new_topoff = cls.create_without_put(id_client, id_reservation, id_person_reservation, money, currency,
                                            transaction_number, topoff_time)
        new_topoff.put()
        return new_topoff

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, money, currency, transaction_number,
                           topoff_time):
        new_topoff = MoneyTopoff(idCliente=id_client,
                                 idReservacionPersona=id_person_reservation,
                                 idReserva=id_reservation,
                                 dineroIncluido=money,
                                 moneda=currency,
                                 numeroTransaccion=transaction_number,
                                 tiempoTopoff=topoff_time,
                                 usuario=Usuario.get_current_username(),
                                 eliminada=False)
        return new_topoff

    def clone(self, id_reservation, id_person_reservation, username):
        return MoneyTopoff(idCliente=self.idCliente,
                           idReservacionPersona=id_person_reservation,
                           idReserva=id_reservation,
                           dineroIncluido=self.dineroIncluido,
                           moneda=self.moneda,
                           numeroTransaccion=self.numeroTransaccion,
                           tiempoTopoff=self.tiempoTopoff,
                           eliminada=self.eliminada,
                           usuario=username)

    def to_dict(self):
        fields_dict = super(MoneyTopoff, self).to_dict()
        fields_dict[MoneyTopoff.MONEY_NAME] = self.dineroIncluido
        fields_dict[MoneyTopoff.CURRENCY_NAME] = self.moneda
        return fields_dict

    @classmethod
    def get_available_money_by_currency(cls, id_reservation, id_person_reservation):
        money_available = dict()
        topoffs = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation)

        for topoff in topoffs:
            money_available[topoff.moneda] = \
                money_available.get(topoff.moneda, 0) + topoff.dineroIncluido

        return money_available
