# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.paquetes.Paquete import Paquete
from CJM.services.validations import MONEY_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE
from commons.excepciones.apiexceptions import EntityDoesNotExists


class _ConsumoDineroCompartido(ndb.Model):
    @classmethod
    def create(cls, package):
        new_consumption = _ConsumoDineroCompartido(parent=package.get_shared_key())
        new_consumption.put()
        return new_consumption


class ConsumoDinero(ndb.Model):
    ID_NAME = "id"
    HISTORIC_ID_NAME = "historic-id"
    MONEY_INCLUDED_NAME = "money-included"
    ID_MONEY_CONSUMPTION_NAME = "id-money-consumption"
    ACTIVE_NAME = "active"
    CURRENCY_NAME = "currency"

    HISTORIC_PACKAGE_ID_NAME = Paquete.ID_PACKAGE_HISTORIC_NAME
    PACKAGE_ID_NAME = Paquete.ID_PACKAGE_NAME

    idCliente = ndb.IntegerProperty()
    idPaquete = ndb.IntegerProperty(indexed=True)
    idPaqueteCompartido = ndb.IntegerProperty(indexed=True)
    moneda = ndb.StringProperty(indexed=True)
    idCompartido = ndb.IntegerProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    dineroIncluido = ndb.FloatProperty()

    @classmethod
    def create(cls, id_client, package, money_included, currency):
        shared_money_consumption = _ConsumoDineroCompartido.create(package)
        id_money_consumption = shared_money_consumption.key.id()
        if money_included is None:
            money_included = 0

        new_money_consumption = ConsumoDinero(idCompartido=id_money_consumption,
                                              idCliente=id_client,
                                              idPaquete=package.key.id(),
                                              idPaqueteCompartido=package.idCompartido,
                                              dineroIncluido=money_included,
                                              moneda=currency,
                                              activo=True,
                                              parent=shared_money_consumption.key)

        new_money_consumption.put()
        return new_money_consumption

    @classmethod
    def update(cls, id_consumption, package, money_included, currency):
        if money_included is None:
            money_included = 0
        consumption_to_edit = ConsumoDinero.get_by_package(package, id_consumption)
        consumption_to_edit.moneda = currency
        consumption_to_edit.dineroIncluido = money_included
        consumption_to_edit.put()
        return consumption_to_edit

    def clone(self, new_package):
        new_consumption = ConsumoDinero(idCompartido=self.idCompartido,
                                        idCliente=self.idCliente,
                                        idPaquete=new_package.key.id(),
                                        idPaqueteCompartido=new_package.idCompartido,
                                        dineroIncluido=self.dineroIncluido,
                                        moneda=self.moneda,
                                        activo=True,
                                        parent=self.get_shared_key(new_package))
        return new_consumption

    @classmethod
    def list(cls):
        return ConsumoDinero.query().fetch()

    @classmethod
    def _list_by_package_without_fetch(cls, package):
        return ConsumoDinero.query(ancestor=package.get_shared_key())\
                        .filter(ConsumoDinero.idPaquete == package.key.id())

    @classmethod
    def list_by_package(cls, package):
        return cls._list_by_package_without_fetch(package).fetch()

    @classmethod
    def list_money_consumptions_by_package(cls, package):
        return ConsumoDinero.query(ancestor=package.get_shared_key())\
                    .filter(ConsumoDinero.idPaquete == package.key.id()).fetch()

    @classmethod
    def get_by_package(cls, package, id_money_consumption):
        try:
            consumption = ConsumoDinero.query(ancestor=ConsumoDinero._get_shared_key(package, id_money_consumption))\
                .filter(ConsumoDinero.idPaquete == package.key.id()).get()
        except ValueError:
            consumption = None
        if consumption is None:
            raise EntityDoesNotExists(u"Money Consumption[{0}]".format(id_money_consumption),
                                      internal_code=MONEY_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return consumption

    @classmethod
    def clone_package_money_consumptions(cls, old_package, new_package):
        consumptions = ConsumoDinero.list_money_consumptions_by_package(old_package)
        for consumption in consumptions:
            consumption.activo = False
        new_consumptions = [consumption.clone(new_package) for consumption in consumptions]
        ndb.put_multi(consumptions + new_consumptions)

    @classmethod
    def _get_shared_key(cls, package, shared_id):
        return ndb.Key(_ConsumoDineroCompartido, shared_id, parent=package.get_shared_key())

    def get_shared_key(self, package):
        return ConsumoDinero._get_shared_key(package, self.idCompartido)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[ConsumoDinero.HISTORIC_ID_NAME] = self.key.id()
        fields_dict[ConsumoDinero.ID_NAME] = self.idCompartido
        fields_dict[ConsumoDinero.PACKAGE_ID_NAME] = self.idPaqueteCompartido
        fields_dict[ConsumoDinero.HISTORIC_PACKAGE_ID_NAME] = self.idPaquete
        fields_dict[ConsumoDinero.ACTIVE_NAME] = self.activo
        fields_dict[ConsumoDinero.CURRENCY_NAME] = self.moneda
        fields_dict[ConsumoDinero.MONEY_INCLUDED_NAME] = self.dineroIncluido
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def get_available_money_for_package_by_currency(cls, package):
        money_available = dict()
        consumptions = cls.list_by_package(package)

        for consumption in consumptions:
            money_available[consumption.moneda] = \
                money_available.get(consumption.moneda, 0) + consumption.dineroIncluido

        return money_available

    @classmethod
    def get_available_money_by_currency(cls, package, reservation):
        money_available = dict()
        consumptions = cls.list_by_package(package)
        number_of_days = reservation.get_number_of_days()

        for consumption in consumptions:
            money_available[consumption.moneda] = \
                money_available.get(consumption.moneda, 0) + number_of_days * consumption.dineroIncluido

        return money_available
