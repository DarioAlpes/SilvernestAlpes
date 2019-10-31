# -*- coding: utf-8 -*
import unittest

import time

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, PERSON_DOES_NOT_EXISTS_CODE, \
    CLIENT_SENSOR_LOCATION_ON_MOBILE_CODE, LOCATION_DOES_NOT_EXISTS_CODE, CLIENT_SENSOR_PERSON_ON_STATIC_CODE, \
    CLIENT_SENSOR_ALREADY_EXISTS_CODE, CLIENT_SENSOR_ALREADY_ASSIGNED_CODE, GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE, \
    CLIENT_SENSOR_DOES_NOT_EXISTS_CODE, CLIENT_SENSOR_INVALID_ACTIVE_CODE, CLIENT_SENSOR_INVALID_SYNCED_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testSensores.testGlobalSensorsViewTestCase import create_test_global_sensor


class ClientSensorsViewTestCase(FlaskClientBaseTestCase):
    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True
    ENTITY_NAME = 'client-sensors'
    ID_NAME = u"id"
    ID_LOCATION_NAME = u"id-location"
    ID_PERSON_NAME = u"id-person"
    ACTIVE_NAME = u"active"
    SYNCED_NAME = u"synced"
    LAST_SYNC_NAME = u"last-sync"
    LAST_ACTIVATION_NAME = u"last-activation"
    LAST_DEACTIVATION_NAME = u"last-deactivation"

    MOBILE_TYPE_NAME = u"MOBILE"
    STATIC_TYPE_NAME = u"STATIC"
    PATCH_FIELDS = {ACTIVE_NAME, SYNCED_NAME}

    RESOURCE_URL = u"/clients/{0}/sensors/"
    ENTITY_DOES_NOT_EXISTS_CODE = CLIENT_SENSOR_DOES_NOT_EXISTS_CODE

    ATTRIBUTES_NAMES_BY_FIELDS = {ID_NAME: "TEST_CLIENT_SENSOR_ID",
                                  ID_LOCATION_NAME: "TEST_CLIENT_SENSOR_ID_LOCATION",
                                  ID_PERSON_NAME: "TEST_CLIENT_SENSOR_ID_PERSON",
                                  ACTIVE_NAME: "TEST_CLIENT_SENSOR_ACTIVE"}

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

    TEST_GLOBAL_SENSOR_TYPE = MOBILE_TYPE_NAME
    SENSOR_ID_TEMPLATE = "123456{0}"
    SYNCING = False
    ACTIVATING = False
    DEACTIVATING = False

    def setUp(self):
        super(ClientSensorsViewTestCase, self).setUp()
        create_test_client(self)
        ClientSensorsViewTestCase.SYNCING = False

    def create_global_sensors(self):
        for sensor_number in range(self.NUMBER_OF_ENTITIES):
            self.TEST_GLOBAL_SENSOR_ID = self.SENSOR_ID_TEMPLATE.format(sensor_number)
            create_test_global_sensor(self, create_new_sensor=True)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.ACTIVE_NAME] = None
        return values

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        if is_create:
            request_values[cls.ACTIVE_NAME] = False
            cls.ACTIVATING = False
        else:
            cls.ACTIVATING = request_values.get(cls.ACTIVE_NAME, False)

        if cls.SYNCED_NAME in request_values:
            if not is_create:
                cls.SYNCING = request_values[cls.SYNCED_NAME]
            del request_values[cls.SYNCED_NAME]
        if cls.LAST_SYNC_NAME in request_values:
            del request_values[cls.LAST_SYNC_NAME]
        if cls.LAST_ACTIVATION_NAME in request_values:
            del request_values[cls.LAST_ACTIVATION_NAME]
        if cls.LAST_DEACTIVATION_NAME in request_values:
            del request_values[cls.LAST_DEACTIVATION_NAME]
        return request_values

    @classmethod
    def validate_additional_values(cls, running_entity, result):
        last_sync_date_time = getattr(running_entity, "sensor_last_sync_date_time", None)
        last_activation_date_time = getattr(running_entity, "sensor_last_activation_date_time", None)
        last_deactivation_date_time = getattr(running_entity, "sensor_last_deactivation_date_time", None)

        if cls.SYNCING is True:
            running_entity.assertTrue(result.get(cls.LAST_SYNC_NAME) is not None)

        if result.get(cls.LAST_SYNC_NAME) is not None:
            running_entity.sensor_last_sync_date_time = result[cls.LAST_SYNC_NAME]
            if last_sync_date_time is not None:
                if cls.SYNCING:
                    running_entity.assertTrue(result[cls.LAST_SYNC_NAME] >= last_sync_date_time)
                else:
                    running_entity.assertEquals(result[cls.LAST_SYNC_NAME], last_sync_date_time)

        if cls.ACTIVATING is True:
            running_entity.assertTrue(result.get(cls.LAST_ACTIVATION_NAME) is not None)
        elif cls.ACTIVATING is False:
            running_entity.assertTrue(result.get(cls.LAST_DEACTIVATION_NAME) is not None)

        if result.get(cls.LAST_ACTIVATION_NAME) is not None:
            running_entity.sensor_last_activation_date_time = result[cls.LAST_ACTIVATION_NAME]
            if last_activation_date_time is not None:
                if cls.ACTIVATING:
                    running_entity.assertTrue(result[cls.LAST_ACTIVATION_NAME] >= last_activation_date_time)
                else:
                    running_entity.assertEquals(result[cls.LAST_ACTIVATION_NAME], last_activation_date_time)

        if result.get(cls.LAST_DEACTIVATION_NAME) is not None:
            running_entity.sensor_last_deactivation_date_time = result[cls.LAST_DEACTIVATION_NAME]
            if last_deactivation_date_time is not None:
                if not cls.ACTIVATING:
                    running_entity.assertTrue(result[cls.LAST_DEACTIVATION_NAME] >= last_deactivation_date_time)
                else:
                    running_entity.assertEquals(result[cls.LAST_DEACTIVATION_NAME], last_deactivation_date_time)

    @classmethod
    def get_entity_values_templates_for_create(cls):
        templates = dict()
        templates[cls.ID_NAME] = cls.SENSOR_ID_TEMPLATE
        return templates

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0,
                                                    expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_empty_tags_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_mobile_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()

    def test_create_valid_mobile_sensors_with_person(self):
        create_test_person(self)
        self.assign_field_value(self.ID_PERSON_NAME, self.expected_ids[PERSON_ENTITY_NAME])
        self.create_global_sensors()
        self.do_create_requests()

    def test_create_valid_static_sensors(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.create_global_sensors()
        self.do_create_requests()

    def test_create_valid_static_sensors_with_location(self):
        create_test_location(self)
        self.assign_field_value(self.ID_LOCATION_NAME, self.expected_ids[LOCATION_ENTITY_NAME])
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.create_global_sensors()
        self.do_create_requests()

    def test_create_valid_sensors_with_true_active_status_and_check_it_is_ignored(self):
        self.create_global_sensors()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests()

    def test_create_valid_sensors_with_invalid_active_status_and_check_it_is_ignored(self):
        self.create_global_sensors()
        self.assign_field_value(self.ACTIVE_NAME, "INVALID")
        self.do_create_requests()

    def test_create_valid_sensors_with_true_synced_status_and_check_it_is_ignored(self):
        self.create_global_sensors()
        self.assign_field_value(self.SYNCED_NAME, True)
        self.do_create_requests()

    def test_create_valid_sensors_with_invalid_synced_status_and_check_it_is_ignored(self):
        self.create_global_sensors()
        self.assign_field_value(self.SYNCED_NAME, "INVALID")
        self.do_create_requests()

    def test_try_create_invalid_sensors_without_id(self):
        self.create_global_sensors()
        self.assign_field_value(self.ID_NAME, None)
        self.do_create_requests(expected_code=404, expected_internal_code=GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_sensors_with_non_existent_global_sensors(self):
        self.do_create_requests(expected_code=404, expected_internal_code=GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_try_create_valid_already_created_sensors_on_the_same_client(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_ALREADY_EXISTS_CODE)

    def test_try_create_valid_already_created_sensors_on_different_client(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.clean_test_data()
        create_test_client(self, create_new_client=True)
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_ALREADY_ASSIGNED_CODE)

    def test_try_create_invalid_mobile_sensors_with_non_existent_person(self):
        create_test_person(self)
        self.assign_field_value(self.ID_PERSON_NAME, self.expected_ids[PERSON_ENTITY_NAME] + 2000)
        self.create_global_sensors()
        self.do_create_requests(expected_code=404, expected_internal_code=PERSON_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_mobile_sensors_with_location(self):
        create_test_location(self)
        self.assign_field_value(self.ID_LOCATION_NAME, self.expected_ids[LOCATION_ENTITY_NAME])
        self.create_global_sensors()
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_LOCATION_ON_MOBILE_CODE)

    def test_try_create_invalid_static_sensors_with_non_existent_location(self):
        create_test_location(self)
        self.assign_field_value(self.ID_LOCATION_NAME, self.expected_ids[LOCATION_ENTITY_NAME] + 2000)
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.create_global_sensors()
        self.do_create_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE)

    def test_try_create_invalid_static_sensors_with_person(self):
        create_test_person(self)
        self.assign_field_value(self.ID_PERSON_NAME, self.expected_ids[PERSON_ENTITY_NAME])
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.create_global_sensors()
        self.do_create_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_PERSON_ON_STATIC_CODE)

    def test_activate_and_deactivate_valid_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def test_sync_sensors_waiting_over_one_second(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, True)
        time.sleep(1.1)
        self.do_patch_requests()

    def test_activate_deactivated_sensors_waiting_over_one_second(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, True)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        time.sleep(1.1)
        self.do_patch_requests()

    def test_deactivate_activated_sensors_waiting_over_one_second(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, True)
        self.do_patch_requests()
        time.sleep(1.1)
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests()

    def test_try_activate_valid_sensors_without_active(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.SYNCED_NAME, True)
        self.assign_field_value(self.ACTIVE_NAME, None)
        self.do_patch_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_INVALID_ACTIVE_CODE)

    def test_try_activate_valid_sensors_with_invalid_active(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.SYNCED_NAME, True)
        self.assign_field_value(self.ACTIVE_NAME, "INVALID")
        self.do_patch_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_INVALID_ACTIVE_CODE)

    def test_try_activate_valid_sensors_without_synced(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, None)
        self.do_patch_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_INVALID_SYNCED_CODE)

    def test_try_activate_valid_sensors_with_invalid_synced(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, "INVALID")
        self.do_patch_requests(expected_code=400, expected_internal_code=CLIENT_SENSOR_INVALID_SYNCED_CODE)

    def test_try_activate_non_existent_global_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(expected_code=404,
                               expected_internal_code=GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_try_activate_non_existent_sensors(self):
        self.create_global_sensors()
        self.change_ids_to_non_existent_entities()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(expected_code=404,
                               expected_internal_code=CLIENT_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_delete_valid_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.do_delete_requests()

    def test_delete_valid_sensors_and_create_them_again(self):
        self.NUMBER_OF_ENTITIES = 1
        self.SENSOR_ID_TEMPLATE = "123456"
        self.assign_field_value(self.ID_NAME, "123456")
        self.create_global_sensors()
        self.do_create_requests()
        self.do_delete_requests()
        self.do_create_requests()

    def test_try_delete_non_existent_global_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404,
                                expected_internal_code=GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_try_delete_non_existent_sensors(self):
        self.create_global_sensors()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404,
                                expected_internal_code=CLIENT_SENSOR_DOES_NOT_EXISTS_CODE)

    def test_check_permissions_for_create_sensors(self):
        self.create_global_sensors()
        allowed_roles = {}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations,
                                      do_delete_after_success=True)

    def test_check_permissions_for_get_all_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_sensor_without_location(self):
        self.create_global_sensors()
        self.do_create_requests()
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_sensor_with_location(self):
        create_test_location(self)
        self.assign_field_value(self.ID_LOCATION_NAME, self.expected_ids[LOCATION_ENTITY_NAME])
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.create_global_sensors()
        self.do_create_requests()
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {self.expected_ids[LOCATION_ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_patch_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.assign_field_value(self.SYNCED_NAME, True)
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, \
            CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {}
        self.check_patch_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_delete_sensors(self):
        self.create_global_sensors()
        self.do_create_requests()
        allowed_roles = {}
        required_locations = {}
        self.check_delete_permissions(allowed_roles, required_locations)

CLIENT_SENSOR_ENTITY_NAME = ClientSensorsViewTestCase.ENTITY_NAME


def create_test_client_sensor(test_class, create_new_sensor=False):
    return ClientSensorsViewTestCase.create_sample_entity_for_another_class(test_class, create_new_sensor)

if __name__ == '__main__':
    unittest.main()
