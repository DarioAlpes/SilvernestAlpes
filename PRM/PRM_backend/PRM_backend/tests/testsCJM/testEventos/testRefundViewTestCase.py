# -*- coding: utf-8 -*
import unittest

from CJM.entidades.eventos.Devolucion import Devolucion
from CJM.entidades.eventos.TipoEvento import TipoEvento
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testEventos import EVENT_ENTITY_NAME
from tests.testsCJM.testEventos.testAccionViewTestCase import create_test_action
from tests.testsCJM.testEventos.testCompraViewTestCase import create_test_purchase
from tests.testsCJM.testEventos.testFeedbackViewTestCase import create_test_feedback
from tests.testsCJM.testEventos.testPedidoViewTestCase import create_test_order
from tests.testsCJM.testEventos.testVisitaViewTestCase import create_test_visit
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class DevolucionViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'devoluciones'
    NUMBER_REFUNDS = 1

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None
    NUMBER_PERSONS = 5
    NUMBER_SKUS = 1

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

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

    NUMBER_ACTIONS = 5
    TEST_ACTION_INITIAL_TIME = "19900101160101"
    TEST_ACTION_NAME = "nombre{0}"
    TEST_ACTION_AMOUNT = 5

    NUMBER_PURCHASES = 5
    TEST_PURCHASE_INITIAL_TIME = "19900101160101"
    TEST_PURCHASE_ID_SKU_TEMPLATE = ["{0}"]
    TEST_PURCHASE_PRICES = [100]
    TEST_PURCHASE_AMOUNTS = [3]

    NUMBER_ORDERS = 5
    TEST_ORDER_INITIAL_TIME = "19900101160101"
    TEST_ORDER_ID_SKU_TEMPLATE = ["{0}"]
    TEST_ORDER_AMOUNTS = [3]

    NUMBER_FEEDBACKS = 5
    TEST_FEEDBACK_INITIAL_TIME = "19900101160101"
    TEST_FEEDBACK_TEXT = "Test text"
    TEST_FEEDBACK_SCORE = 50
    TEST_FEEDBACK_ID_LOCATION_TEMPLATE = "{0}"
    TEST_FEEDBACK_ID_SKU_TEMPLATE = "{0}"

    NUMBER_VISITS = 5
    TEST_VISIT_INITIAL_TIME = "19900101160101"
    TEST_VISIT_FINAL_TIME = "19900101160201"
    TEST_VISIT_ID_LOCATION_TEMPLATE = "{0}"

    def setUp(self):
        super(DevolucionViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)
        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

        self.ids_refunds = []
        for action_number in range(0, self.NUMBER_ACTIONS):
            create_test_action(self, create_new_action=True, create_as_event=True)
            self.ids_refunds.append(self.expected_ids[EVENT_ENTITY_NAME])

        self.ids_purchases = []
        create_test_sku(self)
        self.TEST_PURCHASE_ID_SKU = [int(sku_template.format(self.expected_ids[SKU_ENTITY_NAME]))
                                     for sku_template in self.TEST_PURCHASE_ID_SKU_TEMPLATE]
        for purchase_number in range(0, self.NUMBER_PURCHASES):
            create_test_purchase(self, create_new_purchase=True, create_as_event=True)
            self.ids_purchases.append(self.expected_ids[EVENT_ENTITY_NAME])

        self.ids_feedbacks = []
        create_test_location(self)
        self.TEST_FEEDBACK_ID_LOCATION = int(self.TEST_FEEDBACK_ID_LOCATION_TEMPLATE.
                                             format(self.expected_ids[LOCATION_ENTITY_NAME]))
        self.TEST_FEEDBACK_ID_SKU = int(self.TEST_FEEDBACK_ID_SKU_TEMPLATE.
                                        format(self.expected_ids[SKU_ENTITY_NAME]))
        for feedback_number in range(0, self.NUMBER_FEEDBACKS):
            create_test_feedback(self, create_new_feedback=True, create_as_event=True)
            self.ids_feedbacks.append(self.expected_ids[EVENT_ENTITY_NAME])

        self.ids_visits = []
        self.TEST_VISIT_ID_LOCATION = int(self.TEST_VISIT_ID_LOCATION_TEMPLATE.
                                          format(self.expected_ids[LOCATION_ENTITY_NAME]))
        for visit_number in range(0, self.NUMBER_VISITS):
            create_test_visit(self, create_new_visit=True, create_as_event=True)
            self.ids_visits.append(self.expected_ids[EVENT_ENTITY_NAME])

        self.ids_orders = []
        self.TEST_ORDER_ID_SKU = [int(sku_template.format(self.expected_ids[SKU_ENTITY_NAME]))
                                  for sku_template in self.TEST_ORDER_ID_SKU_TEMPLATE]
        for order_number in range(0, self.NUMBER_ORDERS):
            create_test_order(self, create_new_order=True, create_as_event=True)
            self.ids_orders.append(self.expected_ids[EVENT_ENTITY_NAME])

        self.initial_time = "19900101160101"

    def test_non_existent_person(self):
        id_not_existent_person = self.expected_ids[PERSON_ENTITY_NAME] + 1
        self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME], id_not_existent_person), expected_code=404)

    def test_empty_refunds_view(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 0)

    def test_create_valid_refunds_for_visits(self):
        self.ids_events_to_refund = self.ids_visits
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_valid_refunds_for_refunds(self):
        self.ids_events_to_refund = self.ids_refunds
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_valid_refunds_for_feedbacks(self):
        self.ids_events_to_refund = self.ids_feedbacks
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_valid_refunds_for_orders(self):
        self.ids_events_to_refund = self.ids_orders
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_valid_refunds_for_purchases(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_valid_refunds_without_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = None
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_valid_refunds_with_second_time_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "June 01, 2016 at 11:47AM"
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_create_invalid_refunds_without_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = ""
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_month_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "19902001160101"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_days_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "19900230160101"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_hours_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "19900202300101"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_minutes_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "19900202206101"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_seconds_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "19900202200161"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "INVALID_DATETIME"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_month_with_second_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "INVALID 01, 2016 at 11:47AM"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_day_with_second_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "June 40, 2016 at 11:47AM"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_hour_with_second_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "June 01, 2016 at 13:47AM"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_minute_with_second_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "June 01, 2016 at 11:62AM"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_with_invalid_ampm_with_second_format_for_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = "June 01, 2016 at 11:30CM"
        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_without_event_to_return(self):
        self.ids_events_to_refund = None
        self.create_test_refunds(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_refunds_on_already_refunded_events(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.create_test_refunds(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_delete_refund(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.delete_test_refunds()

    def test_delete_invalid_non_existent_refund(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.delete_test_refunds(offset=self.NUMBER_REFUNDS, expected_code=404)

    def test_delete_refund_as_events(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.delete_test_refunds(delete_as_event=True)

    def test_delete_invalid_non_existent_refund_as_events(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.delete_test_refunds(offset=self.NUMBER_REFUNDS, delete_as_event=True, expected_code=404)

    def test_update_valid_refunds(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.new_initial_time = "20000101160101"
        self.new_ids_events_to_refund = self.ids_visits

        self.update_test_refunds()

        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_update_invalid_non_existent_refunds(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.new_initial_time = "20000101160101"
        self.new_ids_events_to_refund = self.ids_visits

        self.update_test_refunds(offset=self.NUMBER_REFUNDS, expected_code=404)

        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_update_valid_refunds_without_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.initial_time = None
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.new_initial_time = None
        self.new_ids_events_to_refund = self.ids_visits

        self.update_test_refunds()

        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_update_invalid_refunds_with_empty_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.new_initial_time = ""
        self.new_ids_events_to_refund = self.ids_visits

        self.update_test_refunds(expected_code=400)

        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_update_invalid_refunds_with_invalid_initial_time(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.new_initial_time = "20000230160101"
        self.new_ids_events_to_refund = self.ids_visits

        self.update_test_refunds(expected_code=400)

        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def test_update_invalid_refunds_without_events_to_refund(self):
        self.ids_events_to_refund = self.ids_purchases
        self.create_test_refunds()
        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

        self.new_initial_time = "20000101160101"
        self.new_ids_events_to_refund = None

        self.update_test_refunds(expected_code=404)

        results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS)

    def delete_test_refunds(self, offset=0, delete_as_event=False, expected_code=200):
        for refund_number in range(0, self.NUMBER_REFUNDS):
            if self.ids_events_to_refund is None:
                self.id_event_to_refund = None
            else:
                self.id_event_to_refund = self.ids_events_to_refund[refund_number]
            delete_refund(self,
                          refund_number + offset,
                          self.initial_time,
                          self.id_event_to_refund,
                          delete_as_event=delete_as_event,
                          expected_code=expected_code)

            if expected_code == 200:
                results = self.do_get_request("/clients/{0}/persons/{1}/refunds/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PERSON_ENTITY_NAME]))
                self.remove_data_value(self.ENTITY_NAME, Devolucion.ID_EVENT_NAME, self.id_event_to_refund)
                if refund_number + 1 == self.NUMBER_REFUNDS:
                    from commons.validations import DEFAULT_DATETIME_FORMAT
                    date_initial_time = validate_datetime(self.initial_time, Devolucion.INITIAL_TIME_NAME)
                    initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)

                    self.remove_data_value(self.ENTITY_NAME, Devolucion.INITIAL_TIME_NAME, initial_time)
                    self.remove_data_value(self.ENTITY_NAME, Devolucion.TYPE_NAME, TipoEvento.STRING_REFUND)

                self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_REFUNDS - refund_number - 1)

    def update_test_refunds(self, offset=0, expected_code=200):
        for refund_number in range(0, self.NUMBER_REFUNDS):
            if self.ids_events_to_refund is None:
                self.id_event_to_refund = None
            else:
                self.id_event_to_refund = self.ids_events_to_refund[refund_number]

            if self.new_ids_events_to_refund is None:
                self.new_id_event_to_refund = None
            else:
                self.new_id_event_to_refund = self.new_ids_events_to_refund[refund_number]

            update_refund(self,
                          refund_number + offset,
                          self.new_initial_time,
                          self.new_id_event_to_refund,
                          self.initial_time,
                          self.id_event_to_refund,
                          expected_code=expected_code)

    def create_test_refunds(self, expected_code=200):
        for refund_number in range(0, self.NUMBER_REFUNDS):
            if self.ids_events_to_refund is None:
                self.id_event_to_refund = None
            else:
                self.id_event_to_refund = self.ids_events_to_refund[refund_number]
            create_refund(self,
                          self.initial_time,
                          self.id_event_to_refund,
                          expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        pass

REFUND_ENTITY_NAME = DevolucionViewTestCase.ENTITY_NAME


def create_refund(test_class, initial_time, id_event_to_refund, expected_code=200):
    create_test_person(test_class)

    if DevolucionViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[DevolucionViewTestCase.ENTITY_NAME] = DevolucionViewTestCase.STARTING_ID \
                                                                      + test_class.expected_ids[EVENT_ENTITY_NAME]
    else:
        test_class.expected_ids[DevolucionViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/clients/{0}/persons/{1}/refunds/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                test_class.expected_ids[PERSON_ENTITY_NAME]),
                                        data={Devolucion.INITIAL_TIME_NAME: initial_time,
                                              Devolucion.EVENT_ID_NAME: id_event_to_refund},
                                        expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_initial_time = validate_datetime(initial_time, Devolucion.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.INITIAL_TIME_NAME, initial_time)

        expected_type = TipoEvento.STRING_REFUND

        test_class.add_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.EVENT_ID_NAME, id_event_to_refund)
        test_class.add_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.TYPE_NAME, expected_type)
        test_class.add_data_value(DevolucionViewTestCase.ENTITY_NAME,
                                  Devolucion.ID_NAME, test_class.expected_ids[DevolucionViewTestCase.ENTITY_NAME])

        validate_refund(test_class, result, initial_time, expected_type, id_event_to_refund,
                        test_class.expected_ids[DevolucionViewTestCase.ENTITY_NAME])

        validate_refund(test_class,
                        test_class.do_get_request("/clients/{0}/persons/{1}/refunds/{2}/".
                                                  format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                         test_class.expected_ids[PERSON_ENTITY_NAME],
                                                         test_class.expected_ids[DevolucionViewTestCase.ENTITY_NAME])),
                        initial_time, expected_type, id_event_to_refund,
                        test_class.expected_ids[DevolucionViewTestCase.ENTITY_NAME])
    return result


def delete_refund(test_class, refund_number, initial_time, id_event_to_refund, delete_as_event=False,
                  expected_code=200):
    id_refund = DevolucionViewTestCase.STARTING_ID + refund_number + test_class.expected_ids[EVENT_ENTITY_NAME]

    if delete_as_event:
        url = "/clients/{0}/persons/{1}/refunds/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                             test_class.expected_ids[PERSON_ENTITY_NAME],
                                                             id_refund)
    else:
        url = "/clients/{0}/persons/{1}/events/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                            test_class.expected_ids[PERSON_ENTITY_NAME],
                                                            id_refund)

    result = test_class.do_delete_request(url,
                                          expected_code=expected_code)
    if expected_code == 200:

        expected_type = TipoEvento.STRING_REFUND

        test_class.remove_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.ID_NAME, id_refund)
        test_class.remove_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.EVENT_ID_NAME, id_event_to_refund)

        validate_refund(test_class, result, initial_time, expected_type, id_event_to_refund, id_refund)

        test_class.do_get_request(url, expected_code=404)
    return result


def update_refund(test_class, refund_number, initial_time, id_event_to_refund,
                  old_initial_time, old_id_event_to_refund, expected_code=200):
    id_refund = DevolucionViewTestCase.STARTING_ID + refund_number + test_class.expected_ids[EVENT_ENTITY_NAME]

    result = test_class.do_put_request("/clients/{0}/persons/{1}/refunds/{2}/"
                                       .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                               id_refund),
                                       data={Devolucion.INITIAL_TIME_NAME: initial_time,
                                             Devolucion.EVENT_ID_NAME: id_event_to_refund},
                                       expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None and old_initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_old_initial_time = validate_datetime(old_initial_time, Devolucion.INITIAL_TIME_NAME)
            old_initial_time = date_old_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.remove_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.INITIAL_TIME_NAME,
                                         old_initial_time)

            date_initial_time = validate_datetime(initial_time, Devolucion.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.INITIAL_TIME_NAME, initial_time)

        expected_type = TipoEvento.STRING_REFUND

        test_class.remove_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.EVENT_ID_NAME,
                                     old_id_event_to_refund)
        test_class.add_data_value(DevolucionViewTestCase.ENTITY_NAME, Devolucion.EVENT_ID_NAME, id_event_to_refund)

        validate_refund(test_class, result, initial_time, expected_type, id_event_to_refund, id_refund)

        validate_refund(test_class,
                        test_class.do_get_request("/clients/{0}/persons/{1}/refunds/{2}/".
                                                  format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                         test_class.expected_ids[PERSON_ENTITY_NAME],
                                                         id_refund)),
                        initial_time, expected_type, id_event_to_refund, id_refund)
    return result


def validate_refund(test_class, result, initial_time, expected_type, expected_event_to_return_id, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    if initial_time is not None:
        test_class.assertEqual(result[Devolucion.INITIAL_TIME_NAME], initial_time)
    test_class.assertEqual(result[Devolucion.TYPE_NAME], expected_type)
    test_class.assertTrue(result[Devolucion.DESCRIPTION_NAME].startswith(u"Refund"))
    test_class.assertEqual(result[Devolucion.EVENT_ID_NAME], expected_event_to_return_id)
    test_class.assertEqual(result[Devolucion.ID_NAME], expected_id)


def create_test_refund(test_class, create_new_refund=False, create_as_event=False):
    original_entity_name = DevolucionViewTestCase.ENTITY_NAME
    if create_as_event:
        DevolucionViewTestCase.ENTITY_NAME = EVENT_ENTITY_NAME
    result = None

    if DevolucionViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_refund:
        result = create_refund(test_class,
                               test_class.TEST_REFUND_INITIAL_TIME,
                               test_class.TEST_REFUND_ID_EVENT_TO_RETURN)[Devolucion.ID_NAME]

    DevolucionViewTestCase.ENTITY_NAME = original_entity_name
    return result


if __name__ == '__main__':
    unittest.main()
