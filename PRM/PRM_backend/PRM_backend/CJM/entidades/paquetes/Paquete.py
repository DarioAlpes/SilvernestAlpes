# -*- coding: utf-8 -*
import json

from google.appengine.ext import ndb

from commons.entidades.locations.Ubicacion import Ubicacion
from commons.excepciones.apiexceptions import EntityDoesNotExists


class _PaqueteCompartido(ndb.Model):
    @classmethod
    def create(cls):
        new_package = _PaqueteCompartido()
        new_package.put()
        return new_package


class Paquete(ndb.Model):
    ID_NAME = "id"
    HISTORIC_ID_NAME = "historic-id"
    PACKAGE_NAME_NAME = "name"
    BASE_PRICE_NAME = "base-price"
    TAX_RATE_NAME = "tax-rate"
    DESCRIPTION_NAME = "description"
    RESTRICTED_CONSUMPTION_NAME = "restricted-consumption"
    VALID_FROM_NAME = "valid-from"
    VALID_THROUGH_NAME = "valid-through"
    ACTIVE_NAME = "active"
    BASE_TIME_NAME = "base-time"
    EXTERNAL_CODE_NAME = "external-code"
    AVAILABLE_FOR_SALE = "available-for-sale"

    ID_PACKAGE_HISTORIC_NAME = "historic-id-package"
    ID_PACKAGE_NAME = "id-package"
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idCliente = ndb.IntegerProperty()
    nombre = ndb.StringProperty()
    codigoExterno = ndb.StringProperty(indexed=True)
    precio = ndb.FloatProperty(indexed=True)
    impuesto = ndb.FloatProperty(indexed=True)
    descripcion = ndb.TextProperty()
    consumoRestringido = ndb.BooleanProperty(indexed=True)
    validoDesde = ndb.DateTimeProperty(indexed=True)
    validoHasta = ndb.DateTimeProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)
    idCompartido = ndb.IntegerProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    disponibleParaLaVenta = ndb.BooleanProperty(indexed=True)

    @classmethod
    def create(cls, id_client, name, price, tax_rate, description, restricted_consumption,
               valid_from, valid_through, id_location, external_code, available_for_sale, shared_key=None):
        if shared_key is None:
            shared_package = _PaqueteCompartido.create()
            shared_key = shared_package.key

        new_package = Paquete(idCompartido=shared_key.id(),
                              idCliente=id_client,
                              nombre=name,
                              precio=price,
                              impuesto=tax_rate,
                              descripcion=description,
                              consumoRestringido=restricted_consumption,
                              validoDesde=valid_from,
                              validoHasta=valid_through,
                              idUbicacion=id_location,
                              codigoExterno=external_code,
                              activo=True,
                              disponibleParaLaVenta=available_for_sale,
                              parent=shared_key)
        new_package.put()
        return new_package

    def update(self, id_client, name, price, tax_rate, description, restricted_consumption,
               valid_from, valid_through, id_location, external_code, available_for_sale):
        self.idCliente = id_client
        self.nombre = name
        self.precio = price
        self.impuesto = tax_rate
        self.descripcion = description
        self.consumoRestringido = restricted_consumption
        self.validoDesde = valid_from
        self.validoHasta = valid_through
        self.idUbicacion = id_location
        self.codigoExterno = external_code
        self.disponibleParaLaVenta = available_for_sale
        self.put()
        return self

    @classmethod
    def clone_package_for_edit(cls, original_package):
        """
        Crea un nuevo paquete como clon del paquete dado por parámetro.
        :param original_package: paquete a clonar
        :return: Paquete clonado
        """
        new_package = Paquete(idCompartido=original_package.idCompartido,
                              idCliente=original_package.idCliente,
                              nombre=original_package.nombre,
                              precio=original_package.precio,
                              impuesto=original_package.impuesto,
                              descripcion=original_package.descripcion,
                              consumoRestringido=original_package.consumoRestringido,
                              validoDesde=original_package.validoDesde,
                              validoHasta=original_package.validoHasta,
                              idUbicacion=original_package.idUbicacion,
                              codigoExterno=original_package.codigoExterno,
                              disponibleParaLaVenta=original_package.disponibleParaLaVenta,
                              activo=True,
                              parent=original_package.get_shared_key())
        original_package.activo = False
        ndb.put_multi([original_package, new_package])
        return new_package

    @classmethod
    def list(cls):
        return Paquete.query().fetch()

    @classmethod
    def list_active_packages(cls):
        return cls.list_active_packages_without_fetch().fetch()

    @classmethod
    def list_active_packages_without_fetch(cls):
        return Paquete.query(Paquete.activo == True)

    @classmethod
    def list_active_packages_with_base_time(cls, base_time):
        packages = cls.list_active_packages_without_fetch()
        if base_time is not None:
            packages = packages.filter(Paquete.validoHasta >= base_time)
        packages = packages.fetch()
        if base_time is not None:
            packages = [package for package in packages if package.validoDesde <= base_time]
        return packages

    def get_base_price_and_tax_rate(self):
        return self.precio, self.impuesto

    @classmethod
    def get_active_package_by_id(cls, id_package):
        try:
            package = Paquete.query(ancestor=ndb.Key(_PaqueteCompartido, id_package))\
                .filter(Paquete.activo == True).get()
        except ValueError:
            package = None
        if package is None:
            from CJM.services.validations import PACKAGE_DOES_NOT_EXISTS_ERROR_CODE
            raise EntityDoesNotExists(u"Package[{0}]".format(id_package),
                                      internal_code=PACKAGE_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return package

    @classmethod
    def _get_by_external_code_without_get(cls, external_code):
        return cls.query(cls.codigoExterno == external_code).filter(cls.activo == True)

    @classmethod
    def get_by_external_code(cls, external_code):
        return cls._get_by_external_code_without_get(external_code).get()

    @classmethod
    def get_by_external_code_async(cls, external_code):
        return cls._get_by_external_code_without_get(external_code).get_async()

    @classmethod
    def get_key_from_ids(cls, id_package, id_shared_package):
        return ndb.Key(Paquete, id_package, parent=Paquete.get_shared_key_from_shared_id(id_shared_package))

    @classmethod
    def get_by_shared_key_and_id(cls, shared_key, id_package):
        package_key = ndb.Key(Paquete, id_package, parent=shared_key)
        return package_key.get()

    def get_shared_key(self):
        return Paquete.get_shared_key_from_shared_id(self.idCompartido)

    @classmethod
    def get_shared_key_from_shared_id(cls, shared_id):
        return ndb.Key(_PaqueteCompartido, shared_id)

    def to_dict(self):
        from commons.validations import DEFAULT_DATETIME_FORMAT
        fields_dict = dict()
        fields_dict[Paquete.HISTORIC_ID_NAME] = self.key.id()
        fields_dict[Paquete.ID_NAME] = self.idCompartido
        fields_dict[Paquete.PACKAGE_NAME_NAME] = self.nombre
        fields_dict[Paquete.BASE_PRICE_NAME] = self.precio
        fields_dict[Paquete.TAX_RATE_NAME] = self.impuesto
        fields_dict[Paquete.DESCRIPTION_NAME] = self.descripcion
        fields_dict[Paquete.RESTRICTED_CONSUMPTION_NAME] = self.consumoRestringido
        fields_dict[Paquete.VALID_FROM_NAME] = self.validoDesde.strftime(DEFAULT_DATETIME_FORMAT)
        fields_dict[Paquete.VALID_THROUGH_NAME] = self.validoHasta.strftime(DEFAULT_DATETIME_FORMAT)
        fields_dict[Paquete.ACTIVE_NAME] = self.activo
        fields_dict[Paquete.LOCATION_ID_NAME] = self.idUbicacion
        fields_dict[Paquete.EXTERNAL_CODE_NAME] = self.codigoExterno
        if self.disponibleParaLaVenta is None:
            available_for_sale = True
        else:
            available_for_sale = self.disponibleParaLaVenta
        fields_dict[Paquete.AVAILABLE_FOR_SALE] = available_for_sale
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
