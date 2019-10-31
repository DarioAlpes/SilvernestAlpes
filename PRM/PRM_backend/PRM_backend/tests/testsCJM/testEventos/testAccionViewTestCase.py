# -*- coding: utf-8 -*
import unittest

from CJM.entidades.eventos.Accion import Accion
from CJM.entidades.eventos.TipoEvento import TipoEvento
from commons.validations import validate_datetime
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME, create_test_client
from tests.testsCJM.testEventos import EVENT_ENTITY_NAME
from tests.testsCJM.testPersons.testPersonaViewTestCase import create_test_person, PERSON_ENTITY_NAME


class AccionViewTestCase(FlaskClientBaseTestCase):
    STARTING_ID = 1
    ENTITY_NAME = 'acciones'
    NUMBER_ACTIONS = 1

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

    def setUp(self):
        super(AccionViewTestCase, self).setUp()
        create_test_client(self)
        create_test_person(self)

        self.initial_time = "19900101160101"
        self.name_template = "nombre{0}"
        self.amount = 5

    def test_non_existent_person(self):
        id_not_existent_person = self.expected_ids[PERSON_ENTITY_NAME] + 1
        self.do_get_request("/clients/{0}/persons/{1}/actions/"
                            .format(self.expected_ids[CLIENT_ENTITY_NAME], id_not_existent_person), expected_code=404)

    def test_empty_actions_view(self):
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.assertTrue(isinstance(results, list))
        self.assertEqual(len(results), 0)

    def test_create_valid_actions(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_create_valid_actions_without_initial_time(self):
        self.initial_time = None
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))

        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_create_valid_actions_with_second_time_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:47AM"
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_create_invalid_actions_without_initial_time(self):
        self.initial_time = ""
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_month_for_initial_time(self):
        self.initial_time = "19902001160101"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_days_for_initial_time(self):
        self.initial_time = "19900230160101"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_hours_for_initial_time(self):
        self.initial_time = "19900202300101"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_minutes_for_initial_time(self):
        self.initial_time = "19900202206101"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_seconds_for_initial_time(self):
        self.initial_time = "19900202200161"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_format_for_initial_time(self):
        self.initial_time = "INVALID_DATETIME"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_month_with_second_format_for_initial_time(self):
        self.initial_time = "INVALID 01, 2016 at 11:47AM"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_day_with_second_format_for_initial_time(self):
        self.initial_time = "June 40, 2016 at 11:47AM"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_hour_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 13:47AM"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_minute_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:62AM"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_ampm_with_second_format_for_initial_time(self):
        self.initial_time = "June 01, 2016 at 11:30CM"
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_without_name(self):
        self.name_template = ""
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_without_amount(self):
        self.amount = ""
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_invalid_actions_with_invalid_amount(self):
        self.amount = 0
        self.create_test_actions(expected_code=400)
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, 0)

    def test_create_actions_on_multiple_persons(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        current_action_id = self.expected_ids[self.ENTITY_NAME]
        self.clean_test_data()
        self.expected_ids[self.ENTITY_NAME] = current_action_id
        self.TEST_PERSON_DOCUMENT_NUMBER += "1"
        create_test_person(self, create_new_person=True)
        self.prepend_to_templates(str(self.expected_ids[PERSON_ENTITY_NAME]))

        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_delete_action(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.delete_test_actions()

    def test_delete_invalid_non_existent_action(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.delete_test_actions(offset=self.NUMBER_ACTIONS, expected_code=404)

    def test_delete_action_as_events(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.delete_test_actions(delete_as_event=True)

    def test_delete_invalid_non_existent_action_as_events(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.delete_test_actions(offset=self.NUMBER_ACTIONS, delete_as_event=True, expected_code=404)

    def test_update_valid_actions(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = "20000101160101"
        self.new_name_template = "New name{0}"
        self.new_amount = 10

        self.update_test_actions()

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_update_invalid_non_existent_actions(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = "20000101160101"
        self.new_name_template = "New name{0}"
        self.new_amount = 10

        self.update_test_actions(offset=self.NUMBER_ACTIONS, expected_code=404)

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_update_valid_actions_without_initial_time(self):
        self.initial_time = None
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = None
        self.new_name_template = "New name{0}"
        self.new_amount = 10

        self.update_test_actions()

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_update_invalid_actions_with_empty_initial_time(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = ""
        self.new_name_template = "New name{0}"
        self.new_amount = 10

        self.update_test_actions(expected_code=400)

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_update_invalid_actions_with_invalid_initial_time(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = "20000230160101"
        self.new_name_template = "New name{0}"
        self.new_amount = 10

        self.update_test_actions(expected_code=400)

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_update_invalid_actions_without_name(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = "20000101160101"
        self.new_name_template = ""
        self.new_amount = 10

        self.update_test_actions(expected_code=400)

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def test_update_invalid_actions_with_invalid_amount(self):
        self.create_test_actions()
        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

        self.new_initial_time = "20000101160101"
        self.new_name_template = "New name{0}"
        self.new_amount = -1

        self.update_test_actions(expected_code=400)

        results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                      .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                              self.expected_ids[PERSON_ENTITY_NAME]))
        self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS)

    def delete_test_actions(self, offset=0, delete_as_event=False, expected_code=200):
        for action_number in range(0, self.NUMBER_ACTIONS):
            delete_action(self,
                          action_number + offset,
                          self.initial_time,
                          self.name_template.format(action_number),
                          self.amount,
                          delete_as_event=delete_as_event,
                          expected_code=expected_code)

            if expected_code == 200:
                results = self.do_get_request("/clients/{0}/persons/{1}/actions/"
                                              .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.expected_ids[PERSON_ENTITY_NAME]))
                if action_number + 1 == self.NUMBER_ACTIONS:
                    from commons.validations import DEFAULT_DATETIME_FORMAT
                    date_initial_time = validate_datetime(self.initial_time, Accion.INITIAL_TIME_NAME)
                    initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)

                    self.remove_data_value(self.ENTITY_NAME, Accion.INITIAL_TIME_NAME, initial_time)
                    self.remove_data_value(self.ENTITY_NAME, Accion.AMOUNT_NAME, self.amount)
                    self.remove_data_value(self.ENTITY_NAME, Accion.NUMBER_NAME, self.amount)
                    self.remove_data_value(self.ENTITY_NAME, Accion.TYPE_NAME, TipoEvento.STRING_ACTION)

                self.check_list_response(self.ENTITY_NAME, results, self.NUMBER_ACTIONS - action_number - 1)

    def update_test_actions(self, offset=0, expected_code=200):
        for action_number in range(0, self.NUMBER_ACTIONS):
            update_action(self,
                          action_number + offset,
                          self.new_initial_time,
                          self.new_name_template.format(action_number),
                          self.new_amount,
                          self.initial_time,
                          self.name_template.format(action_number),
                          self.amount,
                          expected_code=expected_code)

    def create_test_actions(self, expected_code=200):
        for action_number in range(0, self.NUMBER_ACTIONS):
            create_action(self,
                          self.initial_time,
                          self.name_template.format(action_number),
                          self.amount,
                          expected_code=expected_code)

    def prepend_to_templates(self, prefix):
        self.name_template = "{0}{1}".format(prefix, self.name_template)

ACTION_ENTITY_NAME = AccionViewTestCase.ENTITY_NAME


def create_action(test_class, initial_time, name, amount, expected_code=200):
    create_test_person(test_class)

    if AccionViewTestCase.ENTITY_NAME not in test_class.expected_ids:
        test_class.expected_ids[AccionViewTestCase.ENTITY_NAME] = AccionViewTestCase.STARTING_ID
    else:
        test_class.expected_ids[AccionViewTestCase.ENTITY_NAME] += 1

    result = test_class.do_post_request("/clients/{0}/persons/{1}/actions/"
                                        .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                test_class.expected_ids[PERSON_ENTITY_NAME]),
                                        data={Accion.INITIAL_TIME_NAME: initial_time,
                                              Accion.ACTION_NAME_NAME: name,
                                              Accion.AMOUNT_NAME: amount},
                                        expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_initial_time = validate_datetime(initial_time, Accion.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.INITIAL_TIME_NAME, initial_time)

        expected_number = amount
        expected_type = TipoEvento.STRING_ACTION
        expected_description = name

        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.ACTION_NAME_NAME, name)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.AMOUNT_NAME, amount)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.TYPE_NAME, expected_type)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.NUMBER_NAME, expected_number)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.DESCRIPTION_NAME, expected_description)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME,
                                  Accion.ID_NAME, test_class.expected_ids[AccionViewTestCase.ENTITY_NAME])

        validate_action(test_class, result, initial_time, name, amount,
                        expected_type, expected_number, expected_description,
                        test_class.expected_ids[AccionViewTestCase.ENTITY_NAME])

        validate_action(test_class,
                        test_class.do_get_request("/clients/{0}/persons/{1}/actions/{2}/".
                                                  format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                         test_class.expected_ids[PERSON_ENTITY_NAME],
                                                         test_class.expected_ids[AccionViewTestCase.ENTITY_NAME])),
                        initial_time, name, amount,
                        expected_type, expected_number, expected_description,
                        test_class.expected_ids[AccionViewTestCase.ENTITY_NAME])
    return result


