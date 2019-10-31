# -*- coding: utf-8 -*
import json


class TotalByCurrency:
    MONEY_CONSUMED_NAME = "money-consumed"
    AMOUNT_CONSUMED_NAME = "amount-consumed"
    CURRENCY_NAME = "currency"

    def __init__(self, currency):
        self.currency = currency
        self.amount = 0
        self.money = 0

    def add_money(self, money):
        self.money += money

    def add_amount(self, amount):
        self.amount += amount

    def to_dict(self):
        fields_dict = dict()
        fields_dict[TotalByCurrency.CURRENCY_NAME] = self.currency
        fields_dict[TotalByCurrency.MONEY_CONSUMED_NAME] = self.money
        fields_dict[TotalByCurrency.AMOUNT_CONSUMED_NAME] = self.amount
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())