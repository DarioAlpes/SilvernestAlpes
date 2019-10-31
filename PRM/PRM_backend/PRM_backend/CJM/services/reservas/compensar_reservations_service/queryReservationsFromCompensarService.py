# -*- coding: utf-8 -*
import collections
import json
import logging
import traceback
import time as time_measure
import requests

from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.persons.Persona import Persona
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.entidades.skus.SKU import SKU
from CJM.services.persons.queryPersonFromCompensarService import check_document_type_and_parse_to_string
from CJM.services.validations import BENEFICIARIO_AFFILIATION, COTIZANTE_AFFILIATION, C_CATEGORY, A_CATEGORY, \
    B_CATEGORY, D_CATEGORY, NO_DOCUMENT_DOCUMENT_TYPE, PACKAGE_DOES_NOT_EXISTS_ERROR_CODE, \
    SKU_DOES_NOT_EXISTS_ERROR_CODE, ERROR_QUERYING_ORBITA_ERROR_CODE
from datetime import datetime, time, timedelta

from commons.entidades.users.Usuario import Usuario
from commons.excepciones.apiexceptions import EntityDoesNotExists, ValidationError, APIException
from commons.validations import validate_datetime, validate_date
from config_loader import get_pass_compensar_orbita, is_prod

RESPONSE_DATE_FORMAT = "%Y/%m/%d"
RESPONSE_TIME_FORMAT = "%H:%M:%S"
REQUEST_DATE_FORMAT = "%Y-%m-%dT00:00:00.000"
REQUEST_TIME_FORMAT = "1900-01-01T%H:%M:%S.%f"
_EMPTY_PACKAGE_CODE = "_TT"
_TRANSACTION_NUMBER_SEPARATOR = u";"
_TRANSPORT_CODES = {u"IDAYREGLAS", u"TRAIYRLASA"}
_ALIAS_TRANSLATIONS = {u"PASERVDOMA": u"PASER",
                       u"PASERDOMNI": u"PASER",
                       u"SINTRANPOR": u"PASA",
                       u"PASANINIFA": u"PASA",
                       u"PASADOFER": u"PASA",
                       u"PASFELASOL": u"PASA"}

_CREATE_METHOD = "POST"
_DELETE_METHOD = "DELETE"
_SYNC_URL = "SYNC_RESERVATIONS_COMPENSAR_SERVICE"

DOCUMENT_TYPE_REGISTRY_NAME = u"TipoDocBeneficiario"
DOCUMENT_NUMBER_REGISTRY_NAME = u"IdentificacionBeneficiario"
WORKER_DOCUMENT_NUMBER_REGISTRY_NAME = u"IdentificadorTrabajador"
FULL_NAME_REGISTRY_NAME = u"NombreBeneficiario"
BIRTHDATE_REGISTRY_NAME = u"FechaNacimientoBenef"
CATEGORY_REGISTRY_NAME = u"CategoriaVenta"
PRODUCT_REGISTRY_NAME = u"AliasServicio"
PAYMENT_REGISTRY_NAME = u"ValorTotalUsuario"
INITIAL_DATE_REGISTRY_NAME = u"FechaInicioServicio"
FINAL_DATE_REGISTRY_NAME = u"FechaFinServicio"
TRANSACTION_NUMBER_REGISTRY_NAME = u"OrdenServicio"
PURCHASE_DATE_REGISTRY_NAME = u"FechaRegistro"
PURCHASE_TIME_REGISTRY_NAME = u"HoraRegistro"
PURCHASES_REGISTRY_NAME = u"Pasadia"


