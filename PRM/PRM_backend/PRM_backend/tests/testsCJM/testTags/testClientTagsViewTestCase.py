# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, SUPPORTED_TAGS_DOES_NOT_EXISTS_CODE, \
    CLIENT_TAGS_DOES_NOT_EXISTS_CODE, CLIENT_TAGS_INVALID_NAME_CODE, CLIENT_TAGS_ALREADY_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testsCJM.testTags.testSuppotedTagsViewTestCase import create_test_supported_tag, calculate_default_tag_values


class ClientTagsViewTestCase(FlaskClientBaseTestCase):
    TAG_NAME_NAME = u"name"
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = CLIENT_TAGS_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/tags/"

    ATTRIBUTES_NAMES_BY_FIELDS = {TAG_NAME_NAME: "TEST_CLIENT_TAG_NAME"}

    ENTITY_NAME = 'client-tags'
    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_TAG_NAME = None
    TEST_TAG_TOTAL_SIZE = 1024

    TAG_NAME_TEMPLATE = u"Tag {0}"

    def setUp(self):
        super(ClientTagsViewTestCase, self).setUp()
        create_test_client(self)

        for tag_number in range(self.NUMBER_OF_ENTITIES):
            self.TEST_TAG_NAME = self.TAG_NAME_TEMPLATE.format(tag_number)
            create_test_supported_tag(self, create_new_tag=True)

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.TAG_NAME_NAME] = cls.TAG_NAME_TEMPLATE
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        return calculate_default_tag_values(request_values, cls.TEST_TAG_TOTAL_SIZE)

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0,
                                                    expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_tags_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_tags(self):
        self.do_create_requests()

    def test_create_invalid_duplicated_tags(self):
        self.do_create_requests()
        self.clean_test_data()
        self.do_create_requests(expected_code=400,
                                check_results_as_list=False,
                                do_get_and_check_results=False,
                                expected_internal_code=CLIENT_TAGS_ALREADY_EXISTS_CODE)

    def test_create_invalid_tags_without_name(self):
        self.assign_field_value(self.TAG_NAME_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CLIENT_TAGS_INVALID_NAME_CODE)

    def test_create_invalid_tags_with_empty_name(self):
        self.assign_field_value(self.TAG_NAME_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CLIENT_TAGS_INVALID_NAME_CODE)

    def test_create_invalid_tags_with_non_existent_tag_name(self):
        self.assign_field_value(self.TAG_NAME_NAME, u"INVALID_TAG")
        self.do_create_requests(expected_code=404,
                                expected_internal_code=SUPPORTED_TAGS_DOES_NOT_EXISTS_CODE)

    def test_delete_valid_tags(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_delete_invalid_non_existent_tags(self):
        self.do_create_requests()

        self.change_ids_to_non_existent_entities()

        self.do_delete_requests(expected_code=404,
                                expected_internal_code=CLIENT_TAGS_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_tags(self):
        ClientTagsViewTestCase.CHECK_BY_GET = False
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations,
                                      do_delete_after_success=True)
        ClientTagsViewTestCase.CHECK_BY_GET = True

    def test_check_permissions_for_get_all_tags(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_tag(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_tags(self):
        ClientTagsViewTestCase.CHECK_BY_GET = False
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)
        ClientTagsViewTestCase.CHECK_BY_GET = True


CLIENT_TAGS_ENTITY_NAME = ClientTagsViewTestCase.ENTITY_NAME


def create_test_client_tag(test_class, create_new_tag=False):
    return ClientTagsViewTestCase.create_sample_entity_for_another_class(test_class, create_new_tag)

if __name__ == '__main__':
    unittest.main()
