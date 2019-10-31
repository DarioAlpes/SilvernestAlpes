# -*- coding: utf-8 -*
import unittest

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.validations import validate_datetime, DEFAULT_DATETIME_FORMAT
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import validate_error, PERSON_DOES_NOT_EXISTS_CODE, \
    RESERVATION_INVALID_DOCUMENT_TYPE_CODE, RESERVATION_INVALID_DOCUMENT_NUMBER_CODE, CLIENT_DOES_NOT_EXISTS_CODE, \
    PACKAGE_DOES_NOT_EXISTS_CODE, RESERVATION_DOES_NOT_EXISTS_CODE, PERSON_RESERVATION_DOES_NOT_EXISTS_CODE, \
    DELETE_HOLDER_PERSON_RESERVATION_CODE, \
    PERSON_RESERVATION_INVALID_ACTIVE_CODE, PERSON_RESERVATION_TRYING_TO_ACTIVATE_UNPAID_RESERVATION_CODE, \
    DELETE_ACTIVE_PERSON_RESERVATION_CODE, DELETE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_CODE, \
    DELETE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE, PERSON_RESERVATION_INVALID_DOCUMENT_TYPE_CODE, \
    PERSON_RESERVATION_INVALID_DOCUMENT_NUMBER_CODE, DELETE_PERSON_RESERVATION_WITH_TOPOFFS_CODE, \
    UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_CODE, UPDATE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE, \
    UPDATE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_CODE, UPDATE_ACTIVE_PERSON_RESERVATION_CODE, \
    RESERVATION_INVALID_BASE_TIME_CODE, PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE, \
    PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FROM_CODE, \
    PERSON_RESERVATION_RANGE_OF_DATES_THROUGH_GREATER_THAN_FINAL_CODE, PERSON_RESERVATION_INVALID_FINAL_DATE_CODE, \
    PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FINAL_CODE, \
    PERSON_RESERVATION_INVALID_RANGE_OF_DATES_INITIAL_SMALLER_THAN_EVENT_INITIAL_CODE, \
    PERSON_RESERVATION_INVALID_RANGE_OF_DATES_FINAL_BIGGER_THAN_EVENT_FINAL_CODE, \
    PERSON_RESERVATION_INVALID_BASE_TIME_CODE, PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_INITIAL_TIME_CODE, \
    PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_FINAL_TIME_CODE, \
    PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_CODE, \
    PERSON_RESERVATION_TRYING_TO_ACTIVATE_ALREADY_ACTIVE_PERSON_RESERVATION_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package, PACKAGE_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas import create_and_login_new_admin_user, get_and_check_entities_by_user_report, \
    ADMIN_USERNAME, USERNAME_NAME
from tests.testsCJM.testReservas.testEventoSocialViewTestCase import create_test_social_event, SOCIAL_EVENT_ENTITY_NAME
from tests.testsCJM.testReservas.testReservaViewTestCase import create_test_reservation, RESERVATION_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import SKU_ENTITY_NAME, create_test_sku


