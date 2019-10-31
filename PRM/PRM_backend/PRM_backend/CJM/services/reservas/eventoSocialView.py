# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.reservas.EventoSocial import EventoSocial
from CJM.services.validations import validate_id_social_event, SOCIAL_EVENT_INVALID_NAME_ERROR_CODE, \
    SOCIAL_EVENT_INVALID_DESCRIPTION_ERROR_CODE, SOCIAL_EVENT_INVALID_INITIAL_DATE_ERROR_CODE, \
    SOCIAL_EVENT_INVALID_FINAL_DATE_ERROR_CODE, SOCIAL_EVENT_INVALID_RANGE_OF_DATES_ERROR_CODE, \
    SOCIAL_EVENT_INVALID_COMPANY_ERROR_CODE, validate_document_type, validate_document_number, \
    SOCIAL_EVENT_INVALID_DOCUMENT_TYPE_ERROR_CODE, SOCIAL_EVENT_INVALID_DOCUMENT_NUMBER_ERROR_CODE
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_id_client, validate_string_not_empty, DEFAULT_DATE_FORMAT, \
    validate_datetime

SOCIAL_EVENT_VIEW_NAME = "social-events"
app = Blueprint(SOCIAL_EVENT_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/social-events/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_social_event(id_client):
    """
    Crea un evento social en el namespace del cliente id_client
        Parametros esperados:
            name: str
            description: str
            company: str
            initial-date: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
            final-date: str en formato ["%Y%m%d%H%M%S", "%B %d, %Y at %I:%M%p"]
            document-type: str en {"CC", "TI", "CE", "NIT", "TI", "PA", "RC", "NUIP", "CD"}, opcional
            document-number: str, opcional
    :param id_client: id del cliente asociado
    :return: Eventos social creado
    """
    id_client = validate_id_client(id_client)

    def create_package_on_namespace(id_current_client):
        name = request.json.get(EventoSocial.SOCIAL_EVENT_NAME_NAME)
        name = validate_string_not_empty(name, EventoSocial.SOCIAL_EVENT_NAME_NAME,
                                         internal_code=SOCIAL_EVENT_INVALID_NAME_ERROR_CODE)

        description = request.json.get(EventoSocial.DESCRIPTION_NAME)
        description = validate_string_not_empty(description, EventoSocial.DESCRIPTION_NAME,
                                                internal_code=SOCIAL_EVENT_INVALID_DESCRIPTION_ERROR_CODE)

        initial_date = request.json.get(EventoSocial.INITIAL_DATE_NAME)
        initial_date = validate_datetime(initial_date, EventoSocial.INITIAL_DATE_NAME,
                                         internal_code=SOCIAL_EVENT_INVALID_INITIAL_DATE_ERROR_CODE, allow_none=False)

        final_date = request.json.get(EventoSocial.FINAL_DATE_NAME)
        final_date = validate_datetime(final_date, EventoSocial.FINAL_DATE_NAME,
                                       internal_code=SOCIAL_EVENT_INVALID_FINAL_DATE_ERROR_CODE, allow_none=False)

        company = request.json.get(EventoSocial.COMPANY_NAME)
        company = validate_string_not_empty(company, EventoSocial.COMPANY_NAME,
                                            internal_code=SOCIAL_EVENT_INVALID_COMPANY_ERROR_CODE)

        document_number = request.json.get(EventoSocial.DOCUMENT_NUMBER_NAME)
        document_type = request.json.get(EventoSocial.DOCUMENT_TYPE_NAME)
        if document_number is not None or document_type is not None:
            document_type = validate_document_type(document_type, EventoSocial.DOCUMENT_TYPE_NAME,
                                                   internal_code=SOCIAL_EVENT_INVALID_DOCUMENT_TYPE_ERROR_CODE)

            document_number = validate_document_number(document_number, EventoSocial.DOCUMENT_NUMBER_NAME,
                                                       internal_code=SOCIAL_EVENT_INVALID_DOCUMENT_NUMBER_ERROR_CODE)

        if initial_date > final_date:
            raise ValidationError(u"The value of {0} [{1}] should be greater or equals than the value of {2} [{3}]."
                                  .format(EventoSocial.FINAL_DATE_NAME, final_date.strftime(DEFAULT_DATE_FORMAT),
                                          EventoSocial.INITIAL_DATE_NAME, initial_date.strftime(DEFAULT_DATE_FORMAT)),
                                  internal_code=SOCIAL_EVENT_INVALID_RANGE_OF_DATES_ERROR_CODE)

        return EventoSocial.create(id_current_client, name, description, company, initial_date, final_date,
                                   document_type, document_number)

    return on_client_namespace(id_client, create_package_on_namespace,
                               action=Role.CREATE_ACTION,
                               view=SOCIAL_EVENT_VIEW_NAME)


@app.route('/clients/<int:id_client>/social-events/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_social_events(id_client):
    """
    Lista los eventos sociales del cliente correspondiente.
    :param id_client: id del cliente asociado
    :return: Lista de eventos sociales del cliente
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_social_events_on_namespace(id_current_client):
        return EventoSocial.list()

    return on_client_namespace(id_client, list_social_events_on_namespace,
                               action=Role.READ_ACTION,
                               view=SOCIAL_EVENT_VIEW_NAME)


@app.route('/clients/<int:id_client>/social-events/<int:id_social_event>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_social_event(id_client, id_social_event):
    """
    Da el evento social con id dado.
    :param id_client: id del cliente asociado
    :param id_social_event: id del eventos social asociado
    :return: Evento social con id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_social_event_on_namespace(id_current_client):
        id_current_social_event = validate_id_social_event(id_social_event)
        return EventoSocial.get_by_id(id_current_social_event)

    return on_client_namespace(id_client, get_social_event_on_namespace,
                               action=Role.READ_ACTION,
                               view=SOCIAL_EVENT_VIEW_NAME)
