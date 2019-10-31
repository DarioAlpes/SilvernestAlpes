# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from CJM.entidades.reservas.EntidadHijaReservaPersona import EntidadHijaReservaPersona
from commons.entidades.users.Usuario import Usuario


class ActivacionesReservaPersona(EntidadHijaReservaPersona):
    USERNAME_NAME = "username"
    OPERATION_TIME_NAME = "operation-time"
    INITIAL_TIME_NAME = "initial-time"
    FINAL_TIME_NAME = "final-time"

    fechaOperacion = ndb.DateTimeProperty(indexed=True)
    esActivacion = ndb.BooleanProperty(indexed=True)
    usuario = ndb.StringProperty(indexed=True)

    @classmethod
    def create_without_put(cls, person_reservation, operation_time, is_activation):
        username = Usuario.get_current_username()
        new_activation = ActivacionesReservaPersona(idReserva=person_reservation.idReserva,
                                                    idReservaPersona=person_reservation.key.id(),
                                                    usuario=username,
                                                    esActivacion=is_activation,
                                                    fechaOperacion=operation_time,
                                                    eliminada=False)

        new_activation.put()
        return new_activation

    def clone(self, id_reservation, id_person_reservation):
        if self.eliminada is None:
            deleted = False
        else:
            deleted = self.eliminada
        return ActivacionesReservaPersona(idReserva=id_reservation,
                                          idReservaPersona=id_person_reservation,
                                          usuario=self.usuario,
                                          esActivacion=self.esActivacion,
                                          fechaOperacion=self.fechaOperacion,
                                          eliminada=deleted)

    @classmethod
    def _list_without_fetch(cls):
        return cls.query()

    @classmethod
    def list_by_times(cls, initial_time, final_time):
        query = cls._list_without_fetch()
        if initial_time is not None:
            query = query.filter(cls.fechaOperacion >= initial_time)
        if final_time is not None:
            query = query.filter(cls.fechaOperacion <= final_time)
        return query.fetch()

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(ActivacionesReservaPersona, self).to_dict()
        fields_dict[self.OPERATION_TIME_NAME] = parse_datetime_to_string_on_default_format(self.fechaOperacion)
        fields_dict[self.USERNAME_NAME] = self.usuario
        return fields_dict
