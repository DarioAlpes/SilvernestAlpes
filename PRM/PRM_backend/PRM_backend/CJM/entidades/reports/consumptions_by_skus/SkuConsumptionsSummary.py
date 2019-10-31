# -*- coding: utf-8 -*
import json

from CJM.entidades.reports.consumptions_by_skus.TotalByCurrency import TotalByCurrency
from CJM.entidades.skus.SKU import SKU


class SkuConsumptionsSummary:
    SKU_ID_NAME = SKU.ID_SKU_NAME
    AMOUNT_IN_AMOUNT_CONSUMPTIONS_NAME = "amount-consumed-with-amount"
    AMOUNT_IN_MONEY_CONSUMPTIONS_NAME = "amount-consumed-with-money"
    TOTAL_AMOUNT_CONSUMED_NAME = "total-amount-consumed"
    TOTAL_BY_CURRENCY_NAME = "total-by-currency"

    def __init__(self, id_sku):
        self.id_sku = id_sku
        self.totals_by_currencies = dict()
        self.amount_by_amount_consumptions = 0
        self.amount_by_money_consumptions = 0

    def add_money_consumption(self, money_consumption):
        self.amount_by_money_consumptions += money_consumption.cantidadConsumida
        total_by_currency = self._get_total_by_currency(money_consumption.moneda)
        total_by_currency.add_money(money_consumption.dineroConsumido)
        total_by_currency.add_amount(money_consumption.cantidadConsumida)

    def add_amount_consumption(self, amount_consumption):
        self.amount_by_amount_consumptions += amount_consumption.cantidadConsumida

    def _get_total_by_currency(self, currency):
        if currency not in self.totals_by_currencies:
            self.totals_by_currencies[currency] = TotalByCurrency(currency)
        return self.totals_by_currencies[currency]

    def to_dict(self):
        fields_dict = dict()
        fields_dict[SkuConsumptionsSummary.AMOUNT_IN_AMOUNT_CONSUMPTIONS_NAME] = self.amount_by_amount_consumptions
        fields_dict[SkuConsumptionsSummary.AMOUNT_IN_MONEY_CONSUMPTIONS_NAME] = self.amount_by_money_consumptions
        fields_dict[SkuConsumptionsSummary.TOTAL_AMOUNT_CONSUMED_NAME] = self.amount_by_amount_consumptions + self.amount_by_money_consumptions
        fields_dict[SkuConsumptionsSummary.TOTAL_BY_CURRENCY_NAME] = [total_by_currency.to_dict()
                                                                      for total_by_currency in self.totals_by_currencies.values()]
        fields_dict[SkuConsumptionsSummary.SKU_ID_NAME] = self.id_sku
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
