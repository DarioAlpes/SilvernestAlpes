# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import SUPPORTED_TAGS_DOES_NOT_EXISTS_CODE, SUPPORTED_TAGS_INVALID_NAME_CODE, \
    SUPPORTED_TAGS_INVALID_TOTAL_SIZE_CODE, SUPPORTED_TAGS_ALREADY_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client


class SupportedTagsViewTestCase(FlaskClientBaseTestCase):
    TAG_NAME_NAME = u"name"
    TOTAL_SIZE_NAME = u"total-size"
    HEADER_SIZE_NAME = u"header-size"
    MIN_HEADER_SIZE_NAME = u"min-header-size"
    AVAILABLE_SIZE_NAME = u"available-size"
    NUMBER_SKU_CONSUMPTIONS_NAME = u"max-amount-sku-consumptions"
    NUMBER_SKU_CATEGORY_CONSUMPTIONS_NAME = u"max-amount-sku-category-consumptions"
    NUMBER_MONEY_CONSUMPTIONS_NAME = u"max-money-consumptions"
    NUMBER_ACCESSES_NAME = u"max-accesses"
    SKU_CONSUMPTIONS_SIZE_NAME = u"amount-sku-consumption-size"
    SKU_CATEGORY_CONSUMPTIONS_SIZE_NAME = u"amount-sku-category-consumption-size"
    MONEY_CONSUMPTIONS_SIZE_NAME = u"money-consumption-size"
    ACCESSES_SIZE_NAME = u"access-size"
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = SUPPORTED_TAGS_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/supported-tags/"

    ATTRIBUTES_NAMES_BY_FIELDS = {TAG_NAME_NAME: "TEST_TAG_NAME",
                                  TOTAL_SIZE_NAME: "TEST_TAG_TOTAL_SIZE"}

    ENTITY_NAME = 'tags'
    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    def setUp(self):
        super(SupportedTagsViewTestCase, self).setUp()
        create_test_client(self)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.TOTAL_SIZE_NAME] = 1024
        return values

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.TAG_NAME_NAME] = u"Tag {0}"
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return []

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        byte_size = request_values.get(SupportedTagsViewTestCase.TOTAL_SIZE_NAME)
        return calculate_default_tag_values(request_values, byte_size)

    def test_empty_tags_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_tags(self):
        self.do_create_requests()

    def test_create_valid_tags_with_size_equals_to_header_size(self):
        self.assign_field_value(self.TOTAL_SIZE_NAME, 8 * 2 + 1)
        self.do_create_requests()

    def test_create_valid_tags_with_header_size_bigger_than_min_header_size_but_smaller_than_full_header_size(self):
        self.assign_field_value(self.TOTAL_SIZE_NAME, 8 * 2 + 2)
        self.do_create_requests()

    def test_create_invalid_duplicated_tags(self):
        self.do_create_requests()
        self.clean_test_data()
        self.do_create_requests(expected_code=400,
                                check_results_as_list=False,
                                do_get_and_check_results=False,
                                expected_internal_code=SUPPORTED_TAGS_ALREADY_EXISTS_CODE)

    def test_create_invalid_tags_without_name(self):
        self.assign_field_value(self.TAG_NAME_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SUPPORTED_TAGS_INVALID_NAME_CODE)

    def test_create_invalid_tags_with_empty_name(self):
        self.assign_field_value(self.TAG_NAME_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SUPPORTED_TAGS_INVALID_NAME_CODE)

    def test_create_invalid_tags_without_size(self):
        self.assign_field_value(self.TOTAL_SIZE_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SUPPORTED_TAGS_INVALID_TOTAL_SIZE_CODE)

    def test_create_invalid_tags_with_negative_size(self):
        self.assign_field_value(self.TOTAL_SIZE_NAME, -1)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SUPPORTED_TAGS_INVALID_TOTAL_SIZE_CODE)

    def test_create_invalid_tags_with_zero_size(self):
        self.assign_field_value(self.TOTAL_SIZE_NAME, -1)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SUPPORTED_TAGS_INVALID_TOTAL_SIZE_CODE)

    def test_create_invalid_tags_with_header_size_minus_one(self):
        self.assign_field_value(self.TOTAL_SIZE_NAME, 8 * 2)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SUPPORTED_TAGS_INVALID_TOTAL_SIZE_CODE)

    def test_delete_valid_tags(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_delete_invalid_non_existent_tags(self):
        self.do_create_requests()

        self.change_ids_to_non_existent_entities()

        self.do_delete_requests(expected_code=404,
                                expected_internal_code=SUPPORTED_TAGS_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_tags(self):
        allowed_roles = {}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_tags(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_tag(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_tags(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)


SUPPORTED_TAGS_ENTITY_NAME = SupportedTagsViewTestCase.ENTITY_NAME


def calculate_default_tag_values(request_values, byte_size):
    request_values[SupportedTagsViewTestCase.HEADER_SIZE_NAME] = 8 * 4 + 4 * 4 + 1
    request_values[SupportedTagsViewTestCase.MIN_HEADER_SIZE_NAME] = 8 * 2 + 1

    request_values[SupportedTagsViewTestCase.SKU_CONSUMPTIONS_SIZE_NAME] = 12
    request_values[SupportedTagsViewTestCase.SKU_CATEGORY_CONSUMPTIONS_SIZE_NAME] = 12
    request_values[SupportedTagsViewTestCase.MONEY_CONSUMPTIONS_SIZE_NAME] = 16
    request_values[SupportedTagsViewTestCase.ACCESSES_SIZE_NAME] = 12

    if byte_size is not None:

        request_values[SupportedTagsViewTestCase.AVAILABLE_SIZE_NAME] = max(0, byte_size - request_values[SupportedTagsViewTestCase.HEADER_SIZE_NAME])

        request_values[SupportedTagsViewTestCase.NUMBER_SKU_CONSUMPTIONS_NAME] = request_values[SupportedTagsViewTestCase.AVAILABLE_SIZE_NAME] // request_values[SupportedTagsViewTestCase.SKU_CONSUMPTIONS_SIZE_NAME]
        request_values[SupportedTagsViewTestCase.NUMBER_SKU_CATEGORY_CONSUMPTIONS_NAME] = request_values[SupportedTagsViewTestCase.AVAILABLE_SIZE_NAME] // request_values[SupportedTagsViewTestCase.SKU_CATEGORY_CONSUMPTIONS_SIZE_NAME]
        request_values[SupportedTagsViewTestCase.NUMBER_MONEY_CONSUMPTIONS_NAME] = request_values[SupportedTagsViewTestCase.AVAILABLE_SIZE_NAME] // request_values[SupportedTagsViewTestCase.MONEY_CONSUMPTIONS_SIZE_NAME]
        request_values[SupportedTagsViewTestCase.NUMBER_ACCESSES_NAME] = request_values[SupportedTagsViewTestCase.AVAILABLE_SIZE_NAME] // request_values[SupportedTagsViewTestCase.ACCESSES_SIZE_NAME]

    return request_values


def create_test_supported_tag(test_class, create_new_tag=False):
    return SupportedTagsViewTestCase.create_sample_entity_for_another_class(test_class, create_new_tag)

if __name__ == '__main__':
    unittest.main()
