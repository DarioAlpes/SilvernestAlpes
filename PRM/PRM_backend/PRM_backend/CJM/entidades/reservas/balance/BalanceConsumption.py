# -*- coding: utf-8 -*
import json

from CJM.entidades.Moneda import Moneda
from CJM.entidades.paquetes.Acceso import Acceso
from CJM.entidades.reservas.balance.BalanceOverflowState import BalanceOverflowState
from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from CJM.entidades.skus.SKU import SKU
from commons.entidades.locations.Ubicacion import Ubicacion


class BalanceConsumption:
    ID_NAME = "id"
    AVAILABLE_AMOUNT_NAME = "available-amount"
    AVAILABLE_MONEY_NAME = "available-money"
    BALANCE_AMOUNT_NAME = "balance-amount"
    BALANCE_MONEY_NAME = "balance-money"
    UNLIMITED_AMOUNT_NAME = "unlimited-amount"
    CURRENCY_NAME = "currency"

    OVERFLOW_STATE_NAME = "overflow-state"

    CURRENCY_ID_NAME = Moneda.ID_CURRENCY_NAME
    SKU_CATEGORY_ID_NAME = CategoriaSKU.ID_SKU_CATEGORY_NAME
    SKU_ID_NAME = SKU.ID_SKU_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME
    ACCESS_ID_NAME = Acceso.ID_ACCESS_NAME

    def __init__(self, id_currency=None, id_sku_category=None, id_sku=None, balance_amount=None, currency=None,
                 balance_money=None, id_location=None, unlimited_amount=False, is_balance=False):
        self.id_currency = id_currency
        self.currency = currency
        self.id_sku_category = id_sku_category
        self.id_sku = id_sku
        self.id_location = id_location
        self.unlimited_amount = unlimited_amount
        self.balance_money = balance_money
        self.balance_amount = balance_amount
        if self.unlimited_amount:
            self.mark_as_unlimited()
        self.is_balance = is_balance
        self.overflow_state = BalanceOverflowState()

    def add_amount(self, amount):
        if not self.unlimited_amount:
            self.balance_amount += amount
        self.overflow_state.update_state_from_balance(self.balance_amount)

    def consume_all_amount(self, amount):
        if not self.unlimited_amount:
            self.balance_amount -= amount
        self.overflow_state.update_state_from_balance(self.balance_amount)

    def consume_included_amount(self, amount):
        if self.unlimited_amount:
            amount_left = 0
        else:
            available_amount = max(0, self.balance_amount)
            amount_to_consume = min(available_amount, amount)
            self.balance_amount -= amount_to_consume
            amount_left = amount - amount_to_consume
        self.overflow_state.update_state_from_balance(self.balance_amount)
        return amount_left

    def mark_as_unlimited(self):
        self.unlimited_amount = True
        self.balance_amount = 0
        self.overflow_state.update_state_from_balance(self.balance_amount)

    def add_money(self, money):
        self.balance_money += money
        self.overflow_state.update_state_from_balance(self.balance_money)

    def consume_all_money(self, money):
        self.balance_money -= money
        self.overflow_state.update_state_from_balance(self.balance_money)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.OVERFLOW_STATE_NAME] = str(self.overflow_state)
        if self.currency is not None:
            fields_dict[self.CURRENCY_ID_NAME] = self.id_currency
            fields_dict[self.CURRENCY_NAME] = self.currency
            if self.is_balance:
                fields_dict[self.BALANCE_MONEY_NAME] = self.balance_money
            else:
                fields_dict[self.AVAILABLE_MONEY_NAME] = max(0, self.balance_money)
        else:
            if self.id_sku is not None:
                fields_dict[self.SKU_ID_NAME] = self.id_sku
            elif self.id_sku_category is not None:
                fields_dict[self.SKU_CATEGORY_ID_NAME] = self.id_sku_category
            elif self.id_location is not None:
                fields_dict[self.LOCATION_ID_NAME] = self.id_location
                fields_dict[self.UNLIMITED_AMOUNT_NAME] = self.unlimited_amount
            if self.is_balance:
                fields_dict[self.BALANCE_AMOUNT_NAME] = self.balance_amount
            else:
                fields_dict[self.AVAILABLE_AMOUNT_NAME] = max(0, self.balance_amount)
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
