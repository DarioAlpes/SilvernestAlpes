# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.TipoEvento import TipoEvento
from google.appengine.ext import ndb

from commons.entidades.Generador import Generador
from commons.utils import on_client_namespace


class Devolucion(Evento):
    EVENT_ID_NAME = Evento.ID_EVENT_NAME

    idEventoRetornado = ndb.IntegerProperty(indexed=True)

    @classmethod
    def create(cls, id_client, id_person, initial_time, id_event_to_refund):
        id_event = Generador.get_next_event_id()
        new_refund = Devolucion(key=ndb.Key(Devolucion, id_event),
                                idInterno=id_event,
                                idCliente=id_client,
                                idPersona=id_person,
                                tipo=TipoEvento.EVENT_TYPE_REFUND,
                                tiempoInicio=initial_time,
                                idEventoRetornado=id_event_to_refund)
        new_refund.put()
        return new_refund

    @classmethod
    def update(cls, id_person, id_refund, initial_time, id_event_to_refund):
        refund = Devolucion.get_by_id_for_person(id_refund, id_person, u"Refund")
        refund.tiempoInicio = initial_time
        refund.idEventoRetornado = id_event_to_refund
        refund.put()
        return refund

    def to_dict(self):
        fields_dict = super(Devolucion, self).to_dict()
        fields_dict[self.EVENT_ID_NAME] = self.idEventoRetornado
        return fields_dict

    def get_description(self):
        return u"Refund for {0}".format(self.get_event_to_refund().get_description())

    def get_event_to_refund(self):
        # noinspection PyUnusedLocal
        def get_event_to_refund_on_namespace(id_client):
            return Evento.get_by_id(self.idEventoRetornado)

        return on_client_namespace(self.idCliente, get_event_to_refund_on_namespace)

    @classmethod
    def get_previous_event_devolutions(cls, id_person, id_event_to_return):
        return Devolucion.query(ndb.AND(Devolucion.idEventoRetornado == id_event_to_return,
                                        Devolucion.idPersona == id_person)).fetch()

    def get_number(self):
        return self.get_event_to_refund().get_number()
