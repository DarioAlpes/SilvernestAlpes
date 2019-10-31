# -*- coding: utf-8 -*
import copy
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import LOCATION_DOES_NOT_EXISTS_CODE, \
    SKU_DOES_NOT_EXISTS_CODE, CLIENT_DOES_NOT_EXISTS_CODE, PERSON_RESERVATION_DOES_NOT_EXISTS_CODE, \
    RESERVATION_DOES_NOT_EXISTS_CODE, ORDER_DOES_NOT_EXISTS_CODE, \
    ORDERS_INVALID_ORDER_TIME_CODE, ORDERS_INVALID_MISSING_AMOUNT_CODE, ORDERS_INVALID_ORDERS_CODE, \
    ORDERS_INVALID_AMOUNT_CONSUMPTIONS_CODE, ORDERS_INVALID_EMPTY_CONSUMPTIONS_CODE, ORDERS_INVALID_AMOUNT_CONSUMED_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testOrders import SKU_ID_NAME, AMOUNT_CONSUMED_NAME, LOCATION_ID_NAME, \
    RESERVATION_ID_NAME, PERSON_RESERVATION_ID_NAME, ORDERS_NAME, AMOUNT_CONSUMPTIONS_NAME, \
    MONEY_CONSUMPTIONS_NAME, ORDER_TIME_NAME, MISSING_AMOUNT_NAME, OK_OVERFLOW_STATE, OVERFLOWN_OVERFLOW_STATE, \
    get_and_check_available_funds_for_amount, get_and_check_balance_for_amount, get_and_check_parent_balance_for_amount, \
    get_and_check_skus_consumed_report, CONSUMPTION_TIME_NAME
from tests.testsCJM.testPaquetes.testAmountConsumptionViewTestCase import create_test_amount_consumption
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas import create_and_login_new_admin_user, get_and_check_entities_by_user_report, \
    USERNAME_NAME, ADMIN_USERNAME
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
    PERSON_RESERVATION_ENTITY_NAME, change_person_reservation_active_status
