# -*- coding: utf-8 -*
import unittest

from CJM.entidades.eventos.ReporteVisitaPorTipo import ReporteVisitaPorTipo
from CJM.entidades.eventos.TipoEvento import TipoEvento
from CJM.entidades.eventos.Visita import Visita
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
from tests.testsCJM.testEventos import EVENT_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME


class VisitViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'visitas'
    NUMBER_VISITS = 1

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

    TEST_LOCATION_TYPE = TipoUbicacion.ZONE
    TEST_LOCATION_NAME = "Test location"
    TEST_LOCATION_PARENT_LOCATION_ID = None

    def setUp(self):
        super(VisitViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)
        create_test_location(self)

        self.initial_time = "19900101160101"
        self.final_time = "19900101160201"
        self.location_id = self.expected_ids[LOCATION_ENTITY_NAME]

    def test_non_existent_person(self):
        id_not_existent_person = self.expected_ids[PERSON_ENTITY_NAME] + 1000
        self.do_get_request("/clients/{0}/persons/{1}/visits/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME], id_not_existent_person), expected_code=404)

    def test_empty_visits_view(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 0)

    def test_create_valid_visits(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_create_valid_visits_without_initial_time(self):
        self.initial_time = None
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_create_valid_visits_without_final_time(self):
        self.final_time = None
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_create_valid_visits_without_initial_and_final_time(self):
        self.initial_time = None
        self.final_time = None
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_create_valid_visits_with_second_time_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:47AM"
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_create_invalid_visits_without_initial_time(self):
        self.initial_time = ""
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_month_for_initial_time(self):
        self.initial_time = "19902001160101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_days_for_initial_time(self):
        self.initial_time = "19900230160101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_hours_for_initial_time(self):
        self.initial_time = "19900202300101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_minutes_for_initial_time(self):
        self.initial_time = "19900202206101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_seconds_for_initial_time(self):
        self.initial_time = "19900202200161"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_format_for_initial_time(self):
        self.initial_time = "INVALID_DATETIME"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_month_with_second_format_for_initial_time(self):
        self.initial_time = "INVALID 01, 2016 at 11:47AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_day_with_second_format_for_initial_time(self):
        self.initial_time = "June 40, 2016 at 11:47AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_hour_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 13:47AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_minute_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:62AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_ampm_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:30CM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_without_final_time(self):
        self.final_time = ""
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_month_for_final_time(self):
        self.final_time = "19902001160101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_days_for_final_time(self):
        self.final_time = "19900230160101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_hours_for_final_time(self):
        self.final_time = "19900202300101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_minutes_for_final_time(self):
        self.final_time = "19900202206101"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_seconds_for_final_time(self):
        self.final_time = "19900202200161"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_format_for_final_time(self):
        self.final_time = "INVALID_DATETIME"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_month_with_second_format_for_final_time(self):
        self.final_time = "INVALID 01, 2016 at 11:47AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_day_with_second_format_for_final_time(self):
        self.final_time = "June 40, 2016 at 11:47AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_hour_with_second_format_for_final_time(self):
        self.final_time = "June 01, 2016 at 13:47AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_minute_with_second_format_for_final_time(self):
        self.final_time = "June 01, 2016 at 11:62AM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_ampm_with_second_format_for_final_time(self):
        self.final_time = "June 01, 2016 at 11:30CM"
        self.create_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_without_location(self):
        self.location_id = ""
        self.create_test_visits(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_visits_with_invalid_location(self):
        self.location_id += 1
        self.create_test_visits(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_visits_on_multiple_persons(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        current_visit_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()
        self.expected_ids[self.ENTITY_NAME] = current_visit_id
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        create_test_person(self, create_new_person=True)
        self.prepend_to_templates(str(self.expected_ids[PERSON_ENTITY_NAME]))

        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_delete_feedbacks(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.delete_test_visits()

    def test_delete_invalid_non_existent_feedbacks(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.delete_test_visits(offset=self.NUMBER_VISITS, expected_code=404)

    def test_delete_feedbacks_as_events(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.delete_test_visits(delete_as_event=True)

    def test_delete_invalid_non_existent_feedbacks_as_events(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.delete_test_visits(offset=self.NUMBER_VISITS, delete_as_event=True, expected_code=404)

    def test_update_valid_visits(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "20000101160101"
        self.new_final_time = "20000101160201"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_valid_visits_without_initial_time(self):
        self.initial_time = None
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = None
        self.new_final_time = "20000101160201"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_valid_visits_without_final_time(self):
        self.final_time = None
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "20000101160101"
        self.new_final_time = None
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_invalid_non_existent_visits(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "20000101160101"
        self.new_final_time = "20000101160201"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits(offset=self.NUMBER_VISITS, expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_invalid_visits_with_empty_initial_time(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = ""
        self.new_final_time = "20000101160201"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_invalid_visits_with_invalid_initial_time(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "INVALID_TIME"
        self.new_final_time = "20000101160201"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_invalid_visits_with_empty_final(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "20000101160101"
        self.new_final_time = ""
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_invalid_visits_with_invalid_final_time(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "20000101160101"
        self.new_final_time = "INVALID_TIME"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        self.update_test_visits(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_update_invalid_visits_with_invalid_id_location(self):
        self.create_test_visits()
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

        self.new_initial_time = "20000101160101"
        self.new_final_time = "20000101160201"
        self.new_location_id = self.expected_ids[LOCATION_ENTITY_NAME] + 1

        self.update_test_visits(expected_code=404)
        results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS)

    def test_visits_report(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.location_id))

        self.assertEqual(number_visits_category_a, results["A"])
        self.assertEqual(number_visits_category_b, results["B"])
        self.assertEqual(number_visits_category_c, results["C"])
        self.assertEqual(number_visits_category_d, results["D"])
        self.assertEqual(0, results[""])

    def test_visits_report_with_duplicated_person(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d,
                                                 use_same_person=True)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.location_id))

        self.assertEqual(1, results["A"])
        self.assertEqual(1, results["B"])
        self.assertEqual(1, results["C"])
        self.assertEqual(1, results["D"])
        self.assertEqual(0, results[""])

    def test_visits_report_of_parent_location(self):
        self.TEST_LOCATION_TYPE = TipoUbicacion.AREA
        self.TEST_LOCATION_PARENT_LOCATION_ID = self.location_id
        create_test_location(self, create_new_location=True)
        self.location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_PARENT_LOCATION_ID))

        self.assertEqual(number_visits_category_a, results["A"])
        self.assertEqual(number_visits_category_b, results["B"])
        self.assertEqual(number_visits_category_c, results["C"])
        self.assertEqual(number_visits_category_d, results["D"])
        self.assertEqual(0, results[""])

        self.TEST_LOCATION_PARENT_LOCATION_ID = None

    def test_visits_report_with_initial_time_before_visits_initial_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/?initial-time=19800101010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.location_id))

        self.assertEqual(number_visits_category_a, results["A"])
        self.assertEqual(number_visits_category_b, results["B"])
        self.assertEqual(number_visits_category_c, results["C"])
        self.assertEqual(number_visits_category_d, results["D"])
        self.assertEqual(0, results[""])

    def test_visits_report_with_initial_time_after_visits_initial_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/?initial-time=200001010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.location_id))

        self.assertEqual(0, results["A"])
        self.assertEqual(0, results["B"])
        self.assertEqual(0, results["C"])
        self.assertEqual(0, results["D"])
        self.assertEqual(0, results[""])

    def test_visits_report_with_final_time_after_visits_final_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/?final-time=20000101010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.location_id))

        self.assertEqual(number_visits_category_a, results["A"])
        self.assertEqual(number_visits_category_b, results["B"])
        self.assertEqual(number_visits_category_c, results["C"])
        self.assertEqual(number_visits_category_d, results["D"])
        self.assertEqual(0, results[""])

    def test_visits_report_with_final_time_before_visits_final_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/locations/{1}/visits-per-category/?final-time=19800101010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.location_id))

        self.assertEqual(0, results["A"])
        self.assertEqual(0, results["B"])
        self.assertEqual(0, results["C"])
        self.assertEqual(0, results["D"])
        self.assertEqual(0, results[""])

    def test_visits_report_by_type(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.location_id][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(number_visits_category_a, result["A"])
        self.assertEqual(number_visits_category_b, result["B"])
        self.assertEqual(number_visits_category_c, result["C"])
        self.assertEqual(number_visits_category_d, result["D"])
        self.assertEqual(0, result[""])

    def test_visits_report_by_type_of_parent_location(self):
        self.TEST_LOCATION_TYPE = TipoUbicacion.AREA
        self.TEST_LOCATION_PARENT_LOCATION_ID = self.location_id
        create_test_location(self, create_new_location=True)
        self.TEST_LOCATION_TYPE = TipoUbicacion.ZONE
        self.location_id = self.expected_ids[LOCATION_ENTITY_NAME]

        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.TEST_LOCATION_PARENT_LOCATION_ID][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(number_visits_category_a, result["A"])
        self.assertEqual(number_visits_category_b, result["B"])
        self.assertEqual(number_visits_category_c, result["C"])
        self.assertEqual(number_visits_category_d, result["D"])
        self.assertEqual(0, result[""])

        self.TEST_LOCATION_PARENT_LOCATION_ID = None

    def test_visits_report_by_type_with_duplicated_person(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d,
                                                 use_same_person=True)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.location_id][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(1, result["A"])
        self.assertEqual(1, result["B"])
        self.assertEqual(1, result["C"])
        self.assertEqual(1, result["D"])
        self.assertEqual(0, result[""])

    def test_visits_report_by_type_with_initial_time_before_visits_initial_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}&initial-time=19800101010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.location_id][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(number_visits_category_a, result["A"])
        self.assertEqual(number_visits_category_b, result["B"])
        self.assertEqual(number_visits_category_c, result["C"])
        self.assertEqual(number_visits_category_d, result["D"])
        self.assertEqual(0, result[""])

    def test_visits_report_by_type_with_initial_time_after_visits_initial_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}&initial-time=200001010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.location_id][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(0, result["A"])
        self.assertEqual(0, result["B"])
        self.assertEqual(0, result["C"])
        self.assertEqual(0, result["D"])
        self.assertEqual(0, result[""])

    def test_visits_report_by_type_with_final_time_after_visits_final_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}&final-time=20000101010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.location_id][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(number_visits_category_a, result["A"])
        self.assertEqual(number_visits_category_b, result["B"])
        self.assertEqual(number_visits_category_c, result["C"])
        self.assertEqual(number_visits_category_d, result["D"])
        self.assertEqual(0, result[""])

    def test_visits_report_by_type_with_final_time_before_visits_final_time(self):
        number_visits_category_a = 6
        number_visits_category_b = 5
        number_visits_category_c = 7
        number_visits_category_d = 3

        self.create_test_visits_for_visit_report(number_visits_category_a, number_visits_category_b,
                                                 number_visits_category_c, number_visits_category_d)

        results = self.do_get_request("/clients/{0}/visits-per-category/?type={1}&final-time=19800101010101"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.TEST_LOCATION_TYPE))

        result = [res for res in results
                  if res[ReporteVisitaPorTipo.UBICACION_ID_NAME]
                  == self.location_id][0][ReporteVisitaPorTipo.REPORT_NAME]

        self.assertEqual(0, result["A"])
        self.assertEqual(0, result["B"])
        self.assertEqual(0, result["C"])
        self.assertEqual(0, result["D"])
        self.assertEqual(0, result[""])

    def delete_test_visits(self, offset=0, delete_as_event=False, expected_code=200):
        for visit_number in range(0, self.NUMBER_VISITS):
            delete_visit(self,
                         visit_number + offset,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         delete_as_event=delete_as_event,
                         expected_code=expected_code)

            if expected_code == 200:
                results = self.do_get_request("/clients/{0}/persons/{1}/visits/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PERSON_ENTITY_NAME]))
                if visit_number + 1 == self.NUMBER_VISITS:
                    from commons.validations import DEFAULT_DATETIME_FORMAT
                    date_initial_time = validate_datetime(self.initial_time, Visita.INITIAL_TIME_NAME)
                    initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)

                    date_final_time = validate_datetime(self.final_time, Visita.FINAL_TIME_NAME)
                    final_time = date_final_time.strftime(DEFAULT_DATETIME_FORMAT)

                    expected_number = int((date_final_time - date_initial_time).total_seconds())
                    expected_description = self.TEST_LOCATION_NAME

                    self.remove_data_value(self.ENTITY_NAME, Visita.INITIAL_TIME_NAME, initial_time)
                    self.remove_data_value(self.ENTITY_NAME, Visita.FINAL_TIME_NAME, final_time)
                    self.remove_data_value(self.ENTITY_NAME, Visita.UBICACION_ID_NAME, self.location_id)
                    self.remove_data_value(self.ENTITY_NAME, Visita.NUMBER_NAME, expected_number)
                    self.remove_data_value(self.ENTITY_NAME, Visita.DESCRIPTION_NAME, expected_description)
                    self.remove_data_value(self.ENTITY_NAME, Visita.TYPE_NAME, TipoEvento.STRING_VISIT)

                self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_VISITS - visit_number - 1)

    def update_test_visits(self, offset=0, expected_code=200):
        for visit_number in range(0, self.NUMBER_VISITS):
            update_visit(self,
                         visit_number + offset,
                         self.new_initial_time,
                         self.new_final_time,
                         self.new_location_id,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         expected_code=expected_code)

    def create_test_visits(self, expected_code=200):
        for visit_number in range(0, self.NUMBER_VISITS):
            create_visit(self,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        pass

    def create_test_visits_for_visit_report(self, number_visits_category_a, number_visits_category_b,
                                            number_visits_category_c, number_visits_category_d,
                                            use_same_person=False):
        self.TEST_PERSON_CATEGORY = "A"
        person_document_template = "9{0}"
        for number in range(0, number_visits_category_a):
            if number == 0 or not use_same_person:
                self.TEST_PERSON_DOCUMENT_NUMBER = person_document_template.format(number)
                create_test_person(self, create_new_person=True)
            create_visit(self,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         expected_code=200)

        person_document_template = "9" + person_document_template
        self.TEST_PERSON_CATEGORY = "B"
        for number in range(0, number_visits_category_b):
            if number == 0 or not use_same_person:
                self.TEST_PERSON_DOCUMENT_NUMBER = person_document_template.format(number)
                create_test_person(self, create_new_person=True)
            create_visit(self,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         expected_code=200)

        person_document_template = "9" + person_document_template
        self.TEST_PERSON_CATEGORY = "C"
        for number in range(0, number_visits_category_c):
            if number == 0 or not use_same_person:
                self.TEST_PERSON_DOCUMENT_NUMBER = person_document_template.format(number)
                create_test_person(self, create_new_person=True)
            create_visit(self,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         expected_code=200)

        person_document_template = "9" + person_document_template
        self.TEST_PERSON_CATEGORY = "D"
        for number in range(0, number_visits_category_d):
            if number == 0 or not use_same_person:
                self.TEST_PERSON_DOCUMENT_NUMBER = person_document_template.format(number)
                create_test_person(self, create_new_person=True)
            create_visit(self,
                         self.initial_time,
                         self.final_time,
                         self.location_id,
                         expected_code=200)

VISIT_ENTITY_NAME = VisitViewTestCase.ENTITY_NAME


def create_visit(test_class, initial_time, final_time, location_id, expected_code=200):
    create_test_person(test_class)
    create_test_location(test_class)

    if VisitViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[VisitViewTestCase.ENTITY_NAME] = VisitViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[VisitViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/clients/{0}/persons/{1}/visits/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                test_class.expected_ids[PERSON_ENTITY_NAME]),
                                        data={Visita.INITIAL_TIME_NAME: initial_time,
                                              Visita.FINAL_TIME_NAME: final_time,
                                              Visita.UBICACION_ID_NAME: location_id},
                                        expected_code=expected_code)
    if expected_code == 200:
        from commons.validations import DEFAULT_DATETIME_FORMAT
        if initial_time is not None:
            date_initial_time = validate_datetime(initial_time, Visita.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.INITIAL_TIME_NAME, initial_time)
        else:
            date_initial_time = None

        if final_time is not None:
            date_final_time = validate_datetime(final_time, Visita.INITIAL_TIME_NAME)
            final_time = date_final_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.FINAL_TIME_NAME, final_time)
        else:
            date_final_time = None

        if date_initial_time is not None and date_final_time is not None:
            expected_number = int((date_final_time - date_initial_time).total_seconds())
            test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.NUMBER_NAME, expected_number)
        else:
            expected_number = None

        expected_type = TipoEvento.STRING_VISIT
        expected_description = test_class.TEST_LOCATION_NAME
        test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.UBICACION_ID_NAME, location_id)
        test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.TYPE_NAME, expected_type)
        test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.DESCRIPTION_NAME, expected_description)
        test_class.add_data_value(VisitViewTestCase.ENTITY_NAME,
                                  Visita.ID_NAME, test_class.expected_ids[VisitViewTestCase.ENTITY_NAME])
        validate_visit(test_class, result, initial_time, final_time, location_id,
                       expected_type, expected_number, expected_description,
                       test_class.expected_ids[VisitViewTestCase.ENTITY_NAME])

        validate_visit(test_class,
                       test_class.do_get_request("/clients/{0}/persons/{1}/visits/{2}/".
                                                 format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                        test_class.expected_ids[PERSON_ENTITY_NAME],
                                                        test_class.expected_ids[VisitViewTestCase.ENTITY_NAME])),
                       initial_time, final_time, location_id, expected_type, expected_number, expected_description,
                       test_class.expected_ids[VisitViewTestCase.ENTITY_NAME])
    return result