def query_compensars_external_reservations_service(id_client, document_number):
    try:
        initial_all = time_measure.time()
        current_date = validate_date(None, "Current date", allow_none=True)
        current_date_formatted = current_date.strftime(REQUEST_DATE_FORMAT)

        # noinspection PyArgumentList
        body_headers = collections.OrderedDict(
            [
                ("SecurityHeader", collections.OrderedDict(
                    [
                        ("User", "Silvernest"),
                        ("Password", get_pass_compensar_orbita())
                    ])),
                ("System", collections.OrderedDict(
                    [
                        ("InputSystem", "Portal"),
                        ("ApplicationID", "SWEX999"),
                        ("TransactionID", str(id_client) + ";" + current_date_formatted + ";" + document_number),
                        ("IPAddress", "192.168.0.1")
                    ]))
            ])
        # noinspection PyArgumentList
        # Se debe usar un diccionario ordenado porque el servicio de compensar espera los datos ordenados por alguna razón
        body_body = collections.OrderedDict(
            [
                ("FechaServicio", current_date_formatted),
                ("NumDocBeneficiario", document_number)
            ])

        # noinspection PyArgumentList
        body_request = collections.OrderedDict(
            [
                ("PassDay_Request", collections.OrderedDict(
                    [
                        ("Header", body_headers),
                        ("Body", body_body)
                    ]))
            ])

        logging.info("Body petición: " + json.dumps(body_request))
        initial_request = time_measure.time()
        logging.info("Tiempo creación petición: " + str(initial_request - initial_all))
        if is_prod():
            url = "https://orbitapasadia.compensar.com/Compensar/Turismo/ConsultaPasadia"
        else:
            url = "https://pruorbitapasadia.compensar.com:13092/Compensar/Turismo/ConsultaPasadia"

        logging.info("Url: " + url)
        result = requests.post(url=url,
                               json=body_request)

        final_request = time_measure.time()
        logging.info("Tiempo peticion servicio externo: " + str(final_request - initial_request))
        logging.info("Respuesta: " + unicode(result.text))
        json_result = result.json()
        json_passday = json_result.get(u"PassDay_Response", dict())
        header_result = json_passday.get(u"Header", dict())
        is_success = header_result.get(u"RespCode", u"-1") == u"0"
        if is_success:
            json_body = json_passday[u"Body"]
            parse_compensars_data_and_create_reservations(id_client, json_body)
        else:
            raise ValidationError(u"Error querying external service.",
                                  internal_code=ERROR_QUERYING_ORBITA_ERROR_CODE)
        final_all = time_measure.time()
        logging.info("Tiempo procesamiento resultados: " + str(final_all - final_request))
        logging.info("Tiempo total: " + str(final_all - initial_all))
    except APIException:
        raise
    except Exception as e:
        traceback.print_exc()
        raise ValidationError(unicode(e.message), internal_code=ERROR_QUERYING_ORBITA_ERROR_CODE)


def parse_compensars_data_and_create_reservations(id_client, purchases_data):
    # Cuando el servicio no tiene datos retorna un string vacio en lugar de una lista
    if not isinstance(purchases_data, str) and not isinstance(purchases_data, unicode):
        purchases_data = purchases_data[PURCHASES_REGISTRY_NAME]
        # Cuando el servicio solo tiene un registro no retorna una lista, solo el registro en la raíz
        if not isinstance(purchases_data, list):
            purchases_data = [purchases_data]
        purchases_by_person = PurchasesByPersonTable(id_client, purchases_data)
        purchases_by_person.create_or_update_persons()
        purchases_by_person.create_reservations()
        purchases_by_person.create_transport_topoffs()
        purchases_by_person.wait_for_async_data()
    else:
        logging.info(u"Se obtuvo respuesta vacía: " + purchases_data)