from tests.testsCJM.testReservas.testReservaViewTestCase import RESERVATION_ENTITY_NAME, create_test_reservation
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class PersonOrdersForPackageWithAmountConsumptionViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = ORDER_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/person-orders/"
    SPECIFIC_RESOURCE_BASE_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/person-orders/"
    ALL_CONSUMPTIONS_URL = u"/clients/{0}/person-amount-consumptions/"
    PERSON_RESERVATIONS_CONSUMPTIONS_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/person-amount-consumptions/"

    ATTRIBUTES_NAMES_BY_FIELDS = {LOCATION_ID_NAME: "TEST_ORDER_ID_LOCATION",
                                  ORDERS_NAME: "TEST_ORDER_ORDERS"}

    NUMBER_OF_ENTITIES = 1
    ENTITY_NAME = 'person-amount-order'
    ORDERS_ENTITY_NAME = "person-orders"
    AMOUNT_CONSUMPTIONS_ENTITY_NAME = "person-amount-consumptions"
    AMOUNT_PER_CONSUMPTION = 5

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

    CREATED_CONSUMPTIONS = []

    def create_new_sku(self):
        create_test_sku_category(self, create_new_category=True)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self, create_new_sku=True)

    def _create_package_location(self):
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]

    def _create_package_with_amount_consumption(self, use_sku):
        create_test_package(self, create_new_package=True)
        self.create_new_sku()
        if use_sku:
            self.TEST_AMOUNT_CONSUMPTION_ID_SKU = self.expected_ids[SKU_ENTITY_NAME]
            self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY = None
        else:
            self.TEST_AMOUNT_CONSUMPTION_ID_SKU = None
            self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        total_consumptions = self.NUMBER_OF_ENTITIES * self.number_of_orders * self.number_of_consumptions_per_order
        self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED = total_consumptions * self.AMOUNT_PER_CONSUMPTION
        create_test_amount_consumption(self, create_new_consumption=True)

    def create_package_with_restricted_consumption(self, use_sku):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
        self._create_package_with_amount_consumption(use_sku)

    def create_package_without_restricted_consumption(self, use_sku):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = False
        self._create_package_with_amount_consumption(use_sku)

    def create_person_reservation_for_last_package(self, activate_package=True):
        create_test_reservation(self)

        create_test_person(self, create_new_person=True)
        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]

        create_test_person_reservation(self, create_new_person_reservation=True)
        if activate_package:
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)

    def create_package_with_restricted_consumption_and_person_reservation_with_sku(self):
        self.create_package_with_restricted_consumption(use_sku=True)
        self.create_person_reservation_for_last_package()

    def create_package_with_restricted_consumption_and_person_reservation_with_sku_category(self):
        self.create_package_with_restricted_consumption(use_sku=False)
        self.create_person_reservation_for_last_package()

    def create_package_without_restricted_consumption_and_person_reservation_with_sku(self):
        self.create_package_without_restricted_consumption(use_sku=True)
        self.create_person_reservation_for_last_package()

    def create_package_without_restricted_consumption_and_person_reservation_with_sku_category(self):
        self.create_package_without_restricted_consumption(use_sku=False)
        self.create_person_reservation_for_last_package()

    def create_test_orders(self, create_new_sku_per_consumption=False, create_new_sku_per_order=False):
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
                               MISSING_AMOUNT_NAME: self.missing_amount}
                consumptions.append(consumption)
            order = {RESERVATION_ID_NAME: self.expected_ids[RESERVATION_ENTITY_NAME],
                     PERSON_RESERVATION_ID_NAME: self.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
                     AMOUNT_CONSUMPTIONS_NAME: consumptions,
                     MONEY_CONSUMPTIONS_NAME: [],
                     ORDER_TIME_NAME: consumption_time}
            orders.append(order)
            self.last_consumptions += consumptions

        return orders

    def create_and_assign_orders(self, create_new_sku_per_consumption=False, create_new_sku_per_order=False):
        orders = self.create_test_orders(create_new_sku_per_consumption, create_new_sku_per_order)
        self.assign_field_value(ORDERS_NAME, orders)

        create_test_location(self, create_new_location=True)
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])

    def _check_person_orders_with_user_and_children_data(self, results, username):
        orders = results[0][PERSON_RESERVATION_ENTITY_NAME][0][self.ORDERS_ENTITY_NAME]
        self.check_list_response(self.ENTITY_NAME, orders, len(orders))
        for order in orders:
            self.assertEqual(username, order.get(USERNAME_NAME, None))

    def _check_person_amount_consumptions_with_user_and_children_data(self, results, username):
        orders = results[0][PERSON_RESERVATION_ENTITY_NAME][0][self.ORDERS_ENTITY_NAME]
        if len(orders) > 0:
            amount_consumptions = orders[0][self.AMOUNT_CONSUMPTIONS_ENTITY_NAME]
            self.clean_test_data()
            self._add_data_values(PersonOrdersForPackageWithAmountConsumptionViewTestCase, self.CREATED_CONSUMPTIONS)
            self.ENTITIES_PER_REQUEST = 1
            self.check_list_response(self.ENTITY_NAME, amount_consumptions, len(self.CREATED_CONSUMPTIONS))
            for consumption in amount_consumptions:
                self.assertEqual(username, consumption.get(USERNAME_NAME, None))
        else:
            self.assertEqual(0, len(self.CREATED_CONSUMPTIONS))

    def setUp(self):
        super(PersonOrdersForPackageWithAmountConsumptionViewTestCase, self).setUp()
        create_test_client(self)

        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 1
        self.missing_amount = 0
        self.consumption_time_template = "2010010101010{0}"

        self.id_included_sku = None
        self.id_included_category = None
        self.id_consumed_sku = None
        self.expected_state_sku = OK_OVERFLOW_STATE
        self.expected_state_category = OK_OVERFLOW_STATE
        self.expected_state_consumed_sku = OK_OVERFLOW_STATE
        PersonOrdersForPackageWithAmountConsumptionViewTestCase.CREATED_CONSUMPTIONS = []

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        orders = copy.deepcopy(request_values.get(ORDERS_NAME))
        for order in orders:
            for consumption in order[AMOUNT_CONSUMPTIONS_NAME]:
                consumption[CONSUMPTION_TIME_NAME] = order[ORDER_TIME_NAME]
            cls.CREATED_CONSUMPTIONS += order[AMOUNT_CONSUMPTIONS_NAME]
            del order[AMOUNT_CONSUMPTIONS_NAME]
            del order[MONEY_CONSUMPTIONS_NAME]
            order[LOCATION_ID_NAME] = request_values.get(LOCATION_ID_NAME)
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

    def test_create_valid_amount_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_empty_person_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_orders_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_person_orders_and_query_them_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_orders_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_person_orders_and_query_them_with_reservations_endpoint_true_include_children_and_new_user(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        create_and_login_new_admin_user(self)
        self.create_and_assign_orders()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_orders_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_empty_consumptions(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_amount_consumptions_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_amount_consumptions_and_query_them_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 3
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_amount_consumptions_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_amount_consumptions_and_query_them_with_reservations_endpoint_true_include_children_and_new_user(self):
        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 3
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        create_and_login_new_admin_user(self)
        self.create_and_assign_orders()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_amount_consumptions_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_empty_amount_orders(self):
        self.ENTITIES_PER_REQUEST = 0
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.assign_field_value(ORDERS_NAME, [])
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_amount_orders_with_consumption_time_on_first_second_of_reservation(self):
        self.consumption_time_template = self.TEST_RESERVATION_INITIAL_DATE
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_amount_orders_with_consumption_time_on_last_second_of_reservation(self):
        self.consumption_time_template = self.TEST_RESERVATION_FINAL_DATE
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)

    def test_create_invalid_amount_orders_with_consumption_time_smaller_than_reservation_date(self):
        self.consumption_time_template = "20090101010101"
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_invalid_amount_orders_with_consumption_time_greater_than_reservation_date(self):
        self.consumption_time_template = "20200101010101"
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_amount_consumptions_exceeding_amount_limit(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.AMOUNT_PER_CONSUMPTION = 1000000
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_duplicated_amount_consumptions_on_different_request(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
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

    def test_create_duplicated_amount_consumptions_on_the_same_request(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
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

    def test_create_multiple_valid_amount_orders_with_multiple_consumptions(self):
        self.number_of_orders = 3
        self.number_of_consumptions_per_order = 2
        self.ENTITIES_PER_REQUEST = self.number_of_orders
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_amount_orders_without_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.assign_field_value(LOCATION_ID_NAME, None)
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)

    def test_create_valid_amount_orders_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3
        self.AMOUNT_PER_CONSUMPTION *= number_of_days

        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_create_valid_amount_orders_with_multiple_person_reservations(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        id_first_person = self.expected_ids[PERSON_ENTITY_NAME]
        first_reservation_orders = self.create_test_orders()
        self.TEST_PERSON_DOCUMENT_NUMBER += "10"
        self.create_person_reservation_for_last_package()
        second_reservation_orders = self.create_test_orders()
        self.assign_field_value(ORDERS_NAME, first_reservation_orders + second_reservation_orders)
        self.ENTITIES_PER_REQUEST = 2
        PersonOrdersForPackageWithAmountConsumptionViewTestCase.CHECK_BY_GET = False
        self.do_create_requests()
        PersonOrdersForPackageWithAmountConsumptionViewTestCase.CHECK_BY_GET = True

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     id_first_person))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_orders)
        self.check_all_consumptions_person_consumptions_and_person_orders(number_of_person_reservations=2)

    def test_try_create_invalid_orders_with_non_existent_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 2000)
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_with_non_existent_skus(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[AMOUNT_CONSUMPTIONS_NAME]:
                consumptions[SKU_ID_NAME] += 2000
        self.assign_field_value(ORDERS_NAME, orders)

        self.do_create_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_without_skus(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[AMOUNT_CONSUMPTIONS_NAME]:
                consumptions[SKU_ID_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)

        self.do_create_requests(expected_code=404, expected_internal_code=SKU_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_order_without_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        self.expected_ids[RESERVATION_ENTITY_NAME] = None
        self.create_and_assign_orders()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(check_person_reservation_entities=False)

    def test_try_create_invalid_order_with_non_existent_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        self.expected_ids[RESERVATION_ENTITY_NAME] += 2000
        self.create_and_assign_orders()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(
            expected_code_person_reservation_entities=404,
            expected_internal_code_person_reservation_entities=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_order_without_person_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = None
        self.create_and_assign_orders()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(check_person_reservation_entities=False)

    def test_try_create_invalid_order_with_non_existent_person_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] += 2000
        self.create_and_assign_orders()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(
            expected_code_person_reservation_entities=404,
            expected_internal_code_person_reservation_entities=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_orders_without_consumption_time(self):
        self.consumption_time_template = None
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_ORDER_TIME_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)

    def test_try_create_invalid_orders_with_invalid_consumption_time(self):
        self.consumption_time_template = "INVALID_DATE"
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_ORDER_TIME_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_without_missing_amount(self):
        self.missing_amount = None
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MISSING_AMOUNT_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_with_negative_missing_amount(self):
        self.missing_amount = -10
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_MISSING_AMOUNT_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)

    def test_try_create_invalid_orders_without_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_ORDERS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_without_amount_consumptions(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        orders = self.create_test_orders()
        for order in orders:
            order[AMOUNT_CONSUMPTIONS_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMPTIONS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_with_empty_money_and_amount_consumptions(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        orders = self.create_test_orders()
        for order in orders:
            order[AMOUNT_CONSUMPTIONS_NAME] = []
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_EMPTY_CONSUMPTIONS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_without_amount_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[AMOUNT_CONSUMPTIONS_NAME]:
                consumptions[AMOUNT_CONSUMED_NAME] = None
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_with_zero_amount_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[AMOUNT_CONSUMPTIONS_NAME]:
                consumptions[AMOUNT_CONSUMED_NAME] = 0
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_try_create_invalid_amount_orders_with_negative_amount_consumed(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        orders = self.create_test_orders()
        for order in orders:
            for consumptions in order[AMOUNT_CONSUMPTIONS_NAME]:
                consumptions[AMOUNT_CONSUMED_NAME] = -10
        self.assign_field_value(ORDERS_NAME, orders)
        self.do_create_requests(expected_code=400, expected_internal_code=ORDERS_INVALID_AMOUNT_CONSUMED_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_delete_valid_amount_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.do_delete_requests()
        PersonOrdersForPackageWithAmountConsumptionViewTestCase.CREATED_CONSUMPTIONS = []

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        self.check_all_consumptions_person_consumptions_and_person_orders()

    def test_query_person_orders_with_reservations_endpoint_after_delete_with_true_include_children_and_check_nothing_is_returned(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_orders_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_query_amount_consumptions_with_reservations_endpoint_after_delete_with_true_include_children_and_check_nothing_is_returned(self):
        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 3
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        PersonOrdersForPackageWithAmountConsumptionViewTestCase.CREATED_CONSUMPTIONS = []
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_amount_consumptions_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_delete_valid_persons_consumptions_and_create_them_again(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        PersonOrdersForPackageWithAmountConsumptionViewTestCase.CREATED_CONSUMPTIONS = []
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

    def test_try_delete_valid_orders_with_non_existent_client(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        original_id_client = self.expected_ids[CLIENT_ENTITY_NAME]
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(original_id_client,
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(original_id_client,
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                                                          expected_code_person_reservation_entities=404,
                                                                          expected_internal_code_person_reservation_entities=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_try_delete_valid_orders_with_non_existent_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.expected_ids[RESERVATION_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(
            expected_code_person_reservation_entities=404,
            expected_internal_code_person_reservation_entities=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_valid_orders_with_non_existent_person_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_all_consumptions_person_consumptions_and_person_orders(
            expected_code_person_reservation_entities=404,
            expected_internal_code_person_reservation_entities=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_non_existent_orders(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=ORDER_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)

    def test_check_available_funds_and_balance_for_new_reservation_with_sku(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        self.id_included_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_amount_sku = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_new_reservation_with_sku_and_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()

        self.id_included_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_amount_sku = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED * number_of_days
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_sku(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()

        self.do_create_requests()

        self.id_included_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_amount_sku = 0
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_reservation_with_excess_consumption_for_sku(self):
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()

        self.do_create_requests()
        self.consumption_time_template = "2010010101020{0}"
        self.create_and_assign_orders()
        self.missing_amount = self.AMOUNT_PER_CONSUMPTION
        self.do_create_requests()

        self.id_included_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_state_sku = OVERFLOWN_OVERFLOW_STATE
        self.expected_amount_sku = 0
        get_and_check_available_funds_for_amount(self)
        self.expected_amount_sku = -self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_reservation_with_consumption_on_non_included_sku_for_sku(self):
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        included_sku = self.expected_ids[SKU_ENTITY_NAME]
        create_test_sku(self, create_new_sku=True)
        self.create_and_assign_orders()

        self.missing_amount = self.AMOUNT_PER_CONSUMPTION
        self.do_create_requests()

        self.id_included_sku = included_sku
        self.expected_amount_sku = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        self.id_consumed_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_state_consumed_sku = OVERFLOWN_OVERFLOW_STATE
        self.expected_amount_consumed_sku = 0
        get_and_check_available_funds_for_amount(self)
        self.expected_amount_consumed_sku = -self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_sku_and_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.AMOUNT_PER_CONSUMPTION *= number_of_days
        self.create_and_assign_orders()

        self.do_create_requests()

        self.id_included_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_amount_sku = 0
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_previously_full_reservation_after_delete_for_sku(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()

        self.do_create_requests()
        self.do_delete_requests()

        self.id_included_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_amount_sku = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_new_reservation_with_sku_category(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku_category()

        self.id_included_category = self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY
        self.expected_amount_category = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_new_reservation_with_sku_category_and_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation_with_sku_category()

        self.id_included_category = self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY
        self.expected_amount_category = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED * number_of_days
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_sku_category(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku_category()

        self.create_and_assign_orders()
        self.do_create_requests()

        self.id_included_category = self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY
        self.expected_amount_category = 0
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_reservation_with_excess_consumption_for_sku_category(self):
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku_category()

        self.create_and_assign_orders()
        self.do_create_requests()
        self.consumption_time_template = "2010010101020{0}"
        self.create_and_assign_orders()
        self.missing_amount = self.AMOUNT_PER_CONSUMPTION
        self.do_create_requests()

        self.id_included_category = self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY
        self.expected_amount_category = 0
        self.id_consumed_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_state_consumed_sku = OVERFLOWN_OVERFLOW_STATE
        self.expected_amount_consumed_sku = 0
        get_and_check_available_funds_for_amount(self)
        self.expected_amount_consumed_sku = -self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_reservation_with_consumption_on_non_included_sku_for_sku_category(self):
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku_category()

        id_included_category = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        self.create_new_sku()
        self.create_and_assign_orders()

        self.missing_amount = self.AMOUNT_PER_CONSUMPTION
        self.do_create_requests()

        self.id_included_category = id_included_category
        self.expected_amount_category = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        self.id_consumed_sku = self.expected_ids[SKU_ENTITY_NAME]
        self.expected_state_consumed_sku = OVERFLOWN_OVERFLOW_STATE
        self.expected_amount_consumed_sku = 0
        get_and_check_available_funds_for_amount(self)
        self.expected_amount_consumed_sku = -self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_sku_category_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_without_restricted_consumption_and_person_reservation_with_sku_category()
        self.AMOUNT_PER_CONSUMPTION *= number_of_days
        self.create_and_assign_orders()

        self.do_create_requests()

        self.id_included_category = self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY
        self.expected_amount_category = 0
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_available_funds_and_balance_for_previously_full_reservation_after_delete_for_sku_category(self):
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku_category()
        self.create_and_assign_orders()

        self.do_create_requests()
        self.do_delete_requests()

        self.id_included_category = self.TEST_AMOUNT_CONSUMPTION_ID_SKU_CATEGORY
        self.expected_amount_category = self.TEST_AMOUNT_CONSUMPTION_AMOUNT_INCLUDED
        get_and_check_available_funds_for_amount(self)
        get_and_check_balance_for_amount(self)
        get_and_check_parent_balance_for_amount(self)

    def test_check_empty_entities_by_user_report_with_person_orders_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_orders(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_orders_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.consumption_time_template = "2010010201010{0}"
        self.create_and_assign_orders()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_orders_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self.clean_test_data()

        self.consumption_time_template = "2010010201010{0}"
        self.create_and_assign_orders()
        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests(do_get_and_check_results=False)
        expected_entities_by_user[self.TEST_USER_USERNAME] = self.original_entities
        users.add(self.TEST_USER_USERNAME)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_orders_after_delete(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_orders_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_person_orders_after_delete_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        users = {self.TEST_USER_USERNAME}
        self.do_delete_requests()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ORDERS_ENTITY_NAME,
                                              include_deleted=True)

    def test_check_empty_entities_by_user_report_with_person_amount_consumptions_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_amount_consumptions(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.CREATED_CONSUMPTIONS}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_amount_consumptions_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()

        self.consumption_time_template = "2010010201010{0}"
        self.create_and_assign_orders()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.CREATED_CONSUMPTIONS}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_amount_consumptions_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
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
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_amount_consumptions_after_delete(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_amount_consumptions_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_person_amount_consumptions_after_delete_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.CREATED_CONSUMPTIONS}
        users = {self.TEST_USER_USERNAME}
        self.do_delete_requests()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.AMOUNT_CONSUMPTIONS_ENTITY_NAME,
                                              include_deleted=True)

    def test_check_permissions_for_create_orders(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_WAITER_USER, \
            CLIENT_PROMOTER_USER
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_WAITER_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_orders(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER,
                         CLIENT_WAITER_USER, CLIENT_CASHIER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservation_orders(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER,
                         CLIENT_WAITER_USER, CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.SPECIFIC_RESOURCE_BASE_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[RESERVATION_ENTITY_NAME],
                                                     self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_all_consumptions(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
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
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER,
                         CLIENT_WAITER_USER, CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.PERSON_RESERVATIONS_CONSUMPTIONS_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                               self.expected_ids[RESERVATION_ENTITY_NAME],
                                                               self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_order(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_PROMOTER_USER,
                         CLIENT_WAITER_USER, CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_orders(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.create_package_without_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_delete_permissions(allowed_roles, required_locations)

    def check_all_consumptions_person_consumptions_and_person_orders(self, expected_code=200,
                                                                     expected_internal_code=None,
                                                                     check_person_reservation_entities=True,
                                                                     number_of_person_reservations=1,
                                                                     expected_code_person_reservation_entities=200,
                                                                     expected_internal_code_person_reservation_entities=None):
        original_orders = self.original_entities
        all_consumptions_url = self.ALL_CONSUMPTIONS_URL.format(self.expected_ids[CLIENT_ENTITY_NAME])

        self.clean_test_data()
        self._add_data_values(PersonOrdersForPackageWithAmountConsumptionViewTestCase, self.CREATED_CONSUMPTIONS)
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
                self._add_data_values(PersonOrdersForPackageWithAmountConsumptionViewTestCase, consumptions)
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
                self._add_data_values(PersonOrdersForPackageWithAmountConsumptionViewTestCase, original_orders)
                self.check_list_response(self.ENTITY_NAME, results_reservation_orders, len(original_orders))
            else:
                self.validate_error(results_reservation_orders, expected_internal_code_person_reservation_entities)

    def test_create_valid_amount_orders_and_check_report_with_both_times_included(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.calculate_expected_consumed_values_and_check_skus_consumed_report(initial_time="19900101010101",
                                                                               final_time="21000101010101")

    def test_create_valid_amount_orders_and_check_report_with_initial_time_included(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.calculate_expected_consumed_values_and_check_skus_consumed_report(initial_time="19900101010101")

    def test_create_valid_amount_orders_and_check_report_with_final_time_included(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.calculate_expected_consumed_values_and_check_skus_consumed_report(final_time="21000101010101")

    def test_create_valid_amount_orders_and_check_report_with_empty_time_range(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
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

    def test_create_valid_amount_orders_and_check_report_with_non_empty_non_included_time_range(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
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

    def test_create_valid_amount_orders_and_check_report_with_non_included_initial_time(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
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

    def test_create_valid_amount_orders_and_check_report_with_non_included_final_time(self):
        self.create_package_with_restricted_consumption_and_person_reservation_with_sku()
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

    def calculate_expected_consumed_values_and_check_skus_consumed_report(self, initial_time=None, final_time=None):
        ids_skus = set()
        expected_amounts_by_amount = dict()
        expected_amounts_by_money = dict()
        expected_money_by_sku_by_currency = dict()
        expected_amount_by_sku_by_currency = dict()
        for consumption in self.CREATED_CONSUMPTIONS:
            id_sku = consumption[SKU_ID_NAME]
            ids_skus.add(id_sku)
            if id_sku not in expected_amounts_by_amount:
                expected_amounts_by_amount[id_sku] = 0
                expected_money_by_sku_by_currency[id_sku] = dict()
                expected_amount_by_sku_by_currency[id_sku] = dict()
            expected_amounts_by_amount[id_sku] += consumption[AMOUNT_CONSUMED_NAME]

        get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                           expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                           initial_time, final_time)


def create_test_person_consumption_for_amount(test_class, create_new_consumption=False):
    test_class.TEST_ORDER_ID_LOCATION = getattr(test_class, 'TEST_PERSON_CONSUMPTION_ID_LOCATION', None)
    consumption = {SKU_ID_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_ID_SKU', None),
                   AMOUNT_CONSUMED_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_AMOUNT', 1),
                   MISSING_AMOUNT_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_MISSING_AMOUNT', 0)}
    order = {RESERVATION_ID_NAME: test_class.expected_ids[RESERVATION_ENTITY_NAME],
             PERSON_RESERVATION_ID_NAME: test_class.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
             AMOUNT_CONSUMPTIONS_NAME: [consumption],
             MONEY_CONSUMPTIONS_NAME: [],
             ORDER_TIME_NAME: getattr(test_class, 'TEST_PERSON_CONSUMPTION_TIME', "20100101010101")}
    test_class.TEST_ORDER_ORDERS = [order]
    return PersonOrdersForPackageWithAmountConsumptionViewTestCase.create_sample_entity_for_another_class(test_class,
                                                                                                          create_new_consumption)

if __name__ == '__main__':
    unittest.main()
