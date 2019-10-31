# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.persons.Persona import Persona
from CJM.services.persons import queryPersonFromCompensarService
from CJM.services.validations import validate_document_type, validate_document_number, validate_gender, \
    validate_id_person, NO_DOCUMENT_DOCUMENT_TYPE
from CJM.services.validations import validate_person_category, validate_person_affiliation, \
    PERSON_DOES_NOT_EXISTS_ERROR_CODE, PERSON_INVALID_CITY_ERROR_CODE, PERSON_INVALID_COMPANY_ERROR_CODE, \
    PERSON_INVALID_PROFESSION_ERROR_CODE, PERSON_INVALID_NATIONALITY_ERROR_CODE, PERSON_INVALID_MAIL_ERROR_CODE, \
    PERSON_INVALID_FIRST_NAME_ERROR_CODE, PERSON_INVALID_LAST_NAME_ERROR_CODE, PERSON_INVALID_DOCUMENT_TYPE_ERROR_CODE, \
    PERSON_INVALID_DOCUMENT_NUMBER_ERROR_CODE, PERSON_INVALID_GENDER_ERROR_CODE, PERSON_INVALID_BIRTHDATE_ERROR_CODE, \
    PERSON_DUPLICATED_DOCUMENT_ERROR_CODE
from commons.entidades.Cliente import Cliente
from commons.entidades.optionalities.FieldOptionality import FieldOptionality
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import EntityDoesNotExists, EntityAlreadyExists
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless, with_json_body
from commons.validations import validate_string_not_empty, validate_id_client, validate_date, validate_mail, \
    validate_by_optionality_and_function, COMPENSAR_PERSON_SERVICE_NAME