class PurchasesByPersonTable:
    def __init__(self, id_client, purchases):
        self.id_client = id_client
        self.purchases_by_document = dict()
        self.reservation = None
        self.empty_package = EmptyPackage(self.id_client)
        self.current_time = validate_datetime(None, AmountTopoff.TOPOFF_TIME_NAME, allow_none=True)
        for purchase in purchases:
            self._add_purchase(purchase)

    def _add_purchase(self, purchase):
        try:
            document_type = check_document_type_and_parse_to_string(int(purchase[DOCUMENT_TYPE_REGISTRY_NAME]))
        except ValueError:
            document_type = NO_DOCUMENT_DOCUMENT_TYPE
        document_number = str(purchase[DOCUMENT_NUMBER_REGISTRY_NAME])
        if document_number not in self.purchases_by_document:
            self.purchases_by_document[document_number] = PurchasesWithPerson(document_number, document_type,
                                                                              self.id_client)
        self.purchases_by_document[document_number].add_purchase(purchase, document_type)

    def create_or_update_persons(self):
        for purchases_with_person in self.purchases_by_document.values():
            purchases_with_person.create_or_update_person()

    def create_reservations(self):
        payment = 0.0
        transaction_numbers = set()
        for purchases_with_person in self.purchases_by_document.values():
            purchases_with_person.check_packages_and_skus_exists()
            transaction_numbers = transaction_numbers.union(purchases_with_person.transaction_numbers)
            payment += purchases_with_person.packages_payment
        transaction_number = _TRANSACTION_NUMBER_SEPARATOR.join(transaction_numbers)
        self.reservation = Reserva.create(self.id_client, payment, transaction_number, None, Usuario.ANONYMOUS)
        found_holder = False
        for index, purchases_with_person in enumerate(self.purchases_by_document.values()):
            if found_holder:
                is_holder = False
            else:
                is_holder = purchases_with_person.is_paying or index == (len(self.purchases_by_document.values()) - 1)
            purchases_with_person.create_person_reservations(self.reservation, is_holder,
                                                             self.empty_package.empty_package)
            if is_holder:
                found_holder = True

    def create_transport_topoffs(self):
        for purchases_with_person in self.purchases_by_document.values():
            purchases_with_person.create_transport_topoffs(self.current_time)

    def wait_for_async_data(self):
        for purchases_with_person in self.purchases_by_document.values():
            purchases_with_person.wait_for_async_data()


class PurchasesWithPerson:
    def __init__(self, document_number, document_type, id_client):
        self.id_client = id_client
        self.document_type = document_type
        self.document_number = document_number
        self._person_async = Persona.get_person_by_document_async(self.document_type, self.document_number)
        self._person = None
        self.person_data = None
        self.packages_purchases = []
        self.sku_purchases = []
        self.is_paying = False

    def add_purchase(self, purchase, document_type):
        external_code = purchase[PRODUCT_REGISTRY_NAME]
        is_package = external_code in _ALIAS_TRANSLATIONS
        if self.document_type == NO_DOCUMENT_DOCUMENT_TYPE or (document_type != NO_DOCUMENT_DOCUMENT_TYPE and not is_package):
            self.document_type = document_type
            self._person_async = Persona.get_person_by_document_async(self.document_type, self.document_number)
            self.person_data = purchase
        if self.person_data is None:
            self.person_data = purchase
        if is_package:
            self.packages_purchases.append(RegistryWithPackage(purchase, external_code))
        else:
            self.sku_purchases.append(RegistryWithSKU(purchase, external_code))

    def create_or_update_person(self):
        person = self._person_async.get_result()
        birthdate = datetime.strptime(self.person_data[BIRTHDATE_REGISTRY_NAME], RESPONSE_DATE_FORMAT).date()
        category = check_category_string_and_parse_to_category(self.person_data[CATEGORY_REGISTRY_NAME])
        if person is None:
            first_name = ""
            last_name = self.person_data[FULL_NAME_REGISTRY_NAME]
            self.is_paying = self.person_data[WORKER_DOCUMENT_NUMBER_REGISTRY_NAME] == self.person_data[DOCUMENT_NUMBER_REGISTRY_NAME]
            if self.is_paying:
                affiliation = COTIZANTE_AFFILIATION
            else:
                affiliation = BENEFICIARIO_AFFILIATION
            self._person = Persona.create_without_put(self.id_client, first_name, last_name, self.document_type,
                                                      self.document_number, None, None, birthdate, category, affiliation,
                                                      None, None, None, None)
        else:
            person.update_without_put(person.nombre, person.apellido, person.tipoDocumento,
                                      person.numeroDocumento, person.correo, person.sexo, birthdate,
                                      category, person.afiliacion, person.nacionalidad, person.profesion,
                                      person.ciudad, person.empresa)
            self._person = person
        self._person_async = self._person.put_async()

    @property
    def packages_payment(self):
        payment = 0
        for package_purchase in self.packages_purchases:
            payment += package_purchase.payment
        return payment

    @property
    def transaction_numbers(self):
        transaction_numbers = set()
        for package_purchase in self.packages_purchases:
            transaction_numbers.add(package_purchase.transaction_number)
        return transaction_numbers

    def check_packages_and_skus_exists(self):
        for package_purchase in self.packages_purchases:
            if package_purchase.package is None:
                raise EntityDoesNotExists(u"Package[{0}]".format(package_purchase.purchase_data[PRODUCT_REGISTRY_NAME]),
                                          internal_code=PACKAGE_DOES_NOT_EXISTS_ERROR_CODE)
        for sku_purchase in self.sku_purchases:
            if sku_purchase.sku is None:
                raise EntityDoesNotExists(u"SKU[{0}]".format(sku_purchase.purchase_data[PRODUCT_REGISTRY_NAME]),
                                          internal_code=SKU_DOES_NOT_EXISTS_ERROR_CODE)

    def create_person_reservations(self, reservation, is_holder, empty_package):
        self._person_async.get_result()
        if len(self.packages_purchases) == 0:
            initial_date = None
            final_date = None
            purchase_time = None
            for sku_purchase in self.sku_purchases:
                current_initial_date = sku_purchase.initial_date
                current_final_date = sku_purchase.final_date
                if initial_date is None or current_initial_date < initial_date:
                    initial_date = current_initial_date
                if final_date is None or current_final_date > final_date:
                    final_date = current_final_date
                if purchase_time is None:
                    purchase_time = sku_purchase.purchase_time
            self.packages_purchases.append(RegistryWithEmptyPackage(empty_package, initial_date, final_date,
                                                                    purchase_time))
        for package_purchase in self.packages_purchases:
            package_purchase.create_person_reservation_with_reservation(reservation, is_holder, self._person,
                                                                        self.id_client)

    def create_transport_topoffs(self, current_time):
        for package_purchase in self.packages_purchases:
            package_purchase.wait_for_async_data()
        person_reservation = self.packages_purchases[0].person_reservation
        for sku_purchase in self.sku_purchases:
            sku_purchase.create_amount_topoff_with_person_reservation(person_reservation, self.id_client, current_time)

    def wait_for_async_data(self):
        for sku_purchase in self.sku_purchases:
            sku_purchase.wait_for_async_data()


