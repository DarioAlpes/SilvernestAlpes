# -*- coding: utf-8 -*
from CJM.entidades.reports.consumptions_by_skus.SkuConsumptionsSummary import SkuConsumptionsSummary


class SkuConsumptionsReport:
    INITIAL_TIME_NAME = "initial-time"
    FINAL_TIME_NAME = "final-time"

    def __init__(self):
        self.skus_summaries = dict()

    def add_money_consumption(self, money_consumption):
        sku_summary = self._get_summary_by_sku(money_consumption.idSKUConsumido)
        sku_summary.add_money_consumption(money_consumption)

    def add_amount_consumption(self, amount_consumption):
        sku_summary = self._get_summary_by_sku(amount_consumption.idSKUConsumido)
        sku_summary.add_amount_consumption(amount_consumption)

    def _get_summary_by_sku(self, id_sku):
        if id_sku not in self.skus_summaries:
            self.skus_summaries[id_sku] = SkuConsumptionsSummary(id_sku)
        return self.skus_summaries[id_sku]

    def to_list(self):
        return self.skus_summaries.values()
