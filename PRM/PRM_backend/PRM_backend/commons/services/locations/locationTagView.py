# -*- coding: utf-8 -*
from commons.entidades.locations.LocationTag import LocationTag
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_bodyless, with_json_body
from commons.utils import on_client_namespace
from commons.validations import validate_id_client, validate_string_not_empty, LOCATION_TAG_INVALID_NAME_ERROR_CODE, \
    LOCATION_TAG_ALREADY_EXISTS_ERROR_CODE
from flask import request, Blueprint
LOCATION_TAGS_VIEW_NAME = "location-tags"

app = Blueprint(LOCATION_TAGS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/location-tags/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_location_tag(id_client):
    """
    Crea un tag de ubicación en el namespace del cliente id_client
        Parametros esperados:
            name: str
    :param id_client: id del cliente asociado
    :return: Moneda creada
    """
    id_client = validate_id_client(id_client)

    def create_location_tag_on_namespace(id_current_client):
        name = request.json.get(LocationTag.TAG_NAME_NAME)
        name = validate_string_not_empty(name, LocationTag.TAG_NAME_NAME,
                                         internal_code=LOCATION_TAG_INVALID_NAME_ERROR_CODE)

        if LocationTag.get_by_id(name) is not None:
            raise ValidationError(u"The tag {0} already exists.".format(name),
                                  internal_code=LOCATION_TAG_ALREADY_EXISTS_ERROR_CODE)
        return LocationTag.create(id_current_client, name)

    return on_client_namespace(id_client, create_location_tag_on_namespace,
                               action=Role.CREATE_ACTION,
                               view=LOCATION_TAGS_VIEW_NAME)


@app.route('/clients/<int:id_client>/location-tags/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_location_tags(id_client):
    """
    Da la lista de tags de ubicaciones existentes para el cliente con id dado
    :param id_client: id del cliente a consultar
    :return: Lista de tags de ubicaciones existentes para el cliente con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_location_tags_on_namespace(id_current_client):
        return LocationTag.list()

    return on_client_namespace(id_client, list_location_tags_on_namespace,
                               action=Role.READ_ACTION,
                               view=LOCATION_TAGS_VIEW_NAME)
