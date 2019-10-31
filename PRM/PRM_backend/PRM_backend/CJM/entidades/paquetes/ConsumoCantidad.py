# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.skus.CategoriaSKU import CategoriaSKU
from CJM.entidades.skus.SKU import SKU
from commons.excepciones.apiexceptions import EntityDoesNotExists


class _ConsumoCantidadCompartido(ndb.Model):
    @classmethod
    def create(cls, package):
        new_consumption = _ConsumoCantidadCompartido(parent=package.get_shared_key())
        new_consumption.put()
        return new_consumption


class ConsumoCantidad(ndb.Model):
    ID_NAME = "id"
    HISTORIC_ID_NAME = "historic-id"

    AMOUNT_INCLUDED_NAME = "amount-included"
    ACTIVE_NAME = "active"

    ID_CONSUMPTION_NAME = "id-amount-consumption"

    HISTORIC_PACKAGE_ID_NAME = Paquete.ID_PACKAGE_HISTORIC_NAME
    PACKAGE_ID_NAME = Paquete.ID_PACKAGE_NAME
    SKU_ID_NAME = SKU.ID_SKU_NAME
    SKU_CATEGORY_ID_NAME = CategoriaSKU.ID_SKU_CATEGORY_NAME

    idCliente = ndb.IntegerProperty()
    idPaquete = ndb.IntegerProperty(indexed=True)
    idPaqueteCompartido = ndb.IntegerProperty(indexed=True)
    idSku = ndb.IntegerProperty(indexed=True)
    idCategoriaSku = ndb.IntegerProperty(indexed=True)
    idCompartido = ndb.IntegerProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    cantidadIncluida = ndb.IntegerProperty()

    @classmethod
    def create(cls, id_client, package, id_sku, id_sku_category, amount_included):
        shared_consumption = _ConsumoCantidadCompartido.create(package)
        id_consumption = shared_consumption.key.id()
        if amount_included is None:
            amount_included = 0

        new_consumption = ConsumoCantidad(idCompartido=id_consumption,
                                          idCliente=id_client,
                                          idPaquete=package.key.id(),
                                          idPaqueteCompartido=package.idCompartido,
                                          idSku=id_sku,
                                          idCategoriaSku=id_sku_category,
                                          cantidadIncluida=amount_included,
                                          activo=True,
                                          parent=shared_consumption.key)

        new_consumption.put()
        return new_consumption

    @classmethod
    def update(cls, id_consumption, package, id_sku, id_sku_category, amount_included):
        if amount_included is None:
            amount_included = 0
        consumption_to_edit = ConsumoCantidad.get_by_package(package, id_consumption)
        consumption_to_edit.idSku = id_sku
        consumption_to_edit.idCategoriaSku = id_sku_category
        consumption_to_edit.cantidadIncluida = amount_included
        consumption_to_edit.put()
        return consumption_to_edit

    def clone(self, new_package):
        new_consumption = ConsumoCantidad(idCompartido=self.idCompartido,
                                          idCliente=self.idCliente,
                                          idPaquete=new_package.key.id(),
                                          idPaqueteCompartido=new_package.idCompartido,
                                          idSku=self.idSku,
                                          idCategoriaSku=self.idCategoriaSku,
                                          cantidadIncluida=self.cantidadIncluida,
                                          activo=True,
                                          parent=self.get_shared_key(new_package))
        return new_consumption

    @classmethod
    def _list_by_package_without_fetch(cls, package):
        return ConsumoCantidad.query(ancestor=package.get_shared_key()) \
            .filter(ConsumoCantidad.idPaquete == package.key.id())

    @classmethod
    def list(cls):
        return ConsumoCantidad.query().fetch()

    @classmethod
    def list_by_package(cls, package):
        return cls._list_by_package_without_fetch(package).fetch()

    @classmethod
    def list_amount_consumptions_by_package(cls, package):
        return ConsumoCantidad.query(ancestor=package.get_shared_key()).filter(ConsumoCantidad.idPaquete == package.key.id()).fetch()

    @classmethod
    def get_by_package(cls, package, id_consumption):
        try:
            consumption = ConsumoCantidad.query(ancestor=ConsumoCantidad._get_shared_key(package, id_consumption))\
                .filter(ConsumoCantidad.idPaquete == package.key.id()).get()
        except ValueError:
            consumption = None
        if consumption is None:
            from CJM.services.validations import AMOUNT_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE
            raise EntityDoesNotExists(u"Amount Consumption[{0}]".format(id_consumption),
                                      internal_code=AMOUNT_CONSUMPTION_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return consumption

    @classmethod
    def clone_package_amount_consumptions(cls, old_package, new_package):
        consumptions = ConsumoCantidad.list_amount_consumptions_by_package(old_package)
        for consumption in consumptions:
            consumption.activo = False
        new_consumptions = [consumption.clone(new_package) for consumption in consumptions]
        ndb.put_multi(consumptions + new_consumptions)

    @classmethod
    def _get_shared_key(cls, package, shared_id):
        return ndb.Key(_ConsumoCantidadCompartido, shared_id, parent=package.get_shared_key())

    def get_shared_key(self, package):
        return ConsumoCantidad._get_shared_key(package, self.idCompartido)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[ConsumoCantidad.HISTORIC_ID_NAME] = self.key.id()
        fields_dict[ConsumoCantidad.ID_NAME] = self.idCompartido
        fields_dict[ConsumoCantidad.PACKAGE_ID_NAME] = self.idPaqueteCompartido
        fields_dict[ConsumoCantidad.HISTORIC_PACKAGE_ID_NAME] = self.idPaquete
        fields_dict[ConsumoCantidad.ACTIVE_NAME] = self.activo
        fields_dict[ConsumoCantidad.AMOUNT_INCLUDED_NAME] = self.cantidadIncluida
        if self.idSku is not None:
            fields_dict[ConsumoCantidad.SKU_ID_NAME] = self.idSku
        if self.idCategoriaSku is not None:
            fields_dict[ConsumoCantidad.SKU_CATEGORY_ID_NAME] = self.idCategoriaSku
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def get_available_amounts_for_package_by_ids_skus(cls, package):
        amounts_available = dict()
        consumptions = cls.list_by_package(package)

        for consumption in consumptions:
            if consumption.idSku is not None:
                amounts_available[consumption.idSku] = \
                    amounts_available.get(consumption.idSku, 0) + consumption.cantidadIncluida

        return amounts_available

    @classmethod
    def get_available_amounts_for_package_by_ids_categories(cls, package):
        amounts_available = dict()
        consumptions = cls.list_by_package(package)

        for consumption in consumptions:
            if consumption.idCategoriaSku is not None:
                amounts_available[consumption.idCategoriaSku] = \
                    amounts_available.get(consumption.idCategoriaSku, 0) + consumption.cantidadIncluida

        return amounts_available

    @classmethod
    def get_available_amounts_by_ids_categories(cls, package, reservation):
        amounts_available = dict()
        consumptions = cls.list_by_package(package)
        number_of_days = reservation.get_number_of_days()

        for consumption in consumptions:
            if consumption.idCategoriaSku is not None:
                amounts_available[consumption.idCategoriaSku] = \
                    amounts_available.get(consumption.idCategoriaSku, 0) + number_of_days * consumption.cantidadIncluida

        return amounts_available

    @classmethod
    def get_available_amounts_by_ids_skus(cls, package, reservation):
        amounts_available = dict()
        consumptions = cls.list_by_package(package)
        number_of_days = reservation.get_number_of_days()

        for consumption in consumptions:
            if consumption.idSku is not None:
                amounts_available[consumption.idSku] = \
                    amounts_available.get(consumption.idSku, 0) + number_of_days * consumption.cantidadIncluida

        return amounts_available
