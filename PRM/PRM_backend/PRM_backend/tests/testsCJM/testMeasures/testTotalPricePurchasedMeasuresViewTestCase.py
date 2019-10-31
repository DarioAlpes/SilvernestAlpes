# -*- coding: utf-8 -*
import unittest

from CJM.entidades.measures import PersonMeasures
from CJM.entidades.measures.Measure import Measure
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
from tests.testsCJM.testEventos.testCompraViewTestCase import create_test_purchase
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class TotalPricePurchasedMeasureViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'measures'
    NUMBER_PERSONS = 5

    NUMBER_SKUS = 1

    TEST_CLIENT_NAME = "Test client"

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    NUMBER_PURCHASES = 5
    TEST_PURCHASE_INITIAL_TIME = "19900101160101"
    TEST_PURCHASE_ID_SKU_TEMPLATE = ["{0}"]
    TEST_PURCHASE_PRICES = [100]
    TEST_PURCHASE_AMOUNTS = [3]
    TEST_PURCHASE_MEASURE_UNITS = ["Test measure unit"]

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
        super(TotalPricePurchasedMeasureViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)
        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self)
        self.initial_time = "19800101160101"
        self.final_time = "20000101160101"
        self.TEST_PURCHASE_ID_SKU = [int(sku_template.format(self.expected_ids[SKU_ENTITY_NAME]))
                                     for sku_template in self.TEST_PURCHASE_ID_SKU_TEMPLATE]

        for purchase_number in range(0, self.NUMBER_PURCHASES):
            create_test_purchase(self, create_new_purchase=True, create_as_event=True)

    def test_calculate_valid_measure_without_final_time(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_valid_measure_without_final_time_and_posterior_initial_time(self):
        self.initial_time = self.final_time
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time))

        self.assertEqual(results[Measure.MEASURE_NAME], 0)

    def test_calculate_valid_measure_without_initial_time(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_valid_measure_without_initial_time_and_previous_final_time(self):
        self.final_time = self.initial_time
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], 0)

    def test_calculate_valid_measure_without_initial_time_and_final_time(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME]))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_valid_measure_with_all_parameters(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}&{6}={7}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time,
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_valid_measure_with_all_parameters_and_posterior_initial_time(self):
        self.initial_time = self.final_time
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}&{6}={7}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time,
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], 0)

    def test_calculate_valid_measure_with_all_parameters_and_previous_final_time(self):
        self.final_time = self.initial_time
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}&{6}={7}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time,
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], 0)

    def test_calculate_valid_measure_with_all_parameters_and_exact_purchase_time(self):
        self.initial_time = self.TEST_PURCHASE_INITIAL_TIME
        self.final_time = self.TEST_PURCHASE_INITIAL_TIME
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}&{6}={7}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time,
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_valid_measure_with_second_final_time_format(self):
        self.final_time = "February 01, 2000 at 01:01AM"
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}&{6}={7}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time,
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_valid_measure_with_second_initial_time_format(self):
        self.initial_time = "February 01, 1980 at 01:01AM"
        results = self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                                      "?{2}={3}&{4}={5}&{6}={7}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME],
                                              PersonMeasures.SKU_ID_NAME,
                                              self.expected_ids[SKU_ENTITY_NAME],
                                              PersonMeasures.INITIAL_TIME_NAME,
                                              self.initial_time,
                                              PersonMeasures.FINAL_TIME_NAME,
                                              self.final_time))

        self.assertEqual(results[Measure.MEASURE_NAME], self.TEST_PURCHASE_AMOUNTS[0] * self.NUMBER_PURCHASES *
                         self.TEST_PURCHASE_PRICES[0])

    def test_calculate_invalid_measure_with_invalid_final_time(self):
        self.final_time = "INVALID TIME"
        self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                            "?{2}={3}&{4}={5}&{6}={7}"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.expected_ids[PERSON_ENTITY_NAME],
                                    PersonMeasures.SKU_ID_NAME,
                                    self.expected_ids[SKU_ENTITY_NAME],
                                    PersonMeasures.INITIAL_TIME_NAME,
                                    self.initial_time,
                                    PersonMeasures.FINAL_TIME_NAME,
                                    self.final_time), expected_code=400)

    def test_calculate_invalid_measure_with_invalid_initial_time(self):
        self.initial_time = "INVALID TIME"
        self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                            "?{2}={3}&{4}={5}&{6}={7}"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.expected_ids[PERSON_ENTITY_NAME],
                                    PersonMeasures.SKU_ID_NAME,
                                    self.expected_ids[SKU_ENTITY_NAME],
                                    PersonMeasures.INITIAL_TIME_NAME,
                                    self.initial_time,
                                    PersonMeasures.FINAL_TIME_NAME,
                                    self.final_time), expected_code=400)

    def test_calculate_invalid_measure_without_sku(self):
        self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                            "?{2}={3}&{4}={5}"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.expected_ids[PERSON_ENTITY_NAME],
                                    PersonMeasures.INITIAL_TIME_NAME,
                                    self.initial_time,
                                    PersonMeasures.FINAL_TIME_NAME,
                                    self.final_time), expected_code=404)

    def test_calculate_invalid_measure_with_invalid_sku(self):
        self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                            "?{2}={3}&{4}={5}&{6}={7}"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.expected_ids[PERSON_ENTITY_NAME],
                                    PersonMeasures.SKU_ID_NAME,
                                    "INVALID_SKU",
                                    PersonMeasures.INITIAL_TIME_NAME,
                                    self.initial_time,
                                    PersonMeasures.FINAL_TIME_NAME,
                                    self.final_time), expected_code=404)

    def test_calculate_invalid_measure_with_non_existent_sku(self):
        self.do_get_request("/clients/{0}/persons/{1}/measures/total-price-purchased/"
                            "?{2}={3}&{4}={5}&{6}={7}"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                    self.expected_ids[PERSON_ENTITY_NAME],
                                    PersonMeasures.SKU_ID_NAME,
                                    self.expected_ids[SKU_ENTITY_NAME] + 1,
                                    PersonMeasures.INITIAL_TIME_NAME,
                                    self.initial_time,
                                    PersonMeasures.FINAL_TIME_NAME,
                                    self.final_time), expected_code=404)


if __name__ == '__main__':
    unittest.main()
