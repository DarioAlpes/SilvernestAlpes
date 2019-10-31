# -*- coding: utf-8 -*
import logging
import itertools
import time
import zeep

from google.appengine.ext import ndb
from datetime import datetime

from config_loader import get_pass_compensar_cliente
from zeep.transports import Transport
from zeep.cache import InMemoryCache
from CJM.entidades.persons.Persona import Persona
from assets_loader import get_asset_path
from CJM.entidades.persons.PersonRelationship import PersonRelationship
from CJM.services.persons.personsRelationshipsView import create_symmetric_relationship_without_put
from CJM.services.validations import VALID_CATEGORIES, BENEFICIARIO_AFFILIATION, COTIZANTE_AFFILIATION, \
    GENDERS_ABBREVIATIONS, VALID_GENDERS, CC_DOCUMENT_TYPE, NIT_DOCUMENT_TYPE, TI_DOCUMENT_TYPE, CE_DOCUMENT_TYPE, \
    PASSPORT_DOCUMENT_TYPE, REGISTRO_CIVIL_DOCUMENT_TYPE, NUIP_DOCUMENT_TYPE, CARNE_DIPLOMATICO_DOCUMENT_TYPE, \
    UNKNOWN_RELATIONSHIP, CHILD_RELATIONSHIP, PARENT_RELATIONSHIP, SPOUSE_RELATIONSHIP, SIBLING_RELATIONSHIP
from commons.excepciones.apiexceptions import ValidationError

DOCUMENT_TYPES_BY_ID = {1: CC_DOCUMENT_TYPE,
                        2: NIT_DOCUMENT_TYPE,
                        3: TI_DOCUMENT_TYPE,
                        4: CE_DOCUMENT_TYPE,
                        5: PASSPORT_DOCUMENT_TYPE,
                        7: REGISTRO_CIVIL_DOCUMENT_TYPE,
                        8: NUIP_DOCUMENT_TYPE,
                        10: CARNE_DIPLOMATICO_DOCUMENT_TYPE}

IDS_BY_DOCUMENT_TYPE = {document_type: id_document for id_document, document_type in DOCUMENT_TYPES_BY_ID.iteritems()}

wsdl_path = get_asset_path("PersonaCompensar_V2.wsdl")
client = zeep.Client(wsdl=wsdl_path, transport=Transport(cache=InMemoryCache()))

# Compensar representa la relaciónes al reves, e.g. si la relación es PA la persona dentro del tag familiar es
# el padre de la persona consultada
RELATIONSHIPS_BY_STRING_VALUE = {"HI": PARENT_RELATIONSHIP,
                                 "PA": CHILD_RELATIONSHIP,
                                 "CY": SPOUSE_RELATIONSHIP,
                                 "HM": SIBLING_RELATIONSHIP}


