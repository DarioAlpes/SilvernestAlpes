# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, \
    LOCATION_DOES_NOT_EXISTS_CODE, LOCATION_TAG_DOES_NOT_EXISTS_CODE, LOCATION_INVALID_NAME_CODE, \
    LOCATION_INVALID_TYPE_CODE, LOCATION_INVALID_ACTIVE_CODE, LOCATION_INVALID_DESCRIPTION_CODE, \
    LOCATION_INVALID_WEB_CODE, LOCATION_INVALID_PHONE_CODE, LOCATION_INVALID_ADDRESS_CODE, LOCATION_INVALID_MAIL_CODE, \
    LOCATION_INVALID_SUBTYPE_CODE, LOCATION_INVALID_LATITUDE_CODE, LOCATION_INVALID_LONGITUDE_CODE, \
    LOCATION_INVALID_TYPE_RELATIONSHIP_CODE, LOCATION_INVALID_HIREARCHY_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testLocationTagViewTestCase import create_test_location_tag


class UbicacionViewTestCase(FlaskClientBaseTestCase):
    ID_NAME = u"id"
    TYPE_NAME = u"type"
    ID_PARENT_LOCATION_NAME = u"id-parent-location"
    LOCATION_NAME_NAME = u"name"
    DESCRIPTION_NAME = u"description"
    ACTIVE_NAME = u"active"
    LATITUDE_NAME = u"latitude"
    LONGITUDE_NAME = u"longitude"
    WEB_URL_NAME = u"web"
    PHONE_NAME = u"phone"
    ADDRESS_NAME = u"address"
    MAIL_NAME = u"mail"
    TAGS_NAME = u"tags"
    SUBTYPE_NAME = u"subtype"

    ENTITY_DOES_NOT_EXISTS_CODE = LOCATION_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/locations/"

    ATTRIBUTES_NAMES_BY_FIELDS = {TYPE_NAME: "TEST_LOCATION_TYPE",
                                  ID_PARENT_LOCATION_NAME: "TEST_LOCATION_PARENT_LOCATION_ID",
                                  LOCATION_NAME_NAME: "TEST_LOCATION_NAME",
                                  DESCRIPTION_NAME: "TEST_LOCATION_DESCRIPTION",
                                  ACTIVE_NAME: "TEST_LOCATION_ACTIVE",
                                  LATITUDE_NAME: "TEST_LOCATION_LATITUDE",
                                  LONGITUDE_NAME: "TEST_LOCATION_LONGITUDE",
                                  WEB_URL_NAME: "TEST_LOCATION_WEB_URL",
                                  PHONE_NAME: "TEST_LOCATION_PHONE",
                                  ADDRESS_NAME: "TEST_LOCATION_ADDRESS",
                                  MAIL_NAME: "TEST_LOCATION_MAIL",
                                  TAGS_NAME: "TEST_LOCATION_TAGS",
                                  SUBTYPE_NAME: "TEST_LOCATION_SUBTYPE"}

    PATCH_FIELDS = {ACTIVE_NAME}
    ENTITY_NAME = 'ubicaciones'

    NUMBER_OF_DEFAULT_PLACES = 2
    STARTING_ID = NUMBER_OF_DEFAULT_PLACES + 1
    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_LOCATION_TAG_NAME = u"Test tag"
    NUMBER_LOCATIONS = 5

    COUNTRY_NAME = u"COUNTRY"
    REGION_NAME = u"REGION"
    CITY_NAME = u"CITY"
    POI_NAME = u"POI"
    PROPERTY_NAME = u"PROPERTY"
    ZONE_NAME = u"ZONE"
    AREA_NAME = u"AREA"
    TOUCHPOINT_NAME = u"TOUCHPOINT"

    LOCATION_TYPES = [COUNTRY_NAME, REGION_NAME, CITY_NAME, POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME,
                      TOUCHPOINT_NAME]

    WEB_TYPES = [COUNTRY_NAME, REGION_NAME, CITY_NAME, POI_NAME, PROPERTY_NAME]
    PHONE_TYPES = [POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME]
    ADDRESS_TYPES = [POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME]
    MAIL_TYPES = [POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME]
    TAGS_TYPES = [POI_NAME]
    SUBTYPE_TYPES = [TOUCHPOINT_NAME]

    TYPES_BY_FIELD_NAME = {WEB_URL_NAME: WEB_TYPES,
                           PHONE_NAME: PHONE_TYPES,
                           ADDRESS_NAME: ADDRESS_TYPES,
                           MAIL_NAME: MAIL_TYPES,
                           TAGS_NAME: TAGS_TYPES,
                           SUBTYPE_NAME: SUBTYPE_TYPES}

    TOUCHPOINTS_SUBTYPES = [u"Cashless POS", u"POS", u"Kiosk", u"AP", u"Restricted AP", u"Wireless AP"]

    def setUp(self):
        super(UbicacionViewTestCase, self).setUp()
        create_test_client(self)

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.TYPE_NAME] = cls.CITY_NAME
        values[cls.LOCATION_NAME_NAME] = "Name"
        return values

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.TYPE_NAME] = cls.ZONE_NAME
        values[cls.LOCATION_NAME_NAME] = "New name"
        values[cls.ACTIVE_NAME] = True
        return values

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        location_type = request_values.get(cls.TYPE_NAME)
        if location_type is not None:
            for field, field_types in cls.TYPES_BY_FIELD_NAME.iteritems():
                if location_type not in field_types:
                    request_values[field] = None
                elif field == cls.TAGS_NAME and request_values.get(cls.TAGS_NAME) is None:
                    request_values[cls.TAGS_NAME] = []
        if request_values.get(cls.SUBTYPE_NAME) is not None:
            request_values[cls.SUBTYPE_NAME] = request_values[cls.SUBTYPE_NAME].upper()
        return request_values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0,
                                                    expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                                    number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_empty_locations_view(self):
        self.request_all_resources_and_check_result(0,
                                                    number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, u"Test description")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_phone(self):
        self.assign_field_value(self.TYPE_NAME, self.PHONE_TYPES[0])
        self.assign_field_value(self.PHONE_NAME, u"1234567")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_phone_on_type_without_phone_and_check_its_ignored(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.PHONE_NAME, u"1234567")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_changing_subtype(self):
        self.assign_field_value(self.TYPE_NAME, self.TOUCHPOINT_NAME)
        num_locations = 0
        for location_subtype in self.TOUCHPOINTS_SUBTYPES:
            self.assign_field_value(self.SUBTYPE_NAME, location_subtype)
            self.subtype = location_subtype
            self.do_create_requests(previously_created_entities=num_locations,
                                    number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
            num_locations += self.NUMBER_OF_ENTITIES

    def test_create_valid_locations_with_subtype_on_type_without_subtype_and_check_its_ignored(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.SUBTYPE_NAME, self.TOUCHPOINTS_SUBTYPES[0])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_tags(self):
        create_test_location_tag(self)
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [self.TEST_LOCATION_TAG_NAME])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_empty_tags(self):
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_without_tags(self):
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, None)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_tags_on_type_without_tags_and_check_its_ignored(self):
        create_test_location_tag(self)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.TAGS_NAME, [self.TEST_LOCATION_TAG_NAME])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_address(self):
        self.assign_field_value(self.TYPE_NAME, self.ADDRESS_TYPES[0])
        self.assign_field_value(self.ADDRESS_NAME, u"Cll 1 # 2-3")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_address_on_type_without_address_and_check_its_ignored(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.ADDRESS_NAME, u"Cll 1 # 2-3")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_mail(self):
        self.assign_field_value(self.TYPE_NAME, self.MAIL_TYPES[0])
        self.assign_field_value(self.MAIL_NAME, u"mail@test.com")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_mail_on_type_without_mail_and_check_its_ignored(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.MAIL_NAME, u"mail@test.com")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_web(self):
        self.assign_field_value(self.TYPE_NAME, self.WEB_TYPES[0])
        self.assign_field_value(self.WEB_URL_NAME, u"http://test.com")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_web_on_type_without_web_and_check_its_ignored(self):
        self.assign_field_value(self.TYPE_NAME, self.AREA_NAME)
        self.assign_field_value(self.WEB_URL_NAME, u"http://test.com")
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_true_active(self):
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_false_active(self):
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_latitude_and_longitude(self):
        self.assign_field_value(self.LATITUDE_NAME, -10.5)
        self.assign_field_value(self.LONGITUDE_NAME, 10.5)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_latitude_and_longitude_with_extreme_negative_values(self):
        self.assign_field_value(self.LATITUDE_NAME, -90.0)
        self.assign_field_value(self.LONGITUDE_NAME, -180.0)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_latitude_and_longitude_with_extreme_positive_value(self):
        self.assign_field_value(self.LATITUDE_NAME, 90.0)
        self.assign_field_value(self.LONGITUDE_NAME, 180.0)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_latitude_and_longitude_whit_zero_values(self):
        self.assign_field_value(self.LATITUDE_NAME, 0.0)
        self.assign_field_value(self.LONGITUDE_NAME, 0.0)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_changing_type(self):
        num_locations = 0
        for location_type in self.LOCATION_TYPES:
            if location_type == self.TOUCHPOINT_NAME:
                self.assign_field_value(self.SUBTYPE_NAME, self.TOUCHPOINTS_SUBTYPES[0])
            else:
                self.assign_field_value(self.SUBTYPE_NAME, None)
            self.assign_field_value(self.TYPE_NAME, location_type)
            self.do_create_requests(previously_created_entities=num_locations,
                                    number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
            num_locations += self.NUMBER_LOCATIONS

    def test_create_valid_locations_with_parent(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[self.ENTITY_NAME])
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_locations_with_parent_checking_children(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        parent_id = self.expected_ids[self.ENTITY_NAME]
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, parent_id)
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_create_requests(previously_created_entities=self.NUMBER_OF_ENTITIES,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        results = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              parent_id))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, 1)

    def test_create_valid_locations_changing_type_and_checking_by_get_by_type(self):
        for location_type in self.LOCATION_TYPES:
            if location_type == self.TOUCHPOINT_NAME:
                self.assign_field_value(self.SUBTYPE_NAME, self.TOUCHPOINTS_SUBTYPES[0])
            else:
                self.assign_field_value(self.SUBTYPE_NAME, None)

            if location_type == self.COUNTRY_NAME:
                default_locations = min(1, self.NUMBER_OF_DEFAULT_PLACES)
            elif location_type == self.REGION_NAME:
                default_locations = max(0, self.NUMBER_OF_DEFAULT_PLACES - 1)
            else:
                default_locations = 0

            self.assign_field_value(self.TYPE_NAME, location_type)
            self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES,
                                    do_get_and_check_results=False)
            results = self.do_get_request("/clients/{0}/locations/?{1}={2}"
                                          .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  self.TYPE_NAME, location_type))
            self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, default_locations)
            self.clean_test_data()

    def test_try_query_locations_by_type_with_invalid_type(self):
        result = self.do_get_request("/clients/{0}/locations/?{1}={2}"
                                     .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                             self.TYPE_NAME, "INVALID_TYPE"), expected_code=400)
        self.validate_error(result, LOCATION_INVALID_TYPE_CODE)

    def test_create_inactive_locations_and_check_they_are_not_included_in_active_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.clean_test_data()
        results = self.do_get_request("/clients/{0}/active-locations/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0, self.NUMBER_OF_DEFAULT_PLACES)

    def test_activate_valid_locations_and_check_they_are_included_in_active_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        results = self.do_get_request("/clients/{0}/active-locations/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_valid_active_locations_and_check_they_are_included_in_active_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        results = self.do_get_request("/clients/{0}/active-locations/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, self.NUMBER_OF_DEFAULT_PLACES)

    def test_deactivate_valid_locations_and_check_they_are_not_included_in_active_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.clean_test_data()
        results = self.do_get_request("/clients/{0}/active-locations/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0, self.NUMBER_OF_DEFAULT_PLACES)

    def test_activate_valid_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_deactivate_valid_persons_reservations(self):
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_activate_invalid_non_existent_locations(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.change_ids_to_non_existent_entities()
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_patch_requests(expected_code=404,
                               expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE,
                               number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_deactivate_invalid_non_existent_persons_reservations(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.change_ids_to_non_existent_entities()
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_patch_requests(expected_code=404,
                               expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE,
                               number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_activate_valid_locations_with_invalid_active(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, u"invalid")
        self.do_patch_requests(expected_code=400,
                               expected_internal_code=LOCATION_INVALID_ACTIVE_CODE,
                               number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_create_valid_locations_with_invalid_active(self):
        self.assign_field_value(self.ACTIVE_NAME, u"invalid")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_ACTIVE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_positive_latitude_out_of_range(self):
        self.assign_field_value(self.LATITUDE_NAME, 91)
        self.assign_field_value(self.LONGITUDE_NAME, 50.0)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LATITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_negative_latitude_out_of_range(self):
        self.assign_field_value(self.LATITUDE_NAME, -91)
        self.assign_field_value(self.LONGITUDE_NAME, 50.0)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LATITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_positive_longitude_out_of_range(self):
        self.assign_field_value(self.LATITUDE_NAME, 50.0)
        self.assign_field_value(self.LONGITUDE_NAME, 181)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LONGITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_negative_longitude_out_of_range(self):
        self.assign_field_value(self.LATITUDE_NAME, 50.0)
        self.assign_field_value(self.LONGITUDE_NAME, -181)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LONGITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_latitude_without_longitude(self):
        self.assign_field_value(self.LATITUDE_NAME, 50.0)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LONGITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_longitude_without_latitude(self):
        self.assign_field_value(self.LONGITUDE_NAME, 50.0)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LATITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_description(self):
        self.assign_field_value(self.DESCRIPTION_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_DESCRIPTION_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_web(self):
        self.assign_field_value(self.TYPE_NAME, self.WEB_TYPES[0])
        self.assign_field_value(self.WEB_URL_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_WEB_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_phone(self):
        self.assign_field_value(self.TYPE_NAME, self.PHONE_TYPES[0])
        self.assign_field_value(self.PHONE_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_PHONE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_address(self):
        self.assign_field_value(self.TYPE_NAME, self.ADDRESS_TYPES[0])
        self.assign_field_value(self.ADDRESS_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_ADDRESS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_mail(self):
        self.assign_field_value(self.TYPE_NAME, self.MAIL_TYPES[0])
        self.assign_field_value(self.MAIL_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_MAIL_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_mail(self):
        self.assign_field_value(self.TYPE_NAME, self.MAIL_TYPES[0])
        self.assign_field_value(self.MAIL_NAME, u"invalid mail")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_MAIL_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_without_subtype_for_applicable_type(self):
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_SUBTYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_subtype(self):
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.assign_field_value(self.SUBTYPE_NAME, u"INVALID SUBTYPE")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_SUBTYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_subtype(self):
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.assign_field_value(self.SUBTYPE_NAME, u"")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_SUBTYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_empty_tag(self):
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [u""])
        self.do_create_requests(expected_code=404,
                                expected_internal_code=LOCATION_TAG_DOES_NOT_EXISTS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_inexistent_tag(self):
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [u"invalid tag"])
        self.do_create_requests(expected_code=404,
                                expected_internal_code=LOCATION_TAG_DOES_NOT_EXISTS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_without_name(self):
        self.assign_field_value(self.LOCATION_NAME_NAME, "")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_NAME_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_without_type(self):
        self.assign_field_value(self.TYPE_NAME, "")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_TYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_type(self):
        self.assign_field_value(self.TYPE_NAME, "TIPO_INVALIDO")
        self.do_create_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_TYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_create_invalid_locations_with_invalid_parent(self):
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[UbicacionViewTestCase.ENTITY_NAME])
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_create_requests(expected_code=400, expected_internal_code=LOCATION_INVALID_TYPE_RELATIONSHIP_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES,
                                previously_created_entities=self.NUMBER_OF_ENTITIES)

    def test_create_invalid_locations_with_invalid_parent_with_same_type(self):
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[UbicacionViewTestCase.ENTITY_NAME])
        self.do_create_requests(expected_code=400, expected_internal_code=LOCATION_INVALID_TYPE_RELATIONSHIP_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES,
                                previously_created_entities=self.NUMBER_OF_ENTITIES)

    def test_create_locations_on_multiple_clients(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.clean_test_data()

        create_test_client(self, create_new_client=True)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_description(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.DESCRIPTION_NAME, u"Test description")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_phone(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.PHONE_TYPES[0])
        self.assign_field_value(self.PHONE_NAME, u"1234567")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_phone_on_type_without_phone_and_check_its_ignored(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.PHONE_NAME, u"1234567")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_subtype_on_type_with_subtype(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.assign_field_value(self.SUBTYPE_NAME, self.TOUCHPOINTS_SUBTYPES[0])
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_subtype_on_type_without_subtype_and_check_its_ignored(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.SUBTYPE_NAME, self.TOUCHPOINTS_SUBTYPES[0])
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_tags(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        create_test_location_tag(self)
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [self.TEST_LOCATION_TAG_NAME])
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_empty_tags(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [])
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_without_tags(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, None)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_tags_on_type_without_tags_and_check_its_ignored(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        create_test_location_tag(self)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.TAGS_NAME, [self.TEST_LOCATION_TAG_NAME])
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_address(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.ADDRESS_TYPES[0])
        self.assign_field_value(self.ADDRESS_NAME, u"Cll 1 # 2-3")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_address_on_type_without_address_and_check_its_ignored(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.ADDRESS_NAME, u"Cll 1 # 2-3")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_mail(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.MAIL_TYPES[0])
        self.assign_field_value(self.MAIL_NAME, u"mail@test.com")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_mail_on_type_without_mail_and_check_its_ignored(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.assign_field_value(self.MAIL_NAME, u"mail@test.com")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_web(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.WEB_TYPES[0])
        self.assign_field_value(self.WEB_URL_NAME, u"http://test.com")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_web_on_type_without_web_and_check_its_ignored(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.AREA_NAME)
        self.assign_field_value(self.WEB_URL_NAME, u"http://test.com")
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_true_active(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_false_active(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_latitude_and_longitude(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, -10.5)
        self.assign_field_value(self.LONGITUDE_NAME, 10.5)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_latitude_and_longitude_with_extreme_negative_values(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, -90.0)
        self.assign_field_value(self.LONGITUDE_NAME, -180.0)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_latitude_and_longitude_with_extreme_positive_value(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, 90.0)
        self.assign_field_value(self.LONGITUDE_NAME, 180.0)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_latitude_and_longitude_whit_zero_values(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, 0.0)
        self.assign_field_value(self.LONGITUDE_NAME, 0.0)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_valid_locations_with_parent(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_parent = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()
        self.assign_field_value(self.TYPE_NAME, self.CITY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)

        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, id_parent)
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)

    def test_update_valid_locations_with_parent_checking_children(self):
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        parent_location_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()

        self.assign_field_value(self.TYPE_NAME, self.CITY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, parent_location_id)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)
        results = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              parent_location_id))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, 1)

    def test_update_valid_locations_checking_previously_unconnected_subtrees(self):
        self.NUMBER_OF_ENTITIES = 1

        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_level_0 = self.expected_ids[self.ENTITY_NAME]

        self.assign_field_value(self.TYPE_NAME, self.REGION_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, id_level_0)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_level_1 = self.expected_ids[self.ENTITY_NAME]

        self.assign_field_value(self.TYPE_NAME, self.CITY_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, None)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_level_2 = self.expected_ids[self.ENTITY_NAME]

        self.assign_field_value(self.TYPE_NAME, self.POI_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, id_level_2)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_level_3 = self.expected_ids[self.ENTITY_NAME]

        results_0 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_0))
        self.assertEqual(2, len(results_0))

        results_1 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_1))
        self.assertEqual(1, len(results_1))

        results_2 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_2))
        self.assertEqual(2, len(results_2))

        results_3 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_3))
        self.assertEqual(1, len(results_3))

        self.original_entities = [self.do_get_request("/clients/{0}/locations/{1}/"
                                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                              id_level_2))]

        self.assign_field_value(self.TYPE_NAME, self.CITY_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, id_level_1)
        self.do_update_requests(check_results_as_list=False, do_get_and_check_results=False)

        results_0 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_0))
        self.assertEqual(4, len(results_0))

        results_1 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_1))
        self.assertEqual(3, len(results_1))

        results_2 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_2))
        self.assertEqual(2, len(results_2))

        results_3 = self.do_get_request("/clients/{0}/locations/{1}/children/"
                                        .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                id_level_3))
        self.assertEqual(1, len(results_3))

    def test_activate_valid_locations_with_put_and_check_they_are_included_in_active_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        results = self.do_get_request("/clients/{0}/active-locations/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_OF_ENTITIES, self.NUMBER_OF_DEFAULT_PLACES)

    def test_deactivate_valid_locations_with_put_and_check_they_are_not_included_in_active_locations(self):
        self.assign_field_value(self.ACTIVE_NAME, True)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, False)
        self.do_update_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.clean_test_data()
        results = self.do_get_request("/clients/{0}/active-locations/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0, self.NUMBER_OF_DEFAULT_PLACES)

    def test_update_locations_with_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_update_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES,
                                do_get_and_check_results=False)

    def test_update_non_existent_locations(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.change_ids_to_non_existent_entities()
        self.do_update_requests(expected_code=404, expected_internal_code=LOCATION_DOES_NOT_EXISTS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_valid_locations_with_invalid_active(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.ACTIVE_NAME, u"invalid")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_ACTIVE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_positive_latitude_out_of_range(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, 91)
        self.assign_field_value(self.LONGITUDE_NAME, 50.0)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LATITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_negative_latitude_out_of_range(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, -91)
        self.assign_field_value(self.LONGITUDE_NAME, 50.0)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LATITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_positive_longitude_out_of_range(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, 50.0)
        self.assign_field_value(self.LONGITUDE_NAME, 181)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LONGITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_negative_longitude_out_of_range(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, 50.0)
        self.assign_field_value(self.LONGITUDE_NAME, -181)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LONGITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_latitude_without_longitude(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LATITUDE_NAME, 50.0)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LONGITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_longitude_without_latitude(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LONGITUDE_NAME, 50.0)
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_LATITUDE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_description(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.DESCRIPTION_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_DESCRIPTION_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_web(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.WEB_TYPES[0])
        self.assign_field_value(self.WEB_URL_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_WEB_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_phone(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.PHONE_TYPES[0])
        self.assign_field_value(self.PHONE_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_PHONE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_address(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.ADDRESS_TYPES[0])
        self.assign_field_value(self.ADDRESS_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_ADDRESS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_mail(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.MAIL_TYPES[0])
        self.assign_field_value(self.MAIL_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_MAIL_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_mail(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.MAIL_TYPES[0])
        self.assign_field_value(self.MAIL_NAME, u"invalid mail")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_MAIL_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_without_subtype_for_applicable_type(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_SUBTYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_subtype(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.assign_field_value(self.SUBTYPE_NAME, u"INVALID SUBTYPE")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_SUBTYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_subtype(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.SUBTYPE_TYPES[0])
        self.assign_field_value(self.SUBTYPE_NAME, u"")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_SUBTYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_empty_tag(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [u""])
        self.do_update_requests(expected_code=404,
                                expected_internal_code=LOCATION_TAG_DOES_NOT_EXISTS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_inexistent_tag(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.TAGS_TYPES[0])
        self.assign_field_value(self.TAGS_NAME, [u"invalid tag"])
        self.do_update_requests(expected_code=404,
                                expected_internal_code=LOCATION_TAG_DOES_NOT_EXISTS_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_without_name(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.LOCATION_NAME_NAME, "")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_NAME_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_without_type(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, "")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_TYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_type(self):
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, "TIPO_INVALIDO")
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_TYPE_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_with_invalid_parent_type(self):
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_parent = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()

        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, id_parent)
        self.assign_field_value(self.TYPE_NAME, self.COUNTRY_NAME)
        self.do_update_requests(expected_code=400, expected_internal_code=LOCATION_INVALID_TYPE_RELATIONSHIP_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)

    def test_try_update_invalid_locations_with_invalid_parent_with_same_type(self):
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        id_parent = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()

        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)
        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, id_parent)
        self.do_update_requests(expected_code=400, expected_internal_code=LOCATION_INVALID_TYPE_RELATIONSHIP_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + self.NUMBER_OF_ENTITIES)

    def test_try_update_invalid_locations_with_id_parent_pointing_to_itself(self):
        self.NUMBER_OF_ENTITIES = 1
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_HIREARCHY_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_try_update_invalid_locations_creating_four_level_cicle(self):
        self.NUMBER_OF_ENTITIES = 1
        self.assign_field_value(self.TYPE_NAME, self.REGION_NAME)
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        top_entities = list(self.original_entities)
        self.assign_field_value(self.TYPE_NAME, self.CITY_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.POI_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        self.assign_field_value(self.TYPE_NAME, self.PROPERTY_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[self.ENTITY_NAME])
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

        self.assign_field_value(self.TYPE_NAME, self.ZONE_NAME)
        self.assign_field_value(self.ID_PARENT_LOCATION_NAME, self.expected_ids[self.ENTITY_NAME])
        self.clean_test_data()
        self.original_entities = top_entities
        self.do_update_requests(expected_code=400,
                                expected_internal_code=LOCATION_INVALID_HIREARCHY_CODE,
                                number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES + 3 * self.NUMBER_OF_ENTITIES)

    def test_check_permissions_for_create_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_create_permissions(allowed_roles, required_locations,
                                      number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)

    def test_check_permissions_for_get_all_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_locations_by_type(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = self.get_base_url(type(self)) + "?{0}={1}".format(self.TYPE_NAME, self.COUNTRY_NAME)
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_location(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.expected_ids[self.ENTITY_NAME]}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_active_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {}
        url = "/clients/{0}/active-locations/".format(self.expected_ids[CLIENT_ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_children_locations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_CASHIER_USER, CLIENT_WAITER_USER, CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_WAITER_USER,
                         CLIENT_ACCESS_USER, CLIENT_PROMOTER_USER}
        required_locations = {self.expected_ids[self.ENTITY_NAME]}
        url = "/clients/{0}/locations/{1}/children/".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                            self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_patch_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.expected_ids[self.ENTITY_NAME]}
        self.check_patch_permissions(allowed_roles, required_locations)

    def test_check_permissions_for_put_reservations(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests(number_of_default_entities=self.NUMBER_OF_DEFAULT_PLACES)
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.expected_ids[self.ENTITY_NAME]}
        self.check_update_permissions(allowed_roles, required_locations)


LOCATION_ENTITY_NAME = UbicacionViewTestCase.ENTITY_NAME


def create_test_location(test_class, create_new_location=False, expected_code=200, validate_results_on_success=False):
    return UbicacionViewTestCase.create_sample_entity_for_another_class(test_class, create_new_location,
                                                                        expected_code=expected_code,
                                                                        validate_results_on_success=validate_results_on_success)

if __name__ == '__main__':
    unittest.main()
