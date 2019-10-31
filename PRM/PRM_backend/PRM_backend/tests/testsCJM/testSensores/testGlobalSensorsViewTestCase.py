# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import GLOBAL_SENSOR_INVALID_ID_CODE, GLOBAL_SENSOR_INVALID_TYPE_CODE, \
    GLOBAL_SENSOR_ALREADY_EXISTS_CODE, GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client


class GlobalSensorsViewTestCase(FlaskClientBaseTestCase):
    ENTITY_NAME = 'global-sensors'
    TYPE_NAME = u"type"
    ID_NAME = u"id"
    ACTIVE_NAME = u"active"
    ENTITY_DOES_NOT_EXISTS_CODE = GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE

    TEST_CLIENT_REQUIRES_LOGIN = True

    MOBILE_TYPE_NAME = u"MOBILE"
    STATIC_TYPE_NAME = u"STATIC"

    RESOURCE_URL = u"/sensors/"

    ATTRIBUTES_NAMES_BY_FIELDS = {TYPE_NAME: "TEST_GLOBAL_SENSOR_TYPE",
                                  ID_NAME: "TEST_GLOBAL_SENSOR_ID"}

    def setUp(self):
        super(GlobalSensorsViewTestCase, self).setUp()
        create_test_client(self)

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.ID_NAME] = u"1234{0}"
        return templates

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.TYPE_NAME] = cls.MOBILE_TYPE_NAME
        return values

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        if is_create:
            request_values[cls.ACTIVE_NAME] = False
        return request_values

    @classmethod
    def get_ancestor_entities_names(cls):
        return []

    def test_empty_sensors_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_static_sensors(self):
        self.assign_field_value(self.TYPE_NAME, self.STATIC_TYPE_NAME)
        self.do_create_requests()

    def test_create_valid_mobile_sensors(self):
        self.do_create_requests()

    def test_create_valid_sensors_with_true_active_status_and_check_it_is_ignored(self):
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests()

    def test_create_valid_sensors_with_invalid_active_status_and_check_it_is_ignored(self):
        self.assign_field_value(self.ACTIVE_NAME, "INVALID")
        self.do_create_requests()

    def test_try_create_valid_sensors_with_repeated_id(self):
        self.NUMBER_OF_ENTITIES = 1
        self.assign_field_value(self.ID_NAME, "123456")
        self.do_create_requests()
        self.do_create_requests(expected_code=400, expected_internal_code=GLOBAL_SENSOR_ALREADY_EXISTS_CODE)

    def test_try_create_invalid_sensors_without_id(self):
        self.assign_field_value(self.ID_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=GLOBAL_SENSOR_INVALID_ID_CODE)

    def test_try_create_invalid_sensors_with_empty_id(self):
        self.assign_field_value(self.ID_NAME, "")
        self.do_create_requests(expected_code=400, expected_internal_code=GLOBAL_SENSOR_INVALID_ID_CODE)

    def test_try_create_invalid_sensors_without_type(self):
        self.assign_field_value(self.TYPE_NAME, None)
        self.do_create_requests(expected_code=400, expected_internal_code=GLOBAL_SENSOR_INVALID_TYPE_CODE)

    def test_try_create_invalid_sensors_with_invalid_type(self):
        self.assign_field_value(self.TYPE_NAME, "INVALID_TYPE")
        self.do_create_requests(expected_code=400, expected_internal_code=GLOBAL_SENSOR_INVALID_TYPE_CODE)

    def test_delete_valid_sensors(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_delete_valid_sensors_and_create_them_again(self):
        self.NUMBER_OF_ENTITIES = 1
        self.assign_field_value(self.ID_NAME, "123456")
        self.do_create_requests()
        self.do_delete_requests()
        self.do_create_requests()

    def test_try_delete_non_existent_sensors(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404,
                                expected_internal_code=GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_sensors(self):
        allowed_roles = {}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_get_all_sensors(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_sensors(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_sensors(self):
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)

GLOBAL_SENSOR_ENTITY_NAME = GlobalSensorsViewTestCase.ENTITY_NAME


def create_test_global_sensor(test_class, create_new_sensor=False):
    return GlobalSensorsViewTestCase.create_sample_entity_for_another_class(test_class, create_new_sensor)

if __name__ == '__main__':
    unittest.main()
