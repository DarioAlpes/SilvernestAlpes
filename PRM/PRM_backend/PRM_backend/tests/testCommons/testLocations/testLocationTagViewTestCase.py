# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    LOCATION_TAG_INVALID_NAME_CODE, LOCATION_TAG_ALREADY_EXISTS_CODE, LOCATION_TAG_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client


class LocationTagViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = None
    TAG_NAME = u"name"

    ENTITY_DOES_NOT_EXISTS_CODE = LOCATION_TAG_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/location-tags/"

    ATTRIBUTES_NAMES_BY_FIELDS = {TAG_NAME: "TEST_LOCATION_TAG_NAME"}

    ENTITY_NAME = 'location-tags'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    def setUp(self):
        super(LocationTagViewTestCase, self).setUp()
        create_test_client(self)

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.TAG_NAME] = u"tag {0}"
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0,
                                                    expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_persons_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_tags(self):
        self.do_create_requests()

    def test_create_invalid_tags_with_inexistent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_create_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_create_invalid_duplicated_tags(self):
        self.do_create_requests()
        self.do_create_requests(expected_code=400, expected_internal_code=LOCATION_TAG_ALREADY_EXISTS_CODE)

    def test_create_invalid_tags_without_name(self):
        self.assign_field_value(self.TAG_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=LOCATION_TAG_INVALID_NAME_CODE)

    def test_create_invalid_currencies_with_empty_name(self):
        self.assign_field_value(self.TAG_NAME, u"")
        self.do_create_requests(expected_code=400, expected_internal_code=LOCATION_TAG_INVALID_NAME_CODE)

    def test_check_permissions_for_create_tags(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_tags(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)


LOCATION_TAGS_ENTITY_NAME = LocationTagViewTestCase.ENTITY_NAME


def create_test_location_tag(test_class, create_new_tag=False):
    return LocationTagViewTestCase.create_sample_entity_for_another_class(test_class, create_new_tag)

if __name__ == '__main__':
    unittest.main()
