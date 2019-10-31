# -*- coding: utf-8 -*
from google.appengine.ext import ndb
from google.appengine.ext.ndb import polymodel

from CJM.entidades.eventos.TipoEvento import TipoEvento
from CJM.entidades.persons.Persona import Persona
from commons.excepciones.apiexceptions import ResourceIsNotChild, EntityDoesNotExists


class Evento(polymodel.PolyModel):
    PERSON_ID_NAME = Persona.ID_PERSON_NAME
    TYPE_NAME = "type"
    INITIAL_TIME_NAME = "initial-time"
    DESCRIPTION_NAME = "description"
    NUMBER_NAME = "number"
    ID_NAME = "id"
    ID_EVENT_NAME = "id-event"

    idInterno = ndb.IntegerProperty()
    idCliente = ndb.IntegerProperty()
    idPersona = ndb.IntegerProperty(indexed=True)
    tipo = ndb.IntegerProperty(indexed=True)
    tiempoInicio = ndb.DateTimeProperty(indexed=True)

    @classmethod
    def get_by_id_for_person(cls, id_event, id_person, event_name):
        if id_event is None:
            raise EntityDoesNotExists(event_name)
        current_event = cls.get_by_id(id_event)

        if current_event is None:
            raise EntityDoesNotExists(event_name)

        if id_person != current_event.idPersona:
            raise ResourceIsNotChild(id_person, u"Person", id_event, event_name)
        else:
            return current_event

    @classmethod
    def delete_by_id_for_person(cls, id_event, id_person, event_name):
        current_event = cls.get_by_id(id_event)

        if id_person != current_event.idPersona:
            raise ResourceIsNotChild(id_person, u"Person", id_event, event_name)
        else:
            current_event.key.delete()
            return current_event

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = dict()
        fields_dict[self.ID_NAME] = self.idInterno
        fields_dict[self.PERSON_ID_NAME] = self.idPersona
        fields_dict[self.TYPE_NAME] = TipoEvento.event_type_to_str(self.tipo)
        fields_dict[self.INITIAL_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoInicio)
        fields_dict[self.DESCRIPTION_NAME] = self.get_description()
        fields_dict[self.NUMBER_NAME] = self.get_number()
        return fields_dict

    @classmethod
    def list(cls):
        return cls.query().fetch()

    @classmethod
    def list_by_person(cls, id_person):
        return cls.query(Evento.idPersona == id_person).order(-Evento.tiempoInicio).fetch()

    # noinspection PyMethodMayBeStatic
    def to_json_derived_fields(self):
        """
        Método a sobreescribir en todas las subclases de Evento que retorna el json del campo derivado precedido por ,
        :return: json de los campos derivados
        """
        return ""

    # noinspection PyMethodMayBeStatic
    def get_description(self):
        """
        Método a sobreescribir en todas las subclases de Evento que retorna la descripción del evento como str
        :return: str con la descripción del evento
        """
        return ""

    # noinspection PyMethodMayBeStatic
    def get_number(self):
        """
        Método a sobreescribir en todas las subclases de Evento que retorna el número del evento como int
        :return: int con el número del evento
        """
        return 0
