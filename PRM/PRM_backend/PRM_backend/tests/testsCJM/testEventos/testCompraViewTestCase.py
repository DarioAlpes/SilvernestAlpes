# -*- coding: utf-8 -*
import unittest

from CJM.entidades.eventos.Compra import Compra
from CJM.entidades.eventos.TipoEvento import TipoEvento
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testsCJM.testEventos import EVENT_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class CompraViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'compras'
    NUMBER_PURCHASES = 1

    NUMBER_SKUS = 2

    TEST_CLIENT_NAME = "Test client"
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

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    def setUp(self):
        super(CompraViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)
        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

        self.initial_time = "19900101160101"
        self.id_skus = []
        self.prices = []
        self.amounts = []

        for sku_number in range(0, self.NUMBER_SKUS):
            create_test_sku(self, create_new_sku=True)
            self.id_skus.append(self.expected_ids[SKU_ENTITY_NAME])
            self.prices.append(100.5)
            self.amounts.append(3)

    def test_non_existent_person(self):
        id_not_existent_person = self.expected_ids[PERSON_ENTITY_NAME] + 1
        self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME], id_not_existent_person), expected_code=404)

    def test_emptypurchases_view(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 0)

    def test_create_valid_purchases(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_create_valid_purchases_without_initial_time(self):
        self.initial_time = None
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_create_valid_purchases_with_single_items(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = self.expected_ids[SKU_ENTITY_NAME]
        self.prices = 100.5
        self.amounts = 3
        self.create_test_purchases()
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_create_invalid_purchases_with_single_items_and_invalid_sku(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = "invalid sku"
        self.prices = 100.5
        self.amounts = 3
        self.create_test_purchases(expected_code=404)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_single_items_and_non_existent_sku(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = self.expected_ids[SKU_ENTITY_NAME] + 1
        self.prices = 100.5
        self.amounts = 3
        self.create_test_purchases(expected_code=404)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_single_items_and_invalid_price(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = self.expected_ids[SKU_ENTITY_NAME]
        self.prices = -1
        self.amounts = 3
        self.create_test_purchases(expected_code=400)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_single_items_and_invalid_amount(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = self.expected_ids[SKU_ENTITY_NAME]
        self.prices = 100.5
        self.amounts = -1
        self.create_test_purchases(expected_code=400)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_single_items_for_all_but_skus(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.prices = 100.5
        self.amounts = 3
        self.create_test_purchases(expected_code=400)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_single_items_for_all_but_prices(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = self.expected_ids[SKU_ENTITY_NAME]
        self.amounts = 3
        self.create_test_purchases(expected_code=400)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_single_items_for_all_but_amounts(self):
        previous_number_skus = self.NUMBER_SKUS
        self.NUMBER_SKUS = 1
        self.id_skus = self.expected_ids[SKU_ENTITY_NAME]
        self.prices = 100.5
        self.create_test_purchases(expected_code=400)
        self.NUMBER_SKUS = previous_number_skus
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_valid_purchases_with_second_time_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:47AM"
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
    
    def test_create_invalid_purchases_without_initial_time(self):
        self.initial_time = ""
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_month_for_initial_time(self):
        self.initial_time = "19902001160101"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_days_for_initial_time(self):
        self.initial_time = "19900230160101"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_hours_for_initial_time(self):
        self.initial_time = "19900202300101"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_minutes_for_initial_time(self):
        self.initial_time = "19900202206101"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_seconds_for_initial_time(self):
        self.initial_time = "19900202200161"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_format_for_initial_time(self):
        self.initial_time = "INVALID_DATETIME"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_month_with_second_format_for_initial_time(self):
        self.initial_time = "INVALID 01, 2016 at 11:47AM"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_day_with_second_format_for_initial_time(self):
        self.initial_time = "June 40, 2016 at 11:47AM"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_hour_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 13:47AM"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_minute_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:62AM"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)
    
    def test_create_invalid_purchases_with_invalid_ampm_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:30CM"
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_empty_lists(self):
        self.id_skus = []
        self.prices = []
        self.amounts = []
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_without_skus(self):
        self.id_skus = ""
        self.create_test_purchases(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_extra_sku(self):
        create_test_sku(self, create_new_sku=True)
        self.id_skus.append(self.expected_ids[SKU_ENTITY_NAME])
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_missing_sku(self):
        self.id_skus = self.id_skus[0:-1]
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_invalid_sku(self):
        self.id_skus[0] = self.expected_ids[SKU_ENTITY_NAME] + 1
        self.create_test_purchases(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_without_prices(self):
        self.prices = ""
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_extra_price(self):
        self.prices.append(15)
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_missing_price(self):
        self.prices = self.prices[0:-1]
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_invalid_price(self):
        self.prices[0] = 0
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_without_amounts(self):
        self.amounts = ""
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_extra_amount(self):
        self.amounts.append(15)
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_missing_amount(self):
        self.amounts = self.amounts[0:-1]
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_purchases_with_invalid_amount(self):
        self.amounts[0] = 0
        self.create_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_purchases_on_multiple_persons(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

        current_purchase_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()
        self.expected_ids[self.ENTITY_NAME] = current_purchase_id
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        create_test_person(self, create_new_person=True)
        self.prepend_to_templates(str(self.expected_ids[PERSON_ENTITY_NAME]))

        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_delete_purchases(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

        self.delete_test_purchases()

    def test_delete_invalid_non_existent_purchases(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

        self.delete_test_purchases(offset=self.NUMBER_PURCHASES, expected_code=404)

    def test_delete_purchases_as_events(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

        self.delete_test_purchases(delete_as_event=True)

    def test_delete_invalid_non_existent_purchases_as_events(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

        self.delete_test_purchases(offset=self.NUMBER_PURCHASES, delete_as_event=True, expected_code=404)

    def test_update_valid_purchases(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = "20000101160101"
        self.new_id_skus = self.id_skus
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_valid_purchases_without_initial_time(self):
        self.initial_time = None
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = None
        self.new_id_skus = self.id_skus
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_invalid_non_existente_purchases(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = "20000101160101"
        self.new_id_skus = self.id_skus
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases(offset=self.NUMBER_PURCHASES, expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_invalid_purchases_with_empty_initial_time(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = ""
        self.new_id_skus = self.id_skus
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_invalid_purchases_with_invalid_initial_time(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = "INVALID_TIME"
        self.new_id_skus = self.id_skus
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_invalid_purchases_with_invalid_prices(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = "20000101160101"
        self.new_id_skus = self.id_skus
        self.new_prices = [-price for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_invalid_purchases_with_invalid_amounts(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = "20000101160101"
        self.new_id_skus = self.id_skus
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [-amount for amount in self.amounts]

        self.update_test_purchases(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def test_update_invalid_purchases_with_invalid_skus(self):
        self.create_test_purchases()
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)
        self.new_initial_time = "20000101160101"
        self.new_id_skus = [-sku + 10 for sku in self.id_skus]
        self.new_prices = [price + 10 for price in self.prices]
        self.new_amounts = [amount + 10 for amount in self.amounts]

        self.update_test_purchases(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES)

    def delete_test_purchases(self, offset=0, delete_as_event=False, expected_code=200):
        for purchase_number in range(0, self.NUMBER_PURCHASES):
            delete_purchase(self,
                            purchase_number + offset,
                            self.initial_time,
                            self.id_skus,
                            self.prices,
                            self.amounts,
                            delete_as_event=delete_as_event,
                            expected_code=expected_code)

            if expected_code == 200:
                results = self.do_get_request("/clients/{0}/persons/{1}/purchases/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PERSON_ENTITY_NAME]))
                if purchase_number + 1 == self.NUMBER_PURCHASES:
                    from commons.validations import DEFAULT_DATETIME_FORMAT
                    date_initial_time = validate_datetime(self.initial_time, Compra.INITIAL_TIME_NAME)
                    initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)

                    expected_number = sum(self.amounts)
                    expected_description = u",".join([self.TEST_SKU_NAME] * self.NUMBER_SKUS)

                    self.remove_data_value(self.ENTITY_NAME, Compra.INITIAL_TIME_NAME, initial_time)
                    self.remove_data_value(self.ENTITY_NAME, Compra.SKU_IDS_NAME, self.id_skus)
                    self.remove_data_value(self.ENTITY_NAME, Compra.PRICES_NAME, self.prices)
                    self.remove_data_value(self.ENTITY_NAME, Compra.AMOUNTS_NAME, self.amounts)
                    self.remove_data_value(self.ENTITY_NAME, Compra.DESCRIPTION_NAME, expected_description)
                    self.remove_data_value(self.ENTITY_NAME, Compra.NUMBER_NAME, expected_number)
                    self.remove_data_value(self.ENTITY_NAME, Compra.TYPE_NAME, TipoEvento.STRING_PURCHASE)

                self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_PURCHASES - purchase_number - 1)

    def update_test_purchases(self, offset=0, expected_code=200):
        for purchase_number in range(0, self.NUMBER_PURCHASES):
            update_purchase(self,
                            purchase_number + offset,
                            self.new_initial_time,
                            self.new_id_skus,
                            self.new_prices,
                            self.new_amounts,
                            self.initial_time,
                            self.id_skus,
                            self.prices,
                            self.amounts,
                            expected_code=expected_code)

    def create_test_purchases(self, expected_code=200):
        for feedback_number in range(0, self.NUMBER_PURCHASES):
            create_purchase(self,
                            self.initial_time,
                            self.id_skus,
                            self.prices,
                            self.amounts,
                            expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        pass

PURCHASE_ENTITY_NAME = CompraViewTestCase.ENTITY_NAME


def create_purchase(test_class, initial_time, id_skus, prices, amounts, expected_code=200):
    create_test_person(test_class)

    if CompraViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[CompraViewTestCase.ENTITY_NAME] = CompraViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[CompraViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/clients/{0}/persons/{1}/purchases/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                test_class.expected_ids[PERSON_ENTITY_NAME]),
                                        data={Compra.INITIAL_TIME_NAME: initial_time,
                                              Compra.SKU_IDS_NAME: id_skus,
                                              Compra.PRICES_NAME: prices,
                                              Compra.AMOUNTS_NAME: amounts},
                                        expected_code=expected_code)
    if expected_code == 200:
        if not isinstance(prices, list):
            prices = [prices]

        if not isinstance(amounts, list):
            amounts = [amounts]

        if not isinstance(id_skus, list):
            id_skus = [id_skus]

        if initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_initial_time = validate_datetime(initial_time, Compra.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.INITIAL_TIME_NAME, initial_time)

        expected_number = sum(amounts)
        expected_type = TipoEvento.STRING_PURCHASE
        expected_description = u",".join([test_class.TEST_SKU_NAME] * test_class.NUMBER_SKUS)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.SKU_IDS_NAME, id_skus)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.PRICES_NAME, prices)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.AMOUNTS_NAME, amounts)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.TYPE_NAME, expected_type)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.NUMBER_NAME, expected_number)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.DESCRIPTION_NAME, expected_description)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME,
                                  Compra.ID_NAME, test_class.expected_ids[CompraViewTestCase.ENTITY_NAME])

        validate_purchase(test_class, result, initial_time, id_skus, prices, amounts,
                          expected_type, expected_number, expected_description,
                          test_class.expected_ids[CompraViewTestCase.ENTITY_NAME])

        validate_purchase(test_class,
                          test_class.do_get_request("/clients/{0}/persons/{1}/purchases/{2}/".
                                                    format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                           test_class.expected_ids[PERSON_ENTITY_NAME],
                                                           test_class.expected_ids[CompraViewTestCase.ENTITY_NAME])),
                          initial_time, id_skus, prices, amounts,
                          expected_type, expected_number, expected_description,
                          test_class.expected_ids[CompraViewTestCase.ENTITY_NAME])
    return result


def update_purchase(test_class, purchase_number, initial_time, id_skus, prices, amounts,
                    old_initial_time, old_id_skus, old_prices, old_amounts,
                    expected_code=200):
    id_purchase = CompraViewTestCase.STARTING_ID + purchase_number

    result = test_class.do_put_request("/clients/{0}/persons/{1}/purchases/{2}/"
                                       .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                               id_purchase),
                                       data={Compra.INITIAL_TIME_NAME: initial_time,
                                             Compra.SKU_IDS_NAME: id_skus,
                                             Compra.PRICES_NAME: prices,
                                             Compra.AMOUNTS_NAME: amounts},
                                       expected_code=expected_code)

    if expected_code == 200:
        if initial_time is not None and old_initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT

            date_old_initial_time = validate_datetime(old_initial_time, Compra.INITIAL_TIME_NAME)
            old_initial_time = date_old_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.INITIAL_TIME_NAME, old_initial_time)

            date_initial_time = validate_datetime(initial_time, Compra.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.INITIAL_TIME_NAME, initial_time)

        if not isinstance(prices, list):
            prices = [prices]

        if not isinstance(amounts, list):
            amounts = [amounts]

        if not isinstance(id_skus, list):
            id_skus = [id_skus]

        if not isinstance(old_prices, list):
            old_prices = [old_prices]

        if not isinstance(old_amounts, list):
            old_amounts = [old_amounts]

        if not isinstance(old_id_skus, list):
            old_id_skus = [old_id_skus]

        expected_number = sum(amounts)
        old_expected_number = sum(old_amounts)

        expected_description = u",".join([test_class.TEST_SKU_NAME] * test_class.NUMBER_SKUS)
        old_expected_description = u",".join([test_class.TEST_SKU_NAME] * test_class.NUMBER_SKUS)

        expected_type = TipoEvento.STRING_PURCHASE

        test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.SKU_IDS_NAME, old_id_skus)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.SKU_IDS_NAME, id_skus)

        test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.DESCRIPTION_NAME, old_expected_description)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.DESCRIPTION_NAME, expected_description)

        test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.AMOUNTS_NAME, old_amounts)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.AMOUNTS_NAME, amounts)

        test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.NUMBER_NAME, old_expected_number)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.NUMBER_NAME, expected_number)

        test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.PRICES_NAME, old_prices)
        test_class.add_data_value(CompraViewTestCase.ENTITY_NAME, Compra.PRICES_NAME, prices)

        validate_purchase(test_class, result, initial_time, id_skus, prices, amounts,
                          expected_type, expected_number, expected_description,
                          id_purchase)

        validate_purchase(test_class,
                          test_class.do_get_request("/clients/{0}/persons/{1}/purchases/{2}/".
                                                    format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                           test_class.expected_ids[PERSON_ENTITY_NAME],
                                                           id_purchase)),
                          initial_time, id_skus, prices, amounts,
                          expected_type, expected_number, expected_description,
                          id_purchase)
    return result


def delete_purchase(test_class, purchase_number, initial_time, id_skus, prices, amounts,
                    delete_as_event=False, expected_code=200):
    id_purchase = CompraViewTestCase.STARTING_ID + purchase_number

    if delete_as_event:
        url = "/clients/{0}/persons/{1}/purchases/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                                               id_purchase)
    else:
        url = "/clients/{0}/persons/{1}/events/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                            test_class.expected_ids[PERSON_ENTITY_NAME],
                                                            id_purchase)
    result = test_class.do_delete_request(url, expected_code=expected_code)
    if expected_code == 200:
        if not isinstance(prices, list):
            prices = [prices]

        if not isinstance(amounts, list):
            amounts = [amounts]

        if not isinstance(id_skus, list):
            id_skus = [id_skus]

        expected_number = sum(amounts)

        expected_type = TipoEvento.STRING_PURCHASE

        expected_description = u",".join([test_class.TEST_SKU_NAME] * test_class.NUMBER_SKUS)

        test_class.remove_data_value(CompraViewTestCase.ENTITY_NAME, Compra.ID_NAME, id_purchase)

        validate_purchase(test_class, result, initial_time, id_skus, prices, amounts,
                          expected_type, expected_number, expected_description,
                          id_purchase)

        test_class.do_get_request(url, expected_code=404)
    return result


