# -*- coding: utf-8 -*
from flask.helpers import make_response
from google.appengine.api.blobstore.blobstore import BlobKey
from werkzeug.http import parse_options_header

from CJM.entidades.skus.SKU import SKU
from CJM.services.skus.skuView import SKUS_VIEW_NAME
from CJM.services.validations import validate_id_sku, SKU_IMAGE_DOES_NOT_EXISTS_ERROR_CODE, \
    SKU_IMAGE_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityAlreadyExists, EntityDoesNotExists
from commons.utils import on_client_namespace, with_json_bodyless
from flask import request, Blueprint
from google.appengine.ext import blobstore

from commons.validations import validate_id_client

app = Blueprint("sku-images", __name__)

URL_NAME = "url"
SKU_IMAGE_FILE_NAME = "file"


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/image-url/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_image_upload_url(id_client, id_sku):
    """
    Da la url donde se debe hacer post de la imagen del sku.
    :param id_client: Id del cliente asociado.
    :param id_sku: Id del sku asociado.
    :return: url donde se debe hacer post de la imagen del sku.
    """
    id_client = validate_id_client(id_client)

    def get_image_upload_url_on_namespace(id_current_client):
        id_current_sku = validate_id_sku(id_sku)
        upload_url = blobstore.create_upload_url(u'/clients/{0}/skus/{1}/image/'
                                                 .format(id_current_client, id_current_sku))
        return {URL_NAME: upload_url}

    return on_client_namespace(id_client, get_image_upload_url_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=SKUS_VIEW_NAME)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/image/', methods=['DELETE'],
           strict_slashes=False)
@with_json_bodyless
def delete_sku_image(id_client, id_sku):
    """
    Elimina la imagen con id dado, asociada al sku dado en el namespace del cliente dado.
    :param id_client: Id del cliente asociado.
    :param id_sku: Id del sku asociado.
    :return: sku con la imagen eliminada.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_sku_image_on_namespace(id_current_client):
        id_current_sku = validate_id_sku(id_sku)
        sku = SKU.get_by_id(id_sku)
        if sku.blobKey is None:
            raise EntityDoesNotExists(u"SKU Image[{0}]".format(id_current_sku),
                                      internal_code=SKU_IMAGE_DOES_NOT_EXISTS_ERROR_CODE)

        blobstore.delete(sku.blobKey)
        del sku.blobKey
        sku.put()
        return sku

    return on_client_namespace(id_client, delete_sku_image_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=SKUS_VIEW_NAME)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/image/', methods=['GET'],
           strict_slashes=False)
def get_sku_image(id_client, id_sku):
    """
    Da la imagen del sku con id dado en el namespace del cliente dado.
    :param id_client: Id del cliente asociado.
    :param id_sku: Id del sku asociado.
    :return: imagen con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_sku_image_on_namespace(id_current_client):
        id_current_sku = validate_id_sku(id_sku)
        sku = SKU.get_by_id(id_sku)
        if sku.blobKey is None:
            raise EntityDoesNotExists(u"SKU Image[{0}]".format(id_current_sku),
                                      internal_code=SKU_IMAGE_DOES_NOT_EXISTS_ERROR_CODE)

        blob_info = blobstore.get(sku.blobKey)
        response = make_response(blob_info.open().read())
        response.headers['Content-Type'] = blob_info.content_type
        return response

    return on_client_namespace(id_client, get_sku_image_on_namespace,
                               action=Role.READ_ACTION,
                               view=SKUS_VIEW_NAME)


@app.route('/clients/<int:id_client>/skus/<int:id_sku>/image/', methods=['POST'], strict_slashes=False)
def upload_sku_image(id_client, id_sku):
    """
    Guarda la imagen dada y la asocia al sku dado.
    :param id_client: Id del cliente asociado.
    :param id_sku: Id del sku asociado.
    :return: datos de la imagen guardada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def upload_sku_image_on_namespace(id_current_client):
        id_current_sku = validate_id_sku(id_sku)
        sku = SKU.get_by_id(id_sku)
        if sku.blobKey is not None:
            raise EntityAlreadyExists(u"SKU Image[{0}]".format(id_current_sku),
                                      internal_code=SKU_IMAGE_ALREADY_EXISTS_ERROR_CODE)

        f = request.files[SKU_IMAGE_FILE_NAME]
        header = f.headers['Content-Type']
        parsed_header = parse_options_header(header)
        blob_key = parsed_header[1]['blob-key']

        sku.blobKey = BlobKey(blob_key)
        sku.put()
        response = make_response(sku.to_json())
        if request.environ['HTTP_ORIGIN'] is not None:
            from main import matches_allowed_origin
            origin = request.environ['HTTP_ORIGIN']
            if matches_allowed_origin(origin):
                response.headers.add_header("Access-Control-Allow-Origin", origin)
                response.headers.add_header("Access-Control-Allow-Credentials", "true")
        return response

    return on_client_namespace(id_client, upload_sku_image_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=SKUS_VIEW_NAME)
