# -*- coding: utf-8 -*
from flask import request, Blueprint

from commons.entidades.locations.Ubicacion import Ubicacion, TipoUbicacion
from commons.entidades.optionalities.FieldOptionality import FieldOptionality
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import with_json_bodyless, with_json_body, on_client_namespace
from commons.validations import validate_id_client, validate_location_type, validate_string_not_empty, \
    validate_id_location, validate_bool_not_empty, validate_latitude, validate_longitude, validate_web_url, \
    validate_phone, validate_address, validate_mail, validate_id_location_tags_list, validate_location_subtype, \
    validate_by_optionality_and_function, LOCATION_INVALID_NAME_ERROR_CODE, LOCATION_INVALID_ACTIVE_ERROR_CODE, \
    LOCATION_INVALID_DESCRIPTION_ERROR_CODE, LOCATION_INVALID_WEB_ERROR_CODE, LOCATION_INVALID_PHONE_ERROR_CODE, \
    LOCATION_INVALID_ADDRESS_ERROR_CODE, LOCATION_INVALID_MAIL_ERROR_CODE, LOCATION_INVALID_LATITUDE_ERROR_CODE, \
    LOCATION_INVALID_LONGITUDE_ERROR_CODE

LOCATIONS_VIEW_NAME = "locations"

