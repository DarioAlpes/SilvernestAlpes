# -*- coding: utf-8 -*
from google.appengine.ext import ndb
from commons.excepciones.apiexceptions import EntityAlreadyExists, EntityDoesNotExists


class Generador(ndb.Model):
    ID_PRODUCTS = 'idProducto'
    ID_LOCATIONS = 'idUbicacion'
    ID_TRANSACTIONS = 'idTransaccion'
    ID_PERSONS = 'idPersona'
    ID_CLIENTS = 'idCliente'
    ID_SKUS = 'idSku'
    ID_SKU_CATEGORIES = 'idSkuCategory'
    ID_ALLOCATIONS = 'idAsignacion'
    ID_TRANSFERS = 'idTraslado'
    ID_EVENTS = 'idEvento'
    ID_SENSORS = 'idSensor'
    ID_BEACONS = 'idBeacon'
    ID_READING_LOG = 'idLogLectura'
    ID_PACKAGES = 'idPackage'
    ID_PACKAGE_ITEM = "idPackageItem"
    ID_CURRENCIES = "idCurrency"
    ID_SUPPORTED_TAGS = "idSupportedTag"
    RESERVATION_NUMBER = "reservationNumber"
    cuenta = ndb.IntegerProperty()

    @classmethod
    @ndb.transactional(xg=True)
    def create(cls, name):
        key = ndb.Key(Generador, name)
        if key.get() is None:
            generador = Generador(key=key, cuenta=1)
            generador.put()
        else:
            raise EntityAlreadyExists(u"Generador[{0}]".format(name))

    @classmethod
    def create_multi(cls, names):
        generators = [Generador(key=ndb.Key(Generador, name), cuenta=1) for name in names]
        ndb.put_multi(generators)
        return generators

    @classmethod
    @ndb.transactional(xg=True)
    def next(cls, name, cuenta=1):
        key = ndb.Key(Generador, name)
        generador = key.get()
        if generador is None:
            cls.create(name)
            key = ndb.Key(Generador, name)
            generador = key.get()
            if generador is None:
                raise EntityDoesNotExists(u"Generador[{0}]".format(name))
        ret = generador.cuenta
        generador.cuenta += cuenta
        generador.put()
        return ret

    @classmethod
    def get_next_sku_id(cls, cuenta=1):
        return Generador.next(Generador.ID_SKUS, cuenta)

    @classmethod
    def get_next_person_id(cls, cuenta=1):
        return Generador.next(Generador.ID_PERSONS, cuenta)

    @classmethod
    def get_next_client_id(cls, cuenta=1):
        return Generador.next(Generador.ID_CLIENTS, cuenta)

    @classmethod
    def get_next_product_id(cls, cuenta=1):
        return Generador.next(Generador.ID_PRODUCTS, cuenta)

    @classmethod
    def get_next_location_id(cls, cuenta=1):
        return Generador.next(Generador.ID_LOCATIONS, cuenta)

    @classmethod
    def get_next_transaction_id(cls, cuenta=1):
        return Generador.next(Generador.ID_TRANSACTIONS, cuenta)

    @classmethod
    def get_next_allocation_id(cls, cuenta=1):
        return Generador.next(Generador.ID_ALLOCATIONS, cuenta)

    @classmethod
    def get_next_transfer_id(cls, cuenta=1):
        return Generador.next(Generador.ID_TRANSFERS, cuenta)

    @classmethod
    def get_next_event_id(cls, cuenta=1):
        return Generador.next(Generador.ID_EVENTS, cuenta)

    @classmethod
    def get_next_sensor_id(cls, cuenta=1):
        return Generador.next(Generador.ID_SENSORS, cuenta)

    @classmethod
    def get_next_beacon_id(cls, cuenta=1):
        return Generador.next(Generador.ID_BEACONS, cuenta)

    @classmethod
    def get_next_reading_log_id(cls, cuenta=1):
        return Generador.next(Generador.ID_READING_LOG, cuenta)

    @classmethod
    def get_next_sku_category_id(cls, cuenta=1):
        return Generador.next(Generador.ID_SKU_CATEGORIES, cuenta)

    @classmethod
    def get_next_package_id(cls, cuenta=1):
        return Generador.next(Generador.ID_PACKAGES, cuenta)

    @classmethod
    def get_next_package_id(cls, cuenta=1):
        return Generador.next(Generador.ID_PACKAGES, cuenta)

    @classmethod
    def get_next_package_item_id(cls, cuenta=1):
        return Generador.next(Generador.ID_PACKAGE_ITEM, cuenta)

    @classmethod
    def get_next_currency_id(cls, cuenta=1):
        return Generador.next(Generador.ID_CURRENCIES, cuenta)

    @classmethod
    def get_next_reservation_number(cls, cuenta=1):
        return Generador.next(Generador.RESERVATION_NUMBER, cuenta)

    @classmethod
    def get_next_supported_tag_id(cls, cuenta=1):
        return Generador.next(Generador.ID_SUPPORTED_TAGS, cuenta)

    @classmethod
    def get_next_client_tag_id(cls, cuenta=1):
        return Generador.next(Generador.ID_SUPPORTED_TAGS, cuenta)

    @staticmethod
    def initialize_global_generators():
        if Generador.get_by_id(Generador.ID_CLIENTS) is None:
            Generador.create(Generador.ID_CLIENTS)
