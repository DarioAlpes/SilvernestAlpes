# -*- coding: utf-8 -*
import unittest

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    RESERVATION_DOES_NOT_EXISTS_CODE, PERSON_RESERVATION_DOES_NOT_EXISTS_CODE, ACCESS_TOPOFF_DOES_NOT_EXISTS_CODE, \
    LOCATION_DOES_NOT_EXISTS_CODE, ACCESS_TOPOFF_INVALID_AMOUNT_CODE, ACCESS_TOPOFF_INVALID_TRANSACTION_NUMBER_CODE, \
    ACCESS_TOPOFF_INVALID_TOPOFFS_CODE, \
    ACCESS_TOPOFF_INVALID_TOPOFF_TIME_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas import create_and_login_new_admin_user, get_and_check_entities_by_user_report, \
    ADMIN_USERNAME, USERNAME_NAME
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import PERSON_RESERVATION_ENTITY_NAME, \
    change_person_reservation_active_status, create_test_person_reservation
from tests.testsCJM.testReservas.testReservaViewTestCase import RESERVATION_ENTITY_NAME, create_test_reservation
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME


class AccessTopoffsViewTestCase(FlaskClientBaseTestCase):
    AMOUNT_NAME = u"amount"
    ID_LOCATION_NAME = u"id-location"
    UNLIMITED_AMOUNT_NAME = u"unlimited-amount"
    TRANSACTION_NUMBER_NAME = u"transaction-number"
    PACKAGE_ID_NAME = u"id-package"
    RESERVATION_ID_NAME = u"id-reservation"
    PERSON_RESERVATION_ID_NAME = u"id-person-reservation"
    TOPOFF_TIME_NAME = u"topoff-time"
    TOPOFFS_NAME = u"topoffs"
    ID_NAME = u"id"

    ENTITY_DOES_NOT_EXISTS_CODE = ACCESS_TOPOFF_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/access-topoffs/"
    SPECIFIC_RESOURCE_BASE_URL = u"/clients/{0}/reservations/{1}/persons-reservations/{2}/access-topoffs/"

    ATTRIBUTES_NAMES_BY_FIELDS = {TOPOFFS_NAME: "TEST_ACCESS_TOPOFF_TOPOFFS"}
    ENTITY_NAME = 'access-topoffs'
    NUMBER_OF_ENTITIES = 1

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password_123"
    TEST_USER_ROLE = None

    TEST_PACKAGE_NAME = "Test package"
    TEST_PACKAGE_PRICE = 100.5
    TEST_PACKAGE_DESCRIPTION = "Test description"
    TEST_PACKAGE_VALID_FROM = "19900101010101"
    TEST_PACKAGE_VALID_THROUGH = "20100101010101"
    TEST_PACKAGE_DURATION = 5
    TEST_PACKAGE_ID_SOCIAL_EVENT = None

    NUMBER_LOCATIONS = 5
    TEST_LOCATION_TYPE = TipoUbicacion.CITY
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

    TEST_PERSON_RESERVATION_ID_RESERVATION = None
    TEST_PERSON_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE
    TEST_PERSON_RESERVATION_INITIAL_DATE = TEST_PACKAGE_VALID_FROM
    TEST_PERSON_RESERVATION_FINAL_DATE = TEST_PACKAGE_VALID_THROUGH

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_RESERVATION_COMPANY = "Test company"
    TEST_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE * 50
    TEST_RESERVATION_INITIAL_DATE = TEST_PACKAGE_VALID_FROM
    TEST_RESERVATION_FINAL_DATE = TEST_PACKAGE_VALID_THROUGH

    def setUp(self):
        super(AccessTopoffsViewTestCase, self).setUp()

        active = True

        create_test_client(self)

        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

        create_test_person(self)

        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]

        create_test_location(self)

        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]

        self.TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
        create_test_package(self, create_new_package=True)

        create_test_reservation(self, create_new_reservation=True)
        self.TEST_PERSON_RESERVATION_ID_RESERVATION = self.expected_ids[RESERVATION_ENTITY_NAME]
        create_test_person_reservation(self, create_new_person_reservation=True)
        change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], active)
        create_test_location(self, create_new_location=True)
        self.number_of_topoffs = 1
        self.topoff_time_template = "2010010101010{0}"
        self.transaction_number = "123456"
        self.amount_included = 5

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        topoffs = request_values.get(cls.TOPOFFS_NAME)
        for topoff in topoffs:
            if topoff.get(cls.AMOUNT_NAME) is None:
                topoff[cls.AMOUNT_NAME] = 0
            topoff[cls.UNLIMITED_AMOUNT_NAME] = topoff[cls.AMOUNT_NAME] == 0
        return topoffs

    def create_test_topoffs(self, create_new_location_per_topoff=False):
        topoffs = []
        for topoff_number in range(self.number_of_topoffs):
            if self.topoff_time_template is None:
                topoff_time = None
            else:
                topoff_time = self.topoff_time_template.format(topoff_number)
            create_test_location(self, create_new_location=create_new_location_per_topoff)
            topoff = {self.RESERVATION_ID_NAME: self.expected_ids[RESERVATION_ENTITY_NAME],
                      self.PERSON_RESERVATION_ID_NAME: self.expected_ids[PERSON_RESERVATION_ENTITY_NAME],
                      self.TOPOFF_TIME_NAME: topoff_time,
                      self.ID_LOCATION_NAME: self.expected_ids[LOCATION_ENTITY_NAME],
                      self.TRANSACTION_NUMBER_NAME: self.transaction_number,
                      self.AMOUNT_NAME: self.amount_included}
            topoffs.append(topoff)
        return topoffs

    def create_and_assign_topoffs(self, create_new_location_per_topoff=False):
        topoffs = self.create_test_topoffs(create_new_location_per_topoff)
        self.assign_field_value(self.TOPOFFS_NAME, topoffs)

    def _check_access_topoffs_with_user_and_children_data(self, results, username):
        topoffs = results[0][PERSON_RESERVATION_ENTITY_NAME][0][self.ENTITY_NAME]
        self.check_list_response(self.ENTITY_NAME, topoffs, len(topoffs))
        for topoff in topoffs:
            self.assertEqual(username, topoff.get(USERNAME_NAME, None))

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def get_ancestor_entities_names_for_specific_resource(cls):
        return [CLIENT_ENTITY_NAME, RESERVATION_ENTITY_NAME, PERSON_RESERVATION_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_access_topoffs_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_access_topoffs(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_valid_reservations_and_query_them_with_true_include_children_and_empty_access_topoffs(self):
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_access_topoffs_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_access_topoffs_and_query_them_with_reservations_endpoint_true_include_children_and_default_user(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_access_topoffs_with_user_and_children_data(results, ADMIN_USERNAME)

    def test_create_valid_access_topoffs_and_query_them_with_reservations_endpoint_true_include_children_and_new_user(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_access_topoffs_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_create_valid_access_topoffs_with_empty_list(self):
        self.ENTITIES_PER_REQUEST = 0
        self.assign_field_value(self.TOPOFFS_NAME, [])
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_valid_access_topoffs_with_zero_amount(self):
        self.amount_included = 0
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_valid_access_topoffs_without_amount(self):
        self.amount_included = None
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_valid_access_topoffs_with_topoff_time_on_first_second_of_reservation(self):
        self.topoff_time_template = self.TEST_RESERVATION_INITIAL_DATE
        self.create_and_assign_topoffs()
        self.do_create_requests()

    def test_create_valid_access_topoffs_with_topoff_time_on_last_second_of_reservation(self):
        self.topoff_time_template = self.TEST_RESERVATION_FINAL_DATE
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_multiple_access_topoffs_on_the_same_request(self):
        self.number_of_topoffs = 2
        self.ENTITIES_PER_REQUEST = self.number_of_topoffs
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_duplicated_access_topoffs_on_different_request(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)
        self.check_person_topoffs()

    def test_create_duplicated_access_topoffs_on_the_same_request(self):
        self.topoff_time_template = "20100101010101"
        self.number_of_topoffs = 2
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_invalid_access_topoffs_with_topoff_time_smaller_than_reservation_date(self):
        self.topoff_time_template = "20090101010101"
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_invalid_access_topoffs_with_non_active_reservation(self):
        change_person_reservation_active_status(self, self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], False)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_create_invalid_access_topoffs_with_topoff_time_greater_than_reservation_date(self):
        self.topoff_time_template = "20200101010101"
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_without_topoffs(self):
        self.do_create_requests(expected_code=400,
                                expected_internal_code=ACCESS_TOPOFF_INVALID_TOPOFFS_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_without_access_time(self):
        self.topoff_time_template = None
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=400, expected_internal_code=ACCESS_TOPOFF_INVALID_TOPOFF_TIME_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_with_invalid_access_time(self):
        self.topoff_time_template = "INVALID_DATE"
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=400, expected_internal_code=ACCESS_TOPOFF_INVALID_TOPOFF_TIME_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_without_id_location(self):
        topoffs = self.create_test_topoffs()
        for topoff in topoffs:
            topoff[self.ID_LOCATION_NAME] = None
        self.assign_field_value(self.TOPOFFS_NAME, topoffs)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_with_non_existent_location(self):
        topoffs = self.create_test_topoffs()
        for topoff in topoffs:
            topoff[self.ID_LOCATION_NAME] += 2000
        self.assign_field_value(self.TOPOFFS_NAME, topoffs)
        self.do_create_requests(expected_code=404,
                                expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_without_reservation_id(self):
        self.expected_ids[RESERVATION_ENTITY_NAME] = None
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_access_topoffs_with_non_existent_reservation_id(self):
        self.expected_ids[RESERVATION_ENTITY_NAME] += 2000
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_access_topoffs_without_person_reservation_id(self):
        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] = None
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_access_topoffs_with_non_existent_person_reservation_id(self):
        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME] += 2000
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_access_topoffs_with_negative_amount(self):
        self.amount_included = -5
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=400,
                                expected_internal_code=ACCESS_TOPOFF_INVALID_AMOUNT_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_with_string_amount(self):
        self.amount_included = "5"
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=400,
                                expected_internal_code=ACCESS_TOPOFF_INVALID_AMOUNT_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_without_transaction_number(self):
        self.transaction_number = None
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=400,
                                expected_internal_code=ACCESS_TOPOFF_INVALID_TRANSACTION_NUMBER_CODE)
        self.check_person_topoffs()

    def test_try_create_invalid_access_topoffs_with_empty_transaction_number(self):
        self.transaction_number = ""
        self.create_and_assign_topoffs()
        self.do_create_requests(expected_code=400,
                                expected_internal_code=ACCESS_TOPOFF_INVALID_TRANSACTION_NUMBER_CODE)
        self.check_person_topoffs()

    def test_delete_valid_access_topoffs(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.do_delete_requests()
        self.check_person_topoffs()

    def test_query_access_topoffs_with_reservations_endpoint_after_delete_with_true_include_children_and_check_nothing_is_returned(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.do_delete_requests()
        results = self.do_get_request("/clients/{0}/reservations?include-children=true"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self._check_access_topoffs_with_user_and_children_data(results, self.TEST_USER_USERNAME)

    def test_delete_valid_access_topoffs_and_create_them_again(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.do_delete_requests()
        self.do_create_requests()
        self.check_person_topoffs()

    def test_try_delete_invalid_access_topoffs_with_wrong_id_client(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        create_test_client(self, create_new_client=True)
        self.do_delete_requests(expected_code=404, do_get_and_check_results=False,
                                expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs(expected_code=404, expected_internal_code=RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_invalid_access_topoffs_with_wrong_id_reservation(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        create_test_reservation(self, create_new_reservation=True)
        self.do_delete_requests(expected_code=404, do_get_and_check_results=False,
                                expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs(expected_code=404, expected_internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_CODE)

    def test_try_delete_invalid_access_topoffs_with_wrong_id_person_reservation(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        create_test_person(self, create_new_person=True)
        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
        create_test_person_reservation(self, create_new_person_reservation=True)
        self.do_delete_requests(expected_code=404, do_get_and_check_results=False,
                                expected_internal_code=ACCESS_TOPOFF_DOES_NOT_EXISTS_CODE)

    def test_try_delete_invalid_non_existent_access_topoffs(self):
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404,
                                expected_internal_code=ACCESS_TOPOFF_DOES_NOT_EXISTS_CODE)
        self.check_person_topoffs()

    def test_check_empty_entities_by_user_report_with_access_topoffs_on_different_client(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        expected_entities_by_user = dict()
        users = {}
        create_test_client(self, create_new_client=True)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_access_topoffs(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_access_topoffs_with_the_same_user(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()

        self.topoff_time_template = "2010010201010{0}"
        self.create_and_assign_topoffs()
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES)
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_multiple_access_topoffs_with_different_user(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: self.original_entities}
        users = {self.TEST_USER_USERNAME}
        self.clean_test_data()

        self.topoff_time_template = "2010010201010{0}"
        self.create_and_assign_topoffs()
        self.TEST_USER_USERNAME += "_other"
        create_and_login_new_admin_user(self)
        self.do_create_requests(do_get_and_check_results=False)
        expected_entities_by_user[self.TEST_USER_USERNAME] = self.original_entities
        users.add(self.TEST_USER_USERNAME)
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_access_topoffs_after_delete(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME)

    def test_check_entities_by_user_report_with_access_topoffs_after_delete_with_false_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        self.do_delete_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: []}
        users = {self.TEST_USER_USERNAME}
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=False)

    def test_check_entities_by_user_report_with_access_topoffs_after_delete_with_true_include_deleted(self):
        create_and_login_new_admin_user(self)
        self.create_and_assign_topoffs()
        self.do_create_requests()
        expected_entities_by_user = {self.TEST_USER_USERNAME: list(self.original_entities)}
        users = {self.TEST_USER_USERNAME}
        self.do_delete_requests()
        get_and_check_entities_by_user_report(self, expected_entities_by_user, users, self.ENTITY_NAME,
                                              include_deleted=True)

    def test_check_permissions_for_create_access_topoffs(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE
        self.create_and_assign_topoffs()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_access_topoffs(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_and_assign_topoffs()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_WAITER_USER,
                         CLIENT_CASHIER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_person_reservation_access_topoffs(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_and_assign_topoffs()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_WAITER_USER,
                         CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.SPECIFIC_RESOURCE_BASE_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[RESERVATION_ENTITY_NAME],
                                                     self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_access_topoff(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_WAITER_USER, CLIENT_CASHIER_USER
        self.create_and_assign_topoffs()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_WAITER_USER,
                         CLIENT_CASHIER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_access_topoffs(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.create_and_assign_topoffs()
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_delete_permissions(allowed_roles, required_locations)

    def check_person_topoffs(self, expected_code=200,
                             expected_internal_code=None,
                             number_of_person_reservations=1):
        original_topoffs = self.original_entities
        reservation_orders_url = self.SPECIFIC_RESOURCE_BASE_URL.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                        self.expected_ids[RESERVATION_ENTITY_NAME],
                                                                        self.expected_ids[PERSON_RESERVATION_ENTITY_NAME])
        results_reservation_orders = self.do_get_request(reservation_orders_url,
                                                         expected_code=expected_code)
        if expected_code == 200:
            self.ENTITIES_PER_REQUEST = 1
            if number_of_person_reservations > 1:
                self.clean_test_data()
                original_topoffs = [topoff for topoff in original_topoffs
                                    if topoff[self.PERSON_RESERVATION_ID_NAME] == self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]]
                self._add_data_values(AccessTopoffsViewTestCase, original_topoffs)
            self.check_list_response(self.ENTITY_NAME, results_reservation_orders, len(original_topoffs))
        else:
            self.validate_error(results_reservation_orders, expected_internal_code)


def create_test_access_topoff(test_class, create_new_topoff=False):
    topoff = {AccessTopoffsViewTestCase.TOPOFF_TIME_NAME: getattr(test_class, 'TEST_ACCESS_TOPOFF_TOPOFF_TIME', "20100101010101"),
              AccessTopoffsViewTestCase.AMOUNT_NAME: getattr(test_class, 'TEST_ACCESS_TOPOFF_AMOUNT', 5),
              AccessTopoffsViewTestCase.ID_LOCATION_NAME: getattr(test_class, 'TEST_ACCESS_TOPOFF_ID_LOCATION'),
              AccessTopoffsViewTestCase.TRANSACTION_NUMBER_NAME: getattr(test_class, 'TEST_ACCESS_TOPOFF_TRANSACTION_NUMBER', "123456"),
              AccessTopoffsViewTestCase.RESERVATION_ID_NAME: test_class.expected_ids[RESERVATION_ENTITY_NAME],
              AccessTopoffsViewTestCase.PERSON_RESERVATION_ID_NAME: test_class.expected_ids[PERSON_RESERVATION_ENTITY_NAME]}
    test_class.TEST_ACCESS_TOPOFF_TOPOFFS = [topoff]
    return AccessTopoffsViewTestCase.create_sample_entity_for_another_class(test_class, create_new_topoff)


if __name__ == '__main__':
    unittest.main()
