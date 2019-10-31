# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from commons.entidades.Cliente import Cliente


class LocationTag(ndb.Model):

    TAG_NAME_NAME = "name"
    CLIENT_ID_NAME = Cliente.ID_CLIENT_NAME
    idCliente = ndb.IntegerProperty()

    def to_dict(self):
        fields_dict = dict()
        fields_dict[LocationTag.CLIENT_ID_NAME] = self.idCliente
        fields_dict[LocationTag.TAG_NAME_NAME] = self.key.id()
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create(cls, id_client, name):
        new_tag = LocationTag(
            key=ndb.Key(LocationTag, name),
            idCliente=id_client
        )
        new_tag.put()
        return new_tag

    @classmethod
    def list(cls):
        return LocationTag.query().fetch()
