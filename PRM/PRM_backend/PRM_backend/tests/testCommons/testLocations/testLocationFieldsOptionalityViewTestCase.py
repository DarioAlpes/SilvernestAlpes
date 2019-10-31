# -*- coding: utf-8 -*
import unittest

from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import validate_error, CLIENT_DOES_NOT_EXISTS_CODE, \
    OPTIONALITY_FIELD_DOES_NOT_EXISTS_CODE, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE, \
    OPTIONALITY_INVALID_OPTIONALITY_CODE, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testLocationTagViewTestCase import create_test_location_tag
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location


class LocationFieldsOptionalityViewTestCase(FlaskClientBaseTestCase):
    FIELD_NAME_NAME = u"field"
    OPTIONALITY_NAME = u"optionality"
    APPLICABLE_TYPES_NAME = u"applicable-types"
    DEFAULT_VALUE_NAME = u"default-value"
    VIEW_NAME = u"view"

    ID_NAME = FIELD_NAME_NAME

    ENTITY_DOES_NOT_EXISTS_CODE = None
    RESOURCE_URL = u"/clients/{0}/location-fields-optionalities/"

    TEST_LOCATION_DESCRIPTION = "test description"
    TEST_LOCATION_LATITUDE = None
    TEST_LOCATION_LONGITUDE = None
    TEST_LOCATION_WEB_URL = None
    TEST_LOCATION_PHONE = None
    TEST_LOCATION_MAIL = None
    TEST_LOCATION_ADDRESS = None
    TEST_LOCATION_TAGS = None
    TEST_LOCATION_SUBTYPE = None
    TEST_LOCATION_TYPE = "COUNTRY"
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    ENTITY_NAME = u"location-fields-permissions"
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

    MANDATORY_NAME = u"MANDATORY"
    OPTIONAL_NAME = u"OPTIONAL"
    FORBIDDEN_NAME = u"FORBIDDEN"

    COUNTRY_NAME = u"COUNTRY"
    REGION_NAME = u"REGION"
    CITY_NAME = u"CITY"
    POI_NAME = u"POI"
    PROPERTY_NAME = u"PROPERTY"
    ZONE_NAME = u"ZONE"
    AREA_NAME = u"AREA"
    TOUCHPOINT_NAME = u"TOUCHPOINT"

    LOCATIONS_VIEW_NAME = u"locations"

    WEB_TYPES = [COUNTRY_NAME, REGION_NAME, CITY_NAME, POI_NAME, PROPERTY_NAME]
    PHONE_TYPES = [POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME]
    ADDRESS_TYPES = [POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME]
    MAIL_TYPES = [POI_NAME, PROPERTY_NAME, ZONE_NAME, AREA_NAME]
    TAGS_TYPES = [POI_NAME]
    SUBTYPE_TYPES = [TOUCHPOINT_NAME]

    VALID_SUBTYPES = [u"Cashless POS", u"POS", u"Kiosk", u"AP", u"Restricted AP", u"Wireless AP"]

    TEST_LOCATION_TAG_NAME = u"Test tag"

    def setUp(self):
        super(LocationFieldsOptionalityViewTestCase, self).setUp()
        create_test_client(self)
        create_test_location_tag(self)
        self.test_valid_tags = [self.TEST_LOCATION_TAG_NAME]
        self.expected_optionalities = {self.TYPE_NAME: self.MANDATORY_NAME,
                                       self.LOCATION_NAME_NAME: self.MANDATORY_NAME,
                                       self.ID_PARENT_LOCATION_NAME: self.OPTIONAL_NAME,
                                       self.DESCRIPTION_NAME: self.OPTIONAL_NAME,
                                       self.ACTIVE_NAME: self.OPTIONAL_NAME,
                                       self.LATITUDE_NAME: self.OPTIONAL_NAME,
                                       self.LONGITUDE_NAME: self.OPTIONAL_NAME,
                                       self.WEB_URL_NAME: self.OPTIONAL_NAME,
                                       self.PHONE_NAME: self.OPTIONAL_NAME,
                                       self.ADDRESS_NAME: self.OPTIONAL_NAME,
                                       self.MAIL_NAME: self.OPTIONAL_NAME,
                                       self.TAGS_NAME: self.OPTIONAL_NAME,
                                       self.SUBTYPE_NAME: self.MANDATORY_NAME}
        self.expected_applicable_types = {self.WEB_URL_NAME: self.WEB_TYPES,
                                          self.PHONE_NAME: self.PHONE_TYPES,
                                          self.ADDRESS_NAME: self.ADDRESS_TYPES,
                                          self.MAIL_NAME: self.MAIL_TYPES,
                                          self.TAGS_NAME: self.TAGS_TYPES,
                                          self.SUBTYPE_NAME: self.SUBTYPE_TYPES}
        self.expected_default_values = {self.ACTIVE_NAME: True}

    @classmethod
    def get_static_entity_values_for_update(cls):
        values = dict()
        values[cls.OPTIONALITY_NAME] = cls.OPTIONAL_NAME
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME]

    def test_non_existent_client(self):
        id_not_existent_client = self.expected_ids[CLIENT_ENTITY_NAME] + 1
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/".format(id_not_existent_client),
                                      expected_code=404)
        validate_error(self, results, CLIENT_DOES_NOT_EXISTS_CODE)

    def test_default_locations_fields_optionality_view(self):
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_attempt_to_change_optionality_of_non_existent_field(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              "INVALID FIELD"),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=404)
        validate_error(self, results, OPTIONALITY_FIELD_DOES_NOT_EXISTS_CODE)
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_attempt_to_change_name_optionality(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LOCATION_NAME_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_attempt_to_change_type_optionality(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TYPE_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_attempt_to_change_parent_location_optionality(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.ID_PARENT_LOCATION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_attempt_to_change_active_optionality(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.ACTIVE_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_CAN_NOT_BE_CHANGED_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.DESCRIPTION_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_description_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DESCRIPTION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: "default description",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_description_optionality_to_invalid_optionality(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DESCRIPTION_NAME),
                                      data={self.OPTIONALITY_NAME: "INVALID OPTIONALITY",
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_OPTIONALITY_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_description_optionality_to_mandatory_with_invalid_applicable_types(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DESCRIPTION_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: ["INVALID TYPE"]}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.DESCRIPTION_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.DESCRIPTION_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_description_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_description_optionality_to_mandatory_with_applicable_types_and_create_location_with_description_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_DESCRIPTION = "Test description"
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_mandatory_with_applicable_types_and_create_location_without_description_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_mandatory_and_try_to_create_location_without_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_description_optionality_to_mandatory_and_create_location_with_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = "Test description"
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.DESCRIPTION_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_optional_with_default_value(self):
        default_description = "default description"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_description,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.DESCRIPTION_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.DESCRIPTION_NAME] = default_description
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_optional_with_default_value_and_create_location_without_description(self):
        default_description = "default description"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_description,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_description, location[self.DESCRIPTION_NAME])

    def test_change_description_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.DESCRIPTION_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.DESCRIPTION_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_optional_with_applicable_types_and_create_location_without_description_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_optional_with_applicable_types_and_create_location_with_description_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_DESCRIPTION = "Test description"
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_optional_with_applicable_types_and_create_location_without_description_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_optional_with_applicable_types_and_create_location_with_description_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_DESCRIPTION = "Test description"
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.DESCRIPTION_NAME not in location)

    def test_change_description_optionality_to_optional_and_create_location_without_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_optional_and_create_location_with_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = "Test description"
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.DESCRIPTION_NAME] = self.FORBIDDEN_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_description_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DESCRIPTION_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: "default description",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_description_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.DESCRIPTION_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_description_optionality_to_forbidden_and_create_location_without_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = None
        create_test_location(self, create_new_location=True)

    def test_change_description_optionality_to_forbidden_and_try_to_create_location_with_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.DESCRIPTION_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_DESCRIPTION = "Test description"
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_web_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.WEB_TYPES})
        self.expected_optionalities[self.WEB_URL_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_web_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.WEB_URL_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: "http://test.web",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_web_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.WEB_URL_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.WEB_URL_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_web_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_web_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_web_optionality_to_mandatory_with_applicable_types_and_create_location_with_web_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_WEB_URL = "http://test.web"
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_mandatory_with_applicable_types_and_create_location_without_web_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_mandatory_and_try_to_create_location_without_web(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_web_optionality_to_mandatory_and_create_location_with_description(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = "http://test.web"
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.WEB_TYPES})
        self.expected_optionalities[self.WEB_URL_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_web_optionality_to_optional_with_default_value(self):
        default_web = "http://test.web"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_web,
                                  self.APPLICABLE_TYPES_NAME: self.WEB_TYPES})
        self.expected_optionalities[self.WEB_URL_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.WEB_URL_NAME] = default_web
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_web_optionality_to_optional_with_default_value_and_create_location_without_web(self):
        default_web = "http://test.web"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_web,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_web, location[self.WEB_URL_NAME])

    def test_change_web_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.WEB_URL_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.WEB_URL_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_web_optionality_to_optional_with_applicable_types_and_create_location_without_web_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_optional_with_applicable_types_and_create_location_with_web_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_WEB_URL = "http://test.web"
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_optional_with_applicable_types_and_create_location_without_web_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_optional_with_applicable_types_and_create_location_with_web_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_WEB_URL = "http://test.web"
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.WEB_URL_NAME not in location)

    def test_change_web_optionality_to_optional_and_create_location_without_web(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_optional_and_create_location_with_web(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = "http://test.web"
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.WEB_URL_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.WEB_URL_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_web_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.WEB_URL_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: "http://test.web",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_web_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.WEB_URL_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_web_optionality_to_forbidden_and_create_location_without_web(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = None
        create_test_location(self, create_new_location=True)

    def test_change_web_optionality_to_forbidden_and_try_to_create_location_with_web(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_WEB_URL = "http://test.web"
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_phone_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.PHONE_TYPES})
        self.expected_optionalities[self.PHONE_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_phone_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PHONE_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: "1234567",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_phone_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.PHONE_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.PHONE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_phone_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_phone_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_phone_optionality_to_mandatory_with_applicable_types_and_create_location_with_phone_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_PHONE = "1234567"
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_mandatory_with_applicable_types_and_create_location_without_phone_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_mandatory_and_try_to_create_location_without_phone(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.WEB_URL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_phone_optionality_to_mandatory_and_create_location_with_phone(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = "1234567"
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.PHONE_TYPES})
        self.expected_optionalities[self.PHONE_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_phone_optionality_to_optional_with_default_value(self):
        default_phone = "7654321"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_phone,
                                  self.APPLICABLE_TYPES_NAME: self.PHONE_TYPES})
        self.expected_optionalities[self.PHONE_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.PHONE_NAME] = default_phone
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_phone_optionality_to_optional_with_default_value_and_create_location_without_phone(self):
        default_phone = "7654321"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_phone,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_phone, location[self.PHONE_NAME])

    def test_change_phone_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.PHONE_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.PHONE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_phone_optionality_to_optional_with_applicable_types_and_create_location_without_phone_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_optional_with_applicable_types_and_create_location_with_phone_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_PHONE = "1234567"
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_optional_with_applicable_types_and_create_location_without_phone_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_optional_with_applicable_types_and_create_location_with_phone_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_PHONE = "1234567"
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.WEB_URL_NAME not in location)

    def test_change_phone_optionality_to_optional_and_create_location_without_phone(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_optional_and_create_location_with_phone(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = "1234567"
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.PHONE_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.PHONE_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_phone_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PHONE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: "7654321",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_phone_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.PHONE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_phone_optionality_to_forbidden_and_create_location_without_phone(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = None
        create_test_location(self, create_new_location=True)

    def test_change_phone_optionality_to_forbidden_and_try_to_create_location_with_phone(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.PHONE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_PHONE = "1234567"
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_address_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.ADDRESS_TYPES})
        self.expected_optionalities[self.ADDRESS_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_address_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.ADDRESS_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: "Cll 1 # 2",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_address_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.ADDRESS_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.ADDRESS_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_address_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_address_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_address_optionality_to_mandatory_with_applicable_types_and_create_location_with_address_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_ADDRESS = "Cll 1 # 2"
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_mandatory_with_applicable_types_and_create_location_without_address_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_mandatory_and_try_to_create_location_without_address(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_address_optionality_to_mandatory_and_create_location_with_address(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = "Cll 1 # 2"
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.ADDRESS_TYPES})
        self.expected_optionalities[self.ADDRESS_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_address_optionality_to_optional_with_default_value(self):
        default_address = "Cll 1 # 2"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_address,
                                  self.APPLICABLE_TYPES_NAME: self.PHONE_TYPES})
        self.expected_optionalities[self.ADDRESS_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.ADDRESS_NAME] = default_address
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_address_optionality_to_optional_with_default_value_and_create_location_without_address(self):
        default_address = "Cll 1 # 2"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_address,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_address, location[self.ADDRESS_NAME])

    def test_change_address_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.ADDRESS_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.ADDRESS_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_address_optionality_to_optional_with_applicable_types_and_create_location_without_address_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_optional_with_applicable_types_and_create_location_with_address_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_ADDRESS = "Cll 1 # 2"
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_optional_with_applicable_types_and_create_location_without_address_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_optional_with_applicable_types_and_create_location_with_address_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_ADDRESS = "Cll 1 # 2"
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.ADDRESS_NAME not in location)

    def test_change_address_optionality_to_optional_and_create_location_without_address(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_optional_and_create_location_with_address(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = "Cll 1 # 2"
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.ADDRESS_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.ADDRESS_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_address_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.ADDRESS_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: "Cll 1 # 2",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_address_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.ADDRESS_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_address_optionality_to_forbidden_and_create_location_without_address(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = None
        create_test_location(self, create_new_location=True)

    def test_change_address_optionality_to_forbidden_and_try_to_create_location_with_address(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_ADDRESS = "Cll 1 # 2"
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_mail_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.MAIL_TYPES})
        self.expected_optionalities[self.MAIL_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: "example@test.com",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.MAIL_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.MAIL_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_mail_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_mail_optionality_to_mandatory_with_applicable_types_and_create_location_with_mail_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_MAIL = "example@test.com"
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_mandatory_with_applicable_types_and_create_location_without_mail_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_mandatory_and_try_to_create_location_without_mail(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.ADDRESS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_mail_optionality_to_mandatory_and_create_location_with_mail(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = "example@test.com"
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.MAIL_TYPES})
        self.expected_optionalities[self.MAIL_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_optional_with_default_value(self):
        default_mail = "example@test.com"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_mail,
                                  self.APPLICABLE_TYPES_NAME: self.MAIL_TYPES})
        self.expected_optionalities[self.MAIL_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.MAIL_NAME] = default_mail
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_optional_with_default_value_and_create_location_without_mail(self):
        default_mail = "example@test.com"
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_mail,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_mail, location[self.MAIL_NAME])

    def test_change_mail_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.MAIL_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.MAIL_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_optional_with_applicable_types_and_create_location_without_mail_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_optional_with_applicable_types_and_create_location_with_mail_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_MAIL = "example@test.com"
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_optional_with_applicable_types_and_create_location_without_mail_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_optional_with_applicable_types_and_create_location_with_mail_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_MAIL = "example@test.com"
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.MAIL_NAME not in location)

    def test_change_mail_optionality_to_optional_and_create_location_without_mail(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_optional_and_create_location_with_mail(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = "example@test.com"
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.MAIL_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.MAIL_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: "example@test.com",
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_mail_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.MAIL_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_mail_optionality_to_forbidden_and_create_location_without_mail(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = None
        create_test_location(self, create_new_location=True)

    def test_change_mail_optionality_to_forbidden_and_try_to_create_location_with_mail(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.MAIL_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_MAIL = "example@test.com"
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_subtype_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.SUBTYPE_TYPES})
        self.expected_optionalities[self.SUBTYPE_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_subtype_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.SUBTYPE_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.VALID_SUBTYPES[0],
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_subtype_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.SUBTYPE_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.SUBTYPE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_subtype_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_subtype_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_subtype_optionality_to_mandatory_with_applicable_types_and_create_location_with_subtype_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_SUBTYPE = self.VALID_SUBTYPES[0]
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_mandatory_with_applicable_types_and_create_location_without_subtype_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_mandatory_and_try_to_create_location_without_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_subtype_optionality_to_mandatory_and_create_location_with_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = self.VALID_SUBTYPES[0]
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.SUBTYPE_TYPES})
        self.expected_optionalities[self.SUBTYPE_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_subtype_optionality_to_optional_with_default_value(self):
        default_subtype = self.VALID_SUBTYPES[0]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_subtype,
                                  self.APPLICABLE_TYPES_NAME: self.SUBTYPE_TYPES})
        self.expected_optionalities[self.SUBTYPE_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.SUBTYPE_NAME] = default_subtype
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_subtype_optionality_to_optional_with_default_value_and_create_location_without_subtype(self):
        default_subtype = self.VALID_SUBTYPES[0]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_subtype,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_subtype, location[self.SUBTYPE_NAME])

    def test_change_subtype_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.SUBTYPE_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.SUBTYPE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_subtype_optionality_to_optional_with_applicable_types_and_create_location_without_subtype_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_optional_with_applicable_types_and_create_location_with_subtype_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_SUBTYPE = self.VALID_SUBTYPES[0]
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_optional_with_applicable_types_and_create_location_without_subtype_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_optional_with_applicable_types_and_create_location_with_subtype_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_SUBTYPE = self.VALID_SUBTYPES[0]
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.SUBTYPE_NAME not in location)

    def test_change_subtype_optionality_to_optional_and_create_location_without_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_optional_and_create_location_with_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = self.VALID_SUBTYPES[0]
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.SUBTYPE_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.SUBTYPE_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_subtype_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.SUBTYPE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.VALID_SUBTYPES[0],
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_subtype_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.SUBTYPE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_subtype_optionality_to_forbidden_and_create_location_without_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = None
        create_test_location(self, create_new_location=True)

    def test_change_subtype_optionality_to_forbidden_and_try_to_create_location_with_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.SUBTYPE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_SUBTYPE = self.VALID_SUBTYPES[0]
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_tags_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.TAGS_TYPES})
        self.expected_optionalities[self.TAGS_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_tags_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TAGS_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: self.test_valid_tags,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_tags_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.TAGS_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.TAGS_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_tags_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_tags_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True, expected_code=404)

    def test_change_tags_optionality_to_mandatory_with_applicable_types_and_create_location_with_tags_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_TAGS = self.test_valid_tags
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_mandatory_with_applicable_types_and_create_location_without_tags_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_mandatory_and_try_to_create_location_without_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True, expected_code=404)

    def test_change_tags_optionality_to_mandatory_and_try_to_create_location_with_empty_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = []
        create_test_location(self, create_new_location=True, expected_code=404)

    def test_change_tags_optionality_to_mandatory_and_create_location_with_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = self.test_valid_tags
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: self.TAGS_TYPES})
        self.expected_optionalities[self.TAGS_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_tags_optionality_to_optional_with_default_value(self):
        default_tags = self.test_valid_tags
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TAGS_NAME),
                                      data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                            self.DEFAULT_VALUE_NAME: default_tags,
                                            self.APPLICABLE_TYPES_NAME: self.TAGS_TYPES}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_tags_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.TAGS_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.TAGS_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_tags_optionality_to_optional_with_applicable_types_and_create_location_without_tags_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_optional_with_applicable_types_and_create_location_with_empty_tags_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_TAGS = []
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.TAGS_NAME not in location)

    def test_change_tags_optionality_to_optional_with_applicable_types_and_create_location_with_tags_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_TAGS = self.test_valid_tags
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_optional_with_applicable_types_and_create_location_without_tags_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_optional_with_applicable_types_and_create_location_with_tags_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_TAGS = self.test_valid_tags
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.TAGS_NAME not in location)

    def test_change_tags_optionality_to_optional_and_create_location_without_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_optional_and_create_location_with_empty_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = []
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.TAGS_NAME not in location)

    def test_change_tags_optionality_to_optional_and_create_location_with_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = self.test_valid_tags
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.TAGS_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.TAGS_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_tags_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TAGS_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: self.test_valid_tags,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_tags_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TAGS_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_tags_optionality_to_forbidden_and_create_location_without_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = None
        create_test_location(self, create_new_location=True)

    def test_change_tags_optionality_to_forbidden_and_try_to_create_location_with_tags(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.TAGS_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_TAGS = self.test_valid_tags
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LATITUDE_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_latitude_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LATITUDE_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: 0,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_latitude_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.LATITUDE_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.LATITUDE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_latitude_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_latitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_longitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_mandatory_with_applicable_types_and_create_location_with_latitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_mandatory_with_applicable_types_and_create_location_without_latitude_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_mandatory_and_try_to_create_location_without_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_mandatory_and_create_location_with_subtype(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LATITUDE_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_latitude_optionality_to_optional_with_default_value(self):
        default_latitude = 10
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_latitude,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LATITUDE_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.LATITUDE_NAME] = default_latitude
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_latitude_optionality_to_optional_with_default_value_and_create_location_without_latitude(self):
        default_latitude = 10
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_latitude,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = 20
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_latitude, location[self.LATITUDE_NAME])

    def test_change_latitude_optionality_to_optional_with_default_value_and_try_create_location_without_latitude_and_longitude(self):
        default_latitude = 10
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_latitude,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.LATITUDE_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.LATITUDE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_latitude_optionality_to_optional_with_applicable_types_and_create_location_without_latitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_optional_with_applicable_types_and_create_location_with_latitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_optional_with_applicable_types_and_create_location_without_latitude_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_optional_with_applicable_types_and_create_location_with_latitude_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.LATITUDE_NAME not in location)

    def test_change_latitude_optionality_to_optional_and_create_location_without_latitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_optional_and_try_create_location_with_latitude_but_without_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_optional_and_create_location_with_latitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LATITUDE_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.LATITUDE_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_latitude_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LATITUDE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: 10,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_latitude_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LATITUDE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_latitude_optionality_to_forbidden_and_create_location_without_latitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_latitude_optionality_to_forbidden_and_try_create_location_without_latitude_but_with_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_latitude_optionality_to_forbidden_and_try_to_create_location_with_latitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LATITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_mandatory_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LONGITUDE_NAME] = self.MANDATORY_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_longitude_optionality_to_mandatory_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LONGITUDE_NAME),
                                      data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                            self.DEFAULT_VALUE_NAME: 0,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_longitude_optionality_to_mandatory_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.LONGITUDE_NAME] = self.MANDATORY_NAME
        self.expected_applicable_types[self.LONGITUDE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_longitude_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_longitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_mandatory_with_applicable_types_and_try_to_create_location_without_latitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_mandatory_with_applicable_types_and_create_location_with_longitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_mandatory_with_applicable_types_and_create_location_without_longitude_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_mandatory_and_try_to_create_location_without_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_mandatory_and_create_location_with_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.MANDATORY_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_optional_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LONGITUDE_NAME] = self.OPTIONAL_NAME
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_longitude_optionality_to_optional_with_default_value(self):
        default_longitude = 10
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_longitude,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LONGITUDE_NAME] = self.OPTIONAL_NAME
        self.expected_default_values[self.LONGITUDE_NAME] = default_longitude
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_longitude_optionality_to_optional_with_default_value_and_create_location_without_longitude(self):
        default_longitude = 10
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_longitude,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 20
        self.TEST_LOCATION_LONGITUDE = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertEqual(default_longitude, location[self.LONGITUDE_NAME])

    def test_change_longitude_optionality_to_optional_with_default_value_and_try_create_location_without_latitude_and_longitude(self):
        default_longitude = 10
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: default_longitude,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_optional_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.expected_optionalities[self.LONGITUDE_NAME] = self.OPTIONAL_NAME
        self.expected_applicable_types[self.LONGITUDE_NAME] = applicable_types
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_longitude_optionality_to_optional_with_applicable_types_and_create_location_without_longitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_optional_with_applicable_types_and_create_location_with_latitude_on_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = applicable_types[0]
        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_optional_with_applicable_types_and_create_location_without_longitude_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_optional_with_applicable_types_and_create_location_with_longitude_on_non_applicable_type(self):
        applicable_types = [self.CITY_NAME]
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: applicable_types})
        self.TEST_LOCATION_TYPE = self.AREA_NAME
        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        id_location = create_test_location(self, create_new_location=True, validate_results_on_success=False)

        location = self.do_get_request("/clients/{0}/locations/{1}/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME], id_location))
        self.assertTrue(self.LONGITUDE_NAME not in location)

    def test_change_longitude_optionality_to_optional_and_create_location_without_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_optional_and_try_create_location_with_longitude_but_without_latitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_optional_and_create_location_with_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.OPTIONAL_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_forbidden_and_check_permision_changed(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})
        self.expected_optionalities[self.LONGITUDE_NAME] = self.FORBIDDEN_NAME
        self.expected_applicable_types[self.LONGITUDE_NAME] = None
        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_longitude_optionality_to_forbidden_with_default_value(self):
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LONGITUDE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: 10,
                                            self.APPLICABLE_TYPES_NAME: None}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_DEFAULT_VALUE_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_try_change_longitude_optionality_to_forbidden_with_applicable_types(self):
        applicable_types = [self.CITY_NAME]
        results = self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.LONGITUDE_NAME),
                                      data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                            self.DEFAULT_VALUE_NAME: None,
                                            self.APPLICABLE_TYPES_NAME: applicable_types}, expected_code=400)
        validate_error(self, results, OPTIONALITY_INVALID_APPLICABLE_TYPES_CODE)

        results = self.do_get_request("/clients/{0}/location-fields-optionalities/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME]))
        self.check_optionality_results(results)

    def test_change_longitude_optionality_to_forbidden_and_create_location_without_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = None
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True)

    def test_change_longitude_optionality_to_forbidden_and_try_create_location_without_longitude_but_with_latitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = None
        create_test_location(self, create_new_location=True, expected_code=400)

    def test_change_longitude_optionality_to_forbidden_and_try_to_create_location_with_longitude(self):
        self.do_put_request("/clients/{0}/location-fields-optionalities/{1}/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.LONGITUDE_NAME),
                            data={self.OPTIONALITY_NAME: self.FORBIDDEN_NAME,
                                  self.DEFAULT_VALUE_NAME: None,
                                  self.APPLICABLE_TYPES_NAME: None})

        self.TEST_LOCATION_LATITUDE = 10
        self.TEST_LOCATION_LONGITUDE = 10
        create_test_location(self, create_new_location=True, expected_code=400)

    def check_optionality_results(self, results):
        self.assertEqual(len(self.expected_optionalities), len(results))
        for result in results:
            field_name = result[self.FIELD_NAME_NAME]
            optionality = result[self.OPTIONALITY_NAME]
            self.assertEqual(self.expected_optionalities[field_name], optionality)
            self.assertEqual(self.LOCATIONS_VIEW_NAME, result[self.VIEW_NAME])

            if optionality == self.MANDATORY_NAME or optionality == self.OPTIONAL_NAME:
                expected_types = self.expected_applicable_types.get(field_name, None)
                if expected_types is None or len(expected_types) == 0:
                    self.assertTrue(self.APPLICABLE_TYPES_NAME not in result)
                else:
                    self.assertEqual(set(expected_types), set(result[self.APPLICABLE_TYPES_NAME]))
            else:
                self.assertTrue(self.APPLICABLE_TYPES_NAME not in result)

            expeted_default_value = self.expected_default_values.get(field_name, None)
            if expeted_default_value is None:
                self.assertTrue(self.DEFAULT_VALUE_NAME not in result)
            else:
                self.assertEqual(expeted_default_value, result[self.DEFAULT_VALUE_NAME])

    def test_check_permissions_for_get_all_optionalities(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE}
        required_locations = {}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_update_optionalities(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.original_entities = [{self.FIELD_NAME_NAME: self.DESCRIPTION_NAME}]
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {}
        self.check_update_permissions(allowed_roles, required_locations)

if __name__ == '__main__':
    unittest.main()
