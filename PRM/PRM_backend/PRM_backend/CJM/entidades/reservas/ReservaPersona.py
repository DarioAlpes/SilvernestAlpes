# -*- coding: utf-8 -*
import json

from google.appengine.ext import ndb

from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.persons.Persona import Persona
from CJM.entidades.reservas.EntidadHijaReserva import EntidadHijaReserva
from CJM.services.validations import PERSON_RESERVATION_DOES_NOT_EXISTS_ERROR_CODE, PACKAGE_DOES_NOT_EXISTS_ERROR_CODE
from commons.entidades.users.Usuario import Usuario
from commons.excepciones.apiexceptions import EntityDoesNotExists
from commons.validations import validate_datetime


class ReservaPersona(EntidadHijaReserva):
    ACTIVE_NAME = "active"
    IS_HOLDER_NAME = "is-holder"
    BASE_PRICE_NAME = "base-price"
    TAX_RATE_NAME = "tax-rate"
    INITIAL_DATE_NAME = "initial-date"
    FINAL_DATE_NAME = "final-date"
    ACTIVATION_DATES_NAME = "activation-dates"
    DEACTIVATION_DATES_NAME = "deactivation-dates"
    PURCHASE_TIME_NAME = "purchase-time"

    ID_PERSON_RESERVATION_NAME = "id-person-reservation"
    PERSON_ID_NAME = Persona.ID_PERSON_NAME
    PACKAGE_ID_NAME = Paquete.ID_PACKAGE_NAME
    HISTORIC_PACKAGE_ID_NAME = Paquete.ID_PACKAGE_HISTORIC_NAME

    idReserva = ndb.IntegerProperty(indexed=True)
    idPaquete = ndb.IntegerProperty(indexed=True)
    idPaqueteCompartido = ndb.IntegerProperty(indexed=True)
    idPersona = ndb.IntegerProperty(indexed=True)
    precio = ndb.FloatProperty(indexed=True)
    impuesto = ndb.FloatProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    esPrincipal = ndb.BooleanProperty(indexed=True)
    idInterno = ndb.IntegerProperty(indexed=True)
    fechasActivacion = ndb.DateTimeProperty(indexed=True, repeated=True)
    fechasDesactivacion = ndb.DateTimeProperty(indexed=True, repeated=True)
    fechaCompra = ndb.DateTimeProperty(indexed=True)
    fechaInicial = ndb.DateTimeProperty(indexed=True)
    fechaFinal = ndb.DateTimeProperty(indexed=True)

    @classmethod
    def create_without_put(cls, id_client, id_reservation, package, id_person, is_holder, base_price, tax_rate,
                           initial_date, final_date, purchase_time=None, username=None):
        if username is None:
            username = Usuario.get_current_username()
        if purchase_time is None:
            purchase_time = validate_datetime(None, cls.PURCHASE_TIME_NAME, allow_none=True)
        new_person_reservation = ReservaPersona(idCliente=id_client,
                                                idReserva=id_reservation,
                                                idPaquete=package.key.id(),
                                                idPaqueteCompartido=package.idCompartido,
                                                idPersona=id_person,
                                                precio=base_price,
                                                impuesto=tax_rate,
                                                activo=False,
                                                eliminada=False,
                                                esPrincipal=is_holder,
                                                fechaInicial=initial_date,
                                                fechaFinal=final_date,
                                                fechaCompra=purchase_time,
                                                fechasActivacion=[],
                                                fechasDesactivacion=[],
                                                usuario=username)
        return new_person_reservation

    def clone(self, id_reservation, username):
        return ReservaPersona(idCliente=self.idCliente,
                              idReserva=id_reservation,
                              idPaquete=self.idPaquete,
                              idPaqueteCompartido=self.idPaqueteCompartido,
                              idPersona=self.idPersona,
                              precio=self.precio,
                              impuesto=self.impuesto,
                              activo=self.activo,
                              eliminada=self.eliminada,
                              esPrincipal=self.esPrincipal,
                              fechaInicial=self.fechaInicial,
                              fechaFinal=self.fechaFinal,
                              fechaCompra=self.fechaCompra,
                              fechasActivacion=self.fechasActivacion,
                              fechasDesactivacion=self.fechasDesactivacion,
                              usuario=username)

    @classmethod
    def create(cls, id_client, id_reservation, package, id_person, is_holder, base_price, tax_rate, initial_date,
               final_date, purchase_time=None, username=None):
        new_person_reservation = cls.create_without_put(id_client, id_reservation, package, id_person, is_holder,
                                                        base_price, tax_rate, initial_date, final_date, purchase_time,
                                                        username)
        new_person_reservation.put()
        return new_person_reservation

    def update(self, package, id_person, active, base_price, tax_rate, initial_date, final_date):
        self.update_basic_data_without_put(package, id_person, base_price, tax_rate, initial_date, final_date)
        self.change_active_status(active)

    def update_basic_data_without_put(self, package, id_person, base_price, tax_rate, initial_date, final_date):
        self.idPaquete = package.key.id()
        self.idPaqueteCompartido = package.idCompartido
        self.idPersona = id_person
        self.precio = base_price
        self.impuesto = tax_rate
        self.fechaInicial = initial_date
        self.fechaFinal = final_date

    def is_holder(self):
        if self.esPrincipal is None:
            return False
        else:
            return self.esPrincipal

    @classmethod
    def list_by_range_of_times(cls, initial_time, final_time):
        query = cls.list_without_fetch()
        return cls._filter_query_by_times(query, initial_time, final_time)

    @classmethod
    def list_by_range_of_times_and_id_person(cls, id_person, initial_time, final_time):
        query = cls._list_by_id_person_without_fetch(id_person)
        return cls._filter_query_by_times(query, initial_time, final_time)

    @classmethod
    def _filter_query_by_times(cls, query, initial_time, final_time):
        if initial_time is not None:
            person_reservations = query.filter(cls.fechaFinal >= initial_time).fetch()
            if final_time is not None:
                person_reservations = [person_reservation for person_reservation in person_reservations
                                       if person_reservation.fechaInicial <= final_time]
        elif final_time is not None:
            person_reservations = query.filter(cls.fechaInicial <= final_time).fetch()
        else:
            person_reservations = query.fetch()
        return person_reservations

    @classmethod
    def list_by_dates_and_id_person_async(cls, id_person, initial_date, final_date):
        return cls._list_by_id_person_without_fetch(id_person).filter(cls.fechaInicial == initial_date) \
            .filter(cls.fechaFinal == final_date).fetch_async()

    @classmethod
    def _list_by_id_person_without_fetch(cls, id_person):
        return cls.list_without_fetch().filter(cls.idPersona == id_person)

    @classmethod
    def list_by_id_person_async(cls, id_person):
        return cls._list_by_id_person_without_fetch(id_person).fetch_async()

    @classmethod
    def list_by_id_person(cls, id_person):
        return cls._list_by_id_person_without_fetch(id_person).fetch()

    @classmethod
    def _list_by_specific_package_without_fetch(cls, package):
        return cls.list_without_fetch().filter(ndb.AND(cls.idPaqueteCompartido == package.idCompartido,
                                                       cls.idPaquete == package.key.id()))

    @classmethod
    def get_number_person_reservations_specific_package(cls, package):
        return cls._list_by_specific_package_without_fetch(package).count()

    @classmethod
    def try_get_by_id_reservation(cls, id_reservation, id_person_reservation):
        person_reservation = cls.get_by_reservation_ids(id_reservation, id_person_reservation)
        if person_reservation is None:
            raise EntityDoesNotExists(u"Person Reservation[{0}]".format(id_person_reservation),
                                      internal_code=PERSON_RESERVATION_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return person_reservation

    def get_package_key(self):
        return Paquete.get_key_from_ids(self.idPaquete, self.idPaqueteCompartido)

    def try_get_package(self):
        package = self.get_package_key().get()
        if package is None:
            raise EntityDoesNotExists(u"Package[({0}, {1})]".format(self.idPaqueteCompartido, self.idPaquete),
                                      internal_code=PACKAGE_DOES_NOT_EXISTS_ERROR_CODE)
        return package

    def change_active_status(self, active):
        from CJM.entidades.reservas.ActivacionesReservaPersona import ActivacionesReservaPersona
        if self.activo != active:
            self.activo = active
            current_date = validate_datetime(None, "Current Date", allow_none=True)
            if active:
                if self.fechasActivacion is None:
                    self.fechasActivacion = []
                self.fechasActivacion.append(current_date)
            else:
                if self.fechasDesactivacion is None:
                    self.fechasDesactivacion = []
                self.fechasDesactivacion.append(current_date)
            activation_entity = ActivacionesReservaPersona.create_without_put(self, current_date, active)
            ndb.put_multi([activation_entity, self])
        else:
            self.put()
        return self

    def get_number_of_days(self):
        return (self.fechaFinal - self.fechaInicial).days + 1

    @classmethod
    def get_key_from_id(cls, id_entity):
        return ndb.Key(cls, id_entity)

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = super(ReservaPersona, self).to_dict()
        fields_dict[self.ACTIVE_NAME] = self.activo
        fields_dict[self.PACKAGE_ID_NAME] = self.idPaqueteCompartido
        fields_dict[self.HISTORIC_PACKAGE_ID_NAME] = self.idPaquete
        fields_dict[self.BASE_PRICE_NAME] = self.precio
        fields_dict[self.TAX_RATE_NAME] = self.impuesto
        fields_dict[self.PERSON_ID_NAME] = self.idPersona
        fields_dict[self.IS_HOLDER_NAME] = self.is_holder()
        fields_dict[self.INITIAL_DATE_NAME] = parse_datetime_to_string_on_default_format(self.fechaInicial)
        fields_dict[self.PURCHASE_TIME_NAME] = parse_datetime_to_string_on_default_format(self.fechaCompra)
        fields_dict[self.FINAL_DATE_NAME] = parse_datetime_to_string_on_default_format(self.fechaFinal)
        fields_dict[self.ACTIVATION_DATES_NAME] = [parse_datetime_to_string_on_default_format(activation_date)
                                                   for activation_date in self.fechasActivacion]
        fields_dict[self.DEACTIVATION_DATES_NAME] = [parse_datetime_to_string_on_default_format(deactivation_date)
                                                     for deactivation_date in self.fechasDesactivacion]
        return fields_dict


class UserActivations:
    USERNAME_NAME = Usuario.USERNAME_NAME
    ACTIVATIONS_NAME = "activations"
    DEACTIVATIONS_NAME = "deactivations"

    def __init__(self, username):
        self.username = username
        self.activations = []
        self.deactivations = []
        self.total = 0

    def add_activation(self, activation):
        if activation.esActivacion:
            self.activations.append(activation)
        else:
            self.deactivations.append(activation)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.USERNAME_NAME] = self.username
        self.activations.sort(key=lambda current_activation: current_activation.fechaOperacion)
        fields_dict[self.ACTIVATIONS_NAME] = [activation.to_dict() for activation in self.activations]
        self.deactivations.sort(key=lambda current_deactivation: current_deactivation.fechaOperacion)
        fields_dict[self.DEACTIVATIONS_NAME] = [deactivations.to_dict() for deactivations in self.deactivations]
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
