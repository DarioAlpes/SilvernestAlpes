# -*- coding: utf-8 -*
import json

from google.appengine.ext import ndb

from CJM.entidades.persons.Persona import Persona
from CJM.entidades.sensores.SensorType import SensorType
from CJM.services.validations import CLIENT_SENSOR_ALREADY_EXISTS_ERROR_CODE, CLIENT_SENSOR_ALREADY_ASSIGNED_ERROR_CODE, \
    CLIENT_SENSOR_PERSON_ON_STATIC_ERROR_CODE, CLIENT_SENSOR_LOCATION_ON_MOBILE_ERROR_CODE, \
    GLOBAL_SENSOR_DOES_NOT_EXISTS_ERROR_CODE, \
    CLIENT_SENSOR_DOES_NOT_EXISTS_ERROR_CODE
from commons.entidades.Cliente import Cliente
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.excepciones.apiexceptions import EntityDoesNotExists, EntityAlreadyExists, ValidationError
from commons.validations import validate_datetime


class Sensor(ndb.Model):

    ID_NAME = "id"
    TYPE_NAME = "type"
    ACTIVE_NAME = "active"
    SYNCED_NAME = "synced"
    LAST_SYNC_NAME = "last-sync"
    LAST_ACTIVATION_NAME = "last-activation"
    LAST_DEACTIVATION_NAME = "last-deactivation"
    CLIENT_ID_NAME = Cliente.ID_CLIENT_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME
    PERSON_ID_NAME = Persona.ID_PERSON_NAME

    tipo = ndb.StringProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)
    idPersona = ndb.IntegerProperty(indexed=True)
    idCliente = ndb.IntegerProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    ultimaFechaActivacion = ndb.DateTimeProperty(indexed=True)
    ultimaFechaSincronizacion = ndb.DateTimeProperty(indexed=True)
    ultimaFechaDesactivacion = ndb.DateTimeProperty(indexed=True)

    @classmethod
    def create(cls, sensor_id, sensor_type):
        sensor = Sensor(key=ndb.Key(Sensor, sensor_id),
                        tipo=sensor_type,
                        activo=False)
        sensor.change_active_status(False)
        sensor.put()
        return sensor

    def clone(self):
        cloned_sensor = Sensor(key=self.key,
                               tipo=self.tipo,
                               idUbicacion=self.idUbicacion,
                               idPersona=self.idPersona,
                               idCliente=self.idCliente,
                               activo=self.activo,
                               ultimaFechaSincronizacion=self.ultimaFechaSincronizacion,
                               ultimaFechaActivacion=self.ultimaFechaActivacion,
                               ultimaFechaDesactivacion=self.ultimaFechaDesactivacion)
        return cloned_sensor

    def to_dict(self):
        from commons.validations import DEFAULT_DATETIME_FORMAT
        fields_dict = dict()
        fields_dict[Sensor.ID_NAME] = self.key.id()
        fields_dict[Sensor.TYPE_NAME] = self.tipo
        fields_dict[Sensor.CLIENT_ID_NAME] = self.idCliente
        fields_dict[Sensor.PERSON_ID_NAME] = self.idPersona
        fields_dict[Sensor.LOCATION_ID_NAME] = self.idUbicacion
        fields_dict[Sensor.ACTIVE_NAME] = self.activo
        if self.ultimaFechaSincronizacion is not None:
            fields_dict[Sensor.LAST_SYNC_NAME] = self.ultimaFechaSincronizacion.strftime(DEFAULT_DATETIME_FORMAT)
        if self.ultimaFechaActivacion is not None:
            fields_dict[Sensor.LAST_ACTIVATION_NAME] = self.ultimaFechaActivacion.strftime(DEFAULT_DATETIME_FORMAT)
        if self.ultimaFechaDesactivacion is not None:
            fields_dict[Sensor.LAST_DEACTIVATION_NAME] = self.ultimaFechaDesactivacion.strftime(DEFAULT_DATETIME_FORMAT)
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def list_by_id_client(cls, id_client):
        return Sensor.query(Sensor.idCliente == id_client).fetch()

    @staticmethod
    def list():
        return Sensor.query().fetch()

    def is_static(self):
        return self.tipo == SensorType.STRING_STATIC

    def is_mobile(self):
        return self.tipo == SensorType.STRING_MOBILE

    @classmethod
    def get_sensor_by_id(cls, id_sensor):
        sensor = None
        try:
            if id_sensor is not None:
                sensor = cls.get_by_id(id_sensor)
        except ValueError:
            pass
        if sensor is None:
            raise EntityDoesNotExists(u"Sensor[{0}]".format(id_sensor),
                                      internal_code=GLOBAL_SENSOR_DOES_NOT_EXISTS_ERROR_CODE)
        return sensor

    @classmethod
    def delete_sensor_by_id(cls, id_sensor):
        sensor = cls.get_sensor_by_id(id_sensor)
        sensor.key.delete()
        return sensor

    def assign_client(self, id_client):
        if self.idCliente is not None:
            if self.idCliente == id_client:
                raise EntityAlreadyExists(u"Sensor[{0}]".format(self.key.id()),
                                          internal_code=CLIENT_SENSOR_ALREADY_EXISTS_ERROR_CODE)
            else:
                raise EntityAlreadyExists(u"Sensor[{0}]".format(self.key.id()),
                                          internal_code=CLIENT_SENSOR_ALREADY_ASSIGNED_ERROR_CODE)
        else:
            self.idCliente = id_client

    def assign_location(self, id_location):
        if id_location is not None and self.is_mobile():
            raise ValidationError(u"Can not assign a location to a mobile sensor.",
                                  internal_code=CLIENT_SENSOR_LOCATION_ON_MOBILE_ERROR_CODE)
        self.idUbicacion = id_location

    def assign_person(self, id_person):
        if id_person is not None and self.is_static():
            raise ValidationError(u"Can not assign a person to a static sensor.",
                                  internal_code=CLIENT_SENSOR_PERSON_ON_STATIC_ERROR_CODE)
        self.idPersona = id_person

    def remove_client(self):
        self.idPersona = None
        self.idUbicacion = None
        self.idCliente = None
        self.change_active_status(False)

    def change_active_status(self, active):
        self.activo = active
        current_date = validate_datetime(None, "Current Date", allow_none=True)
        if active:
            # noinspection PyUnresolvedReferences
            self.ultimaFechaActivacion = current_date
        else:
            # noinspection PyUnresolvedReferences
            self.ultimaFechaDesactivacion = current_date

    def register_synced(self, synced):
        if synced:
            current_date = validate_datetime(None, "Current Date", allow_none=True)
            # noinspection PyUnresolvedReferences
            self.ultimaFechaSincronizacion = current_date

    def check_id_client(self, id_client, id_sensor):
        if self.idCliente != id_client:
            raise EntityDoesNotExists(u"Sensor[{0}]".format(id_sensor),
                                      internal_code=CLIENT_SENSOR_DOES_NOT_EXISTS_ERROR_CODE)
