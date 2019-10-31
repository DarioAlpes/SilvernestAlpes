# -*- coding: utf-8 -*
import unittest

from CJM.entidades.beacons.ClientBeacon import ClientBeacon
from CJM.entidades.lecturas.LogLectura import LogLectura
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testBeacons.testClientBeaconsViewTestCase import create_test_client_static_beacon, \
    create_test_client_mobile_beacon, CLIENT_BEACON_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testSensores.testClientSensorsViewTestCase import create_test_client_sensor, \
    CLIENT_SENSOR_ENTITY_NAME
from tests.testsCJM.testSensores.testGlobalSensorsViewTestCase import create_test_global_sensor


class ReadingViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    NUMBER_READINGS = 2
    NUMBER_READINGS_PER_REQUEST = 2
    ENTITY_NAME = 'reading-log'

    MOBILE_TYPE_NAME = u"MOBILE"
    STATIC_TYPE_NAME = u"STATIC"

    TEST_CLIENT_NAME = "Test client"

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_SENSOR_GLOBAL_SENSOR = None

    TEST_BEACON_MAJOR = 1
    TEST_BEACON_MINOR = 1
    TEST_BEACON_GLOBAL_BEACON = None

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

    TEST_GLOBAL_SENSOR_ID = "123456"
    TEST_GLOBAL_SENSOR_TYPE = MOBILE_TYPE_NAME

    TEST_CLIENT_SENSOR_ID = TEST_GLOBAL_SENSOR_ID

    def setUp(self):
        super(ReadingViewTestCase, self).setUp()
        create_test_client(self)
        create_test_location(self)
        create_test_person(self)

        self.TEST_SENSOR_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.TEST_BEACON_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        self.reading_time_template = "20160101010101"

    def test_create_valid_readings_with_mobile_sensor_and_static_beacon(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.MOBILE_TYPE_NAME
        self.TEST_CLIENT_SENSOR_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_static_beacon(self)

        self.create_test_readings()

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), self.NUMBER_READINGS * self.NUMBER_READINGS_PER_REQUEST)

        for task in tasks:
            self.do_post_request(task.url, {}, expected_code=200)

    def test_create_invalid_readings_with_mobile_sensor_and_static_beacon_without_person(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.MOBILE_TYPE_NAME
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_static_beacon(self)
        self.create_test_readings()

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), self.NUMBER_READINGS * self.NUMBER_READINGS_PER_REQUEST)

        for task in tasks:
            self.do_post_request(task.url, {}, expected_code=400)

    def test_create_valid_readings_with_static_sensor_and_mobile_beacon(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.TEST_CLIENT_SENSOR_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_mobile_beacon(self)

        self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                      self.expected_ids[CLIENT_BEACON_ENTITY_NAME]),
                              data={ClientBeacon.PERSON_ID_NAME: self.expected_ids[PERSON_ENTITY_NAME]},
                              expected_code=200)

        self.create_test_readings()

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), self.NUMBER_READINGS * self.NUMBER_READINGS_PER_REQUEST)

        for task in tasks:
            self.do_post_request(task.url, {}, expected_code=200)

    def test_create_invalid_readings_with_static_sensor_and_mobile_beacon_without_location(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_mobile_beacon(self)

        self.do_patch_request("/clients/{0}/mobile-beacons/{1}/"
                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                      self.expected_ids[CLIENT_BEACON_ENTITY_NAME]),
                              data={ClientBeacon.PERSON_ID_NAME: self.expected_ids[PERSON_ENTITY_NAME]},
                              expected_code=200)

        self.create_test_readings()

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), self.NUMBER_READINGS * self.NUMBER_READINGS_PER_REQUEST)

        for task in tasks:
            self.do_post_request(task.url, {}, expected_code=400)

    def test_create_invalid_readings_with_static_sensor_and_mobile_beacon_without_person(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.STATIC_TYPE_NAME
        self.TEST_CLIENT_SENSOR_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_mobile_beacon(self)

        self.create_test_readings()

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), self.NUMBER_READINGS * self.NUMBER_READINGS_PER_REQUEST)

        for task in tasks:
            self.do_post_request(task.url, {}, expected_code=400)

    def test_create_invalid_readings_with_non_existent_sensor(self):
        create_test_client_static_beacon(self)
        self.expected_ids[CLIENT_SENSOR_ENTITY_NAME] = self.TEST_CLIENT_SENSOR_ID
        self.create_test_readings(expected_code=404)

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), 0)

    def test_create_invalid_readings_with_non_existent_beacon(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.MOBILE_TYPE_NAME
        self.TEST_CLIENT_SENSOR_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_static_beacon(self)
        create_test_client_static_beacon(self)
        self.expected_ids[CLIENT_BEACON_ENTITY_NAME] += 1
        self.create_test_readings(expected_code=404)

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), 0)

    def test_create_invalid_readings_with_invalid_date(self):
        self.TEST_GLOBAL_SENSOR_TYPE = self.MOBILE_TYPE_NAME
        self.TEST_CLIENT_SENSOR_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
        create_test_global_sensor(self)
        create_test_client_sensor(self)
        create_test_client_static_beacon(self)
        self.reading_time_template = "INVALID TIME"
        self.create_test_readings(expected_code=400)

        tasks = self.taskqueue_stub.get_filtered_tasks(queue_names=LogLectura.READINGS_QUEUE_NAME)
        self.assertEqual(len(tasks), 0)

    def create_test_readings(self, expected_code=200):
        for reading_request_number in range(0, self.NUMBER_READINGS):
            reading_times = []
            for reading_number in range(0, self.NUMBER_READINGS_PER_REQUEST):
                reading_times.append(self.reading_time_template.format(reading_request_number, reading_number))
            create_reading(self,
                           reading_times,
                           expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        self.reading_time_template = "{0}{1}".format(prefix, self.reading_time_template)


def create_reading(test_class, reading_times, expected_code=200):
    create_test_client(test_class)

    if ReadingViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[ReadingViewTestCase.ENTITY_NAME] = ReadingViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[ReadingViewTestCase.ENTITY_NAME] += 1

    initial_id = test_class.expected_ids[ReadingViewTestCase.ENTITY_NAME]

    test_class.expected_ids[ReadingViewTestCase.ENTITY_NAME] += len(reading_times) - 1

    reading_data = [{LogLectura.BEACON_ID_NAME: test_class.expected_ids[CLIENT_BEACON_ENTITY_NAME],
                     LogLectura.READING_TIME_NAME: reading_time}
                    for reading_time in reading_times]

    result = test_class.do_post_request("/clients/{0}/sensors/{1}/readings/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                test_class.expected_ids[CLIENT_SENSOR_ENTITY_NAME]),
                                        data={LogLectura.READINGS_NAME: reading_data},
                                        expected_code=expected_code)
    if expected_code == 200:
        from commons.validations import DEFAULT_DATETIME_FORMAT
        expected_readings = []
        for reading_time in reading_times:

            expected_readings.append({LogLectura.BEACON_ID_NAME: test_class.expected_ids[CLIENT_BEACON_ENTITY_NAME],
                                      LogLectura.READING_TIME_NAME:
                                          validate_datetime(reading_time,
                                                            LogLectura.READING_TIME_NAME).strftime(
                                              DEFAULT_DATETIME_FORMAT),
                                      LogLectura.ID_NAME: initial_id})
            initial_id += 1

        validate_readings(test_class, result, expected_readings)
    return result


def validate_readings(test_class, result, expected_readings):
    test_class.maxDiff = None
    test_class.assertTrue(isinstance(result, list))
    test_class.assertEqual(result, expected_readings)


if __name__ == '__main__':
    unittest.main()