def delete_action(test_class, action_number, initial_time, name, amount, delete_as_event=False, expected_code=200):
    id_action = AccionViewTestCase.STARTING_ID + action_number

    if delete_as_event:
        url = "/clients/{0}/persons/{1}/actions/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                             test_class.expected_ids[PERSON_ENTITY_NAME],
                                                             id_action)
    else:
        url = "/clients/{0}/persons/{1}/events/{2}/".format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                            test_class.expected_ids[PERSON_ENTITY_NAME],
                                                            id_action)

    result = test_class.do_delete_request(url,
                                          expected_code=expected_code)
    if expected_code == 200:
        expected_number = amount

        expected_type = TipoEvento.STRING_ACTION

        expected_description = name
        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.ACTION_NAME_NAME, name)
        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.DESCRIPTION_NAME, expected_description)

        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.ID_NAME, id_action)

        validate_action(test_class, result, initial_time, name, amount,
                        expected_type, expected_number, expected_description,
                        id_action)

        test_class.do_get_request(url, expected_code=404)
    return result


def update_action(test_class, action_number, initial_time, name, amount,
                  old_initial_time, old_name, old_amount,
                  expected_code=200):
    id_action = AccionViewTestCase.STARTING_ID + action_number

    result = test_class.do_put_request("/clients/{0}/persons/{1}/actions/{2}/"
                                       .format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                               test_class.expected_ids[PERSON_ENTITY_NAME],
                                               id_action),
                                       data={Accion.INITIAL_TIME_NAME: initial_time,
                                             Accion.ACTION_NAME_NAME: name,
                                             Accion.AMOUNT_NAME: amount},
                                       expected_code=expected_code)
    if expected_code == 200:
        if initial_time is not None and old_initial_time is not None:
            from commons.validations import DEFAULT_DATETIME_FORMAT
            date_old_initial_time = validate_datetime(old_initial_time, Accion.INITIAL_TIME_NAME)
            old_initial_time = date_old_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.INITIAL_TIME_NAME, old_initial_time)

            date_initial_time = validate_datetime(initial_time, Accion.INITIAL_TIME_NAME)
            initial_time = date_initial_time.strftime(DEFAULT_DATETIME_FORMAT)
            test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.INITIAL_TIME_NAME, initial_time)

        old_expected_number = old_amount
        expected_number = amount

        old_expected_description = old_name
        expected_description = name

        expected_type = TipoEvento.STRING_ACTION

        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.ACTION_NAME_NAME, old_name)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.ACTION_NAME_NAME, name)

        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.DESCRIPTION_NAME, old_expected_description)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.DESCRIPTION_NAME, expected_description)

        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.AMOUNT_NAME, old_amount)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.AMOUNT_NAME, amount)

        test_class.remove_data_value(AccionViewTestCase.ENTITY_NAME, Accion.NUMBER_NAME, old_expected_number)
        test_class.add_data_value(AccionViewTestCase.ENTITY_NAME, Accion.NUMBER_NAME, expected_number)

        validate_action(test_class, result, initial_time, name, amount,
                        expected_type, expected_number, expected_description, id_action)

        validate_action(test_class,
                        test_class.do_get_request("/clients/{0}/persons/{1}/actions/{2}/".
                                                  format(test_class.expected_ids[CLIENT_ENTITY_NAME],
                                                         test_class.expected_ids[PERSON_ENTITY_NAME],
                                                         id_action)),
                        initial_time, name, amount,
                        expected_type, expected_number, expected_description, id_action)
    return result


