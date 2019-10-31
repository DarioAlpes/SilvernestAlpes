# -*- coding: utf-8 -*
import unittest

from commons.entidades.users.Usuario import Usuario
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    LOCATION_DOES_NOT_EXISTS_CODE, USER_INVALID_USERNAME_CODE, USER_INVALID_PASSWORD_CODE, USER_INVALID_ROLE_CODE, \
    USER_ALREADY_EXISTS_CODE, USER_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import LOCATION_ENTITY_NAME, create_test_location

DEFAULT_GLOBAL_ADMIN_USER = "admin"
DEFAULT_GLOBAL_ADMIN_PASSWORD = "password"

DEFAULT_CLIENT_ADMIN_USER = "admin"
DEFAULT_CLIENT_ADMIN_PASSWORD = "password"

CLIENT_ADMIN_ROLE = u"admin"
CLIENT_QUERY_ROLE = u"query"
CLIENT_SALES_ROLE = u"sales"
CLIENT_CASHIER_USER = u"cashier"
CLIENT_WAITER_USER = u"waiter"
CLIENT_ACCESS_USER = u"access"
CLIENT_PROMOTER_USER = u"promoter"
CLIENT_CASHIER_WAITER_USER = u"cashier-waiter"
GLOBAL_ADMIN_ROLE = u"global-admin"

CLIENT_ROLES = [CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER, CLIENT_CASHIER_WAITER_USER]


