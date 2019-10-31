# -*- coding: utf-8 -*
import copy
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import ORDER_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testCurrencyViewTestCase import create_test_currency
from tests.testsCJM.testOrders import MONEY_CONSUMED_NAME, SKU_ID_NAME, AMOUNT_CONSUMED_NAME, LOCATION_ID_NAME, \
    CURRENCY_NAME, RESERVATION_ID_NAME, PERSON_RESERVATION_ID_NAME, ORDERS_NAME, \
    AMOUNT_CONSUMPTIONS_NAME, MONEY_CONSUMPTIONS_NAME, MISSING_MONEY_NAME, ORDER_TIME_NAME, OK_OVERFLOW_STATE, \
    OVERFLOWN_OVERFLOW_STATE, TEMPORALLY_OVERFLOWN_OVERFLOW_STATE, get_and_check_balance_for_money, \
    get_and_check_available_funds_for_money, get_and_check_parent_balance_for_money
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas.testMoneyTopoffsViewTestCase import create_test_money_topoff
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
    PERSON_RESERVATION_ENTITY_NAME, change_person_reservation_active_status
from tests.testsCJM.testReservas.testReservaViewTestCase import RESERVATION_ENTITY_NAME, create_test_reservation
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class PersonOrdersForPackageWithMoneyTopoffsViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = ORDER_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/person-orders/"
    SPECIFIC_RESOURCE_BASE_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/person-orders/"

    NUMBER_OF_ENTITIES = 1
    ENTITY_NAME = 'person-money-order'
    MONEY_PER_CONSUMPTION = 5
    AMOUNT_PER_CONSUMPTION = 1

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
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

    TEST_MONEY_TOPOFF_CURRENCY = None
    TEST_MONEY_TOPOFF_TRANSACTION_NUMBER = "123456"
    TEST_MONEY_TOPOFF_TOPOFF_TIME = "20100101010101"

    DEFAULT_CURRENCY = u"COP"

    def _create_package_location(self):
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]

    def create_package_with_restricted_consumption(self):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
        create_test_package(self, create_new_package=True)

    def create_package_without_restricted_consumption(self):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = False
        create_test_package(self, create_new_package=True)

    def create_person_reservation_for_last_package(self, activate_package=True):
        create_test_reservation(self)
        self.TEST_PERSON_RESERVATION_ID_RESERVATION = self.expected_ids[RESERVATION_ENTITY_NAME]

        create_test_person(self, create_new_person=True)
        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]

        create_test_person_reservation(self, create_new_person_reservation=True)
        if activate_package:
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)

    def create_money_topoff(self):
        total_consumptions = self.NUMBER_OF_ENTITIES * self.number_of_orders * self.number_of_consumptions_per_order
        self.TEST_MONEY_TOPOFF_MONEY = total_consumptions * self.MONEY_PER_CONSUMPTION
        create_test_money_topoff(self, create_new_topoff=True)

    def create_package_with_restricted_consumption_and_person_reservation(self):
        self.create_package_with_restricted_consumption()
        self.create_person_reservation_for_last_package()
        self.create_money_topoff()

    def create_package_without_restricted_consumption_and_person_reservation(self):
        self.create_package_without_restricted_consumption()
        self.create_person_reservation_for_last_package()
        self.create_money_topoff()

    def create_test_orders(self, create_new_sku_per_consumption=True, create_new_sku_per_order=False):
        if create_new_sku_per_consumption:
            create_new_sku_per_order = False

        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

        orders = []
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
                               MISSING_MONEY_NAME: self.missing_money,
                               MONEY_CONSUMED_NAME: self.MONEY_PER_CONSUMPTION}
                consumptions.append(consumption)
            order = {CURRENCY_NAME: self.currency_name,
                     RESERVATION_ID_NAME: self.expected_ids[RESERVATION_ENTITY_NAME],
                     PERSON_RESERVATION_ID_NAME: self.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
                     MONEY_CONSUMPTIONS_NAME: consumptions,
                     AMOUNT_CONSUMPTIONS_NAME: [],
                     ORDER_TIME_NAME: consumption_time}
            orders.append(order)

        return orders

    def create_and_assign_orders(self, create_new_sku_per_consumption=True, create_new_sku_per_order=False):
        orders = self.create_test_orders(create_new_sku_per_consumption, create_new_sku_per_order)
        self.assign_field_value(ORDERS_NAME, orders)

        create_test_location(self, create_new_location=True)
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])

    def setUp(self):
        super(PersonOrdersForPackageWithMoneyTopoffsViewTestCase, self).setUp()
        create_test_client(self)

        self.number_of_orders = 1
        self.number_of_consumptions_per_order = 1
        self.currency_name = self.DEFAULT_CURRENCY
        self.missing_money = 0
        self.consumption_time_template = "2010010102010{0}"

        self.included_currency = self.DEFAULT_CURRENCY
        self.consumed_currency = None
        self.expected_state = OK_OVERFLOW_STATE
        self.expected_state_consumed = OK_OVERFLOW_STATE

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        orders = copy.deepcopy(request_values.get(ORDERS_NAME))
        for order in orders:
            del order[MONEY_CONSUMPTIONS_NAME]
            del order[AMOUNT_CONSUMPTIONS_NAME]
            order[LOCATION_ID_NAME] = request_values.get(LOCATION_ID_NAME)
            if order.get(CURRENCY_NAME) is None:
                order[CURRENCY_NAME] = cls.DEFAULT_CURRENCY
        return orders

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def get_ancestor_entities_names_for_specific_resource(cls):
        return [CLIENT_ENTITY_NAME, RESERVATION_ENTITY_NAME, PERSON_RESERVATION_ENTITY_NAME]

    def test_check_available_funds_and_balance_for_new_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_money = self.TEST_MONEY_TOPOFF_MONEY
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_new_reservation_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"

        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_money = self.TEST_MONEY_TOPOFF_MONEY
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_new_reservation_and_custom_currency(self):
        self.create_package_with_restricted_consumption()
        create_test_currency(self, create_new_currency=True)
        self.TEST_MONEY_TOPOFF_CURRENCY = self.TEST_CURRENCY_NAME
        self.create_person_reservation_for_last_package()
        self.create_money_topoff()
        self.included_currency = self.TEST_CURRENCY_NAME
        self.expected_money = self.TEST_MONEY_TOPOFF_MONEY
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
        self.expected_state = OVERFLOWN_OVERFLOW_STATE
        self.expected_money = 0
        get_and_check_available_funds_for_money(self)
        self.expected_money = -self.TEST_MONEY_TOPOFF_MONEY
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
        self.expected_money = self.TEST_MONEY_TOPOFF_MONEY
        self.expected_money_consumed = 0
        self.expected_state_consumed = OVERFLOWN_OVERFLOW_STATE
        get_and_check_available_funds_for_money(self)
        self.expected_money_consumed = -self.TEST_MONEY_TOPOFF_MONEY
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_previously_full_reservation_after_delete(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()

        self.do_create_requests()
        self.do_delete_requests()
        self.expected_money = self.TEST_MONEY_TOPOFF_MONEY
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_topoffs_after_consumptions(self):
        self.TEST_MONEY_TOPOFF_TOPOFF_TIME = "20100101230101"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_orders()
        self.do_create_requests()
        self.expected_state = TEMPORALLY_OVERFLOWN_OVERFLOW_STATE
        self.expected_money = 0
        get_and_check_available_funds_for_money(self)
        get_and_check_balance_for_money(self)
        get_and_check_parent_balance_for_money(self)

if __name__ == '__main__':
    unittest.main()
