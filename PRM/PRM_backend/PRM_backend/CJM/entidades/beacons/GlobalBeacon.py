# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from CJM.entidades.beacons.BeaconType import BeaconType
from commons.entidades.Cliente import Cliente
from commons.entidades.Generador import Generador
from commons.excepciones.apiexceptions import ValidationError


class GlobalBeacon(ndb.Model):

    ID_NAME = "id"
    TYPE_NAME = "type"
    ID_GLOBAL_BEACON_NAME = "id-global-beacon"
    ID_CLIENT_NAME = Cliente.ID_CLIENT_NAME

    idInterno = ndb.IntegerProperty()
    tipo = ndb.IntegerProperty()
    idCliente = ndb.IntegerProperty(indexed=True)

    @classmethod
    def create_static(cls):
        id_beacon = Generador.get_next_beacon_id()
        beacon = GlobalBeacon(key=ndb.Key(GlobalBeacon, id_beacon),
                              idInterno=id_beacon, tipo=BeaconType.STATIC_BEACON_TYPE)
        beacon.put()
        return beacon

    @classmethod
    def create_mobile(cls):
        id_beacon = Generador.get_next_beacon_id()
        beacon = GlobalBeacon(key=ndb.Key(GlobalBeacon, id_beacon),
                              idInterno=id_beacon, tipo=BeaconType.MOBILE_BEACON_TYPE)
        beacon.put()
        return beacon

    @classmethod
    def register_client(cls, id_beacon, id_client, expected_type):
        beacon = GlobalBeacon.get_by_id(id_beacon)
        if beacon.idCliente is not None:
            raise ValidationError(u"The beacon with id {0} already has an assigned client.".format(id_beacon))
        if beacon.tipo != expected_type:
            raise ValidationError(u"The beacon to assign is not of type {0}."
                                  .format(BeaconType.beacon_type_to_string(expected_type)))
        beacon.idCliente = id_client
        beacon.put()

    @classmethod
    def unregister_client(cls, id_beacon):
        beacon = GlobalBeacon.get_by_id(id_beacon)
        if beacon.idCliente is None:
            raise ValidationError(u"The beacon with id {0} does not have an assigned client.")
        del beacon.idCliente
        beacon.put()

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.ID_CLIENT_NAME] = self.idCliente
        fields_dict[self.TYPE_NAME] = BeaconType.beacon_type_to_string(self.tipo)
        fields_dict[self.ID_NAME] = self.idInterno
        return fields_dict

    @staticmethod
    def list_static():
        return GlobalBeacon.query(GlobalBeacon.tipo == BeaconType.STATIC_BEACON_TYPE).fetch()

    @staticmethod
    def list_mobile():
        return GlobalBeacon.query(GlobalBeacon.tipo == BeaconType.MOBILE_BEACON_TYPE).fetch()

    @staticmethod
    def list():
        return GlobalBeacon.query().fetch()