class UsuarioViewTestCase(FlaskClientBaseTestCase):
    CHECK_BY_GET = False
    ROLE_NAME = u"role"
    USERNAME_NAME = u"username"
    PASSWORD_NAME = u"password"
    IDS_LOCATIONS_NAME = u"ids-locations"

    ID_NAME = USERNAME_NAME

    ENTITY_DOES_NOT_EXISTS_CODE = USER_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/users/"

    ATTRIBUTES_NAMES_BY_FIELDS = {USERNAME_NAME: "TEST_USER_USERNAME",
                                  PASSWORD_NAME: "TEST_USER_PASSWORD",
                                  IDS_LOCATIONS_NAME: "TEST_USER_IDS_LOCATIONS",
                                  ROLE_NAME: "TEST_USER_ROLE"}
    PATCH_FIELDS = {PASSWORD_NAME}

    STARTING_ID = 1
    NUMBER_USERS = 5
    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True
    ENTITY_NAME = 'usuarios'

    TEST_LOCATION_TYPE = u"CITY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    IDS_LOCATIONS_NAME = u"ids-locations"

    def setUp(self):
        super(UsuarioViewTestCase, self).setUp()
        create_test_client(self)
        self.username_template = "Username{0}"
        self.password_template = "Password{0}"
        self.role = CLIENT_ADMIN_ROLE
        self.ids_locations = None
        logout(self, DEFAULT_GLOBAL_ADMIN_USER, use_client_url=False, expected_code=200)
        login(self, DEFAULT_CLIENT_ADMIN_USER, DEFAULT_CLIENT_ADMIN_PASSWORD,
              use_client_url=True, expected_code=200)
        self.add_data_value(USER_ENTITY_NAME, Usuario.USERNAME_NAME, DEFAULT_CLIENT_ADMIN_USER)
        self.add_data_value(USER_ENTITY_NAME, self.ROLE_NAME, CLIENT_ADMIN_ROLE)
        self.add_data_value(USER_ENTITY_NAME, self.IDS_LOCATIONS_NAME, None)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.ROLE_NAME] = CLIENT_ADMIN_ROLE
        return values

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.USERNAME_NAME] = "Username{0}"
        templates[cls.PASSWORD_NAME] = "Password1{0}"
        return templates

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.ROLE_NAME] = CLIENT_ADMIN_ROLE
        return values

    @classmethod
    def get_entity_values_templates_for_update(cls):
        templates = dict()
        templates[cls.USERNAME_NAME] = "New username{0}"
        templates[cls.PASSWORD_NAME] = "New password1{0}"
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        request_values[cls.PASSWORD_NAME] = None
        return request_values

    def test_global_admin_user_created(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)

    def test_invalid_login_as_global_admin_user(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, "INVALID_PASSWORD", use_client_url=False,
              expected_code=403)
        login(self, "INVALID_USER", DEFAULT_GLOBAL_ADMIN_PASSWORD, use_client_url=False,
              expected_code=403)

    def test_global_admin_user_logout(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        logout(self, DEFAULT_GLOBAL_ADMIN_USER, use_client_url=False, expected_code=200)

    def test_change_global_admin_password(self):
        new_password = u"new_password1"
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                              data={self.PASSWORD_NAME: new_password})
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=403)
        login(self, DEFAULT_GLOBAL_ADMIN_USER, new_password,
              use_client_url=False, expected_code=200)

    def test_try_change_global_password_without_password(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        results = self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                                        data={self.PASSWORD_NAME: None}, expected_code=400)
        validate_error(self, results, USER_INVALID_PASSWORD_CODE)

    def test_try_change_global_password_with_empty_password(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        results = self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                                        data={self.PASSWORD_NAME: ""}, expected_code=400)
        validate_error(self, results, USER_INVALID_PASSWORD_CODE)

    def test_try_change_global_password_with_short_password(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        results = self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                                        data={self.PASSWORD_NAME: "$hort1"}, expected_code=400)
        validate_error(self, results, USER_INVALID_PASSWORD_CODE)

    def test_try_change_global_password_with_password_starting_with_username(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        results = self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                                        data={self.PASSWORD_NAME: DEFAULT_GLOBAL_ADMIN_USER + "$hort1"},
                                        expected_code=400)
        validate_error(self, results, USER_INVALID_PASSWORD_CODE)

    def test_try_change_global_password_without_letter_password(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        results = self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                                        data={self.PASSWORD_NAME: "$1234!·!$\"123"}, expected_code=400)
        validate_error(self, results, USER_INVALID_PASSWORD_CODE)

    def test_try_change_global_password_without_number_password(self):
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        results = self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                                        data={self.PASSWORD_NAME: "my_test_pa$$word"}, expected_code=400)
        validate_error(self, results, USER_INVALID_PASSWORD_CODE)

    def test_attempt_to_change_global_admin_password_without_login(self):
        new_password = u"new_password1"
        self.do_patch_request("/users/{0}/".format(DEFAULT_GLOBAL_ADMIN_USER),
                              data={self.PASSWORD_NAME: new_password}, expected_code=403)

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                                    number_of_default_entities=1)

    def test_empty_users_view(self):
        self.request_all_resources_and_check_result(0, number_of_default_entities=1)

    def test_login_and_logout_as_default_admin_user(self):
        login(self, DEFAULT_CLIENT_ADMIN_USER, DEFAULT_CLIENT_ADMIN_PASSWORD)
        logout(self, DEFAULT_CLIENT_ADMIN_USER)
        logout(self, DEFAULT_CLIENT_ADMIN_USER, expected_code=401)

    def test_create_users(self):
        self.do_create_requests(number_of_default_entities=1)

    def test_create_users_with_each_role(self):
        num_users = 0
        for role in CLIENT_ROLES:
            self.assign_field_value(self.ROLE_NAME, role)
            self.do_create_requests(number_of_default_entities=1, previously_created_entities=num_users)
            num_users += self.NUMBER_OF_ENTITIES

    def test_create_users_with_locations(self):
        create_test_location(self)
        self.assign_field_value(self.IDS_LOCATIONS_NAME, [self.expected_ids[LOCATION_ENTITY_NAME]])
        self.do_create_requests(number_of_default_entities=1)

    def test_try_create_users_without_name(self):
        self.assign_field_value(self.USERNAME_NAME, None)
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_USERNAME_CODE)

    def test_try_create_users_with_empty_name(self):
        self.assign_field_value(self.USERNAME_NAME, "")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_USERNAME_CODE)

    def test_try_create_users_with_money_symbol_in_name(self):
        self.assign_field_value(self.USERNAME_NAME, "TEST$USER")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_USERNAME_CODE)

    def test_try_create_users_with_duplicated_name(self):
        self.do_create_requests(number_of_default_entities=1)
        self.clean_test_data()
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_ALREADY_EXISTS_CODE,
                                check_results_as_list=False,
                                do_get_and_check_results=False)

    def test_try_create_users_without_password(self):
        self.assign_field_value(self.PASSWORD_NAME, None)
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_create_users_with_empty_password(self):
        self.assign_field_value(self.PASSWORD_NAME, "")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_create_users_with_short_password(self):
        self.assign_field_value(self.PASSWORD_NAME, "$hort1")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_create_users_without_letter_password(self):
        self.assign_field_value(self.PASSWORD_NAME, "$1234!·!$\"123")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_create_users_without_number_password(self):
        self.assign_field_value(self.PASSWORD_NAME, "my_test_pa$$word")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_create_users_with_password_starting_with_username(self):
        self.assign_field_value(self.USERNAME_NAME, "test_user")
        self.assign_field_value(self.PASSWORD_NAME, "test_user_$hort1")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_create_users_without_role(self):
        self.assign_field_value(self.ROLE_NAME, None)
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_ROLE_CODE)

    def test_try_create_users_with_invalid_roles(self):
        self.assign_field_value(self.ROLE_NAME, "INVALID_ROLE")
        self.do_create_requests(number_of_default_entities=1, expected_code=400,
                                expected_internal_code=USER_INVALID_ROLE_CODE)

    def test_try_create_users_with_empty_locations_list(self):
        self.assign_field_value(self.IDS_LOCATIONS_NAME, [])
        self.do_create_requests(number_of_default_entities=1, expected_code=404,
                                expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_users_with_non_existent_location(self):
        self.assign_field_value(self.IDS_LOCATIONS_NAME, [100])
        self.do_create_requests(number_of_default_entities=1, expected_code=404,
                                expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_change_password(self):
        self.do_create_requests(number_of_default_entities=1)
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False)

    def test_try_change_password_for_non_existent_users(self):
        self.do_create_requests(number_of_default_entities=1)
        for user in self.original_entities:
            user[self.USERNAME_NAME] += "_invalid"
        self.do_patch_requests(number_of_default_entities=1,
                               expected_code=404,
                               expected_internal_code=USER_DOES_NOT_EXISTS_CODE)

    def test_change_own_password_as_each_role(self):
        password = "test_password1"
        for role in CLIENT_ROLES:
            self.assign_field_value(self.ROLE_NAME, role)
            self.assign_field_value(self.PASSWORD_NAME, password)
            login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD)
            self.do_create_requests(number_of_default_entities=1)
            login(self, self.original_entities[0][self.USERNAME_NAME], password)
            self.do_patch_requests(number_of_default_entities=1,
                                   check_results_as_list=False,
                                   do_get_and_check_results=False)
            login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD)
            self.do_delete_requests(number_of_default_entities=1)

    def test_try_change_password_without_password(self):
        self.do_create_requests(number_of_default_entities=1)
        self.assign_field_value(self.PASSWORD_NAME, None)
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False,
                               expected_code=400,
                               expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_change_password_with_empty_password(self):
        self.do_create_requests(number_of_default_entities=1)
        self.assign_field_value(self.PASSWORD_NAME, "")
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False,
                               expected_code=400,
                               expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_change_password_with_short_password(self):
        self.do_create_requests(number_of_default_entities=1)
        self.assign_field_value(self.PASSWORD_NAME, "$hort1")
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False,
                               expected_code=400,
                               expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_change_password_without_letter_password(self):
        self.do_create_requests(number_of_default_entities=1)
        self.assign_field_value(self.PASSWORD_NAME, "$1234!·!$\"123")
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False,
                               expected_code=400,
                               expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_change_password_without_number_password(self):
        self.do_create_requests(number_of_default_entities=1)
        self.assign_field_value(self.PASSWORD_NAME, "my_test_pa$$word")
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False,
                               expected_code=400,
                               expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_try_change_password_with_password_starting_with_username(self):
        self.assign_field_value(self.USERNAME_NAME, "test_user")
        self.do_create_requests(number_of_default_entities=1)
        self.assign_field_value(self.PASSWORD_NAME, "test_user_$hort1")
        self.do_patch_requests(number_of_default_entities=1, check_results_as_list=False,
                               expected_code=400,
                               expected_internal_code=USER_INVALID_PASSWORD_CODE)

    def test_delete_users(self):
        self.do_create_requests(number_of_default_entities=1)
        self.do_delete_requests(number_of_default_entities=1)

    def test_try_delete_non_existent_users(self):
        self.do_create_requests(number_of_default_entities=1)
        for user in self.original_entities:
            user[self.USERNAME_NAME] += "_invalid"
        self.do_delete_requests(number_of_default_entities=1,
                                expected_code=404,
                                expected_internal_code=USER_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_users(self):
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_users(self):
        self.do_create_requests(number_of_default_entities=1)
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_patch_users_password(self):
        self.do_create_requests(number_of_default_entities=1)
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_patch_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_delete_users(self):
        self.do_create_requests(number_of_default_entities=1)
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)


USER_ENTITY_NAME = UsuarioViewTestCase.ENTITY_NAME


def login(test_class, username, password, use_client_url=True, expected_code=200):
    if use_client_url:
        url = "/clients/{0}/users/{1}/login/".format(
            test_class.expected_ids[CLIENT_ENTITY_NAME],
            username)
    else:
        url = "/users/{0}/login/".format(username)

    result = test_class.do_post_request(url,
                                        data={Usuario.PASSWORD_NAME: password},
                                        expected_code=expected_code)
    return result


def logout(test_class, username, use_client_url=True, expected_code=200):
    if use_client_url:
        url = "/clients/{0}/users/{1}/logout/".format(
            test_class.expected_ids[CLIENT_ENTITY_NAME],
            username)
    else:
        url = "/users/{0}/logout/".format(username)

    result = test_class.do_get_request(url,
                                       expected_code=expected_code)
    return result


def create_test_user(test_class, create_new_user=False):
    return UsuarioViewTestCase.create_sample_entity_for_another_class(test_class, create_new_user)

if __name__ == '__main__':
    unittest.main()
