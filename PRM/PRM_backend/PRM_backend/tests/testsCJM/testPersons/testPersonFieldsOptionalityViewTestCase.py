# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    OPTIONALITY_FIELD_DOES_NOT_EXISTS_CODE, OPTIONALITY_INVALID_OPTIONALITY_CODE, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE, \
    OPTIONALITY_INVALID_DEFAULT_VALUE_CODE, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, update_test_person


class PersonFieldsOptionalityViewTestCase(FlaskClientBaseTestCase):
    FIELD_NAME_NAME = u"field"
    OPTIONALITY_NAME = u"optionality"
    APPLICABLE_TYPES_NAME = u"applicable-types"
    DEFAULT_VALUE_NAME = u"default-value"
    VIEW_NAME = u"view"

    ID_NAME = FIELD_NAME_NAME

    ENTITY_DOES_NOT_EXISTS_CODE = None
    RESOURCE_URL = u"/clients/{0}/person-fields-optionalities/"

    TEST_LOCATION_DESCRIPTION = "test description"
    TEST_LOCATION_LATITUDE = None
    TEST_LOCATION_LONGITUDE = None
    TEST_LOCATION_WEB_URL = None
    TEST_LOCATION_PHONE = None
    TEST_LOCATION_MAIL = None
    TEST_LOCATION_ADDRESS = None
    TEST_LOCATION_TAGS = None
    TEST_LOCATION_SUBTYPE = None
    TEST_LOCATION_TYPE = "COUNTRY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_PERSON_FIRST_NAME = u"FirstName"
    TEST_PERSON_LAST_NAME = u"LastName"
    TEST_PERSON_DOCUMENT_TYPE = u"CC"
    TEST_PERSON_DOCUMENT_NUMBER = u"12345"
    TEST_PERSON_MAIL = u"mail@test.com"
    TEST_PERSON_GENDER = u"m"
    TEST_PERSON_BIRTHDATE = u"19900101"
    TEST_PERSON_TYPE = u"Adulto"
    TEST_PERSON_CATEGORY = u"A"
    TEST_PERSON_AFFILIATION = u"Cotizante"
    TEST_PERSON_NATIONALITY = u"Colombiano"
    TEST_PERSON_PROFESSION = u"Ingeniero"
    TEST_PERSON_CITY_OF_RESIDENCE = u"Bogotá"
    TEST_PERSON_COMPANY = u"Empresa"

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    ENTITY_NAME = u"person-fields-permissions"
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

    MANDATORY_NAME = u"MANDATORY"
    OPTIONAL_NAME = u"OPTIONAL"
    FORBIDDEN_NAME = u"FORBIDDEN"

    PERSONS_VIEW_NAME = u"person"

    def setUp(self):
        super(PersonFieldsOptionalityViewTestCase, self).setUp()
        create_test_client(self)
        self.expected_optionalities = {self.FIRST_NAME_NAME: self.MANDATORY_NAME,
                                       self.LAST_NAME_NAME: self.MANDATORY_NAME,
                                       self.DOCUMENT_TYPE_NAME: self.MANDATORY_NAME,
                                       self.DOCUMENT_NUMBER_NAME: self.MANDATORY_NAME,
                                       self.GENDER_NAME: self.MANDATORY_NAME,
                                       self.BIRTHDATE_NAME: self.MANDATORY_NAME,
                                       self.MAIL_NAME: self.OPTIONAL_NAME,
                                       self.CATEGORY_NAME: self.OPTIONAL_NAME,
                                       self.AFFILIATION_NAME: self.OPTIONAL_NAME,
                                       self.NATIONALITY_NAME: self.OPTIONAL_NAME,
                                       self.PROFESSION_NAME: self.OPTIONAL_NAME,
                                       self.COMPANY_NAME: self.OPTIONAL_NAME,
                                       self.CITY_NAME: self.OPTIONAL_NAME}
        self.expected_applicable_types = {}
        self.expected_default_values = {}

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.OPTIONALITY_NAME] = cls.OPTIONAL_NAME
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        id_not_existent_client = self.expected_ids[CLIENT_ENTITY_NAME] + 1
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/".format(id_not_existent_client),
                                      expected_code=404)
        validate_error(self, results, CLIENT_DOES_NOT_EXISTS_CODE)

    def test_default_locations_fields_optionality_view(self):
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_attempt_to_change_optionality_of_non_existent_field(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "INVALID FIELD"),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=404)
        validate_error(self, results, OPTIONALITY_FIELD_DOES_NOT_EXISTS_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_to_change_first_name_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.FIRST_NAME_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_to_change_last_name_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LAST_NAME_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_to_change_document_type_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DOCUMENT_TYPE_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_to_change_document_number_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DOCUMENT_NUMBER_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_to_change_gender_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.GENDER_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_to_change_birthdate_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BIRTHDATE_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_invalid_optionality(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: "INVALID_OPTIONALITY",
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_OPTIONALITY_CODE)

    def test_change_mail_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.MAIL_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_MAIL,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_mandatory_and_create_person_with_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_mail_optionality_to_mandatory_and_try_create_person_without_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_mail_optionality_to_mandatory_and_update_person_with_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_mail_optionality_to_mandatory_and_try_update_person_without_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_mail_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.MAIL_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_MAIL,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.MAIL_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.MAIL_NAME] = self.TEST_PERSON_MAIL
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_optional_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_optional_and_create_person_with_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_mail_optionality_to_optional_and_create_person_without_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        create_test_person(self, create_new_person=True)

    def test_change_mail_optionality_to_optional_with_default_value_and_create_person_with_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"prefix" + self.TEST_PERSON_MAIL,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_mail_optionality_to_optional_with_default_value_and_create_person_without_mail(self):
        default_value = self.TEST_PERSON_MAIL
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.MAIL_NAME])

    def test_change_mail_optionality_to_optional_and_update_person_with_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_mail_optionality_to_optional_and_update_person_without_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        update_test_person(self, id_person)

    def test_change_mail_optionality_to_optional_with_default_value_and_update_person_with_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"prefix" + self.TEST_PERSON_MAIL,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_mail_optionality_to_optional_with_default_value_and_update_person_without_mail(self):
        default_value = u"prefix" + self.TEST_PERSON_MAIL
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.MAIL_NAME])

    def test_change_mail_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.MAIL_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_MAIL,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_forbidden_and_try_create_person_with_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_mail_optionality_to_forbidden_and_create_person_without_mail(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        create_test_person(self, create_new_person=True)

    def test_change_mail_optionality_to_forbidden_and_try_update_person_with_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_mail_optionality_to_forbidden_and_update_person_without_mail(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_MAIL = None
        update_test_person(self, id_person)

    def test_change_category_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CATEGORY_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_category_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CATEGORY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_CATEGORY,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_category_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CATEGORY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_category_optionality_to_mandatory_and_create_person_with_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_category_optionality_to_mandatory_and_try_create_person_without_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_category_optionality_to_mandatory_and_update_person_with_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_category_optionality_to_mandatory_and_try_update_person_without_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_category_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CATEGORY_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_category_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_CATEGORY,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CATEGORY_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.CATEGORY_NAME] = self.TEST_PERSON_CATEGORY
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_category_optionality_to_optional_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CATEGORY_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_category_optionality_to_optional_and_create_person_with_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_category_optionality_to_optional_and_create_person_without_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        create_test_person(self, create_new_person=True)

    def test_change_category_optionality_to_optional_with_default_value_and_create_person_with_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"B",
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_category_optionality_to_optional_with_default_value_and_create_person_without_category(self):
        default_value = self.TEST_PERSON_CATEGORY.upper()
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.CATEGORY_NAME])

    def test_change_category_optionality_to_optional_and_update_person_with_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_category_optionality_to_optional_and_update_person_without_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        update_test_person(self, id_person)

    def test_change_category_optionality_to_optional_with_default_value_and_update_person_with_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"B",
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_category_optionality_to_optional_with_default_value_and_update_person_without_category(self):
        default_value = self.TEST_PERSON_CATEGORY.upper()
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.CATEGORY_NAME])

    def test_change_category_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CATEGORY_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_category_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CATEGORY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_CATEGORY,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_category_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CATEGORY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_category_optionality_to_forbidden_and_try_create_person_with_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_category_optionality_to_forbidden_and_create_person_without_category(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        create_test_person(self, create_new_person=True)

    def test_change_category_optionality_to_forbidden_and_try_update_person_with_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_category_optionality_to_forbidden_and_update_person_without_category(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CATEGORY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CATEGORY = None
        update_test_person(self, id_person)

    def test_change_affiliation_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.AFFILIATION_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_affiliation_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.AFFILIATION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_AFFILIATION,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_affiliation_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.AFFILIATION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_affiliation_optionality_to_mandatory_and_create_person_with_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_affiliation_optionality_to_mandatory_and_try_create_person_without_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_affiliation_optionality_to_mandatory_and_update_person_with_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_affiliation_optionality_to_mandatory_and_try_update_person_without_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_affiliation_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.AFFILIATION_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_affiliation_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_AFFILIATION,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.AFFILIATION_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.AFFILIATION_NAME] = self.TEST_PERSON_AFFILIATION
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_affiliation_optionality_to_optional_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.AFFILIATION_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_affiliation_optionality_to_optional_and_create_person_with_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_affiliation_optionality_to_optional_and_create_person_without_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        create_test_person(self, create_new_person=True)

    def test_change_affiliation_optionality_to_optional_with_default_value_and_create_person_with_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"BENEFICIARIO",
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_affiliation_optionality_to_optional_with_default_value_and_create_person_without_affiliation(self):
        default_value = self.TEST_PERSON_AFFILIATION.upper()
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.AFFILIATION_NAME])

    def test_change_affiliation_optionality_to_optional_and_update_person_with_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_affiliation_optionality_to_optional_and_update_person_without_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        update_test_person(self, id_person)

    def test_change_affiliation_optionality_to_optional_with_default_value_and_update_person_with_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"BENEFICIARIO",
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_affiliation_optionality_to_optional_with_default_value_and_update_person_without_affiliation(self):
        default_value = self.TEST_PERSON_AFFILIATION.upper()
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.AFFILIATION_NAME])

    def test_change_affiliation_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.AFFILIATION_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_affiliation_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.AFFILIATION_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_AFFILIATION,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_affiliation_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.AFFILIATION_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_affiliation_optionality_to_forbidden_and_try_create_person_with_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_affiliation_optionality_to_forbidden_and_create_person_without_affiliation(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        create_test_person(self, create_new_person=True)

    def test_change_affiliation_optionality_to_forbidden_and_try_update_person_with_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_affiliation_optionality_to_forbidden_and_update_person_without_affiliation(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.AFFILIATION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_AFFILIATION = None
        update_test_person(self, id_person)

    def test_change_nationality_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.NATIONALITY_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_nationality_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.NATIONALITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_NATIONALITY,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_nationality_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.NATIONALITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_nationality_optionality_to_mandatory_and_create_person_with_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_nationality_optionality_to_mandatory_and_try_create_person_without_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_nationality_optionality_to_mandatory_and_update_person_with_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_nationality_optionality_to_mandatory_and_try_update_person_without_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_nationality_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.NATIONALITY_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_nationality_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_NATIONALITY,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.NATIONALITY_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.NATIONALITY_NAME] = self.TEST_PERSON_NATIONALITY
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_nationality_optionality_to_optional_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.NATIONALITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_nationality_optionality_to_optional_and_create_person_with_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_nationality_optionality_to_optional_and_create_person_without_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        create_test_person(self, create_new_person=True)

    def test_change_nationality_optionality_to_optional_with_default_value_and_create_person_with_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Venezlano",
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_nationality_optionality_to_optional_with_default_value_and_create_person_without_nationality(self):
        default_value = self.TEST_PERSON_NATIONALITY.upper()
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.NATIONALITY_NAME])

    def test_change_nationality_optionality_to_optional_and_update_person_with_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_nationality_optionality_to_optional_and_update_person_without_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        update_test_person(self, id_person)

    def test_change_nationality_optionality_to_optional_with_default_value_and_update_person_with_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Venezolano",
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_nationality_optionality_to_optional_with_default_value_and_update_person_without_nationality(self):
        default_value = self.TEST_PERSON_NATIONALITY.upper()
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.NATIONALITY_NAME])

    def test_change_nationality_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.NATIONALITY_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_nationality_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.NATIONALITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_NATIONALITY,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_nationality_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.NATIONALITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_nationality_optionality_to_forbidden_and_try_create_person_with_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_nationality_optionality_to_forbidden_and_create_person_without_nationality(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        create_test_person(self, create_new_person=True)

    def test_change_nationality_optionality_to_forbidden_and_try_update_person_with_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_nationality_optionality_to_forbidden_and_update_person_without_nationality(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.NATIONALITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_NATIONALITY = None
        update_test_person(self, id_person)

    def test_change_profession_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.PROFESSION_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_profession_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PROFESSION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_PROFESSION,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_profession_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PROFESSION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_profession_optionality_to_mandatory_and_create_person_with_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_profession_optionality_to_mandatory_and_try_create_person_without_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_profession_optionality_to_mandatory_and_update_person_with_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_profession_optionality_to_mandatory_and_try_update_person_without_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_profession_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.PROFESSION_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_profession_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_PROFESSION,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.PROFESSION_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.PROFESSION_NAME] = self.TEST_PERSON_PROFESSION
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_profession_optionality_to_optional_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PROFESSION_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_profession_optionality_to_optional_and_create_person_with_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_profession_optionality_to_optional_and_create_person_without_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        create_test_person(self, create_new_person=True)

    def test_change_profession_optionality_to_optional_with_default_value_and_create_person_with_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Medico",
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_profession_optionality_to_optional_with_default_value_and_create_person_without_profession(self):
        default_value = self.TEST_PERSON_PROFESSION.upper()
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.PROFESSION_NAME])

    def test_change_profession_optionality_to_optional_and_update_person_with_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_profession_optionality_to_optional_and_update_person_without_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        update_test_person(self, id_person)

    def test_change_profession_optionality_to_optional_with_default_value_and_update_person_with_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Medico",
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_profession_optionality_to_optional_with_default_value_and_update_person_without_profession(self):
        default_value = self.TEST_PERSON_PROFESSION.upper()
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.PROFESSION_NAME])

    def test_change_profession_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.PROFESSION_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_profession_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PROFESSION_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_PROFESSION,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_profession_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PROFESSION_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_profession_optionality_to_forbidden_and_try_create_person_with_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_profession_optionality_to_forbidden_and_create_person_without_profession(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        create_test_person(self, create_new_person=True)

    def test_change_profession_optionality_to_forbidden_and_try_update_person_with_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_profession_optionality_to_forbidden_and_update_person_without_profession(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PROFESSION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_PROFESSION = None
        update_test_person(self, id_person)

    def test_change_company_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.COMPANY_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_company_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.COMPANY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_COMPANY,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_company_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.COMPANY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_company_optionality_to_mandatory_and_create_person_with_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_company_optionality_to_mandatory_and_try_create_person_without_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_company_optionality_to_mandatory_and_update_person_with_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_company_optionality_to_mandatory_and_try_update_person_without_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_company_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.COMPANY_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_company_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_COMPANY,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.COMPANY_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.COMPANY_NAME] = self.TEST_PERSON_COMPANY
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_company_optionality_to_optional_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.COMPANY_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_company_optionality_to_optional_and_create_person_with_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_company_optionality_to_optional_and_create_person_without_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        create_test_person(self, create_new_person=True)

    def test_change_company_optionality_to_optional_with_default_value_and_create_person_with_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Default company",
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_company_optionality_to_optional_with_default_value_and_create_person_without_company(self):
        default_value = self.TEST_PERSON_COMPANY.upper()
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.COMPANY_NAME])

    def test_change_company_optionality_to_optional_and_update_person_with_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_company_optionality_to_optional_and_update_person_without_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        update_test_person(self, id_person)

    def test_change_company_optionality_to_optional_with_default_value_and_update_person_with_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Default company",
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_company_optionality_to_optional_with_default_value_and_update_person_without_company(self):
        default_value = self.TEST_PERSON_COMPANY.upper()
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.COMPANY_NAME])

    def test_change_company_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.COMPANY_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_company_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.COMPANY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_COMPANY,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_company_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.COMPANY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_company_optionality_to_forbidden_and_try_create_person_with_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_company_optionality_to_forbidden_and_create_person_without_company(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        create_test_person(self, create_new_person=True)

    def test_change_company_optionality_to_forbidden_and_try_update_person_with_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_company_optionality_to_forbidden_and_update_person_without_company(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.COMPANY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_COMPANY = None
        update_test_person(self, id_person)

    def test_change_city_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CITY_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_city_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_CITY_OF_RESIDENCE,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_city_optionality_to_mandatory_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_city_optionality_to_mandatory_and_create_person_with_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_city_optionality_to_mandatory_and_try_create_person_without_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_city_optionality_to_mandatory_and_update_person_with_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_city_optionality_to_mandatory_and_try_update_person_without_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        update_test_person(self, id_person, expected_code=400)

    def test_change_city_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CITY_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_city_optionality_to_optional_with_default_value(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: self.TEST_PERSON_CITY_OF_RESIDENCE,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CITY_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.CITY_NAME] = self.TEST_PERSON_CITY_OF_RESIDENCE
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_city_optionality_to_optional_with_applicable_types(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: []},
                            expected_code=400)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_city_optionality_to_optional_and_create_person_with_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_city_optionality_to_optional_and_create_person_without_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        create_test_person(self, create_new_person=True)

    def test_change_city_optionality_to_optional_with_default_value_and_create_person_with_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Cali",
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True)

    def test_change_city_optionality_to_optional_with_default_value_and_create_person_without_city(self):
        default_value = self.TEST_PERSON_COMPANY.upper()
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        id_person = create_test_person(self, create_new_person=True, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.CITY_NAME])

    def test_change_city_optionality_to_optional_and_update_person_with_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_city_optionality_to_optional_and_update_person_without_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        update_test_person(self, id_person)

    def test_change_city_optionality_to_optional_with_default_value_and_update_person_with_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: u"Cali",
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person)

    def test_change_city_optionality_to_optional_with_default_value_and_update_person_without_city(self):
        default_value = self.TEST_PERSON_CITY_OF_RESIDENCE.upper()
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_value,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        update_test_person(self, id_person, validate_results_on_success=False)
        person = self.do_get_request("/clients/{0}/persons/{1}/".
                                     format(self.expected_ids[CLIENT_ENTITY_NAME],
                                            id_person))

        self.assertEqual(default_value, person[self.CITY_NAME])

    def test_change_city_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.CITY_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_city_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.TEST_PERSON_CITY_OF_RESIDENCE,
                                            self.APPLICABLE_TYPES_NAME: None},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_city_optionality_to_forbidden_with_applicable_types(self):
        results = self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.CITY_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: []},
                                      expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/person-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_city_optionality_to_forbidden_and_try_create_person_with_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        create_test_person(self, create_new_person=True, expected_code=400)

    def test_change_city_optionality_to_forbidden_and_create_person_without_city(self):
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        create_test_person(self, create_new_person=True)

    def test_change_city_optionality_to_forbidden_and_try_update_person_with_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        update_test_person(self, id_person, expected_code=400)

    def test_change_city_optionality_to_forbidden_and_update_person_without_city(self):
        id_person = create_test_person(self, create_new_person=True)
        self.do_put_request("/clients/{0}/person-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.CITY_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.TEST_PERSON_CITY_OF_RESIDENCE = None
        update_test_person(self, id_person)

    # noinspection PyTypeChecker
    def check_optionality_results(self, results):
        self.assertEqual(len(self.expected_optionalities), len(results))
        for result in results:
            field_name = result[self.FIELD_NAME_NAME]
            optionality = result[self.OPTIONALITY_NAME]
            self.assertEqual(self.expected_optionalities[field_name], optionality)
            self.assertEqual(self.PERSONS_VIEW_NAME, result[self.VIEW_NAME])

            if optionality == self.MANDATORY_NAME or optionality == self.OPTIONAL_NAME:
                expected_types = self.expected_applicable_types.get(field_name, None)
                if expected_types is None or len(expected_types) == 0:
                    self.assertTrue(self.APPLICABLE_TYPES_NAME not in result)
                else:
                    self.assertEqual(set(expected_types), set(result[self.APPLICABLE_TYPES_NAME]))
            else:
                self.assertTrue(self.APPLICABLE_TYPES_NAME not in result)

            expeted_default_value = self.expected_default_values.get(field_name, None)
            if expeted_default_value is None:
                self.assertTrue(self.DEFAULT_VALUE_NAME not in result)
            else:
                self.assertEqual(expeted_default_value, result[self.DEFAULT_VALUE_NAME])

    def test_check_permissions_for_get_all_optionalities(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_optionalities(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.original_entities = [{self.FIELD_NAME_NAME: self.CATEGORY_NAME}]
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)

if __name__ == '__main__':
    unittest.main()
