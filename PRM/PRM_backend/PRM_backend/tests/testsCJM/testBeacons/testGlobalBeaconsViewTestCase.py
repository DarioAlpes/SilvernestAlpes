# -*- coding: utf-8 -*
import unittest

from CJM.entidades.beacons.BeaconType import BeaconType
from CJM.entidades.beacons.GlobalBeacon import GlobalBeacon
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase


class GlobalBeaconsViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'global-beacons'
    NUMBER_BEACONS = 5

    def setUp(self):
        super(GlobalBeaconsViewTestCase, self).setUp()

    def test_create_valid_static_beacons(self):
        self.create_test_static_beacons()

        results = self.do_get_request("/static-beacons/")
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

        results = self.do_get_request("/beacons/")
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

    def test_create_valid_mobile_beacons(self):
        self.create_test_mobile_beacons()

        results = self.do_get_request("/mobile-beacons/")
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

        results = self.do_get_request("/beacons/")
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

    def test_create_valid_static_and_mobile_beacons(self):
        self.create_test_static_beacons()

        results = self.do_get_request("/beacons/")
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS)

        self.create_test_mobile_beacons()

        results = self.do_get_request("/beacons/")
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_BEACONS * 2)

    def create_test_static_beacons(self, expected_code=200):
        for static_beacon_number in range(0, self.NUMBER_BEACONS):
            create_static_beacon(self,
                                 expected_code=expected_code)

    def create_test_mobile_beacons(self, expected_code=200):
        for static_beacon_number in range(0, self.NUMBER_BEACONS):
            create_mobile_beacon(self,
                                 expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        pass


GLOBAL_BEACON_ENTITY_NAME = GlobalBeaconsViewTestCase.ENTITY_NAME


def create_static_beacon(test_class, expected_code=200):

    if GlobalBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME] = GlobalBeaconsViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/static-beacons/",
                                        data={},
                                        expected_code=expected_code)
    if expected_code == 200:
        test_class.add_data_value(GlobalBeaconsViewTestCase.ENTITY_NAME, GlobalBeacon.TYPE_NAME,
                                  BeaconType.STRING_STATIC)
        test_class.add_data_value(GlobalBeaconsViewTestCase.ENTITY_NAME,
                                  GlobalBeacon.ID_NAME, test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME])
        validate_static_beacon(test_class, result,
                               test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME])
    return result


def validate_static_beacon(test_class, result, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    test_class.assertEqual(result[GlobalBeacon.TYPE_NAME], BeaconType.STRING_STATIC)
    test_class.assertEqual(result[GlobalBeacon.ID_NAME], expected_id)


def create_mobile_beacon(test_class, expected_code=200):

    if GlobalBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME] = GlobalBeaconsViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/mobile-beacons/",
                                        data={},
                                        expected_code=expected_code)
    if expected_code == 200:
        test_class.add_data_value(GlobalBeaconsViewTestCase.ENTITY_NAME, GlobalBeacon.TYPE_NAME,
                                  BeaconType.STRING_MOBILE)
        test_class.add_data_value(GlobalBeaconsViewTestCase.ENTITY_NAME,
                                  GlobalBeacon.ID_NAME, test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME])
        validate_mobile_beacon(test_class, result,
                               test_class.expected_ids[GlobalBeaconsViewTestCase.ENTITY_NAME])
    return result


def validate_mobile_beacon(test_class, result, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    test_class.assertEqual(result[GlobalBeacon.TYPE_NAME], BeaconType.STRING_MOBILE)
    test_class.assertEqual(result[GlobalBeacon.ID_NAME], expected_id)


def create_test_global_static_beacon(test_class, create_new_beacon=False):
    if GlobalBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_beacon:
        return create_static_beacon(test_class)[GlobalBeacon.ID_NAME]


def create_test_global_mobile_beacon(test_class, create_new_beacon=False):
    if GlobalBeaconsViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_beacon:
        return create_mobile_beacon(test_class)[GlobalBeacon.ID_NAME]


if __name__ == '__main__':
    unittest.main()
