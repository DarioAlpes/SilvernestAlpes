# -*- coding: utf-8 -*
import datetime
import json

from flask import request, Blueprint

from CJM.entidades.reports.transactions.Transaction import Transaction
from CJM.entidades.reports.transactions.UserTransactions import UserTransactions
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.services.validations import TRANSACTIONS_PER_USER_INVALID_INITIAL_TIME_ERROR_CODE, \
    TRANSACTIONS_PER_USER_INVALID_FINAL_TIME_ERROR_CODE
from commons.entidades.logs.SuccessLog import SuccessLog
from commons.entidades.users import Role
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless
from commons.validations import validate_id_client, validate_datetime

TRANSACTIONS_PER_USER_VIEW_NAME = "transactions-per-user"
app = Blueprint(TRANSACTIONS_PER_USER_VIEW_NAME, __name__)

TRANSACTION_KINDS = {Reserva, MoneyTopoff}
TRANSACTION_KINDS_NAMES = {kind.__name__ for kind in TRANSACTION_KINDS}

POSSIBLE_VALUES_NAMES = {Reserva.PAYMENT_NAME, MoneyTopoff.MONEY_NAME}
POSSIBLE_TRANSACTION_NUMBER_NAMES = {Reserva.TRANSACTION_NUMBER_NAME, MoneyTopoff.TRANSACTION_NUMBER_NAME}
POSSIBLE_TRANSACTION_TIME_NAMES = {MoneyTopoff.TOPOFF_TIME_NAME}
POSSIBLE_LIST_FIELDS_NAMES = {MoneyTopoff.TOPOFFS_NAME}

METHODS_CREATE_TRANSACTIONS = {"PATCH", "POST", "PUT"}
METHODS_DELETE_TRANSACTIONS = {"DELETE"}

ALL_METHODS = {"DELETE", "PATCH", "POST", "PUT"}


@app.route('/clients/<int:id_client>/transactions-per-user/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_transactions_per_user(id_client):
    """
    Da la lista de transacciones por usuario
        Parametros esperados en el query string:
            initial-time: Fecha y hora inicial a filtrar, opcional
            final-time: Fecha y hora inicial a filtrar, opcional
    :param id_client: id del cliente asociado
    :return: Lista de transacciones por usuario
    """
    id_client = validate_id_client(id_client)

    on_client_namespace(id_client, _dummy_function_for_login_check,
                        action=Role.READ_ACTION,
                        view=TRANSACTIONS_PER_USER_VIEW_NAME)

    initial_time = request.args.get(Transaction.INITIAL_TIME_NAME)
    if initial_time is not None:
        initial_time = validate_datetime(initial_time, Transaction.INITIAL_TIME_NAME, allow_none=False,
                                         internal_code=TRANSACTIONS_PER_USER_INVALID_INITIAL_TIME_ERROR_CODE)

    final_time = request.args.get(Transaction.FINAL_TIME_NAME)
    final_time_filter = None
    if final_time is not None:
        final_time = validate_datetime(final_time, Transaction.FINAL_TIME_NAME, allow_none=False,
                                       internal_code=TRANSACTIONS_PER_USER_INVALID_FINAL_TIME_ERROR_CODE)
        final_time_filter = final_time + datetime.timedelta(days=2)
    logs = SuccessLog.get_by_client_and_methods_and_between_dates(id_client, TRANSACTION_KINDS_NAMES, ALL_METHODS,
                                                                  initial_time, final_time_filter)
    transactions_lists = logs.map(_get_function_to_parse_to_transactions_with_dates(initial_time, final_time))

    transactions_per_user = dict()
            
    for user_key, transactions in transactions_lists:
        if len(transactions) > 0:
            if user_key not in transactions_per_user:
                transactions_per_user[user_key] = UserTransactions(user_key)

            transactions_per_user[user_key].add_transactions(transactions)
    return transactions_per_user.values()


def _get_function_to_parse_to_transactions_with_dates(initial_time, final_time):

    def _parse_to_transactions(log):
        method = log.method.upper()
        is_delete = False
        if method in METHODS_CREATE_TRANSACTIONS:
            decoded_list = json.loads(log.json_result)
        elif method in METHODS_DELETE_TRANSACTIONS:
            is_delete = True
            decoded_list = json.loads(log.json_result)
        else:
            decoded_list = []

        decoded_list = get_entities_list(decoded_list)

        if not isinstance(decoded_list, list):
            decoded_list = [decoded_list]
        transactions = []
        for decoded_entity in decoded_list:
            transaction_number = _get_transaction_number_from_decoded_entity(decoded_entity)
            if transaction_number is None:
                continue

            value = _get_value_from_decoded_entity(decoded_entity)
            if value is None:
                continue

            transaction_time = _get_transaction_time_from_decoded_entity(decoded_entity)
            if transaction_time is None:
                transaction_time = log.server_date
            elif initial_time is not None and transaction_time < initial_time:
                continue
            elif final_time is not None and transaction_time > final_time:
                continue

            if is_delete:
                value *= -1

            transactions.append(Transaction(value, transaction_number, transaction_time))

        return log.user_key, transactions

    return _parse_to_transactions


def _get_value_from_decoded_entity(decoded_entity):
    value = None
    for value_name in POSSIBLE_VALUES_NAMES:
        value = decoded_entity.get(value_name, None)
        if value is not None:
            break
    return value


def _get_transaction_number_from_decoded_entity(decoded_entity):
    transaction_number = None
    for transaction_number_name in POSSIBLE_TRANSACTION_NUMBER_NAMES:
        transaction_number = decoded_entity.get(transaction_number_name, None)
        if transaction_number is not None:
            break
    return transaction_number


def _get_transaction_time_from_decoded_entity(decoded_entity):
    transaction_time = None
    for transaction_time_name in POSSIBLE_TRANSACTION_TIME_NAMES:
        try:
            transaction_time = decoded_entity.get(transaction_time_name, None)
            if transaction_time is not None:
                transaction_time = validate_datetime(transaction_time, transaction_time_name)
        except ValidationError:
            transaction_time = None
        if transaction_time is not None:
            break
    return transaction_time


def get_entities_list(decoded_entity):
    if isinstance(decoded_entity, list):
        return decoded_entity
    for list_name in POSSIBLE_LIST_FIELDS_NAMES:
        if list_name in decoded_entity:
            return decoded_entity[list_name]
    return decoded_entity


# noinspection PyUnusedLocal
def _dummy_function_for_login_check(id_client):
    pass
