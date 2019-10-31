# -*- coding: utf-8 -*
import unittest

from CJM.entidades.eventos.Feedback import Feedback
from CJM.entidades.eventos.TipoEvento import TipoEvento
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testEventos import EVENT_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME
from tests.testsCJM.testSkus.testCategoriaSKUViewTestCase import create_test_sku_category, SKU_CATEGORY_ENTITY_NAME
from tests.testsCJM.testSkus.testSkuViewTestCase import create_test_sku, SKU_ENTITY_NAME


class FeedbackViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'feedbacks'
    NUMBER_FEEDBACKS = 1

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

    TEST_LOCATION_TYPE = TipoUbicacion.CITY
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    TEST_SKU_NAME = "Test SKU"
    TEST_SKU_MEASURE_UNIT = "Unidad"
    TEST_SKU_COST = 100.5
    TEST_SKU_EAN_CODE = None

    TEST_SKU_CATEGORY_NAME = "Test Category"
    TEST_SKU_CATEGORY_PARENT_SKU_CATEGORY_ID = None

    def setUp(self):
        super(FeedbackViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)
        create_test_location(self)
        create_test_sku_category(self)
        self.TEST_SKU_CATEGORY_ID = self.expected_ids[SKU_CATEGORY_ENTITY_NAME]
        create_test_sku(self)

        self.initial_time = "19900101160101"
        self.text_template = "texto{0}"
        self.score = 50
        self.location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.sku_id = self.expected_ids[SKU_ENTITY_NAME]

    def test_non_existent_person(self):
        id_not_existent_person = self.expected_ids[PERSON_ENTITY_NAME] + 1
        self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME], id_not_existent_person), expected_code=404)

    def test_empty_feedbacks_view(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 0)

    def test_create_valid_feedbacks(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_create_valid_feedbacks_without_initial_time(self):
        self.initial_time = None
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_create_valid_feedbacks_with_second_time_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:47AM"
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_create_invalid_feedbacks_without_initial_time(self):
        self.initial_time = ""
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_month_for_initial_time(self):
        self.initial_time = "19902001160101"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_days_for_initial_time(self):
        self.initial_time = "19900230160101"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_hours_for_initial_time(self):
        self.initial_time = "19900202300101"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_minutes_for_initial_time(self):
        self.initial_time = "19900202206101"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_seconds_for_initial_time(self):
        self.initial_time = "19900202200161"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_format_for_initial_time(self):
        self.initial_time = "INVALID_DATETIME"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_month_with_second_format_for_initial_time(self):
        self.initial_time = "INVALID 01, 2016 at 11:47AM"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_day_with_second_format_for_initial_time(self):
        self.initial_time = "June 40, 2016 at 11:47AM"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_hour_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 13:47AM"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_minute_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:62AM"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_ampm_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:30CM"
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_without_location(self):
        self.location_id = ""
        self.create_test_feedbacks(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_location(self):
        self.location_id += 1
        self.create_test_feedbacks(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_without_sku(self):
        self.sku_id = ""
        self.create_test_feedbacks(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_sku(self):
        self.sku_id += 1
        self.create_test_feedbacks(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_without_score(self):
        self.score = ""
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_with_invalid_score(self):
        self.score += 200
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_feedbacks_without_text(self):
        self.text_template = ""
        self.create_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_feedbacks_on_multiple_persons(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        current_feddback_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()
        self.expected_ids[self.ENTITY_NAME] = current_feddback_id
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        create_test_person(self, create_new_person=True)
        self.prepend_to_templates(str(self.expected_ids[PERSON_ENTITY_NAME]))

        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_delete_feedbacks(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.delete_test_feedbacks()

    def test_delete_invalid_non_existent_feedbacks(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.delete_test_feedbacks(offset=self.NUMBER_FEEDBACKS, expected_code=404)

    def test_delete_feedbacks_as_events(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.delete_test_feedbacks(delete_as_event=True)

    def test_delete_invalid_non_existent_feedbacks_as_events(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.delete_test_feedbacks(offset=self.NUMBER_FEEDBACKS, delete_as_event=True, expected_code=404)

    def test_update_valid_feedbacks(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "20000101160101"
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_valid_feedbacks_without_initial_time(self):
        self.initial_time = None
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = None
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_non_existent_feedbacks(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "20000101160101"
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks(offset=self.NUMBER_FEEDBACKS, expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_with_empty_initial_time(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = ""
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_with_invalid_initial_time(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "INVALID_TIME"
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_without_text(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "20000101160101"
        self.new_text_template = ""
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_with_invalid_score(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "20000101160101"
        self.new_text_template = "New_ Text{0}"
        self.new_score = 1000
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_with_non_existent_location(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "20000101160101"
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME] + 1
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME]

        self.update_test_feedbacks(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def test_update_invalid_with_non_existent_sku(self):
        self.create_test_feedbacks()
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

        self.new_initial_time = "20000101160101"
        self.new_text_template = "New_ Text{0}"
        self.new_score = 70
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]
        self.new_sku_id = self.expected_ids[SKU_ENTITY_NAME] + 1

        self.update_test_feedbacks(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS)

    def delete_test_feedbacks(self, offset=0, delete_as_event=False, expected_code=200):
        for feedback_number in range(0, self.NUMBER_FEEDBACKS):
            delete_feedback(self,
                            feedback_number + offset,
                            self.initial_time,
                            self.text_template.format(feedback_number),
                            self.score,
                            self.location_id,
                            self.sku_id,
                            delete_as_event=delete_as_event,
                            expected_code=expected_code)

            if expected_code == 200:
                results = self.do_get_request("/clients/{0}/persons/{1}/feedbacks/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PERSON_ENTITY_NAME]))
                if feedback_number + 1 == self.NUMBER_FEEDBACKS:
                    from commons.validations import DEFAULT_DATETIME_FORMAT
                    date_initial_time = validate_datetime(self.initial_time, Feedback.INITIAL_TIME_NAME)
                    initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)

                    self.remove_data_value(self.ENTITY_NAME, Feedback.INITIAL_TIME_NAME, initial_time)
                    self.remove_data_value(self.ENTITY_NAME, Feedback.SCORE_NAME, self.score)
                    self.remove_data_value(self.ENTITY_NAME, Feedback.UBICACION_ID_NAME, self.location_id)
                    self.remove_data_value(self.ENTITY_NAME, Feedback.SKU_ID_NAME, self.sku_id)
                    self.remove_data_value(self.ENTITY_NAME, Feedback.NUMBER_NAME, self.score)
                    self.remove_data_value(self.ENTITY_NAME, Feedback.TYPE_NAME, TipoEvento.STRING_FEEDBACK)

                self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_FEEDBACKS - feedback_number - 1)

    def update_test_feedbacks(self, offset=0, expected_code=200):
        for feedback_number in range(0, self.NUMBER_FEEDBACKS):
            update_feedback(self,
                            feedback_number + offset,
                            self.new_initial_time,
                            self.new_text_template.format(feedback_number),
                            self.new_score,
                            self.new_location_id,
                            self.new_sku_id,
                            self.initial_time,
                            self.text_template.format(feedback_number),
                            self.score,
                            self.location_id,
                            self.sku_id,
                            expected_code=expected_code)

    def create_test_feedbacks(self, expected_code=200):
        for feedback_number in range(0, self.NUMBER_FEEDBACKS):
            create_feedback(self,
                            self.initial_time,
                            self.text_template.format(feedback_number),
                            self.score,
                            self.location_id,
                            self.sku_id,
                            expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        self.text_template = "{0}{1}".format(prefix, self.text_template)


FEEDBACK_ENTITY_NAME = FeedbackViewTestCase.ENTITY_NAME


def create_feedback(test_class, initial_time, text, score, location_id, sku_id, expected_code=200):
    create_test_person(test_class)
    create_test_location(test_class)
    create_test_sku(test_class)

    if FeedbackViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[FeedbackViewTestCase.ENTITY_NAME] = FeedbackViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[FeedbackViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/clients/{0}/persons/{1}/feedbacks/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                test_class.expected_ids[PERSON_ENTITY_NAME]),
                                        data={Feedback.INITIAL_TIME_NAME: initial_time,
                                              Feedback.TEXT_NAME: text,
                                              Feedback.SCORE_NAME: score,
                                              Feedback.UBICACION_ID_NAME: location_id,
                                              Feedback.SKU_ID_NAME: sku_id},
                                        expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_initial_time = validate_datetime(initial_time, Feedback.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.INITIAL_TIME_NAME, initial_time)

        expected_number = score
        expected_type = TipoEvento.STRING_FEEDBACK
        expected_description = text
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.TEXT_NAME, text)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.SCORE_NAME, score)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.UBICACION_ID_NAME, location_id)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.SKU_ID_NAME, sku_id)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.TYPE_NAME, expected_type)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.NUMBER_NAME, expected_number)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.DESCRIPTION_NAME, expected_description)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME,
                                  Feedback.ID_NAME, test_class.expected_ids[FeedbackViewTestCase.ENTITY_NAME])

        validate_feedback(test_class, result, initial_time, text, score, location_id, sku_id,
                          expected_type, expected_number, expected_description,
                          test_class.expected_ids[FeedbackViewTestCase.ENTITY_NAME])

        validate_feedback(test_class,
                          test_class.do_get_request("/clients/{0}/persons/{1}/feedbacks/{2}/".
                                                    format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                           test_class.expected_ids[PERSON_ENTITY_NAME],
                                                           test_class.expected_ids[FeedbackViewTestCase.ENTITY_NAME])),
                          initial_time, text, score, location_id, sku_id,
                          expected_type, expected_number, expected_description,
                          test_class.expected_ids[FeedbackViewTestCase.ENTITY_NAME])
    return result


def update_feedback(test_class, feedback_number, initial_time, text, score, location_id, sku_id,
                    old_initial_time, old_text, old_score, old_location_id, old_sku_id,
                    expected_code=200):
    id_feedback = FeedbackViewTestCase.STARTING_ID + feedback_number

    result = test_class.do_put_request("/clients/{0}/persons/{1}/feedbacks/{2}/"
                                       .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                               id_feedback),
                                       data={Feedback.INITIAL_TIME_NAME: initial_time,
                                             Feedback.TEXT_NAME: text,
                                             Feedback.SCORE_NAME: score,
                                             Feedback.UBICACION_ID_NAME: location_id,
                                             Feedback.SKU_ID_NAME: sku_id},
                                       expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None and old_initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_old_initial_time = validate_datetime(old_initial_time, Feedback.INITIAL_TIME_NAME)
            old_initial_time = date_old_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.INITIAL_TIME_NAME, old_initial_time)

            date_initial_time = validate_datetime(initial_time, Feedback.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.INITIAL_TIME_NAME, initial_time)

        old_expected_number = old_score
        expected_number = score

        old_expected_description = old_text
        expected_description = text

        expected_type = TipoEvento.STRING_FEEDBACK

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.TEXT_NAME, old_text)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.TEXT_NAME, text)

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME,
                                     Feedback.DESCRIPTION_NAME, old_expected_description)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME,
                                  Feedback.DESCRIPTION_NAME, expected_description)

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.SCORE_NAME, old_score)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.SCORE_NAME, score)

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.NUMBER_NAME, old_expected_number)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.NUMBER_NAME, expected_number)

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.UBICACION_ID_NAME, old_location_id)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.UBICACION_ID_NAME, location_id)

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.SKU_ID_NAME, old_sku_id)
        test_class.add_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.SKU_ID_NAME, sku_id)

        validate_feedback(test_class, result, initial_time, text, score, location_id, sku_id,
                          expected_type, expected_number, expected_description, id_feedback)

        validate_feedback(test_class,
                          test_class.do_get_request("/clients/{0}/persons/{1}/feedbacks/{2}/".
                                                    format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                           test_class.expected_ids[PERSON_ENTITY_NAME],
                                                           id_feedback)),
                          initial_time, text, score, location_id, sku_id,
                          expected_type, expected_number, expected_description, id_feedback)
    return result


