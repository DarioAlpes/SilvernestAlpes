# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    SKU_CATEGORY_DOES_NOT_EXISTS_CODE, SKU_DOES_NOT_EXISTS_CODE, SKU_INVALID_NAME_CODE, SKU_INVALID_MEASURE_CODE, \
    SKU_INVALID_COST_CODE, SKU_INVALID_EAN_CODE_CODE, SKU_INVALID_TAX_RATE_CODE, SKU_INVALID_EXTERNAL_CODE_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME


class SkuViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"
    SKU_NAME_NAME = u"name"
    UNIT_OF_MEASURE_NAME = u"measure"
    COST_NAME = u"cost"
    EAN_CODE_NAME = u"ean-code"
    SKU_CATEGORY_ID_NAME = u"id-sku-category"
    TAX_RATE_NAME = u"tax-rate"
    EXTERNAL_CODE_NAME = u"external-code"

    ENTITY_DOES_NOT_EXISTS_CODE = SKU_CATEGORY_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/skus/"

    ATTRIBUTES_NAMES_BY_FIELDS = {SKU_NAME_NAME: "TEST_SKU_NAME",
                                  UNIT_OF_MEASURE_NAME: "TEST_SKU_MEASURE_UNIT",
                                  COST_NAME: "TEST_SKU_COST",
                                  EAN_CODE_NAME: "TEST_SKU_EAN_CODE",
                                  SKU_CATEGORY_ID_NAME: "TEST_SKU_CATEGORY_ID",
                                  TAX_RATE_NAME: "TEST_SKU_TAX_RATE",
                                  EXTERNAL_CODE_NAME: "TEST_SKU_EXTERNAL_CODE"}

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    ENTITY_NAME = 'skus'
    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    def setUp(self):
        super(SkuViewTestCase, self).setUp()
        create_test_client(self)
        create_test_sku_category(self)
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, self.expected_ids[SKU_CATEGORY_ENTITY_NAME])

        self.name_template = "Nombre{0}"
        self.measure_unit = "Unidad"
        self.cost = 100.5
        self.ean_code = None
        self.category_id = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.SKU_NAME_NAME] = "Name"
        values[cls.UNIT_OF_MEASURE_NAME] = "Unidad"
        values[cls.COST_NAME] = 100.5
        values[cls.TAX_RATE_NAME] = 15
        values[cls.EAN_CODE_NAME] = None
        values[cls.EXTERNAL_CODE_NAME] = "123456abc"
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.SKU_NAME_NAME] = "New name"
        values[cls.UNIT_OF_MEASURE_NAME] = "New unidad"
        values[cls.COST_NAME] = 200
        values[cls.TAX_RATE_NAME] = 13.0
        values[cls.EAN_CODE_NAME] = None
        values[cls.EXTERNAL_CODE_NAME] = "467def"
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_skus_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_skus(self):
        self.do_create_requests()

    def test_create_valid_skus_with_valid_ean_code_ean_13(self):
        self.assign_field_value(self.EAN_CODE_NAME, "1" * 13)
        self.do_create_requests()

    def test_create_valid_skus_with_valid_ean_code_upc_a(self):
        self.assign_field_value(self.EAN_CODE_NAME, "1" * 12)
        self.do_create_requests()

    def test_create_invalid_skus_with_invalid_ean_code_ean_13(self):
        self.assign_field_value(self.EAN_CODE_NAME, "a" * 13)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_EAN_CODE_CODE)

    def test_create_invalid_skus_with_invalid_ean_code_upc_a(self):
        self.assign_field_value(self.EAN_CODE_NAME, "a" * 12)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_EAN_CODE_CODE)

    def test_create_invalid_skus_with_invalid_ean_code_format(self):
        self.assign_field_value(self.EAN_CODE_NAME, "1" * 20)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_EAN_CODE_CODE)

    def test_create_invalid_skus_without_category(self):
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, None)
        self.do_create_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_skus_with_empty_category(self):
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, "")
        self.do_create_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_skus_with_invalid_category(self):
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, self.expected_ids[SKU_CATEGORY_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_skus_without_name(self):
        self.assign_field_value(self.SKU_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_NAME_CODE)

    def test_create_invalid_skus_with_empty_name(self):
        self.assign_field_value(self.SKU_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_NAME_CODE)

    def test_create_invalid_skus_without_measure_unit(self):
        self.assign_field_value(self.UNIT_OF_MEASURE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_MEASURE_CODE)

    def test_create_invalid_skus_with_empty_measure_unit(self):
        self.assign_field_value(self.UNIT_OF_MEASURE_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_MEASURE_CODE)

    def test_create_invalid_skus_without_cost(self):
        self.assign_field_value(self.COST_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_COST_CODE)

    def test_create_invalid_skus_with_empty_cost(self):
        self.assign_field_value(self.COST_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_COST_CODE)

    def test_create_invalid_skus_with_invalid_cost(self):
        self.assign_field_value(self.COST_NAME, 0)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_COST_CODE)

    def test_try_create_invalid_skus_without_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_skus_with_invalid_zero_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 0)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_skus_with_invalid_negative_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, -12.0)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_skus_with_invalid_one_hundred_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 100.0)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_skus_with_invalid_over_one_hundred_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 112.0)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_skus_without_external_code(self):
        self.assign_field_value(self.EXTERNAL_CODE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_EXTERNAL_CODE_CODE)

    def test_try_create_invalid_skus_with_invalid_empty_external_code(self):
        self.assign_field_value(self.EXTERNAL_CODE_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SKU_INVALID_EXTERNAL_CODE_CODE)

    def test_update_valid_skus(self):
        self.do_create_requests()
        self.do_update_requests()

    def test_update_valid_skus_with_valid_ean_code_ean_13(self):
        self.do_create_requests()
        self.assign_field_value(self.EAN_CODE_NAME, "1" * 13)
        self.do_update_requests()

    def test_update_valid_skus_with_valid_ean_code_upc_a(self):
        self.do_create_requests()
        self.assign_field_value(self.EAN_CODE_NAME, "1" * 12)
        self.do_update_requests()

    def test_try_update_invalid_skus_with_invalid_ean_code_ean_13(self):
        self.do_create_requests()
        self.assign_field_value(self.EAN_CODE_NAME, "a" * 13)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_EAN_CODE_CODE)

    def test_try_update_invalid_skus_with_invalid_ean_code_upc_a(self):
        self.do_create_requests()
        self.assign_field_value(self.EAN_CODE_NAME, "a" * 12)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_EAN_CODE_CODE)

    def test_try_update_invalid_skus_with_invalid_ean_code_format(self):
        self.do_create_requests()
        self.assign_field_value(self.EAN_CODE_NAME, "1" * 20)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_EAN_CODE_CODE)

    def test_try_update_invalid_skus_without_category(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, None)
        self.do_update_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_skus_with_empty_category(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, "")
        self.do_update_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_skus_with_invalid_category(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_CATEGORY_ID_NAME, self.expected_ids[SKU_CATEGORY_ENTITY_NAME] + 1)
        self.do_update_requests(expected_code=404, expected_internal_code=SKU_CATEGORY_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_skus_without_name(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_NAME_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_NAME_CODE)

    def test_try_update_invalid_skus_with_empty_name(self):
        self.do_create_requests()
        self.assign_field_value(self.SKU_NAME_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_NAME_CODE)

    def test_try_update_invalid_skus_without_measure_unit(self):
        self.do_create_requests()
        self.assign_field_value(self.UNIT_OF_MEASURE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_MEASURE_CODE)

    def test_try_update_invalid_skus_with_empty_measure_unit(self):
        self.do_create_requests()
        self.assign_field_value(self.UNIT_OF_MEASURE_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_MEASURE_CODE)

    def test_try_update_invalid_skus_without_cost(self):
        self.do_create_requests()
        self.assign_field_value(self.COST_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_COST_CODE)

    def test_try_update_invalid_skus_with_empty_cost(self):
        self.do_create_requests()
        self.assign_field_value(self.COST_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_COST_CODE)

    def test_try_update_invalid_skus_with_invalid_cost(self):
        self.do_create_requests()
        self.assign_field_value(self.COST_NAME, 0)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_COST_CODE)

    def test_try_update_invalid_skus_without_tax_rate(self):
        self.do_create_requests()
        self.assign_field_value(self.TAX_RATE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_update_invalid_skus_with_invalid_zero_tax_rate(self):
        self.do_create_requests()
        self.assign_field_value(self.TAX_RATE_NAME, 0)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_update_invalid_skus_with_invalid_negative_tax_rate(self):
        self.do_create_requests()
        self.assign_field_value(self.TAX_RATE_NAME, -12.0)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_update_invalid_skus_with_invalid_one_hundred_tax_rate(self):
        self.do_create_requests()
        self.assign_field_value(self.TAX_RATE_NAME, 100.0)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_update_invalid_skus_with_invalid_over_one_hundred_tax_rate(self):
        self.do_create_requests()
        self.assign_field_value(self.TAX_RATE_NAME, 112.0)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_TAX_RATE_CODE)

    def test_try_update_invalid_skus_without_external_code(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_CODE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_EXTERNAL_CODE_CODE)

    def test_try_update_invalid_skus_with_invalid_empty_external_code(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_CODE_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=SKU_INVALID_EXTERNAL_CODE_CODE)

    def test_try_update_invalid_non_existent_skus(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_update_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE)

    def test_create_skus_on_multiple_clients(self):
        self.do_create_requests()

        self.clean_test_data(clean_all_ids=True)
        create_test_client(self, create_new_client=True)
        create_test_sku_category(self, create_new_category=True)
        self.do_create_requests()

    def test_check_permissions_for_create_skus(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_skus(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_sku(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_skus(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)


SKU_ENTITY_NAME = SkuViewTestCase.ENTITY_NAME


def create_test_sku(test_class, create_new_sku=False):
    return SkuViewTestCase.create_sample_entity_for_another_class(test_class, create_new_sku)

if __name__ == '__main__':
    unittest.main()
