# -*- coding: utf-8 -*
import unittest

from CJM.services.validations import GENDERS_ABBREVIATIONS, VALID_DOCUMENTS
from commons.validations import validate_date
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    PERSON_DOES_NOT_EXISTS_CODE, PERSON_INVALID_CITY_CODE, PERSON_INVALID_COMPANY_CODE, PERSON_INVALID_PROFESSION_CODE, \
    PERSON_INVALID_NATIONALITY_CODE, PERSON_INVALID_AFFILIATION_CODE, PERSON_INVALID_CATEGORY_CODE, \
    PERSON_INVALID_MAIL_CODE, PERSON_INVALID_FIRST_NAME_CODE, PERSON_INVALID_LAST_NAME_CODE, \
    PERSON_INVALID_DOCUMENT_TYPE_CODE, PERSON_INVALID_DOCUMENT_NUMBER_CODE, PERSON_INVALID_GENDER_CODE, \
    PERSON_INVALID_BIRTHDATE_CODE, PERSON_DUPLICATED_DOCUMENT_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import LOCATION_ENTITY_NAME
from tests.testsCJM.testReservas import ADMIN_USERNAME, create_and_login_new_admin_user, USERNAME_NAME


class PersonaViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"
    FIRST_NAME_NAME = u"first-name"
    LAST_NAME_NAME = u"last-name"
    DOCUMENT_TYPE_NAME = u"document-type"
    DOCUMENT_NUMBER_NAME = u"document-number"
    GENDER_NAME = u"gender"
    BIRTHDATE_NAME = u"birthdate"
    MAIL_NAME = u"mail"
    CATEGORY_NAME = u"category"
    AFFILIATION_NAME = u"affiliation"
    NATIONALITY_NAME = u"nationality"
    PROFESSION_NAME = u"profession"
    COMPANY_NAME = u"company"
    CITY_NAME = u"city"
    COMPENSAR_PERSON_SERVICE_NAME = u"COMPENSAR"
    NO_DOCUMENT_DOCUMENT_TYPE = u"NO_DOCUMENT"
    UNKNOWN_DOCUMENT_NUMBER = u"Desconocido"

    ENTITY_DOES_NOT_EXISTS_CODE = PERSON_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/persons/"

    ATTRIBUTES_NAMES_BY_FIELDS = {FIRST_NAME_NAME: "TEST_PERSON_FIRST_NAME",
                                  LAST_NAME_NAME: "TEST_PERSON_LAST_NAME",
                                  DOCUMENT_TYPE_NAME: "TEST_PERSON_DOCUMENT_TYPE",
                                  DOCUMENT_NUMBER_NAME: "TEST_PERSON_DOCUMENT_NUMBER",
                                  GENDER_NAME: "TEST_PERSON_GENDER",
                                  BIRTHDATE_NAME: "TEST_PERSON_BIRTHDATE",
                                  MAIL_NAME: "TEST_PERSON_MAIL",
                                  CATEGORY_NAME: "TEST_PERSON_CATEGORY",
                                  AFFILIATION_NAME: "TEST_PERSON_AFFILIATION",
                                  NATIONALITY_NAME: "TEST_PERSON_NATIONALITY",
                                  CITY_NAME: "TEST_PERSON_CITY_OF_RESIDENCE",
                                  COMPANY_NAME: "TEST_PERSON_COMPANY",
                                  PROFESSION_NAME: "TEST_PERSON_PROFESSION"}

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password_123"
    TEST_USER_ROLE = None

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    ENTITY_NAME = 'personas'
    PERSON_NAME = 'person'

    def setUp(self):
        super(PersonaViewTestCase, self).setUp()
        create_test_client(self)

    def setup_scenario_with_person_reservation_and_person(self):
        from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location
        from tests.testsCJM.testReservas.testReservaViewTestCase import create_test_reservation
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation
        from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        create_test_location(self)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[self.ENTITY_NAME]
        create_test_reservation(self)
        create_test_person_reservation(self)

    def _check_persons_with_user_and_children_data(self, results, username):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import PERSON_RESERVATION_ENTITY_NAME
        person = results[0][PERSON_RESERVATION_ENTITY_NAME][0][self.PERSON_NAME]
        if person is not None:
            self.check_list_response(self.ENTITY_NAME, [person], 1)
            self.assertEqual(username, person.get(USERNAME_NAME, None))
        else:
            self.check_list_response(self.ENTITY_NAME, [], 0)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.DOCUMENT_TYPE_NAME] = "CC"
        values[cls.GENDER_NAME] = "m"
        values[cls.BIRTHDATE_NAME] = "19901020"
        values[cls.CATEGORY_NAME] = "A"
        values[cls.AFFILIATION_NAME] = "Cotizante"
        values[cls.NATIONALITY_NAME] = "Colombiano"
        values[cls.PROFESSION_NAME] = "Ingeniero"
        values[cls.CITY_NAME] = u"Bogotá"
        values[cls.COMPANY_NAME] = "Empresa"
        return values

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.FIRST_NAME_NAME] = "Nombre{0}"
        templates[cls.LAST_NAME_NAME] = "Apellido{0}"
        templates[cls.DOCUMENT_NUMBER_NAME] = "100{0}"
        templates[cls.MAIL_NAME] = "mail{0}@mail.com"
        return templates

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.DOCUMENT_TYPE_NAME] = "TI"
        values[cls.GENDER_NAME] = "f"
        values[cls.BIRTHDATE_NAME] = "19911020"
        values[cls.CATEGORY_NAME] = "B"
        values[cls.AFFILIATION_NAME] = "BENEFICIARIO"
        values[cls.NATIONALITY_NAME] = "Venezolano"
        values[cls.PROFESSION_NAME] = "Medico"
        values[cls.CITY_NAME] = u"Caracas"
        values[cls.COMPANY_NAME] = "Otra empresa"
        return values

    @classmethod
    def get_entity_values_templates_for_update(cls):
        templates = dict()
        templates[cls.FIRST_NAME_NAME] = "New nombre{0}"
        templates[cls.LAST_NAME_NAME] = "New apellido{0}"
        templates[cls.DOCUMENT_NUMBER_NAME] = "200{0}"
        templates[cls.MAIL_NAME] = "new_mail{0}@mail.com"
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        if request_values.get(cls.CATEGORY_NAME) is not None:
            request_values[cls.CATEGORY_NAME] = request_values[cls.CATEGORY_NAME].upper()
        if request_values.get(cls.AFFILIATION_NAME) is not None:
            request_values[cls.AFFILIATION_NAME] = request_values[cls.AFFILIATION_NAME].upper()
        if request_values.get(cls.GENDER_NAME) is not None:
            gender = request_values[cls.GENDER_NAME]
            if gender in GENDERS_ABBREVIATIONS:
                request_values[cls.GENDER_NAME] = GENDERS_ABBREVIATIONS[gender]
        if request_values.get(cls.BIRTHDATE_NAME) is not None:
            from commons.validations import DEFAULT_DATE_FORMAT
            request_values[cls.BIRTHDATE_NAME] = validate_date(request_values[cls.BIRTHDATE_NAME],
                                                               PersonaViewTestCase.BIRTHDATE_NAME).strftime(DEFAULT_DATE_FORMAT)
        return request_values

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_persons_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_persons(self):
        self.do_create_requests()

    def test_create_valid_person_reservation_and_query_person_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.setup_scenario_with_person_reservation_and_person()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_persons_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_person_reservation_and_query_person_with_reservations_endpoint_true_include_children_and_new_user(self):
        create_and_login_new_admin_user(self)
        self.setup_scenario_with_person_reservation_and_person()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_persons_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_persons_with_birthdate_second_format(self):
        self.assign_field_value(self.BIRTHDATE_NAME, "June 01, 2016")
        self.do_create_requests()

    def test_create_valid_persons_changing_document_type(self):
        num_persons = 0
        for document_type in VALID_DOCUMENTS:
            self.assign_field_value(self.DOCUMENT_TYPE_NAME, document_type)
            self.do_create_requests(previously_created_entities=num_persons)
            num_persons += self.NUMBER_OF_ENTITIES

    def test_create_valid_persons_changing_gender(self):
        num_persons = 0
        for gender_abbrevation in GENDERS_ABBREVIATIONS:
            self.assign_field_value(self.GENDER_NAME, gender_abbrevation)
            self.do_create_requests(previously_created_entities=num_persons)
            num_persons += self.NUMBER_OF_ENTITIES

            self.assign_field_value(self.GENDER_NAME, GENDERS_ABBREVIATIONS[gender_abbrevation])
            self.do_create_requests(previously_created_entities=num_persons)
            num_persons += self.NUMBER_OF_ENTITIES

    def test_create_valid_persons_without_mail(self):
        self.assign_field_value(self.MAIL_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_without_category(self):
        self.assign_field_value(self.CATEGORY_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_without_affiliation(self):
        self.assign_field_value(self.AFFILIATION_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_without_nationality(self):
        self.assign_field_value(self.NATIONALITY_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_without_profession(self):
        self.assign_field_value(self.PROFESSION_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_without_company(self):
        self.assign_field_value(self.COMPANY_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_without_city(self):
        self.assign_field_value(self.CITY_NAME, None)
        self.do_create_requests()

    def test_create_valid_persons_with_duplicated_documents_with_document_type_no_document(self):
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, self.NO_DOCUMENT_DOCUMENT_TYPE)
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, self.UNKNOWN_DOCUMENT_NUMBER)
        self.do_create_requests()
        self.do_create_requests()

    def test_create_invalid_persons_with_duplicated_documents(self):
        self.do_create_requests()
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_DUPLICATED_DOCUMENT_CODE)

    def test_try_create_invalid_persons_with_empty_city(self):
        self.assign_field_value(self.CITY_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_CITY_CODE)

    def test_try_create_invalid_persons_with_empty_company(self):
        self.assign_field_value(self.COMPANY_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_COMPANY_CODE)

    def test_try_create_invalid_persons_with_empty_profession(self):
        self.assign_field_value(self.PROFESSION_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_PROFESSION_CODE)

    def test_try_create_invalid_persons_with_empty_nationality(self):
        self.assign_field_value(self.NATIONALITY_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_NATIONALITY_CODE)

    def test_try_create_invalid_persons_with_empty_affiliation(self):
        self.assign_field_value(self.AFFILIATION_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_AFFILIATION_CODE)

    def test_try_create_invalid_persons_with_invalid_affiliation(self):
        self.assign_field_value(self.AFFILIATION_NAME, "INVALID AFFILIATION")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_AFFILIATION_CODE)

    def test_try_create_invalid_persons_with_empty_category(self):
        self.assign_field_value(self.CATEGORY_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_CATEGORY_CODE)

    def test_try_create_invalid_persons_with_invalid_category(self):
        self.assign_field_value(self.CATEGORY_NAME, "INVALID CATEGORY")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_CATEGORY_CODE)

    def test_try_create_invalid_persons_with_empty_mail(self):
        self.assign_field_value(self.MAIL_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_MAIL_CODE)

    def test_try_create_invalid_persons_with_invalid_mail(self):
        self.assign_field_value(self.MAIL_NAME, "INVALID_MAIL")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_MAIL_CODE)

    def test_create_invalid_persons_without_first_name(self):
        self.assign_field_value(self.FIRST_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_FIRST_NAME_CODE)

    def test_create_invalid_persons_with_empty_first_name(self):
        self.assign_field_value(self.FIRST_NAME_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_FIRST_NAME_CODE)

    def test_create_invalid_persons_without_last_name(self):
        self.assign_field_value(self.LAST_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_LAST_NAME_CODE)

    def test_create_invalid_persons_with_empty_last_name(self):
        self.assign_field_value(self.LAST_NAME_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_LAST_NAME_CODE)

    def test_create_invalid_persons_without_document_type(self):
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_DOCUMENT_TYPE_CODE)

    def test_create_invalid_persons_with_invalid_document_type(self):
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, "INVALID_DOC_TYPE")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_DOCUMENT_TYPE_CODE)

    def test_create_invalid_persons_without_document_number(self):
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_DOCUMENT_NUMBER_CODE)

    def test_create_invalid_persons_without_gender(self):
        self.assign_field_value(self.GENDER_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_GENDER_CODE)

    def test_create_invalid_persons_with_invalid_gender(self):
        self.assign_field_value(self.GENDER_NAME, "INVALID_GENDER")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_GENDER_CODE)

    def test_create_invalid_persons_without_birthdate(self):
        self.assign_field_value(self.BIRTHDATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_create_invalid_persons_with_invalid_birthdate(self):
        self.assign_field_value(self.BIRTHDATE_NAME, "INVALID_BIRTHDATE")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_create_invalid_persons_with_out_of_range_birthdate_by_month(self):
        self.assign_field_value(self.BIRTHDATE_NAME, "19901501")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_create_invalid_persons_with_out_of_range_birthdate_by_day(self):
        self.assign_field_value(self.BIRTHDATE_NAME, "19900230")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_create_persons_on_multiple_clients(self):
        self.do_create_requests()

        self.clean_test_data()
        create_test_client(self, create_new_client=True)

        self.do_create_requests()

    def test_delete_persons(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_create_valid_person_reservation_and_query_person_with_reservations_endpoint_true_include_children_after_delete(self):
        self.setup_scenario_with_person_reservation_and_person()
        create_and_login_new_admin_user(self)
        self.do_delete_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_persons_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_delete_invalid_persons_with_wrong_client(self):
        self.do_create_requests()
        create_test_client(self, create_new_client=True)
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_delete_invalid_non_existent_person(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE)

    def test_update_valid_persons(self):
        self.do_create_requests()
        self.do_update_requests()

    def test_create_valid_person_reservation_and_query_person_with_reservations_endpoint_true_include_children_after_update(self):
        self.setup_scenario_with_person_reservation_and_person()
        create_and_login_new_admin_user(self)
        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_persons_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_try_update_invalid_non_existent_persons(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()

        self.do_update_requests(expected_code=404, expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE)

    def test_update_valid_persons_with_birthdate_second_format(self):
        self.do_create_requests()
        self.assign_field_value(self.BIRTHDATE_NAME, "June 01, 2016")
        self.do_update_requests()

    def test_update_valid_persons_without_mail(self):
        self.do_create_requests()
        self.assign_field_value(self.MAIL_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_without_category(self):
        self.do_create_requests()
        self.assign_field_value(self.CATEGORY_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_without_affiliation(self):
        self.do_create_requests()
        self.assign_field_value(self.AFFILIATION_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_without_nationality(self):
        self.do_create_requests()
        self.assign_field_value(self.NATIONALITY_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_without_profession(self):
        self.do_create_requests()
        self.assign_field_value(self.PROFESSION_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_without_company(self):
        self.do_create_requests()
        self.assign_field_value(self.COMPANY_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_without_city(self):
        self.do_create_requests()
        self.assign_field_value(self.CITY_NAME, None)
        self.do_update_requests()

    def test_update_valid_persons_with_duplicated_documents_with_document_type_no_document(self):
        self.do_create_requests()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False,
                                check_results_as_list=False)
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, self.NO_DOCUMENT_DOCUMENT_TYPE)
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, self.UNKNOWN_DOCUMENT_NUMBER)
        self.do_update_requests()

    def test_update_valid_person_with_same_document(self):
        original_number_of_entities = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1

        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, document_number)
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, document_type)
        self.do_update_requests()

        self.NUMBER_OF_ENTITIES = original_number_of_entities

    def test_try_update_invalid_persons_with_duplicated_documents(self):
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        self.clean_test_data()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False,
                                check_results_as_list=False)
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, document_number)
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, document_type)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_DUPLICATED_DOCUMENT_CODE,
                                do_get_and_check_results=False)

    def test_try_update_invalid_persons_with_empty_city(self):
        self.do_create_requests()
        self.assign_field_value(self.CITY_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_CITY_CODE)

    def test_try_update_invalid_persons_with_empty_company(self):
        self.do_create_requests()
        self.assign_field_value(self.COMPANY_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_COMPANY_CODE)

    def test_try_update_invalid_persons_with_empty_profession(self):
        self.do_create_requests()
        self.assign_field_value(self.PROFESSION_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_PROFESSION_CODE)

    def test_try_update_invalid_persons_with_empty_nationality(self):
        self.do_create_requests()
        self.assign_field_value(self.NATIONALITY_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_NATIONALITY_CODE)

    def test_try_update_invalid_persons_with_empty_affiliation(self):
        self.do_create_requests()
        self.assign_field_value(self.AFFILIATION_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_AFFILIATION_CODE)

    def test_try_update_invalid_persons_with_invalid_affiliation(self):
        self.do_create_requests()
        self.assign_field_value(self.AFFILIATION_NAME, "INVALID AFFILIATION")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_AFFILIATION_CODE)

    def test_try_update_invalid_persons_with_empty_category(self):
        self.do_create_requests()
        self.assign_field_value(self.CATEGORY_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_CATEGORY_CODE)

    def test_try_update_invalid_persons_with_invalid_category(self):
        self.do_create_requests()
        self.assign_field_value(self.CATEGORY_NAME, "INVALID CATEGORY")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_CATEGORY_CODE)

    def test_try_update_invalid_persons_with_empty_mail(self):
        self.do_create_requests()
        self.assign_field_value(self.MAIL_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_MAIL_CODE)

    def test_try_update_invalid_persons_with_invalid_mail(self):
        self.do_create_requests()
        self.assign_field_value(self.MAIL_NAME, "INVALID_MAIL")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_MAIL_CODE)

    def test_try_update_invalid_persons_without_first_name(self):
        self.do_create_requests()
        self.assign_field_value(self.FIRST_NAME_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_FIRST_NAME_CODE)

    def test_try_update_invalid_persons_with_empty_first_name(self):
        self.do_create_requests()
        self.assign_field_value(self.FIRST_NAME_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_FIRST_NAME_CODE)

    def test_try_update_invalid_persons_without_last_name(self):
        self.do_create_requests()
        self.assign_field_value(self.LAST_NAME_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_LAST_NAME_CODE)

    def test_try_update_invalid_persons_with_empty_last_name(self):
        self.do_create_requests()
        self.assign_field_value(self.LAST_NAME_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_LAST_NAME_CODE)

    def test_try_update_invalid_persons_without_document_type(self):
        self.do_create_requests()
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_update_invalid_persons_with_invalid_document_type(self):
        self.do_create_requests()
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, "INVALID_DOC_TYPE")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_update_invalid_persons_without_document_number(self):
        self.do_create_requests()
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_DOCUMENT_NUMBER_CODE)

    def test_try_update_invalid_persons_without_gender(self):
        self.do_create_requests()
        self.assign_field_value(self.GENDER_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_GENDER_CODE)

    def test_try_update_invalid_persons_with_invalid_gender(self):
        self.do_create_requests()
        self.assign_field_value(self.GENDER_NAME, "INVALID_GENDER")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_GENDER_CODE)

    def test_try_update_invalid_persons_without_birthdate(self):
        self.do_create_requests()
        self.assign_field_value(self.BIRTHDATE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_try_update_invalid_persons_with_invalid_birthdate(self):
        self.do_create_requests()
        self.assign_field_value(self.BIRTHDATE_NAME, "INVALID_BIRTHDATE")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_try_update_invalid_persons_with_out_of_range_birthdate_by_month(self):
        self.do_create_requests()
        self.assign_field_value(self.BIRTHDATE_NAME, "19901501")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_try_update_invalid_persons_with_out_of_range_birthdate_by_day(self):
        self.do_create_requests()
        self.assign_field_value(self.BIRTHDATE_NAME, "19900230")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_INVALID_BIRTHDATE_CODE)

    def test_query_valid_person_by_id(self):
        self.original_number_of_entities = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]

        result = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}&document-number={2}"
                                     .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                             document_type,
                                             document_number))
        self.check_list_response(self.ENTITY_NAME, [result], self.NUMBER_OF_ENTITIES)
        self.NUMBER_OF_ENTITIES = self.original_number_of_entities

    def test_query_valid_person_by_id_doesnt_calls_compensars_service_when_the_client_does_not_has_compensar_as_external_service(self):
        self.original_number_of_entities = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        from CJM.services.persons import queryPersonFromCompensarService
        original_service = queryPersonFromCompensarService.try_get_person_from_compensar_service
        queryPersonFromCompensarService.try_get_person_from_compensar_service = _get_replacemente_function(self, document_number, document_type)
        self.external_service_called = False

        result = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}&document-number={2}"
                                     .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                             document_type,
                                             document_number))
        self.check_list_response(self.ENTITY_NAME, [result], self.NUMBER_OF_ENTITIES)
        self.NUMBER_OF_ENTITIES = self.original_number_of_entities
        self.assertFalse(self.external_service_called)
        queryPersonFromCompensarService.try_get_person_from_compensar_service = original_service

    def test_query_valid_person_by_id_calls_compensars_service_when_the_client_has_compensar_as_external_service(self):
        self.original_number_of_entities = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_CLIENT_EXTERNAL_PERSON_SERVICE = self.COMPENSAR_PERSON_SERVICE_NAME
        create_test_client(self, create_new_client=True)
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        from CJM.services.persons import queryPersonFromCompensarService
        original_service = queryPersonFromCompensarService.try_get_person_from_compensar_service
        queryPersonFromCompensarService.try_get_person_from_compensar_service = _get_replacemente_function(self, document_number, document_type)
        self.external_service_called = False

        result = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}&document-number={2}"
                                     .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                             document_type,
                                             document_number))
        self.check_list_response(self.ENTITY_NAME, [result], self.NUMBER_OF_ENTITIES)
        self.NUMBER_OF_ENTITIES = self.original_number_of_entities
        self.assertTrue(self.external_service_called)
        queryPersonFromCompensarService.try_get_person_from_compensar_service = original_service

    def test_try_query_person_by_id_without_document_type(self):
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        results = self.do_get_request("/clients/{0}/person-by-id/?document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              document_type,
                                              document_number),
                                      expected_code=400)
        validate_error(self, results, PERSON_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_query_person_by_id_without_document_number(self):
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        results = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              document_type,
                                              document_number),
                                      expected_code=400)
        validate_error(self, results, PERSON_INVALID_DOCUMENT_NUMBER_CODE)

    def test_try_query_person_by_id_with_invalid_document_type(self):
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        results = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "INVALID TYPE",
                                              document_number),
                                      expected_code=400)
        validate_error(self, results, PERSON_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_query_person_by_id_with_empty_document_number(self):
        self.do_create_requests()
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        results = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              document_type,
                                              ""),
                                      expected_code=400)
        validate_error(self, results, PERSON_INVALID_DOCUMENT_NUMBER_CODE)

    def test_try_query_non_existent_person_by_id(self):
        self.do_create_requests()
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        results = self.do_get_request("/clients/{0}/person-by-id/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              document_type,
                                              document_number + "100"),
                                      expected_code=404)
        validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_persons(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, \
            CLIENT_PROMOTER_USER
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_persons(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_person(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_by_document(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        document_number = self.original_entities[0][self.DOCUMENT_NUMBER_NAME]
        document_type = self.original_entities[0][self.DOCUMENT_TYPE_NAME]
        url = "/clients/{0}/person-by-id/?document-type={1}&document-number={2}".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                                        document_type,
                                                                                        document_number)
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_persons(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, \
            CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_delete_persons(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)


PERSON_ENTITY_NAME = PersonaViewTestCase.ENTITY_NAME


def create_test_person(test_class, create_new_person=False, validate_results_on_success=True, expected_code=200):
    return PersonaViewTestCase.create_sample_entity_for_another_class(test_class, create_new_person,
                                                                      expected_code=expected_code,
                                                                      validate_results_on_success=validate_results_on_success)


def update_test_person(test_class, id_person, expected_code=200, validate_results_on_success=True):
    return PersonaViewTestCase.update_sample_entity_for_another_class(test_class, id_person,
                                                                      expected_code=expected_code,
                                                                      validate_results_on_success=validate_results_on_success)


if __name__ == '__main__':
    unittest.main()


def _get_replacemente_function(running_entity, expected_document_number, expected_document_type):
    def compensar_service_replacement(id_client, document_number, document_type):
        running_entity.assertEqual(running_entity.expected_ids[CLIENT_ENTITY_NAME], id_client)
        running_entity.assertEqual(expected_document_number, document_number)
        running_entity.assertEqual(expected_document_type, document_type)
        running_entity.external_service_called = True
    return compensar_service_replacement
