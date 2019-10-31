# -*- coding: utf-8 -*
import unittest

from CJM.entidades.paquetes.Paquete import Paquete
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    LOCATION_DOES_NOT_EXISTS_CODE, PACKAGE_INVALID_NAME_CODE, \
    PACKAGE_INVALID_PRICE_CODE, PACKAGE_INVALID_DESCRIPTION_CODE, PACKAGE_INVALID_RESTRICTED_CONSUMPTION_CODE, \
    PACKAGE_INVALID_VALID_FROM_CODE, PACKAGE_INVALID_VALID_THROUGH_CODE, \
    PACKAGE_INVALID_RANGE_OF_DATES_FROM_GREATER_THAN_THROUGH_CODE, PACKAGE_DOES_NOT_EXISTS_CODE, \
    PACKAGE_INVALID_BASE_TIME_CODE, PACKAGE_INVALID_TAX_RATE_CODE, PACKAGE_INVALID_EXTERNAL_CODE_CODE, \
    PACKAGE_INVALID_AVAILABLE_FOR_SALE_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import LOCATION_ENTITY_NAME, create_test_location


class PaqueteViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"
    HISTORIC_ID_NAME = u"historic-id"
    PACKAGE_NAME_NAME = u"name"
    BASE_PRICE_NAME = u"base-price"
    EXTERNAL_CODE_NAME = u"external-code"
    DESCRIPTION_NAME = u"description"
    RESTRICTED_CONSUMPTION_NAME = u"restricted-consumption"
    VALID_FROM_NAME = u"valid-from"
    VALID_THROUGH_NAME = u"valid-through"
    ACTIVE_NAME = u"active"
    LOCATION_ID_NAME = u"id-location"
    TAX_RATE_NAME = u"tax-rate"
    BASE_TIME_NAME = u"base-time"
    AVAILABLE_FOR_SALE_NAME = u"available-for-sale"

    ENTITY_DOES_NOT_EXISTS_CODE = PACKAGE_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/packages/"

    ATTRIBUTES_NAMES_BY_FIELDS = {PACKAGE_NAME_NAME: "TEST_PACKAGE_NAME",
                                  BASE_PRICE_NAME: "TEST_PACKAGE_PRICE",
                                  DESCRIPTION_NAME: "TEST_PACKAGE_DESCRIPTION",
                                  RESTRICTED_CONSUMPTION_NAME: "TEST_PACKAGE_RESTRICTED_CONSUMPTION",
                                  VALID_FROM_NAME: "TEST_PACKAGE_VALID_FROM",
                                  VALID_THROUGH_NAME: "TEST_PACKAGE_VALID_THROUGH",
                                  LOCATION_ID_NAME: "TEST_PACKAGE_ID_LOCATION",
                                  TAX_RATE_NAME: "TEST_PACKAGE_TAX_RATE",
                                  EXTERNAL_CODE_NAME: "TEST_PACKAGE_EXTERNAL_CODE",
                                  AVAILABLE_FOR_SALE_NAME: "TEST_PACKAGE_AVAILABLE_FOR_SALE"}
    ENTITY_NAME = 'packages'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_SOCIAL_EVENT_NAME = "Test event"
    TEST_SOCIAL_EVENT_DESCRIPTION = "Test description"
    TEST_SOCIAL_EVENT_INITIAL_DATE = "19900101010101"
    TEST_SOCIAL_EVENT_FINAL_DATE = "20100101010101"
    TEST_SOCIAL_EVENT_CAPACITY = 50

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_RESERVATION_COMPANY = "Test Company"
    TEST_RESERVATION_NUMBER_PERSONS = 10

    TEST_ACCESS_AMOUNT_INCLUDED = 0

    def setUp(self):
        super(PaqueteViewTestCase, self).setUp()

        create_test_client(self)
        create_test_location(self)

        self.TEST_ACCESS_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]

        self.TEST_RESERVATION_PAYMENT = 100.5 * self.TEST_RESERVATION_NUMBER_PERSONS
        self.TEST_RESERVATION_INITIAL_DATE = u"19960101010101"
        self.TEST_RESERVATION_FINAL_DATE = u"19960106010101"

        self.assign_field_value(self.LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME])

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.PACKAGE_NAME_NAME] = u"Test package"
        values[cls.BASE_PRICE_NAME] = 100.5
        values[cls.DESCRIPTION_NAME] = u"Description test"
        values[cls.RESTRICTED_CONSUMPTION_NAME] = True
        values[cls.VALID_FROM_NAME] = u"19900101010101"
        values[cls.VALID_THROUGH_NAME] = u"20100101010101"
        values[cls.TAX_RATE_NAME] = 12.5
        values[cls.EXTERNAL_CODE_NAME] = None
        values[cls.AVAILABLE_FOR_SALE_NAME] = None
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.PACKAGE_NAME_NAME] = u"Test edited package"
        values[cls.BASE_PRICE_NAME] = 200.5
        values[cls.DESCRIPTION_NAME] = u"Description edited test"
        values[cls.RESTRICTED_CONSUMPTION_NAME] = False
        values[cls.VALID_FROM_NAME] = u"19950101010101"
        values[cls.VALID_THROUGH_NAME] = u"20050101010101"
        values[cls.TAX_RATE_NAME] = 16.5
        values[cls.EXTERNAL_CODE_NAME] = None
        values[cls.AVAILABLE_FOR_SALE_NAME] = None
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        available_for_sale = request_values.get(cls.AVAILABLE_FOR_SALE_NAME)
        if available_for_sale is None:
            request_values[cls.AVAILABLE_FOR_SALE_NAME] = True
        from commons.validations import DEFAULT_DATETIME_FORMAT
        initial_date = request_values.get(cls.VALID_FROM_NAME)
        if initial_date is not None:
            request_values[cls.VALID_FROM_NAME] = validate_datetime(initial_date, cls.VALID_FROM_NAME).strftime(DEFAULT_DATETIME_FORMAT)

        final_date = request_values.get(cls.VALID_THROUGH_NAME)
        if final_date is not None:
            request_values[cls.VALID_THROUGH_NAME] = validate_datetime(final_date, cls.VALID_THROUGH_NAME).strftime(DEFAULT_DATETIME_FORMAT)
        return request_values

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME] + 1), expected_code=404)
        validate_error(self, results, CLIENT_DOES_NOT_EXISTS_CODE)

    def test_try_query_non_existent_package(self):
        results = self.do_get_request(self.get_item_url(type(self))
                                      .format(1), expected_code=404)
        validate_error(self, results, PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_empty_packages_view(self):
        self.request_all_resources_and_check_result(0)
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_valid_packages(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_with_zero_price(self):
        self.assign_field_value(self.BASE_PRICE_NAME, 0)
        self.do_create_requests()

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_with_zero_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 0)
        self.do_create_requests()

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_with_external_code(self):
        self.assign_field_value(self.EXTERNAL_CODE_NAME, u"Código 123")
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_with_true_available_for_sale(self):
        self.assign_field_value(self.AVAILABLE_FOR_SALE_NAME, True)
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_with_false_available_for_sale(self):
        self.assign_field_value(self.AVAILABLE_FOR_SALE_NAME, False)
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_with_long_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, u"Test description" * 500)
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_and_get_with_base_time_included(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/packages/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BASE_TIME_NAME,
                                              "20000101010101"))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_and_get_with_base_time_equals_to_valid_from(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/packages/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BASE_TIME_NAME,
                                              "19900101010101"))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_and_get_with_base_time_equals_to_valid_through(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/packages/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BASE_TIME_NAME,
                                              "20100101010101"))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_create_valid_packages_and_get_with_base_time_smaller_than_initial_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/packages/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BASE_TIME_NAME,
                                              "19800101010101"))
        self.clean_test_data()
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_get_packages_with_invalid_base_time(self):
        results = self.do_get_request("/clients/{0}/packages/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BASE_TIME_NAME,
                                              "INVALID_TIME"),
                                      expected_code=400)
        self.validate_error(results, PACKAGE_INVALID_BASE_TIME_CODE)

    def test_create_valid_packages_and_get_with_base_time_bigger_than_final_date(self):
        self.do_create_requests()
        results = self.do_get_request("/clients/{0}/packages/?{1}={2}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.BASE_TIME_NAME,
                                              "30000101010101"))
        self.clean_test_data()
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_non_existent_location(self):
        self.assign_field_value(self.LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 1)
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_invalid_location_id(self):
        self.assign_field_value(self.LOCATION_ID_NAME, "INVALID_LOCATION")
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_without_name(self):
        self.assign_field_value(self.PACKAGE_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_NAME_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_empty_name(self):
        self.assign_field_value(self.PACKAGE_NAME_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_NAME_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_without_price(self):
        self.assign_field_value(self.BASE_PRICE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_PRICE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_invalid_price(self):
        self.assign_field_value(self.BASE_PRICE_NAME, -1)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_PRICE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_create_invalid_packages_without_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_create_invalid_packages_with_invalid_negative_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, -12.0)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_create_invalid_packages_with_invalid_one_hundred_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 100.0)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_create_invalid_packages_with_invalid_over_one_hundred_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 112.0)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_try_create_invalid_packages_with_empty_external_code(self):
        self.assign_field_value(self.EXTERNAL_CODE_NAME, u"  ")
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_EXTERNAL_CODE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_invalid_available_for_sale(self):
        self.assign_field_value(self.AVAILABLE_FOR_SALE_NAME, "true")
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_AVAILABLE_FOR_SALE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_without_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_DESCRIPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_empty_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_DESCRIPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_without_restricted_consumption(self):
        self.assign_field_value(self.RESTRICTED_CONSUMPTION_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_RESTRICTED_CONSUMPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_without_valid_from(self):
        self.assign_field_value(self.VALID_FROM_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_VALID_FROM_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_invalid_valid_from(self):
        self.assign_field_value(self.VALID_FROM_NAME, "INVALID_DATE")
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_VALID_FROM_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_without_valid_through(self):
        self.assign_field_value(self.VALID_THROUGH_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_VALID_THROUGH_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_invalid_valid_through(self):
        self.assign_field_value(self.VALID_THROUGH_NAME, "INVALID_DATE")
        self.do_create_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_VALID_THROUGH_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_packages_with_valid_through_smaller_than_valid_from(self):
        self.assign_field_value(self.VALID_FROM_NAME, "20000101010101")
        self.assign_field_value(self.VALID_THROUGH_NAME, "19900101010101")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_RANGE_OF_DATES_FROM_GREATER_THAN_THROUGH_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_edit_valid_packages_without_reservations(self):
        self.do_create_requests()

        self.do_update_requests()

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_update_packages_with_valid_zero_price(self):
        self.do_create_requests()

        self.assign_field_value(self.BASE_PRICE_NAME, 0)
        self.do_update_requests()

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_update_packages_with_valid_zero_tax_rate(self):
        self.do_create_requests()

        self.assign_field_value(self.TAX_RATE_NAME, 0)
        self.do_update_requests()

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_valid_packages_without_reservations_with_external_code(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_CODE_NAME, u"Código 456")

        self.do_update_requests()

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_valid_packages_without_reservations_with_true_available_for_sale(self):
        self.do_create_requests()
        self.assign_field_value(self.AVAILABLE_FOR_SALE_NAME, True)
        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_valid_packages_without_reservations_false_available_for_sale(self):
        self.do_create_requests()
        self.assign_field_value(self.AVAILABLE_FOR_SALE_NAME, False)
        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_non_existent_packages(self):
        self.do_create_requests()

        self.change_ids_to_non_existent_entities()

        self.do_update_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_without_name(self):
        self.do_create_requests()

        self.assign_field_value(self.PACKAGE_NAME_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_NAME_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_name(self):
        self.do_create_requests()

        self.assign_field_value(self.PACKAGE_NAME_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_NAME_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_without_price(self):
        self.do_create_requests()

        self.assign_field_value(self.BASE_PRICE_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_PRICE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_price(self):
        self.do_create_requests()

        self.assign_field_value(self.BASE_PRICE_NAME, -1)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_PRICE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_try_update_packages_without_tax_rate(self):
        self.do_create_requests()

        self.assign_field_value(self.TAX_RATE_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_try_update_packages_with_invalid_negative_tax_rate(self):
        self.do_create_requests()

        self.assign_field_value(self.TAX_RATE_NAME, -12.0)
        self.do_update_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_try_update_packages_with_invalid_one_hundred_tax_rate(self):
        self.do_create_requests()

        self.assign_field_value(self.TAX_RATE_NAME, 100.0)
        self.do_update_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_try_update_packages_with_invalid_over_one_hundred_tax_rate(self):
        self.do_create_requests()

        self.assign_field_value(self.TAX_RATE_NAME, 112.0)
        self.do_update_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_TAX_RATE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_without_description(self):
        self.do_create_requests()

        self.assign_field_value(self.DESCRIPTION_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_DESCRIPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_description(self):
        self.do_create_requests()

        self.assign_field_value(self.DESCRIPTION_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_DESCRIPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_empty_external_code(self):
        self.do_create_requests()

        self.assign_field_value(self.EXTERNAL_CODE_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_EXTERNAL_CODE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_available_for_sale(self):
        self.do_create_requests()

        self.assign_field_value(self.AVAILABLE_FOR_SALE_NAME, "FALSE")
        self.do_update_requests(expected_code=400, expected_internal_code=PACKAGE_INVALID_AVAILABLE_FOR_SALE_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_without_restricted_consumption(self):
        self.do_create_requests()

        self.assign_field_value(self.RESTRICTED_CONSUMPTION_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_RESTRICTED_CONSUMPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_restricted_consumption(self):
        self.do_create_requests()

        self.assign_field_value(self.RESTRICTED_CONSUMPTION_NAME, u"INVALID")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_RESTRICTED_CONSUMPTION_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_without_valid_from(self):
        self.do_create_requests()

        self.assign_field_value(self.VALID_FROM_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_VALID_FROM_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_valid_from(self):
        self.do_create_requests()

        self.assign_field_value(self.VALID_FROM_NAME, u"INVALID DATE")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_VALID_FROM_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_without_valid_through(self):
        self.do_create_requests()

        self.assign_field_value(self.VALID_THROUGH_NAME, None)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_VALID_THROUGH_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_valid_through(self):
        self.do_create_requests()

        self.assign_field_value(self.VALID_THROUGH_NAME, u"INVALID DATE")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_VALID_THROUGH_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_valid_from_greater_than_valid_through(self):
        self.do_create_requests()

        self.assign_field_value(self.VALID_FROM_NAME, u"20050101010101")
        self.assign_field_value(self.VALID_THROUGH_NAME, u"19950101010101")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=PACKAGE_INVALID_RANGE_OF_DATES_FROM_GREATER_THAN_THROUGH_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_invalid_packages_without_reservations_with_invalid_id_location(self):
        self.do_create_requests()

        self.assign_field_value(self.LOCATION_ID_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 1)
        self.do_update_requests(expected_code=404,
                                expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES)

    def test_edit_valid_packages_with_reservations(self):
        self.do_create_requests()
        self.create_test_reservations_on_packages()

        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.assertEqual(self.NUMBER_OF_ENTITIES * 2, len(results))

    def test_edit_valid_packages_with_reservations_multiple_times_and_check_its_only_duplicated_once(self):
        self.do_create_requests()
        self.create_test_reservations_on_packages()

        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.assertEqual(self.NUMBER_OF_ENTITIES * 2, len(results))

        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.assertEqual(self.NUMBER_OF_ENTITIES * 2, len(results))

    def test_edit_valid_packages_multiple_times_and_check_its_duplicated_again_with_new_reservation(self):
        self.do_create_requests()
        self.create_test_reservations_on_packages()

        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.assertEqual(self.NUMBER_OF_ENTITIES * 2, len(results))

        self.create_test_reservations_on_packages()
        self.do_update_requests()
        results = self.do_get_request("/clients/{0}/all-packages/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.assertEqual(self.NUMBER_OF_ENTITIES * 3, len(results))

    def create_test_reservations_on_packages(self):
        from tests.testsCJM.testReservas.testReservaViewTestCase import create_test_reservation
        from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation
        original_id = self.expected_ids[self.ENTITY_NAME]
        for package_id in self.get_data_values(self.ENTITY_NAME, Paquete.ID_NAME):
            self.expected_ids[self.ENTITY_NAME] = package_id
            create_test_reservation(self, create_new_reservation=True)
            create_test_person_reservation(self, create_new_person_reservation=True)
        self.expected_ids[self.ENTITY_NAME] = original_id

    def test_check_permissions_for_create_packages(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_packages(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_package(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_all_historic_packages(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = "/clients/{0}/all-packages/".format(self.expected_ids[CLIENT_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_packages(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_update_permissions(allowed_roles, required_locations)


PACKAGE_ENTITY_NAME = PaqueteViewTestCase.ENTITY_NAME


def create_test_package(test_class, create_new_package=False):
    return PaqueteViewTestCase.create_sample_entity_for_another_class(test_class, create_new_package)

if __name__ == '__main__':
    unittest.main()
