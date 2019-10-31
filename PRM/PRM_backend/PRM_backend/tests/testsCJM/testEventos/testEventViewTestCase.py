# -*- coding: utf-8 -*
import unittest

from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import create_test_client, CLIENT_ENTITY_NAME
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


class EventViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    TEST_CLIENT_NAME = "Test client"
    ENTITY_NAME = 'eventos'
    NUMBER_PERSONS = 1
    NUMBER_SKUS = 1

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

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
        super(EventViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)
        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]

    def test_create_and_consult_timeline(self):

        events_results = self.do_get_request("/clients/{0}/persons/{1}/events/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(EVENT_ENTITY_NAME, events_results, 0)

        for action_number in range(0, self.NUMBER_ACTIONS):
            create_test_action(self, create_new_action=True, create_as_event=True)

        events_results = self.do_get_request("/clients/{0}/persons/{1}/events/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(EVENT_ENTITY_NAME, events_results, self.NUMBER_ACTIONS)

        create_test_sku(self)
        self.TEST_PURCHASE_ID_SKU = [int(sku_template.format(self.expected_ids[SKU_ENTITY_NAME]))
                                     for sku_template in self.TEST_PURCHASE_ID_SKU_TEMPLATE]

        for purchase_number in range(0, self.NUMBER_PURCHASES):
            create_test_purchase(self, create_new_purchase=True, create_as_event=True)

        events_results = self.do_get_request("/clients/{0}/persons/{1}/events/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(EVENT_ENTITY_NAME, events_results, self.NUMBER_ACTIONS + self.NUMBER_PURCHASES)

        create_test_location(self)
        self.TEST_FEEDBACK_ID_LOCATION = int(self.TEST_FEEDBACK_ID_LOCATION_TEMPLATE.
                                             format(self.expected_ids[LOCATION_ENTITY_NAME]))
        self.TEST_FEEDBACK_ID_SKU = int(self.TEST_FEEDBACK_ID_SKU_TEMPLATE.
                                        format(self.expected_ids[SKU_ENTITY_NAME]))

        for feedback_number in range(0, self.NUMBER_FEEDBACKS):
            create_test_feedback(self, create_new_feedback=True, create_as_event=True)

        events_results = self.do_get_request("/clients/{0}/persons/{1}/events/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(EVENT_ENTITY_NAME, events_results,
                                 self.NUMBER_ACTIONS + self.NUMBER_PURCHASES + self.NUMBER_FEEDBACKS)

        self.TEST_VISIT_ID_LOCATION = int(self.TEST_VISIT_ID_LOCATION_TEMPLATE.
                                          format(self.expected_ids[LOCATION_ENTITY_NAME]))

        for visit_number in range(0, self.NUMBER_VISITS):
            create_test_visit(self, create_new_visit=True, create_as_event=True)

        events_results = self.do_get_request("/clients/{0}/persons/{1}/events/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(EVENT_ENTITY_NAME, events_results,
                                 self.NUMBER_ACTIONS + self.NUMBER_PURCHASES + self.NUMBER_FEEDBACKS +
                                 self.NUMBER_VISITS)

        self.TEST_ORDER_ID_SKU = [int(sku_template.format(self.expected_ids[SKU_ENTITY_NAME]))
                                  for sku_template in self.TEST_ORDER_ID_SKU_TEMPLATE]

        for order_number in range(0, self.NUMBER_ORDERS):
            create_test_order(self, create_new_order=True, create_as_event=True)

        events_results = self.do_get_request("/clients/{0}/persons/{1}/events/"
                                             .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                     self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(EVENT_ENTITY_NAME, events_results,
                                 self.NUMBER_ACTIONS + self.NUMBER_PURCHASES + self.NUMBER_FEEDBACKS +
                                 self.NUMBER_VISITS + self.NUMBER_ORDERS)


if __name__ == '__main__':
    unittest.main()
