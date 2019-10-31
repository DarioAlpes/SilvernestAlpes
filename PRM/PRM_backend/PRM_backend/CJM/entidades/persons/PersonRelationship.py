# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.persons.Persona import Persona
from commons.excepciones.apiexceptions import EntityDoesNotExists
from tests.errorDefinitions.errorConstants import PERSON_DOES_NOT_EXISTS_CODE


class PersonRelationship(ndb.Model):
    PERSON_ID_NAME = Persona.ID_PERSON_NAME
    RELATIONSHIP_NAME = "relationship"

    idPersonaOrigen = ndb.IntegerProperty(indexed=True)
    idPersonaDestino = ndb.IntegerProperty(indexed=True)
    relacion = ndb.StringProperty(indexed=True)

    @classmethod
    def create_without_put(cls, id_origin_person, id_person_destination, relationship):
        return PersonRelationship(idPersonaOrigen=id_origin_person,
                                  idPersonaDestino=id_person_destination,
                                  relacion=relationship)

    @classmethod
    def create(cls, id_origin_person, id_person_destination, relationship):
        new_relationship = cls.create_without_put(id_origin_person, id_person_destination, relationship)
        new_relationship.put()
        return new_relationship

    @classmethod
    def list_for_person(cls, id_person):
        return PersonRelationship.query(PersonRelationship.idPersonaOrigen == id_person)

    @classmethod
    def list_to_dtos(cls, relationships):
        persons_keys = [Persona.get_key_from_id(relationship.idPersonaDestino) for relationship in relationships]
        persons = ndb.get_multi(persons_keys)
        return [_PersonRelationshipDTO(relationship, person) for (relationship, person) in zip(relationships, persons)]

    @classmethod
    def _get_by_persons_ids_without_get(cls, id_origin, id_destination):
        return cls.query(ndb.AND(cls.idPersonaOrigen == id_origin,
                                 cls.idPersonaDestino == id_destination))

    @classmethod
    def get_by_persons_ids_async(cls, id_origin, id_destination):
        return cls._get_by_persons_ids_without_get(id_origin, id_destination).get_async()

    @classmethod
    def get_by_persons_ids(cls, id_origin, id_destination):
        return cls._get_by_persons_ids_without_get(id_origin, id_destination).get()


class _PersonRelationshipDTO:
    def __init__(self, relationship, person):
        if person is None:
            raise EntityDoesNotExists(u"Person", PERSON_DOES_NOT_EXISTS_CODE)
        self.relationship = relationship
        self.person = person

    def to_dict(self):
        fields_dict = self.person.to_dict()
        fields_dict[PersonRelationship.RELATIONSHIP_NAME] = self.relationship.relacion
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
