# -*- coding: utf-8 -*
import copy
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CURRENCY_DOES_NOT_EXISTS_CODE, SKU_DOES_NOT_EXISTS_CODE, \
    CLIENT_DOES_NOT_EXISTS_CODE, CONSUMPTIONS_PER_SKU_INVALID_INITIAL_TIME_CODE, \
    CONSUMPTIONS_PER_SKU_INVALID_FINAL_TIME_CODE, ORDER_DOES_NOT_EXISTS_CODE, ORDERS_INVALID_MISSING_MONEY_CODE, \
    ORDERS_INVALID_MONEY_CONSUMPTIONS_CODE, ORDERS_INVALID_MONEY_CONSUMED_CODE, ORDERS_INVALID_AMOUNT_CONSUMED_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testCurrencyViewTestCase import create_test_currency
from tests.testsCJM.testOrders import MONEY_CONSUMED_NAME, SKU_ID_NAME, AMOUNT_CONSUMED_NAME, LOCATION_ID_NAME, \
    CURRENCY_NAME, RESERVATION_ID_NAME, PERSON_RESERVATION_ID_NAME, ORDERS_NAME, \
    AMOUNT_CONSUMPTIONS_NAME, MONEY_CONSUMPTIONS_NAME, MISSING_MONEY_NAME, ORDER_TIME_NAME, OK_OVERFLOW_STATE, \
    OVERFLOWN_OVERFLOW_STATE, get_and_check_balance_for_money, get_and_check_available_funds_for_money, \
    get_and_check_parent_balance_for_money, get_and_check_skus_consumed_report, CONSUMPTION_TIME_NAME
from tests.testsCJM.testPaquetes.testMoneyConsumptionViewTestCase import create_test_money_consumption
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas import create_and_login_new_admin_user, get_and_check_entities_by_user_report, \
    ADMIN_USERNAME, USERNAME_NAME
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
    PERSON_RESERVATION_ENTITY_NAME, change_person_reservation_active_status
