# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, PERSON_ACCESS_DOES_NOT_EXISTS_CODE, \
    LOCATION_DOES_NOT_EXISTS_CODE, PERSON_RESERVATION_DOES_NOT_EXISTS_CODE, PERSON_ACCESS_INVALID_ACCESS_TIME_CODE, \
    PERSON_ACCESS_INVALID_ACCESSES_CODE, RESERVATION_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testOrders import LOCATION_ID_NAME, \
    RESERVATION_ID_NAME, PERSON_RESERVATION_ID_NAME, ACCESSES_NAME, ACCESS_TIME_NAME, \
    OK_OVERFLOW_STATE, OVERFLOWN_OVERFLOW_STATE, get_and_check_available_funds_for_access, get_and_check_balance_for_access, \
    get_and_check_parent_balance_for_access
from tests.testsCJM.testPaquetes.testAccesoPaqueteViewTestCase import create_test_access
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas import create_and_login_new_admin_user, get_and_check_entities_by_user_report, \
    USERNAME_NAME, ADMIN_USERNAME
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
    PERSON_RESERVATION_ENTITY_NAME, change_person_reservation_active_status
from tests.testsCJM.testReservas.testReservaViewTestCase import RESERVATION_ENTITY_NAME, create_test_reservation


class PersonAccessesForPackageWithAccessesViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = PERSON_ACCESS_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/person-accesses/"
    SPECIFIC_RESOURCE_BASE_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/person-accesses/"

    ATTRIBUTES_NAMES_BY_FIELDS = {LOCATION_ID_NAME: "TEST_PERSON_ACCESS_ID_LOCATION",
                                  ACCESSES_NAME: "TEST_PERSON_ACCESS_ACCESSES"}

    NUMBER_OF_ENTITIES = 1
    ENTITY_NAME = 'person-accesses'

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

    UNLIMITED_AMOUNT_NAME = u"unlimited-amount"

    def _create_package_location(self):
        create_test_location(self, create_new_location=True)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]

    def _create_package_with_access(self):
        create_test_package(self, create_new_package=True)
        create_test_location(self, create_new_location=True)
        self.TEST_ACCESS_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        total_consumptions = self.NUMBER_OF_ENTITIES * self.number_of_accesses
        self.TEST_ACCESS_AMOUNT_INCLUDED = total_consumptions
        create_test_access(self, create_new_access=True)

    def create_package_with_restricted_consumption(self):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
        self._create_package_with_access()

    def create_package_without_restricted_consumption(self):
        self._create_package_location()
        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = False
        self._create_package_with_access()

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

    def create_test_accesses(self):
        accesses = []
        for access_number in range(self.number_of_accesses):
            if self.access_time_template is None:
                access_time = None
            else:
                access_time = self.access_time_template.format(access_number)
            access = {RESERVATION_ID_NAME: self.expected_ids[RESERVATION_ENTITY_NAME],
                      PERSON_RESERVATION_ID_NAME: self.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
                      ACCESS_TIME_NAME: access_time}
            accesses.append(access)
        return accesses

    def create_and_assign_accesses(self):
        accesses = self.create_test_accesses()
        self.assign_field_value(ACCESSES_NAME, accesses)
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])

    def _check_person_accesses_with_user_and_children_data(self, results, username):
        topoffs = results[0][PERSON_RESERVATION_ENTITY_NAME][0][self.ENTITY_NAME]
        self.check_list_response(self.ENTITY_NAME, topoffs, len(topoffs))
        for topoff in topoffs:
            self.assertEqual(username, topoff.get(USERNAME_NAME, None))

    def setUp(self):
        super(PersonAccessesForPackageWithAccessesViewTestCase, self).setUp()
        create_test_client(self)

        self.number_of_accesses = 1
        self.access_time_template = "2010010101010{0}"
        self.is_unlimited = False
        self.expected_state = OK_OVERFLOW_STATE

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        accesses = request_values.get(ACCESSES_NAME)
        for access in accesses:
            access[LOCATION_ID_NAME] = request_values.get(LOCATION_ID_NAME)
        return accesses

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def get_ancestor_entities_names_for_specific_resource(cls):
        return [CLIENT_ENTITY_NAME, RESERVATION_ENTITY_NAME, PERSON_RESERVATION_ENTITY_NAME]

    def test_empty_order_view(self):
        self.request_all_resources_and_check_result(0)

    def test_invalid_order_view_with_invalid_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_create_valid_person_accesses(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_empty_person_accesses(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_accesses_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_person_accesses_and_query_them_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_accesses_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_person_accesses_and_query_them_with_reservations_endpoint_true_include_children_and_new_user(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        create_and_login_new_admin_user(self)
        self.create_and_assign_accesses()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_accesses_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_empty_person_accesses(self):
        self.ENTITIES_PER_REQUEST = 0
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])
        self.assign_field_value(ACCESSES_NAME, [])
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses()

    def test_create_valid_person_accesses_with_access_time_on_first_second_of_reservation(self):
        self.access_time_template = self.TEST_RESERVATION_INITIAL_DATE
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_valid_person_accesses_with_access_time_on_last_second_of_reservation(self):
        self.access_time_template = self.TEST_RESERVATION_FINAL_DATE
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_invalid_person_accesses_with_access_time_smaller_than_reservation_date(self):
        self.access_time_template = "20090101010101"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_invalid_person_accesses_with_access_time_greater_than_reservation_date(self):
        self.access_time_template = "20200101010101"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_valid_person_accesses_exceeding_amount_limit(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.access_time_template = "2010010101020{0}"
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 2 * self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_duplicated_person_accesses_on_different_request(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_duplicated_person_accesses_on_the_same_request(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.access_time_template = "20200101010101"
        self.number_of_accesses = 2
        self.create_and_assign_accesses()
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        self.check_person_accesses()

    def test_create_multiple_valid_person_accesses_with_multiple_accesses(self):
        self.number_of_accesses = 3
        self.ENTITIES_PER_REQUEST = self.number_of_accesses
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_valid_person_accesses_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100102010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100102010101"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.access_time_template = "2010010101020{0}"
        self.create_and_assign_accesses()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 2 * self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses()

    def test_create_valid_person_accesses_with_multiple_person_reservations(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])
        id_first_person = self.expected_ids[PERSON_ENTITY_NAME]
        first_reservation_accesses = self.create_test_accesses()
        self.TEST_PERSON_DOCUMENT_NUMBER += "10"
        self.create_person_reservation_for_last_package()
        second_reservation_accesses = self.create_test_accesses()
        self.assign_field_value(ACCESSES_NAME, first_reservation_accesses + second_reservation_accesses)
        self.ENTITIES_PER_REQUEST = 2
        PersonAccessesForPackageWithAccessesViewTestCase.CHECK_BY_GET = False
        self.do_create_requests()
        PersonAccessesForPackageWithAccessesViewTestCase.CHECK_BY_GET = True

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     id_first_person))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES * self.number_of_accesses)
        self.check_person_accesses(number_of_person_reservations=2)

    def test_try_create_invalid_person_accesses_with_non_existent_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 2000)
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses()

    def test_try_create_invalid_person_accesses_without_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.assign_field_value(LOCATION_ID_NAME, None)
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses()

    def test_try_create_invalid_amount_without_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.expected_ids[RESERVATION_ENTITY_NAME] = None
        self.create_and_assign_accesses()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)

    def test_try_create_invalid_amount_with_non_existent_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.expected_ids[RESERVATION_ENTITY_NAME] += 2000
        self.create_and_assign_accesses()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_amount_without_person_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = None
        self.create_and_assign_accesses()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)

    def test_try_create_invalid_amount_with_non_existent_person_reservation_id(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] += 2000
        self.create_and_assign_accesses()

        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_person_accesses_without_access_time(self):
        self.access_time_template = None
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_ACCESS_INVALID_ACCESS_TIME_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses()

    def test_try_create_invalid_person_accesses_with_invalid_access_time(self):
        self.access_time_template = "INVALID_DATE"
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_ACCESS_INVALID_ACCESS_TIME_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses()

    def test_try_create_invalid_person_accesses_without_accesses(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.assign_field_value(LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])
        self.do_create_requests(expected_code=400, expected_internal_code=PERSON_ACCESS_INVALID_ACCESSES_CODE)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses()

    def test_delete_valid_person_accesses(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        self.do_delete_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        self.check_person_accesses()

    def test_delete_valid_persons_consumptions_and_create_them_again(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_delete_requests()
        self.do_create_requests()

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 2 * self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        self.check_person_accesses()

    def test_query_person_accesses_with_reservations_endpoint_after_delete_with_true_include_children_and_check_nothing_is_returned(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_delete_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_person_accesses_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_try_delete_valid_person_accesses_with_non_existent_client(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        original_id_client = self.expected_ids[CLIENT_ENTITY_NAME]
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(original_id_client,
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(original_id_client,
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_try_delete_valid_person_accesses_with_non_existent_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        self.expected_ids[RESERVATION_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_valid_person_accesses_with_non_existent_person_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)
        self.check_person_accesses(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_non_existent_person_accesses(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=PERSON_ACCESS_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

        results_events = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), self.NUMBER_OF_ENTITIES)
        results_events = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertEqual(len(results_events), 0)

    def test_check_available_funds_and_balance_for_new_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_amount = self.TEST_ACCESS_AMOUNT_INCLUDED
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_new_reservation_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_amount = self.TEST_ACCESS_AMOUNT_INCLUDED * number_of_days
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_new_reservation_and_unlimited_access(self):
        self.number_of_accesses = 0
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.expected_amount = 0
        self.is_unlimited = True
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_full_reservation(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.expected_amount = 0
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_full_reservation_consuming_child_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.TEST_LOCATION_PARENT_LOCATION_ID = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_LOCATION_TYPE = "ZONE"
        create_test_location(self, create_new_location=True)

        self.create_and_assign_accesses()
        self.do_create_requests()
        self.expected_amount = 0
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

        self.expected_ids[LOCATION_ENTITY_NAME] = self.TEST_LOCATION_PARENT_LOCATION_ID
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_overflown_reservation_consuming_child_location(self):
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.TEST_LOCATION_PARENT_LOCATION_ID = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_LOCATION_TYPE = "ZONE"
        create_test_location(self, create_new_location=True)

        self.create_and_assign_accesses()
        self.do_create_requests()
        self.access_time_template = "2010010101020{0}"
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.expected_amount = 0
        self.expected_state = OVERFLOWN_OVERFLOW_STATE
        get_and_check_available_funds_for_access(self)
        self.expected_amount = -self.TEST_ACCESS_AMOUNT_INCLUDED
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

        self.expected_ids[LOCATION_ENTITY_NAME] = self.TEST_LOCATION_PARENT_LOCATION_ID
        self.expected_amount = 0
        self.expected_state = OK_OVERFLOW_STATE
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_full_reservation_with_multiple_days(self):
        self.TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_RESERVATION_INITIAL_DATE = "20100101010101"
        self.TEST_PERSON_RESERVATION_FINAL_DATE = "20100103010101"
        self.TEST_RESERVATION_FINAL_DATE = "20100103010101"
        number_of_days = 3

        self.create_package_with_restricted_consumption_and_person_reservation()
        self.number_of_accesses = number_of_days
        self.ENTITIES_PER_REQUEST = number_of_days
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.expected_amount = 0
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_reservation_with_person_accesses_and_unlimited_access(self):
        self.number_of_accesses = 0
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.number_of_accesses = 1
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.expected_amount = 0
        self.is_unlimited = True
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_reservation_with_excess_person_accesses(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.create_and_assign_accesses()
        self.do_create_requests()
        self.access_time_template = "2010010101020{0}"
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.expected_amount = 0
        self.expected_state = OVERFLOWN_OVERFLOW_STATE
        get_and_check_available_funds_for_access(self)
        self.expected_amount = -self.TEST_ACCESS_AMOUNT_INCLUDED
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_previously_full_reservation_after_delete(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_delete_requests()
        self.expected_amount = self.TEST_ACCESS_AMOUNT_INCLUDED
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_available_funds_and_balance_for_previously_reservation_with_excess_accesses_after_delete(self):
        self.create_package_with_restricted_consumption_and_person_reservation()

        self.create_and_assign_accesses()
        self.do_create_requests()
        self.access_time_template = "2010010101020{0}"
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_delete_requests()
        self.expected_amount = self.TEST_ACCESS_AMOUNT_INCLUDED
        get_and_check_available_funds_for_access(self)
        get_and_check_balance_for_access(self)
        get_and_check_parent_balance_for_access(self)

    def test_check_empty_entities_by_user_report_with_person_accesses_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_accesses(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_accesses_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()

        self.access_time_template = "2010010201010{0}"
        self.create_and_assign_accesses()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_person_accesses_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self.clean_test_data()

        self.access_time_template = "2010010201010{0}"
        self.create_and_assign_accesses()
        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests(do_get_and_check_results=False)
        expected_entities_by_user[self.TEST_USER_USERNAME] = self.original_entities
        users.add(self.TEST_USER_USERNAME)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_accesses_after_delete(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_person_accesses_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_person_accesses_after_delete_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_package_with_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        users = {self.TEST_USER_USERNAME}
        self.do_delete_requests()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=True)

    def test_check_permissions_for_create_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_ACCESS_USER
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_ACCESS_USER}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_WAITER_USER,
                         CLIENT_CASHIER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservation_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_WAITER_USER,
                         CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.SPECIFIC_RESOURCE_BASE_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[RESERVATION_ENTITY_NAME],
                                                     self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_access(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_WAITER_USER,
                         CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.create_package_without_restricted_consumption_and_person_reservation()
        self.create_and_assign_accesses()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_delete_permissions(allowed_roles, required_locations)

    def check_person_accesses(self, expected_code=200,
                              expected_internal_code=None,
                              number_of_person_reservations=1):
        original_accesses = self.original_entities
        reservation_orders_url = self.SPECIFIC_RESOURCE_BASE_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                        self.expected_ids[RESERVATION_ENTITY_NAME],
                                                                        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        results_reservation_orders = self.do_get_request(reservation_orders_url,
                                                         expected_code=expected_code)
        if expected_code == 200:
            self.ENTITIES_PER_REQUEST = 1
            if number_of_person_reservations > 1:
                self.clean_test_data()
                original_accesses = [access for access in original_accesses
                                     if access[PERSON_RESERVATION_ID_NAME] == self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]]
                self._add_data_values(PersonAccessesForPackageWithAccessesViewTestCase, original_accesses)
            self.check_list_response(self.ENTITY_NAME, results_reservation_orders, len(original_accesses))
        else:
            self.validate_error(results_reservation_orders, expected_internal_code)


def create_test_person_access(test_class, create_new_access=False):
    access = {ACCESS_TIME_NAME: getattr(test_class, 'TEST_PERSON_ACCESS_TIME', "20100101010101"),
              RESERVATION_ID_NAME: test_class.expected_ids[RESERVATION_ENTITY_NAME],
              PERSON_RESERVATION_ID_NAME: test_class.expected_ids[PERSON_RESERVATION_ENTITY_NAME]}
    test_class.TEST_PERSON_ACCESS_ACCESSES = [access]
    return PersonAccessesForPackageWithAccessesViewTestCase.create_sample_entity_for_another_class(test_class,
                                                                                                   create_new_access)

if __name__ == '__main__':
    unittest.main()
