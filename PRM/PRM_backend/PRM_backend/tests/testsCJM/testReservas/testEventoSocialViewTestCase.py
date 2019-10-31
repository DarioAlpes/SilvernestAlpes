# -*- coding: utf-8 -*
import unittest

from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    SOCIAL_EVENT_DOES_NOT_EXISTS_CODE, SOCIAL_EVENT_INVALID_NAME_CODE, \
    SOCIAL_EVENT_INVALID_DESCRIPTION_CODE, SOCIAL_EVENT_INVALID_INITIAL_DATE_CODE, SOCIAL_EVENT_INVALID_FINAL_DATE_CODE, \
    SOCIAL_EVENT_INVALID_RANGE_OF_DATES_CODE, SOCIAL_EVENT_INVALID_COMPANY_CODE, \
    SOCIAL_EVENT_INVALID_DOCUMENT_NUMBER_CODE, SOCIAL_EVENT_INVALID_DOCUMENT_TYPE_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client


class EventoSocialViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"
    SOCIAL_EVENT_NAME_NAME = u"name"
    DESCRIPTION_NAME = u"description"
    INITIAL_DATE_NAME = u"initial-date"
    FINAL_DATE_NAME = u"final-date"
    COMPANY_NAME = u"company"
    DOCUMENT_NUMBER_NAME = u"document-number"
    DOCUMENT_TYPE_NAME = u"document-type"

    ENTITY_DOES_NOT_EXISTS_CODE = SOCIAL_EVENT_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/social-events/"

    ATTRIBUTES_NAMES_BY_FIELDS = {SOCIAL_EVENT_NAME_NAME: "TEST_SOCIAL_EVENT_NAME",
                                  COMPANY_NAME: "TEST_SOCIAL_EVENT_COMPANY",
                                  DESCRIPTION_NAME: "TEST_SOCIAL_EVENT_DESCRIPTION",
                                  INITIAL_DATE_NAME: "TEST_SOCIAL_EVENT_INITIAL_DATE",
                                  FINAL_DATE_NAME: "TEST_SOCIAL_EVENT_FINAL_DATE",
                                  DOCUMENT_NUMBER_NAME: "TEST_SOCIAL_EVENT_DOCUMENT_NUMBER",
                                  DOCUMENT_TYPE_NAME: "TEST_SOCIAL_EVENT_DOCUMENT_TYPE"}

    ENTITY_NAME = 'social-events'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    def setUp(self):
        super(EventoSocialViewTestCase, self).setUp()

        create_test_client(self)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.SOCIAL_EVENT_NAME_NAME] = u"Test event"
        values[cls.DESCRIPTION_NAME] = u"Description test"
        values[cls.INITIAL_DATE_NAME] = u"19900101010101"
        values[cls.FINAL_DATE_NAME] = u"20100101010101"
        values[cls.COMPANY_NAME] = u"Empresa prueba"
        values[cls.DOCUMENT_NUMBER_NAME] = u"123456"
        values[cls.DOCUMENT_TYPE_NAME] = u"NIT"
        return values

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        from commons.validations import DEFAULT_DATETIME_FORMAT
        initial_date = request_values.get(cls.INITIAL_DATE_NAME)
        if initial_date is not None:
            request_values[cls.INITIAL_DATE_NAME] = validate_datetime(initial_date, cls.INITIAL_DATE_NAME).strftime(DEFAULT_DATETIME_FORMAT)

        final_date = request_values.get(cls.FINAL_DATE_NAME)
        if final_date is not None:
            request_values[cls.FINAL_DATE_NAME] = validate_datetime(final_date, cls.INITIAL_DATE_NAME).strftime(DEFAULT_DATETIME_FORMAT)

        return request_values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_non_existent_social_event(self):
        results = self.do_get_request(self.get_item_url(type(self))
                                      .format(1), expected_code=404)
        validate_error(self, results, SOCIAL_EVENT_DOES_NOT_EXISTS_CODE)

    def test_empty_social_events_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_social_events(self):
        self.do_create_requests()

    def test_create_valid_social_events_with_long_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, u"Description test" * 500)
        self.do_create_requests()

    def test_create_valid_social_events_without_document(self):
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, None)
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, None)
        self.do_create_requests()

    def test_create_invalid_social_events_without_name(self):
        self.assign_field_value(self.SOCIAL_EVENT_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_NAME_CODE)

    def test_create_invalid_social_events_with_empty_name(self):
        self.assign_field_value(self.SOCIAL_EVENT_NAME_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_NAME_CODE)

    def test_create_invalid_social_events_without_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_DESCRIPTION_CODE)

    def test_create_invalid_social_events_with_empty_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_DESCRIPTION_CODE)

    def test_create_invalid_social_events_without_document_number_but_with_document_type(self):
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_DOCUMENT_NUMBER_CODE)

    def test_create_invalid_social_events_with_invalid_document_number(self):
        self.assign_field_value(self.DOCUMENT_NUMBER_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_DOCUMENT_NUMBER_CODE)

    def test_create_invalid_social_events_with_document_number_but_without_document_type(self):
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_DOCUMENT_TYPE_CODE)

    def test_create_invalid_social_events_with_invalid_document_type(self):
        self.assign_field_value(self.DOCUMENT_TYPE_NAME, "INVALID")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_DOCUMENT_TYPE_CODE)

    def test_try_create_invalid_social_events_without_company(self):
        self.assign_field_value(self.COMPANY_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_COMPANY_CODE)

    def test_try_create_invalid_social_events_with_empty_company(self):
        self.assign_field_value(self.COMPANY_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_COMPANY_CODE)

    def test_create_invalid_social_events_without_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_INITIAL_DATE_CODE)

    def test_create_invalid_social_events_with_invalid_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "INVALID_DATE")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_INITIAL_DATE_CODE)

    def test_create_invalid_social_events_without_final_date(self):
        self.assign_field_value(self.FINAL_DATE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_FINAL_DATE_CODE)

    def test_create_invalid_social_events_with_invalid_final_date(self):
        self.assign_field_value(self.FINAL_DATE_NAME, "INVALID_DATE")
        self.do_create_requests(expected_code=400, expected_internal_code=SOCIAL_EVENT_INVALID_FINAL_DATE_CODE)

    def test_create_invalid_social_events_with_final_date_smaller_than_initial_date(self):
        self.assign_field_value(self.INITIAL_DATE_NAME, "20000101010101")
        self.assign_field_value(self.FINAL_DATE_NAME, "19900101010101")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=SOCIAL_EVENT_INVALID_RANGE_OF_DATES_CODE)

    def test_check_permissions_for_create_social_events(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_social_events(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_social_event(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)


SOCIAL_EVENT_ENTITY_NAME = EventoSocialViewTestCase.ENTITY_NAME


def create_test_social_event(test_class, create_new_event=False):
    return EventoSocialViewTestCase.create_sample_entity_for_another_class(test_class, create_new_event)


if __name__ == '__main__':
    unittest.main()