def validate_action(test_class, result, initial_time, name, amount,
                    expected_type, expected_number, expected_description, expected_id):
    test_class.assertTrue(isinstance(result, dict))
    if initial_time is not None:
        test_class.assertEqual(result[Accion.INITIAL_TIME_NAME], initial_time)
    test_class.assertEqual(result[Accion.ACTION_NAME_NAME], name)
    test_class.assertEqual(result[Accion.AMOUNT_NAME], amount)
    test_class.assertEqual(result[Accion.TYPE_NAME], expected_type)
    test_class.assertEqual(result[Accion.NUMBER_NAME], expected_number)
    test_class.assertEqual(result[Accion.DESCRIPTION_NAME], expected_description)
    test_class.assertEqual(result[Accion.ID_NAME], expected_id)


def create_test_action(test_class, create_new_action=False, create_as_event=False):
    original_entity_name = AccionViewTestCase.ENTITY_NAME
    if create_as_event:
        AccionViewTestCase.ENTITY_NAME = EVENT_ENTITY_NAME
    result = None

    if AccionViewTestCase.ENTITY_NAME not in test_class.expected_ids or create_new_action:
        result = create_action(test_class,
                               test_class.TEST_ACTION_INITIAL_TIME,
                               test_class.TEST_ACTION_NAME,
                               test_class.TEST_ACTION_AMOUNT)[Accion.ID_NAME]

    AccionViewTestCase.ENTITY_NAME = original_entity_name
    return result


if __name__ == '__main__':
    unittest.main()
