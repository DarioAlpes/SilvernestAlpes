# -*- coding: utf-8 -*
import json

from CJM.entidades.reservas.EntidadHijaReserva import EntidadHijaReserva
from CJM.entidades.reservas.Reserva import Reserva
from google.appengine.ext import ndb

from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from commons.entidades.locations.Ubicacion import Ubicacion


class BaseTopoff(EntidadHijaReserva):
    ID_NAME = "id"
    TRANSACTION_NUMBER_NAME = "transaction-number"
    TOPOFFS_NAME = "topoffs"
    TOPOFF_TIME_NAME = "topoff-time"

    PERSON_RESERVATION_ID_NAME = ReservaPersona.ID_PERSON_RESERVATION_NAME
    RESERVATION_ID_NAME = Reserva.ID_RESERVATION_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idReservacionPersona = ndb.IntegerProperty(indexed=True)
    cantidadIncluida = ndb.IntegerProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)
    numeroTransaccion = ndb.StringProperty(indexed=True)
    tiempoTopoff = ndb.DateTimeProperty(indexed=True)

    @classmethod
    def list_by_id_reservation_sorted_without_fetch(cls, id_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).order(cls.tiempoTopoff)

    @classmethod
    def list_by_ids_person_reservation_without_fetch(cls, id_reservation, id_person_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).filter(cls.idReservacionPersona == id_person_reservation)

    @classmethod
    def list_by_ids_person_reservation(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).fetch()

    @classmethod
    def list_by_ids_person_reservation_sorted_without_fetch(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).order(cls.tiempoTopoff)

    @classmethod
    def list_by_id_reservation_ordered_by_parents_without_fetch(cls, id_reservation):
        return super(BaseTopoff, cls).list_by_id_reservation_ordered_by_parents_without_fetch(id_reservation).order(cls.idReservacionPersona)

    @classmethod
    def get_by_ids(cls, id_reservation, id_person_reservation, id_topoff):
        topoff = cls.get_by_id(id_topoff)
        if topoff is None or topoff.eliminada or topoff.idReserva != id_reservation or topoff.idReservacionPersona != id_person_reservation:
            return None
        else:
            return topoff

    @classmethod
    def count_by_ids_person_reservation(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).count()

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(BaseTopoff, self).to_dict()
        fields_dict[self.PERSON_RESERVATION_ID_NAME] = self.idReservacionPersona
        fields_dict[self.LOCATION_ID_NAME] = self.idUbicacion
        fields_dict[self.TRANSACTION_NUMBER_NAME] = self.numeroTransaccion
        fields_dict[self.TOPOFF_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoTopoff)
        return fields_dict
