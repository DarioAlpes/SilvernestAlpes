# -*- coding: utf-8 -*
import json

from google.appengine.api.datastore_errors import Error
from google.appengine.ext import ndb

from CJM.entidades.reports.transactions.Transaction import Transaction
from CJM.entidades.reservas.EventoSocial import EventoSocial
from commons.entidades.Generador import Generador
from commons.entidades.users.Usuario import Usuario
from commons.excepciones.apiexceptions import EntityDoesNotExists


class Reserva(ndb.Model):
    ID_NAME = "id"
    PAYMENT_NAME = "payment"
    IS_PAID_NAME = "is-paid"
    RESERVATION_NUMBER_NAME = "reservation-number"
    RESERVATION_NUMBER_PREFIX = "#R"
    TRANSACTION_NUMBER_NAME = "transaction-number"
    SOCIAL_EVENT_ID_NAME = EventoSocial.ID_SOCIAL_EVENT_NAME
    INITIAL_TIME_NAME = "initial-time"
    FINAL_TIME_NAME = "final-time"
    INCLUDE_CHILDREN_NAME = "include-children"
    USERNAME_NAME = "username"

    ID_RESERVATION_NAME = "id-reservation"

    idCliente = ndb.IntegerProperty()
    valorPagado = ndb.FloatProperty(indexed=True)
    numeroReserva = ndb.IntegerProperty(indexed=True)
    numeroTransaccion = ndb.StringProperty(indexed=True)
    idEventoSocial = ndb.IntegerProperty(indexed=True)
    eliminada = ndb.BooleanProperty(indexed=True)
    usuario = ndb.StringProperty(indexed=True)

    @classmethod
    def create_without_put(cls, id_client, payment, transaction_number, id_event, username=None):
        if username is None:
            username = Usuario.get_current_username()
        reservation_number = Generador.get_next_reservation_number()
        new_reservation = Reserva(idCliente=id_client,
                                  valorPagado=payment,
                                  numeroReserva=reservation_number,
                                  numeroTransaccion=transaction_number,
                                  idEventoSocial=id_event,
                                  eliminada=False,
                                  usuario=username)
        return new_reservation

    def clone(self, username):
        return Reserva(idCliente=self.idCliente,
                       valorPagado=self.valorPagado,
                       numeroReserva=self.numeroReserva,
                       numeroTransaccion=self.numeroTransaccion,
                       idEventoSocial=self.idEventoSocial,
                       eliminada=self.eliminada,
                       usuario=username)

    @classmethod
    def create(cls, id_client, payment, transaction_number, id_event, username=None):
        new_reservation = cls.create_without_put(id_client, payment, transaction_number, id_event, username)
        new_reservation.put()
        return new_reservation

    def update_without_put(self, payment, transaction_number, id_event):
        self.valorPagado = payment
        self.numeroTransaccion = transaction_number
        self.idEventoSocial = id_event

    def update(self, payment, transaction_number, id_event):
        self.update_without_put(payment, transaction_number, id_event)
        self.put()

    def update_payment_info(self, payment, transaction_number):
        self.valorPagado = payment
        self.numeroTransaccion = transaction_number
        self.put()

    @classmethod
    def _list_without_fetch(cls):
        return cls.query(cls.eliminada == False)

    @classmethod
    def list_without_fetch_ordered(cls):
        return cls.query().order(cls.key)

    @classmethod
    def list(cls):
        return cls._list_without_fetch().fetch()

    @classmethod
    def try_get_by_id(cls, id_reservation):
        try:
            reservation = Reserva.get_by_id(id_reservation)
        except (ValueError, Error):
            reservation = None
        if reservation is None or reservation.eliminada:
            from CJM.services.validations import RESERVATION_DOES_NOT_EXISTS_ERROR_CODE
            raise EntityDoesNotExists(u"Reservation[{0}]".format(id_reservation),
                                      internal_code=RESERVATION_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return reservation

    @classmethod
    def get_key_from_id(cls, id_entity):
        return ndb.Key(cls, id_entity)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[Reserva.ID_NAME] = self.key.id()
        fields_dict[Reserva.SOCIAL_EVENT_ID_NAME] = self.idEventoSocial
        fields_dict[self.USERNAME_NAME] = self.usuario
        if self.valorPagado is None:
            fields_dict[Reserva.PAYMENT_NAME] = 0
        else:
            fields_dict[Reserva.PAYMENT_NAME] = self.valorPagado
        fields_dict[Reserva.IS_PAID_NAME] = self.is_paid()
        if self.numeroReserva is not None:
            fields_dict[Reserva.RESERVATION_NUMBER_NAME] = Reserva.RESERVATION_NUMBER_PREFIX + str(self.numeroReserva)
        if self.numeroTransaccion is not None:
            fields_dict[Reserva.TRANSACTION_NUMBER_NAME] = self.numeroTransaccion
        return fields_dict

    def deleted(self):
        return self.eliminada

    def mark_as_deleted(self):
        self.eliminada = True

    def to_json(self):
        return json.dumps(self.to_dict())

    def is_paid(self):
        return self.numeroTransaccion is not None

    @classmethod
    def get_key_from_person_reservation(cls, person_reservation):
        return cls.get_key_from_id(person_reservation.idReserva)

    @classmethod
    def get_key_from_id(cls, id_reservation):
        return ndb.Key(Reserva, id_reservation)

    @classmethod
    def get_by_reservation_number(cls, reservation_number):
        try:
            return Reserva.query(Reserva.numeroReserva == reservation_number).get()
        except ValueError:
            return None

    def get_transactions(self, log_date):
        return [Transaction(self.numeroTransaccion, self.valorPagado, log_date)]
