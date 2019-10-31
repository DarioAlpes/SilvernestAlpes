# -*- coding: utf-8 -*
from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME

ENTITIES_NAME = "entities"
ENTITY_NAME = "entity"
KIND_NAME = "kind"
INCLUDE_DELETED_NAME = "include-deleted"
OPERATION_TIME_NAME = "operation-time"
USERNAME_NAME = "username"
INITIAL_TIME_NAME = "initial-time"
FINAL_TIME_NAME = "final-time"
ENTITY_DELETED_NAME = "entity-deleted"
ADMIN_USERNAME = u"admin"


def create_and_login_new_admin_user(self):
    from tests.testCommons.testUsers.testUsuarioViewTestCase import CLIENT_ADMIN_ROLE, create_test_user, login
    self.TEST_USER_ROLE = CLIENT_ADMIN_ROLE
    create_test_user(self, create_new_user=True)
    login(self, self.TEST_USER_USERNAME, self.TEST_USER_PASSWORD)


def get_and_check_transactions_report(self, expected_transactions, expected_values, num_transactions,
                                      users, initial_date=None, final_date=None, expected_code=200,
                                      expected_internal_code=None):
    filter_str = u""
    if initial_date is not None:
        filter_str = u"?{0}={1}".format(self.INITIAL_TIME_NAME, initial_date)
    if final_date is not None:
        if initial_date is None:
            filter_format = u"?{0}={1}"
        else:
            filter_format = u"&{0}={1}"
        filter_str += filter_format.format(self.FINAL_TIME_NAME, final_date)

    url = u"/clients/{0}/transactions-per-user/" + filter_str

    all_transactions = self.do_get_request(url.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                      self.TEST_USER_USERNAME),
                                           expected_code=expected_code)
    if expected_code == 200:
        # Ignorar usuario admin que crea todas las entidades de prueba
        all_transactions = [transactions_per_user for transactions_per_user in all_transactions
                            if transactions_per_user[self.USERNAME_NAME] != ADMIN_USERNAME]
        self.assertEqual(len(users), len(all_transactions))
        users_to_find = set(users)
        for transactions_per_user in all_transactions:
            user = transactions_per_user[self.USERNAME_NAME]
            self.assertIn(user, users_to_find)
            users_to_find.remove(user)
            total = _check_transactions_for_user(self,
                                                 transactions_per_user[self.TRANSACTIONS_NAME],
                                                 expected_transactions[user],
                                                 expected_values[user],
                                                 num_transactions[user])
            self.assertEqual(total, transactions_per_user[self.TOTAL_NAME])
    else:
        self.validate_error(all_transactions, expected_internal_code)


def _check_transactions_for_user(self, transactions, expected_transactions, expected_values, num_transactions):
    self.assertEqual(num_transactions, len(transactions))
    if num_transactions > 0:
        transactions_numbers = {transaction[self.TRANSACTION_NUMBER_NAME] for transaction in transactions}
        transactions_values = {transaction[self.VALUE_NAME] for transaction in transactions}
        self.assertEqual(transactions_numbers, expected_transactions)
        self.assertEqual(transactions_values, expected_values)

        for transaction in transactions:
            self.assertIn(self.TRANSACTION_TIME_NAME, transaction)
    return sum([transaction[self.VALUE_NAME] for transaction in transactions])


def get_and_check_entities_by_user_report(self, expected_entities_by_user, users, kind,
                                          initial_date=None, final_date=None, include_deleted=None, expected_code=200,
                                          expected_internal_code=None, delete_filter_function=None):
    filter_str = u""
    if kind is not None:
        filter_str = u"?{0}={1}".format(KIND_NAME, kind)
    if include_deleted is not None:
        if len(filter_str) == 0:
            filter_format = u"?{0}={1}"
        else:
            filter_format = u"&{0}={1}"
        filter_str += filter_format.format(INCLUDE_DELETED_NAME, str(include_deleted).lower())
    if initial_date is not None:
        if len(filter_str) == 0:
            filter_format = u"?{0}={1}"
        else:
            filter_format = u"&{0}={1}"
        filter_str += filter_format.format(INITIAL_TIME_NAME, initial_date)
    if final_date is not None:
        if len(filter_str) == 0:
            filter_format = u"?{0}={1}"
        else:
            filter_format = u"&{0}={1}"
        filter_str += filter_format.format(FINAL_TIME_NAME, final_date)

    url = u"/clients/{0}/entities-per-user/" + filter_str

    all_entities = self.do_get_request(url.format(self.expected_ids[CLIENT_ENTITY_NAME],
                                                  self.TEST_USER_USERNAME),
                                       expected_code=expected_code)
    if expected_code == 200:
        self.assertEqual(len(users), len(all_entities))
        users_to_find = set(users)
        for entities_per_user in all_entities:
            user = entities_per_user[USERNAME_NAME]
            self.assertIn(user, users_to_find)
            users_to_find.remove(user)
            _check_entities_for_user(self,
                                     entities_per_user[ENTITIES_NAME],
                                     expected_entities_by_user[user],
                                     include_deleted,
                                     delete_filter_function)
    else:
        self.validate_error(all_entities, expected_internal_code)


def _check_entities_for_user(self, entities, expected_entities, include_deleted, delete_filter_function):
    self.assertEqual(len(entities), len(expected_entities))
    for index, entity_with_date in enumerate(entities):
        self.assertTrue(OPERATION_TIME_NAME in entity_with_date)
        expected_entity = expected_entities[index]
        entity = entity_with_date[ENTITY_NAME]
        for key in expected_entity:
            self.assertEqual(entity[key], expected_entity[key])
        deleted = include_deleted is True and (delete_filter_function is None or delete_filter_function(expected_entity))
        self.assertEqual(deleted, entity[ENTITY_DELETED_NAME])
