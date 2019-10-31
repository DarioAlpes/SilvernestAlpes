# -*- coding: utf-8 -*
import unittest

from dateutil.relativedelta import relativedelta

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.errorDefinitions.errorConstants import CLIENT_DOES_NOT_EXISTS_CODE, validate_error, \
    PACKAGE_DOES_NOT_EXISTS_CODE, PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_CODE, PACKAGE_PRICE_RULE_INVALID_BASE_PRICE_CODE, \
    PACKAGE_PRICE_RULE_INVALID_TAX_RATE_CODE, PACKAGE_PRICE_RULE_INVALID_RULES_CODE, \
    PACKAGE_PRICE_RULE_INVALID_PROPERTY_CODE, PACKAGE_PRICE_RULE_INVALID_VALUE_CODE, \
    PACKAGE_PRICE_RULE_INVALID_DUPLICATED_RULE_CODE
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testPaquetes.testPaqueteViewTestCase import create_test_package, PACKAGE_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import create_test_person_reservation, \
    PERSON_RESERVATION_ENTITY_NAME
from tests.testsCJM.testReservas.testReservaViewTestCase import create_test_reservation, RESERVATION_ENTITY_NAME


class PackagePriceRuleViewTestCase(FlaskClientBaseTestCase):
    NUMBER_OF_ENTITIES = 1

    PACKAGE_HISTORIC_ID_NAME = u"historic-id"
    ID_NAME = u"id"
    HISTORIC_ID_NAME = u"historic-id"
    BASE_PRICE_NAME = u"base-price"
    TAX_RATE_NAME = u"tax-rate"
    RULES_NAME = u"rules"
    PROPERTY_NAME = u"property"
    VALUE_NAME = u"value"

    AGE_GROUP_PROPERTY_NAME = u"age-group"
    CATEGORY_PROPERTY_NAME = u"category"

    A_CATEGORY = u"A"
    B_CATEGORY = u"B"
    C_CATEGORY = u"C"
    D_CATEGORY = u"D"
    VALID_CATEGORIES = {A_CATEGORY, B_CATEGORY, C_CATEGORY, D_CATEGORY, None}

    INFANT_AGE_GROUP = u"INFANT"
    KID_AGE_GROUP = u"KID"
    ADULT_AGE_GROUP = u"ADULT"
    VALID_AGE_GROUPS = {KID_AGE_GROUP, ADULT_AGE_GROUP, INFANT_AGE_GROUP}
    AGES_BY_AGE_GROUP = {INFANT_AGE_GROUP: [0, 1, 2], KID_AGE_GROUP: [3, 6, 11], ADULT_AGE_GROUP: [12, 40, 100]}

    ENTITY_DOES_NOT_EXISTS_CODE = PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_CODE
    RESOURCE_URL = u"/clients/{0}/packages/{1}/price-rules/"

    ENTITY_NAME = 'package-price-rules'

    TEST_CLIENT_NAME = "Test client"
    TEST_CLIENT_REQUIRES_LOGIN = True

    TEST_USER_USERNAME = "test_user"
    TEST_USER_PASSWORD = "test_password"
    TEST_USER_ROLE = None

    TEST_PACKAGE_NAME = "Test package"
    TEST_PACKAGE_PRICE = 100.5
    TEST_PACKAGE_TAX_RATE = 19.0
    TEST_PACKAGE_DESCRIPTION = "Test description"
    TEST_PACKAGE_RESTRICTED_CONSUMPTION = True
    TEST_PACKAGE_VALID_FROM = "19900101010101"
    TEST_PACKAGE_VALID_THROUGH = "20100101010101"
    TEST_PACKAGE_DURATION = 5
    TEST_PACKAGE_ID_SOCIAL_EVENT = None

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_PERSON_RESERVATION_ID_RESERVATION = None
    TEST_PERSON_RESERVATION_ID_PERSON = None
    TEST_PERSON_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE
    TEST_PERSON_RESERVATION_INITIAL_DATE = "20100101010101"
    TEST_PERSON_RESERVATION_FINAL_DATE = TEST_PERSON_RESERVATION_INITIAL_DATE

    TEST_RESERVATION_COMPANY = "Test company"
    TEST_RESERVATION_NUMBER_PERSONS = 500
    TEST_RESERVATION_PAYMENT = TEST_PACKAGE_PRICE * TEST_RESERVATION_NUMBER_PERSONS
    TEST_RESERVATION_INITIAL_DATE = TEST_PERSON_RESERVATION_INITIAL_DATE
    TEST_RESERVATION_FINAL_DATE = TEST_PERSON_RESERVATION_INITIAL_DATE

    TEST_PERSON_NAME = "Test person"
    TEST_PERSON_DOCUMENT_TYPE = "CC"
    TEST_PERSON_DOCUMENT_NUMBER = "12345"
    TEST_PERSON_MAIL = "mail@test.com"
    TEST_PERSON_GENDER = "m"
    TEST_PERSON_BIRTHDATE = "19900101"
    TEST_PERSON_CATEGORY = "A"
    TEST_PERSON_AFFILIATION = "Cotizante"
    TEST_PERSON_NATIONALITY = "Colombiano"
    TEST_PERSON_PROFESSION = "Ingeniero"
    TEST_PERSON_CITY_OF_RESIDENCE = "Bogotá"
    TEST_PERSON_COMPANY = "Empresa"

    def setUp(self):
        super(PackagePriceRuleViewTestCase, self).setUp()

        create_test_client(self)
        create_test_location(self)
        self.TEST_PACKAGE_ID_LOCATION = self.expected_ids[LOCATION_ENTITY_NAME]
        create_test_package(self)
        self.TEST_PERSON_RESERVATION_ID_PACKAGE = self.expected_ids[PACKAGE_ENTITY_NAME]

    @classmethod
    def get_static_entity_values_for_create(cls):
        values = dict()
        values[cls.BASE_PRICE_NAME] = 95.5
        values[cls.TAX_RATE_NAME] = 16
        values[cls.RULES_NAME] = [{cls.PROPERTY_NAME: cls.AGE_GROUP_PROPERTY_NAME,
                                   cls.VALUE_NAME: cls.ADULT_AGE_GROUP}]
        return values

    @classmethod
    def get_ancestor_entities_names(cls):
        return [CLIENT_ENTITY_NAME, PACKAGE_ENTITY_NAME]

    def _create_person_reservation_and_check_price_and_tax_rate(self, expected_price, expected_tax_rate):
        create_test_reservation(self, create_new_reservation=True)
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        create_test_person(self, create_new_person=True)
        self.TEST_PERSON_RESERVATION_ID_PERSON = self.expected_ids[PERSON_ENTITY_NAME]
        create_test_person_reservation(self, create_new_person_reservation=True)
        reservation = self.do_get_request(u"/clients/{0}/reservations/{1}/persons-reservations/{2}/".format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                                                                            self.expected_ids[RESERVATION_ENTITY_NAME],
                                                                                                            self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]))
        self.assertEqual(expected_price, reservation[self.BASE_PRICE_NAME])
        self.assertEqual(expected_tax_rate, reservation[self.TAX_RATE_NAME])

    def test_non_existent_client(self):
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE)

    def test_non_existent_package(self):
        self.expected_ids[PACKAGE_ENTITY_NAME] += 1
        self.request_all_resources_and_check_result(0, expected_code=404,
                                                    expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE)

    def test_try_query_non_existent_rule(self):
        results = self.do_get_request(self.get_item_url(type(self))
                                      .format(1), expected_code=404)
        validate_error(self, results, PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_CODE)

    def test_empty_rules_view(self):
        self.request_all_resources_and_check_result(0)

    def test_create_valid_rules(self):
        self.do_create_requests()

    def test_create_valid_rules_with_zero_price(self):
        self.assign_field_value(self.BASE_PRICE_NAME, 0)
        self.do_create_requests()

    def test_create_valid_rules_with_zero_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 0)
        self.do_create_requests()

    def test_create_valid_rules_with_multiple_rules(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()

    def test_create_valid_rules_with_every_possible_age_group(self):
        for age_group in self.VALID_AGE_GROUPS:
            self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                       self.VALUE_NAME: age_group}])
            self.do_create_requests()

    def test_create_valid_rules_with_every_possible_category(self):
        for category in self.VALID_CATEGORIES:
            self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                       self.VALUE_NAME: category}])
            self.do_create_requests()

    def test_create_valid_new_less_restrictive_overlapping_rule(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()

    def test_create_valid_new_equally_restrictive_overlapping_rule_with_different_values(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: None}])
        self.do_create_requests()

    def test_check_person_reservation_base_price_and_tax_rate_are_the_package_ones_when_there_is_no_price_rule(self):
        self._create_person_reservation_and_check_price_and_tax_rate(self.TEST_PACKAGE_PRICE,
                                                                     self.TEST_PACKAGE_TAX_RATE)

    def test_check_person_reservation_base_price_and_tax_rate_are_the_specified_ones_for_category_price_rule(self):
        current_price = self.TEST_PACKAGE_PRICE
        current_tax_rate = self.TEST_PACKAGE_TAX_RATE
        for category in self.VALID_CATEGORIES:
            current_price += 1
            current_tax_rate += 0.5
            self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                       self.VALUE_NAME: category}])
            self.assign_field_value(self.BASE_PRICE_NAME, current_price)
            self.assign_field_value(self.TAX_RATE_NAME, current_tax_rate)
            self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)
            self.TEST_PERSON_CATEGORY = category
            self._create_person_reservation_and_check_price_and_tax_rate(current_price, current_tax_rate)

    def test_check_person_reservation_base_price_and_tax_rate_are_the_specified_ones_for_age_group_price_rule(self):
        current_price = self.TEST_PACKAGE_PRICE
        current_tax_rate = self.TEST_PACKAGE_TAX_RATE
        for age_group in self.VALID_AGE_GROUPS:
            current_price += 1
            current_tax_rate += 0.5
            self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                       self.VALUE_NAME: age_group}])
            self.assign_field_value(self.BASE_PRICE_NAME, current_price)
            self.assign_field_value(self.TAX_RATE_NAME, current_tax_rate)
            self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)
            for age in self.AGES_BY_AGE_GROUP[age_group]:
                from commons.validations import validate_date, DEFAULT_DATE_FORMAT
                now = validate_date(None, "Now", allow_none=True)
                now -= relativedelta(years=age)
                self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
                self._create_person_reservation_and_check_price_and_tax_rate(current_price, current_tax_rate)

    def test_check_person_reservation_base_price_and_tax_rate_are_the_package_ones_when_rules_do_not_apply_for_non_applicable_category_price_rule(self):
        current_price = self.TEST_PACKAGE_PRICE
        current_tax_rate = self.TEST_PACKAGE_TAX_RATE
        for category in self.VALID_CATEGORIES:
            create_test_package(self, create_new_package=True)
            self.TEST_PERSON_RESERVATION_ID_PACKAGE = self.expected_ids[PACKAGE_ENTITY_NAME]
            current_price += 1
            current_tax_rate += 0.5
            self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                       self.VALUE_NAME: category}])
            self.assign_field_value(self.BASE_PRICE_NAME, current_price)
            self.assign_field_value(self.TAX_RATE_NAME, current_tax_rate)
            self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)
            other_categories = [other_category for other_category in self.VALID_CATEGORIES
                                if other_category != category]
            for other_category in other_categories:
                self.TEST_PERSON_CATEGORY = other_category
                self._create_person_reservation_and_check_price_and_tax_rate(self.TEST_PACKAGE_PRICE,
                                                                             self.TEST_PACKAGE_TAX_RATE)

    def test_check_person_reservation_base_price_and_tax_rate_are_the_package_ones_when_rules_do_not_apply_for_non_applicable_age_group_price_rule(self):
        current_price = self.TEST_PACKAGE_PRICE
        current_tax_rate = self.TEST_PACKAGE_TAX_RATE
        for age_group in self.VALID_AGE_GROUPS:
            create_test_package(self, create_new_package=True)
            self.TEST_PERSON_RESERVATION_ID_PACKAGE = self.expected_ids[PACKAGE_ENTITY_NAME]
            current_price += 1
            current_tax_rate += 0.5
            self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                       self.VALUE_NAME: age_group}])
            self.assign_field_value(self.BASE_PRICE_NAME, current_price)
            self.assign_field_value(self.TAX_RATE_NAME, current_tax_rate)
            self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)
            other_age_groups = [other_age_group for other_age_group in self.VALID_AGE_GROUPS
                                if other_age_group != age_group]
            for other_age_group in other_age_groups:
                for age in self.AGES_BY_AGE_GROUP[other_age_group]:
                    from commons.validations import validate_date, DEFAULT_DATE_FORMAT
                    now = validate_date(None, "Now", allow_none=True)
                    now -= relativedelta(years=age)
                    self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
                    self._create_person_reservation_and_check_price_and_tax_rate(self.TEST_PACKAGE_PRICE,
                                                                                 self.TEST_PACKAGE_TAX_RATE)

    def test_check_person_reservation_base_price_and_tax_rate_are_the_specified_ones_for_category_and_age_group_price_rule(self):
        current_price = self.TEST_PACKAGE_PRICE
        current_tax_rate = self.TEST_PACKAGE_TAX_RATE
        for category in self.VALID_CATEGORIES:
            for age_group in self.VALID_AGE_GROUPS:
                current_price += 1
                current_tax_rate += 0.5
                self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                           self.VALUE_NAME: category},
                                                          {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                           self.VALUE_NAME: age_group}])
                self.assign_field_value(self.BASE_PRICE_NAME, current_price)
                self.assign_field_value(self.TAX_RATE_NAME, current_tax_rate)
                self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False)
                for age in self.AGES_BY_AGE_GROUP[age_group]:
                    from commons.validations import validate_date, DEFAULT_DATE_FORMAT
                    now = validate_date(None, "Now", allow_none=True)
                    now -= relativedelta(years=age)
                    self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
                    self.TEST_PERSON_CATEGORY = category
                    self._create_person_reservation_and_check_price_and_tax_rate(current_price, current_tax_rate)

    def test_check_person_reservation_base_price_and_tax_rate_uses_the_more_specific_one_when_applicable_and_less_specific_rule_was_created_first(self):
        price_less_specific = self.TEST_PACKAGE_PRICE + 1
        tax_rate_less_specific = self.TEST_PACKAGE_TAX_RATE + 0.5
        self.assign_field_value(self.BASE_PRICE_NAME, price_less_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_less_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()

        price_more_specific = self.TEST_PACKAGE_PRICE + 2
        tax_rate_more_specific = self.TEST_PACKAGE_TAX_RATE + 1
        self.assign_field_value(self.BASE_PRICE_NAME, price_more_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_more_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY},
                                                  {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()

        from commons.validations import validate_date, DEFAULT_DATE_FORMAT
        now = validate_date(None, "Now", allow_none=True)
        now -= relativedelta(years=self.AGES_BY_AGE_GROUP[self.ADULT_AGE_GROUP][0])
        self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
        self.TEST_PERSON_CATEGORY = self.A_CATEGORY
        self._create_person_reservation_and_check_price_and_tax_rate(price_more_specific, tax_rate_more_specific)

    def test_check_person_reservation_base_price_and_tax_rate_uses_the_more_specific_one_when_applicable_and_less_specific_rule_was_created_last(self):
        price_more_specific = self.TEST_PACKAGE_PRICE + 2
        tax_rate_more_specific = self.TEST_PACKAGE_TAX_RATE + 1
        self.assign_field_value(self.BASE_PRICE_NAME, price_more_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_more_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY},
                                                  {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()

        price_less_specific = self.TEST_PACKAGE_PRICE + 1
        tax_rate_less_specific = self.TEST_PACKAGE_TAX_RATE + 0.5
        self.assign_field_value(self.BASE_PRICE_NAME, price_less_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_less_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()

        from commons.validations import validate_date, DEFAULT_DATE_FORMAT
        now = validate_date(None, "Now", allow_none=True)
        now -= relativedelta(years=self.AGES_BY_AGE_GROUP[self.ADULT_AGE_GROUP][0])
        self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
        self.TEST_PERSON_CATEGORY = self.A_CATEGORY
        self._create_person_reservation_and_check_price_and_tax_rate(price_more_specific, tax_rate_more_specific)

    def test_check_person_reservation_base_price_and_tax_rate_uses_the_less_specific_one_when_more_specific_rule_is_not_applicable_and_less_specific_rule_was_created_first(self):
        price_less_specific = self.TEST_PACKAGE_PRICE + 1
        tax_rate_less_specific = self.TEST_PACKAGE_TAX_RATE + 0.5
        self.assign_field_value(self.BASE_PRICE_NAME, price_less_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_less_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()

        price_more_specific = self.TEST_PACKAGE_PRICE + 2
        tax_rate_more_specific = self.TEST_PACKAGE_TAX_RATE + 1
        self.assign_field_value(self.BASE_PRICE_NAME, price_more_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_more_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY},
                                                  {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()

        from commons.validations import validate_date, DEFAULT_DATE_FORMAT
        now = validate_date(None, "Now", allow_none=True)
        now -= relativedelta(years=self.AGES_BY_AGE_GROUP[self.INFANT_AGE_GROUP][0])
        self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
        self.TEST_PERSON_CATEGORY = self.A_CATEGORY
        self._create_person_reservation_and_check_price_and_tax_rate(price_less_specific, tax_rate_less_specific)

    def test_check_person_reservation_base_price_and_tax_rate_uses_the_less_specific_one_when_applicable_and_less_specific_rule_was_created_last(self):
        price_more_specific = self.TEST_PACKAGE_PRICE + 2
        tax_rate_more_specific = self.TEST_PACKAGE_TAX_RATE + 1
        self.assign_field_value(self.BASE_PRICE_NAME, price_more_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_more_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY},
                                                  {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()

        price_less_specific = self.TEST_PACKAGE_PRICE + 1
        tax_rate_less_specific = self.TEST_PACKAGE_TAX_RATE + 0.5
        self.assign_field_value(self.BASE_PRICE_NAME, price_less_specific)
        self.assign_field_value(self.TAX_RATE_NAME, tax_rate_less_specific)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()

        from commons.validations import validate_date, DEFAULT_DATE_FORMAT
        now = validate_date(None, "Now", allow_none=True)
        now -= relativedelta(years=self.AGES_BY_AGE_GROUP[self.KID_AGE_GROUP][0])
        self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
        self.TEST_PERSON_CATEGORY = self.A_CATEGORY
        self._create_person_reservation_and_check_price_and_tax_rate(price_less_specific, tax_rate_less_specific)

    def test_check_person_reservation_base_price_and_tax_rate_uses_the_package_one_when_multi_property_rule_is_not_applicable_by_category(self):
        self.assign_field_value(self.BASE_PRICE_NAME, self.TEST_PACKAGE_PRICE + 2)
        self.assign_field_value(self.TAX_RATE_NAME, self.TEST_PACKAGE_TAX_RATE + 1)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY},
                                                  {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()

        from commons.validations import validate_date, DEFAULT_DATE_FORMAT
        now = validate_date(None, "Now", allow_none=True)
        now -= relativedelta(years=self.AGES_BY_AGE_GROUP[self.ADULT_AGE_GROUP][0])
        self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
        self.TEST_PERSON_CATEGORY = None
        self._create_person_reservation_and_check_price_and_tax_rate(self.TEST_PACKAGE_PRICE,
                                                                     self.TEST_PACKAGE_TAX_RATE)

    def test_check_person_reservation_base_price_and_tax_rate_uses_the_package_one_when_multi_property_rule_is_not_applicable_by_age(self):
        self.assign_field_value(self.BASE_PRICE_NAME, self.TEST_PACKAGE_PRICE + 2)
        self.assign_field_value(self.TAX_RATE_NAME, self.TEST_PACKAGE_TAX_RATE + 1)
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY},
                                                  {self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.INFANT_AGE_GROUP}])
        self.do_create_requests()

        from commons.validations import validate_date, DEFAULT_DATE_FORMAT
        now = validate_date(None, "Now", allow_none=True)
        now -= relativedelta(years=self.AGES_BY_AGE_GROUP[self.ADULT_AGE_GROUP][0])
        self.TEST_PERSON_BIRTHDATE = now.strftime(DEFAULT_DATE_FORMAT)
        self.TEST_PERSON_CATEGORY = self.A_CATEGORY
        self._create_person_reservation_and_check_price_and_tax_rate(self.TEST_PACKAGE_PRICE,
                                                                     self.TEST_PACKAGE_TAX_RATE)

    def test_try_create_valid_duplicated_rules(self):
        self.do_create_requests()
        self.assign_field_value(self.BASE_PRICE_NAME, 115)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_DUPLICATED_RULE_CODE)

    def test_try_create_invalid_rules_with_non_existent_package(self):
        self.expected_ids[PACKAGE_ENTITY_NAME] += 1
        self.do_create_requests(expected_code=404,
                                expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_try_create_invalid_rules_without_price(self):
        self.assign_field_value(self.BASE_PRICE_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_BASE_PRICE_CODE)

    def test_try_create_invalid_rules_with_negative_price(self):
        self.assign_field_value(self.BASE_PRICE_NAME, -10)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_BASE_PRICE_CODE)

    def test_try_create_invalid_rules_without_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_rules_with_negative_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, -12)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_rules_with_one_hundred_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 100)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_rules_with_over_one_hundred_tax_rate(self):
        self.assign_field_value(self.TAX_RATE_NAME, 112)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_TAX_RATE_CODE)

    def test_try_create_invalid_rules_without_rules(self):
        self.assign_field_value(self.RULES_NAME, None)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_RULES_CODE)

    def test_try_create_invalid_rules_with_empty_rules(self):
        self.assign_field_value(self.RULES_NAME, [])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_RULES_CODE)

    def test_try_create_invalid_rules_without_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: None,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_PROPERTY_CODE)

    def test_try_create_invalid_rules_with_invalid_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: "INVALID_PROPERTY",
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_PROPERTY_CODE)

    def test_try_create_invalid_rules_without_value_for_age_group_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: None}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_CODE)

    def test_try_create_invalid_rules_with_invalid_value_for_age_group_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: "INVALID_VALUE"}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_CODE)

    def test_try_create_invalid_rules_with_valid_category_value_for_age_group_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_CODE)

    def test_try_create_invalid_rules_with_invalid_value_for_category_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: "INVALID_VALUE"}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_CODE)

    def test_try_create_invalid_rules_with_valid_age_group_value_for_category_property(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_VALUE_CODE)

    def test_create_valid_new_more_restrictive_overlapping_rule(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP}])
        self.do_create_requests()
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()

    def test_try_create_valid_duplicated_rules_with_multiple_rules(self):
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        self.do_create_requests()
        self.assign_field_value(self.BASE_PRICE_NAME, 115)
        self.do_create_requests(expected_code=400,
                                expected_internal_code=PACKAGE_PRICE_RULE_INVALID_DUPLICATED_RULE_CODE)

    def test_create_rules_on_package_without_reservations_and_check_historic_id_didnt_change(self):
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.do_create_requests()
        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.assertEqual(original_historic_id, new_historic_id)

    def test_create_rules_on_package_with_reservations_and_check_historic_id_changed_and_old_rules_were_kept(self):
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.do_create_requests()
        self.assign_field_value(self.RULES_NAME, [{self.PROPERTY_NAME: self.AGE_GROUP_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.ADULT_AGE_GROUP},
                                                  {self.PROPERTY_NAME: self.CATEGORY_PROPERTY_NAME,
                                                   self.VALUE_NAME: self.A_CATEGORY}])
        create_test_reservation(self)
        create_test_person_reservation(self)
        self.do_create_requests(do_get_and_check_results=False, check_results_as_list=False,
                                number_of_default_entities=self.NUMBER_OF_ENTITIES)
        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.assertNotEquals(original_historic_id, new_historic_id)
        results = self.do_get_request(self.get_base_url(type(self)))
        self.assertEqual(len(results), 2 * self.NUMBER_OF_ENTITIES)

    def test_delete_rules(self):
        self.do_create_requests()
        self.do_delete_requests()

    def test_delete_invalid_rules_with_non_existent_client(self):
        self.do_create_requests()
        self.expected_ids[CLIENT_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=CLIENT_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_delete_invalid_rules_with_non_existent_package(self):
        self.do_create_requests()
        self.expected_ids[PACKAGE_ENTITY_NAME] += 1
        self.do_delete_requests(expected_code=404, expected_internal_code=PACKAGE_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_delete_invalid_non_existent_rules(self):
        self.do_create_requests()
        self.change_ids_to_non_existent_entities()
        self.do_delete_requests(expected_code=404, expected_internal_code=PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_CODE,
                                do_get_and_check_results=False)

    def test_delete_rules_on_package_without_reservations_and_check_historic_id_didnt_change(self):
        self.do_create_requests()
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.do_delete_requests()
        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.assertEqual(original_historic_id, new_historic_id)

    def test_delete_rules_on_package_with_reservations_and_check_historic_id_changed(self):
        self.do_create_requests()
        original_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                                   .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                           self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        create_test_reservation(self)
        create_test_person_reservation(self)
        self.do_delete_requests(do_get_and_check_results=False, check_deleted=False)
        new_historic_id = self.do_get_request("/clients/{0}/packages/{1}/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PACKAGE_ENTITY_NAME]))[self.PACKAGE_HISTORIC_ID_NAME]
        self.assertNotEquals(original_historic_id, new_historic_id)
        results = self.do_get_request(self.get_base_url(type(self)))
        self.assertEqual(len(results), 0)

    def test_check_permissions_for_create_rules(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        self.check_create_permissions(allowed_roles, required_locations, do_delete_after_success=True)

    def test_check_permissions_for_get_all_ruless(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, \
            CLIENT_SALES_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_QUERY_ROLE, CLIENT_SALES_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.get_base_url(type(self))
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_get_specific_rule(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, \
            CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER, CLIENT_PROMOTER_USER
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE, CLIENT_SALES_ROLE, CLIENT_QUERY_ROLE, CLIENT_CASHIER_USER,
                         CLIENT_PROMOTER_USER}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION}
        url = self.get_item_url(type(self)).format(self.expected_ids[self.ENTITY_NAME])
        self.check_get_permissions(url, allowed_roles, required_locations)

    def test_check_permissions_for_delete_rules(self):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE
        self.do_create_requests()
        allowed_roles = {CLIENT_ADMIN_ROLE}
        required_locations = {self.TEST_PACKAGE_ID_LOCATION, self.expected_ids[LOCATION_ENTITY_NAME]}
        self.check_delete_permissions(allowed_roles, required_locations)


PACKAGE_PRICE_RULES_ENTITY_NAME = PackagePriceRuleViewTestCase.ENTITY_NAME

if __name__ == '__main__':
    unittest.main()
