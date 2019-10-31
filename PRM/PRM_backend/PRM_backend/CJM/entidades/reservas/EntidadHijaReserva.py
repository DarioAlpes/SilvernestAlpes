# -*- coding: utf-8 -*
import json

from CJM.entidades.reservas.Reserva import Reserva
from google.appengine.ext import ndb


class EntidadHijaReserva(ndb.Model):
    ID_NAME = "id"

    RESERVATION_ID_NAME = Reserva.ID_RESERVATION_NAME
    USERNAME_NAME = "username"

    idCliente = ndb.IntegerProperty()
    idReserva = ndb.IntegerProperty(indexed=True)
    usuario = ndb.StringProperty(indexed=True)
    eliminada = ndb.BooleanProperty(indexed=True)

    @classmethod
    def list_without_fetch(cls):
        return cls.query(cls.eliminada == False)

    @classmethod
    def list(cls):
        return cls.list_without_fetch().fetch()

    @classmethod
    def list_ordered_by_id_reservation_without_fetch(cls):
        return cls.list_without_fetch().order(cls.idReserva)

    @classmethod
    def list_by_id_reservation_without_fetch(cls, id_reservation):
        return cls.list_without_fetch().filter(cls.idReserva == id_reservation)

    @classmethod
    def list_by_id_reservation_ordered_by_parents_without_fetch(cls, id_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).order(cls.idReserva)

    @classmethod
    def list_by_id_reservation(cls, id_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).fetch()

    @classmethod
    def get_by_reservation_ids(cls, id_reservation, id_entity):
        entity = cls.get_by_id(id_entity)
        if entity is None or entity.eliminada or entity.idReserva != id_reservation:
            return None
        else:
            return entity

    def deleted(self):
        return self.eliminada

    def mark_as_deleted(self):
        self.eliminada = True

    @classmethod
    def count_by_id_reservation(cls, id_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).count()

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.ID_NAME] = self.key.id()
        fields_dict[self.RESERVATION_ID_NAME] = self.idReserva
        fields_dict[self.USERNAME_NAME] = self.usuario
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