from tests.testsCJM.testReservas.testReservaViewTestCase import RESERVATION_ENTITY_NAME, create_test_reservation
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class PersonOrdersForPackageWithMoneyConsumptionViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = ORDER_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/person-orders/"
    SPECIFIC_RESOURCE_BASE_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/person-orders/"
    ALL_CONSUMPTIONS_URL = u"/clients/{0}/person-money-consumptions/"
    PERSON_RESERVATIONS_CONSUMPTIONS_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/person-money-consumptions/"

    ATTRIBUTES_NAMES_BY_FIELDS = {LOCATION_ID_NAME: "TEST_ORDER_ID_LOCATION",
                                  ORDERS_NAME: "TEST_ORDER_ORDERS"}

    NUMBER_OF_ENTITIES = 1
    ENTITY_NAME = 'person-money-order'
    ORDERS_ENTITY_NAME = "person-orders"
    MONEY_CONSUMPTIONS_ENTITY_NAME = "person-money-consumptions"
    MONEY_PER_CONSUMPTION = 5
    AMOUNT_PER_CONSUMPTION = 1

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password_123"
    TEST_USER_ROLE = None

    TEST_PACKAGE_NAME = "Test package"
    TEST_PACKAGE_PRICE = 100.5
    TEST_PACKAGE_DESCRIPTION = "Test description"
    TEST_PACKAGE_VALID_FROM = "19900101010101"
    TEST_PACKAGE_VALID_THROUGH = "21000101010101"
    TEST_PACKAGE_DURATION = 5
    TEST_PACKAGE_ID_SOCIAL_EVENT = None

    NUMBER_LOCATIONS = 5
    TEST_LOCATION_TYPE = "CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_PERSON_NAME = "Test person"
    TEST_PERSON_DOCUMENT_TYPE = "CC"
    TEST_PERSON_DOCUMENT_NUMBER = "12345"
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

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    TEST_PERSON_RESERVATION_ID_RESERVATION = None
    TEST_PERSON_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE
    TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
    TEST_PERSON_RESERVATION_FINAL_DATE = TEST_PERSON_RESERVATION_INITIAL_DATE

    TEST_RESERVATION_COMPANY = "Test company"
    TEST_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE * 50
    TEST_RESERVATION_INITIAL_DATE = TEST_PERSON_RESERVATION_INITIAL_DATE
    TEST_RESERVATION_FINAL_DATE = TEST_PERSON_RESERVATION_INITIAL_DATE

    TEST_CURRENCY_NAME = "Test currency"
    TEST_MONEY_CONSUMPTION_CURRENCY = None

    DEFAULT_CURRENCY = u"COP"

    CREATED_CONSUMPTIONS = []

    def _create_package_location(self):
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]

    def _create_package_with_money_consumption(self):
        create_test_package(self, create_new_package=True)
        total_consumptions = self.NUMBER_OF_ENTITIES * self.number_of_orders * self.number_of_consumptions_per_order
        self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED = total_consumptions * self.MONEY_PER_CONSUMPTION
        create_test_money_consumption(self, create_new_consumption=True)

    def create_package_with_restricted_consumption(self):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
        self._create_package_with_money_consumption()

    def create_package_without_restricted_consumption(self):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = False
        self._create_package_with_money_consumption()

    def create_person_reservation_for_last_package(self, activate_package=True):
        create_test_reservation(self)

        create_test_person(self, create_new_person=True)
        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]

        create_test_person_reservation(self, create_new_person_reservation=True)
        if activate_package:
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)

    def create_package_with_restricted_consumption_and_person_reservation(self):
        self.create_package_with_restricted_consumption()
        self.create_person_reservation_for_last_package()

    def create_package_without_restricted_consumption_and_person_reservation(self):
        self.create_package_without_restricted_consumption()
        self.create_person_reservation_for_last_package()

    def create_test_orders(self, create_new_sku_per_consumption=True, create_new_sku_per_order=False):
        if create_new_sku_per_consumption:
            create_new_sku_per_order = False

        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

        orders = []
        self.last_consumptions = []
        for order_number in range(self.number_of_orders):
            if self.consumption_time_template is None:
                consumption_time = None
            else:
                consumption_time = self.consumption_time_template.format(order_number)
            consumptions = []
            create_test_sku(self, create_new_sku=create_new_sku_per_order)
            for consumption_number in range(self.number_of_consumptions_per_order):
                create_test_sku(self, create_new_sku=create_new_sku_per_consumption)
                consumption = {SKU_ID_NAME: self.expected_ids[SKU_ENTITY_NAME],
                               AMOUNT_CONSUMED_NAME: self.AMOUNT_PER_CONSUMPTION,
                               MONEY_CONSUMED_NAME: self.MONEY_PER_CONSUMPTION,
                               MISSING_MONEY_NAME: self.missing_money}
                consumptions.append(consumption)
            order = {CURRENCY_NAME: self.currency_name,
                     RESERVATION_ID_NAME: self.expected_ids[RESERVATION_ENTITY_NAME],
                     PERSON_RESERVATION_ID_NAME: self.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
                     MONEY_CONSUMPTIONS_NAME: consumptions,
                     AMOUNT_CONSUMPTIONS_NAME: [],
                     ORDER_TIME_NAME: consumption_time}
            orders.append(order)
            self.last_consumptions += consumptions

        return orders

    def create_and_assign_orders(self, create_new_sku_per_consumption=True, create_new_sku_per_order=False):
        orders = self.create_test_orders(create_new_sku_per_consumption, create_new_sku_per_order)
        self.assign_field_value(ORDERS_NAME, orders)

        create_test_location(self, create_new_location=True)
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])

    def _check_person_money_consumptions_with_user_and_children_data(self, results, username):
        orders = results[0][PERSON_RESERVATION_ENTITY_NAME][0][self.ORDERS_ENTITY_NAME]
        if len(orders) > 0:
            amount_consumptions = orders[0][self.MONEY_CONSUMPTIONS_ENTITY_NAME]
            self.clean_test_data()
            self._add_data_values(PersonOrdersForPackageWithMoneyConsumptionViewTestCase, self.CREATED_CONSUMPTIONS)
            self.ENTITIES_PER_REQUEST = 1
            self.check_list_response(self.ENTITY_NAME, amount_consumptions, len(self.CREATED_CONSUMPTIONS))
            for consumption in amount_consumptions:
                self.assertEqual(username, consumption.get(USERNAME_NAME, None))
        else:
            self.assertEqual(0, len(self.CREATED_CONSUMPTIONS))

    def setUp(self):
        super(PersonOrdersForPackageWithMoneyConsumptionViewTestCase, self).setUp()
        create_test_client(self)

        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 1
        self.currency_name = self.DEFAULT_CURRENCY
        self.missing_money = 0
        self.consumption_time_template = "2010010101010{0}"

        self.included_currency = self.DEFAULT_CURRENCY
        self.consumed_currency = None
        self.expected_state = OK_OVERFLOW_STATE
        self.expected_state_consumed = OK_OVERFLOW_STATE
        PersonOrdersForPackageWithMoneyConsumptionViewTestCase.CREATED_CONSUMPTIONS = []

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        orders = copy.deepcopy(request_values.get(ORDERS_NAME))
        for order in orders:
            for consumption in order[MONEY_CONSUMPTIONS_NAME]:
                consumption[CONSUMPTION_TIME_NAME] = order[ORDER_TIME_NAME]
            cls.CREATED_CONSUMPTIONS += order[MONEY_CONSUMPTIONS_NAME]
            order[LOCATION_ID_NAME] = request_values.get(LOCATION_ID_NAME)
            if order.get(CURRENCY_NAME) is None:
                order[CURRENCY_NAME] = cls.DEFAULT_CURRENCY
            for consumption in order[MONEY_CONSUMPTIONS_NAME]:
                consumption[CURRENCY_NAME] = order[CURRENCY_NAME]
            del order[MONEY_CONSUMPTIONS_NAME]
            del order[AMOUNT_CONSUMPTIONS_NAME]
        return orders

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def get_ancestor_entities_names_for_specific_resource(cls):
        return [CLIENT_ENTITY_NAME, RESERVATION_ENTITY_NAME, PERSON_RESERVATION_ENTITY_NAME]

    def test_empty_order_view(self):
        self.request_all_resources_and_check_result(0)
        self.check_all_consumptions_person_consumptions_and_person_orders(check_person_reservation_entities=False)

    def test_invalid_order_view_with_invalid_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_create_valid_money_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_empty_consumptions(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_money_consumptions_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_money_consumptions_and_query_them_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 3
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_money_consumptions_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_money_consumptions_and_query_them_with_reservations_endpoint_true_include_children_and_new_user(self):
        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 3
        self.create_package_with_restricted_consumption_and_person_reservation()
        create_and_login_new_admin_user(self)
        self.create_and_assign_orders()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_money_consumptions_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_empty_money_orders(self):
        self.ENTITIES_PER_REQUEST = 0
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.assign_field_value(ORDERS_NAME, [])
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_with_consumption_time_on_first_second_of_reservation(self):
        self.consumption_time_template = self.TEST_RESERVATION_INITIAL_DATE
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_with_consumption_time_on_last_second_of_reservation(self):
        self.consumption_time_template = self.TEST_RESERVATION_FINAL_DATE
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_invalid_money_orders_with_consumption_time_smaller_than_reservation_date(self):
        self.consumption_time_template = "20090101010101"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_invalid_money_orders_with_consumption_time_greater_than_reservation_date(self):
        self.consumption_time_template = "20200101010101"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_consumptions_exceeding_money_limit(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.MONEY_PER_CONSUMPTION = 1000000
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_duplicated_money_consumptions_on_different_request(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)

        number_of_consumptions = self.NUMBER_OF_ENTITIES * self.number_of_orders * self.number_of_consumptions_per_order
        self.CREATED_CONSUMPTIONS = self.CREATED_CONSUMPTIONS[:number_of_consumptions]
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_duplicated_money_consumptions_on_the_same_request(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.consumption_time_template = "20200101010101"
        self.number_of_orders = 2
        self.create_and_assign_orders()
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)

        self.CREATED_CONSUMPTIONS = self.CREATED_CONSUMPTIONS[:self.number_of_consumptions_per_order]
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_multiple_valid_money_orders_with_multiple_consumptions(self):
        self.number_of_orders = 3
        self.number_of_consumptions_per_order = 2
        self.ENTITIES_PER_REQUEST = self.number_of_orders
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_multiple_valid_money_orders_with_multiple_consumptions_with_the_same_sku(self):
        self.number_of_orders = 3
        self.number_of_consumptions_per_order = 2
        self.ENTITIES_PER_REQUEST = self.number_of_orders
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders(create_new_sku_per_consumption=False)
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_without_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.assign_field_value(LOCATION_ID_NAME, None)
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_without_currency(self):
        create_test_currency(self, create_new_currency=True)
        self.currency_name = None

        self.TEST_MONEY_CONSUMPTION_CURRENCY = None
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_with_custom_currency(self):
        create_test_currency(self, create_new_currency=True)
        self.currency_name = self.TEST_CURRENCY_NAME

        self.TEST_MONEY_CONSUMPTION_CURRENCY = self.TEST_CURRENCY_NAME
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3
        self.MONEY_PER_CONSUMPTION *= number_of_days

        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_money_orders_with_multiple_person_reservations(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        id_first_person = self.expected_ids[PERSON_ENTITY_NAME]
        first_reservation_orders = self.create_test_orders()
        self.TEST_PERSON_DOCUMENT_NUMBER += "10"
        self.create_person_reservation_for_last_package()
        second_reservation_orders = self.create_test_orders()
        self.assign_field_value(ORDERS_NAME, first_reservation_orders + second_reservation_orders)
        self.ENTITIES_PER_REQUEST = 2
        PersonOrdersForPackageWithMoneyConsumptionViewTestCase.CHECK_BY_GET = False
        self.do_create_requests()
        PersonOrdersForPackageWithMoneyConsumptionViewTestCase.CHECK_BY_GET = True

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     id_first_person))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders(number_of_person_reservations=2)

    def test_try_create_invalid_money_orders_with_non_existent_currency(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.currency_name = self.TEST_CURRENCY_NAME
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=404, expected_internal_code=CURRENCY_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_with_non_existent_skus(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[SKU_ID_NAME] += 2000
        self.assign_field_value(ORDERS_NAME, orders)

        self.do_create_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_without_skus(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[SKU_ID_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)

        self.do_create_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_without_missing_money(self):
        self.missing_money = None
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MISSING_MONEY_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_with_negative_missing_money(self):
        self.missing_money = -10
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MISSING_MONEY_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_without_money_consumptions(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            order[MONEY_CONSUMPTIONS_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MONEY_CONSUMPTIONS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_without_money_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[MONEY_CONSUMED_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MONEY_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_with_zero_money_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[MONEY_CONSUMED_NAME] = 0
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MONEY_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_with_negative_money_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[MONEY_CONSUMED_NAME] = -10
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MONEY_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_without_amount_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[AMOUNT_CONSUMED_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_with_zero_amount_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[AMOUNT_CONSUMED_NAME] = 0
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_money_orders_with_negative_amount_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[MONEY_CONSUMPTIONS_NAME]:
                consumptions[AMOUNT_CONSUMED_NAME] = -10
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_delete_valid_money_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.do_delete_requests()
        PersonOrdersForPackageWithMoneyConsumptionViewTestCase.CREATED_CONSUMPTIONS = []

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_query_money_consumptions_with_reservations_endpoint_after_delete_with_true_include_children_and_check_nothing_is_returned(self):
        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 3
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        PersonOrdersForPackageWithMoneyConsumptionViewTestCase.CREATED_CONSUMPTIONS = []
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_money_consumptions_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_delete_valid_persons_consumptions_and_create_them_again(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        PersonOrdersForPackageWithMoneyConsumptionViewTestCase.CREATED_CONSUMPTIONS = []
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 2 * self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_check_available_funds_and_balance_for_new_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_money = self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_new_reservation_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_money = self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED * number_of_days
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_new_reservation_and_custom_currency(self):
        self.create_package_with_restricted_consumption()
        create_test_currency(self, create_new_currency=True)
        self.TEST_MONEY_CONSUMPTION_CURRENCY = self.TEST_CURRENCY_NAME
        create_test_money_consumption(self, create_new_consumption=True)
        self.TEST_MONEY_CONSUMPTION_CURRENCY = None
        self.create_person_reservation_for_last_package()
        self.consumed_currency = self.TEST_CURRENCY_NAME
        self.expected_money = self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        self.expected_money_consumed = self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_full_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.expected_money = 0
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_reservation_with_excess_consumption(self):
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.consumption_time_template = "2010010101020{0}"
        self.create_and_assign_orders()
        self.missing_money = self.MONEY_PER_CONSUMPTION
        self.do_create_requests()

        self.expected_money = 0
        self.expected_state = OVERFLOWN_OVERFLOW_STATE
        get_and_check_available_funds_for_money(self)
        self.expected_money = -self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_reservation_with_consumption_on_non_included_currency(self):
        self.create_package_without_restricted_consumption_and_person_reservation()
        create_test_currency(self, create_new_currency=True)
        self.currency_name = self.TEST_CURRENCY_NAME
        self.create_and_assign_orders()

        self.missing_money = self.MONEY_PER_CONSUMPTION
        self.do_create_requests()
        self.consumed_currency = self.TEST_CURRENCY_NAME
        self.expected_money = self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        self.expected_state_consumed = OVERFLOWN_OVERFLOW_STATE
        self.expected_money_consumed = 0
        get_and_check_available_funds_for_money(self)
        self.expected_money_consumed = -self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation()
        self.MONEY_PER_CONSUMPTION *= number_of_days
        self.create_and_assign_orders()
        self.do_create_requests()

        self.expected_money = 0
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_previously_full_reservation_after_delete(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()

        self.do_create_requests()
        self.do_delete_requests()

        self.expected_money = self.TEST_MONEY_CONSUMPTION_MONEY_INCLUDED
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_empty_entities_by_user_report_with_person_money_consumptions_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_money_consumptions(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.CREATED_CONSUMPTIONS}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_money_consumptions_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.consumption_time_template = "2010010201010{0}"
        self.create_and_assign_orders()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.CREATED_CONSUMPTIONS}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_money_consumptions_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: list(self.CREATED_CONSUMPTIONS)}
        users = {self.TEST_USER_USERNAME}
        self.clean_test_data()
        self.CREATED_CONSUMPTIONS = []

        self.consumption_time_template = "2010010201010{0}"
        self.create_and_assign_orders()
        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests(do_get_and_check_results=False)
        expected_entities_by_user[self.TEST_USER_USERNAME] = self.last_consumptions
        users.add(self.TEST_USER_USERNAME)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_money_consumptions_after_delete(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_money_consumptions_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_person_money_consumptions_after_delete_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.CREATED_CONSUMPTIONS}
        users = {self.TEST_USER_USERNAME}
        self.do_delete_requests()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.MONEY_CONSUMPTIONS_ENTITY_NAME,
                                              include_deleted=True)

    def test_check_permissions_for_get_all_consumptions(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER,
                         CLIENT_WAITER_USER, CLIENT_CASHIER_USER}
        required_locations = {}
        url = self.ALL_CONSUMPTIONS_URL.format(self.expected_ids[CLIENT_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservation_consumptions(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER,
                         CLIENT_WAITER_USER, CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.PERSON_RESERVATIONS_CONSUMPTIONS_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                               self.expected_ids[RESERVATION_ENTITY_NAME],
                                                               self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def check_all_consumptions_person_consumptions_and_person_orders(self, expected_code=200,
                                                                     expected_internal_code=None,
                                                                     check_person_reservation_entities=True,
                                                                     number_of_person_reservations=1,
                                                                     expected_code_person_reservation_entities=200,
                                                                     expected_internal_code_person_reservation_entities=None):
        original_orders = self.original_entities
        all_consumptions_url = self.ALL_CONSUMPTIONS_URL.format(self.expected_ids[CLIENT_ENTITY_NAME])

        self.clean_test_data()
        self._add_data_values(PersonOrdersForPackageWithMoneyConsumptionViewTestCase, self.CREATED_CONSUMPTIONS)
        results_all_consumptions = self.do_get_request(all_consumptions_url, expected_code=expected_code)
        if expected_code == 200:
            self.ENTITIES_PER_REQUEST = 1
            self.check_list_response(self.ENTITY_NAME, results_all_consumptions, len(self.CREATED_CONSUMPTIONS))
            self.calculate_expected_consumed_values_and_check_skus_consumed_report()
        else:
            self.validate_error(results_all_consumptions, expected_internal_code)

        if check_person_reservation_entities:
            reservation_consumptions_url = self.PERSON_RESERVATIONS_CONSUMPTIONS_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                                            self.expected_ids[RESERVATION_ENTITY_NAME],
                                                                                            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
            results_reservation_consumptions = self.do_get_request(reservation_consumptions_url,
                                                                   expected_code=expected_code_person_reservation_entities)
            if expected_code_person_reservation_entities == 200:
                self.ENTITIES_PER_REQUEST = 1
                self.clean_test_data()
                consumptions = self.CREATED_CONSUMPTIONS[len(self.CREATED_CONSUMPTIONS) - len(self.CREATED_CONSUMPTIONS)/number_of_person_reservations:]
                self._add_data_values(PersonOrdersForPackageWithMoneyConsumptionViewTestCase, consumptions)
                self.check_list_response(self.ENTITY_NAME, results_reservation_consumptions, len(consumptions))
            else:
                self.validate_error(results_reservation_consumptions, expected_internal_code_person_reservation_entities)

            reservation_orders_url = self.SPECIFIC_RESOURCE_BASE_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                            self.expected_ids[RESERVATION_ENTITY_NAME],
                                                                            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
            results_reservation_orders = self.do_get_request(reservation_orders_url,
                                                             expected_code=expected_code_person_reservation_entities)
            if expected_code_person_reservation_entities == 200:
                self.ENTITIES_PER_REQUEST = 1
                self.clean_test_data()
                if number_of_person_reservations > 1:
                    original_orders = [order for order in original_orders
                                       if order[PERSON_RESERVATION_ID_NAME] == self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]]
                self._add_data_values(PersonOrdersForPackageWithMoneyConsumptionViewTestCase, original_orders)
                self.check_list_response(self.ENTITY_NAME, results_reservation_orders, len(original_orders))
            else:
                self.validate_error(results_reservation_orders, expected_internal_code_person_reservation_entities)

    def test_create_valid_money_orders_and_check_report_with_both_times_included(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.calculate_expected_consumed_values_and_check_skus_consumed_report(initial_time="19900101010101",
                                                                               final_time="21000101010101")

    def test_create_valid_money_orders_and_check_report_with_initial_time_included(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.calculate_expected_consumed_values_and_check_skus_consumed_report(initial_time="19900101010101")

    def test_create_valid_money_orders_and_check_report_with_final_time_included(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.calculate_expected_consumed_values_and_check_skus_consumed_report(final_time="21000101010101")

    def test_create_valid_money_orders_and_check_report_with_empty_time_range(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           initial_time="21000101010101", final_time="19900101010101")

    def test_create_valid_money_orders_and_check_report_with_non_empty_non_included_time_range(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           initial_time="21000101010101", final_time="21010101010101")

    def test_create_valid_money_orders_and_check_report_with_non_included_initial_time(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           initial_time="21000101010101")

    def test_create_valid_money_orders_and_check_report_with_non_included_final_time(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           final_time="19900101010101")

    def test_create_valid_money_orders_and_check_report_with_invalid_initial_time(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           initial_time="INVALID_DATE", expected_code=400,
                                           expected_internal_code=CONSUMPTIONS_PER_SKU_INVALID_INITIAL_TIME_CODE)

    def test_create_valid_money_orders_and_check_report_with_invalid_final_time(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           final_time="INVALID_DATE", expected_code=400,
                                           expected_internal_code=CONSUMPTIONS_PER_SKU_INVALID_FINAL_TIME_CODE)

    def calculate_expected_consumed_values_and_check_skus_consumed_report(self, initial_time=None, final_time=None):
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        for consumption in self.CREATED_CONSUMPTIONS:
            id_sku = consumption[SKU_ID_NAME]
            ids_skus.add(id_sku)
            if id_sku not in expected_amounts_by_money:
                expected_amounts_by_money[id_sku] = 0
                expected_money_by_sku_by_currency[id_sku] = dict()
                expected_amount_by_sku_by_currency[id_sku] = dict()
            expected_amounts_by_money[id_sku] += consumption[AMOUNT_CONSUMED_NAME]
            currency = consumption[CURRENCY_NAME]
            if currency not in expected_money_by_sku_by_currency[id_sku]:
                expected_money_by_sku_by_currency[id_sku][currency] = 0
                expected_amount_by_sku_by_currency[id_sku][currency] = 0
            expected_money_by_sku_by_currency[id_sku][currency] += consumption[MONEY_CONSUMED_NAME]
            expected_amount_by_sku_by_currency[id_sku][currency] += consumption[AMOUNT_CONSUMED_NAME]

        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           initial_time, final_time)


def create_test_person_consumption_for_money(test_class, create_new_consumption=False):
    test_class.TEST_ORDER_ID_LOCATION = getattr(test_class, 'TEST_PERSON_CONSUMPTION_ID_LOCATION', None)
    consumption = {SKU_ID_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_ID_SKU', None),
                   MISSING_MONEY_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_MISSING_MONEY', 0),
                   AMOUNT_CONSUMED_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_AMOUNT', 1),
                   MONEY_CONSUMED_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_MONEY', 10)}
    order = {CURRENCY_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_CURRENCY', None),
             RESERVATION_ID_NAME: test_class.expected_ids[RESERVATION_ENTITY_NAME],
             PERSON_RESERVATION_ID_NAME: test_class.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
             MONEY_CONSUMPTIONS_NAME: [consumption],
             AMOUNT_CONSUMPTIONS_NAME: [],
             ORDER_TIME_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_TIME', "20100101010101")}
    test_class.TEST_ORDER_ORDERS = [order]
    return PersonOrdersForPackageWithMoneyConsumptionViewTestCase.create_sample_entity_for_another_class(test_class,
                                                                                                         create_new_consumption)

if __name__ == '__main__':
    unittest.main()