def delete_feedback(test_class, feedback_number, initial_time, text, score, location_id, sku_id,
                    delete_as_event=False, expected_code=200):
    id_feedback = FeedbackViewTestCase.STARTING_ID + feedback_number

    if delete_as_event:
        url = "/clients/{0}/persons/{1}/feedbacks/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                                               id_feedback)
    else:
        url = "/clients/{0}/persons/{1}/events/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                            test_class.expected_ids[PERSON_ENTITY_NAME],
                                                            id_feedback)
    result = test_class.do_delete_request(url, expected_code=expected_code)
    if expected_code == 200:
        expected_number = score
        expected_type = TipoEvento.STRING_FEEDBACK
        expected_description = text

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.TEXT_NAME, text)
        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.DESCRIPTION_NAME, expected_description)

        test_class.remove_data_value(FeedbackViewTestCase.ENTITY_NAME, Feedback.ID_NAME, id_feedback)

        validate_feedback(test_class, result, initial_time, text, score, location_id, sku_id,
                          expected_type, expected_number, expected_description,
                          id_feedback)

        test_class.do_get_request(url, expected_code=404)
    return result


def validate_feedback(test_class, result, initial_time, text, score, location_id, sku_id,
                      expected_type, expected_number, expected_description, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    if initial_time is not None:
        test_class.assertEqual(result[Feedback.INITIAL_TIME_NAME], initial_time)
    test_class.assertEqual(result[Feedback.TEXT_NAME], text)
    test_class.assertEqual(result[Feedback.SCORE_NAME], score)
    test_class.assertEqual(result[Feedback.UBICACION_ID_NAME], location_id)
    test_class.assertEqual(result[Feedback.SKU_ID_NAME], sku_id)
    test_class.assertEqual(result[Feedback.TYPE_NAME], expected_type)
    test_class.assertEqual(result[Feedback.NUMBER_NAME], expected_number)
    test_class.assertEqual(result[Feedback.DESCRIPTION_NAME], expected_description)
    test_class.assertEqual(result[Feedback.ID_NAME], expected_id)


def create_test_feedback(test_class, create_new_feedback=False, create_as_event=False):
    original_entity_name = FeedbackViewTestCase.ENTITY_NAME
    if create_as_event:
        FeedbackViewTestCase.ENTITY_NAME = EVENT_ENTITY_NAME
    result = None

    if FeedbackViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_feedback:
        result = create_feedback(test_class,
                                 test_class.TEST_FEEDBACK_INITIAL_TIME,
                                 test_class.TEST_FEEDBACK_TEXT,
                                 test_class.TEST_FEEDBACK_SCORE,
                                 test_class.TEST_FEEDBACK_ID_LOCATION,
                                 test_class.TEST_FEEDBACK_ID_SKU)[Feedback.ID_NAME]

    FeedbackViewTestCase.ENTITY_NAME = original_entity_name
    return result

if __name__ == '__main__':
    unittest.main()
