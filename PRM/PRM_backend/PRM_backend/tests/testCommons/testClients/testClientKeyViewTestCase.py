# -*- coding: utf-8 -*
import string
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client


class ClientKeyViewTestCase(FlaskClientBaseTestCase):
    TAG_KEY_NAME = u"tag-key"

    RESOURCE_URL = u"/clients/{0}/secret-keys/"

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    ENTITY_NAME = "client-key"

    KEY_BYTES_LENGTH = 128

    def setUp(self):
        super(ClientKeyViewTestCase, self).setUp()
        create_test_client(self)

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def get_and_check_key_response(self, expected_code=200, expected_internal_code=None):
        results = self.do_get_request(self.get_base_url(type(self)), expected_code=expected_code)
        if expected_code == 200:
            self.assertTrue(isinstance(results, list))
            self.assertEqual(1, len(results))
            key = results[0][self.TAG_KEY_NAME]
            self.assertTrue(len(key) == self.KEY_BYTES_LENGTH * 2)
            self.assertTrue(all(c in string.hexdigits for c in key))
            return key
        else:
            self.validate_error(results, expected_internal_code)

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.get_and_check_key_response(expected_code=404,
                                        expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_key_exists_and_is_idempotent(self):
        first_key = self.get_and_check_key_response()
        second_key = self.get_and_check_key_response()
        self.assertEqual(first_key, second_key)

    def test_key_is_different_for_each_client(self):
        first_key = self.get_and_check_key_response()
        create_test_client(self, create_new_client=True)
        second_key = self.get_and_check_key_response()
        self.assertNotEqual(first_key, second_key)

    def test_check_permissions_for_get_key(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)


CLIENT_KEY_ENTITY_NAME = ClientKeyViewTestCase.ENTITY_NAME

if __name__ == '__main__':
    unittest.main()
