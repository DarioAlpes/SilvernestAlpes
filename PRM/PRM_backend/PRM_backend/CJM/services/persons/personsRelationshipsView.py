# -*- coding: utf-8 -*
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.persons.PersonRelationship import PersonRelationship
from CJM.services.validations import validate_id_person, \
    validate_person_relationship, RELATIONSHIPS_SYMMETRY, PERSONS_RELATIONSHIP_ALREADY_EXIST_ERROR_CODE, \
    PERSONS_RELATIONSHIP_DOES_NOT_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityAlreadyExists, EntityDoesNotExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client

PERSONS_RELATIONSHIPS_VIEW_NAME = "persons-relationships"
app = Blueprint(PERSONS_RELATIONSHIPS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/relationships/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_person_relationship(id_client, id_person):
    """
    Relaciona la persona
        Parametros esperados:
            relationship: Tipo de relación entre la persona con id id_person y la persona con id body[id-person]
            id-person: Id de la persona destino de la relación
    :param id_client: id del cliente asociado
    :param id_person: id de la persona origen de la relación
    :return: Asociacion creada
    """

    # noinspection PyUnusedLocal
    def create_person_relationship_on_namespace(id_current_client):
        id_origin_person = validate_id_person(id_person)
        id_person_destination = request.json.get(PersonRelationship.PERSON_ID_NAME)
        id_person_destination = validate_id_person(id_person_destination)

        previous_relationships = PersonRelationship.get_by_persons_ids(id_origin_person, id_person_destination)
        if previous_relationships is not None:
            raise EntityAlreadyExists(u"Person relationship",
                                      internal_code=PERSONS_RELATIONSHIP_ALREADY_EXIST_ERROR_CODE)

        relationship = request.json.get(PersonRelationship.RELATIONSHIP_NAME)
        relationship = validate_person_relationship(relationship, PersonRelationship.RELATIONSHIP_NAME)

        origin_relationship, destination_relationship = create_symmetric_relationship_without_put(id_origin_person,
                                                                                                  id_person_destination,
                                                                                                  relationship)
        ndb.put_multi([origin_relationship, destination_relationship])

        return PersonRelationship.list_to_dtos([origin_relationship])[0]

    return on_client_namespace(id_client, create_person_relationship_on_namespace,
                               action=Role.CREATE_ACTION,
                               view=PERSONS_RELATIONSHIPS_VIEW_NAME)


def create_symmetric_relationship_without_put(id_person_origin, id_person_destination, relationship):

    origin_relationship = PersonRelationship.create_without_put(id_person_origin,
                                                                id_person_destination,
                                                                relationship)
    destination_relationship = PersonRelationship.create_without_put(id_person_destination,
                                                                     id_person_origin,
                                                                     RELATIONSHIPS_SYMMETRY[relationship])
    return origin_relationship, destination_relationship


@app.route('/clients/<int:id_client>/persons/<int:id_person>/relationships/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_related_persons(id_client, id_person):
    """
    Da la lista de personas relacionadas a la persona con id dado para el cliente con id dado
    :param id_client: id del cliente a consultar
    :param id_person: id de una de las personas a consultar
    :return: Lista de personas relacionadas a la persona dada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_related_persons_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        relationships = PersonRelationship.list_for_person(id_current_person)
        return PersonRelationship.list_to_dtos(relationships)

    return on_client_namespace(id_client, list_related_persons_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSONS_RELATIONSHIPS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/relationships/<int:id_related_person>/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_related_person_by_id(id_client, id_person, id_related_person):
    """
    Da la información de la persona con id id_related_person y su relación con la persona con id id_person.
    :param id_client: id del cliente a consultar
    :param id_person: de de la persona origen de la relación
    :param id_related_person: id de de la persona origen de la relación
    :return: Persona con el id id_related_person y su relación con la persona con id id_person
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_related_person_by_id_on_namespace(id_current_client):
        id_origin_person = validate_id_person(id_person)
        id_person_destination = validate_id_person(id_related_person)
        relationship = PersonRelationship.get_by_persons_ids(id_origin_person, id_person_destination)
        if relationship is None:
            raise EntityDoesNotExists(u"Person relationship", PERSONS_RELATIONSHIP_DOES_NOT_EXISTS_ERROR_CODE)
        return PersonRelationship.list_to_dtos([relationship])[0]
    return on_client_namespace(id_client, get_related_person_by_id_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSONS_RELATIONSHIPS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/relationships/<int:id_related_person>/',
           methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_related_person_by_id(id_client, id_person, id_related_person):
    """
    Elimina la relacion entre la persona con id id_person y id_related_person
    :param id_client: id del cliente a consultar
    :param id_person: de de la persona origen de la relación
    :param id_related_person: id de de la persona origen de la relación
    :return: Persona con el id id_related_person eliminada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_related_person_by_id_on_namespace(id_current_client):
        id_origin_person = validate_id_person(id_person)
        id_person_destination = validate_id_person(id_related_person)
        relationship = PersonRelationship.get_by_persons_ids(id_origin_person, id_person_destination)
        if relationship is None:
            raise EntityDoesNotExists(u"Person relationship", PERSONS_RELATIONSHIP_DOES_NOT_EXISTS_ERROR_CODE)
        symmetric_relationship = PersonRelationship.get_by_persons_ids(id_person_destination, id_origin_person)
        ndb.delete_multi([relationship.key, symmetric_relationship.key])
        return PersonRelationship.list_to_dtos([relationship])[0]
    return on_client_namespace(id_client, delete_related_person_by_id_on_namespace,
                               action=Role.DELETE_ACTION,
                               view=PERSONS_RELATIONSHIPS_VIEW_NAME)
