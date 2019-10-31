# -*- coding: utf-8 -*
from flask.helpers import make_response
from google.appengine.api.blobstore.blobstore import BlobKey
from werkzeug.http import parse_options_header

from commons.entidades.locations.Ubicacion import Ubicacion
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists, EntityAlreadyExists
from commons.services.locations.ubicacionView import LOCATIONS_VIEW_NAME
from commons.utils import on_client_namespace, with_json_bodyless
from flask import request, Blueprint
from google.appengine.ext import blobstore

from commons.validations import validate_id_client, validate_id_location, LOCATION_IMAGE_DOES_NOT_EXISTS_ERROR_CODE, \
    LOCATION_IMAGE_ALREADY_EXISTS_ERROR_CODE

app = Blueprint("location-images", __name__)

URL_NAME = "url"
LOCATION_IMAGE_FILE_NAME = "file"


@app.route('/clients/<int:id_client>/locations/<int:id_location>/image-url/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_image_upload_url(id_client, id_location):
    """
    Da la url donde se debe hacer post de la imagen de la ubicación.
    :param id_client: Id del cliente asociado.
    :param id_location: Id de la ubicación asociada.
    :return: url donde se debe hacer post de la imagen de la ubicación.
    """
    id_client = validate_id_client(id_client)

    def get_image_upload_url_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        upload_url = blobstore.create_upload_url(u'/clients/{0}/locations/{1}/image/'
                                                 .format(id_current_client, id_current_location))
        return {URL_NAME: upload_url}

    return on_client_namespace(id_client, get_image_upload_url_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=LOCATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/image/', methods=['DELETE'],
           strict_slashes=False)
@with_json_bodyless
def delete_location_image(id_client, id_location):
    """
    Elimina la imagen con id dado, asociada a la ubicación dada en el namespace del cliente dado.
    :param id_client: Id del cliente asociado.
    :param id_location: Id de la ubicación asociada.
    :return: Ubicación con la imagen eliminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_location_image_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        location = Ubicacion.get_by_id(id_current_location)
        if location.blobKey is None:
            raise EntityDoesNotExists(u"Location Image[{0}]".format(id_current_location),
                                      internal_code=LOCATION_IMAGE_DOES_NOT_EXISTS_ERROR_CODE)

        blobstore.delete(location.blobKey)
        del location.blobKey
        location.put()
        return location

    return on_client_namespace(id_client, delete_location_image_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=LOCATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/image/', methods=['GET'],
           strict_slashes=False)
def get_location_image(id_client, id_location):
    """
    Da la imagen de la ubicación con id dado en el namespace del cliente dado.
    :param id_client: Id del cliente asociado.
    :param id_location: Id de la ubicación asociada.
    :return: imagen con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_location_image_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        location = Ubicacion.get_by_id(id_current_location)
        if location.blobKey is None:
            raise EntityDoesNotExists(u"Location Image[{0}]".format(id_current_location),
                                      internal_code=LOCATION_IMAGE_DOES_NOT_EXISTS_ERROR_CODE)

        blob_info = blobstore.get(location.blobKey)
        response = make_response(blob_info.open().read())
        response.headers['Content-Type'] = blob_info.content_type
        return response

    return on_client_namespace(id_client, get_location_image_on_namespace,
                               action=Role.READ_ACTION,
                               view=LOCATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/image/', methods=['POST'], strict_slashes=False)
def upload_location_image(id_client, id_location):
    """
    Guarda la imagen dada y la asocia a la ubicación dada.
    :param id_client: Id del cliente asociado.
    :param id_location: Id de la ubicación asociada.
    :return: datos de la imagen guardada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def upload_location_image_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        location = Ubicacion.get_by_id(id_current_location)
        if location.blobKey is not None:
            raise EntityAlreadyExists(u"Location Image[{0}]".format(id_current_location),
                                      internal_code=LOCATION_IMAGE_ALREADY_EXISTS_ERROR_CODE)

        f = request.files[LOCATION_IMAGE_FILE_NAME]
        header = f.headers['Content-Type']
        parsed_header = parse_options_header(header)
        blob_key = parsed_header[1]['blob-key']

        location.blobKey = BlobKey(blob_key)
        location.put()
        response = make_response(location.to_json())
        if request.environ['HTTP_ORIGIN'] is not None:
            from main import matches_allowed_origin
            origin = request.environ['HTTP_ORIGIN']
            if matches_allowed_origin(origin):
                response.headers.add_header("Access-Control-Allow-Origin", origin)
                response.headers.add_header("Access-Control-Allow-Credentials", "true")

        return response

    return on_client_namespace(id_client, upload_location_image_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=LOCATIONS_VIEW_NAME)
