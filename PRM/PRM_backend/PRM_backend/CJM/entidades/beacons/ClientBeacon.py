# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from CJM.entidades.beacons.BeaconType import BeaconType
from CJM.entidades.beacons.GlobalBeacon import GlobalBeacon
from CJM.entidades.persons.Persona import Persona
from commons.entidades.EPC import format_hex
from commons.entidades.locations.Ubicacion import Ubicacion


class ClientBeacon(ndb.Model):

    ID_NAME = GlobalBeacon.ID_NAME
    TYPE_NAME = GlobalBeacon.TYPE_NAME
    MAJOR_NAME = "major"
    MINOR_NAME = "minor"
    KEY_NAME = "key"
    ID_BEACON_NAME = "id-beacon"
    UBICACION_ID_NAME = Ubicacion.ID_UBICACION_NAME
    PERSON_ID_NAME = Persona.ID_PERSON_NAME
    GLOBAL_BEACON_ID_NAME = GlobalBeacon.ID_GLOBAL_BEACON_NAME

    idPersona = ndb.IntegerProperty(indexed=True)
    minorMajorKey = ndb.StringProperty(indexed=True)
    idInterno = ndb.IntegerProperty()
    tipo = ndb.IntegerProperty()
    idUbicacion = ndb.IntegerProperty()
    major = ndb.IntegerProperty()
    minor = ndb.IntegerProperty()
    idCliente = ndb.IntegerProperty()
    esGlobal = ndb.IntegerProperty()

    @classmethod
    def register_person(cls, id_client, id_person, id_beacon):
        beacon = ndb.Key(ClientBeacon, id_beacon).get()
        beacon.idPersona = id_person
        beacon.idCliente = id_client
        beacon.put()
        return beacon

    @classmethod
    def create_static(cls, id_beacon, id_client, id_location, major, minor, is_global):
        beacon = ClientBeacon(key=ndb.Key(ClientBeacon, id_beacon),
                              minorMajorKey=ClientBeacon.major_minor_to_key(major, minor), idInterno=id_beacon,
                              major=major, minor=minor, tipo=BeaconType.STATIC_BEACON_TYPE,
                              idUbicacion=id_location, idCliente=id_client, esGlobal=is_global)
        beacon.put()
        return beacon

    @classmethod
    def create_mobile(cls, id_beacon, id_client, major, minor, is_global):
        beacon = ClientBeacon(key=ndb.Key(ClientBeacon, id_beacon),
                              minorMajorKey=ClientBeacon.major_minor_to_key(major, minor), idInterno=id_beacon,
                              idCliente=id_client, major=major, minor=minor, tipo=BeaconType.MOBILE_BEACON_TYPE,
                              esGlobal=is_global)
        beacon.put()
        return beacon

    @staticmethod
    def major_minor_to_key(major, minor):
        hex_major = format_hex(major, 4)  # 4
        hex_minor = format_hex(minor, 4)  # 4
        return hex_major + '_' + hex_minor

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.UBICACION_ID_NAME] = self.idUbicacion
        fields_dict[self.PERSON_ID_NAME] = self.idPersona
        fields_dict[self.TYPE_NAME] = BeaconType.beacon_type_to_string(self.tipo)
        fields_dict[self.MAJOR_NAME] = self.major
        fields_dict[self.MINOR_NAME] = self.minor
        fields_dict[self.KEY_NAME] = self.minorMajorKey
        fields_dict[self.ID_NAME] = self.idInterno
        return fields_dict

    @staticmethod
    def list():
        return ClientBeacon.query().fetch()

    @staticmethod
    def list_static():
        return ClientBeacon.query(ClientBeacon.tipo == BeaconType.STATIC_BEACON_TYPE).fetch()

    @staticmethod
    def list_mobile():
        return ClientBeacon.query(ClientBeacon.tipo == BeaconType.MOBILE_BEACON_TYPE).fetch()
