# -*- coding: utf-8 -*
import unittest

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, SKU_DOES_NOT_EXISTS_CODE, \
    LOCATION_DOES_NOT_EXISTS_CODE, SALE_LOCATION_DOES_NOT_EXISTS_CODE, SALE_LOCATION_INVALID_PRICE_CODE, \
    SALE_LOCATION_ALREADY_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class UbicacionVentaSKUViewTestCase(FlaskClientBaseTestCase):
    PRICE_NAME = u"price"
    SKU_ID_NAME = u"id-sku"
    LOCATION_ID_NAME = u"id-location"
    ORIGINAL_ID_NAME = u"id"

    ID_NAME = LOCATION_ID_NAME

    ENTITY_DOES_NOT_EXISTS_CODE = None
    CHECK_BY_GET = False
    RESOURCE_URL = u"/clients/{0}/skus/{1}/sales-locations/"

    ENTITY_NAME = 'SKU_Sales_Location'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    def setUp(self):
        super(UbicacionVentaSKUViewTestCase, self).setUp()
        create_test_client(self)
        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self)

        self.sales_locations = []
        for number_sku in range(0, self.NUMBER_OF_ENTITIES):
            create_test_location(self, create_new_location=True)
            self.sales_locations.append(self.expected_ids[LOCATION_ENTITY_NAME])

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.PRICE_NAME] = 100.5
        return values

    def get_entity_values_for_create(self, entity_number):
        values = super(UbicacionVentaSKUViewTestCase, self).get_entity_values_for_create(entity_number)
        values[self.SKU_ID_NAME] = self.expected_ids[SKU_ENTITY_NAME]
        values[self.LOCATION_ID_NAME] = self.sales_locations[entity_number]
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME, SKU_ENTITY_NAME]

    def __assign_ids_locations_to_ids(self):
        self.clean_data_values(self.ENTITY_NAME, self.SKU_ID_NAME)
        self.clean_data_values(self.ENTITY_NAME, self.LOCATION_ID_NAME)
        for location in self.sales_locations:
            self.add_data_value(self.ENTITY_NAME, self.ORIGINAL_ID_NAME, location)

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_non_existent_sku(self):
        self.expected_ids[SKU_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=SKU_DOES_NOT_EXISTS_CODE)

    def test_empty_sales_locations_view(self):
        self.request_all_resources_and_check_result(0)

    def test_do_create_requests(self):
        self.do_create_requests(do_get_and_check_results=False)
        self.__assign_ids_locations_to_ids()
        self.request_all_resources_and_check_result(self.NUMBER_OF_ENTITIES)

    def test_create_invalid_sales_locations_with_non_existent_sku(self):
        self.expected_ids[SKU_ENTITY_NAME] += 1
        self.do_create_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_create_invalid_sales_locations_with_non_existent_locations(self):
        self.sales_locations = [self.expected_ids[LOCATION_ENTITY_NAME] + 2 * self.NUMBER_OF_ENTITIES]
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_sales_locations_without_price(self):
        self.assign_field_value(self.PRICE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SALE_LOCATION_INVALID_PRICE_CODE)

    def test_create_invalid_sales_locations_with_invalid_price(self):
        self.assign_field_value(self.PRICE_NAME, 0)
        self.do_create_requests(expected_code=400, expected_internal_code=SALE_LOCATION_INVALID_PRICE_CODE)

    def test_create_invalid_sales_locations_with_duplicated_location(self):
        self.do_create_requests(do_get_and_check_results=False)
        self.do_create_requests(expected_code=400, expected_internal_code=SALE_LOCATION_ALREADY_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_delete_valid_sales_locations(self):
        self.do_create_requests(do_get_and_check_results=False)
        self.do_delete_requests()

    def test_try_delete_valid_sales_locations_on_wrong_sku(self):
        self.do_create_requests(do_get_and_check_results=False)
        create_test_sku(self, create_new_sku=True)
        self.do_delete_requests(expected_code=404, expected_internal_code=SALE_LOCATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_delete_valid_sales_locations_on_non_existent_sku(self):
        self.do_create_requests(do_get_and_check_results=False)
        self.expected_ids[SKU_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_delete_non_existent_sales_locations(self):
        self.do_create_requests(do_get_and_check_results=False)
        for sale_location in self.original_entities:
            sale_location[self.LOCATION_ID_NAME] += 2 * self.NUMBER_OF_ENTITIES

        self.do_delete_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_delete_locations_not_selling_the_sku(self):
        self.do_create_requests(do_get_and_check_results=False)
        for sale_location in self.original_entities:
            create_test_location(self, create_new_location=True)
            sale_location[self.LOCATION_ID_NAME] = self.expected_ids[LOCATION_ENTITY_NAME]

        self.do_delete_requests(expected_code=404, expected_internal_code=SALE_LOCATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_check_permissions_for_create_sales_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_create_permissions(allowed_roles, required_locations, do_delete_after_success=True)

    def test_check_permissions_for_get_sales_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_WAITER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(do_get_and_check_results=False)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_WAITER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_sales_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests(do_get_and_check_results=False)
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_delete_permissions(allowed_roles, required_locations)


if __name__ == '__main__':
    unittest.main()