app = Blueprint(LOCATIONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/locations/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_location(id_client):
    """
    Crea una ubicación en el namespace del cliente id_client
        Parametros esperados:
            name: str
            type: str
            id-parent-location: int opcional
            description: str opcinal. Descripción de la ubicación
            active: bool opcional, se supone True si se omite. Indica si la ubicación esta activa.
            latitude: float opcional (Si se envía longitude es obligatorio)
            longitude: float opcional (Si se envía latitude es obligatorio)
            web: str opcional, solo se lee para tipos de ubicación COUNTRY, REGION, CITY, POI, PROPERTY. Para cualquier
            otro tipo de ubicación se ignora
            phone: str opcional, solo se lee para tipos de ubicación POI, PROPERTY, ZONE, AREA. Para
            cualquier otro tipo de ubicación se ignora
            address: str opcional, solo se lee para tipos de ubicación POI, PROPERTY, ZONE, AREA. Para
            cualquier otro tipo de ubicación se ignora
            mail: str opcional, solo se lee para tipos de ubicación POI, PROPERTY, ZONE, AREA. Para
            cualquier otro tipo de ubicación se ignora
            tags: arreglo de str opcional (deben ser crearse los tags previamente en el servicio
            /clients/<int:id_client>/location-tags/), solo se lee para tipos de ubicación POI. Para cualquier
            otro tipo de ubicación se ignora
            subtype: str obligatorio para ubicaciones de tipo TOUCHPOINT. Para cualquier
            otro tipo de ubicación se ignora
    :param id_client: id del cliente asociado
    :return: Ubicación creada
    """
    id_client = validate_id_client(id_client)

    def create_location_on_namespace(id_current_client, name, location_type, id_parent, description, active,
                                     latitude, longitude, web, phone, address, mail, tags, subtype):
        return Ubicacion.create(id_current_client, name, location_type, id_parent, description, active, latitude,
                                longitude, web, phone, address, mail, tags, subtype)

    return _get_and_validate_json_parameters(id_client, create_location_on_namespace, action=Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/locations/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_locations(id_client):
    """
    Da la lista de ubicaciones del cliente con id dado filtrandolas segun el tipo de ubicación dado.
        Parametros esperados en el query string:
            type: str opcional
    :param id_client: Id del cliente asociado.
    :return: Lista de ubicaciones del cliente dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_locations_on_namespace(id_current_client):
        location_type = request.args.get(Ubicacion.TYPE_NAME)
        if location_type is not None:
            location_type = validate_location_type(location_type, Ubicacion.TYPE_NAME)
            return Ubicacion.list_by_type(location_type)
        else:
            return Ubicacion.list()

    return on_client_namespace(id_client, list_locations_on_namespace,
                               action=Role.READ_ACTION,
                               view=LOCATIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/active-locations/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_active_locations(id_client):
    """
    Da la lista de ubicaciones activas del cliente con id dado.
    :param id_client: Id del cliente asociado.
    :return: Lista de ubicaciones activas del cliente dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_active_locations_on_namespace(id_current_client):
        return Ubicacion.list_active_locations()

    return on_client_namespace(id_client, list_active_locations_on_namespace,
                               action=Role.READ_ACTION,
                               view=LOCATIONS_VIEW_NAME)


@app.route('/location-types/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_location_types():
    return TipoUbicacion.list()


@app.route('/clients/<int:id_client>/locations/<int:id_location>/children/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_locations_by_parent_id(id_client, id_location):
    """
    Da la lista de ubicaciones hijo de la ubicación con id dado para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_location: id de la ubicación asociada.
    :return: Lista de ubicaciones hijo de la ubicación con id dado para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_locations_by_parent_id_on_namespace(id_current_client):
        id_parent_location = validate_id_location(id_location)
        return Ubicacion.list_by_parent_id(id_parent_location)

    return on_client_namespace(id_client, list_locations_by_parent_id_on_namespace,
                               action=Role.READ_ACTION,
                               view=LOCATIONS_VIEW_NAME,
                               id_location=id_location)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_location(id_client, id_location):
    """
    Da la ubicación con id dado para el cliente con id dado.
    :param id_client: id del cliente asociado.
    :param id_location: id de la ubicación buscada.
    :return: ubicación con id dado para el cliente con id dado.
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_location_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        return Ubicacion.get_by_id(id_current_location)

    return on_client_namespace(id_client, get_location_on_namespace,
                               action=Role.READ_ACTION,
                               view=LOCATIONS_VIEW_NAME,
                               id_location=id_location)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/', methods=['PUT'], strict_slashes=False)
@with_json_body
def update_location(id_client, id_location):
    """
    Actualiza la ubicación con id dado con los parámetros datos
        Parametros esperados:
            name: str
            type: str
            id-parent-location: int opcional
            description: str opcinal. Descripción de la ubicación
            active: bool opcional, se supone True si se omite. Indica si la ubicación esta activa.
            latitude: float opcional (Si se envía longitude es obligatorio)
            longitude: float opcional (Si se envía latitude es obligatorio)
            web: str opcional, solo se lee para tipos de ubicación COUNTRY, REGION, CITY, POI, PROPERTY. Para cualquier
            otro tipo de ubicación se ignora
            phone: str opcional, solo se lee para tipos de ubicación POI, PROPERTY, ZONE, AREA. Para
            cualquier otro tipo de ubicación se ignora
            address: str opcional, solo se lee para tipos de ubicación POI, PROPERTY, ZONE, AREA. Para
            cualquier otro tipo de ubicación se ignora
            mail: str opcional, solo se lee para tipos de ubicación POI, PROPERTY, ZONE, AREA. Para
            cualquier otro tipo de ubicación se ignora
            tags: arreglo de str opcional (deben ser crearse los tags previamente en el servicio
            /clients/<int:id_client>/location-tags/), solo se lee para tipos de ubicación POI. Para cualquier
            otro tipo de ubicación se ignora
            subtype: str obligatorio para ubicaciones de tipo TOUCHPOINT. Para cualquier
            otro tipo de ubicación se ignora
    :param id_client: id del cliente asociado.
    :param id_location: id de la ubicación buscada.
    :return: ubicación actualizada
    """
    id_client = validate_id_client(id_client)

    def update_location_on_namespace(_, name, location_type, id_parent, description, active,
                                     latitude, longitude, web, phone, address, mail, tags, subtype):
        id_current_location = validate_id_location(id_location)
        location_to_udpate = Ubicacion.get_by_id(id_current_location)
        location_to_udpate.update(name, location_type, id_parent, description, active,
                                  latitude, longitude, web, phone, address, mail, tags, subtype)
        return location_to_udpate

    return _get_and_validate_json_parameters(id_client, update_location_on_namespace, action=Role.UPDATE_ACTION,
                                             id_location=id_location)


@app.route('/clients/<int:id_client>/locations/<int:id_location>/', methods=['PATCH'], strict_slashes=False)
@with_json_body
def activate_location(id_client, id_location):
    """
    Activa o desactiva la ubicación con id dado.
        Parametros esperados:
            active: bool. True si se quiere activar el paquete, false si no
    :param id_client: id del cliente asociado
    :param id_location: id del paquete asociado
    :return: Ubicación modificada
    """
    id_client = validate_id_client(id_client)

    active = request.json.get(Ubicacion.ACTIVE_NAME)
    active = validate_bool_not_empty(active, Ubicacion.ACTIVE_NAME, internal_code=LOCATION_INVALID_ACTIVE_ERROR_CODE)

    # noinspection PyUnusedLocal
    def activate_location_on_namespace(id_current_client):
        id_current_location = validate_id_location(id_location)
        location = Ubicacion.get_by_id(id_current_location)
        location.activo = active
        location.put()
        return location
    return on_client_namespace(id_client, activate_location_on_namespace,
                               action=Role.UPDATE_ACTION,
                               view=LOCATIONS_VIEW_NAME,
                               id_location=id_location)


def _get_and_validate_json_parameters(id_client, on_namespace_callaback, action, id_location=None):
    def _get_and_validate_json_parameters_on_namespace(id_current_client):
        location_type = request.json.get(Ubicacion.TYPE_NAME)
        location_type = validate_location_type(location_type, Ubicacion.TYPE_NAME)

        name = request.json.get(Ubicacion.LOCATION_NAME_NAME)
        name = validate_string_not_empty(name, Ubicacion.LOCATION_NAME_NAME,
                                         internal_code=LOCATION_INVALID_NAME_ERROR_CODE)

        active = request.json.get(Ubicacion.ACTIVE_NAME, True)
        active = validate_bool_not_empty(active, Ubicacion.ACTIVE_NAME, internal_code=LOCATION_INVALID_ACTIVE_ERROR_CODE)

        fields_optionalities = FieldOptionality.list_by_view_as_dict(LOCATIONS_VIEW_NAME)

        description = request.json.get(Ubicacion.DESCRIPTION_NAME)
        description = validate_by_optionality_and_function(fields_optionalities, description, location_type,
                                                           Ubicacion.DESCRIPTION_NAME, validate_string_not_empty,
                                                           internal_code=LOCATION_INVALID_DESCRIPTION_ERROR_CODE)

        web = request.json.get(Ubicacion.WEB_URL_NAME)
        web = validate_by_optionality_and_function(fields_optionalities, web, location_type,
                                                   Ubicacion.WEB_URL_NAME, validate_web_url,
                                                   internal_code=LOCATION_INVALID_WEB_ERROR_CODE)

        phone = request.json.get(Ubicacion.PHONE_NAME)
        phone = validate_by_optionality_and_function(fields_optionalities, phone, location_type,
                                                     Ubicacion.PHONE_NAME, validate_phone,
                                                     internal_code=LOCATION_INVALID_PHONE_ERROR_CODE)

        address = request.json.get(Ubicacion.ADDRESS_NAME)
        address = validate_by_optionality_and_function(fields_optionalities, address, location_type,
                                                       Ubicacion.ADDRESS_NAME, validate_address,
                                                       internal_code=LOCATION_INVALID_ADDRESS_ERROR_CODE)

        mail = request.json.get(Ubicacion.MAIL_NAME)
        mail = validate_by_optionality_and_function(fields_optionalities, mail, location_type,
                                                    Ubicacion.MAIL_NAME, validate_mail,
                                                    internal_code=LOCATION_INVALID_MAIL_ERROR_CODE)

        subtype = request.json.get(Ubicacion.SUBTYPE_NAME)
        subtype = validate_by_optionality_and_function(fields_optionalities, subtype, location_type,
                                                       Ubicacion.SUBTYPE_NAME, validate_location_subtype)

        allow_empty_tag_list = fields_optionalities[Ubicacion.TAGS_NAME].opcionalidad == FieldOptionality.OPTIONAL_NAME
        tags = request.json.get(Ubicacion.TAGS_NAME)
        tags = validate_by_optionality_and_function(fields_optionalities, tags, location_type,
                                                    Ubicacion.TAGS_NAME, validate_id_location_tags_list,
                                                    allow_empty_list=allow_empty_tag_list)

        latitude = request.json.get(Ubicacion.LATITUDE_NAME)
        latitude = validate_by_optionality_and_function(fields_optionalities, latitude, location_type,
                                                        Ubicacion.LATITUDE_NAME, validate_latitude,
                                                        internal_code=LOCATION_INVALID_LATITUDE_ERROR_CODE)

        longitude = request.json.get(Ubicacion.LONGITUDE_NAME)
        longitude = validate_by_optionality_and_function(fields_optionalities, longitude, location_type,
                                                         Ubicacion.LONGITUDE_NAME, validate_longitude,
                                                         internal_code=LOCATION_INVALID_LONGITUDE_ERROR_CODE)

        if latitude is not None and longitude is None:
            raise ValidationError(u"The field {0} can not be null when field {1} is not."
                                  .format(Ubicacion.LONGITUDE_NAME,
                                          Ubicacion.LATITUDE_NAME),
                                  internal_code=LOCATION_INVALID_LONGITUDE_ERROR_CODE)

        if longitude is not None and latitude is None:
            raise ValidationError(u"The field {0} can not be null when field {1} is not."
                                  .format(Ubicacion.LATITUDE_NAME,
                                          Ubicacion.LONGITUDE_NAME),
                                  internal_code=LOCATION_INVALID_LATITUDE_ERROR_CODE)
        if tags is None:
            tags = []

        id_parent = request.json.get(Ubicacion.PARENT_NAME)
        if id_parent is not None:
            id_parent = validate_id_location(id_parent)
        return on_namespace_callaback(id_current_client, name, location_type, id_parent, description, active,
                                      latitude, longitude, web, phone, address, mail, tags, subtype)

    return on_client_namespace(id_client, _get_and_validate_json_parameters_on_namespace,
                               action=action,
                               view=LOCATIONS_VIEW_NAME,
                               id_location=id_location)