def try_get_person_from_compensar_service(id_client, document_number, document_type):
    try:
        inicial_peticion = time.time()
        result = _do_wsdl_request(document_number, document_type)
        final_peticion = time.time()
        logging.info("Tiempo peticion servicio externo: " + str(final_peticion - inicial_peticion))
    except Exception as e:
        raise ValidationError("Error consultando servicio SOAP: " + str(e))
    is_success = result.resultadoOperacion
    if is_success:
        inicial = time.time()
        all_data = result.consultarClientePersona
        person_client = all_data.clientePersona

        main_person_async = _get_person_by_document_from_person_client_async(person_client)
        person_data = person_client.datosPersona
        family_list = person_data.familia
        family_list = _filter_duplicated_family_members(family_list)
        related_persons_async = [_get_person_by_document_from_person_client_async(family_data.familiar)
                                 for family_data in family_list]
        relationships_types = [_check_relationship_and_parse_to_string(family_data.tipoParentesco)
                               for family_data in family_list]

        main_person = _update_person_from_person_client(id_client, main_person_async, person_client, is_family=False)
        related_persons = [_update_person_from_person_client(id_client, person_async, family_data.familiar, is_family=True)
                           for person_async, family_data in zip(related_persons_async, family_list)]
        ndb.put_multi([main_person] + related_persons)

        previous_relationships = PersonRelationship.list_for_person(main_person.key.id())
        previous_symmetric_relationships_async = [PersonRelationship.get_by_persons_ids_async(relationship.idPersonaDestino,
                                                                                              relationship.idPersonaOrigen)
                                                  for relationship in previous_relationships]
        previous_symmetric_relationships = [previous_relationship.get_result()
                                            for previous_relationship in previous_symmetric_relationships_async]
        previous_relationships_keys = [previous_relationship.key
                                       for previous_relationship in itertools.chain(previous_relationships, previous_symmetric_relationships)
                                       if previous_relationship is not None]
        delete_relationships = ndb.delete_multi_async(previous_relationships_keys)

        new_relationships = [create_symmetric_relationship_without_put(main_person.key.id(),
                                                                       other_person.key.id(),
                                                                       relationship)
                             for relationship, other_person in zip(relationships_types, related_persons)]

        ndb.put_multi([new_relationship
                       for new_relationship_tuple in new_relationships
                       for new_relationship in new_relationship_tuple])
        [delete_relationship.get_result() for delete_relationship in delete_relationships]
        total = time.time()
        logging.info("Tiempo procesando respuesta: " + str(total - inicial))
        return main_person
    else:
        return None


def _filter_duplicated_family_members(family_list):
    previous_documents = set()
    filtered_family_list = []
    for family_member in family_list:
        current_document = (family_member.familiar.tipoIdentificacion, family_member.familiar.legalID)
        if (family_member.familiar.tipoIdentificacion in DOCUMENT_TYPES_BY_ID) and (current_document not in previous_documents):
            filtered_family_list.append(family_member)
            previous_documents.add(current_document)
    return filtered_family_list


def _do_wsdl_request(document_number, document_type):
    headers = {
        "part1": {
            "userName": "usrextclientepersonaSN",
            "password": get_pass_compensar_cliente(),
            "idAplicacion": "SWEX999"}}
    credenciales = dict()
    atributos_comunes = dict()
    # noinspection PyArgumentList
    objeto_consulta = {
        "clientePersona": {
            "legalID": long(document_number),
            "tipoIdentificacion": IDS_BY_DOCUMENT_TYPE[document_type],
            "id": -1},
        "atributosComunes": atributos_comunes}
    opciones_consulta = {
        "opcion": [1, 2, 3]}
    return client.service.Consultar(credenciales, objeto_consulta, opciones_consulta, _soapheaders=headers)


