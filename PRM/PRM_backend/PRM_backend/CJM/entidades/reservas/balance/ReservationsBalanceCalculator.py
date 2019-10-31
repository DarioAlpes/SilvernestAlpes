# -*- coding: utf-8 -*
import json

from CJM.entidades.reservas.AccessTopoff import AccessTopoff
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.entidades.reservas.orders.PersonAccess import PersonAccess
from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.entidades.reservas.orders.PersonConsumptionByMoney import PersonConsumptionByMoney
from google.appengine.ext import ndb

from CJM.entidades.Moneda import Moneda
from CJM.entidades.paquetes.Acceso import Acceso
from CJM.entidades.paquetes.ConsumoCantidad import ConsumoCantidad
from CJM.entidades.paquetes.ConsumoDinero import ConsumoDinero
from CJM.entidades.reservas.balance.BalancePersonReservation import BalancePersonReservation
from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from CJM.entidades.skus.SKU import SKU
from commons.entidades.locations.Ubicacion import Ubicacion


class ReservationsBalanceCalculator:
    BALANCES_NAME = "balances"

    def __init__(self, packages, reservations, person_reservations, sorted_access_topoffs, sorted_amount_topoffs,
                 sorted_money_topoffs, sorted_access_consumptions, sorted_amount_consumptions,
                 sorted_money_consumptions, sku_categories_by_consumptions_skus, currencies_dict, locations_by_id,
                 is_balance):
        self.is_balance = is_balance
        self.reservation_balances = dict()
        self._available_sku_amounts_per_package = dict()
        self._available_sku_categories_amounts_per_package = dict()
        self._available_location_amounts_per_package = dict()
        self._available_money_per_package = dict()
        self.person_reservations_dict = {person_reservation.key.id(): person_reservation
                                         for person_reservation in person_reservations}
        self.currencies_dict = currencies_dict
        self.locations_by_id = locations_by_id
        self.sku_categories_by_consumptions_skus = sku_categories_by_consumptions_skus
        self._calculate_initial_funds(packages, reservations, person_reservations)
        self._process_transactions(sorted_access_topoffs, sorted_amount_topoffs, sorted_money_topoffs,
                                   sorted_access_consumptions, sorted_amount_consumptions, sorted_money_consumptions)

    def add_sku_amount(self, person_reservation, id_sku, amount):
        balance = self.get_balance_for_person_reservation(person_reservation)
        balance.add_sku_amount(id_sku, amount)

    def add_sku_category_amount(self, person_reservation, id_sku_category, amount):
        balance = self.get_balance_for_person_reservation(person_reservation)
        balance.add_sku_category_amount(id_sku_category, amount)

    def add_location_amount(self, person_reservation, id_location, amount):
        balance = self.get_balance_for_person_reservation(person_reservation)
        balance.add_location_amount(id_location, amount)

    def mark_location_as_unlimited(self, person_reservation, id_location):
        balance = self.get_balance_for_person_reservation(person_reservation)
        balance.mark_location_as_unlimited(id_location)

    def add_money(self, person_reservation, currency_name, money):
        balance = self.get_balance_for_person_reservation(person_reservation)
        currency_id = self.currencies_dict[currency_name].idInterno
        balance.add_money(currency_name, currency_id, money)

    def consume_sku(self, person_reservation, id_sku, amount, sku_category):
        balance = self.get_balance_for_person_reservation(person_reservation)
        balance.consume_sku(id_sku, amount, sku_category)

    def consume_location(self, person_reservation, location, amount):
        balance = self.get_balance_for_person_reservation(person_reservation)
        balance.consume_location(location, amount)

    def consume_money(self, person_reservation, currency_name, money):
        balance = self.get_balance_for_person_reservation(person_reservation)
        currency_id = self.currencies_dict[currency_name].idInterno
        balance.consume_money(currency_name, currency_id, money)

    def get_balance_for_person_reservation(self, person_reservation):
        balance = self.reservation_balances.get(person_reservation.key)
        if balance is None:
            balance = BalancePersonReservation(person_reservation, self.is_balance)
            self.reservation_balances[person_reservation.key] = balance
        return balance

    def to_dict(self):
        fields_dict = dict()
        balances = [balance.to_dict() for balance in self.reservation_balances.values()]
        fields_dict[self.BALANCES_NAME] = balances
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    def get_balances_as_list(self):
        return [balance for balance in self.reservation_balances.values()]

    def _calculate_initial_funds(self, packages, reservations, person_reservations):
        for index, package in enumerate(packages):
            self._calculate_package_funds(package)
            reservation = reservations[index]
            person_reservation = person_reservations[index]
            self._calculate_initial_person_reservation_skus_funds(package, reservation, person_reservation)
            self._calculate_initial_person_reservation_sku_categories_funds(package, reservation, person_reservation)
            self._calculate_initial_person_reservation_locations_funds(package, reservation, person_reservation)
            self._calculate_initial_person_reservation_money_funds(package, reservation, person_reservation)

    def _calculate_package_funds(self, package):
        if self._available_sku_amounts_per_package.get(package.key) is None:
            skus_funds = ConsumoCantidad.get_available_amounts_for_package_by_ids_skus(package)
            self._available_sku_amounts_per_package[package.key] = skus_funds
            sku_categories_funds = ConsumoCantidad.get_available_amounts_for_package_by_ids_categories(package)
            self._available_sku_categories_amounts_per_package[package.key] = sku_categories_funds
            locations_funds = Acceso.get_available_amounts_for_package_by_ids_locations(package)
            self._available_location_amounts_per_package[package.key] = locations_funds
            currencies_funds = ConsumoDinero.get_available_money_for_package_by_currency(package)
            currencies_keys = {ndb.Key(Moneda, currency_name) for currency_name in currencies_funds.keys()}
            currencies = ndb.get_multi(currencies_keys)
            for currency in currencies:
                self.currencies_dict[currency.key.id()] = currency
            self._available_money_per_package[package.key] = currencies_funds

    # noinspection PyUnusedLocal
    def _calculate_initial_person_reservation_skus_funds(self, package, reservation, person_reservation):
        number_of_days = person_reservation.get_number_of_days()
        available_funds_skus = self._available_sku_amounts_per_package[package.key]
        for id_sku, amount in available_funds_skus.iteritems():
            self.add_sku_amount(person_reservation, id_sku, amount * number_of_days)

    # noinspection PyUnusedLocal
    def _calculate_initial_person_reservation_sku_categories_funds(self, package, reservation, person_reservation):
        number_of_days = person_reservation.get_number_of_days()
        available_funds_sku_categories = self._available_sku_categories_amounts_per_package[package.key]
        for id_sku_category, amount in available_funds_sku_categories.iteritems():
            self.add_sku_category_amount(person_reservation, id_sku_category, amount * number_of_days)

    # noinspection PyUnusedLocal
    def _calculate_initial_person_reservation_locations_funds(self, package, reservation, person_reservation):
        number_of_days = person_reservation.get_number_of_days()
        available_funds_locations = self._available_location_amounts_per_package[package.key]
        for id_location, amount in available_funds_locations.iteritems():
            if amount == 0:
                self.mark_location_as_unlimited(person_reservation, id_location)
            else:
                self.add_location_amount(person_reservation, id_location, amount * number_of_days)

    # noinspection PyUnusedLocal
    def _calculate_initial_person_reservation_money_funds(self, package, reservation, person_reservation):
        number_of_days = person_reservation.get_number_of_days()
        available_funds_currencies = self._available_money_per_package[package.key]
        for id_currency, money in available_funds_currencies.iteritems():
            self.add_money(person_reservation, id_currency, money * number_of_days)

    def _process_transactions(self, sorted_access_topoffs, sorted_amount_topoffs, sorted_money_topoffs,
                              sorted_access_consumptions, sorted_amount_consumptions, sorted_money_consumptions):
        self._process_access_transactions(sorted_access_topoffs, sorted_access_consumptions)
        self._process_amount_transactions(sorted_amount_topoffs, sorted_amount_consumptions)
        self._process_money_transactions(sorted_money_topoffs, sorted_money_consumptions)

    def _process_access_transactions(self, sorted_topoffs, sorted_consumptions):
        index_topoffs = 0
        index_consumptions = 0
        while len(sorted_topoffs) > index_topoffs or len(sorted_consumptions) > index_consumptions:
            next_element = None
            is_topoff = None
            if len(sorted_topoffs) > index_topoffs:
                next_element = sorted_topoffs[index_topoffs]
                is_topoff = True
            if len(sorted_consumptions) > index_consumptions:
                consumption = sorted_consumptions[index_consumptions]
                if next_element is None or next_element.tiempoTopoff > consumption.tiempoAcceso:
                    next_element = consumption
                    is_topoff = False

            if is_topoff:
                person_reservation_key = next_element.idReservacionPersona
                person_reservation = self.person_reservations_dict[person_reservation_key]
                if next_element.cantidadIncluida == 0:
                    self.mark_location_as_unlimited(person_reservation, next_element.idUbicacion)
                else:
                    self.add_location_amount(person_reservation, next_element.idUbicacion,
                                             next_element.cantidadIncluida)
                index_topoffs += 1
            else:
                person_reservation_key = next_element.idReservaPersona
                person_reservation = self.person_reservations_dict[person_reservation_key]
                self.consume_location(person_reservation, self.locations_by_id[next_element.idUbicacionAcceso], 1)
                index_consumptions += 1

    def _process_amount_transactions(self, sorted_topoffs, sorted_consumptions):
        index_topoffs = 0
        index_consumptions = 0
        while len(sorted_topoffs) > index_topoffs or len(sorted_consumptions) > index_consumptions:
            next_element = None
            is_topoff = None
            if len(sorted_topoffs) > index_topoffs:
                next_element = sorted_topoffs[index_topoffs]
                is_topoff = True
            if len(sorted_consumptions) > index_consumptions:
                consumption = sorted_consumptions[index_consumptions]
                if next_element is None or next_element.tiempoTopoff > consumption.tiempoConsumo:
                    next_element = consumption
                    is_topoff = False

            if is_topoff:
                person_reservation_key = next_element.idReservacionPersona
                person_reservation = self.person_reservations_dict[person_reservation_key]
                if next_element.idSku is not None:
                    self.add_sku_amount(person_reservation, next_element.idSku, next_element.cantidadIncluida)
                elif next_element.idCategoriaSku is not None:
                    self.add_sku_category_amount(person_reservation, next_element.idCategoriaSku,
                                                 next_element.cantidadIncluida)
                index_topoffs += 1
            else:
                person_reservation_key = next_element.idReservaPersona
                person_reservation = self.person_reservations_dict[person_reservation_key]
                sku_category = self.sku_categories_by_consumptions_skus[next_element.idSKUConsumido]
                self.consume_sku(person_reservation, next_element.idSKUConsumido, next_element.cantidadConsumida,
                                 sku_category)
                index_consumptions += 1

    def _process_money_transactions(self, sorted_topoffs, sorted_consumptions):
        index_topoffs = 0
        index_consumptions = 0
        while len(sorted_topoffs) > index_topoffs or len(sorted_consumptions) > index_consumptions:
            next_element = None
            is_topoff = None
            if len(sorted_topoffs) > index_topoffs:
                next_element = sorted_topoffs[index_topoffs]
                is_topoff = True
            if len(sorted_consumptions) > index_consumptions:
                consumption = sorted_consumptions[index_consumptions]
                if next_element is None or next_element.tiempoTopoff > consumption.tiempoConsumo:
                    next_element = consumption
                    is_topoff = False

            if is_topoff:
                person_reservation_key = next_element.idReservacionPersona
                person_reservation = self.person_reservations_dict[person_reservation_key]
                self.add_money(person_reservation, next_element.moneda, next_element.dineroIncluido)
                index_topoffs += 1
            else:
                person_reservation_key = next_element.idReservaPersona
                person_reservation = self.person_reservations_dict[person_reservation_key]
                self.consume_money(person_reservation, next_element.moneda, next_element.dineroConsumido)
                index_consumptions += 1

    @classmethod
    def calculate_funds_for_person_reservation(cls, package, reservation, person_reservation, is_balance):
        access_topoffs_async = AccessTopoff.list_by_ids_person_reservation_sorted_without_fetch(reservation.key.id(),
                                                                                                person_reservation.key.id()).fetch_async()
        amount_topoffs_async = AmountTopoff.list_by_ids_person_reservation_sorted_without_fetch(reservation.key.id(),
                                                                                                person_reservation.key.id()).fetch_async()
        money_topoffs_async = MoneyTopoff.list_by_ids_person_reservation_sorted_without_fetch(reservation.key.id(),
                                                                                              person_reservation.key.id()).fetch_async()
        access_consumptions_async = PersonAccess.list_by_ids_person_reservation_sorted_without_fetch(reservation.key.id(),
                                                                                                     person_reservation.key.id()).fetch_async()
        amount_consumptions_async = PersonConsumptionByAmount.list_by_ids_person_reservation_sorted_without_fetch(reservation.key.id(),
                                                                                                                  person_reservation.key.id()).fetch_async()
        money_consumptions_async = PersonConsumptionByMoney.list_by_ids_person_reservation_sorted_without_fetch(reservation.key.id(),
                                                                                                                person_reservation.key.id()).fetch_async()

        access_topoffs = access_topoffs_async.get_result()
        amount_topoffs = amount_topoffs_async.get_result()
        money_topoffs = money_topoffs_async.get_result()
        access_consumptions = access_consumptions_async.get_result()
        amount_consumptions = amount_consumptions_async.get_result()
        money_consumptions = money_consumptions_async.get_result()

        sku_categories_by_consumptions_skus = cls.get_sku_categories_by_consumptions_skus(amount_consumptions)
        currencies = cls.get_topoffs_and_consumptions_currencies_dict(money_topoffs, money_consumptions)
        locations_by_id = cls.get_locations_by_accesses_dict(access_consumptions)

        return ReservationsBalanceCalculator([package], [reservation], [person_reservation], access_topoffs,
                                             amount_topoffs, money_topoffs, access_consumptions, amount_consumptions,
                                             money_consumptions, sku_categories_by_consumptions_skus, currencies,
                                             locations_by_id, is_balance)

    @classmethod
    def calculate_funds_for_reservation(cls, reservation, is_balance):
        person_reservations = ReservaPersona.list_by_id_reservation(reservation.key.id())
        packages = ndb.get_multi([person_reservation.get_package_key() for person_reservation in person_reservations])
        reservations = [reservation] * len(person_reservations)
        access_topoffs_async = AccessTopoff.list_by_id_reservation_sorted_without_fetch(reservation.key.id()).fetch_async()
        amount_topoffs_async = AmountTopoff.list_by_id_reservation_sorted_without_fetch(reservation.key.id()).fetch_async()
        money_topoffs_async = MoneyTopoff.list_by_id_reservation_sorted_without_fetch(reservation.key.id()).fetch_async()
        access_consumptions_async = PersonAccess.list_by_id_reservation_sorted_without_fetch(reservation.key.id()).fetch_async()
        amount_consumptions_async = PersonConsumptionByAmount.list_by_id_reservation_sorted_without_fetch(reservation.key.id()).fetch_async()
        money_consumptions_async = PersonConsumptionByMoney.list_by_id_reservation_sorted_without_fetch(reservation.key.id()).fetch_async()

        access_topoffs = access_topoffs_async.get_result()
        amount_topoffs = amount_topoffs_async.get_result()
        money_topoffs = money_topoffs_async.get_result()
        access_consumptions = access_consumptions_async.get_result()
        amount_consumptions = amount_consumptions_async.get_result()
        money_consumptions = money_consumptions_async.get_result()

        sku_categories_by_consumptions_skus = cls.get_sku_categories_by_consumptions_skus(amount_consumptions)
        locations_by_id = cls.get_locations_by_accesses_dict(access_consumptions)
        currencies = cls.get_topoffs_and_consumptions_currencies_dict(money_topoffs, money_consumptions)

        return ReservationsBalanceCalculator(packages, reservations, person_reservations, access_topoffs,
                                             amount_topoffs, money_topoffs, access_consumptions, amount_consumptions,
                                             money_consumptions, sku_categories_by_consumptions_skus, currencies,
                                             locations_by_id, is_balance)

    @classmethod
    def get_topoffs_and_consumptions_currencies_dict(cls, money_topoffs, money_consumptions):
        topoff_keys = {ndb.Key(Moneda, topoff.moneda) for topoff in money_topoffs}
        consumption_keys = {ndb.Key(Moneda, consumption.moneda) for consumption in money_consumptions}
        currencies = ndb.get_multi(topoff_keys.union(consumption_keys))
        return {currency.key.id(): currency for currency in currencies}

    @classmethod
    def get_sku_categories_by_consumptions_skus(cls, amount_consumptions):
        keys_skus_consumptions = [ndb.Key(SKU, consumption.idSKUConsumido) for consumption in amount_consumptions]
        skus = ndb.get_multi(keys_skus_consumptions)
        sku_categories_keys = [ndb.Key(CategoriaSKU, sku.idCategoriaSku) for sku in skus]
        sku_categories = ndb.get_multi(sku_categories_keys)
        sku_categories_by_consumptions_skus = dict()
        for index, sku in enumerate(skus):
            sku_categories_by_consumptions_skus[sku.key.id()] = sku_categories[index]

        return sku_categories_by_consumptions_skus

    @classmethod
    def get_locations_by_accesses_dict(cls, accesses):
        keys_locations = [ndb.Key(Ubicacion, access.idUbicacionAcceso) for access in accesses]
        locations = ndb.get_multi(keys_locations)
        return {location.key.id(): location for location in locations}