class RegistryWithEmptyPackage:
    def __init__(self, empty_package, initial_date, final_date, purchase_time):
        self.package = empty_package
        self.initial_date = initial_date
        self.final_date = final_date
        self.purchase_time = purchase_time
        self.person_reservation = None
        self._person_reservation_async = None

    def create_person_reservation_with_reservation(self, reservation, is_holder, person, id_client):
        id_person = person.key.id()
        tax_rate = 0
        payment = 0
        self.person_reservation = ReservaPersona.create_without_put(id_client, reservation.key.id(), self.package,
                                                                    id_person, is_holder, payment, tax_rate,
                                                                    self.initial_date, self.final_date,
                                                                    self.purchase_time, Usuario.ANONYMOUS)
        self._person_reservation_async = self.person_reservation.put_async()

    def wait_for_async_data(self):
        self._person_reservation_async.get_result()


class RegistryWithPackage:
    def __init__(self, purchase_data, external_code):
        self.person_reservation = None
        self._person_reservation_async = None
        self.purchase_data = purchase_data
        external_code = _ALIAS_TRANSLATIONS.get(external_code, external_code)
        self._package_async = Paquete.get_by_external_code_async(external_code)
        self.payment = float(purchase_data[PAYMENT_REGISTRY_NAME])
        self.transaction_number = purchase_data[TRANSACTION_NUMBER_REGISTRY_NAME]

    @property
    def package(self):
        return self._package_async.get_result()

    def create_person_reservation_with_reservation(self, reservation, is_holder, person, id_client):
        id_person = person.key.id()
        tax_rate = 0
        initial_date = datetime.combine(datetime.strptime(self.purchase_data[INITIAL_DATE_REGISTRY_NAME],
                                                          RESPONSE_DATE_FORMAT).date(),
                                        time.min)
        final_date = datetime.combine(datetime.strptime(self.purchase_data[FINAL_DATE_REGISTRY_NAME],
                                                        RESPONSE_DATE_FORMAT).date(),
                                      time.max)
        purchase_time = datetime.combine(datetime.strptime(self.purchase_data[PURCHASE_DATE_REGISTRY_NAME],
                                                           RESPONSE_DATE_FORMAT).date(),
                                         datetime.strptime(self.purchase_data[PURCHASE_TIME_REGISTRY_NAME],
                                                           RESPONSE_TIME_FORMAT).time())
        self.person_reservation = ReservaPersona.create_without_put(id_client, reservation.key.id(), self.package, id_person,
                                                                    is_holder, self.payment, tax_rate, initial_date,
                                                                    final_date, purchase_time, Usuario.ANONYMOUS)
        self._person_reservation_async = self.person_reservation.put_async()

    def wait_for_async_data(self):
        self._person_reservation_async.get_result()


