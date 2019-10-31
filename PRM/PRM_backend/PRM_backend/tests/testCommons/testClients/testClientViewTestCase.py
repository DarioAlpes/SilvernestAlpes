# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    CLIENT_INVALID_NAME_CODE, CLIENT_INVALID_REQUIRES_LOGIN_CODE, CLIENT_INVALID_EXTERNAL_PERSON_SERVICE_CODE, \
    CLIENT_INVALID_EXTERNAL_RESERVATIONS_SERVICE_CODE


class ClientViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"
    REQUIRES_LOGIN_NAME = u"requires-login"
    CLIENT_NAME_NAME = u"name"
    EXTERNAL_PERSON_SERVICE_NAME = u"external-person-service"
    COMPENSAR_PERSON_SERVICE_NAME = u"COMPENSAR"
    EXTERNAL_RESERVATIONS_SERVICE_NAME = u"external-reservations-service"
    COMPENSAR_RESERVATIONS_SERVICE_NAME = u"COMPENSAR"

    ENTITY_DOES_NOT_EXISTS_CODE = CLIENT_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/"

    ATTRIBUTES_NAMES_BY_FIELDS = {CLIENT_NAME_NAME: "TEST_CLIENT_NAME",
                                  REQUIRES_LOGIN_NAME: "TEST_CLIENT_REQUIRES_LOGIN",
                                  EXTERNAL_PERSON_SERVICE_NAME: "TEST_CLIENT_EXTERNAL_PERSON_SERVICE",
                                  EXTERNAL_RESERVATIONS_SERVICE_NAME: "TEST_CLIENT_EXTERNAL_RESERVATIONS_SERVICE"}

    ENTITY_NAME = 'clientes'

    def setUp(self):
        super(ClientViewTestCase, self).setUp()
        from tests.testCommons.testUsers.testUsuarioViewTestCase import DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD, login
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD, use_client_url=False)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.CLIENT_NAME_NAME] = "Name"
        values[cls.REQUIRES_LOGIN_NAME] = False
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.CLIENT_NAME_NAME] = "New name"
        values[cls.REQUIRES_LOGIN_NAME] = False
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return []

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        if request_values.get(cls.REQUIRES_LOGIN_NAME) is None:
            request_values[cls.REQUIRES_LOGIN_NAME] = False
        return request_values

    def test_warmup_view(self):
        self.do_get_request(u"/_ah/warmup/")

    def test_start_view(self):
        self.do_get_request(u"/_ah/start/")

    def test_stop_view(self):
        self.do_get_request(u"/_ah/stop/")

    def test_empty_client_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_client_without_requires_login(self):
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, None)
        self.do_create_requests()

    def test_create_client_with_requires_login_true(self):
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, True)
        self.do_create_requests()

    def test_create_client_with_requires_login_false(self):
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, False)
        self.do_create_requests()

    def test_create_client_with_external_person_service_with_compensar(self):
        self.assign_field_value(self.EXTERNAL_PERSON_SERVICE_NAME, self.COMPENSAR_PERSON_SERVICE_NAME)
        self.do_create_requests()

    def test_create_client_with_external_reservations_service_with_compensar(self):
        self.assign_field_value(self.EXTERNAL_RESERVATIONS_SERVICE_NAME, self.COMPENSAR_RESERVATIONS_SERVICE_NAME)
        self.do_create_requests()

    def test_create_invalid_client_without_name(self):
        self.assign_field_value(self.CLIENT_NAME_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_NAME_CODE)

    def test_create_invalid_client_with_empty_name(self):
        self.assign_field_value(self.CLIENT_NAME_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_NAME_CODE)

    def test_create_invalid_client_with_invalid_requires_login(self):
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, "invalid")
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_REQUIRES_LOGIN_CODE)

    def test_create_invalid_client_with_invalid_external_person_service(self):
        self.assign_field_value(self.EXTERNAL_PERSON_SERVICE_NAME, "invalid")
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_EXTERNAL_PERSON_SERVICE_CODE)

    def test_create_invalid_client_with_invalid_external_resevations_service(self):
        self.assign_field_value(self.EXTERNAL_RESERVATIONS_SERVICE_NAME, "invalid")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=CLIENT_INVALID_EXTERNAL_RESERVATIONS_SERVICE_CODE)

    def test_update_client_without_requires_login(self):
        self.do_create_requests()
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, None)
        self.do_update_requests()

    def test_update_client_with_requires_login_true(self):
        self.do_create_requests()
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, True)
        self.do_update_requests()

    def test_update_client_with_requires_login_false(self):
        self.do_create_requests()
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, False)
        self.do_update_requests()

    def test_update_client_with_external_person_service_with_compensar(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_PERSON_SERVICE_NAME, self.COMPENSAR_PERSON_SERVICE_NAME)
        self.do_update_requests()

    def test_update_client_with_external_reservations_service_with_compensar(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_RESERVATIONS_SERVICE_NAME, self.COMPENSAR_RESERVATIONS_SERVICE_NAME)
        self.do_update_requests()

    def test_update_invalid_client_without_name(self):
        self.do_create_requests()
        self.assign_field_value(self.CLIENT_NAME_NAME, None)
        self.do_update_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_NAME_CODE)

    def test_update_invalid_client_with_empty_name(self):
        self.do_create_requests()
        self.assign_field_value(self.CLIENT_NAME_NAME, "")
        self.do_update_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_NAME_CODE)

    def test_update_invalid_client_with_invalid_requires_login(self):
        self.do_create_requests()
        self.assign_field_value(self.REQUIRES_LOGIN_NAME, "invalid")
        self.do_update_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_REQUIRES_LOGIN_CODE)

    def test_update_invalid_client_with_invalid_external_person_service(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_PERSON_SERVICE_NAME, "invalid")
        self.do_update_requests(expected_code=400, expected_internal_code=CLIENT_INVALID_EXTERNAL_PERSON_SERVICE_CODE)

    def test_update_invalid_client_with_invalid_external_reservations_service(self):
        self.do_create_requests()
        self.assign_field_value(self.EXTERNAL_RESERVATIONS_SERVICE_NAME, "invalid")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=CLIENT_INVALID_EXTERNAL_RESERVATIONS_SERVICE_CODE)

    def test_update_invalid_non_existent_clients(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()

        self.do_update_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_delete_clients(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_delete_invalid_non_existent_clients(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_clients(self):
        allowed_roles = {}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_clients(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_client(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_clients(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_delete_clients(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)


CLIENT_ENTITY_NAME = ClientViewTestCase.ENTITY_NAME


def create_test_client(test_class, create_new_client=False):
    if ClientViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_client:
        from tests.testCommons.testUsers.testUsuarioViewTestCase import DEFAULT_GLOBAL_ADMIN_USER, \
            DEFAULT_GLOBAL_ADMIN_PASSWORD, login
        login(test_class, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False)
    return ClientViewTestCase.create_sample_entity_for_another_class(test_class, create_new_client)

if __name__ == '__main__':
    unittest.main()
