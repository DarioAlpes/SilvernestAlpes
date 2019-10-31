# -*- coding: utf-8 -*
from CJM.entidades.reservas.BaseTopoff import BaseTopoff
from google.appengine.ext import ndb

from commons.entidades.locations.Ubicacion import Ubicacion
from commons.entidades.users.Usuario import Usuario


class AccessTopoff(BaseTopoff):
    AMOUNT_NAME = "amount"
    UNLIMITED_AMOUNT_NAME = "unlimited-amount"
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    cantidadIncluida = ndb.IntegerProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_reservation, id_person_reservation, amount, id_location,
               transaction_number, topoff_time):
        new_topoff = cls.create_without_put(id_client, id_reservation, id_person_reservation, amount, id_location, transaction_number,
                                            topoff_time)
        new_topoff.put()
        return new_topoff

    @classmethod
    def create_without_put(cls, id_client, id_reservation, id_person_reservation, amount, id_location,
                           transaction_number, topoff_time):
        new_topoff = AccessTopoff(idCliente=id_client,
                                  idReservacionPersona=id_person_reservation,
                                  idReserva=id_reservation,
                                  cantidadIncluida=amount,
                                  idUbicacion=id_location,
                                  numeroTransaccion=transaction_number,
                                  tiempoTopoff=topoff_time,
                                  usuario=Usuario.get_current_username(),
                                  eliminada=False)
        return new_topoff

    def clone(self, id_reservation, id_person_reservation, username):
        return AccessTopoff(idCliente=self.idCliente,
                            idReservacionPersona=id_person_reservation,
                            idReserva=id_reservation,
                            cantidadIncluida=self.cantidadIncluida,
                            idUbicacion=self.idUbicacion,
                            numeroTransaccion=self.numeroTransaccion,
                            tiempoTopoff=self.tiempoTopoff,
                            eliminada=self.eliminada,
                            usuario=username)

    def to_dict(self):
        fields_dict = super(AccessTopoff, self).to_dict()
        fields_dict[AccessTopoff.AMOUNT_NAME] = self.cantidadIncluida
        fields_dict[AccessTopoff.LOCATION_ID_NAME] = self.idUbicacion
        fields_dict[AccessTopoff.UNLIMITED_AMOUNT_NAME] = self.cantidadIncluida == 0
        return fields_dict

    @classmethod
    def get_available_amounts_of_person_reservation_by_ids_locations(cls, id_reservation, id_person_reservation):
        amounts_available = dict()
        topoffs = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation)

        for topoff in topoffs:
            amounts_available[topoff.idUbicacion] = \
                amounts_available.get(topoff.idUbicacion, 0) + topoff.cantidadIncluida

        return amounts_available

    @classmethod
    def get_ids_locations_of_person_reservation_with_unlimited_amount(cls, id_reservation, id_person_reservation):
        unlimited_amount_topoffs = cls.list_by_ids_person_reservation_without_fetch(id_reservation, id_person_reservation).filter(AccessTopoff.cantidadIncluida == 0)
        return [access.idUbicacion for access in unlimited_amount_topoffs]
