# -*- coding: utf-8 -*
import unittest

from CJM.services.validations import ENTITIES_PER_USER_INVALID_INCLUDE_DELETED_ERROR_CODE
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    RESERVATION_DOES_NOT_EXISTS_CODE, RESERVATION_INVALID_PAYMENT_CODE, \
    DELETE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_CODE, \
    DELETE_RESERVATION_WITH_CONSUMED_ACCESSES_CODE, DELETE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE, \
    RESERVATION_INVALID_RESERVATION_NUMBER_CODE, RESERVATION_INVALID_TRANSACTION_NUMBER_CODE, \
    RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_CODE, \
    DELETE_RESERVATION_WITH_TOPOFFS_CODE, SOCIAL_EVENT_DOES_NOT_EXISTS_CODE, \
    UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_CODE, UPDATE_RESERVATION_WITH_TOPOFFS_CODE, \
    UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE, UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_CODE, \
    TRANSACTIONS_PER_USER_INVALID_INITIAL_TIME_CODE, TRANSACTIONS_PER_USER_INVALID_FINAL_TIME_CODE, \
    ENTITIES_PER_USER_INVALID_KIND_CODE, ENTITIES_PER_USER_INVALID_INITIAL_TIME_CODE, \
    ENTITIES_PER_USER_INVALID_FINAL_TIME_CODE, RESERVATION_INVALID_FINAL_TIME_CODE, \
    RESERVATION_INVALID_INITIAL_TIME_CODE, RESERVATION_INVALID_INCLUDE_CHILDREN_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas import get_and_check_transactions_report, create_and_login_new_admin_user, \
    get_and_check_entities_by_user_report, USERNAME_NAME, ADMIN_USERNAME
