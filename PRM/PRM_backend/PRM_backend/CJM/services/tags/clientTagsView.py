# -*- coding: utf-8 -*
from flask import request, Blueprint
from google.appengine.ext import ndb

from CJM.entidades.tags.ClientTag import ClientTag
from CJM.entidades.tags.SupportedTag import SupportedTag
from CJM.services.tags.supportedTagsView import get_supported_tag_by_name, get_supported_tag_by_id
from CJM.services.validations import CLIENT_TAG_DOES_NOT_EXISTS_ERROR_CODE, CLIENT_TAGS_INVALID_NAME_ERROR_CODE, \
    CLIENT_TAGS_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists, EntityAlreadyExists
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_string_not_empty

CLIENT_TAGS_VIEW_NAME = "client-tags"
app = Blueprint(CLIENT_TAGS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/tags/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_tag(id_client):
    """
    Crea el tag con el nombre dado
        Parametros esperados:
            name: str, debe ser uno de los nombre de los tags soportados
    :param id_client: id del cliente asociado
    :return: Tag soportado asociado al cliente dado
    """
    id_client = validate_id_client(id_client)

    name = request.json.get(ClientTag.TAG_NAME_NAME)
    name = validate_string_not_empty(name, ClientTag.TAG_NAME_NAME,
                                     internal_code=CLIENT_TAGS_INVALID_NAME_ERROR_CODE)
    supported_tag = get_supported_tag_by_name(name)

    # noinspection PyUnusedLocal
    def create_tag_on_namespace(id_current_client):

        if ClientTag.get_by_id(supported_tag.idInterno) is not None:
            raise EntityAlreadyExists(u"Client Tag[{0}]".format(name),
                                      internal_code=CLIENT_TAGS_ALREADY_EXISTS_ERROR_CODE)
        ClientTag.create(supported_tag)
        return supported_tag

    return on_client_namespace(id_client, create_tag_on_namespace,
                               action=Role.CREATE_ACTION,
                               view=CLIENT_TAGS_VIEW_NAME)


@app.route('/clients/<int:id_client>/tags/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_tags(id_client):
    """
    Da la lista de tags del cliente con id dado
    :param id_client: id del cliente a consultar
    :return: Lista de tags del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_tags_on_namespace(id_current_client):
        return ClientTag.list()

    client_tags = on_client_namespace(id_client, list_tags_on_namespace,
                                      action=Role.READ_ACTION,
                                      view=CLIENT_TAGS_VIEW_NAME)
    return ndb.get_multi([ndb.Key(SupportedTag, client_tag.nombre) for client_tag in client_tags])


@app.route('/clients/<int:id_client>/tags/<int:id_tag>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_tag_by_id(id_client, id_tag):
    """
    Da el tag con id dado
    :param id_client: id del cliente a consultar
    :param id_tag: id del tag a consultar
    :return: Tag con id dado del cliente con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_tag_by_id_on_namespace(id_current_client):
        client_tag = ClientTag.get_by_id(id_tag)
        return get_client_tag_by_id(id_tag)

    tag = on_client_namespace(id_client, get_tag_by_id_on_namespace,
                              action=Role.READ_ACTION,
                              view=CLIENT_TAGS_VIEW_NAME)
    return get_supported_tag_by_id(tag.key.id())


@app.route('/clients/<int:id_client>/tags/<int:id_tag>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_tag_by_id(id_client, id_tag):
    """
    Elimina el tag con id dado
    :param id_client: id del cliente a consultar
    :param id_tag: id del tag a eliminar
    :return: Tag eliminado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def delete_tag_by_id_on_namespace(id_current_client):
        client_tag = get_client_tag_by_id(id_tag)
        client_tag.key.delete()
        return client_tag

    tag = on_client_namespace(id_client, delete_tag_by_id_on_namespace,
                              action=Role.DELETE_ACTION,
                              view=CLIENT_TAGS_VIEW_NAME)
    return get_supported_tag_by_id(tag.key.id())


def get_client_tag_by_id(id_tag):
    client_tag = ClientTag.get_by_id(id_tag)
    if client_tag is None:
        raise EntityDoesNotExists(u"Client Tag[{0}]".format(id_tag),
                                  internal_code=CLIENT_TAG_DOES_NOT_EXISTS_ERROR_CODE)
    else:
        return client_tag