class ReservaPersonaViewTestCase(FlaskClientBaseTestCase):
    PACKAGE_ID_NAME = u"id-package"
    PERSON_RESERVATION_ID_NAME = u"id-person-reservation"
    RESERVATION_ID_NAME = u"id-reservation"
    ID_NAME = u"id"
    ACTIVE_NAME = u"active"
    PERSON_ID_NAME = u"id-person"
    IS_HOLDER_NAME = u"is-holder"
    BASE_TIME_NAME = u"base-time"
    INITIAL_DATE_NAME = u"initial-date"
    FINAL_DATE_NAME = u"final-date"
    PURCHASE_TIME_NAME = u"purchase-time"
    ID_PACKAGE_NAME = u"id-package"
    ACTIVATION_DATES_NAME = u"activation-dates"
    DEACTIVATION_DATES_NAME = u"deactivation-dates"

    BASE_PRICE_NAME = u"base-price"
    TAX_RATE_NAME = u"tax-rate"

    INITIAL_TIME_NAME = u"initial-time"
    FINAL_TIME_NAME = u"final-time"
    USERNAME_NAME = u"username"
    ACTIVATIONS_NAME = u"activations"
    DEACTIVATIONS_NAME = u"deactivations"
    OPERATION_TIME_NAME = u"operation-time"

    ENTITY_DOES_NOT_EXISTS_CODE = PERSON_RESERVATION_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/reservations/{1}/persons-reservations/"

    PATCH_FIELDS = {ACTIVE_NAME}

    ATTRIBUTES_NAMES_BY_FIELDS = {PERSON_ID_NAME: "TEST_PERSON_RESERVATION_ID_PERSON",
                                  INITIAL_DATE_NAME: "TEST_PERSON_RESERVATION_INITIAL_DATE",
                                  FINAL_DATE_NAME: "TEST_PERSON_RESERVATION_FINAL_DATE",
                                  ID_PACKAGE_NAME: "TEST_PERSON_RESERVATION_ID_PACKAGE"}

    NUMBER_OF_ENTITIES = 2
    ENTITY_NAME = 'persons-reservations'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password_123"
    TEST_USER_ROLE = None

    TEST_PACKAGE_NAME = "Test package"
    TEST_PACKAGE_PRICE = 100.5
    TEST_PACKAGE_DESCRIPTION = "Test description"
    TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
    TEST_PACKAGE_VALID_FROM = "19900101010101"
    TEST_PACKAGE_VALID_THROUGH = "20100101010101"
    TEST_PACKAGE_DURATION = 5
    TEST_PACKAGE_ID_SOCIAL_EVENT = None

    TEST_SOCIAL_EVENT_NAME = "Test event"
    TEST_SOCIAL_EVENT_DESCRIPTION = "Test description"
    TEST_SOCIAL_EVENT_INITIAL_DATE = "19950101010101"
    TEST_SOCIAL_EVENT_FINAL_DATE = "20050101010101"

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_PERSON_NAME = "Test person"
    TEST_PERSON_DOCUMENT_TYPE = "CC"
    TEST_PERSON_DOCUMENT_NUMBER = "12345"
    TEST_PERSON_MAIL = "mail@test.com"
    TEST_PERSON_GENDER = "m"
    TEST_PERSON_BIRTHDATE = "19900101"
    TEST_PERSON_CATEGORY = "A"
    TEST_PERSON_AFFILIATION = "Cotizante"
    TEST_PERSON_NATIONALITY = "Colombiano"
    TEST_PERSON_PROFESSION = "Ingeniero"
    TEST_PERSON_CITY_OF_RESIDENCE = "Bogotá"
    TEST_PERSON_COMPANY = "Empresa"

    TEST_RESERVATION_COMPANY = "Test Company"
    TEST_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    AMOUNT_PER_CONSUMPTION = 1
    MONEY_PER_CONSUMPTION = 1

    NUMBER_OF_ACTIVATIONS_PER_ENTITY = 0
    NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 0

    CC_DOCUMENT_TYPE = "CC"
    NIT_DOCUMENT_TYPE = "NIT"
    TI_DOCUMENT_TYPE = "TI"
    CE_DOCUMENT_TYPE = "CE"
    PASSPORT_DOCUMENT_TYPE = "PA"
    REGISTRO_CIVIL_DOCUMENT_TYPE = "RC"
    NUIP_DOCUMENT_TYPE = "NUIP"
    CARNE_DIPLOMATICO_DOCUMENT_TYPE = "CD"
    NO_DOCUMENT_DOCUMENT_TYPE = "NO_DOCUMENT"

    VALID_DOCUMENTS = {CC_DOCUMENT_TYPE,
                       NIT_DOCUMENT_TYPE,
                       TI_DOCUMENT_TYPE,
                       CE_DOCUMENT_TYPE,
                       PASSPORT_DOCUMENT_TYPE,
                       REGISTRO_CIVIL_DOCUMENT_TYPE,
                       NUIP_DOCUMENT_TYPE,
                       CARNE_DIPLOMATICO_DOCUMENT_TYPE,
                       NO_DOCUMENT_DOCUMENT_TYPE}

    def setUp(self):
        super(ReservaPersonaViewTestCase, self).setUp()
        self.TEST_SOCIAL_EVENT_CAPACITY = self.NUMBER_OF_ENTITIES * 2

        create_test_client(self)

        create_test_location(self)

        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_social_event(self)

        self.TEST_RESERVATION_ID_SOCIAL_EVENT = self.expected_ids[SOCIAL_EVENT_ENTITY_NAME]
        create_test_package(self)

        create_test_reservation(self)

        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.create_test_persons()

    def create_test_persons(self):
        self.persons_ids = []
        for i in range(0, self.NUMBER_OF_ENTITIES):
            self.TEST_PERSON_DOCUMENT_NUMBER += str(i)
            create_test_person(self, create_new_person=True)
            self.persons_ids.append(self.expected_ids[PERSON_ENTITY_NAME])

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.INITIAL_DATE_NAME] = cls.TEST_SOCIAL_EVENT_INITIAL_DATE
        values[cls.FINAL_DATE_NAME] = cls.TEST_SOCIAL_EVENT_FINAL_DATE
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.ACTIVE_NAME] = True
        values[cls.INITIAL_DATE_NAME] = cls.TEST_SOCIAL_EVENT_INITIAL_DATE
        values[cls.FINAL_DATE_NAME] = cls.TEST_SOCIAL_EVENT_FINAL_DATE
        return values

    def get_entity_values_for_create(self, entity_number):
        values = super(ReservaPersonaViewTestCase, self).get_entity_values_for_create(entity_number)
        if self.PERSON_ID_NAME not in values:
            values[self.PERSON_ID_NAME] = self.persons_ids[entity_number % len(self.persons_ids)]
        return values

    def get_entity_values_for_update(self, entity_number):
        values = super(ReservaPersonaViewTestCase, self).get_entity_values_for_update(entity_number)
        if self.PERSON_ID_NAME not in values:
            values[self.PERSON_ID_NAME] = self.persons_ids[entity_number % len(self.persons_ids)]
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME, RESERVATION_ENTITY_NAME]

    @classmethod
    def validate_additional_values(cls, running_entity, result):
        running_entity.assertTrue(cls.BASE_PRICE_NAME in result)
        running_entity.assertTrue(cls.TAX_RATE_NAME in result)
        running_entity.assertTrue(cls.PURCHASE_TIME_NAME in result)
        running_entity.assertTrue(isinstance(result[cls.ACTIVATION_DATES_NAME], list))
        if hasattr(running_entity, "NUMBER_OF_ACTIVATIONS_PER_ENTITY"):
            running_entity.assertEquals(running_entity.NUMBER_OF_ACTIVATIONS_PER_ENTITY, len(result[cls.ACTIVATION_DATES_NAME]))
        running_entity.assertTrue(isinstance(result[cls.DEACTIVATION_DATES_NAME], list))
        if hasattr(running_entity, "NUMBER_OF_DEACTIVATIONS_PER_ENTITY"):
            running_entity.assertEquals(running_entity.NUMBER_OF_DEACTIVATIONS_PER_ENTITY, len(result[cls.DEACTIVATION_DATES_NAME]))

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        initial_date = request_values.get(cls.INITIAL_DATE_NAME)
        if initial_date is not None:
            request_values[cls.INITIAL_DATE_NAME] = validate_datetime(initial_date, ReservaPersonaViewTestCase.INITIAL_DATE_NAME).strftime(DEFAULT_DATETIME_FORMAT)

        final_date = request_values.get(cls.FINAL_DATE_NAME)
        if final_date is not None:
            request_values[cls.FINAL_DATE_NAME] = validate_datetime(final_date, ReservaPersonaViewTestCase.INITIAL_DATE_NAME).strftime(DEFAULT_DATETIME_FORMAT)

        if is_create:
            request_values[cls.IS_HOLDER_NAME] = entity_number == 0
            request_values[cls.ACTIVE_NAME] = False
        if cls.PERSON_ID_NAME in request_values and request_values[cls.PERSON_ID_NAME] is None:
            del request_values[cls.PERSON_ID_NAME]
        return request_values

    def setup_scenario_with_valid_inactive_person_reservations_with_access_consumption(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

        self.TEST_ACCESS_TOPOFF_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_TIME = self.TEST_SOCIAL_EVENT_INITIAL_DATE
        create_test_location(self, create_new_location=True)
        self.TEST_ACCESS_TOPOFF_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_ACCESS_ID_LOCATION = self.TEST_ACCESS_TOPOFF_ID_LOCATION

        original_person_reservation_id = self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]
        for person_reservation in self.original_entities:
            from tests.testsCJM.testReservas.testAccessTopoffsViewTestCase import create_test_access_topoff
            from tests.testsCJM.testOrders.testPersonAccessesForPackageWithAccessesViewTestCase import \
                create_test_person_access
            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = person_reservation[self.ID_NAME]
            create_test_access_topoff(self, create_new_topoff=True)
            create_test_person_access(self, create_new_access=True)

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = original_person_reservation_id

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def setup_scenario_with_valid_inactive_person_reservations_with_amount_consumption(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

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
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_SOCIAL_EVENT_INITIAL_DATE

        original_person_reservation_id = self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]
        for person_reservation in self.original_entities:
            from tests.testsCJM.testReservas.testAmountTopoffsViewTestCase import create_test_amount_topoff
            from tests.testsCJM.testOrders.testPersonOrdersForPackageWithAmountConsumptionViewTestCase import \
                create_test_person_consumption_for_amount
            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = person_reservation[self.ID_NAME]
            create_test_amount_topoff(self, create_new_topoff=True)
            create_test_person_consumption_for_amount(self, create_new_consumption=True)

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = original_person_reservation_id

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def setup_scenario_with_valid_inactive_person_reservations_with_money_consumption(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

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
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_SOCIAL_EVENT_INITIAL_DATE

        original_person_reservation_id = self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]
        for person_reservation in self.original_entities:
            from tests.testsCJM.testReservas.testMoneyTopoffsViewTestCase import create_test_money_topoff
            from tests.testsCJM.testOrders.testPersonOrdersForPackageWithMoneyConsumptionViewTestCase import \
                create_test_person_consumption_for_money
            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = person_reservation[self.ID_NAME]
            create_test_money_topoff(self, create_new_topoff=True)
            create_test_person_consumption_for_money(self, create_new_consumption=True)

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = original_person_reservation_id

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def setup_scenario_with_valid_inactive_person_reservations_with_access_topoffs(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

        self.TEST_ACCESS_TOPOFF_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_AMOUNT = self.AMOUNT_PER_CONSUMPTION
        self.TEST_PERSON_ACCESS_TIME = self.TEST_SOCIAL_EVENT_INITIAL_DATE
        create_test_location(self, create_new_location=True)
        self.TEST_ACCESS_TOPOFF_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_PERSON_ACCESS_ID_LOCATION = self.TEST_ACCESS_TOPOFF_ID_LOCATION

        original_person_reservation_id = self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]
        for person_reservation in self.original_entities:
            from tests.testsCJM.testReservas.testAccessTopoffsViewTestCase import create_test_access_topoff
            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = person_reservation[self.ID_NAME]
            create_test_access_topoff(self, create_new_topoff=True)

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = original_person_reservation_id

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def setup_scenario_with_valid_inactive_person_reservations_with_amount_topoffs(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

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
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_SOCIAL_EVENT_INITIAL_DATE

        original_person_reservation_id = self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]
        for person_reservation in self.original_entities:
            from tests.testsCJM.testReservas.testAmountTopoffsViewTestCase import create_test_amount_topoff
            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = person_reservation[self.ID_NAME]
            create_test_amount_topoff(self, create_new_topoff=True)

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = original_person_reservation_id

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def setup_scenario_with_valid_inactive_person_reservations_with_money_topoffs(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

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
        self.TEST_PERSON_CONSUMPTION_TIME = self.TEST_SOCIAL_EVENT_INITIAL_DATE

        original_person_reservation_id = self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]
        for person_reservation in self.original_entities:
            from tests.testsCJM.testReservas.testMoneyTopoffsViewTestCase import create_test_money_topoff
            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = person_reservation[self.ID_NAME]
            create_test_money_topoff(self, create_new_topoff=True)

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = original_person_reservation_id

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def _check_person_reservations_with_user_and_children_data(self, results, username):
        person_reservations = results[0][self.ENTITY_NAME]
        self.check_list_response(self.ENTITY_NAME, person_reservations, len(person_reservations))
        for person_reservation in person_reservations:
            self.assertEqual(username, person_reservation.get(USERNAME_NAME, None))

    def _check_person_reservations_activations_with_user_and_children_data(self, results, username):
        person_reservations = results[0][self.ENTITY_NAME]
        if len(person_reservations) > 0:
            activations = person_reservations[0][self.ACTIVATIONS_NAME]
            self.assertEqual(len(activations), self.NUMBER_OF_ACTIVATIONS_PER_ENTITY)
            for activation in activations:
                self.assertIn(self.OPERATION_TIME_NAME, activation)
                self.assertIn(self.RESERVATION_ID_NAME, activation)
                self.assertIn(self.PERSON_RESERVATION_ID_NAME, activation)
                self.assertEqual(username, activation.get(USERNAME_NAME, None))
            deactivations = person_reservations[0][self.DEACTIVATIONS_NAME]
            self.assertEqual(len(deactivations), self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY)
            for deactivation in deactivations:
                self.assertIn(self.OPERATION_TIME_NAME, deactivation)
                self.assertIn(self.RESERVATION_ID_NAME, deactivation)
                self.assertIn(self.PERSON_RESERVATION_ID_NAME, deactivation)
                self.assertEqual(username, deactivation.get(USERNAME_NAME, None))
        else:
            self.assertEqual(0, self.NUMBER_OF_ACTIVATIONS_PER_ENTITY)
            self.assertEqual(0, self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY)

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_persons_reservations_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_persons_reservations(self):
        self.do_create_requests()

    def test_create_valid_persons_reservations_for_the_same_person_on_non_overlapping_dates(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000101235959")
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000102010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000102235959")
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)

    def test_try_create_persons_reservations_for_the_same_persons_with_reservations_the_same_days(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000101235959")
        self.do_create_requests()
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_CODE)

    def test_try_create_persons_reservations_for_the_same_persons_with_reservations_the_overlapping_days(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000102235959")
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000102010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000103235959")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_CODE)

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_empty_person_reservations(self):
        create_and_login_new_admin_user(self)
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_person_reservations_and_query_them_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_new_user(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_person_reservations_and_query_empty_activations_them_with_reservations_endpoint_true_include_children(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 0
        self._check_person_reservations_activations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_persons_reservations_and_consult_by_document(self):
        self.NUMBER_OF_ENTITIES = 1
        self.create_test_persons()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_consult_by_document_doesnt_returns_data_if_client_doesnt_has_compensars_reservation_services_person_was_created_without_document_and_another_document_is_given(self):
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.NO_DOCUMENT_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        for document_type in self.VALID_DOCUMENTS:
            if document_type != self.NO_DOCUMENT_DOCUMENT_TYPE:
                self.TEST_PERSON_DOCUMENT_TYPE = document_type
                create_test_person(self, create_new_person=True)
            results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  document_type,
                                                  self.TEST_PERSON_DOCUMENT_NUMBER))
            if document_type != self.NO_DOCUMENT_DOCUMENT_TYPE:
                self.assertEqual(0, len(results))
            else:
                self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_consult_by_document_returns_data_if_client_has_compensars_reservation_services_person_was_created_without_document_and_another_document_is_given(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.NO_DOCUMENT_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        for document_type in self.VALID_DOCUMENTS:
            self.external_service_called = False
            queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
            results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  document_type,
                                                  self.TEST_PERSON_DOCUMENT_NUMBER))
            self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)
            self.assertFalse(self.external_service_called)

    def test_consult_by_document_returns_data_of_person_with_document_if_client_has_compensars_reservation_services_and_person_was_created_with_and_without_document(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.NO_DOCUMENT_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        self.clean_test_data()
        create_test_reservation(self, create_new_reservation=True)
        self.TEST_PERSON_DOCUMENT_TYPE = self.CC_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        self.external_service_called = False
        queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)
        self.assertFalse(self.external_service_called)

    def test_consult_by_document_raises_person_does_not_exist_if_client_has_compensars_reservation_and_person_does_not_exist_with_the_given_document_or_no_document(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.TI_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        for document_type in self.VALID_DOCUMENTS:
            queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
            if document_type != self.TI_DOCUMENT_TYPE:
                self.external_service_called = False
                results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      document_type,
                                                      self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=404)
                validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)
                self.assertFalse(self.external_service_called)

    def test_consult_by_document_calls_external_service_if_client_has_compensars_reservation_and_there_is_no_reservation_for_persons_with_given_document(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        for document_type in self.VALID_DOCUMENTS:
            self.TEST_PERSON_DOCUMENT_TYPE = document_type
            self.create_test_persons()
            queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
            self.external_service_called = False
            results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  document_type,
                                                  self.TEST_PERSON_DOCUMENT_NUMBER))
            self.assertEqual(0, len(results))

    def test_create_valid_persons_reservations_and_consult_person_reservations_by_document_with_included_base_time(self):
        self.NUMBER_OF_ENTITIES = 1
        self.create_test_persons()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "20000101010101"))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_persons_reservations_by_document_with_base_time_equals_to_reservation_final_date(self):
        self.NUMBER_OF_ENTITIES = 1
        self.create_test_persons()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              self.TEST_SOCIAL_EVENT_FINAL_DATE))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_persons_reservations_by_document_with_base_time_equals_to_reservation_initial_date(self):
        self.NUMBER_OF_ENTITIES = 1
        self.create_test_persons()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              self.TEST_SOCIAL_EVENT_INITIAL_DATE))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_persons_reservations_by_document_with_base_time_smaller_than_reservation_initial_date(self):
        self.NUMBER_OF_ENTITIES = 1
        self.create_test_persons()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "19800101010101"))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_persons_reservations_and_consult_persons_reservations_by_document_with_base_time_bigger_than_reservation_final_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "30000101010101"))
        self.clean_test_data()
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_consult_persons_reservations_by_document_with_invalid_base_time(self):
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "INVALID_DATE"),
                                      expected_code=400)
        self.validate_error(results, PERSON_RESERVATION_INVALID_BASE_TIME_CODE)

    def test_create_valid_persons_reservations_and_consult_empty_view_by_document_with_different_document_type(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "TI",
                                              self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=404)
        validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)

    def test_create_valid_persons_reservations_and_consult_empty_view_by_document_with_different_document_number(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER+"6"), expected_code=404)
        validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)

    def test_try_query_persons_reservations_by_document_without_document_type(self):
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=400)
        validate_error(self, results, PERSON_RESERVATION_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_query_persons_reservations_by_document_without_document_number(self):
        results = self.do_get_request("/clients/{0}/persons-reservations-by-document/?document-type={1}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=400)
        validate_error(self, results, PERSON_RESERVATION_INVALID_DOCUMENT_NUMBER_CODE)

    def test_create_valid_persons_reservations_and_consult_reservations_by_document(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0][self.ID_NAME], self.expected_ids[RESERVATION_ENTITY_NAME])

    def test_consult_reservations_by_document_doesnt_returns_data_if_client_doesnt_has_compensars_reservation_services_person_was_created_without_document_and_another_document_is_given(self):
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.NO_DOCUMENT_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        for document_type in self.VALID_DOCUMENTS:
            if document_type != self.NO_DOCUMENT_DOCUMENT_TYPE:
                self.TEST_PERSON_DOCUMENT_TYPE = document_type
                create_test_person(self, create_new_person=True)
            results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  document_type,
                                                  self.TEST_PERSON_DOCUMENT_NUMBER))
            if document_type != self.NO_DOCUMENT_DOCUMENT_TYPE:
                self.assertEqual(0, len(results))
            else:
                self.assertEqual(1, len(results))
                self.assertEqual(self.expected_ids[RESERVATION_ENTITY_NAME], results[0][self.ID_NAME])

    def test_consult_reservations_by_document_returns_data_if_client_has_compensars_reservation_services_person_was_created_without_document_and_another_document_is_given(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.NO_DOCUMENT_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        for document_type in self.VALID_DOCUMENTS:
            self.external_service_called = False
            queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
            results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  document_type,
                                                  self.TEST_PERSON_DOCUMENT_NUMBER))
            self.assertEqual(1, len(results))
            self.assertFalse(self.external_service_called)
            self.assertEqual(self.expected_ids[RESERVATION_ENTITY_NAME], results[0][self.ID_NAME])

    def test_consult_reservations_by_document_returns_data_of_person_with_document_if_client_has_compensars_reservation_services_and_person_was_created_with_and_without_document(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.NO_DOCUMENT_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        self.clean_test_data()
        create_test_reservation(self, create_new_reservation=True)
        self.TEST_PERSON_DOCUMENT_TYPE = self.CC_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        self.external_service_called = False
        queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
        self.assertFalse(self.external_service_called)
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER))
        self.assertEqual(1, len(results))
        self.assertEqual(self.expected_ids[RESERVATION_ENTITY_NAME], results[0][self.ID_NAME])

    def test_consult_reservations_by_document_raises_person_does_not_exist_if_client_has_compensars_reservation_and_person_does_not_exist_with_the_given_document_or_no_document(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        self.TEST_PERSON_DOCUMENT_TYPE = self.TI_DOCUMENT_TYPE
        self.create_test_persons()
        self.do_create_requests()
        for document_type in self.VALID_DOCUMENTS:
            queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
            if document_type != self.TI_DOCUMENT_TYPE:
                self.external_service_called = False
                results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      document_type,
                                                      self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=404)
                validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)
                self.assertFalse(self.external_service_called)

    def test_consult_reservations_by_document_calls_external_service_if_client_has_compensars_reservation_and_there_is_no_reservation_for_persons_with_given_document(self):
        from CJM.services.reservas.compensar_reservations_service import queryReservationsFromCompensarService
        self.TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE = u"COMPENSAR"
        create_test_client(self, create_new_client=True)
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self, create_new_package=True)
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME])
        self.NUMBER_OF_ENTITIES = 1
        for document_type in self.VALID_DOCUMENTS:
            self.TEST_PERSON_DOCUMENT_TYPE = document_type
            self.create_test_persons()
            queryReservationsFromCompensarService.query_compensars_external_reservations_service = _get_replacemente_function(self, self.TEST_PERSON_DOCUMENT_NUMBER)
            self.external_service_called = False
            results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  document_type,
                                                  self.TEST_PERSON_DOCUMENT_NUMBER))
            self.assertEqual(0, len(results))

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_included_base_time(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "20000101010101"))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0][self.ID_NAME], self.expected_ids[RESERVATION_ENTITY_NAME])

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_base_time_equals_to_reservation_final_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              self.TEST_SOCIAL_EVENT_FINAL_DATE))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0][self.ID_NAME], self.expected_ids[RESERVATION_ENTITY_NAME])

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_base_time_equals_to_reservation_initial_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              self.TEST_SOCIAL_EVENT_INITIAL_DATE))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0][self.ID_NAME], self.expected_ids[RESERVATION_ENTITY_NAME])

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_base_time_smaller_than_reservation_initial_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "19800101010101"))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0][self.ID_NAME], self.expected_ids[RESERVATION_ENTITY_NAME])

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_base_time_bigger_than_reservation_final_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "30000101010101"))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 0)

    def test_try_consult_reservations_by_document_with_invalid_base_time(self):
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}&{3}={4}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER,
                                              self.BASE_TIME_NAME,
                                              "INVALID_DATE"),
                                      expected_code=400)
        self.validate_error(results, RESERVATION_INVALID_BASE_TIME_CODE)

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_different_document_type(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "TI",
                                              self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=404)
        validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)

    def test_create_valid_persons_reservations_and_consult_reservations_by_document_with_different_document_number(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}&document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER+"6"), expected_code=404)
        validate_error(self, results, PERSON_DOES_NOT_EXISTS_CODE)

    def test_try_query_reservations_by_document_without_document_type(self):
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-number={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=400)
        validate_error(self, results, RESERVATION_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_query_reservations_by_document_without_document_number(self):
        results = self.do_get_request("/clients/{0}/reservations-by-document/?document-type={1}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_PERSON_DOCUMENT_TYPE,
                                              self.TEST_PERSON_DOCUMENT_NUMBER), expected_code=400)
        validate_error(self, results, RESERVATION_INVALID_DOCUMENT_NUMBER_CODE)

    def test_activate_valid_persons_reservations(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

    def test_query_activations_with_reservations_endpoint_after_patch_and_true_include_children(self):
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        create_and_login_new_admin_user(self)

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_activations_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_query_deactivations_with_reservations_endpoint_after_patch_and_true_include_children(self):
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_activations_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_query_person_reservations_with_reservations_endpoint_after_patch_with_true_include_children_and_check_they_return_the_user_which_created_them(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_deactivate_valid_persons_reservations(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1

        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1

        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def test_deactivate_valid_persons_reservations_with_unpaid_reservation(self):
        self.TEST_RESERVATION_PAYMENT = None
        self.TEST_RESERVATION_TRANSACTION_NUMBER = None
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 0
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_update_requests()

    def test_try_activate_valid_persons_reservations_with_empty_active(self):
        self.do_create_requests()

        self.assign_field_value(self.ACTIVE_NAME, "")
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=PERSON_RESERVATION_INVALID_ACTIVE_CODE)

    def test_try_activate_valid_persons_reservations_without_active(self):
        self.do_create_requests()

        self.assign_field_value(self.ACTIVE_NAME, None)
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=PERSON_RESERVATION_INVALID_ACTIVE_CODE)

    def test_try_activate_valid_persons_reservations_with_unpaid_reservation(self):
        self.TEST_RESERVATION_PAYMENT = None
        self.TEST_RESERVATION_TRANSACTION_NUMBER = None
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()

        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=PERSON_RESERVATION_TRYING_TO_ACTIVATE_UNPAID_RESERVATION_CODE)

    def test_activate_invalid_non_existent_persons_reservations(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()

        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_to_activate_already_active_person_reservations(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=PERSON_RESERVATION_TRYING_TO_ACTIVATE_ALREADY_ACTIVE_PERSON_RESERVATION_CODE)

    def test_deactivate_invalid_non_existent_persons_reservations(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()

        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_create_valid_persons_reservations_without_person(self):
        self.assign_field_value(self.PERSON_ID_NAME, None)
        self.do_create_requests()

    def test_create_invalid_persons_reservations_with_non_existent_person(self):
        self.assign_field_value(self.PERSON_ID_NAME, self.expected_ids[PERSON_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_persons_reservations_with_non_existent_reservation(self):
        self.expected_ids[RESERVATION_ENTITY_NAME] += 1
        self.do_create_requests(expected_code=404,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_create_invalid_persons_reservations_with_non_existent_package(self):
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_persons_reservations_without_package(self):
        self.assign_field_value(self.PACKAGE_ID_NAME, None)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_person_reservations_without_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE)

    def test_create_invalid_person_reservations_with_empty_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE)

    def test_create_invalid_person_reservations_with_invalid_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "INVALID DATE")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE)

    def test_create_invalid_person_reservation_with_valid_initial_date_smaller_than_package_valid_from(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, u"19800101010101")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FROM_CODE)

    def test_create_invalid_person_reservations_without_final_date(self):
        self.assign_field_value(self.FINAL_DATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_CODE)

    def test_create_invalid_person_reservations_with_empty_final_date(self):
        self.assign_field_value(self.FINAL_DATE_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_CODE)

    def test_create_invalid_person_reservations_with_invalid_final_date(self):
        self.assign_field_value(self.FINAL_DATE_NAME, "INVALID DATE")
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_CODE)

    def test_create_invalid_person_reservation_with_valid_final_date_bigger_than_package_valid_through(self):
        self.assign_field_value(self.FINAL_DATE_NAME, u"20200101010101")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_RANGE_OF_DATES_THROUGH_GREATER_THAN_FINAL_CODE)

    def test_create_invalid_person_reservations_with_final_date_smaller_than_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, self.TEST_PACKAGE_VALID_THROUGH)
        self.assign_field_value(self.FINAL_DATE_NAME, self.TEST_PACKAGE_VALID_FROM)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FINAL_CODE)

    def test_create_invalid_person_reservations_with_social_event_with_initial_date_smaller_than_event_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, self.TEST_PACKAGE_VALID_FROM)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_INVALID_RANGE_OF_DATES_INITIAL_SMALLER_THAN_EVENT_INITIAL_CODE)

    def test_create_invalid_person_reservations_with_social_event_with_final_date_bigger_than_event_final_date(self):
        self.assign_field_value(self.FINAL_DATE_NAME, self.TEST_PACKAGE_VALID_THROUGH)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_INVALID_RANGE_OF_DATES_FINAL_BIGGER_THAN_EVENT_FINAL_CODE)

    def test_delete_valid_person_reservations(self):
        self.do_create_requests()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(filter_function=is_not_holder_person_reservation)

    def test_query_person_reservations_with_reservations_endpoint_after_delete_with_true_include_children_and_check_only_holder_is_returned(self):
        self.do_create_requests()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(filter_function=is_not_holder_person_reservation)
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_try_delete_valid_person_reservations_with_is_holder(self):
        self.do_create_requests()

        def is_holder_person_reservation(person_reservation):
            return person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_holder_person_reservation,
                                expected_internal_code=DELETE_HOLDER_PERSON_RESERVATION_CODE)

    def test_try_delete_valid_active_person_reservations(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_ACTIVE_PERSON_RESERVATION_CODE)

    def test_try_delete_valid_inactive_active_person_reservations_with_access_consumptions(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_access_consumption()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_CODE)

    def test_try_delete_valid_inactive_active_person_reservations_with_amount_consumptions(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_amount_consumption()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_delete_valid_inactive_active_person_reservations_with_money_consumptions(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_money_consumption()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_delete_valid_inactive_active_person_reservations_with_access_topoffs(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_access_topoffs()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_PERSON_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_delete_valid_inactive_active_person_reservations_with_amount_topoffs(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_amount_topoffs()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_PERSON_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_delete_valid_inactive_active_person_reservations_with_money_topoffs(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_money_topoffs()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(expected_code=400, filter_function=is_not_holder_person_reservation,
                                expected_internal_code=DELETE_PERSON_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_delete_invalid_person_reservations_with_wrong_id_client(self):
        self.do_create_requests()
        create_test_client(self, create_new_client=True)
        self.do_delete_requests(expected_code=404, do_get_and_check_results=False,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_invalid_person_reservations_with_wrong_id_reservation(self):
        self.do_create_requests()
        self.expected_ids[RESERVATION_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_delete_invalid_non_existent_person_reservations(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404,
                                expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_update_valid_person_reservations(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests()

    def test_query_activations_with_reservations_endpoint_after_update_and_true_include_children(self):
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests()
        create_and_login_new_admin_user(self)

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_activations_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_query_person_reservations_with_reservations_endpoint_after_update_with_true_include_children_and_check_they_return_the_user_which_created_them(self):
        self.do_create_requests()

        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_reservations_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_update_valid_person_reservations_without_id_person(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.PERSON_ID_NAME, None)
        self.do_update_requests()

    def test_update_valid_person_reservations_with_true_active_status(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_update_requests()

    def test_update_valid_person_reservation_with_valid_dates_changing_reservation_duration(self):
        self.TEST_RESERVATION_ID_SOCIAL_EVENT = None
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.INITIAL_DATE_NAME, self.TEST_PACKAGE_VALID_FROM)
        self.assign_field_value(self.FINAL_DATE_NAME, self.TEST_PACKAGE_VALID_THROUGH)
        self.do_update_requests()

    def test_update_valid_persons_reservations_for_the_same_person_on_non_overlapping_dates(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000101235959")
        self.do_create_requests()
        self.clean_test_data()
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000102010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000102235959")
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False)
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000103010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000103235959")
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests(do_get_and_check_results=False)

    def test_try_update_valid_person_reservations_with_false_active_status_after_activating_it(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_update_requests(expected_code=400, expected_internal_code=UPDATE_ACTIVE_PERSON_RESERVATION_CODE)

    def test_try_update_valid_persons_reservations_for_the_same_person_with_the_same_days(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000101235959")
        self.do_create_requests()
        self.clean_test_data()
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000102010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000102235959")
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False)
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000101235959")
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_CODE,
                                do_get_and_check_results=False)

    def test_try_update_valid_persons_reservations_for_the_same_person_with_overlapping_days(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000102235959")
        self.do_create_requests()
        self.clean_test_data()
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000103010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000103235959")
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False)
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000102010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000103235959")
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_CODE,
                                do_get_and_check_results=False)

    def test_try_update_valid_persons_reservations_to_the_same_person_with_overlapping_days(self):
        self.NUMBER_OF_ENTITIES *= 2
        self.create_test_persons()
        self.NUMBER_OF_ENTITIES /= 2
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "20000102235959")
        self.do_create_requests()
        self.clean_test_data()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_ALREADY_EXISTS_ON_GIVEN_DATE_CODE,
                                do_get_and_check_results=False)

    def test_try_update_valid_person_reservations_without_active(self):
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_INVALID_ACTIVE_CODE)

    def test_try_update_valid_person_reservations_with_invalid_active(self):
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, "INVALID ACTIVE")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_INVALID_ACTIVE_CODE)

    def test_try_update_invalid_persons_reservations_with_non_existent_person(self):
        self.do_create_requests()
        self.assign_field_value(self.PERSON_ID_NAME, self.expected_ids[PERSON_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_person_reservations_with_wrong_id_client(self):
        self.do_create_requests()
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_update_requests(expected_code=404, do_get_and_check_results=False,
                                expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_person_reservations_with_wrong_id_package(self):
        self.do_create_requests()
        self.assign_field_value(self.PACKAGE_ID_NAME, self.expected_ids[PACKAGE_ENTITY_NAME] + 1)
        self.do_update_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_person_reservations_without_id_package(self):
        self.do_create_requests()
        self.assign_field_value(self.PACKAGE_ID_NAME, None)
        self.do_update_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_person_reservations_with_wrong_id_reservation(self):
        self.do_create_requests()
        self.expected_ids[RESERVATION_ENTITY_NAME] += 1
        self.do_update_requests(expected_code=404, do_get_and_check_results=False,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_non_existent_person_reservations(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_update_requests(expected_code=404,
                                expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_update_valid_active_person_reservations(self):
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_ACTIVE_PERSON_RESERVATION_CODE)

    def test_try_update_valid_inactive_active_person_reservations_with_access_consumptions(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_access_consumption()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_PERSON_RESERVATION_WITH_CONSUMED_ACCESSES_CODE)

    def test_try_update_valid_inactive_active_person_reservations_with_amount_consumptions(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_amount_consumption()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_update_valid_inactive_active_person_reservations_with_money_consumptions(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_money_consumption()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_PERSON_RESERVATION_WITH_CONSUMED_CONSUMPTIONS_CODE)

    def test_try_update_valid_inactive_active_person_reservations_with_access_topoffs(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_access_topoffs()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_valid_inactive_active_person_reservations_with_amount_topoffs(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_amount_topoffs()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_valid_inactive_active_person_reservations_with_money_topoffs(self):
        self.setup_scenario_with_valid_inactive_person_reservations_with_money_topoffs()
        self.do_update_requests(expected_code=400,
                                expected_internal_code=UPDATE_PERSON_RESERVATION_WITH_TOPOFFS_CODE)

    def test_try_update_invalid_person_reservations_without_initial_date(self):
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE)

    def test_try_update_invalid_person_reservations_with_empty_initial_date(self):
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE)

    def test_try_update_invalid_person_reservations_with_invalid_initial_date(self):
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, "INVALID DATE")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_INITIAL_DATE_CODE)

    def test_try_update_invalid_person_reservation_with_valid_initial_date_smaller_than_package_valid_from(self):
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, u"19800101010101")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FROM_CODE)

    def test_try_update_invalid_person_reservations_without_final_date(self):
        self.do_create_requests()
        self.assign_field_value(self.FINAL_DATE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_CODE)

    def test_try_update_invalid_person_reservations_with_empty_final_date(self):
        self.do_create_requests()
        self.assign_field_value(self.FINAL_DATE_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_CODE)

    def test_try_update_invalid_person_reservations_with_invalid_final_date(self):
        self.do_create_requests()
        self.assign_field_value(self.FINAL_DATE_NAME, "INVALID DATE")
        self.do_update_requests(expected_code=400, expected_internal_code=PERSON_RESERVATION_INVALID_FINAL_DATE_CODE)

    def test_try_update_invalid_reservation_with_valid_final_date_bigger_than_package_valid_through(self):
        self.do_create_requests()
        self.assign_field_value(self.FINAL_DATE_NAME, u"20200101010101")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_RANGE_OF_DATES_THROUGH_GREATER_THAN_FINAL_CODE)

    def test_try_update_invalid_person_reservations_with_final_date_smaller_than_initial_date(self):
        self.do_create_requests()
        self.assign_field_value(self.INITIAL_DATE_NAME, self.TEST_PACKAGE_VALID_THROUGH)
        self.assign_field_value(self.FINAL_DATE_NAME, self.TEST_PACKAGE_VALID_FROM)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_RANGE_OF_DATES_INITIAL_GREATER_THAN_FINAL_CODE)

    def test_try_update_invalid_person_reservations_with_initial_date_smaller_than_event_initial_date(self):
        self.do_create_requests()

        self.assign_field_value(self.INITIAL_DATE_NAME, self.TEST_PACKAGE_VALID_FROM)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_INVALID_RANGE_OF_DATES_INITIAL_SMALLER_THAN_EVENT_INITIAL_CODE)

    def test_try_update_invalid_person_reservations_with_final_date_bigger_than_event_final_date(self):
        self.do_create_requests()

        self.assign_field_value(self.FINAL_DATE_NAME, self.TEST_PACKAGE_VALID_THROUGH)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PERSON_RESERVATION_INVALID_RANGE_OF_DATES_FINAL_BIGGER_THAN_EVENT_FINAL_CODE)

    def test_check_empty_entities_by_user_report_with_persons_reservations_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_persons_reservations(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_persons_reservations_with_the_same_user(self):
        self.NUMBER_OF_ENTITIES *= 2
        self.create_test_persons()
        self.NUMBER_OF_ENTITIES /= 2
        create_and_login_new_admin_user(self)
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()

        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_persons_reservations_with_different_user(self):
        self.NUMBER_OF_ENTITIES *= 2
        self.create_test_persons()
        self.NUMBER_OF_ENTITIES /= 2
        create_and_login_new_admin_user(self)
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self.clean_test_data()

        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES, do_get_and_check_results=False)
        expected_entities_by_user[self.TEST_USER_USERNAME] = self.original_entities
        users.add(self.TEST_USER_USERNAME)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_persons_reservations_after_update(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_persons_reservations_after_patch(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_persons_reservations_after_delete(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(filter_function=is_not_holder_person_reservation)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_persons_reservations_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(filter_function=is_not_holder_person_reservation)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_persons_reservations_after_delete_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.do_create_requests()

        expected_entities_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        users = {self.TEST_USER_USERNAME}

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.do_delete_requests(filter_function=is_not_holder_person_reservation)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=True,
                                              delete_filter_function=is_not_holder_person_reservation)

    def test_check_empty_person_reservations_activations_by_user_report(self):
        create_and_login_new_admin_user(self)
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_empty_person_reservations_activations_by_user_report_with_person_reservations_never_activated(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_with_put(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_update_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_multiple_times(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 2
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_with_different_users_times(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        original_user = self.TEST_USER_USERNAME
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        self.TEST_USER_USERNAME += "1"
        users = {original_user, self.TEST_USER_USERNAME}
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 2
        create_and_login_new_admin_user(self)
        self.do_patch_requests()
        for index, entity in enumerate(self.original_entities):
            for activation_date in expected_person_reservation_by_user[original_user][index][self.ACTIVATION_DATES_NAME]:
                entity[self.ACTIVATION_DATES_NAME].remove(activation_date)
            for deactivation_date in expected_person_reservation_by_user[original_user][index][self.DEACTIVATION_DATES_NAME]:
                entity[self.DEACTIVATION_DATES_NAME].remove(deactivation_date)
        expected_person_reservation_by_user[self.TEST_USER_USERNAME] = list(self.original_entities)
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_deactivations_by_user_report_after_activating_person_reservations_multiple_times(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 2
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 2
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_deactivations_by_user_report_after_deactivating_person_reservations_with_different_users_times(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 2
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        original_user = self.TEST_USER_USERNAME
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        self.TEST_USER_USERNAME += "1"
        users = {original_user, self.TEST_USER_USERNAME}
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 2
        self.assign_field_value(self.ACTIVE_NAME, False)
        create_and_login_new_admin_user(self)
        self.do_patch_requests()
        for index, entity in enumerate(self.original_entities):
            for activation_date in expected_person_reservation_by_user[original_user][index][self.ACTIVATION_DATES_NAME]:
                entity[self.ACTIVATION_DATES_NAME].remove(activation_date)
            for deactivation_date in expected_person_reservation_by_user[original_user][index][self.DEACTIVATION_DATES_NAME]:
                entity[self.DEACTIVATION_DATES_NAME].remove(deactivation_date)
        expected_person_reservation_by_user[self.TEST_USER_USERNAME] = list(self.original_entities)
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users)

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_with_initial_time_smaller_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="19900101010101")

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_with_initial_time_bigger_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="29900101010101")

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations_with_initial_time_smaller_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="19900101010101")

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations_with_initial_time_bigger_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="29900101010101")

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_with_final_time_bigger_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, final_date="29900101010101")

    def test_check_person_reservations_activations_by_user_report_after_activating_person_reservations_with_final_time_smaller_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, final_date="19900101010101")

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations_with_final_time_bigger_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, final_date="29900101010101")

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations_with_final_time_smaller_than_today(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, final_date="19900101010101")

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations_with_initial_and_final_dates_included(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="19900101010101",
                                                                           final_date="29900101010101")

    def test_check_person_reservations_activations_by_user_report_after_deactivating_person_reservations_with_with_initial_and_final_dates_included(self):
        self.do_create_requests()
        create_and_login_new_admin_user(self)
        self.NUMBER_OF_ACTIVATIONS_PER_ENTITY = 1
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.NUMBER_OF_DEACTIVATIONS_PER_ENTITY = 1
        self.do_patch_requests()
        expected_person_reservation_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="19900101010101",
                                                                           final_date="29900101010101")

    def test_check_person_reservations_activations_by_user_report_with_invalid_initial_time(self):
        create_and_login_new_admin_user(self)
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, initial_date="INVALID_TIME",
                                                                           expected_code=400,
                                                                           expected_internal_code=PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_INITIAL_TIME_CODE)

    def test_check_person_reservations_activations_by_user_report_with_invalid_final_time(self):
        create_and_login_new_admin_user(self)
        expected_person_reservation_by_user = dict()
        users = set()
        self._get_and_check_person_reservations_activations_by_user_report(expected_person_reservation_by_user,
                                                                           users, final_date="INVALID_TIME",
                                                                           expected_code=400,
                                                                           expected_internal_code=PERSON_RESERVATIONS_ACTIVATIONS_BY_USER_INVALID_FINAL_TIME_CODE)

    def _get_and_check_person_reservations_activations_by_user_report(self, person_reservations_by_user, users,
                                                                      initial_date=None, final_date=None,
                                                                      expected_code=200, expected_internal_code=None):
        filter_str = u""
        if initial_date is not None:
            if len(filter_str) == 0:
                filter_format = u"?{0}={1}"
            else:
                filter_format = u"&{0}={1}"
            filter_str += filter_format.format(self.INITIAL_TIME_NAME, initial_date)
        if final_date is not None:
            if len(filter_str) == 0:
                filter_format = u"?{0}={1}"
            else:
                filter_format = u"&{0}={1}"
            filter_str += filter_format.format(self.FINAL_TIME_NAME, final_date)

        url = u"/clients/{0}/persons-reservations-activations-per-user/" + filter_str

        all_activations = self.do_get_request(url.format(self.expected_ids[CLIENT_ENTITY_NAME]),
                                              expected_code=expected_code)
        if expected_code == 200:
            self.assertEqual(len(users), len(all_activations))
            users_to_find = set(users)
            for activations_per_user in all_activations:
                user = activations_per_user[self.USERNAME_NAME]
                self.assertIn(user, users_to_find)
                users_to_find.remove(user)
                self._check_person_activations_for_user(activations_per_user,
                                                        person_reservations_by_user[user])
        else:
            self.validate_error(all_activations, expected_internal_code)

    def _check_person_activations_for_user(self, activations_data, person_reservations):
        expected_activation_dates = dict()
        expected_deactivation_dates = dict()
        for person_reservation in person_reservations:
            reservation_key = (person_reservation[self.RESERVATION_ID_NAME], person_reservation[self.ID_NAME])
            if reservation_key not in expected_activation_dates:
                expected_activation_dates[reservation_key] = list()
                expected_deactivation_dates[reservation_key] = list()
            for activation_date in person_reservation[self.ACTIVATION_DATES_NAME]:
                expected_activation_dates[reservation_key].append(activation_date)
            for deactivation_date in person_reservation[self.DEACTIVATION_DATES_NAME]:
                expected_deactivation_dates[reservation_key].append(deactivation_date)
        activations = activations_data[self.ACTIVATIONS_NAME]
        deactivations = activations_data[self.DEACTIVATIONS_NAME]

        self._check_activations_or_deactivations(activations, expected_activation_dates)
        self._check_activations_or_deactivations(deactivations, expected_deactivation_dates)

    def _check_activations_or_deactivations(self, activations, expected_activation_dates):
        for activation in activations:
            reservation_key = (activation[self.RESERVATION_ID_NAME], activation[self.PERSON_RESERVATION_ID_NAME])
            self.assertIn(activation[self.OPERATION_TIME_NAME], expected_activation_dates[reservation_key])
            expected_activation_dates[reservation_key].remove(activation[self.OPERATION_TIME_NAME])
        all(len(reservation_activation_dates) == 0 for reservation_activation_dates in expected_activation_dates)

    def test_check_permissions_for_create_person_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER, CLIENT_ROLES
        self.NUMBER_OF_ENTITIES *= len(CLIENT_ROLES) + 4
        self.create_test_persons()
        self.NUMBER_OF_ENTITIES /= len(CLIENT_ROLES) + 4
        self.TEST_PACKAGE_ID_SOCIAL_EVENT = None
        create_test_package(self, create_new_package=True)
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()
        self.assign_field_value(self.IS_HOLDER_NAME, False)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_person_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_person_reservation(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservations_activations_by_user_report(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        create_test_person(self)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        url = "/clients/{0}/persons-reservations-activations-per-user/".format(self.expected_ids[CLIENT_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservations_by_person_document(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        create_test_person(self)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = "/clients/{0}/persons-reservations-by-document/?document-type={1}&document-number={2}".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                                                            self.TEST_PERSON_DOCUMENT_TYPE,
                                                                                                            self.TEST_PERSON_DOCUMENT_NUMBER)
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservation_available_funds(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])+"available-funds/"
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservation_balance(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])+"balance/"
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_person_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.check_update_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_patch_person_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.check_patch_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_delete_person_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_PROMOTER_USER, \
            CLIENT_CASHIER_USER, CLIENT_SALES_ROLE, CLIENT_ROLES
        self.TEST_PACKAGE_ID_SOCIAL_EVENT = None
        self.NUMBER_OF_ENTITIES *= len(CLIENT_ROLES) + 4
        self.create_test_persons()
        self.NUMBER_OF_ENTITIES /= len(CLIENT_ROLES) + 4
        create_test_package(self, create_new_package=True)
        create_test_reservation(self, create_new_reservation=True)
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_PROMOTER_USER, CLIENT_CASHIER_USER, CLIENT_SALES_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        self.assign_field_value(self.IS_HOLDER_NAME, False)

        def is_not_holder_person_reservation(person_reservation):
            return not person_reservation[self.IS_HOLDER_NAME]

        self.check_delete_permissions(allowed_roles, required_locations, is_not_holder_person_reservation)


PERSON_RESERVATION_ENTITY_NAME = ReservaPersonaViewTestCase.ENTITY_NAME


def change_person_reservation_active_status(test_class, id_person_reservation, active, expected_code=200):
    original_entity = test_class.do_get_request(test_class.get_item_url(ReservaPersonaViewTestCase)
                                                .format(id_person_reservation))
    values = {ReservaPersonaViewTestCase.ACTIVE_NAME: active}
    test_class.do_single_patch_request(ReservaPersonaViewTestCase, original_entity, values, expected_code=expected_code)


def create_test_person_reservation(test_class, create_new_person_reservation=False):
    if getattr(test_class, "TEST_PERSON_RESERVATION_ID_PACKAGE", None) is None:
        test_class.TEST_PERSON_RESERVATION_ID_PACKAGE = test_class.expected_ids[PACKAGE_ENTITY_NAME]
    return ReservaPersonaViewTestCase.create_sample_entity_for_another_class(test_class, create_new_person_reservation)


if __name__ == '__main__':
    unittest.main()


def _get_replacemente_function(running_entity, expected_document_number):
    def compensar_service_replacement(id_client, document_number):
        running_entity.assertEqual(running_entity.expected_ids[CLIENT_ENTITY_NAME], id_client)
        running_entity.assertEqual(expected_document_number, document_number)
        running_entity.external_service_called = True
    return compensar_service_replacement
