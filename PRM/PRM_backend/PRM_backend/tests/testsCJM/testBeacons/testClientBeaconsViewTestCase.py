# -*- coding: utf-8 -*
import unittest

from CJM.entidades.beacons.BeaconType import BeaconType
from CJM.entidades.beacons.ClientBeacon import ClientBeacon
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testBeacons.testGlobalBeaconsViewTestCase import create_test_global_mobile_beacon,\
    create_test_global_static_beacon, GLOBAL_BEACON_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME


class ClientBeaconsViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    TEST_CLIENT_NAME = "Test client"
    ENTITY_NAME = 'client-beacons'
    NUMBER_BEACONS = 5

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

    def setUp(self):
        super(ClientBeaconsViewTestCase, self).setUp()
        create_test_client(self)
        create_test_location(self)
        create_test_person(self)

        self.major_template = "1{0}"
        self.minor_template = "1{0}"
        self.location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.global_beacon_id = None

    def test_create_valid_static_beacons(self):
        self.create_test_static_beacons()

        results = self.do_get_request("/clients/{0}/beacons/".format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

        results = self.do_get_request("/clients/{0}/static-beacons/".format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

    def test_delete_valid_static_beacons(self):
        self.create_test_static_beacons()
        self.do_delete_request("/clients/{0}/static-beacons/{1}/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[self.ENTITY_NAME]))

    def test_delete_non_existent_static_beacons(self):
        self.create_test_static_beacons()
        self.do_delete_request("/clients/{0}/static-beacons/{1}/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[self.ENTITY_NAME] + 1),
                               expected_code=404)

    def test_assign_valid_static_beacon(self):
        create_test_global_static_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_static_beacons()

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_delete_valid_assigned_static_beacon(self):
        create_test_global_static_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_static_beacons()

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

        self.do_delete_request("/clients/{0}/static-beacons/{1}/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[self.ENTITY_NAME]))

    def test_assign_invalid_static_beacon_to_previously_assigned_beacon(self):
        create_test_global_static_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_static_beacons()
        self.create_test_static_beacons(expected_code=400)

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_assign_invalid_static_beacon_to_mobile_beacon(self):
        create_test_global_mobile_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_static_beacons(expected_code=400)

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_assign_invalid_static_beacon_to_non_existent_global_beacon(self):
        create_test_global_static_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME] + 1

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_static_beacons(expected_code=404)

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_create_invalid_static_beacons_without_major(self):
        self.major_template = None
        self.create_test_static_beacons(400)

    def test_create_invalid_static_beacons_with_invalid_major(self):
        self.major_template = "-1{0}"
        self.create_test_static_beacons(400)

    def test_create_invalid_static_beacons_without_minor(self):
        self.minor_template = None
        self.create_test_static_beacons(400)

    def test_create_invalid_static_beacons_with_invalid_minor(self):
        self.minor_template = "-1{0}"
        self.create_test_static_beacons(400)

    def test_create_invalid_static_beacons_without_location(self):
        self.location_id = ""
        self.create_test_static_beacons(404)

    def test_create_invalid_static_beacons_with_invalid_non_existent_location(self):
        self.location_id += 1
        self.create_test_static_beacons(404)

    def test_create_valid_mobile_beacons(self):
        self.create_test_mobile_beacons()

        results = self.do_get_request("/clients/{0}/beacons/".format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

        results = self.do_get_request("/clients/{0}/mobile-beacons/".format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

    def test_delete_valid_mobile_beacons(self):
        self.create_test_mobile_beacons()
        self.do_delete_request("/clients/{0}/mobile-beacons/{1}/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[self.ENTITY_NAME]))

    def test_delete_non_existent_mobile_beacons(self):
        self.create_test_static_beacons()
        self.do_delete_request("/clients/{0}/mobile-beacons/{1}/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[self.ENTITY_NAME] + 1),
                               expected_code=404)

    def test_delete_valid_assigned_mobile_beacon(self):
        create_test_global_mobile_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_mobile_beacons()

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

        self.do_delete_request("/clients/{0}/mobile-beacons/{1}/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[self.ENTITY_NAME]))

    def test_assign_valid_mobile_beacon(self):
        create_test_global_mobile_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_mobile_beacons()

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_assign_invalid_mobile_beacon_to_previously_assigned_beacon(self):
        create_test_global_mobile_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_mobile_beacons()
        self.create_test_mobile_beacons(expected_code=400)

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_assign_invalid_mobile_beacon_to_static_beacon(self):
        create_test_global_static_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME]

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_mobile_beacons(expected_code=400)

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_assign_invalid_mobile_beacon_to_non_existent_global_beacon(self):
        create_test_global_mobile_beacon(self)
        self.global_beacon_id = self.expected_ids[GLOBAL_BEACON_ENTITY_NAME] + 1

        previous_starting_id = self.STARTING_ID
        self.STARTING_ID = self.global_beacon_id
        previous_number_of_beacons = self.NUMBER_BEACONS
        self.NUMBER_BEACONS = 1

        self.create_test_mobile_beacons(expected_code=404)

        self.NUMBER_BEACONS = previous_number_of_beacons
        self.STARTING_ID = previous_starting_id

    def test_create_invalid_mobile_beacons_without_major(self):
        self.major_template = None
        self.create_test_mobile_beacons(400)

    def test_create_invalid_mobile_beacons_with_invalid_major(self):
        self.major_template = "-1{0}"
        self.create_test_mobile_beacons(400)

    def test_create_invalid_mobile_beacons_without_minor(self):
        self.minor_template = None
        self.create_test_mobile_beacons(400)

    def test_create_invalid_static_mobile_with_invalid_minor(self):
        self.minor_template = "-1{0}"
        self.create_test_mobile_beacons(400)

    def test_register_valid_mobile_beacons(self):
        self.create_test_mobile_beacons()
        result = self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                               self.expected_ids[self.ENTITY_NAME]),
                                       data={ClientBeacon.PERSON_ID_NAME: self.expected_ids[PERSON_ENTITY_NAME]},
                                       expected_code=200)
        minor_major_key = ClientBeacon.major_minor_to_key(self.major, self.minor)
        validate_mobile_beacon(self, result, self.major, self.minor, self.expected_ids[PERSON_ENTITY_NAME],
                               minor_major_key, self.expected_ids[self.ENTITY_NAME])

    def test_register_valid_non_existent_mobile_beacons(self):
        self.create_test_mobile_beacons()
        self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                      self.expected_ids[self.ENTITY_NAME] + 1),
                              data={ClientBeacon.PERSON_ID_NAME: self.expected_ids[PERSON_ENTITY_NAME]},
                              expected_code=404)

    def test_register_invalid_mobile_beacons_without_id_person(self):
        self.create_test_mobile_beacons()
        self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                      self.expected_ids[self.ENTITY_NAME]),
                              data={},
                              expected_code=404)

    def test_register_invalid_mobile_beacons_with_invalid_id_person(self):
        self.create_test_mobile_beacons()
        self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                      self.expected_ids[self.ENTITY_NAME]),
                              data={ClientBeacon.PERSON_ID_NAME: self.expected_ids[PERSON_ENTITY_NAME] + 1},
                              expected_code=404)

    def test_try_register_static_beacons(self):
        self.create_test_static_beacons()
        self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                      self.expected_ids[self.ENTITY_NAME]),
                              data={ClientBeacon.PERSON_ID_NAME: self.expected_ids[PERSON_ENTITY_NAME]},
                              expected_code=400)

    def create_test_static_beacons(self, expected_code=200):
        for static_beacon_number in range(0, self.NUMBER_BEACONS):
            self.major = ""
            if self.major_template is not None:
                self.major = int(self.major_template.format(static_beacon_number))
            self.minor = ""
            if self.minor_template is not None:
                self.minor = int(self.minor_template.format(static_beacon_number))
            create_static_beacon(self,
                                 self.major,
                                 self.minor,
                                 self.location_id,
                                 self.global_beacon_id,
                                 expected_code=expected_code)

    def create_test_mobile_beacons(self, expected_code=200):
        for static_beacon_number in range(0, self.NUMBER_BEACONS):
            self.major = ""
            if self.major_template is not None:
                self.major = int(self.major_template.format(static_beacon_number))
            self.minor = ""
            if self.minor_template is not None:
                self.minor = int(self.minor_template.format(static_beacon_number))
            create_mobile_beacon(self,
                                 self.major,
                                 self.minor,
                                 self.global_beacon_id,
                                 expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        self.name_template = "{0}{1}".format(prefix, self.major_template)
        self.name_template = "{0}{1}".format(prefix, self.minor_template)


def create_static_beacon(test_class, major, minor, id_location, global_beacon_id, expected_code=200):
    create_test_client(test_class)
    create_test_location(test_class)

    if ClientBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME] = ClientBeaconsViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME] += 1

    data = {ClientBeacon.MAJOR_NAME: major,
            ClientBeacon.MINOR_NAME: minor,
            ClientBeacon.UBICACION_ID_NAME: id_location}

    if global_beacon_id is not None:
        data[ClientBeacon.GLOBAL_BEACON_ID_NAME] = global_beacon_id

    result = test_class.do_post_request("/clients/{0}/static-beacons/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME]),
                                        data=data,
                                        expected_code=expected_code)
    if expected_code == 200:
        minor_major_key = ClientBeacon.major_minor_to_key(major, minor)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.MAJOR_NAME, major)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.MINOR_NAME, minor)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.UBICACION_ID_NAME, id_location)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.TYPE_NAME,
                                  BeaconType.STRING_STATIC)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME,
                                  ClientBeacon.ID_NAME, test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME])
        validate_static_beacon(test_class, result, major, minor, id_location, minor_major_key,
                               test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME])
    return result


