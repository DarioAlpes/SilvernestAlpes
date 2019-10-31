# -*- coding: utf-8 -*
from datetime import timedelta
from google.appengine.ext import ndb

from CJM.entidades.beacons.ClientBeacon import ClientBeacon
from CJM.entidades.eventos.Visita import Visita
from CJM.services.validations import validate_id_client_beacon
from commons.entidades.Generador import Generador
from commons.excepciones.apiexceptions import ValidationError
from commons.validations import validate_datetime


class LogLectura(ndb.Model):
    MAX_DELTA_FOR_UPDATE = timedelta(minutes=5)

    READINGS_QUEUE_NAME = "readings"

    READINGS_NAME = "readings"
    ID_NAME = "id"
    READING_TIME_NAME = "reading-time"
    BEACON_ID_NAME = ClientBeacon.ID_BEACON_NAME

    idInterno = ndb.IntegerProperty()
    idBeacon = ndb.IntegerProperty(indexed=True)
    idSensor = ndb.StringProperty(indexed=True)
    tiempoLectura = ndb.DateTimeProperty(indexed=False)
    tiempoCreacion = ndb.DateTimeProperty(indexed=False, auto_now_add=True)

    def get_id(self):
        return self.key.id()

    @classmethod
    def create(cls, id_beacon, id_sensor, reading_time):
        id_reading = Generador.get_next_reading_log_id()
        return LogLectura(
            key=ndb.Key(LogLectura, id_reading),
            idInterno=id_reading,
            idBeacon=id_beacon,
            idSensor=id_sensor,
            tiempoLectura=reading_time
        )

    @staticmethod
    def create_readings(id_sensor, readings):
        reading_logs = []
        for reading in readings:
            if LogLectura.BEACON_ID_NAME not in reading:
                raise ValidationError(u"The field {0} is required for every reading."
                                      .format(LogLectura.BEACON_ID_NAME), error_code=404)

            id_beacon = validate_id_client_beacon(reading[LogLectura.BEACON_ID_NAME])

            reading_time = validate_datetime(reading[LogLectura.READING_TIME_NAME], LogLectura.READING_TIME_NAME,
                                             allow_none=False)

            reading_logs.append(LogLectura.create(id_beacon, id_sensor, reading_time))

        ndb.put_multi(reading_logs)
        return reading_logs

    @staticmethod
    def process_reading(id_reading, id_client, sensor):
        reading_log = LogLectura.get_by_id(id_reading)

        beacon = ClientBeacon.get_by_id(reading_log.idBeacon)

        if beacon.idPersona is not None:
            id_person = beacon.idPersona
        elif sensor.idPersona is not None:
            id_person = sensor.idPersona
        else:
            raise ValidationError(u"Can not infer a person from sensor or beacon.".format(beacon.idInterno))

        if beacon.idUbicacion is not None:
            id_location = beacon.idUbicacion
        elif sensor.idUbicacion is not None:
            id_location = sensor.idUbicacion
        else:
            raise ValidationError(u"Can not infer a location from sensor or beacon.".format(beacon.idInterno))

        previous_visit = Visita.query(ndb.AND(Visita.tiempoInicio <= reading_log.tiempoLectura,
                                              Visita.idPersona == id_person,
                                              Visita.idUbicacion == id_location)).\
            order(-Visita.tiempoInicio).get()

        next_visit = Visita.query(ndb.AND(Visita.tiempoInicio > reading_log.tiempoLectura,
                                          Visita.idPersona == id_person,
                                          Visita.idUbicacion == id_location)). \
            order(Visita.tiempoInicio).get()

        if previous_visit is not None:
            # Si existe una visita que empieza antes del tiempo del log y termina después del tiempo del log
            # no hacer nada
            if reading_log.tiempoLectura <= previous_visit.tiempoFin:
                return []
            # Si existe una visita que empieza antes del tiempo del log y termina menos de DELTA minutos antes que
            # el log actualizar la visita para indicar que la persona sigue en la ubicación
            elif reading_log.tiempoLectura <= previous_visit.tiempoFin + LogLectura.MAX_DELTA_FOR_UPDATE:
                previous_visit.tiempoFin = reading_log.tiempoLectura
                # Si existe una visita que empieza menos de DELTA minutos después del tiempo del log fusionar las
                # dos visitas
                if next_visit is not None \
                        and reading_log.tiempoLectura >= next_visit.tiempoInicio - LogLectura.MAX_DELTA_FOR_UPDATE:
                    previous_visit.tiempoFin = next_visit.tiempoFin
                    next_visit.key.delete()
                previous_visit.put()
                return [previous_visit]

        # Si existe una visita que empieza menos de DELTA minutos después del tiempo del log actualizar el tiempo
        # inicial al tiempo del log
        if next_visit is not None:
            if reading_log.tiempoLectura >= next_visit.tiempoInicio - LogLectura.MAX_DELTA_FOR_UPDATE:
                next_visit.tiempoInicio = reading_log.tiempoLectura
                next_visit.put()
                return [next_visit]

        # Si no existe un Visita con suficiente cercania crear una nueva visita
        return [Visita.create(id_client, id_person, reading_log.tiempoLectura,
                              reading_log.tiempoLectura, id_location)]

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = dict()
        fields_dict[self.ID_NAME] = self.idInterno
        fields_dict[self.READING_TIME_NAME] = parse_datetime_to_string_on_default_format(self.tiempoLectura)
        fields_dict[self.BEACON_ID_NAME] = self.idBeacon
        return fields_dict
