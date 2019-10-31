# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from CJM.entidades.eventos.Evento import Evento
from CJM.entidades.eventos.TipoEvento import TipoEvento
from CJM.entidades.skus.SKU import SKU
from commons.entidades.Generador import Generador
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.utils import on_client_namespace


class Feedback(Evento):
    TEXT_NAME = "text"
    SCORE_NAME = "score"
    UBICACION_ID_NAME = Ubicacion.ID_UBICACION_NAME
    SKU_ID_NAME = SKU.ID_SKU_NAME

    texto = ndb.StringProperty()
    puntaje = ndb.IntegerProperty()
    idUbicacion = ndb.IntegerProperty()
    idSKU = ndb.IntegerProperty()

    @classmethod
    def create(cls, id_client, id_person, initial_time, text, score, id_location, id_sku):
        id_event = Generador.get_next_event_id()
        new_feedback = Feedback(key=ndb.Key(Feedback, id_event),
                                idInterno=id_event,
                                idCliente=id_client,
                                idPersona=id_person,
                                tipo=TipoEvento.EVENT_TYPE_FEEDBACK,
                                tiempoInicio=initial_time,
                                texto=text,
                                puntaje=score,
                                idUbicacion=id_location,
                                idSKU=id_sku)
        new_feedback.put()
        return new_feedback

    @classmethod
    def update(cls, id_person, id_feedback, initial_time, text, score, id_location, id_sku):
        feedback = Feedback.get_by_id_for_person(id_feedback, id_person, u"Feedback")
        feedback.tiempoInicio = initial_time
        feedback.texto = text
        feedback.puntaje = score
        feedback.idUbicacion = id_location
        feedback.idSKU = id_sku
        feedback.put()
        return feedback

    def to_dict(self):
        fields_dict = super(Feedback, self).to_dict()
        fields_dict[self.TEXT_NAME] = self.texto
        fields_dict[self.SCORE_NAME] = self.puntaje
        fields_dict[self.UBICACION_ID_NAME] = self.idUbicacion
        fields_dict[self.SKU_ID_NAME] = self.idSKU
        return fields_dict

    def get_description(self):
        return self.texto

    def get_ubicacion(self):
        # noinspection PyUnusedLocal
        def get_ubicacion_on_namespace(id_client):
            return Ubicacion.get_by_id(self.idUbicacion)

        return on_client_namespace(self.idCliente, get_ubicacion_on_namespace)

    def get_number(self):
        return self.puntaje
