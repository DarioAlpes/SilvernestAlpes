# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb


class EventoSocial(ndb.Model):
    ID_NAME = "id"
    COMPANY_NAME = "company"
    SOCIAL_EVENT_NAME_NAME = "name"
    DESCRIPTION_NAME = "description"
    INITIAL_DATE_NAME = "initial-date"
    FINAL_DATE_NAME = "final-date"
    DOCUMENT_NUMBER_NAME = "document-number"
    DOCUMENT_TYPE_NAME = "document-type"

    ID_SOCIAL_EVENT_NAME = "id-social-event"

    idCliente = ndb.IntegerProperty()
    nombre = ndb.StringProperty()
    descripcion = ndb.TextProperty()
    fechaInicial = ndb.DateTimeProperty(indexed=True)
    fechaFinal = ndb.DateTimeProperty(indexed=True)
    empresa = ndb.StringProperty()
    numeroDocumento = ndb.StringProperty(indexed=True)
    tipoDocumento = ndb.StringProperty(indexed=True)

    @classmethod
    def create(cls, id_client, name, description, company, initial_date, final_date, document_type, document_number):
        new_event = EventoSocial(idCliente=id_client,
                                 nombre=name,
                                 descripcion=description,
                                 empresa=company,
                                 fechaInicial=initial_date,
                                 fechaFinal=final_date,
                                 numeroDocumento=document_number,
                                 tipoDocumento=document_type)

        new_event.put()
        return new_event

    @classmethod
    def list(cls):
        return EventoSocial.query().fetch()

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = dict()
        fields_dict[EventoSocial.ID_NAME] = self.key.id()
        fields_dict[EventoSocial.SOCIAL_EVENT_NAME_NAME] = self.nombre
        fields_dict[EventoSocial.DESCRIPTION_NAME] = self.descripcion
        fields_dict[EventoSocial.COMPANY_NAME] = self.empresa
        fields_dict[EventoSocial.INITIAL_DATE_NAME] = parse_datetime_to_string_on_default_format(self.fechaInicial)
        fields_dict[EventoSocial.FINAL_DATE_NAME] = parse_datetime_to_string_on_default_format(self.fechaFinal)
        fields_dict[EventoSocial.DOCUMENT_TYPE_NAME] = self.tipoDocumento
        fields_dict[EventoSocial.DOCUMENT_NUMBER_NAME] = self.numeroDocumento
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
