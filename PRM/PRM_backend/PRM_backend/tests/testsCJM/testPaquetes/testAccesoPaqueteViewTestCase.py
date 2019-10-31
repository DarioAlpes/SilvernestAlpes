# -*- coding: utf-8 -*
import unittest

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    PACKAGE_DOES_NOT_EXISTS_CODE, ACCESS_DOES_NOT_EXISTS_CODE, LOCATION_DOES_NOT_EXISTS_CODE, \
    ACCESS_INVALID_AMOUNT_INCLUDED_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package, PACKAGE_ENTITY_NAME


class AccesoPaqueteViewTestCase(FlaskClientBaseTestCase):
    PACKAGE_HISTORIC_ID_NAME = u"historic-id"
    ID_NAME = u"id"
    HISTORIC_ID_NAME = u"historic-id"
    AMOUNT_INCLUDED_NAME = u"amount-included"
    UNLIMITED_AMOUNT_NAME = u"unlimited-amount"
    PACKAGE_ID_NAME = u"id-package"
    LOCATION_ID_NAME = u"id-location"

    ENTITY_DOES_NOT_EXISTS_CODE = ACCESS_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/packages/{1}/accesses/"

    ATTRIBUTES_NAMES_BY_FIELDS = {AMOUNT_INCLUDED_NAME: "TEST_ACCESS_AMOUNT_INCLUDED",
                                  LOCATION_ID_NAME: "TEST_ACCESS_ID_LOCATION"}

    ENTITY_NAME = 'package-accesses'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_PACKAGE_NAME = "Test package"
    TEST_PACKAGE_PRICE = 100.5
    TEST_PACKAGE_DESCRIPTION = "Test description"
    TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
    TEST_PACKAGE_VALID_FROM = "19900101010101"
    TEST_PACKAGE_VALID_THROUGH = "20100101010101"
    TEST_PACKAGE_DURATION = 5
    TEST_PACKAGE_ID_SOCIAL_EVENT = None

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_RESERVATION_COMPANY = "Test Company"
    TEST_RESERVATION_NUMBER_PERSONS = 20
    TEST_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE * TEST_RESERVATION_NUMBER_PERSONS
    TEST_RESERVATION_INITIAL_DATE = TEST_PACKAGE_VALID_FROM
    TEST_RESERVATION_FINAL_DATE = TEST_PACKAGE_VALID_THROUGH

    def setUp(self):
        super(AccesoPaqueteViewTestCase, self).setUp()

        create_test_client(self)
        create_test_location(self)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        create_test_location(self, create_new_location=True)

        self.assign_field_value(self.LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.AMOUNT_INCLUDED_NAME] = 5
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.AMOUNT_INCLUDED_NAME] = 10
        return values

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        amount = request_values.get(cls.AMOUNT_INCLUDED_NAME)
        if amount is None:
            amount = 0
        request_values[cls.AMOUNT_INCLUDED_NAME] = amount
        request_values[cls.UNLIMITED_AMOUNT_NAME] = amount == 0

        return request_values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME, PACKAGE_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_non_existent_package(self):
        self.expected_ids[PACKAGE_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_try_query_non_existent_access(self):
        results = self.do_get_request(self.get_item_url(type(self))
                                      .format(1), expected_code=404)
        validate_error(self, results, ACCESS_DOES_NOT_EXISTS_CODE)

    def test_empty_accesses_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_accesses_with_limited_amount(self):
        self.do_create_requests()

    def test_create_valid_accesses_without_amount(self):
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, None)
        self.do_create_requests()

    def test_create_valid_accesses_with_unlimited_amount(self):
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, 0)
        self.do_create_requests()

    def test_create_invalid_accesses_with_non_existent_package(self):
        self.expected_ids[PACKAGE_ENTITY_NAME] += 1
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_create_invalid_accesses_with_non_existent_location(self):
        self.assign_field_value(self.LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_accesses_without_location(self):
        self.assign_field_value(self.LOCATION_ID_NAME, None)
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_create_invalid_accesses_with_invalid_amount(self):
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, -1)
        self.do_create_requests(expected_code=400, expected_internal_code=ACCESS_INVALID_AMOUNT_INCLUDED_CODE)

    def test_create_access_on_package_without_reservations_and_check_historic_id_didnt_change(self):
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.do_create_requests()
        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.assertEqual(original_historic_id, new_historic_id)

    def test_create_access_on_package_with_reservations_and_check_historic_id_changed_and_old_accesses_were_kept(self):
        from tests.testsCJM.testReservas.testReservaViewTestCase import create_test_reservation
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.do_create_requests()
        create_test_reservation(self)
        create_test_person_reservation(self)
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False,
                                number_of_default_entities=self.NUMBER_OF_ENTITIES)
        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.assertNotEquals(original_historic_id, new_historic_id)
        results = self.do_get_request(self.get_base_url(type(self)))
        self.assertEqual(len(results), 2 * self.NUMBER_OF_ENTITIES)

    def test_update_valid_accesses_with_limited_amount(self):
        self.do_create_requests()
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, 10)
        self.do_update_requests()

    def test_update_valid_accesses_without_amount(self):
        self.do_create_requests()
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, None)
        self.do_update_requests()

    def test_update_valid_accesses_with_unlimited_amount(self):
        self.do_create_requests()
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, 0)
        self.do_update_requests()

    def test_try_update_invalid_accesses_with_non_existent_package(self):
        self.do_create_requests()
        self.expected_ids[PACKAGE_ENTITY_NAME] += 1
        self.do_update_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_update_invalid_non_existent_accesses(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_update_requests(expected_code=404,
                                expected_internal_code=ACCESS_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_accesses_with_non_existent_location(self):
        self.do_create_requests()
        self.assign_field_value(self.LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 1)
        self.do_update_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_accesses_without_empty_location(self):
        self.do_create_requests()
        self.assign_field_value(self.LOCATION_ID_NAME, None)
        self.do_update_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_try_update_invalid_accesses_with_invalid_amount(self):
        self.do_create_requests()
        self.assign_field_value(self.AMOUNT_INCLUDED_NAME, -1)
        self.do_update_requests(expected_code=400, expected_internal_code=ACCESS_INVALID_AMOUNT_INCLUDED_CODE)

    def test_update_access_on_package_without_reservations_and_check_historic_ids_did_not_change(self):
        self.do_create_requests()
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]

        original_accesses_historic_ids = {original_access[self.HISTORIC_ID_NAME]
                                          for original_access in self.original_entities}

        self.do_update_requests()

        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]

        new_accesses_historic_ids = {new_access[self.HISTORIC_ID_NAME]
                                     for new_access in self.original_entities}

        self.assertEqual(original_historic_id, new_historic_id)
        self.assertEqual(original_accesses_historic_ids, new_accesses_historic_ids)

    def test_update_accesses_on_package_with_reservations_and_check_historic_id_changed(self):
        from tests.testsCJM.testReservas.testReservaViewTestCase import create_test_reservation
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation
        self.do_create_requests()
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        original_accesses_historic_ids = {original_access[self.HISTORIC_ID_NAME]
                                          for original_access in self.original_entities}

        create_test_reservation(self)
        create_test_person_reservation(self)
        self.do_update_requests()

        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]

        new_accesses_historic_ids = {new_access[self.HISTORIC_ID_NAME]
                                     for new_access in self.original_entities}

        self.assertNotEquals(original_historic_id, new_historic_id)
        self.assertTrue(len(new_accesses_historic_ids.intersection(original_accesses_historic_ids)) == 0)

    def test_check_permissions_for_create_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_access(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_accesses(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_update_permissions(allowed_roles, required_locations)


ACCESS_ENTITY_NAME = AccesoPaqueteViewTestCase.ENTITY_NAME


def create_test_access(test_class, create_new_access=False):
    return AccesoPaqueteViewTestCase.create_sample_entity_for_another_class(test_class, create_new_access)

if __name__ == '__main__':
    unittest.main()
