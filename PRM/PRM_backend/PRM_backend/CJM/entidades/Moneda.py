# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from commons.entidades.Cliente import Cliente
from commons.entidades.Generador import Generador


class Moneda(ndb.Model):
    _COP_NAME = u"COP"
    DEFAULT_CURRENCY_NAME = _COP_NAME
    ID_CURRENCY_NAME = "id-currency"

    MONEY_NAME_NAME = "name"
    ID_NAME = "id"
    CLIENT_ID_NAME = Cliente.ID_CLIENT_NAME

    idCliente = ndb.IntegerProperty()
    idInterno = ndb.IntegerProperty(indexed=True)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[Moneda.MONEY_NAME_NAME] = self.key.id()
        fields_dict[Moneda.CLIENT_ID_NAME] = self.idCliente
        fields_dict[Moneda.ID_NAME] = self.idInterno
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create(cls, id_client, name):
        new_currency = cls.create_currency_without_put(id_client, name)
        new_currency.put()
        return new_currency

    @classmethod
    def create_currency_without_put(cls, id_client, name):
        internal_id = Generador.get_next_currency_id()
        new_currency = Moneda(
            key=ndb.Key(Moneda, name),
            idCliente=id_client,
            idInterno=internal_id
        )
        return new_currency

    @classmethod
    def list(cls):
        return Moneda.query().fetch()

    @classmethod
    def create_default_currency_without_put(cls, id_client):
        return cls.create_currency_without_put(id_client, cls.DEFAULT_CURRENCY_NAME)

    @classmethod
    def get_by_internal_id(cls, id_currency):
        try:
            return Moneda.query(Moneda.idInterno == id_currency).get()
        except ValueError:
            return None
