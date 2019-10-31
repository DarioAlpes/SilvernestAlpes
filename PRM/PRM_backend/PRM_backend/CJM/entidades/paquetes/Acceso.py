# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.paquetes.Paquete import Paquete
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.excepciones.apiexceptions import EntityDoesNotExists


class _AccesoCompartido(ndb.Model):
    @classmethod
    def create(cls, package):
        new_access = _AccesoCompartido(parent=package.get_shared_key())
        new_access.put()
        return new_access


class Acceso(ndb.Model):
    ID_NAME = "id"
    HISTORIC_ID_NAME = "historic-id"
    AMOUNT_INCLUDED_NAME = "amount-included"
    UNLIMITED_AMOUNT_NAME = "unlimited-amount"
    ACTIVE_NAME = "active"

    ID_ACCESS_NAME = "id-access"

    HISTORIC_PACKAGE_ID_NAME = Paquete.ID_PACKAGE_HISTORIC_NAME
    PACKAGE_ID_NAME = Paquete.ID_PACKAGE_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idCliente = ndb.IntegerProperty()
    idPaquete = ndb.IntegerProperty(indexed=True)
    idPaqueteCompartido = ndb.IntegerProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)
    idCompartido = ndb.IntegerProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    cantidadIncluida = ndb.IntegerProperty(indexed=True)

    @classmethod
    def create(cls, id_client, package, id_location, amount_included):
        shared_access = _AccesoCompartido.create(package)
        id_access = shared_access.key.id()

        new_access = Acceso(idCompartido=id_access,
                            idCliente=id_client,
                            idPaquete=package.key.id(),
                            idPaqueteCompartido=package.idCompartido,
                            idUbicacion=id_location,
                            cantidadIncluida=amount_included,
                            activo=True,
                            parent=shared_access.key)

        new_access.put()
        return new_access

    @classmethod
    def update(cls, id_access, package, id_location, amount_included):
        access_to_update = Acceso.get_by_package(package, id_access)
        access_to_update.idUbicacion = id_location
        access_to_update.cantidadIncluida = amount_included
        access_to_update.put()
        return access_to_update

    def clone(self, new_package):
        new_access = Acceso(idCompartido=self.idCompartido,
                            idCliente=self.idCliente,
                            idPaquete=new_package.key.id(),
                            idPaqueteCompartido=new_package.idCompartido,
                            idUbicacion=self.idUbicacion,
                            cantidadIncluida=self.cantidadIncluida,
                            activo=True,
                            parent=self.get_shared_key(new_package))
        return new_access

    @classmethod
    def list(cls):
        return Acceso.query().fetch()

    @classmethod
    def _list_accesses_by_package_without_fetch(cls, package):
        return Acceso.query(ancestor=package.get_shared_key()).filter(Acceso.idPaquete == package.key.id())

    @classmethod
    def list_accesses_by_package(cls, package):
        return cls._list_accesses_by_package_without_fetch(package).fetch()

    @classmethod
    def get_by_package(cls, package, id_access):
        try:
            access = Acceso.query(ancestor=Acceso._get_shared_key(package, id_access))\
                .filter(Acceso.idPaquete == package.key.id()).get()
        except ValueError:
            access = None
        if access is None:
            from CJM.services.validations import ACCESS_DOES_NOT_EXISTS_ERROR_CODE
            raise EntityDoesNotExists(u"Access[{0}]".format(id_access),
                                      internal_code=ACCESS_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return access

    @classmethod
    def clone_package_accesses(cls, old_package, new_package):
        accesses = Acceso.list_accesses_by_package(old_package)
        for access in accesses:
            access.activo = False
        new_accesses = [access.clone(new_package) for access in accesses]
        ndb.put_multi(accesses + new_accesses)

    @classmethod
    def _get_shared_key(cls, package, shared_id):
        return ndb.Key(_AccesoCompartido, shared_id, parent=package.get_shared_key())

    def get_shared_key(self, package):
        return Acceso._get_shared_key(package, self.idCompartido)

    def is_unlimited(self):
        return self.cantidadIncluida == 0

    def to_dict(self):
        fields_dict = dict()
        fields_dict[Acceso.HISTORIC_ID_NAME] = self.key.id()
        fields_dict[Acceso.ID_NAME] = self.idCompartido
        fields_dict[Acceso.PACKAGE_ID_NAME] = self.idPaqueteCompartido
        fields_dict[Acceso.HISTORIC_PACKAGE_ID_NAME] = self.idPaquete
        fields_dict[Acceso.ACTIVE_NAME] = self.activo
        fields_dict[Acceso.LOCATION_ID_NAME] = self.idUbicacion
        fields_dict[Acceso.AMOUNT_INCLUDED_NAME] = self.cantidadIncluida
        fields_dict[Acceso.UNLIMITED_AMOUNT_NAME] = self.is_unlimited()
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def get_by_package_and_id_location(cls, package, id_location):
        try:
            consumption = Acceso.query().filter(ndb.AND(Acceso.idPaquete == package.key.id(),
                                                        Acceso.idUbicacion == id_location)).fetch()
        except ValueError:
            consumption = None
        return consumption

    @classmethod
    def get_available_amounts_for_package_by_ids_locations(cls, package):
        amounts_available = dict()
        accesses = cls.list_accesses_by_package(package)
        for access in accesses:
            if amounts_available.get(access.idUbicacion) is None:
                amounts_available[access.idUbicacion] = access.cantidadIncluida
            elif amounts_available.get(access.idUbicacion) > 0:
                amounts_available[access.idUbicacion] += access.cantidadIncluida
        return amounts_available

    @classmethod
    def get_available_amounts_by_ids_locations(cls, package, reservation):
        amounts_available = dict()
        accesses = cls.list_accesses_by_package(package)
        number_of_days = reservation.get_number_of_days()

        for access in accesses:
            amounts_available[access.idUbicacion] = \
                amounts_available.get(access.idUbicacion, 0) + number_of_days * access.cantidadIncluida

        return amounts_available

    @classmethod
    def get_ids_locations_of_package_acceses_with_unlimited_amount(cls, package):
        unlimited_amount_acceses = cls._list_accesses_by_package_without_fetch(package).filter(Acceso.cantidadIncluida == 0).fetch()
        return [access.idUbicacion for access in unlimited_amount_acceses]
