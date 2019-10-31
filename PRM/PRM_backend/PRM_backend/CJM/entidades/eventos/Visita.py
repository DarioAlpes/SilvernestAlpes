# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.TipoEvento import TipoEvento
from commons.entidades.Generador import Generador
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.utils import on_client_namespace


class Visita(Evento):
    FINAL_TIME_NAME = "final-time"
    UBICACION_ID_NAME = Ubicacion.ID_UBICACION_NAME
    idUbicacion = ndb.IntegerProperty(indexed=True)
    tiempoFin = ndb.DateTimeProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_person, initial_time, final_time, id_location):
        id_event = Generador.get_next_event_id()
        new_visit = Visita(key=ndb.Key(Visita, id_event),
                           idInterno=id_event,
                           idCliente=id_client,
                           idPersona=id_person,
                           tipo=TipoEvento.EVENT_TYPE_VISIT,
                           tiempoInicio=initial_time,
                           tiempoFin=final_time,
                           idUbicacion=id_location)
        new_visit.put()
        return new_visit

    @classmethod
    def update(cls, id_person, id_visit, initial_time, final_time, id_location):
        visit = Visita.get_by_id_for_person(id_visit, id_person, u"Visit")
        visit.tiempoInicio = initial_time
        visit.tiempoFin = final_time
        visit.idUbicacion = id_location
        visit.put()
        return visit

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(Visita, self).to_dict()
        fields_dict[self.FINAL_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoFin)
        fields_dict[self.UBICACION_ID_NAME] = self.idUbicacion
        return fields_dict

    def get_description(self):
        return self.get_ubicacion().nombre

    def get_ubicacion(self):
        # noinspection PyUnusedLocal
        def get_ubicacion_on_namespace(id_client):
            return Ubicacion.get_by_id(self.idUbicacion)

        return on_client_namespace(self.idCliente, get_ubicacion_on_namespace)

    def get_number(self):
        return int((self.tiempoFin - self.tiempoInicio).total_seconds())

    @classmethod
    def list_by_location_and_datetimes(cls, id_location, initial_time, final_time):
        child_locations = Ubicacion.list_by_parent_id(id_location)
        child_ids = [ubicacion.key.id() for ubicacion in child_locations]
        return cls.list_by_locations_list_and_datetimes(child_ids, initial_time, final_time)

    @classmethod
    def list_by_locations_list_and_datetimes(cls, ids_locations, initial_time, final_time):
        if len(ids_locations) == 0:
            return []
        # noinspection PyUnresolvedReferences
        results = Visita.query(Visita.idUbicacion.IN(ids_locations)).fetch()
        if initial_time is not None:
            results = [visit for visit in results if visit.tiempoFin >= initial_time]
        if final_time is not None:
            results = [visit for visit in results if visit.tiempoInicio <= final_time]
        return results
