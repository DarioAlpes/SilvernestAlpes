# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.tags.SupportedTag import SupportedTag
from CJM.services.validations import validate_amount, SUPPORTED_TAG_DOES_NOT_EXISTS_ERROR_CODE, \
    SUPPORTED_TAGS_INVALID_NAME_ERROR_CODE, SUPPORTED_TAGS_INVALID_TOTAL_SIZE_ERROR_CODE, \
    SUPPORTED_TAGS_ALREADY_EXISTS_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityAlreadyExists, EntityDoesNotExists
from commons.utils import with_json_body, with_json_bodyless, validate_user_logged_in_is_global_user
from commons.validations import validate_string_not_empty


SUPPORTED_TAGS_VIEW_NAME = "supported-tags"
app = Blueprint(SUPPORTED_TAGS_VIEW_NAME, __name__)


@app.route('/supported-tags/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_supported_tag():
    """
    Crea un tag soportado en el namespace global
        Parametros esperados:
            name: Nombre del tag
            total-size: Tamaño del tag
    :return: tag creado
    """
    validate_user_logged_in_is_global_user(SUPPORTED_TAGS_VIEW_NAME, Role.READ_ACTION)
    name = request.json.get(SupportedTag.TAG_NAME_NAME)
    name = validate_string_not_empty(name, SupportedTag.TAG_NAME_NAME,
                                     internal_code=SUPPORTED_TAGS_INVALID_NAME_ERROR_CODE)

    size = request.json.get(SupportedTag.TOTAL_SIZE_NAME)
    size = validate_amount(size, SupportedTag.TOTAL_SIZE_NAME,
                           internal_code=SUPPORTED_TAGS_INVALID_TOTAL_SIZE_ERROR_CODE)

    SupportedTag.validate_size(size)

    if SupportedTag.get_by_id(name) is not None:
        raise EntityAlreadyExists(u"Supported Tag[{0}]".format(name),
                                  internal_code=SUPPORTED_TAGS_ALREADY_EXISTS_ERROR_CODE)

    return SupportedTag.create(name, size)


@app.route('/supported-tags/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_all_supported_tags():
    """
    Da la lista de tags soportados
    :return: Lista de tags soportados
    """
    validate_user_logged_in_is_global_user(SUPPORTED_TAGS_VIEW_NAME, Role.READ_ACTION)
    return SupportedTag.list()


@app.route('/supported-tags/<int:id_tag>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_supported_tag(id_tag):
    """
    Da el tag soportado con id dado
    :param id_tag: id del tag a recuperar
    :return: tag soportado con id dado
    """
    validate_user_logged_in_is_global_user(SUPPORTED_TAGS_VIEW_NAME, Role.READ_ACTION)
    return get_supported_tag_by_id(id_tag)


@app.route('/supported-tags/<int:id_tag>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_supported_tag(id_tag):
    """
    Elimina el tag soportado con id dado
    :param id_tag: id del tag a recuperar
    :return: tag soportado eliminado
    """
    validate_user_logged_in_is_global_user(SUPPORTED_TAGS_VIEW_NAME, Role.READ_ACTION)
    tag = get_supported_tag_by_id(id_tag)
    tag.key.delete()
    return tag


def get_supported_tag_by_id(id_tag):
    try:
        tag = SupportedTag.get_by_internal_id(id_tag)
    except ValueError:
        tag = None
    if tag is None:
        raise EntityDoesNotExists(u"Supported tag[{0}]".format(id_tag),
                                  internal_code=SUPPORTED_TAG_DOES_NOT_EXISTS_ERROR_CODE)
    return tag


def get_supported_tag_by_name(tag_name):
    try:
        tag = SupportedTag.get_by_id(tag_name)
    except ValueError:
        tag = None
    if tag is None:
        raise EntityDoesNotExists(u"Supported tag[{0}]".format(tag_name),
                                  internal_code=SUPPORTED_TAG_DOES_NOT_EXISTS_ERROR_CODE)
    return tag
