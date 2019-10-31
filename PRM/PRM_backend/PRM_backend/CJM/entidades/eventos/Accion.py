# -*- coding: utf-8 -*
from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.TipoEvento import TipoEvento
from google.appengine.ext import ndb

from commons.entidades.Generador import Generador


class Accion(Evento):
    ACTION_NAME_NAME = "name"
    AMOUNT_NAME = "amount"

    nombre = ndb.StringProperty()
    cantidad = ndb.IntegerProperty()

    @classmethod
    def create(cls, id_client, id_person, initial_time, name, amount):
        id_event = Generador.get_next_event_id()
        new_action = Accion(key=ndb.Key(Accion, id_event),
                            idInterno=id_event,
                            idCliente=id_client,
                            idPersona=id_person,
                            tipo=TipoEvento.EVENT_TYPE_ACTION,
                            tiempoInicio=initial_time,
                            nombre=name,
                            cantidad=amount)
        new_action.put()
        return new_action

    @classmethod
    def update(cls, id_person, id_action, initial_time, name, amount):
        action = Accion.get_by_id_for_person(id_action, id_person, u"Action")
        action.tiempoInicio = initial_time
        action.nombre = name
        action.cantidad = amount
        action.put()
        return action

    def to_dict(self):
        fields_dict = super(Accion, self).to_dict()
        fields_dict[self.ACTION_NAME_NAME] = self.nombre
        fields_dict[self.AMOUNT_NAME] = self.cantidad
        return fields_dict

    def get_description(self):
        return self.nombre

    def get_number(self):
        return self.cantidad
