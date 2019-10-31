from tests.testCommons.testClients.testClientViewTestCase import CLIENT_ENTITY_NAME
from tests.testCommons.testLocations.testUbicacionViewTestCase import LOCATION_ENTITY_NAME
from tests.testsCJM.testReservas.testReservaPersonaViewTestCase import PERSON_RESERVATION_ENTITY_NAME
from tests.testsCJM.testReservas.testReservaViewTestCase import RESERVATION_ENTITY_NAME

AMOUNT_CONSUMED_NAME = u"amount-consumed"
MONEY_CONSUMED_NAME = u"money-consumed"
CONSUMPTION_TIME_NAME = u"consumption-time"
SKU_ID_NAME = u"id-sku"
SKU_CATEGORY_ID_NAME = u"id-sku-category"
CURRENCY_NAME = u"currency"
CURRENCY_ID_NAME = u"id-currency"
LOCATION_ID_NAME = u"id-location"
RESERVATION_ID_NAME = u"id-reservation"
PERSON_RESERVATION_ID_NAME = u"id-person-reservation"
ID_NAME = u"id"
ORDERS_NAME = u"orders"
AMOUNT_CONSUMPTIONS_NAME = u"amount-consumptions"
MONEY_CONSUMPTIONS_NAME = u"money-consumptions"
ACCESSES_NAME = u"accesses"
MISSING_MONEY_NAME = u"missing-money"
MISSING_AMOUNT_NAME = u"missing-amount"
ORDER_TIME_NAME = u"order-time"
ACCESS_TIME_NAME = u"access-time"
AVAILABLE_AMOUNT_NAME = u"available-amount"
AVAILABLE_MONEY_NAME = u"available-money"
BALANCE_NAME = u"balance"
BALANCE_MONEY_NAME = u"balance-money"
BALANCE_AMOUNT_NAME = u"balance-amount"

OVERFLOW_STATE_NAME = u"overflow-state"
OK_OVERFLOW_STATE = u"ok"
OVERFLOWN_OVERFLOW_STATE = u"overflown"
TEMPORALLY_OVERFLOWN_OVERFLOW_STATE = u"temporally-overflown"

AMOUNT_IN_AMOUNT_CONSUMPTIONS_NAME = u"amount-consumed-with-amount"
AMOUNT_IN_MONEY_CONSUMPTIONS_NAME = u"amount-consumed-with-money"
TOTAL_BY_CURRENCY_NAME = u"total-by-currency"
TOTAL_AMOUNT_CONSUMED_NAME = u"total-amount-consumed"
INITIAL_TIME_NAME = u"initial-time"
FINAL_TIME_NAME = u"final-time"


def _get_available_funds(self):
    self.amount_name = AVAILABLE_AMOUNT_NAME
    self.money_name = AVAILABLE_MONEY_NAME
    return self.do_get_request("/clients/{0}/reservations/{1}/persons-reservations/{2}/available-funds/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[RESERVATION_ENTITY_NAME],
                                       self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]))


def _get_balance(self):
    self.amount_name = BALANCE_AMOUNT_NAME
    self.money_name = BALANCE_MONEY_NAME
    return self.do_get_request("/clients/{0}/reservations/{1}/persons-reservations/{2}/balance/"
                               .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                       self.expected_ids[RESERVATION_ENTITY_NAME],
                                       self.expected_ids[PERSON_RESERVATION_ENTITY_NAME]))


def _get_balance_from_parent(self):
    self.amount_name = BALANCE_AMOUNT_NAME
    self.money_name = BALANCE_MONEY_NAME
    results_list = self.do_get_request("/clients/{0}/reservations/{1}/balance/"
                                       .format(self.expected_ids[CLIENT_ENTITY_NAME],
                                               self.expected_ids[RESERVATION_ENTITY_NAME]))
    self.assertEqual(len(results_list), 1)
    self.assertEqual(self.expected_ids[PERSON_RESERVATION_ENTITY_NAME], results_list[0][PERSON_RESERVATION_ID_NAME])
    return results_list[0][BALANCE_NAME]


def get_and_check_available_funds_for_access(self):
    results = _get_available_funds(self)
    check_available_funds_or_balance_for_access(self, results)


def get_and_check_balance_for_access(self):
    results = _get_balance(self)
    check_available_funds_or_balance_for_access(self, results)


def get_and_check_parent_balance_for_access(self):
    results = _get_balance_from_parent(self)
    check_available_funds_or_balance_for_access(self, results)


def check_available_funds_or_balance_for_access(self, results):
    results_access = [result for result in results
                      if result.get(LOCATION_ID_NAME, "") == self.expected_ids[LOCATION_ENTITY_NAME]]
    self.assertEqual(len(results_access), 1)
    self.assertEqual(results_access[0][self.amount_name], self.expected_amount)
    self.assertEqual(results_access[0][OVERFLOW_STATE_NAME], self.expected_state)
    self.assertEqual(results_access[0][self.UNLIMITED_AMOUNT_NAME], self.is_unlimited)


def get_and_check_available_funds_for_money(self):
    results = _get_available_funds(self)
    check_available_funds_or_balance_for_money(self, results)


def get_and_check_balance_for_money(self):
    results = _get_balance(self)
    check_available_funds_or_balance_for_money(self, results)


def get_and_check_parent_balance_for_money(self):
    results = _get_balance_from_parent(self)
    check_available_funds_or_balance_for_money(self, results)


