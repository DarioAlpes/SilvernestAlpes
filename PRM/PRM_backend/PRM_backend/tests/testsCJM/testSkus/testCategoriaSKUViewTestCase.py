# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    SKU_CATEGORY_DOES_NOT_EXISTS_CODE, SKU_CATEGORY_INVALID_NAME_CODE, SKU_CATEGORY_INVALID_HIREARCHY_NAME_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME


# noinspection PyTypeChecker
class CategoriaSKUViewTestCase(FlaskClientBaseTestCase):
    SKU_CATEGORY_NAME_NAME = u"name"
    PARENT_NAME = u"id-parent-category"
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = SKU_CATEGORY_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/sku-categories/"

    ATTRIBUTES_NAMES_BY_FIELDS = {SKU_CATEGORY_NAME_NAME: "TEST_SKU_CATEGORY_NAME",
                                  PARENT_NAME: "TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID"}

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    ENTITY_NAME = 'SKU_Category'

    def setUp(self):
        super(CategoriaSKUViewTestCase, self).setUp()
        create_test_client(self)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.SKU_CATEGORY_NAME_NAME] = "Name"
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.SKU_CATEGORY_NAME_NAME] = "New name"
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0,
                                                    expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_sku_categories_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_sku_categories(self):
        self.do_create_requests()

    def test_create_valid_sku_categories_with_parent(self):
        self.do_create_requests()

        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests()

    def test_create_valid_sku_categories_with_parent_checking_children(self):
        self.do_create_requests()

        id_parent = self.expected_ids[CategoriaSKUViewTestCase.ENTITY_NAME]
        self.assign_field_value(self.PARENT_NAME, id_parent)
        self.do_create_requests()

        results = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              id_parent))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES * 2)

    def test_create_invalid_sku_categories_without_name(self):
        self.assign_field_value(self.SKU_CATEGORY_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_CATEGORY_INVALID_NAME_CODE)

    def test_create_invalid_sku_categories_without_empty_name(self):
        self.assign_field_value(self.SKU_CATEGORY_NAME_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_CATEGORY_INVALID_NAME_CODE)

    def test_create_invalid_sku_categories_with_invalid_parent(self):
        self.assign_field_value(self.PARENT_NAME, 1)
        self.do_create_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_update_valid_sku_categories(self):
        self.do_create_requests()
        self.do_update_requests()

    def test_update_valid_sku_categories_with_parent(self):
        self.do_create_requests()
        parent_category_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()

        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_ENTITIES)
        self.assign_field_value(self.PARENT_NAME, parent_category_id)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_ENTITIES)

    def test_update_valid_sku_categories_with_parent_checking_children(self):
        self.do_create_requests()
        parent_category_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()

        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_ENTITIES)
        self.assign_field_value(self.PARENT_NAME, parent_category_id)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_ENTITIES)
        results = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              parent_category_id))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, 1)

    def test_update_valid_sku_categories_checking_previously_unconected_subtrees(self):
        original_number_categories = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1

        self.do_create_requests()
        id_level_0 = self.expected_ids[self.ENTITY_NAME]

        self.assign_field_value(self.PARENT_NAME, id_level_0)
        self.do_create_requests()
        id_level_1 = self.expected_ids[self.ENTITY_NAME]

        self.assign_field_value(self.PARENT_NAME, None)
        self.do_create_requests()
        id_level_2 = self.expected_ids[self.ENTITY_NAME]

        self.assign_field_value(self.PARENT_NAME, id_level_2)
        self.do_create_requests()
        id_level_3 = self.expected_ids[self.ENTITY_NAME]

        results_0 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_0))
        self.assertEqual(2, len(results_0))

        results_1 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_1))
        self.assertEqual(1, len(results_1))

        results_2 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_2))
        self.assertEqual(2, len(results_2))

        results_3 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_3))
        self.assertEqual(1, len(results_3))

        self.original_entities = [self.do_get_request("/clients/{0}/sku-categories/{1}/"
                                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                              id_level_2))]

        self.assign_field_value(self.PARENT_NAME, id_level_1)
        self.do_update_requests(check_results_as_list=False, do_get_and_check_results=False)

        results_0 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_0))
        self.assertEqual(4, len(results_0))

        results_1 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_1))
        self.assertEqual(3, len(results_1))

        results_2 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_2))
        self.assertEqual(2, len(results_2))

        results_3 = self.do_get_request("/clients/{0}/sku-categories/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_3))
        self.assertEqual(1, len(results_3))

        self.NUMBER_OF_ENTITIES = original_number_categories

    def test_update_non_existent_sku_categories(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_update_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_update_invalid_sku_categories_without_name(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_CATEGORY_NAME_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_CATEGORY_INVALID_NAME_CODE)

    def test_update_invalid_sku_categories_with_empty_name(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_CATEGORY_NAME_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_CATEGORY_INVALID_NAME_CODE)

    def test_update_invalid_sku_categories_with_invalid_parent(self):
        self.do_create_requests()

        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME] + 1)
        self.do_update_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_update_invalid_sku_categories_with_id_parent_pointing_to_itself(self):
        original_number_categories = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()

        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_update_requests(expected_code=400,
                                expected_internal_code=SKU_CATEGORY_INVALID_HIREARCHY_NAME_CODE)
        self.NUMBER_OF_ENTITIES = original_number_categories

    def test_update_invalid_sku_categories_creating_four_level_cicle(self):
        original_number_categories = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests()
        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests()
        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests()

        self.assign_field_value(self.PARENT_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_update_requests(expected_code=400,
                                expected_internal_code=SKU_CATEGORY_INVALID_HIREARCHY_NAME_CODE)
        self.NUMBER_OF_ENTITIES = original_number_categories

    def test_create_sku_categories_on_multiple_clients(self):
        self.do_create_requests()

        self.clean_test_data()
        create_test_client(self, create_new_client=True)
        self.do_create_requests()

    def test_check_permissions_for_create_sku_categories(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_sku_categories(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_sku_categories(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_child_sku_categories(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "children/"
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_sku_categories(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)

SKU_CATEGORY_ENTITY_NAME = CategoriaSKUViewTestCase.ENTITY_NAME


def create_test_sku_category(test_class, create_new_category=False):
    return CategoriaSKUViewTestCase.create_sample_entity_for_another_class(test_class, create_new_category)

if __name__ == '__main__':
    unittest.main()