from tests.testsCJM.testReservas.testEventoSocialViewTestCase import create_test_social_event, SOCIAL_EVENT_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class ReservaViewTestCase(FlaskClientBaseTestCase):
    PAYMENT_NAME = u"payment"
    IS_PAID_NAME = u"is-paid"
    ID_NAME = u"id"
    SOCIAL_EVENT_ID_NAME = u"id-social-event"
    RESERVATION_NUMBER_NAME = u"reservation-number"
    RESERVATION_NUMBER_PREFIX = u"#R"
    TRANSACTION_NUMBER_NAME = u"transaction-number"

    INITIAL_TIME_NAME = u"initial-time"
    FINAL_TIME_NAME = u"final-time"
    VALUE_NAME = u"value"
    TRANSACTION_TIME_NAME = u"transaction-time"
    USERNAME_NAME = u"username"
    TRANSACTIONS_NAME = u"transactions"
    TOTAL_NAME = u"total"

    ENTITY_DOES_NOT_EXISTS_CODE = RESERVATION_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/reservations/"

    ATTRIBUTES_NAMES_BY_FIELDS = {PAYMENT_NAME: "TEST_RESERVATION_PAYMENT",
                                  TRANSACTION_NUMBER_NAME: "TEST_RESERVATION_TRANSACTION_NUMBER",
                                  SOCIAL_EVENT_ID_NAME: "TEST_RESERVATION_ID_SOCIAL_EVENT"}
    PATCH_FIELDS = {PAYMENT_NAME, TRANSACTION_NUMBER_NAME}

    STARTING_ID = 1
    ENTITY_NAME = 'reservations'
    PERSONS_PER_RESERVATON = 5

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password_123"
    TEST_USER_ROLE = None

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

    TEST_PACKAGE_NAME = "Test package"
    TEST_PACKAGE_PRICE = 100.5
    TEST_PACKAGE_DESCRIPTION = "Test description"
    TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
    TEST_PACKAGE_VALID_FROM = "19900101010101"
    TEST_PACKAGE_VALID_THROUGH = "20100101010101"
    TEST_PACKAGE_DURATION = 5

    TEST_SOCIAL_EVENT_NAME = "Test event"
    TEST_SOCIAL_EVENT_DESCRIPTION = "Test description"
    TEST_SOCIAL_EVENT_INITIAL_DATE = "19950101010101"
    TEST_SOCIAL_EVENT_FINAL_DATE = "20050101010101"

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    AMOUNT_PER_CONSUMPTION = 1
    MONEY_PER_CONSUMPTION = 1

    def setUp(self):
        super(ReservaViewTestCase, self).setUp()
        self.TEST_SOCIAL_EVENT_CAPACITY = self.PERSONS_PER_RESERVATON * self.NUMBER_OF_ENTITIES

        create_test_client(self)
        create_test_location(self)
        create_test_social_event(self)

    def setup_scenario_with_valid_reservations_with_person_reservations(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)

    def setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)

    def setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_consumptions(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        from tests.testsCJM.testReservas.testAccessTopoffsViewTestCase import create_test_access_topoff
        from tests.testsCJM.testOrders.testPersonAccessesForPackageWithAccessesViewTestCase import \
            create_test_person_access
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()

        self.TEST_ACCESS_TOPOFF_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_TIME = self.TEST_PACKAGE_VALID_FROM
        create_test_location(self, create_new_location=True)
        self.TEST_ACCESS_TOPOFF_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_ACCESS_ID_LOCATION = self.TEST_ACCESS_TOPOFF_ID_LOCATION

        original_reservation_id = self.expected_ids[self.ENTITY_NAME]
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)
            create_test_access_topoff(self, create_new_topoff=True)
            create_test_person_access(self, create_new_access=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)

        self.expected_ids[self.ENTITY_NAME] = original_reservation_id

    def setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_consumptions(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        from tests.testsCJM.testReservas.testAmountTopoffsViewTestCase import create_test_amount_topoff
        from tests.testsCJM.testOrders.testPersonOrdersForPackageWithAmountConsumptionViewTestCase import \
            create_test_person_consumption_for_amount
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()

        self.TEST_AMOUNT_TOPOFF_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_AMOUNT_TOPOFF_ID_SKU_CATEGORY = None
        create_test_sku_category(self, create_new_category=True)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self, create_new_sku=True)
        self.TEST_AMOUNT_TOPOFF_ID_SKU = self.expected_ids[SKU_ENTITY_NAME]

        self.TEST_PERSON_CONSUMPTION_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_CONSUMPTION_ID_SKU = self.TEST_AMOUNT_TOPOFF_ID_SKU
        create_test_location(self, create_new_location=True)
        self.TEST_PERSON_CONSUMPTION_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_PACKAGE_VALID_FROM

        original_reservation_id = self.expected_ids[self.ENTITY_NAME]
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)
            create_test_amount_topoff(self, create_new_topoff=True)
            create_test_person_consumption_for_amount(self, create_new_consumption=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)

        self.expected_ids[self.ENTITY_NAME] = original_reservation_id

    def setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_consumptions(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        from tests.testsCJM.testReservas.testMoneyTopoffsViewTestCase import create_test_money_topoff
        from tests.testsCJM.testOrders.testPersonOrdersForPackageWithMoneyConsumptionViewTestCase import \
            create_test_person_consumption_for_money
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()

        self.TEST_MONEY_TOPOFF_MONEY = self.MONEY_PER_CONSUMPTION
        self.TEST_MONEY_TOPOFF_CURRENCY = None

        self.TEST_PERSON_CONSUMPTION_MONEY = self.TEST_MONEY_TOPOFF_MONEY
        create_test_sku_category(self, create_new_category=True)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self, create_new_sku=True)
        self.TEST_PERSON_CONSUMPTION_ID_SKU = self.expected_ids[SKU_ENTITY_NAME]
        create_test_location(self, create_new_location=True)
        self.TEST_PERSON_CONSUMPTION_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_CONSUMPTION_CURRENCY = None
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_PACKAGE_VALID_FROM

        original_reservation_id = self.expected_ids[self.ENTITY_NAME]
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)
            create_test_money_topoff(self, create_new_topoff=True)
            create_test_person_consumption_for_money(self, create_new_consumption=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)

        self.expected_ids[self.ENTITY_NAME] = original_reservation_id

    def setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_topoffs(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        from tests.testsCJM.testReservas.testAccessTopoffsViewTestCase import create_test_access_topoff
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()

        self.TEST_ACCESS_TOPOFF_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_TIME = self.TEST_PACKAGE_VALID_FROM
        create_test_location(self, create_new_location=True)
        self.TEST_ACCESS_TOPOFF_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_ACCESS_ID_LOCATION = self.TEST_ACCESS_TOPOFF_ID_LOCATION

        original_reservation_id = self.expected_ids[self.ENTITY_NAME]
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)
            create_test_access_topoff(self, create_new_topoff=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)

        self.expected_ids[self.ENTITY_NAME] = original_reservation_id

    def setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_topoffs(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        from tests.testsCJM.testReservas.testAmountTopoffsViewTestCase import create_test_amount_topoff
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()

        self.TEST_AMOUNT_TOPOFF_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_AMOUNT_TOPOFF_ID_SKU_CATEGORY = None
        create_test_sku_category(self, create_new_category=True)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self, create_new_sku=True)
        self.TEST_AMOUNT_TOPOFF_ID_SKU = self.expected_ids[SKU_ENTITY_NAME]

        self.TEST_PERSON_CONSUMPTION_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_CONSUMPTION_ID_SKU = self.TEST_AMOUNT_TOPOFF_ID_SKU
        create_test_location(self, create_new_location=True)
        self.TEST_PERSON_CONSUMPTION_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_PACKAGE_VALID_FROM

        original_reservation_id = self.expected_ids[self.ENTITY_NAME]
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)
            create_test_amount_topoff(self, create_new_topoff=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)

        self.expected_ids[self.ENTITY_NAME] = original_reservation_id

    def setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_topoffs(self):
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
            change_person_reservation_active_status, PERSON_RESERVATION_ENTITY_NAME
        from tests.testsCJM.testReservas.testMoneyTopoffsViewTestCase import create_test_money_topoff
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.do_create_requests()

        self.TEST_MONEY_TOPOFF_MONEY = self.MONEY_PER_CONSUMPTION
        self.TEST_MONEY_TOPOFF_CURRENCY = None

        self.TEST_PERSON_CONSUMPTION_MONEY = self.TEST_MONEY_TOPOFF_MONEY
        create_test_sku_category(self, create_new_category=True)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self, create_new_sku=True)
        self.TEST_PERSON_CONSUMPTION_ID_SKU = self.expected_ids[SKU_ENTITY_NAME]
        create_test_location(self, create_new_location=True)
        self.TEST_PERSON_CONSUMPTION_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_CONSUMPTION_CURRENCY = None
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_PACKAGE_VALID_FROM

        original_reservation_id = self.expected_ids[self.ENTITY_NAME]
        for reservation in self.original_entities:
            self.expected_ids[self.ENTITY_NAME] = reservation[self.ID_NAME]
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            self.TEST_PERSON_DOCUMENT_NUMBER += "1"
            create_test_person(self, create_new_person=True)
            self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
            create_test_person_reservation(self, create_new_person_reservation=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], True)
            create_test_money_topoff(self, create_new_topoff=True)
            change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)

        self.expected_ids[self.ENTITY_NAME] = original_reservation_id

    def _check_reservations_with_user_and_children_data(self, results, username):
        self.check_list_response(self.ENTITY_NAME, results, len(results))
        for reservation in results:
            self.assertEqual(username, reservation.get(USERNAME_NAME, None))

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        return values

    @classmethod
    def get_entity_values_templates_for_create(cls):
        values = dict()
        values[cls.TRANSACTION_NUMBER_NAME] = "123456{0}"
        values[cls.PAYMENT_NAME] = str(cls.TEST_PACKAGE_PRICE) + "{0}"
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        return values

    @classmethod
    def get_entity_values_templates_for_update(cls):
        values = dict()
        values[cls.TRANSACTION_NUMBER_NAME] = "78943{0}"
        values[cls.PAYMENT_NAME] = str(cls.TEST_PACKAGE_PRICE) + "{0}"
        return values

    @classmethod
    def get_types_for_template_parsing(cls):
        values = dict()
        values[cls.PAYMENT_NAME] = float
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        if request_values.get(cls.PAYMENT_NAME) is None:
            request_values[cls.PAYMENT_NAME] = 0

        request_values[cls.IS_PAID_NAME] = request_values.get(cls.TRANSACTION_NUMBER_NAME) is not None
        return request_values

    @classmethod
    def validate_additional_values(cls, running_entity, result):
        running_entity.assertEqual(result[cls.IS_PAID_NAME], result.get(cls.TRANSACTION_NUMBER_NAME) is not None)
        running_entity.assertTrue(cls.RESERVATION_NUMBER_NAME in result)
        running_entity.assertTrue(result[cls.RESERVATION_NUMBER_NAME].startswith(cls.RESERVATION_NUMBER_PREFIX))

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_try_query_non_existent_reservation(self):
        self.request_specific_resource_and_check_result(1, expected_code=404)

    def test_empty_reservations_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_reservations(self):
        self.do_create_requests()

    def test_create_valid_reservations_without_transaction_number_and_payment(self):
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.do_create_requests()

    def test_create_valid_reservations_without_transaction_number_and_zero_payment(self):
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.assign_field_value(self.PAYMENT_NAME, 0)
        self.do_create_requests()

    def test_create_valid_reservations_without_payment(self):
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.do_create_requests()

    def test_create_valid_reservations_with_zero_payment(self):
        self.assign_field_value(self.PAYMENT_NAME, 0)
        self.do_create_requests()

    def test_create_valid_reservations_with_social_event(self):
        self.assign_field_value(self.SOCIAL_EVENT_ID_NAME, self.expected_ids[SOCIAL_EVENT_ENTITY_NAME])
        self.do_create_requests()

    def test_create_valid_reservations_and_query_them_with_false_include_and_default_user(self):
        self.do_create_requests()
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=false")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_query_empty_reservations_with_true_include_children(self):
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=true")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_default_user(self):
        self.do_create_requests()
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=true")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_new_user(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=true")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_try_consult_reservations_by_document_with_invalid_include_children(self):
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=INVALID")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_INCLUDE_CHILDREN_CODE)

    def test_create_valid_persons_reservations_and_consult_reservations_with_included_time_range(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        results = self.do_get_request((self.RESOURCE_URL + "?initial-time={1}&final-time={2}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "19600101010101",
                                              "30000101010101"))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_reservations_with_initial_time_equals_to_reservation_final_date(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        results = self.do_get_request((self.RESOURCE_URL + "?initial-time={1}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_SOCIAL_EVENT_FINAL_DATE))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_reservations_with_final_time_equals_to_reservation_initial_date(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        results = self.do_get_request((self.RESOURCE_URL + "?final-time={1}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_SOCIAL_EVENT_INITIAL_DATE))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_reservations_with_initial_time_bigger_than_reservation_final_date(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        results = self.do_get_request((self.RESOURCE_URL + "?initial-time={1}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "30000101010101"))
        self.clean_test_data()
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_valid_persons_reservations_and_consult_reservations_with_final_time_bigger_than_reservation_initial_date(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        results = self.do_get_request((self.RESOURCE_URL + "?final-time={1}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "19600101010101"))
        self.clean_test_data()
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_consult_reservations_by_document_with_invalid_initial_time(self):
        results = self.do_get_request((self.RESOURCE_URL + "?initial-time={1}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "INVALID_DATE"),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_INITIAL_TIME_CODE)

    def test_try_consult_reservations_by_document_with_invalid_final_time(self):
        results = self.do_get_request((self.RESOURCE_URL + "?final-time={1}")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "INVALID_DATE"),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_FINAL_TIME_CODE)

    def test_create_valid_reservation_and_query_by_reservation_number(self):
        self.original_number_reservations = self.NUMBER_OF_ENTITIES
        self.NUMBER_OF_ENTITIES = 1

        list_results = self.do_create_requests()

        result = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                     .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                             self.RESERVATION_NUMBER_NAME,
                                             list_results[0][self.RESERVATION_NUMBER_NAME]))
        self.check_list_response(self.ENTITY_NAME, [result], 1)

        self.NUMBER_OF_ENTITIES = self.original_number_reservations

    def test_try_query_by_reservation_number_without_reservation_number(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]), expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_RESERVATION_NUMBER_CODE)

    def test_try_query_by_reservation_number_with_reservation_number_not_starting_with_num_r(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.RESERVATION_NUMBER_NAME,
                                              "RES1"),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_RESERVATION_NUMBER_CODE)

    def test_try_query_by_reservation_number_with_reservation_number_without_prefix(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.RESERVATION_NUMBER_NAME,
                                              "1"),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_RESERVATION_NUMBER_CODE)

    def test_try_query_by_reservation_number_with_reservation_number_without_number(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.RESERVATION_NUMBER_NAME,
                                              self.RESERVATION_NUMBER_PREFIX),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_RESERVATION_NUMBER_CODE)

    def test_try_query_by_reservation_number_with_short_invalid_reservation_number(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.RESERVATION_NUMBER_NAME,
                                              self.RESERVATION_NUMBER_PREFIX[:-1]),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_RESERVATION_NUMBER_CODE)

    def test_try_query_by_reservation_number_with_empty_reservation_number(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.RESERVATION_NUMBER_NAME,
                                              ""),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_RESERVATION_NUMBER_CODE)

    def test_try_query_by_reservation_number_with_non_existent_reservation(self):
        results = self.do_get_request("/clients/{0}/reservation-by-number/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.RESERVATION_NUMBER_NAME,
                                              self.RESERVATION_NUMBER_PREFIX + "1"),
                                      expected_code=404)
        self.validate_error(results, RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_reservations_with_empty_transaction_number(self):
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=RESERVATION_INVALID_TRANSACTION_NUMBER_CODE)

    def test_try_create_invalid_reservations_without_transaction_number_and_with_payment(self):
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_CODE)

    def test_create_invalid_reservations_with_negative_payment(self):
        self.assign_field_value(self.PAYMENT_NAME, -100)
        self.do_create_requests(expected_code=400, expected_internal_code=RESERVATION_INVALID_PAYMENT_CODE)

    def test_create_invalid_reservations_with_non_existent_social_event(self):
        self.assign_field_value(self.SOCIAL_EVENT_ID_NAME, self.expected_ids[SOCIAL_EVENT_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404, expected_internal_code=SOCIAL_EVENT_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_reservations_with_invalid_social_event_id(self):
        self.assign_field_value(self.SOCIAL_EVENT_ID_NAME, "INVALID_EVENT")
        self.do_create_requests(expected_code=404, expected_internal_code=SOCIAL_EVENT_DOES_NOT_EXISTS_CODE)

    def test_set_valid_unpaid_reservations_as_paid(self):
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_create_requests()

        self.assign_field_value(self.PAYMENT_NAME, self.TEST_PACKAGE_PRICE)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, "123456")
        self.do_patch_requests()

    def test_query_reservations_after_patch_with_true_include_children_and_check_they_return_the_user_which_created_them(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.do_patch_requests()
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=true")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_set_valid_unpaid_reservations_as_unpaid(self):
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_create_requests()

        self.do_patch_requests()

    def test_set_valid_paid_reservations_as_paid(self):
        self.assign_field_value(self.PAYMENT_NAME, self.TEST_PACKAGE_PRICE)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, "123456")
        self.do_create_requests()

        self.assign_field_value(self.PAYMENT_NAME, None)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_patch_requests()

    def test_set_valid_paid_reservations_as_unpaid(self):
        self.assign_field_value(self.PAYMENT_NAME, self.TEST_PACKAGE_PRICE)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, "123456")
        self.do_create_requests()

        self.assign_field_value(self.PAYMENT_NAME, None)
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_patch_requests()

    def test_set_reservations_as_unpaid_without_transaction_number_and_payment(self):
        self.do_create_requests()

        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.do_update_requests()

    def test_set_reservations_payment_without_transaction_number_and_zero_payment(self):
        self.do_create_requests()

        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.assign_field_value(self.PAYMENT_NAME, 0)
        self.do_patch_requests()

    def test_try_set_reservations_payment_with_negative_value(self):
        self.do_create_requests()

        self.assign_field_value(self.PAYMENT_NAME, -100)
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=RESERVATION_INVALID_PAYMENT_CODE)

    def test_try_set_reservations_payment_with_empty_transaction_number(self):
        self.do_create_requests()

        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, "")
        self.do_patch_requests(expected_code=400, expected_internal_code=RESERVATION_INVALID_TRANSACTION_NUMBER_CODE)

    def test_try_set_reservations_payment_without_transaction_number_and_with_payment(self):
        self.do_create_requests()

        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_CODE)

    def test_try_set_reservations_payment_with_wrong_client_id(self):
        self.do_create_requests()
        create_test_client(self, create_new_client=True)

        self.assign_field_value(self.PAYMENT_NAME, self.TEST_PACKAGE_PRICE)
        self.do_patch_requests(expected_code=404,
                               expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                               do_get_and_check_results=False)

    def test_try_set_reservations_payment_on_non_existent_reservations(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()

        self.assign_field_value(self.PAYMENT_NAME, self.TEST_PACKAGE_PRICE)
        self.do_patch_requests(expected_code=404,
                               expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_update_payment_info_for_reservations_with_active_person_reservations(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_CODE)

    def test_try_update_payment_info_for_reservations_with_inactive_person_reservations_with_access_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_consumptions()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_CODE)

    def test_try_update_payment_info_for_reservations_with_inactive_person_reservations_with_amount_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_consumptions()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_update_payment_info_for_reservations_with_inactive_person_reservations_with_money_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_consumptions()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_update_payment_info_for_reservations_with_inactive_person_reservations_with_access_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_topoffs()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_payment_info_for_reservations_with_inactive_person_reservations_with_amount_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_topoffs()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_payment_info_for_reservations_with_inactive_person_reservations_with_money_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_topoffs()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=UPDATE_RESERVATION_WITH_TOPOFFS_CODE)

    def test_update_valid_reservations_with_package_without_event(self):
        self.do_create_requests()
        self.do_update_requests()

    def test_query_reservations_after_update_with_true_include_children_and_check_they_return_the_user_which_created_them(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.do_update_requests()
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=true")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_update_valid_reservations_without_payment(self):
        self.do_create_requests()
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.do_update_requests()

    def test_update_valid_reservations_with_zero_payment(self):
        self.do_create_requests()
        self.assign_field_value(self.PAYMENT_NAME, 0)
        self.do_update_requests()

    def test_update_valid_reservations_without_transaction_number_and_payment(self):
        self.do_create_requests()
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.assign_field_value(self.PAYMENT_NAME, None)
        self.do_update_requests()

    def test_update_valid_reservations_without_transaction_number_and_zero_payment(self):
        self.do_create_requests()
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.assign_field_value(self.PAYMENT_NAME, 0)
        self.do_update_requests()

    def test_try_update_invalid_reservations_with_empty_transaction_number(self):
        self.do_create_requests()
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=RESERVATION_INVALID_TRANSACTION_NUMBER_CODE)

    def test_try_update_invalid_reservations_without_transaction_number_and_with_payment(self):
        self.do_create_requests()
        self.assign_field_value(self.TRANSACTION_NUMBER_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=RESERVATION_WITHOUT_TRANSACTION_NUMBER_AND_WITH_PAYMENT_CODE)

    def test_update_invalid_reservations_with_negative_payment(self):
        self.do_create_requests()
        self.assign_field_value(self.PAYMENT_NAME, -100)
        self.do_update_requests(expected_code=400, expected_internal_code=RESERVATION_INVALID_PAYMENT_CODE)

    def test_update_invalid_reservations_with_wrong_client_id(self):
        self.do_create_requests()
        create_test_client(self, create_new_client=True)
        self.do_update_requests(expected_code=404,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_update_invalid_non_existent_reservations(self):
        self.do_create_requests()

        self.change_ids_to_non_existent_entities()

        self.do_update_requests(expected_code=404,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_update_valid_reservations_with_active_person_reservations(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_CODE)

    def test_try_update_valid_reservations_with_inactive_person_reservations_with_access_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_consumptions()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_CONSUMED_ACCESSES_CODE)

    def test_try_update_valid_reservations_with_inactive_person_reservations_with_amount_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_consumptions()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_update_valid_reservations_with_inactive_person_reservations_with_money_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_consumptions()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_update_valid_reservations_with_inactive_person_reservations_with_access_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_topoffs()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_valid_reservations_with_inactive_person_reservations_with_amount_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_topoffs()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_valid_reservations_with_inactive_person_reservations_with_money_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_topoffs()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_invalid_reservations_with_non_existent_id_event(self):
        self.do_create_requests()

        self.assign_field_value(self.SOCIAL_EVENT_ID_NAME, self.expected_ids[SOCIAL_EVENT_ENTITY_NAME] + 1)
        self.do_update_requests(expected_code=404,
                                expected_internal_code=SOCIAL_EVENT_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_reservations_with_invalid_id_event(self):
        self.do_create_requests()

        self.assign_field_value(self.SOCIAL_EVENT_ID_NAME, "INVALID_EVENT")
        self.do_update_requests(expected_code=404,
                                expected_internal_code=SOCIAL_EVENT_DOES_NOT_EXISTS_CODE)

    def test_delete_valid_reservations_without_person_reservations(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_query_reservations_after_delete_with_true_include_children_and_check_nothing_is_returned(self):
        self.do_create_requests()
        self.do_delete_requests()
        results = self.do_get_request((self.RESOURCE_URL + "?include-children=true")
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_delete_valid_reservations_with_inactive_person_reservations(self):
        self.setup_scenario_with_valid_reservations_with_person_reservations()
        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

        self.do_delete_requests()

    def test_try_delete_valid_reservations_with_active_person_reservations(self):
        self.setup_scenario_with_valid_reservations_with_one_active_and_one_inactive_person_reservation()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_ACTIVE_PERSON_RESERVATION_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_valid_reservations_with_inactive_person_reservations_with_access_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_consumptions()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_CONSUMED_ACCESSES_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_valid_reservations_with_inactive_person_reservations_with_amount_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_consumptions()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_valid_reservations_with_inactive_person_reservations_with_money_consumptions(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_consumptions()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_valid_reservations_with_inactive_person_reservations_with_access_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_access_topoffs()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_TOPOFFS_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_valid_reservations_with_inactive_person_reservations_with_amount_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_amount_topoffs()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_TOPOFFS_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_valid_reservations_with_inactive_person_reservations_with_money_topoffs(self):
        self.setup_scenario_with_valid_reservations_with_one_person_reservation_with_money_topoffs()
        self.do_delete_requests(expected_code=400,
                                expected_internal_code=DELETE_RESERVATION_WITH_TOPOFFS_CODE)

        results_person_reservations = self.do_get_request(self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME]) + "persons-reservations/")
        self.assertEqual(2 * self.NUMBER_OF_ENTITIES, len(results_person_reservations))

    def test_try_delete_invalid_reservations_with_wrong_id_client(self):
        self.do_create_requests()
        create_test_client(self, create_new_client=True)
        self.do_delete_requests(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_delete_invalid_non_existent_reservations(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_check_empty_transactions_report(self):
        create_and_login_new_admin_user(self)
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_empty_transactions_report_with_transactions_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        create_test_client(self, create_new_client=True)
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}
        num_transactions = {self.TEST_USER_USERNAME: self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations_and_dates_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}
        num_transactions = {self.TEST_USER_USERNAME: self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          "19900101010101", "21000101010101")

    def test_check_transactions_report_with_reservations_and_empty_dates_range(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          "21000101010101", "19900101010101")

    def test_check_transactions_report_with_reservations_and_dates_not_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          "19900101010101", "19910101010101")

    def test_check_transactions_report_with_reservations_and_initial_date_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}
        num_transactions = {self.TEST_USER_USERNAME: self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          "19900101010101", None)

    def test_check_transactions_report_with_reservations_and_initial_date_not_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          "21000101010101", None)

    def test_check_transactions_report_with_reservations_and_final_date_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}
        num_transactions = {self.TEST_USER_USERNAME: self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          None, "21000101010101")

    def test_check_transactions_report_with_reservations_and_final_date_not_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          None, "19900101010101")

    def test_check_transactions_report_with_invalid_initial_date(self):
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          "INVALID_TIME", None, expected_code=400,
                                          expected_internal_code=TRANSACTIONS_PER_USER_INVALID_INITIAL_TIME_CODE)

    def test_check_transactions_report_with_invalid_final_date(self):
        expected_transactions = dict()
        expected_values = dict()
        num_transactions = dict()
        users = set()
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users,
                                          None, "INVALID_TIME", expected_code=400,
                                          expected_internal_code=TRANSACTIONS_PER_USER_INVALID_FINAL_TIME_CODE)

    def test_check_transactions_report_with_multiple_reservations_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}

        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)

        num_transactions = {self.TEST_USER_USERNAME: 2 * self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_multiple_reservations_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}
        num_transactions = {self.TEST_USER_USERNAME:  self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions[self.TEST_USER_USERNAME] = set(self.get_data_values(self.ENTITY_NAME,
                                                                                  self.TRANSACTION_NUMBER_NAME))
        expected_values[self.TEST_USER_USERNAME] = set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))
        num_transactions[self.TEST_USER_USERNAME] = self.NUMBER_OF_ENTITIES
        users.add(self.TEST_USER_USERNAME)
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations_after_update(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}

        self.do_update_requests()
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)

        num_transactions = {self.TEST_USER_USERNAME: 2 * self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations_after_multiple_updates(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}

        self.do_update_requests()
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)
        self.do_update_requests()
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)

        num_transactions = {self.TEST_USER_USERNAME: 3 * self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations_after_patch(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}

        self.do_patch_requests()
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)

        num_transactions = {self.TEST_USER_USERNAME: 2 * self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations_after_multiple_patchs(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}

        self.do_patch_requests()
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)
        self.do_patch_requests()
        expected_transactions[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME,
                                                                               self.TRANSACTION_NUMBER_NAME)
        expected_values[self.TEST_USER_USERNAME] |= self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME)

        num_transactions = {self.TEST_USER_USERNAME: 3 * self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_transactions_report_with_reservations_after_delete(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_transactions = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME,
                                                                                   self.TRANSACTION_NUMBER_NAME))}
        expected_values = {self.TEST_USER_USERNAME: set(self.get_data_values(self.ENTITY_NAME, self.PAYMENT_NAME))}
        self.do_delete_requests()
        expected_values[self.TEST_USER_USERNAME] |= {-payment for payment in expected_values[self.TEST_USER_USERNAME]}
        num_transactions = {self.TEST_USER_USERNAME: 2 * self.NUMBER_OF_ENTITIES}
        users = {self.TEST_USER_USERNAME}
        get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions, users)

    def test_check_empty_entities_by_user_report(self):
        create_and_login_new_admin_user(self)
        expected_entities_by_user = dict()
        users = set()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_empty_entities_by_user_report_with_reservations_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_reservations(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_reservations_and_dates_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              "19900101010101", "21000101010101")

    def test_check_entities_by_user_report_with_reservations_and_empty_dates_range(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              "21000101010101", "19900101010101")

    def test_check_entities_by_user_report_with_reservations_and_dates_not_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              "19900101010101", "19910101010101")

    def test_check_entities_by_user_report_with_reservations_and_initial_date_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              "19900101010101", None)

    def test_check_entities_by_user_report_with_reservations_and_initial_date_not_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              "21000101010101", None)

    def test_check_entities_by_user_report_with_reservations_and_final_date_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              None, "21000101010101")

    def test_check_entities_by_user_report_with_reservations_and_final_date_not_included(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              None, "19900101010101")

    def test_check_entities_by_user_report_with_multiple_reservations_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()

        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_reservations_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self.clean_test_data()

        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests(do_get_and_check_results=False)
        expected_entities_by_user[self.TEST_USER_USERNAME] = self.original_entities
        users.add(self.TEST_USER_USERNAME)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_reservations_after_update(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        self.do_update_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_reservations_after_patch(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        self.do_patch_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_reservations_after_delete(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_reservations_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_reservations_after_delete_requesting_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        users = {self.TEST_USER_USERNAME}
        self.do_delete_requests()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=True)

    def test_check_entities_by_user_report_with_invalid_kind(self):
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, "INVALID_KIND",
                                              None, None, expected_code=400,
                                              expected_internal_code=ENTITIES_PER_USER_INVALID_KIND_CODE)

    def test_check_entities_by_user_report_with_invalid_include_deleted(self):
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              None, None, include_deleted="INVALID_BOOL", expected_code=400,
                                              expected_internal_code=ENTITIES_PER_USER_INVALID_INCLUDE_DELETED_ERROR_CODE)

    def test_check_entities_by_user_report_without_kind(self):
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, None,
                                              None, None, expected_code=400,
                                              expected_internal_code=ENTITIES_PER_USER_INVALID_KIND_CODE)

    def test_check_entities_by_user_report_with_invalid_initial_date(self):
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              "INVALID_TIME", None, expected_code=400,
                                              expected_internal_code=ENTITIES_PER_USER_INVALID_INITIAL_TIME_CODE)

    def test_check_entities_by_user_report_with_invalid_final_date(self):
        expected_entities_by_user = dict()
        users = {}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              None, "INVALID_TIME", expected_code=400,
                                              expected_internal_code=ENTITIES_PER_USER_INVALID_FINAL_TIME_CODE)

    def test_check_permissions_for_create_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE,\
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_reservation(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_reservation_balance(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])+"balance/"
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_reservations_by_person_document(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        create_test_person(self)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = "/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                                                    self.TEST_PERSON_DOCUMENT_TYPE,
                                                                                                    self.TEST_PERSON_DOCUMENT_NUMBER)
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_reservations_by_reservation_number(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        reservation_number = self.original_entities[0][self.RESERVATION_NUMBER_NAME]
        url = "/clients/{0}/reservation-by-number/?{1}={2}".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                   self.RESERVATION_NUMBER_NAME,
                                                                   reservation_number)
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_transactions_per_user(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        url = "/clients/{0}/transactions-per-user/".format(self.expected_ids[CLIENT_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_entities_per_user(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        url = "/clients/{0}/entities-per-user/?kind={1}".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                self.ENTITY_NAME)
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_patch_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_patch_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_delete_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)


RESERVATION_ENTITY_NAME = ReservaViewTestCase.ENTITY_NAME


def create_test_reservation(test_class, create_new_reservation=False):
    return ReservaViewTestCase.create_sample_entity_for_another_class(test_class, create_new_reservation)


if __name__ == '__main__':
    unittest.main()