def check_available_funds_or_balance_for_money(self, results):
    results_currency = [result for result in results
                        if result.get(CURRENCY_NAME, "") == self.included_currency]
    self.assertEqual(len(results_currency), 1)
    self.assertEqual(results_currency[0][self.money_name], self.expected_money)
    self.assertEqual(results_currency[0][OVERFLOW_STATE_NAME], self.expected_state)
    self.assertTrue(CURRENCY_ID_NAME in results_currency[0])
    if self.consumed_currency is not None:
        results_consumed_currency = [result for result in results
                                     if result.get(CURRENCY_NAME, "") == self.consumed_currency]
        self.assertEqual(len(results_consumed_currency), 1)
        self.assertEqual(results_consumed_currency[0][self.money_name], self.expected_money_consumed)
        self.assertEqual(results_consumed_currency[0][OVERFLOW_STATE_NAME], self.expected_state_consumed)
        self.assertTrue(CURRENCY_ID_NAME in results_consumed_currency[0])


def get_and_check_available_funds_for_amount(self):
    results = _get_available_funds(self)
    check_available_funds_or_balance_for_amount(self, results)


def get_and_check_balance_for_amount(self):
    results = _get_balance(self)
    check_available_funds_or_balance_for_amount(self, results)


def get_and_check_parent_balance_for_amount(self):
    results = _get_balance_from_parent(self)
    check_available_funds_or_balance_for_amount(self, results)


def check_available_funds_or_balance_for_amount(self, results):
    if self.id_included_sku is not None:
        results_included_sku = [result for result in results
                                if result.get(SKU_ID_NAME, "") == self.id_included_sku]
        self.assertEqual(len(results_included_sku), 1)
        self.assertEqual(results_included_sku[0][self.amount_name], self.expected_amount_sku)
        self.assertEqual(results_included_sku[0][OVERFLOW_STATE_NAME], self.expected_state_sku)

    if self.id_included_category is not None:
        results_included_category = [result for result in results
                                     if result.get(SKU_CATEGORY_ID_NAME, "") == self.id_included_category]
        self.assertEqual(len(results_included_category), 1)
        self.assertEqual(results_included_category[0][self.amount_name], self.expected_amount_category)
        self.assertEqual(results_included_category[0][OVERFLOW_STATE_NAME], self.expected_state_category)

    if self.id_consumed_sku is not None:
        results_included_sku = [result for result in results
                                if result.get(SKU_ID_NAME, "") == self.id_consumed_sku]
        self.assertEqual(len(results_included_sku), 1)
        self.assertEqual(results_included_sku[0][self.amount_name], self.expected_amount_consumed_sku)
        self.assertEqual(results_included_sku[0][OVERFLOW_STATE_NAME], self.expected_state_consumed_sku)


def get_and_check_skus_consumed_report(self, ids_skus, expected_amounts_by_amount, expected_amounts_by_money,
                                       expected_money_by_sku_by_currency, expected_amount_by_sku_by_currency,
                                       initial_time=None, final_time=None,
                                       expected_code=200, expected_internal_code=None):
    filter_str = u""
    if initial_time is not None:
        filter_str = u"?{0}={1}".format(INITIAL_TIME_NAME, initial_time)
    if final_time is not None:
        if initial_time is None:
            filter_format = u"?{0}={1}"
        else:
            filter_format = u"&{0}={1}"
        filter_str += filter_format.format(FINAL_TIME_NAME, final_time)

    url = u"/clients/{0}/consumptions-per-sku/" + filter_str
    report = self.do_get_request(url.format(self.expected_ids[CLIENT_ENTITY_NAME]),
                                 expected_code=expected_code)
    if expected_code == 200:
        self.assertEqual(len(ids_skus), len(report))
        for sku_report in report:
            id_sku = sku_report[SKU_ID_NAME]
            self.assertIn(id_sku, ids_skus)
            ids_skus.remove(id_sku)
            expected_amount_by_amount = expected_amounts_by_amount.get(id_sku, 0)
            expected_amount_by_money = expected_amounts_by_money.get(id_sku, 0)
            self.assertEqual(expected_amount_by_amount, sku_report[AMOUNT_IN_AMOUNT_CONSUMPTIONS_NAME])
            self.assertEqual(expected_amount_by_money, sku_report[AMOUNT_IN_MONEY_CONSUMPTIONS_NAME])
            self.assertEqual(expected_amount_by_amount + expected_amount_by_money, sku_report[TOTAL_AMOUNT_CONSUMED_NAME])
            expected_money_by_currency = expected_money_by_sku_by_currency.get(id_sku)
            expected_amount_by_currency = expected_amount_by_sku_by_currency.get(id_sku)
            expected_currencies = set(expected_money_by_currency.keys())
            money_consumed_by_currency = sku_report[TOTAL_BY_CURRENCY_NAME]
            for money_consumed in money_consumed_by_currency:
                currency = money_consumed[CURRENCY_NAME]
                self.assertIn(currency, expected_currencies)
                expected_currencies.remove(currency)
                self.assertEqual(expected_money_by_currency.get(currency, 0), money_consumed[MONEY_CONSUMED_NAME])
                self.assertEqual(expected_amount_by_currency.get(currency, 0), money_consumed[AMOUNT_CONSUMED_NAME])
    else:
        self.validate_error(report, expected_internal_code)