CLIENT_BEACON_ENTITY_NAME = ClientBeaconsViewTestCase.ENTITY_NAME


def validate_static_beacon(test_class, result, major, minor, id_location, minor_major_key, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    test_class.assertEqual(result[ClientBeacon.MAJOR_NAME], major)
    test_class.assertEqual(result[ClientBeacon.MINOR_NAME], minor)
    test_class.assertEqual(result[ClientBeacon.UBICACION_ID_NAME], id_location)
    test_class.assertIsNone(result[ClientBeacon.PERSON_ID_NAME])
    test_class.assertEqual(result[ClientBeacon.TYPE_NAME], BeaconType.STRING_STATIC)
    test_class.assertEqual(result[ClientBeacon.ID_NAME], expected_id)
    test_class.assertEqual(result[ClientBeacon.KEY_NAME], minor_major_key)


def create_mobile_beacon(test_class, major, minor, global_beacon_id, expected_code=200):
    create_test_client(test_class)
    create_test_location(test_class)

    if ClientBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME] = ClientBeaconsViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME] += 1

    data = {ClientBeacon.MAJOR_NAME: major,
            ClientBeacon.MINOR_NAME: minor}

    if global_beacon_id is not None:
        data[ClientBeacon.GLOBAL_BEACON_ID_NAME] = global_beacon_id

    result = test_class.do_post_request("/clients/{0}/mobile-beacons/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME]),
                                        data=data,
                                        expected_code=expected_code)
    if expected_code == 200:
        minor_major_key = ClientBeacon.major_minor_to_key(major, minor)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.KEY_NAME, minor_major_key)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.MAJOR_NAME, major)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.MINOR_NAME, minor)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME, ClientBeacon.TYPE_NAME,
                                  BeaconType.STRING_MOBILE)
        test_class.add_data_value(ClientBeaconsViewTestCase.ENTITY_NAME,
                                  ClientBeacon.ID_NAME, test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME])
        validate_mobile_beacon(test_class, result, major, minor, None, minor_major_key,
                               test_class.expected_ids[ClientBeaconsViewTestCase.ENTITY_NAME])
    return result