def update_visit(test_class, visit_number, initial_time, final_time, location_id,
                 old_initial_time, old_final_time, old_location_id,
                 expected_code=200):
    id_visit = VisitViewTestCase.STARTING_ID + visit_number

    result = test_class.do_put_request("/clients/{0}/persons/{1}/visits/{2}/"
                                       .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                               id_visit),
                                       data={Visita.INITIAL_TIME_NAME: initial_time,
                                             Visita.FINAL_TIME_NAME: final_time,
                                             Visita.UBICACION_ID_NAME: location_id},
                                       expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None and old_initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_old_initial_time = validate_datetime(old_initial_time, Visita.INITIAL_TIME_NAME)
            old_initial_time = date_old_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.remove_data_value(VisitViewTestCase.ENTITY_NAME, Visita.INITIAL_TIME_NAME, old_initial_time)

            date_initial_time = validate_datetime(initial_time, Visita.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.INITIAL_TIME_NAME, initial_time)
        else:
            date_initial_time = None
            date_old_initial_time = None

        if final_time is not None and old_final_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_old_final_time = validate_datetime(old_final_time, Visita.INITIAL_TIME_NAME)
            old_final_time = date_old_final_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.remove_data_value(VisitViewTestCase.ENTITY_NAME, Visita.FINAL_TIME_NAME, old_final_time)

            date_final_time = validate_datetime(final_time, Visita.INITIAL_TIME_NAME)
            final_time = date_final_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.FINAL_TIME_NAME, final_time)
        else:
            date_final_time = None
            date_old_final_time = None

        if date_initial_time is not None and date_final_time is not None:
            expected_number = int((date_final_time - date_initial_time).total_seconds())
            old_expected_number = int((date_old_final_time - date_old_initial_time).total_seconds())
            test_class.remove_data_value(VisitViewTestCase.ENTITY_NAME, Visita.NUMBER_NAME, old_expected_number)
            test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.NUMBER_NAME, expected_number)
        else:
            expected_number = None

        old_expected_description = test_class.TEST_LOCATION_NAME
        expected_description = test_class.TEST_LOCATION_NAME

        expected_type = TipoEvento.STRING_VISIT

        test_class.remove_data_value(VisitViewTestCase.ENTITY_NAME,
                                     Visita.DESCRIPTION_NAME, old_expected_description)
        test_class.add_data_value(VisitViewTestCase.ENTITY_NAME,
                                  Visita.DESCRIPTION_NAME, expected_description)

        test_class.remove_data_value(VisitViewTestCase.ENTITY_NAME, Visita.UBICACION_ID_NAME, old_location_id)
        test_class.add_data_value(VisitViewTestCase.ENTITY_NAME, Visita.UBICACION_ID_NAME, location_id)

        validate_visit(test_class, result, initial_time, final_time, location_id,
                       expected_type, expected_number, expected_description,
                       id_visit)

        validate_visit(test_class,
                       test_class.do_get_request("/clients/{0}/persons/{1}/visits/{2}/"
                                                 .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                         test_class.expected_ids[PERSON_ENTITY_NAME],
                                                         id_visit)),
                       initial_time, final_time, location_id,
                       expected_type, expected_number, expected_description,
                       id_visit)
    return result


