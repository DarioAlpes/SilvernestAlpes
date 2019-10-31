# -*- coding: utf-8 -*
import itertools
import json

from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.balance.BalanceConsumption import BalanceConsumption

from CJM.entidades.reservas.ReservaPersona import ReservaPersona


class BalancePersonReservation:
    BALANCE_NAME = "balance"
    PERSON_RESERVATION_ID_NAME = ReservaPersona.ID_PERSON_RESERVATION_NAME
    RESERVATION_ID_NAME = Reserva.ID_RESERVATION_NAME

    def __init__(self, person_reservation, is_balance):
        self.is_balance = is_balance
        self.person_reservation = person_reservation
        self.balance_skus = dict()
        self.balance_sku_categories = dict()
        self.balance_locations = dict()
        self.balance_money = dict()

    def add_sku_amount(self, id_sku, amount):
        balance = self.get_balance_for_sku(id_sku)
        balance.add_amount(amount)

    def add_sku_category_amount(self, id_sku_category, amount):
        balance = self.get_balance_for_sku_category(id_sku_category)
        balance.add_amount(amount)

    def add_location_amount(self, id_location, amount):
        balance = self.get_balance_for_location(id_location)
        balance.add_amount(amount)

    def mark_location_as_unlimited(self, id_location):
        balance = self.get_balance_for_location(id_location)
        balance.mark_as_unlimited()

    def add_money(self, currency_name, currency_id, money):
        balance = self.get_balance_for_currency(currency_name, currency_id)
        balance.add_money(money)

    def consume_sku(self, id_sku, amount, sku_category):
        amount = self.consume_included_sku_amount(id_sku, amount)
        if amount > 0:
            amount = self.consume_included_sku_categories_amount(sku_category, amount)
        if amount > 0:
            self.consume_all_sku_amount(id_sku, amount)

    def consume_location(self, location, amount):
        amount = self.consume_included_locations_amount(location, amount)
        if amount > 0:
            self.consume_all_location_amount(location.key.id(), amount)

    def consume_money(self, currency_name, currency_id, money):
        balance = self.get_balance_for_currency(currency_name, currency_id)
        balance.consume_all_money(money)

    def consume_included_locations_amount(self, location, amount):
        sorted_ids_valid_locations = reversed(location.get_hierarchy_ids())
        # Consume from more specific to least specific location
        for id_location in sorted_ids_valid_locations:
            balance = self.get_balance_for_location(id_location)
            amount = balance.consume_included_amount(amount)
            if amount == 0:
                break
        return amount

    def consume_all_location_amount(self, id_location, amount):
        balance = self.get_balance_for_location(id_location)
        balance.consume_all_amount(amount)

    def consume_included_sku_amount(self, id_sku, amount):
        balance = self.get_balance_for_sku(id_sku)
        return balance.consume_included_amount(amount)

    def consume_included_sku_categories_amount(self, sku_category, amount):
        sorted_ids_valid_categories = reversed(sku_category.get_hierarchy_ids())
        # Consume from more specific to least specific category
        for id_sku_category in sorted_ids_valid_categories:
            balance = self.get_balance_for_sku_category(id_sku_category)
            amount = balance.consume_included_amount(amount)
            if amount == 0:
                break
        return amount

    def consume_all_sku_amount(self, id_sku, amount):
        balance = self.get_balance_for_sku(id_sku)
        balance.consume_all_amount(amount)

    def get_balance_for_sku(self, id_sku):
        balance = self.balance_skus.get(id_sku)
        if balance is None:
            balance = BalanceConsumption(id_sku=id_sku, balance_amount=0, is_balance=self.is_balance)
            self.balance_skus[id_sku] = balance
        return balance

    def get_balance_for_sku_category(self, id_sku_category):
        balance = self.balance_sku_categories.get(id_sku_category)
        if balance is None:
            balance = BalanceConsumption(id_sku_category=id_sku_category, balance_amount=0, is_balance=self.is_balance)
            self.balance_sku_categories[id_sku_category] = balance
        return balance

    def get_balance_for_location(self, id_location):
        balance = self.balance_locations.get(id_location)
        if balance is None:
            balance = BalanceConsumption(id_location=id_location, balance_amount=0, is_balance=self.is_balance)
            self.balance_locations[id_location] = balance
        return balance

    def get_balance_for_currency(self, currency_name, currency_id):
        balance = self.balance_money.get(currency_name)
        if balance is None:
            balance = BalanceConsumption(currency=currency_name, id_currency=currency_id, balance_money=0,
                                         is_balance=self.is_balance)
            self.balance_money[currency_name] = balance
        return balance

    def to_dict(self):
        fields_dict = dict()
        balance_list = self.get_balance_details_as_list()
        fields_dict[self.BALANCE_NAME] = [detail.to_dict() for detail in balance_list]
        fields_dict[self.PERSON_RESERVATION_ID_NAME] = self.person_reservation.key.id()
        fields_dict[self.RESERVATION_ID_NAME] = self.person_reservation.idReserva
        return fields_dict

    def get_balance_details_as_list(self):
        return list(itertools.chain(self.balance_skus.values(), self.balance_sku_categories.values(),
                                    self.balance_locations.values(), self.balance_money.values()))

    def to_json(self):
        return json.dumps(self.to_dict())
