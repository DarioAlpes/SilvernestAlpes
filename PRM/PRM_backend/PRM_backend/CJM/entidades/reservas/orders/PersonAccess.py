# -*- coding: utf-8 -*
from CJM.entidades.reservas.EntidadHijaReservaPersona import EntidadHijaReservaPersona
from google.appengine.ext import ndb

from commons.entidades.locations.Ubicacion import Ubicacion
from commons.entidades.users.Usuario import Usuario


class PersonAccess(EntidadHijaReservaPersona):
    AMOUNT_CONSUMED_NAME = "amount-consumed"
    ACCESS_TIME_NAME = "access-time"
    ACCESSES_NAME = "accesses"
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idUbicacionAcceso = ndb.IntegerProperty(indexed=True)
    idEvento = ndb.IntegerProperty(indexed=True)
    tiempoAcceso = ndb.DateTimeProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, id_location, access_time):
        new_person_access = cls.create_without_put(id_client, id_reservation, id_person_reservation, id_location,
                                                   access_time)
        new_person_access.put()
        return new_person_access

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, id_location, access_time):
        new_person_access = PersonAccess(idCliente=id_client,
                                         idReservaPersona=id_person_reservation,
                                         idReserva=id_reservation,
                                         idUbicacionAcceso=id_location,
                                         tiempoAcceso=access_time,
                                         usuario=Usuario.get_current_username(),
                                         eliminada=False)
        return new_person_access

    def clone(self, id_reservation, id_person_reservation, username):
        return PersonAccess(idCliente=self.idCliente,
                            idReservaPersona=id_person_reservation,
                            idReserva=id_reservation,
                            idUbicacionAcceso=self.idUbicacionAcceso,
                            tiempoAcceso=self.tiempoAcceso,
                            eliminada=self.eliminada,
                            usuario=username)

    @classmethod
    def list_by_ids_person_reservation_sorted_without_fetch(cls, id_reservation, id_person_reservation):
        return cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).order(cls.tiempoAcceso)

    @classmethod
    def list_by_id_reservation_sorted_without_fetch(cls, id_reservation):
        return cls.list_by_id_reservation_without_fetch(id_reservation).order(PersonAccess.tiempoAcceso)

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(PersonAccess, self).to_dict()
        fields_dict[PersonAccess.LOCATION_ID_NAME] = self.idUbicacionAcceso
        fields_dict[PersonAccess.AMOUNT_CONSUMED_NAME] = 1
        fields_dict[PersonAccess.ACCESS_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoAcceso)
        return fields_dict

    @classmethod
    def get_consumed_amount_by_ids_locations(cls, id_reservation, id_person_reservation):
        amounts_consumed = dict()
        accesses = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation)

        for access in accesses:
            amounts_consumed[access.idUbicacionAcceso] = amounts_consumed.get(access.idUbicacionAcceso, 0) + 1

        return amounts_consumed

    def associate_visit(self, visit):
        self.idEvento = visit.key.id()

    def mark_as_deleted(self):
        self.eliminada = True
