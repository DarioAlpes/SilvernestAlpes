# -*- coding: utf-8 -*
import json


class Transaction:
    VALUE_NAME = "value"
    TRANSACTION_NUMBER_NAME = "transaction-number"
    TRANSACTION_TIME_NAME = "transaction-time"

    INITIAL_TIME_NAME = "initial-time"
    FINAL_TIME_NAME = "final-time"

    def __init__(self, value, transaction_number, transaction_time):
        self.value = value
        self.transaction_number = transaction_number
        self.transaction_time = transaction_time

    def to_dict(self):
        from commons.validations import DEFAULT_DATETIME_FORMAT
        fields_dict = dict()
        fields_dict[Transaction.VALUE_NAME] = self.value
        fields_dict[Transaction.TRANSACTION_NUMBER_NAME] = self.transaction_number
        fields_dict[Transaction.TRANSACTION_TIME_NAME] = self.transaction_time.strftime(DEFAULT_DATETIME_FORMAT)
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
