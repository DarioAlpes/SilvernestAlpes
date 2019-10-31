# -*- coding: utf-8 -*
import unittest
import json
import os
import appengine_config

from google.appengine.datastore import datastore_stub_util
from google.appengine.ext import testbed
from google.appengine.ext import deferred

from commons.entidades.logs.SuccessLog import SuccessLog
from tests.errorDefinitions.errorConstants import MESSAGE_NAME, INTERNAL_ERROR_CODE_NAME, \
    GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE


# noinspection PyCompatibility
class FlaskClientBaseTestCase(unittest.TestCase):

    CHECK_BY_GET = True
    ENTITY_DOES_NOT_EXISTS_CODE = GLOBAL_SENSOR_DOES_NOT_EXISTS_CODE
    ENTITY_NAME = u"BASE_ENTITY"
    NUMBER_OF_ENTITIES = 1
    ENTITIES_PER_REQUEST = 1
    RESOURCE_URL = None
    SPECIFIC_RESOURCE_BASE_URL = None
    ID_NAME = u"id"
    ATTRIBUTES_NAMES_BY_FIELDS = dict()
    PATCH_FIELDS = set()

    def setUp(self):
        self.testbed = testbed.Testbed()
        self.testbed.activate()
        # Create a consistency policy that will simulate the High Replication consistency model.
        self.policy = datastore_stub_util.PseudoRandomHRConsistencyPolicy(probability=1)
        # Initialize the datastore stub with this policy.
        self.testbed.init_datastore_v3_stub(consistency_policy=self.policy)
        self.testbed.init_memcache_stub()
        self.testbed.init_user_stub()
        self.testbed.init_taskqueue_stub(root_path=os.path.dirname(appengine_config.__file__))
        self.taskqueue_stub = self.testbed.get_stub(testbed.TASKQUEUE_SERVICE_NAME)
        import main
        main.app.config['TESTING'] = True
        # main.app.config['PROPAGATE_EXCEPTIONS'] = False
        # main.app.config['PRESERVE_CONTEXT_ON_EXCEPTION'] = False
        main.app.testing = True
        self.app = main.app.test_client()
        main.prepare_for_testing(num_default_locations=2)
        self.clean_test_data(clean_all_ids=True)
        self.overwrite_values = dict()
        self.original_entities = []

    def tearDown(self):
        self.testbed.deactivate()
        self._delete_data()

    def _delete_data(self):
        del self.test_data
        del self.backup_data
        del self.expected_ids

    def clean_test_data(self, clean_all_ids=False):
        self.test_data = dict()
        self.backup_data = dict()
        if clean_all_ids:
            self.expected_ids = dict()
        elif self.ENTITY_NAME in self.expected_ids:
            del self.expected_ids[self.ENTITY_NAME]
        self.original_entities = []

    def do_get_request(self, url, expected_code=200):
        previous_success_logs = FlaskClientBaseTestCase._get_number_success_logs(expected_code)
        result = self.app.get(url)
        return self._check_and_return_response(result, previous_success_logs, expected_code)

    def do_delete_request(self, url, expected_code=200):
        previous_success_logs = FlaskClientBaseTestCase._get_number_success_logs(expected_code)
        result = self.app.delete(url)
        return self._check_and_return_response(result, previous_success_logs, expected_code)

    def do_post_request(self, url, data, expected_code=200):
        previous_success_logs = FlaskClientBaseTestCase._get_number_success_logs(expected_code)
        result = self.app.post(url,
                               data=json.dumps(dict(data)),
                               content_type="application/json")
        return self._check_and_return_response(result, previous_success_logs, expected_code)

    def do_put_request(self, url, data, expected_code=200):
        previous_success_logs = FlaskClientBaseTestCase._get_number_success_logs(expected_code)
        result = self.app.put(url,
                              data=json.dumps(dict(data)),
                              content_type="application/json")
        return self._check_and_return_response(result, previous_success_logs, expected_code)

    def do_patch_request(self, url, data, expected_code=200):
        previous_success_logs = FlaskClientBaseTestCase._get_number_success_logs(expected_code)
        result = self.app.patch(url,
                                data=json.dumps(dict(data)),
                                content_type="application/json")
        return self._check_and_return_response(result, previous_success_logs, expected_code)

    @staticmethod
    def _get_number_success_logs(expected_code=200):
        if expected_code == 200:
            return SuccessLog.count()
        return None

    def _check_and_return_response(self, result, previous_success_logs, expected_code=200):
        list_response = list(result.response)
        self.assertGreaterEqual(len(list_response), 1)
        self.assertEqual(expected_code, result.status_code)
        if expected_code == 200:
            self.assertTrue(previous_success_logs < SuccessLog.count())
        return json.loads(list_response[0])

    def change_ids_to_non_existent_entities(self):
        for entity in self.original_entities:
            if isinstance(entity[self.ID_NAME], basestring):
                entity[self.ID_NAME] += str(2000 * self.NUMBER_OF_ENTITIES)
            else:
                entity[self.ID_NAME] += 2000 * self.NUMBER_OF_ENTITIES

    @staticmethod
    def freeze_value(value):
        if isinstance(value, dict):
            return frozenset((key, FlaskClientBaseTestCase.freeze_value(dict_value))
                             for key, dict_value in value.iteritems())
        elif isinstance(value, list):
            return tuple(FlaskClientBaseTestCase.freeze_value(list_value) for list_value in value)
        else:
            return value

    def add_data_value(self, entity_name, key, value):
        value = FlaskClientBaseTestCase.freeze_value(value)
        if entity_name not in self.test_data:
            self.test_data[entity_name] = dict()
        if key not in self.test_data[entity_name]:
            self.test_data[entity_name][key] = set()
        self.test_data[entity_name][key].add(value)

    def save_and_clear_current_data(self, entity_name):
        self.backup_data[entity_name] = self.test_data[entity_name]
        self.test_data[entity_name] = dict()

    def restore_saved_data(self, entity_name):
        self.test_data[entity_name] = self.backup_data[entity_name]

    def join_current_data_with_saved_data(self, entity_name):
        for key in self.test_data[entity_name]:
            self.test_data[entity_name][key] |= self.backup_data[entity_name][key]

    def get_data_values(self, entity_name, key):
        return self.test_data.get(entity_name, dict()).get(key, dict())

    def get_saved_data_values(self, entity_name, key):
        return self.backup_data[entity_name][key]

    def clean_data_values(self, entity_name, key):
        if entity_name in self.test_data and key in self.test_data[entity_name]:
            self.test_data[entity_name][key] = set()

    def remove_data_value(self, entity_name, key, value):
        value = FlaskClientBaseTestCase.freeze_value(value)
        if entity_name in self.test_data:
            if key in self.test_data[entity_name]:
                self.test_data[entity_name][key].discard(value)

    @staticmethod
    def flat_list(list_to_flatten):
        if any([isinstance(item, list) for item in list_to_flatten]):
            return [item for sublist in list_to_flatten for item in FlaskClientBaseTestCase.flat_list(sublist)]
        else:
            return list_to_flatten

    def check_list_response(self, entity_name, list_response, number_of_created_entities, number_of_default_entities=0):
        self.assertTrue(isinstance(list_response, list))
        self.assertEqual(len(list_response),
                         number_of_created_entities * self.ENTITIES_PER_REQUEST + number_of_default_entities)
        result_data = dict()
        if entity_name in self.test_data:
            for data_key in self.test_data[entity_name]:
                result_data[data_key] = set()

            for top_response in list_response:
                if not isinstance(top_response, list):
                    top_response = [top_response]
                for response in top_response:
                    self.assertTrue(isinstance(response, dict))
                    for data_key in self.test_data[entity_name]:
                        if data_key in response:
                            result_data[data_key].add(FlaskClientBaseTestCase.freeze_value(response[data_key]))

            for data_key in self.test_data[entity_name]:
                if number_of_default_entities == 0:
                    self.assertEqual(self.test_data[entity_name][data_key], result_data[data_key], u"{0}, {1} <> {2}".format(data_key,
                                                                                                                             self.test_data[entity_name][data_key],
                                                                                                                             result_data[data_key]))
                else:
                    self.assertGreaterEqual(result_data[data_key], self.test_data[entity_name][data_key])

    def get_entity_values_for_create(self, entity_number):
        """
        Da los valores a usar para crear la entidad número entity_number
        Para cada campo el valor va a ser:
            1. Si se asigno un valor especifico de pruebas (ver assign_field_value) el valor asignado
            2. Si existe template (ver get_entity_values_templates_for_create)
                2.1. Si existe tipo asociado al template (ver get_types_for_template_parsing) va a ser
                        tipo(template.format(entity_number)).
                2.2 En caso contrario template.format(entity_number)
            3. En caso contrario el valor estatico por defecto (ver get_static_entity_values_for_create)
        :param entity_number: Número de la entidad a crear
        :return: Valores a usar para crear la entidad número entity_number
        """
        static_values = self.get_static_entity_values_for_create()
        templates = self.get_entity_values_templates_for_create()
        template_types = self.get_types_for_template_parsing()
        return self._calculate_values(static_values, templates, template_types, self.overwrite_values, entity_number)

    def get_entity_values_for_update(self, entity_number):
        """
        Da los valores a usar para crear la entidad número entity_number
        Para cada campo el valor va a ser:
            1. Si se asigno un valor especifico de pruebas (ver assign_field_value) el valor asignado
            2. Si existe template (ver get_entity_values_templates_for_update)
                2.1. Si existe tipo asociado al template (ver get_types_for_template_parsing) va a ser
                        tipo(template.format(entity_number)).
                2.2 En caso contrario template.format(entity_number)
            3. En caso contrario el valor estatico por defecto (ver get_static_entity_values_for_update)
        :param entity_number: Número de la entidad a crear
        :return: Valores a usar para crear la entidad número entity_number
        """
        static_values = self.get_static_entity_values_for_update()
        templates = self.get_entity_values_templates_for_update()
        template_types = self.get_types_for_template_parsing()
        return self._calculate_values(static_values, templates, template_types, self.overwrite_values, entity_number)

    def _get_entity_values_for_patch(self, entity_number):
        values = self.get_entity_values_for_update(entity_number)
        new_values = dict(values)
        for key in values:
            if key not in self.PATCH_FIELDS:
                del new_values[key]
        return new_values

    def assign_field_value(self, field_name, value):
        """
        Asigna un valor especifico a un campo dado para todas las entidades a crear/actualizar.
        :param field_name: Nombre del campo
        :param value: Valor del campo
        """
        self.overwrite_values[field_name] = value

    @classmethod
    def get_static_entity_values_for_create(cls):
        """
        Se debe sobreescribir en cada subclase
        Da los valores de los campos de la entidad que NO deben cambiar por cada entidad creada
        :return: Diccionario con los valores de los campos a que NO deben cambiar en cada iteración
        """
        return dict()

    @classmethod
    def get_entity_values_templates_for_create(cls):
        """
        Se debe sobreescribir en cada subclase
        Da templates para los campos de la entidad que deben cambiar por cada entidad creada
        Los templates deben ser de tipo str
        :return: Diccionario con los templates de los campos a que deben cambiar en cada iteración
        """
        return dict()

    @classmethod
    def get_types_for_template_parsing(cls):
        """
        Se debe sobreescribir en cada subclase
        Indica los tipos a los que se deben parsear los campos asociados a templates
        (ver get_entity_values_templates_for_create y get_entity_values_templates_for_update)
        después de aplicar el template. Solo aplica para los campos con un template asociado. En caso de no
        asociar un tipo se supone que el campo es de tipo str
        :return: Diccionario con los tipos a los que se deben parsear los campos asociados a templates
        """
        return dict()

    @classmethod
    def get_static_entity_values_for_update(cls):
        """
        Se recomienda sobreescribir en cada subclase
        Da los valores de los campos de la entidad que NO deben cambiar por cada entidad actualizada
        :return: Diccionario con los valores de los campos a que NO deben cambiar en cada iteración
        """
        return cls.get_static_entity_values_for_create()

    @classmethod
    def get_entity_values_templates_for_update(cls):
        """
        Se recomienda sobreescribir en cada subclase
        Da templates para los campos de la entidad que deben cambiar por cada entidad actualizada
        Los templates deben ser de tipo str
        :return: Diccionario con los templates de los campos a que deben cambiar en cada iteración
        """
        return cls.get_entity_values_templates_for_create()

    @classmethod
    def parse_values_to_default_format(cls, request_values, entity_number, is_create):
        """
        Método a sobreescribir para reemplazar los valores con múltiples formatos/valores por defecto por el valor que
        se espera que retorne el servidor
        :param request_values: Valores originales del request
        :return: Valores corregidos del request
        """
        return request_values

    # noinspection PyUnusedLocal
    @classmethod
    def add_generated_ancestor_ids_to_entity(cls, result, runing_entity):
        """
        Método a sobreescribir para asignar los valores de expected_ids autogenereados cuando se crean entidades
        automaticamente en el back
        """
        return result

    def get_base_url(self, entity_test_class):
        """
        Da la url donde se puede crear un recurso de la clase o consultar la lista de recursos
        :return: url donde se puede crear un recurso de la clase o consultar la lista de recursos
        """
        base_url = entity_test_class.RESOURCE_URL
        return base_url.format(*self._get_entities_ids(entity_test_class.get_ancestor_entities_names()))

    def get_base_item_url(self, entity_test_class):
        """
        Da la url a la que se le debe agregar el id de un recurso especifico para consultar
        :return: url a la que se le debe agregar el id de un recurso especifico para consultar
        """
        base_url = entity_test_class.SPECIFIC_RESOURCE_BASE_URL
        if base_url is None:
            base_url = entity_test_class.RESOURCE_URL
        return base_url.format(*self._get_entities_ids(entity_test_class.get_ancestor_entities_names_for_specific_resource()))

    def get_item_url(self, entity_test_class):
        """
        Da la url donde se puede consultar/editar/eliminar un recurso especifico de la clase
        :return: url donde se puede consultar/editar/eliminar un recurso especifico
        """
        return self.get_base_item_url(entity_test_class) + "{0}/"

    @classmethod
    def get_ancestor_entities_names(cls):
        """
        Da la lista de entidades cuyos id se deben reemplazar en la url del recurso en el orden en que aparecen
        :return: Lista de entidades cuyos id se deben reemplazar en la url del recurso en el orden en que aparecen
        """
        return list()

    @classmethod
    def get_ancestor_entities_names_for_specific_resource(cls):
        """
        Da la lista de entidades cuyos id se deben reemplazar en la url del recurso en el orden en que aparecen para
        acceder un recurso especifico
        :return: Lista de entidades cuyos id se deben reemplazar en la url del recurso en el orden en que aparecen
        """
        return cls.get_ancestor_entities_names()

    def _get_entities_ids(self, entities_names):
        return [self.expected_ids[entity_name] for entity_name in entities_names]

    @classmethod
    def get_server_assigned_default_values(cls):
        return dict()

    @classmethod
    def validate_additional_values(cls, running_entity, result):
        default_values = cls.get_server_assigned_default_values()
        for key, value in default_values.iteritems():
            running_entity.assertEqual(value, result.get(key))

    def do_create_requests(self, expected_code=200, expected_internal_code=None, check_results_as_list=True,
                           do_get_and_check_results=True, previously_created_entities=0, number_of_default_entities=0):
        test_class = type(self)
        list_results = []
        for entity_number in range(previously_created_entities, previously_created_entities + self.NUMBER_OF_ENTITIES):
            list_results.append(self.do_single_create_request(test_class,
                                                              self.get_entity_values_for_create(entity_number),
                                                              expected_code,
                                                              expected_internal_code,
                                                              entity_number,
                                                              do_get_and_check_results))

        list_results = FlaskClientBaseTestCase.flat_list(list_results)

        if expected_code == 200:
            expected_number_of_entities = self.NUMBER_OF_ENTITIES + len(self.original_entities)
            self.original_entities += list_results
        else:
            expected_number_of_entities = len(self.original_entities)
        if check_results_as_list and number_of_default_entities == 0:
            self.check_list_response(self.ENTITY_NAME, self.original_entities, expected_number_of_entities)
        if do_get_and_check_results and expected_code != 403:
            self.request_all_resources_and_check_result(expected_number_of_entities,
                                                        number_of_default_entities=number_of_default_entities)
        return list_results

    def do_single_create_request(self, entity_test_class, values, expected_code=200, expected_internal_code=None,
                                 entity_number=None, validate_results_on_success=True):
        base_url = self.get_base_url(entity_test_class)
        entity_name = entity_test_class.ENTITY_NAME
        result = self.do_post_request(base_url,
                                      data=values,
                                      expected_code=expected_code)
        if expected_code == 200:
            values = entity_test_class.parse_values_to_default_format(values, entity_number, is_create=True)
            if isinstance(result, list):
                for index, item in enumerate(result):
                    self._check_create_result(entity_test_class, entity_name, item, values[index],
                                              validate_results_on_success)
            else:
                self._check_create_result(entity_test_class, entity_name, result, values, validate_results_on_success)
        else:
            self.validate_error(result, expected_internal_code)

        return result

    def _check_create_result(self, entity_test_class, entity_name, result, values, validate_results_on_success):
        if entity_test_class.ID_NAME is not None:
            self.expected_ids[entity_name] = result[entity_test_class.ID_NAME]

        if validate_results_on_success and entity_test_class.CHECK_BY_GET:
            if entity_test_class.ID_NAME is not None:
                values[entity_test_class.ID_NAME] = result[entity_test_class.ID_NAME]

            entity_test_class.add_generated_ancestor_ids_to_entity(result, self)

            self._validate_entity_with_result_and_get(entity_test_class, result, values)

            self._add_data_values(entity_test_class, result)

    def do_update_requests(self, expected_code=200, expected_internal_code=None, check_results_as_list=True,
                           do_get_and_check_results=True, number_of_default_entities=0):
        test_class = type(self)
        list_results = []

        for entity_number, original_entity in enumerate(self.original_entities):
            list_results.append(self.do_single_update_request(test_class,
                                                              original_entity,
                                                              self.get_entity_values_for_update(entity_number),
                                                              expected_code,
                                                              expected_internal_code,
                                                              entity_number,
                                                              validate_results_on_success=do_get_and_check_results))

        expected_number_of_entities = len(self.original_entities)

        if check_results_as_list and expected_code == 200:
            self.check_list_response(self.ENTITY_NAME, list_results, expected_number_of_entities)
            self.original_entities = list_results
        if do_get_and_check_results and expected_code != 403:
            self.request_all_resources_and_check_result(expected_number_of_entities,
                                                        number_of_default_entities=number_of_default_entities)
        return list_results

    def do_single_update_request(self, entity_test_class, original_entity, values, expected_code=200,
                                 expected_internal_code=None, entity_number=None, validate_results_on_success=True):
        url_item = self.get_item_url(entity_test_class).format(original_entity[entity_test_class.ID_NAME])
        result = self.do_put_request(url_item,
                                     data=values,
                                     expected_code=expected_code)
        if expected_code == 200:
            if validate_results_on_success:
                values[entity_test_class.ID_NAME] = original_entity[entity_test_class.ID_NAME]
                values = entity_test_class.parse_values_to_default_format(values, entity_number, is_create=False)

                self._validate_entity_with_result_and_get(entity_test_class, result, values)

                self._remove_data_values(entity_test_class, original_entity)
                self._add_data_values(entity_test_class, result)
        else:
            self.validate_error(result, expected_internal_code)

        return result

    def do_patch_requests(self, expected_code=200, expected_internal_code=None, check_results_as_list=True,
                          do_get_and_check_results=True, number_of_default_entities=0):
        test_class = type(self)
        list_results = []

        for entity_number, original_entity in enumerate(self.original_entities):
            list_results.append(self.do_single_patch_request(test_class,
                                                             original_entity,
                                                             self._get_entity_values_for_patch(entity_number),
                                                             expected_code,
                                                             expected_internal_code,
                                                             entity_number,
                                                             validate_results_on_success=do_get_and_check_results))

        expected_number_of_entities = len(self.original_entities)

        if check_results_as_list and expected_code == 200:
            self.check_list_response(self.ENTITY_NAME, list_results, expected_number_of_entities)
            self.original_entities = list_results
        if do_get_and_check_results and expected_code != 403:
            self.request_all_resources_and_check_result(expected_number_of_entities,
                                                        number_of_default_entities=number_of_default_entities)
        return list_results

    def do_single_patch_request(self, entity_test_class, original_entity, values, expected_code=200,
                                expected_internal_code=None, entity_number=None, validate_results_on_success=True):
        url_item = self.get_item_url(entity_test_class).format(original_entity[entity_test_class.ID_NAME])
        result = self.do_patch_request(url_item,
                                       data=values,
                                       expected_code=expected_code)
        if expected_code == 200:
            if validate_results_on_success:
                for key, value in original_entity.iteritems():
                    if key not in values:
                        values[key] = value

                values = entity_test_class.parse_values_to_default_format(values, entity_number, is_create=False)

                self._validate_entity_with_result_and_get(entity_test_class, result, values)

                self._remove_data_values(entity_test_class, original_entity)
                self._add_data_values(entity_test_class, result)
        else:
            self.validate_error(result, expected_internal_code)

        return result

    def do_delete_requests(self, expected_code=200, expected_internal_code=None, do_get_and_check_results=True,
                           filter_function=None, number_of_default_entities=0, check_deleted=True):
        test_class = type(self)

        if filter_function is not None:
            filtered_entities = [entity for entity in self.original_entities if filter_function(entity)]
        else:
            filtered_entities = self.original_entities

        for original_entity in filtered_entities:
            self.do_single_delete_request(test_class,
                                          original_entity,
                                          expected_code,
                                          expected_internal_code,
                                          check_deleted)

        if expected_code == 200:
            if filter_function is not None:
                self.original_entities = [entity for entity in self.original_entities if not filter_function(entity)]
            else:
                self.original_entities = []
            for entity in self.original_entities:
                self._add_data_values(type(self), entity)
        expected_number_of_entities = len(self.original_entities)

        if do_get_and_check_results and expected_code != 403:
            self.request_all_resources_and_check_result(expected_number_of_entities,
                                                        number_of_default_entities=number_of_default_entities)

    def do_single_delete_request(self, entity_test_class, original_entity, expected_code=200,
                                 expected_internal_code=None, check_deleted=True):
        url_item = self.get_item_url(entity_test_class).format(original_entity[entity_test_class.ID_NAME])
        result = self.do_delete_request(url_item,
                                        expected_code=expected_code)
        if expected_code == 200:
            if check_deleted:
                self._validate_entity_with_result_and_get(entity_test_class, result, original_entity, expected_code=404)
            self._remove_data_values(entity_test_class, result)
        else:
            self.validate_error(result, expected_internal_code)

        return result

    def request_all_resources_and_check_result(self, expected_number_of_entities, expected_code=200,
                                               expected_internal_code=None, number_of_default_entities=0):
        get_results = self.do_get_request(self.get_base_url(type(self)), expected_code=expected_code)
        if expected_code == 200:
            self.check_list_response(self.ENTITY_NAME, get_results, expected_number_of_entities,
                                     number_of_default_entities)
        else:
            self.validate_error(get_results, expected_internal_code)

    def request_specific_resource_and_check_result(self, id_resource, expected_code=200, entity_test_class=None,
                                                   values=None):
        if entity_test_class is None:
            entity_test_class = type(self)
        result_get = self.do_get_request(self.get_item_url(entity_test_class)
                                         .format(id_resource),
                                         expected_code=expected_code)
        if expected_code == 200:
            if values is not None:
                self._validate_entity_result(entity_test_class, result_get, values)
        else:
            self.validate_error(result_get, self.ENTITY_DOES_NOT_EXISTS_CODE)

    def validate_error(self, error_result, expected_internal_code):
        if expected_internal_code is not None:
            self.assertEqual(expected_internal_code, error_result[INTERNAL_ERROR_CODE_NAME])
            self.assertTrue(MESSAGE_NAME in error_result)

    @classmethod
    def update_sample_entity_for_another_class(cls, runing_entity, entity_id, expected_code=200,
                                               validate_results_on_success=False):
        overwrite_values = cls._get_class_values_from_running_entity(runing_entity)
        static_values = cls.get_static_entity_values_for_create()
        templates = cls.get_entity_values_templates_for_create()
        template_types = cls.get_types_for_template_parsing()
        values = cls._calculate_values(static_values, templates, template_types, overwrite_values, 0)

        url_item = runing_entity.get_item_url(cls).format(entity_id)
        original_entity = runing_entity.do_get_request(url_item)
        result = runing_entity.do_single_update_request(cls, original_entity, values, expected_code=expected_code,
                                                        validate_results_on_success=validate_results_on_success)
        if expected_code == 200:
            return result[cls.ID_NAME]

    @classmethod
    def _get_class_values_from_running_entity(cls, running_entity):
        overwrite_values = dict()
        for field, attribute in cls.ATTRIBUTES_NAMES_BY_FIELDS.iteritems():
            if hasattr(running_entity, attribute):
                attribute_value = getattr(running_entity, attribute)
                if attribute_value is not None and isinstance(attribute_value, str):
                    # noinspection PyCompatibility
                    attribute_value = unicode(attribute_value, "utf-8")
                overwrite_values[field] = attribute_value
        return overwrite_values

    @classmethod
    def create_sample_entity_for_another_class(cls, runing_entity, create_new_entity, expected_code=200,
                                               validate_results_on_success=False):
        if create_new_entity or cls.ENTITY_NAME not in runing_entity.expected_ids:
            overwrite_values = cls._get_class_values_from_running_entity(runing_entity)
            static_values = cls.get_static_entity_values_for_create()
            templates = cls.get_entity_values_templates_for_create()
            template_types = cls.get_types_for_template_parsing()
            values = cls._calculate_values(static_values, templates, template_types, overwrite_values, 0)
            result = runing_entity.do_single_create_request(cls,
                                                            values,
                                                            expected_code=expected_code,
                                                            expected_internal_code=None,
                                                            validate_results_on_success=validate_results_on_success)
            if expected_code == 200 and cls.ID_NAME is not None:
                if isinstance(result, list):
                    return max([item[cls.ID_NAME] for item in result])
                else:
                    return result[cls.ID_NAME]

    def _validate_entity_with_result_and_get(self, entity_test_class, result, values, expected_code=200):
        self._validate_entity_result(entity_test_class, result, values)

        if entity_test_class.ID_NAME is not None and entity_test_class.CHECK_BY_GET:
            self.request_specific_resource_and_check_result(result[entity_test_class.ID_NAME], expected_code,
                                                            entity_test_class, values)

    def _add_data_values(self, entity_test_class, result):
        if isinstance(result, list):
            for item_result in result:
                self._add_data_values(entity_test_class, item_result)
        else:
            for key, value in result.iteritems():
                self.add_data_value(entity_test_class.ENTITY_NAME, key, value)

    def _remove_data_values(self, entity_test_class, result):
        for key, value in result.iteritems():
            self.remove_data_value(entity_test_class.ENTITY_NAME, key, value)

    def _validate_entity_result(self, entity_test_class, result, values):
        self.check_equals(values, result, "result")
        entity_test_class.validate_additional_values(self, result)

    def check_equals(self, original_value, result_value, key):
        if isinstance(original_value, dict):
            for key, value in original_value.iteritems():
                self.check_equals(value, result_value.get(key), key)
        elif isinstance(original_value, list):
            for original_item, result_item in zip(original_value, result_value):
                self.check_equals(original_item, result_item, key + "_item")
        else:
            self.assertEqual(original_value, result_value, u"{0}: {1} <> {2}".format(key, original_value, result_value))

    @classmethod
    def _calculate_values(cls, static_values, templates, template_types, overwrite_values, entity_number):
        values = static_values
        for key, template in templates.iteritems():
            str_value = template.format(entity_number)

            template_type = template_types.get(key)
            if template_type is None:
                values[key] = str_value
            else:
                values[key] = template_type(str_value)
        for key, value in overwrite_values.iteritems():
            values[key] = value
        return values

    @staticmethod
    def run_deferred(task):
        return deferred.run(task.payload)

    def _check_permission_for_function(self, action_function, allowed_roles, required_locations):
        from tests.testCommons.testLocations.testUbicacionViewTestCase import create_test_location, LOCATION_ENTITY_NAME
        from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ROLES, login, \
            DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD, create_test_user, logout, CLIENT_CASHIER_USER, \
            CLIENT_WAITER_USER, CLIENT_CASHIER_WAITER_USER

        # Test with global admin user
        login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
              use_client_url=False, expected_code=200)
        action_function(expected_code=200, is_global_admin=True)

        # Test while not logged in
        logout(self, DEFAULT_GLOBAL_ADMIN_USER,
               use_client_url=False, expected_code=200)
        action_function(expected_code=401, do_login_if_required=False)
        # CLIENT_CASHIER_WAITER_USER debe tener todos los permisos de CLIENT_CASHIER_USER más CLIENT_WAITER_USER (y solo esos)
        if CLIENT_CASHIER_USER in allowed_roles or CLIENT_WAITER_USER in allowed_roles:
            allowed_roles.add(CLIENT_CASHIER_WAITER_USER)
        elif CLIENT_CASHIER_WAITER_USER in allowed_roles:
            allowed_roles.remove(CLIENT_CASHIER_WAITER_USER)
        all_roles = CLIENT_ROLES
        for role in all_roles:
            if role in allowed_roles:
                expected_code = 200
            else:
                expected_code = 403

            # Test with a user with the correct role and no locations
            login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                  use_client_url=False, expected_code=200)
            self.TEST_USER_USERNAME = "test_user{0}".format(role)
            self.TEST_USER_PASSWORD = "test_password1{0}".format(role)
            self.TEST_USER_ROLE = role
            self.TEST_USER_IDS_LOCATIONS = None
            create_test_user(self, create_new_user=True)
            login(self, self.TEST_USER_USERNAME, self.TEST_USER_PASSWORD,
                  use_client_url=True, expected_code=200)

            action_function(expected_code=expected_code)

            # Test with a user with the correct role and all required locations
            if len(required_locations) > 0:
                login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                      use_client_url=False, expected_code=200)
                self.TEST_USER_USERNAME = "test_user_with_locations{0}".format(role)
                self.TEST_USER_PASSWORD = "test_password1_with_locations{0}".format(role)
                self.TEST_USER_ROLE = role
                self.TEST_USER_IDS_LOCATIONS = list(required_locations)
                create_test_user(self, create_new_user=True)

                login(self, self.TEST_USER_USERNAME, self.TEST_USER_PASSWORD,
                      use_client_url=True, expected_code=200)
                action_function(expected_code=expected_code)

            # Test with a user with the correct role and all but one of the locations
            ids_locations_test = set(required_locations)
            for location_number, location_id in enumerate(required_locations):
                ids_locations_test.remove(location_id)

                if len(ids_locations_test) > 0:
                    login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                          use_client_url=False, expected_code=200)
                    self.TEST_USER_USERNAME = "test_user{0}{1}".format(role, location_number)
                    self.TEST_USER_PASSWORD = "test_password1{0}{1}".format(role, location_number)
                    self.TEST_USER_ROLE = role
                    self.TEST_USER_IDS_LOCATIONS = list(ids_locations_test)
                    create_test_user(self, create_new_user=True)

                    login(self, self.TEST_USER_USERNAME, self.TEST_USER_PASSWORD,
                          use_client_url=True, expected_code=200)
                    action_function(expected_code=403)

                    ids_locations_test.add(location_id)

            # Test with a user with the correct role and one new location
            login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                  use_client_url=False, expected_code=200)
            id_location = self.expected_ids.get(LOCATION_ENTITY_NAME)
            create_test_location(self, create_new_location=True)
            self.TEST_USER_USERNAME = "test_user_with_wrong_location{0}".format(role)
            self.TEST_USER_PASSWORD = "test_password1_with_wrong_location{0}".format(role)
            self.TEST_USER_ROLE = role
            self.TEST_USER_IDS_LOCATIONS = [self.expected_ids[LOCATION_ENTITY_NAME]]
            create_test_user(self, create_new_user=True)

            if id_location is not None:
                self.expected_ids[LOCATION_ENTITY_NAME] = id_location

            login(self, self.TEST_USER_USERNAME, self.TEST_USER_PASSWORD,
                  use_client_url=True, expected_code=200)
            action_function(expected_code=403)

    def check_create_permissions(self, allowed_roles, required_locations, number_of_default_entities=0,
                                 do_delete_after_success=False):
        # noinspection PyUnusedLocal
        def create_wrapper(expected_code, is_global_admin=False, do_login_if_required=True):
            self.do_create_requests(expected_code=expected_code,
                                    previously_created_entities=len(self.original_entities),
                                    number_of_default_entities=number_of_default_entities,
                                    check_results_as_list=False,
                                    do_get_and_check_results=False)
            if do_delete_after_success and expected_code == 200:
                from tests.testCommons.testUsers.testUsuarioViewTestCase import login, DEFAULT_GLOBAL_ADMIN_USER, \
                    DEFAULT_GLOBAL_ADMIN_PASSWORD
                login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                      use_client_url=False, expected_code=200)
                self.do_delete_requests()

        self._check_permission_for_function(create_wrapper, allowed_roles, required_locations)

    def check_update_permissions(self, allowed_roles, required_locations):
        # noinspection PyUnusedLocal
        def update_wrapper(expected_code, is_global_admin=False, do_login_if_required=True):
            self.do_update_requests(expected_code=expected_code,
                                    check_results_as_list=False,
                                    do_get_and_check_results=False)

        self._check_permission_for_function(update_wrapper, allowed_roles, required_locations)

    def check_patch_permissions(self, allowed_roles, required_locations):
        # noinspection PyUnusedLocal
        def patch_wrapper(expected_code, is_global_admin=False, do_login_if_required=True):
            self.do_patch_requests(expected_code=expected_code,
                                   check_results_as_list=False,
                                   do_get_and_check_results=False)

        self._check_permission_for_function(patch_wrapper, allowed_roles, required_locations)

    def check_delete_permissions(self, allowed_roles, required_locations, filter_function=None):
        from tests.testCommons.testUsers.testUsuarioViewTestCase import login, logout, DEFAULT_GLOBAL_ADMIN_USER, \
            DEFAULT_GLOBAL_ADMIN_PASSWORD

        # noinspection PyUnusedLocal
        def delete_wrapper(expected_code, is_global_admin=False, do_login_if_required=True):
            if filter_function is None:
                filtered_entitites = self.original_entities
            else:
                filtered_entitites = [entity for entity in self.original_entities if filter_function(entity)]
            if len(filtered_entitites) == 0:
                login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                      use_client_url=False, expected_code=200)
                self.do_create_requests(previously_created_entities=len(self.original_entities),
                                        check_results_as_list=False,
                                        do_get_and_check_results=False)
                logout(self, DEFAULT_GLOBAL_ADMIN_USER, use_client_url=False, expected_code=200)
            
            if do_login_if_required:
                if is_global_admin:
                    login(self, DEFAULT_GLOBAL_ADMIN_USER, DEFAULT_GLOBAL_ADMIN_PASSWORD,
                          use_client_url=False, expected_code=200)
                else:
                    login(self, self.TEST_USER_USERNAME, self.TEST_USER_PASSWORD,
                          use_client_url=True, expected_code=200)
            self.do_delete_requests(expected_code=expected_code, filter_function=filter_function,
                                    do_get_and_check_results=False, check_deleted=False)
            if expected_code == 200 and self.ENTITY_NAME == "clientes":
                self.do_create_requests()

        self._check_permission_for_function(delete_wrapper, allowed_roles, required_locations)

    def check_get_permissions(self, url, allowed_roles, required_locations):
        # noinspection PyUnusedLocal
        def get_wrapper(expected_code, is_global_admin=False, do_login_if_required=True):
            self.do_get_request(url, expected_code)

        self._check_permission_for_function(get_wrapper, allowed_roles, required_locations)


if __name__ == '__main__':
    unittest.main()
