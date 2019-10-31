# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    PERSON_DOES_NOT_EXISTS_CODE, PERSONS_RELATIONSHIPS_INVALID_RELATIONSHIPS_CODE, \
    PERSONS_RELATIONSHIP_ALREADY_EXIST_CODE, PERSONS_RELATIONSHIP_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import PERSON_ENTITY_NAME, \
    create_test_person


class PersonRelationshipViewTestCase(FlaskClientBaseTestCase):
    NUMBER_OF_ENTITIES = 1
    ID_NAME = u"id"
    PERSON_ID_NAME = u"id-person"
    RELATIONSHIP_NAME = u"relationship"

    PARENT_RELATIONSHIP = u"PARENT"
    CHILD_RELATIONSHIP = u"CHILD"
    SPOUSE_RELATIONSHIP = u"SPOUSE"
    SIBLINGS_RELATIONSHIP = u"SIBLING"
    UNKNOWN_RELATIONSHIP = u"UNKNOWN"

    RELATIONSHIPS_SYMMETRY = {PARENT_RELATIONSHIP: CHILD_RELATIONSHIP,
                              CHILD_RELATIONSHIP: PARENT_RELATIONSHIP,
                              SPOUSE_RELATIONSHIP: SPOUSE_RELATIONSHIP,
                              SIBLINGS_RELATIONSHIP: SIBLINGS_RELATIONSHIP,
                              UNKNOWN_RELATIONSHIP: UNKNOWN_RELATIONSHIP}

    RESOURCE_URL = u"/clients/{0}/persons/{1}/relationships/"
    ENTITY_DOES_NOT_EXISTS_CODE = PERSONS_RELATIONSHIP_DOES_NOT_EXISTS_CODE

    ATTRIBUTES_NAMES_BY_FIELDS = {PERSON_ID_NAME: "TEST_PERSON_RELATIONSHIP_ID_PERSON",
                                  RELATIONSHIP_NAME: "TEST_PERSON_RELATIONSHIP_RELATIONSHIP"}

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_PERSON_FIRST_NAME = "Test name"
    TEST_PERSON_LAST_NAME = "Test last name"
    TEST_PERSON_DOCUMENT_TYPE = "CC"
    TEST_PERSON_DOCUMENT_NUMBER = "1"
    TEST_PERSON_MAIL = "mail@test.com"
    TEST_PERSON_GENDER = "m"
    TEST_PERSON_BIRTHDATE = "19900101"
    TEST_PERSON_TYPE = "Adulto"
    TEST_PERSON_CATEGORY = "A"
    TEST_PERSON_AFFILIATION = "Cotizante"
    TEST_PERSON_NATIONALITY = "Colombiano"
    TEST_PERSON_PROFESSION = "Ingeniero"
    TEST_PERSON_CITY_OF_RESIDENCE = "Bogotá"
    TEST_PERSON_COMPANY = "Empresa"

    ENTITY_NAME = 'persons-relationships'

    def setUp(self):
        super(PersonRelationshipViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)

    def create_test_data_for_relationship(self, relationship):
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        self.id_person_destination = create_test_person(self, create_new_person=True)
        self.assign_field_value(self.PERSON_ID_NAME, self.id_person_destination)
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        self.relationship = relationship
        self.assign_field_value(self.RELATIONSHIP_NAME, relationship)
        create_test_person(self, create_new_person=True)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.RELATIONSHIP_NAME] = cls.UNKNOWN_RELATIONSHIP
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME, PERSON_ENTITY_NAME]

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        request_values[cls.ID_NAME] = request_values.get(cls.PERSON_ID_NAME)
        request_values[cls.PERSON_ID_NAME] = None
        return request_values

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_non_existent_person(self):
        self.expected_ids[PERSON_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE)

    def test_empty_person_relationships_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_persons_relationships(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self._check_symmetric_relationship(True)

    def test_create_valid_persons_relationships_with_every_possible_relationship(self):
        for relationship in self.RELATIONSHIPS_SYMMETRY:
            self.create_test_data_for_relationship(relationship)
            self.do_create_requests()
            self._check_symmetric_relationship(True)
            self.clean_test_data()

    def test_try_create_invalid_persons_without_relationship(self):
        self.create_test_data_for_relationship(None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSONS_RELATIONSHIPS_INVALID_RELATIONSHIPS_CODE)
        self._check_symmetric_relationship(False)

    def test_try_create_invalid_persons_with_invalid_relationship(self):
        self.create_test_data_for_relationship(u"Invalid relationship")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSONS_RELATIONSHIPS_INVALID_RELATIONSHIPS_CODE)
        self._check_symmetric_relationship(False)

    def test_try_create_invalid_persons_with_non_existent_origin_person(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.expected_ids[PERSON_ENTITY_NAME] += 1
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE, do_get_and_check_results=False)
        self._check_symmetric_relationship(False)

    def test_try_create_invalid_persons_with_non_existent_destination_person(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.assign_field_value(self.PERSON_ID_NAME, self.expected_ids[PERSON_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE, do_get_and_check_results=False)
        self._check_symmetric_relationship(False)

    def test_try_create_valid_duplicated_persons_relationships(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self.do_create_requests(expected_code=400, expected_internal_code=PERSONS_RELATIONSHIP_ALREADY_EXIST_CODE,
                                do_get_and_check_results=False)
        self._check_symmetric_relationship(True)

    def test_try_create_valid_duplicated_persons_relationships_changing_relationship(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self.assign_field_value(self.RELATIONSHIP_NAME, self.SIBLINGS_RELATIONSHIP)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSONS_RELATIONSHIP_ALREADY_EXIST_CODE,
                                do_get_and_check_results=False)
        self._check_symmetric_relationship(True)

    def test_try_create_valid_duplicated_persons_relationships_changing_relationship_direction(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        original_id_origin = self.expected_ids[PERSON_ENTITY_NAME]
        self.do_create_requests()
        self.assign_field_value(self.PERSON_ID_NAME, original_id_origin)
        self.expected_ids[PERSON_ENTITY_NAME] = self.id_person_destination
        self.do_create_requests(expected_code=400, expected_internal_code=PERSONS_RELATIONSHIP_ALREADY_EXIST_CODE,
                                do_get_and_check_results=False)
        self.expected_ids[PERSON_ENTITY_NAME] = original_id_origin
        self._check_symmetric_relationship(True)

    def test_delete_valid_person_relationships(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self.do_delete_requests()
        self._check_symmetric_relationship(False)

    def test_try_delete_invalid_person_relationships_with_non_existent_client(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)
        self.expected_ids[CLIENT_ENTITY_NAME] -= 1
        self._check_symmetric_relationship(True)

    def test_try_delete_invalid_person_relationships_with_non_existent_origin_person(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self.expected_ids[PERSON_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)
        self.expected_ids[PERSON_ENTITY_NAME] -= 1
        self._check_symmetric_relationship(True)

    def test_try_delete_invalid_person_relationships_with_non_existent_destination_person(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)
        self._check_symmetric_relationship(True)

    def _check_symmetric_relationship(self, was_successful):
        expected_length = 0
        new_id_person_destination = self.expected_ids[PERSON_ENTITY_NAME]
        self.expected_ids[PERSON_ENTITY_NAME] = self.id_person_destination
        self.clean_test_data()
        if was_successful:
            symmetric_relationship = self.RELATIONSHIPS_SYMMETRY[self.relationship]
            self.add_data_value(self.ENTITY_NAME, self.ID_NAME, new_id_person_destination)
            self.add_data_value(self.ENTITY_NAME, self.RELATIONSHIP_NAME, symmetric_relationship)
            expected_length = self.NUMBER_OF_ENTITIES
        self.request_all_resources_and_check_result(expected_length)

    def test_check_permissions_for_create_persons_relationships(self):
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER,\
            CLIENT_PROMOTER_USER
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations, do_delete_after_success=True)

    def test_check_permissions_for_get_all_persons_relationships(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_person_relationship(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_persons_relationships(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.create_test_data_for_relationship(self.UNKNOWN_RELATIONSHIP)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)


PERSON_RELATIONSHIP_ENTITY_NAME = PersonRelationshipViewTestCase.ENTITY_NAME


if __name__ == '__main__':
    unittest.main()
