# -*- coding: utf-8 -*
from CJM.entidades.reservas.EntidadHijaReserva import EntidadHijaReserva
from google.appengine.ext import ndb

from CJM.entidades.reservas.ReservaPersona import ReservaPersona


class EntidadHijaReservaPersona(EntidadHijaReserva):
    ID_NAME = "id"

    PERSON_RESERVATION_ID_NAME = ReservaPersona.ID_PERSON_RESERVATION_NAME

    idReservaPersona = ndb.IntegerProperty(indexed=True)

    @classmethod
    def list_by_ids_person_reservation_without_fetch(cls, id_reservation, id_person_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).filter(cls.idReservaPersona == id_person_reservation)

    @classmethod
    def list_by_ids_person_reservation(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).fetch()

    @classmethod
    def list_by_id_reservation_ordered_by_parents_without_fetch(cls, id_reservation):
        return super(EntidadHijaReservaPersona, cls).list_by_id_reservation_ordered_by_parents_without_fetch(id_reservation).order(cls.idReservaPersona)

    @classmethod
    def get_by_ids(cls, id_reservation, id_person_reservation, id_entity):
        entity = cls.get_by_id(id_entity)
        if entity is None or entity.eliminada or entity.idReserva != id_reservation or entity.idReservaPersona != id_person_reservation:
            return None
        else:
            return entity

    @classmethod
    def count_by_ids_person_reservation(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).count()

    def to_dict(self):
        fields_dict = super(EntidadHijaReservaPersona, self).to_dict()
        fields_dict[self.PERSON_RESERVATION_ID_NAME] = self.idReservaPersona
        return fields_dict