def delete_visit(test_class, visit_number, initial_time, final_time, location_id,
                 delete_as_event=False, expected_code=200):
    id_visit = VisitViewTestCase.STARTING_ID + visit_number

    if delete_as_event:
        url = "/clients/{0}/persons/{1}/visits/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                            test_class.expected_ids[PERSON_ENTITY_NAME],
                                                            id_visit)
    else:
        url = "/clients/{0}/persons/{1}/events/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                            test_class.expected_ids[PERSON_ENTITY_NAME],
                                                            id_visit)

    result = test_class.do_delete_request(url, expected_code=expected_code)
    if expected_code == 200:

        from commons.validations import DEFAULT_DATETIME_FORMAT
        date_initial_time = validate_datetime(initial_time, Visita.INITIAL_TIME_NAME)
        date_final_time = validate_datetime(final_time, Visita.INITIAL_TIME_NAME)
        initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
        final_time = date_final_time.strftime(DEFAULT_DATETIME_FORMAT)

        expected_number = int((date_final_time - date_initial_time).total_seconds())
        expected_type = TipoEvento.STRING_VISIT
        expected_description = test_class.TEST_LOCATION_NAME

        test_class.remove_data_value(VisitViewTestCase.ENTITY_NAME, Visita.ID_NAME, id_visit)

        validate_visit(test_class, result, initial_time, final_time, location_id,
                       expected_type, expected_number, expected_description,
                       id_visit)

        test_class.do_get_request(url, expected_code=404)
    return result