def validate_purchase(test_class, result, initial_time, id_skus, prices, amounts,
                      expected_type, expected_number, expected_description, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    if initial_time is not None:
        test_class.assertEqual(result[Compra.INITIAL_TIME_NAME], initial_time)
    test_class.assertEqual(result[Compra.SKU_IDS_NAME], id_skus)
    test_class.assertEqual(result[Compra.PRICES_NAME], prices)
    test_class.assertEqual(result[Compra.AMOUNTS_NAME], amounts)
    test_class.assertEqual(result[Compra.TYPE_NAME], expected_type)
    test_class.assertEqual(result[Compra.NUMBER_NAME], expected_number)
    test_class.assertEqual(result[Compra.DESCRIPTION_NAME], expected_description)
    test_class.assertEqual(result[Compra.ID_NAME], expected_id)


def create_test_purchase(test_class, create_new_purchase=False, create_as_event=False):
    original_entity_name = CompraViewTestCase.ENTITY_NAME
    if create_as_event:
        CompraViewTestCase.ENTITY_NAME = EVENT_ENTITY_NAME
    result = None

    if CompraViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_purchase:
        result = create_purchase(test_class,
                                 test_class.TEST_PURCHASE_INITIAL_TIME,
                                 test_class.TEST_PURCHASE_ID_SKU,
                                 test_class.TEST_PURCHASE_PRICES,
                                 test_class.TEST_PURCHASE_AMOUNTS)[Compra.ID_NAME]

    CompraViewTestCase.ENTITY_NAME = original_entity_name
    return result

if __name__ == '__main__':
    unittest.main()
