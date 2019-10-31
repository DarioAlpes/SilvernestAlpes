# -*- coding: utf-8 -*
from google.appengine.ext import ndb


class ClientTag(ndb.Model):
    TAG_NAME_NAME = "name"

    nombre = ndb.StringProperty(indexed=True)

    @classmethod
    def create(cls, supported_tag):
        new_tag = ClientTag(
            key=ndb.Key(ClientTag, supported_tag.idInterno),
            nombre=supported_tag.key.id()
        )
        new_tag.put()
        return new_tag

    @classmethod
    def list(cls):
        return cls.query().fetch()