def validate_visit(test_class, result, initial_time, final_time, location_id, expected_type, expected_number,
                   expected_description, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    if initial_time is not None:
        test_class.assertEqual(result[Visita.INITIAL_TIME_NAME], initial_time)
    if final_time is not None:
        test_class.assertEqual(result[Visita.FINAL_TIME_NAME], final_time)
    test_class.assertEqual(result[Visita.UBICACION_ID_NAME], location_id)
    test_class.assertEqual(result[Visita.TYPE_NAME], expected_type)
    if expected_number is not None:
        test_class.assertEqual(result[Visita.NUMBER_NAME], expected_number)
    test_class.assertEqual(result[Visita.DESCRIPTION_NAME], expected_description)
    test_class.assertEqual(result[Visita.ID_NAME], expected_id)


def create_test_visit(test_class, create_new_visit=False, create_as_event=False):
    original_entity_name = VisitViewTestCase.ENTITY_NAME
    if create_as_event:
        VisitViewTestCase.ENTITY_NAME = EVENT_ENTITY_NAME
    result = None

    if VisitViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_visit:
        result = create_visit(test_class,
                              test_class.TEST_VISIT_INITIAL_TIME,
                              test_class.TEST_VISIT_FINAL_TIME,
                              test_class.TEST_VISIT_ID_LOCATION)[Visita.ID_NAME]

    VisitViewTestCase.ENTITY_NAME = original_entity_name
    return result

if __name__ == '__main__':
    unittest.main()