PERSONS_VIEW_NAME = "person"
app = Blueprint(PERSONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/persons/', methods=['POST'], strict_slashes=False)
@with_json_body
def create_person(id_client):
    """
    Crea una persona en el namespace del cliente id_client
        Parametros esperados:
            first-name: str
            laste-name: str
            document-type: str en {"CC", "TI", "CE"}
            document-number: str
            mail: str
            gender: str en {"masculino, femenino, f, m"}
            birthdate: str en formato YYYYMMDD
            category: str en {"A", "B", "C", "D"}
            affiliation: str en {"COTIZANTE", "BENEFICIARIO"}
            nationality: str
            profession: str
            city: str
            company: str
    :param id_client: id del cliente asociado
    :return: Persona creada
    """
    def create_person_on_namespace(id_current_client, first_name, last_name, document_type, document_number, mail,
                                   gender, birthdate, category, affiliation, nationality, profession, city, company):
        if document_type != NO_DOCUMENT_DOCUMENT_TYPE:
            person_by_document = Persona.get_person_by_document(document_type, document_number)
            if person_by_document is not None:
                raise EntityAlreadyExists(u"Person[{0}={1}, {2}={3}]"
                                          .format(Persona.DOCUMENT_TYPE_NAME, document_type,
                                                  Persona.DOCUMENT_NUMBER_NAME, document_number),
                                          internal_code=PERSON_DUPLICATED_DOCUMENT_ERROR_CODE)
        return Persona.create(id_current_client, first_name, last_name, document_type, document_number, mail,
                              gender, birthdate, category, affiliation, nationality, profession, city, company)

    return _get_and_validate_person_json_params(id_client, create_person_on_namespace, Role.CREATE_ACTION)


@app.route('/clients/<int:id_client>/persons/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_persons(id_client):
    """
    Da la lista de personas del cliente con id dado
    :param id_client: id del cliente a consultar
    :return: Lista de personas del cliente dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_persons_on_namespace(id_current_client):
        return Persona.list()

    return on_client_namespace(id_client, list_persons_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_person_by_id(id_client, id_person):
    """
    Da la información de la persona con id dado asociada al cliente dado.
    :param id_client: id del cliente a consultar
    :param id_person: de de la persona a consultar
    :return: Persona con el id dado
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        return Persona.get_by_id(id_current_person)
    return on_client_namespace(id_client, get_person_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/person-by-id/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_person_by_document(id_client):
    """
    Da la de persona con tipo y numero de documento dados con id dado del cliente correspondiente.
        Parametros esperados en el query string:
            document-type: str en {"CC", "TI", "CE"}
            document-number: str
    :param id_client: id del cliente asociado
    :return: Persona con tipo y numero de documento dados
    """
    id_client = validate_id_client(id_client)
    client = Cliente.get_by_id(id_client)
    document_type = request.args.get(Persona.DOCUMENT_TYPE_NAME)
    document_type = validate_document_type(document_type, Persona.DOCUMENT_TYPE_NAME,
                                           internal_code=PERSON_INVALID_DOCUMENT_TYPE_ERROR_CODE)

    document_number = request.args.get(Persona.DOCUMENT_NUMBER_NAME)
    document_number = validate_document_number(document_number, Persona.DOCUMENT_NUMBER_NAME,
                                               internal_code=PERSON_INVALID_DOCUMENT_NUMBER_ERROR_CODE)

    def get_person_by_document_on_namespace(id_current_client):
        person = None
        if client.servicoPersonasExterno is not None:
            person = _query_person_by_external_service(id_current_client, client.servicoPersonasExterno,
                                                       document_number, document_type)
        if person is None:
            person = Persona.get_person_by_document(document_type, document_number)
        if person is None:
            raise EntityDoesNotExists(u"Person", internal_code=PERSON_DOES_NOT_EXISTS_ERROR_CODE)
        return person

    return on_client_namespace(id_client, get_person_by_document_on_namespace,
                               view=PERSONS_VIEW_NAME,
                               action=Role.READ_ACTION)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/', methods=['DELETE'], strict_slashes=False)
@with_json_bodyless
def delete_person_by_id(id_client, id_person):
    """
    Elimina la persona con id dado asociada al cliente dado.
    :param id_client: id del cliente a consultar
    :param id_person: id de de la persona a eliminar
    :return: Persona eliminada
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def get_person_on_namespace(id_current_client):
        id_current_person = validate_id_person(id_person)
        person = Persona.get_by_id(id_current_person)
        person.key.delete()
        return person
    return on_client_namespace(id_client, get_person_on_namespace,
                               action=Role.DELETE_ACTION,
                               view=PERSONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/persons/<int:id_person>/', methods=['PUT'], strict_slashes=False)
@with_json_body
def update_person_by_id(id_client, id_person):
    """
    Actualiza la persona con id dado en el namespace del cliente id_client
        Parametros esperados:
            first-name: str
            laste-name: str
            document-type: str en {"CC", "TI", "CE", "NIT", "TI", "PA", "RC", "NUIP", "CD"}
            document-number: str
            mail: str
            gender: str en {"masculino, femenino, f, m"}
            birthdate: str en formato YYYYMMDD
            category: str en {"A", "B", "C", "D"}
            affiliation: str en {"COTIZANTE", "BENEFICIARIO"}
            nationality: str
            profession: str
            city: str
            company: str
    :param id_client: id del cliente asociado
    :param id_person: id de la persona a actualizar
    :return: Persona actualizada
    """
    # noinspection PyUnusedLocal
    def update_person_by_id_on_namespace(id_current_client, first_name, last_name, document_type, document_number, mail,
                                         gender, birthdate, category, affiliation, nationality, profession, city,
                                         company):
        id_current_person = validate_id_person(id_person)
        if document_type != NO_DOCUMENT_DOCUMENT_TYPE:
            person_by_document = Persona.get_person_by_document(document_type, document_number)
            if person_by_document is not None and person_by_document.key.id() != id_current_person:
                raise EntityAlreadyExists(u"The entity (Person[{0}={1}, {2}={3}]) already exists."
                                          .format(Persona.DOCUMENT_TYPE_NAME, document_type,
                                                  Persona.DOCUMENT_NUMBER_NAME, document_number),
                                          internal_code=PERSON_DUPLICATED_DOCUMENT_ERROR_CODE)
        return Persona.update(id_current_person, first_name, last_name, document_type, document_number, mail,
                              gender, birthdate, category, affiliation, nationality, profession, city, company)

    return _get_and_validate_person_json_params(id_client, update_person_by_id_on_namespace, Role.UPDATE_ACTION)


def _query_person_by_external_service(id_client, external_service, document_number, document_type):
    if external_service == COMPENSAR_PERSON_SERVICE_NAME:
        # Se debe usar a nivel de modulo para permitir mockearla en pruebas
        return queryPersonFromCompensarService.try_get_person_from_compensar_service(id_client,
                                                                                     document_number, document_type)
    else:
        return None


def _get_and_validate_person_json_params(id_client, on_namespace_callback, action):
    id_client = validate_id_client(id_client)

    def _get_and_validate_person_json_params_on_namespace(id_current_client):
        first_name = request.json.get(Persona.FIRST_NAME_NAME)
        first_name = validate_string_not_empty(first_name, Persona.FIRST_NAME_NAME,
                                               internal_code=PERSON_INVALID_FIRST_NAME_ERROR_CODE)

        last_name = request.json.get(Persona.LAST_NAME_NAME)
        last_name = validate_string_not_empty(last_name, Persona.LAST_NAME_NAME,
                                              internal_code=PERSON_INVALID_LAST_NAME_ERROR_CODE)

        document_type = request.json.get(Persona.DOCUMENT_TYPE_NAME)
        document_type = validate_document_type(document_type, Persona.DOCUMENT_TYPE_NAME,
                                               internal_code=PERSON_INVALID_DOCUMENT_TYPE_ERROR_CODE)

        document_number = request.json.get(Persona.DOCUMENT_NUMBER_NAME)
        document_number = validate_document_number(document_number, Persona.DOCUMENT_NUMBER_NAME,
                                                   internal_code=PERSON_INVALID_DOCUMENT_NUMBER_ERROR_CODE)

        gender = request.json.get(Persona.GENDER_NAME)
        gender = validate_gender(gender, Persona.GENDER_NAME, internal_code=PERSON_INVALID_GENDER_ERROR_CODE)

        birthdate = request.json.get(Persona.BIRTHDATE_NAME)
        birthdate = validate_date(birthdate, Persona.BIRTHDATE_NAME, internal_code=PERSON_INVALID_BIRTHDATE_ERROR_CODE)

        fields_optionalities = FieldOptionality.list_by_view_as_dict(PERSONS_VIEW_NAME)
        person_type_for_optionality = None

        mail = request.json.get(Persona.MAIL_NAME)
        mail = validate_by_optionality_and_function(fields_optionalities, mail,
                                                    person_type_for_optionality,
                                                    Persona.MAIL_NAME, validate_mail,
                                                    internal_code=PERSON_INVALID_MAIL_ERROR_CODE)

        category = request.json.get(Persona.CATEGORY_NAME)
        category = validate_by_optionality_and_function(fields_optionalities, category,
                                                        person_type_for_optionality,
                                                        Persona.CATEGORY_NAME, validate_person_category)

        affiliation = request.json.get(Persona.AFFILIATION_NAME)
        affiliation = validate_by_optionality_and_function(fields_optionalities, affiliation,
                                                           person_type_for_optionality,
                                                           Persona.AFFILIATION_NAME, validate_person_affiliation)

        nationality = request.json.get(Persona.NATIONALITY_NAME)
        nationality = validate_by_optionality_and_function(fields_optionalities, nationality,
                                                           person_type_for_optionality,
                                                           Persona.NATIONALITY_NAME, validate_string_not_empty,
                                                           internal_code=PERSON_INVALID_NATIONALITY_ERROR_CODE)

        profession = request.json.get(Persona.PROFESSION_NAME)
        profession = validate_by_optionality_and_function(fields_optionalities, profession,
                                                          person_type_for_optionality,
                                                          Persona.PROFESSION_NAME, validate_string_not_empty,
                                                          internal_code=PERSON_INVALID_PROFESSION_ERROR_CODE)

        company = request.json.get(Persona.COMPANY_NAME)
        company = validate_by_optionality_and_function(fields_optionalities, company,
                                                       person_type_for_optionality,
                                                       Persona.COMPANY_NAME, validate_string_not_empty,
                                                       internal_code=PERSON_INVALID_COMPANY_ERROR_CODE)

        city = request.json.get(Persona.CITY_NAME)
        city = validate_by_optionality_and_function(fields_optionalities, city,
                                                    person_type_for_optionality,
                                                    Persona.CITY_NAME, validate_string_not_empty,
                                                    internal_code=PERSON_INVALID_CITY_ERROR_CODE)

        return on_namespace_callback(id_current_client, first_name, last_name, document_type, document_number, mail,
                                     gender, birthdate, category, affiliation, nationality, profession,
                                     city, company)

    return on_client_namespace(id_client, _get_and_validate_person_json_params_on_namespace,
                               action=action,
                               view=PERSONS_VIEW_NAME)
