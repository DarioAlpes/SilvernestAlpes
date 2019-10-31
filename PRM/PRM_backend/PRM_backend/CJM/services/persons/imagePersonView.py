# -*- coding: utf-8 -*
from flask import request, Blueprint
from google.appengine.api.blobstore.blobstore import BlobKey
from google.appengine.ext import blobstore

from CJM.entidades.persons.Persona import Persona
from CJM.services.persons.personaView import PERSONS_VIEW_NAME
from CJM.services.validations import validate_id_person, PERSON_IMAGE_DOES_NOT_EXISTS_ERROR_CODE, \
    PERSON_IMAGE_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityAlreadyExists, EntityDoesNotExists
from commons.utils import on_client_namespace, with_json_bodyless
from commons.validations import validate_id_client
from flask.helpers import make_response
from werkzeug.http import parse_options_header

app = Blueprint("person-images", __name__)

URL_NAME = "url"
PERSON_IMAGE_FILE_NAME = "file"


@app.route('/clients/<int:id_client>/persons/<int:id_person>/image-url/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_image_upload_url(id_client, id_person):
    """
    Da la url donde se debe hacer post de la imagen de la persona.
    :param id_client: Id del cliente asociado.
    :param id_person: Id de la persona asociada.
    :return: url donde se debe hacer post de la imagen de la persona.
    """
    id_client = validate_id_client(id_client)

    def get_image_upload_url_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        upload_url = blobstore.create_upload_url(u'/clients/{0}/persons/{1}/image/'
                                                 .format(id_current_client, id_current_person))
        return {URL_NAME: upload_url}

    return on_client_namespace(id_client, get_image_upload_url_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=PERSONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/image/', methods=['DELETE'],
           strict_slashes=False)
@with_json_bodyless
def delete_person_image(id_client, id_person):
    """
    Elimina la imagen con id dado, asociada a la persona dada en el namespace del cliente dado.
    :param id_client: Id del cliente asociado.
    :param id_person: Id de la persona asociado.
    :return: persona con la imagen eliminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_person_image_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        person = Persona.get_by_id(id_current_person)
        if person.blobKey is None:
            raise EntityDoesNotExists(u"Person Image[{0}]".format(id_current_person),
                                      internal_code=PERSON_IMAGE_DOES_NOT_EXISTS_ERROR_CODE)

        blobstore.delete(person.blobKey)
        del person.blobKey
        person.put()
        return person

    return on_client_namespace(id_client, delete_person_image_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=PERSONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/image/', methods=['GET'],
           strict_slashes=False)
def get_person_image(id_client, id_person):
    """
    Da la imagen de la persona con id dado en el namespace del cliente dado.
    :param id_client: Id del cliente asociado.
    :param id_person: Id de la persona asociado.
    :return: imagen con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_person_image_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        person = Persona.get_by_id(id_current_person)
        if person.blobKey is None:
            raise EntityDoesNotExists(u"Person Image[{0}]".format(id_current_person),
                                      internal_code=PERSON_IMAGE_DOES_NOT_EXISTS_ERROR_CODE)

        blob_info = blobstore.get(person.blobKey)
        response = make_response(blob_info.open().read())
        response.headers['Content-Type'] = blob_info.content_type
        return response

    return on_client_namespace(id_client, get_person_image_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/image/', methods=['POST'], strict_slashes=False)
def upload_person_image(id_client, id_person):
    """
    Guarda la imagen dada y la asocia a la persona dada.
    :param id_client: Id del cliente asociado.
    :param id_person: Id de la persona asociada.
    :return: datos de la imagen guardada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def upload_person_image_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        person = Persona.get_by_id(id_current_person)
        if person.blobKey is not None:
            raise EntityAlreadyExists(u"Person Image[{0}]".format(id_current_person),
                                      internal_code=PERSON_IMAGE_ALREADY_EXISTS_ERROR_CODE)

        f = request.files[PERSON_IMAGE_FILE_NAME]
        header = f.headers['Content-Type']
        parsed_header = parse_options_header(header)
        blob_key = parsed_header[1]['blob-key']

        person.blobKey = BlobKey(blob_key)
        person.put()
        response = make_response(person.to_json())
        if request.environ['HTTP_ORIGIN'] is not None:
            from main import matches_allowed_origin
            origin = request.environ['HTTP_ORIGIN']
            if matches_allowed_origin(origin):
                response.headers.add_header("Access-Control-Allow-Origin", origin)
                response.headers.add_header("Access-Control-Allow-Credentials", "true")

        return response

    return on_client_namespace(id_client, upload_person_image_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=PERSONS_VIEW_NAME)
