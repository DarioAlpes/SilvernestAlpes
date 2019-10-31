# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    CURRENCY_DOES_NOT_EXISTS_CODE, CURRENCY_INVALID_NAME_CODE, CURRENCY_ALREADY_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client


class CurrencyViewTestCase(FlaskClientBaseTestCase):
    MONEY_NAME = u"name"
    ID_NAME = u"id"

    DEFAULT_CURRENCY = u"COP"

    ENTITY_DOES_NOT_EXISTS_CODE = CURRENCY_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/currencies/"

    ATTRIBUTES_NAMES_BY_FIELDS = {MONEY_NAME: "TEST_CURRENCY_NAME"}

    ENTITY_NAME = 'currencies'
    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    def setUp(self):
        super(CurrencyViewTestCase, self).setUp()
        self.expected_ids[self.ENTITY_NAME] = 1
        create_test_client(self)
        self.currency_name_template = u"currency {0}"

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.MONEY_NAME] = u"currency {0}"
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0,
                                                    expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                                    number_of_default_entities=1)

    def test_try_get_non_existent_currency(self):
        results = self.do_get_request(self.get_item_url(type(self)).format(self.NUMBER_OF_ENTITIES * 2),
                                      expected_code=404)
        validate_error(self, results, CURRENCY_DOES_NOT_EXISTS_CODE)

    def test_empty_currencies_view(self):
        self.add_data_value(self.ENTITY_NAME, self.MONEY_NAME, self.DEFAULT_CURRENCY)
        self.add_data_value(self.ENTITY_NAME, self.ID_NAME, 1)
        self.request_all_resources_and_check_result(0, number_of_default_entities=1)

    def test_create_valid_currencies(self):
        self.do_create_requests(number_of_default_entities=1)

    def test_create_invalid_currencies_with_default_currency_name(self):
        self.assign_field_value(self.MONEY_NAME, self.DEFAULT_CURRENCY)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CURRENCY_ALREADY_EXISTS_CODE,
                                number_of_default_entities=1)

    def test_create_invalid_duplicated_currencies(self):
        self.do_create_requests(number_of_default_entities=1)
        self.clean_test_data()
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CURRENCY_ALREADY_EXISTS_CODE,
                                number_of_default_entities=1,
                                check_results_as_list=False,
                                do_get_and_check_results=False)

    def test_create_invalid_currencies_without_name(self):
        self.assign_field_value(self.MONEY_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CURRENCY_INVALID_NAME_CODE,
                                number_of_default_entities=1)

    def test_create_invalid_currencies_with_empty_name(self):
        self.assign_field_value(self.MONEY_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CURRENCY_INVALID_NAME_CODE,
                                number_of_default_entities=1)

    def test_check_permissions_for_create_currencies(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_currencies(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=1)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_currency(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE,\
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=1)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)


CURRENCY_ENTITY_NAME = CurrencyViewTestCase.ENTITY_NAME


def create_test_currency(test_class, create_new_currency=False):
    return CurrencyViewTestCase.create_sample_entity_for_another_class(test_class, create_new_currency)

if __name__ == '__main__':
    unittest.main()