def _update_person_from_person_client(id_client, person_async, person_client, is_family):
    document_type = person_client.tipoIdentificacion
    document_type = check_document_type_and_parse_to_string(document_type)

    document_number = person_client.legalID
    document_number = _check_document_number_and_parse_to_string(document_number)

    person_data = person_client.datosPersona
    first_name = _check_and_join_names(person_data.nombrePrimero,
                                       person_data.nombreSegundo)
    last_name = _check_and_join_names(person_data.apellidoPrimero,
                                      person_data.apellidoSegundo)

    demography_data = person_data.demografia
    gender = demography_data.genero
    gender = _check_gender_and_parse_to_default_format(gender)
    birthdate = demography_data.nacimientoFecha
    birthdate = _check_date_and_parse_to_date_type(birthdate)

    affiliation_list = person_client.afiliacion
    affiliation_list = [affiliation_data for affiliation_data in affiliation_list
                        if affiliation_data.estado == 0]

    category = None
    company = None
    for affiliation_data in affiliation_list:
        new_category = _check_category_and_parse_to_default_format(affiliation_data.categoriaAfiliacion)
        if new_category is not None and (category is None or new_category < category):
            category = new_category
        if company is None:
            responsible_clients = affiliation_data.clienteResponsable
            responsible_company_list = [responsible_client for responsible_client in responsible_clients
                                        if responsible_client.tipoResponsable == 2]
            if len(responsible_company_list) > 0:
                company = responsible_company_list[0].nombre
    affiliation = _get_affiliation_from_category(category)

    communication_list = person_client.comunicacion
    city = None
    mail = None
    biggest_last_modified_date = None
    for communication_data in communication_list:
        last_modified_date = communication_data.adicionalesContacto.ultimaNovedadFecha
        last_modified_date = _check_date_and_parse_to_date_type(last_modified_date)
        is_closer_date = last_modified_date is not None and (biggest_last_modified_date is None or biggest_last_modified_date < last_modified_date)
        if is_closer_date:
            biggest_last_modified_date = last_modified_date
        if city is None or is_closer_date:
            residence_list = communication_data.domicilio
            for residence_data in residence_list:
                locality_data = residence_data.localidad
                new_city = locality_data.nombre
                if new_city is not None:
                    city = new_city
                    break
        if mail is None or is_closer_date:
            mail_list = communication_data.correoElectronico
            if len(mail_list) > 0:
                mail = mail_list[0]
    nationality = None
    profession = None

    person_by_document = person_async.get_result()
    if person_by_document is not None:
        if is_family:
            category = person_by_document.categoria
            affiliation = person_by_document.afiliacion
        person_by_document.update_without_put(first_name, last_name, document_type, document_number, mail, gender,
                                              birthdate, category, affiliation, nationality, profession, city, company)
        return person_by_document
    else:
        return Persona.create_without_put(id_client, first_name, last_name, document_type, document_number, mail,
                                          gender, birthdate, category, affiliation, nationality, profession, city,
                                          company)


def _get_person_by_document_from_person_client_async(person_client):
    document_type = person_client.tipoIdentificacion
    document_type = check_document_type_and_parse_to_string(document_type)

    document_number = person_client.legalID
    document_number = _check_document_number_and_parse_to_string(document_number)
    return Persona.get_person_by_document_async(document_type, document_number)


def _check_relationship_and_parse_to_string(relationship):
    if relationship is None:
        return None
    return RELATIONSHIPS_BY_STRING_VALUE.get(relationship, UNKNOWN_RELATIONSHIP)


def check_document_type_and_parse_to_string(document_type):
    if document_type is None:
        return None
    return DOCUMENT_TYPES_BY_ID.get(document_type, None)


def _check_document_number_and_parse_to_string(document_number):
    if document_number is None:
        return None
    try:
        document_number = str(document_number)
        return _parse_empty_string_to_none(document_number)
    except ValueError:
        return None


def _check_date_and_parse_to_date_type(date_value):
    if date_value is None:
        return None
    if isinstance(date_value, datetime):
        return date_value.date()
    if isinstance(date_value, str):
        try:
            return datetime.strptime(date_value, "%Y-%m-%dT%H:%M:%S").date()
        except ValueError:
            return None
    return None


def _check_gender_and_parse_to_default_format(gender):
    if gender is None:
        return None
    gender = gender.lower()
    if gender in GENDERS_ABBREVIATIONS:
        return GENDERS_ABBREVIATIONS[gender]
    elif gender in VALID_GENDERS:
        return gender
    else:
        return None


def _get_affiliation_from_category(category):
    if category is None:
        return BENEFICIARIO_AFFILIATION
    else:
        return COTIZANTE_AFFILIATION


def _check_category_and_parse_to_default_format(category):
    if category is None:
        return None
    category = category.upper()
    if category in VALID_CATEGORIES:
        return category
    else:
        return None


def _check_and_join_names(first_name, second_name):
    first_name = _parse_empty_string_to_none(first_name)
    second_name = _parse_empty_string_to_none(second_name)
    if first_name is None:
        return second_name
    else:
        if second_name is None:
            return first_name
        else:
            return first_name + " " + second_name


def _parse_empty_string_to_none(string_to_parse):
    if string_to_parse is not None:
        string_to_parse = string_to_parse.strip()
        if len(string_to_parse) == 0:
            string_to_parse = None
    return string_to_parse