def validate_mobile_beacon(test_class, result, major, minor, id_person, minor_major_key,
                           expected_id):
    test_class.assertTrue(isinstance(result, dict))
    test_class.assertEqual(result[ClientBeacon.MAJOR_NAME], major)
    test_class.assertEqual(result[ClientBeacon.MINOR_NAME], minor)
    test_class.assertEqual(result[ClientBeacon.PERSON_ID_NAME], id_person)
    test_class.assertIsNone(result[ClientBeacon.UBICACION_ID_NAME])
    test_class.assertEqual(result[ClientBeacon.TYPE_NAME], BeaconType.STRING_MOBILE)
    test_class.assertEqual(result[ClientBeacon.ID_NAME], expected_id)
    test_class.assertEqual(result[ClientBeacon.KEY_NAME], minor_major_key)


def create_test_client_static_beacon(test_class, create_new_beacon=False):
    if ClientBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_beacon:
        return create_static_beacon(test_class,
                                    test_class.TEST_BEACON_MAJOR,
                                    test_class.TEST_BEACON_MINOR,
                                    test_class.TEST_BEACON_ID_LOCATION,
                                    test_class.TEST_BEACON_GLOBAL_BEACON)[ClientBeacon.ID_NAME]


def create_test_client_mobile_beacon(test_class, create_new_beacon=False):
    if ClientBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_beacon:
        return create_mobile_beacon(test_class,
                                    test_class.TEST_BEACON_MAJOR,
                                    test_class.TEST_BEACON_MINOR,
                                    test_class.TEST_BEACON_GLOBAL_BEACON)[ClientBeacon.ID_NAME]

if __name__ == '__main__':
    unittest.main()