class RegistryWithSKU:
    def __init__(self, purchase_data, external_code):
        self.amount_topoff = None
        self._amount_topoff_async = None
        self.purchase_data = purchase_data
        self._sku_async = SKU.get_by_external_code_async(external_code)

    @property
    def initial_date(self):
        return datetime.combine(datetime.strptime(self.purchase_data[INITIAL_DATE_REGISTRY_NAME],
                                                  RESPONSE_DATE_FORMAT).date(),
                                time.min)

    @property
    def final_date(self):
        return datetime.combine(datetime.strptime(self.purchase_data[FINAL_DATE_REGISTRY_NAME],
                                                  RESPONSE_DATE_FORMAT).date(),
                                time.max)

    @property
    def purchase_time(self):
        return datetime.combine(datetime.strptime(self.purchase_data[PURCHASE_DATE_REGISTRY_NAME],
                                                  RESPONSE_DATE_FORMAT).date(),
                                datetime.strptime(self.purchase_data[PURCHASE_TIME_REGISTRY_NAME],
                                                  RESPONSE_TIME_FORMAT).time())

    @property
    def sku(self):
        return self._sku_async.get_result()

    def create_amount_topoff_with_person_reservation(self, person_reservation, id_client, topoff_time):
        amount = 1
        transaction_number = self.purchase_data[TRANSACTION_NUMBER_REGISTRY_NAME]
        self.amount_topoff = AmountTopoff.create_without_put(id_client, person_reservation.idReserva,
                                                             person_reservation.key.id(), amount, self.sku.key.id(),
                                                             None, transaction_number, topoff_time, Usuario.ANONYMOUS)
        self._amount_topoff_async = self.amount_topoff.put_async()

    def wait_for_async_data(self):
        self._amount_topoff_async.get_result()


class EmptyPackage:
    def __init__(self, id_client):
        self.id_client = id_client
        self._empty_package_async = Paquete.get_by_external_code_async(_EMPTY_PACKAGE_CODE)
        self._empty_package = None

    @property
    def empty_package(self):
        if self._empty_package is None:
            self._empty_package = self._empty_package_async.get_result()
            if self._empty_package is None:
                today = validate_datetime(None, Paquete.VALID_FROM_NAME, allow_none=True)
                days_in_year = 365
                self._empty_package = Paquete.create(self.id_client, "Paquete vacío", 0.0, 15,
                                                     "Paquete vacío. No incluye nada", True,
                                                     today - timedelta(days=10 * days_in_year),
                                                     today + timedelta(days=100 * days_in_year),
                                                     None, _EMPTY_PACKAGE_CODE, False)
        return self._empty_package


CATEGORIES_BY_STRING = {u"Afiliado Cat. A": A_CATEGORY,
                        u"Afiliado Cat. B": B_CATEGORY,
                        u"Afiliado Cat. C": C_CATEGORY,
                        u"Afiliado Cat. D": D_CATEGORY}


def check_category_string_and_parse_to_category(category):
    if category is None:
        return None
    return CATEGORIES_BY_STRING.get(category, None)
