# -*- coding: utf-8 -*
import json

import itertools

from commons.entidades.logs.SuccessLog import SuccessLog
from commons.entidades.users.Usuario import Usuario


class UserTransactions:
    USERNAME_NAME = Usuario.USERNAME_NAME
    TRANSACTIONS_NAME = "transactions"
    TOTAL_NAME = "total"

    def __init__(self, user_key):
        if user_key is None:
            self.username = SuccessLog.ANONYMOUS
        else:
            self.username = user_key.id()
        self.transactions = []
        self.total = 0

    def add_transactions(self, transactions):
        self.transactions = itertools.chain(self.transactions, transactions)
        for transaction in transactions:
            self.total += transaction.value

    def to_dict(self):
        fields_dict = dict()
        fields_dict[UserTransactions.USERNAME_NAME] = self.username
        self.transactions = list(self.transactions)
        self.transactions.sort(key=lambda trans: trans.transaction_time)
        fields_dict[UserTransactions.TRANSACTIONS_NAME] = [transaction.to_dict() for transaction in self.transactions]
        fields_dict[UserTransactions.TOTAL_NAME] = self.total
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
