# -*- coding: utf-8 -*
import unittest


from CJM.entidades.paquetes.Paquete import Paquete
from CJM.entidades.persons.Persona import Persona
from CJM.entidades.reservas.AccessTopoff import AccessTopoff
from CJM.entidades.reservas.AmountTopoff import AmountTopoff
from CJM.entidades.reservas.MoneyTopoff import MoneyTopoff
from CJM.entidades.reservas.Reserva import Reserva
from CJM.entidades.reservas.ReservaPersona import ReservaPersona
from CJM.entidades.skus.SKU import SKU
from CJM.services.reservas.compensar_reservations_service.queryReservationsFromCompensarService import \
    parse_compensars_data_and_create_reservations
from commons.entidades.users.Usuario import Usuario
from commons.excepciones.apiexceptions import EntityDoesNotExists
from tests.FlaskClientBaseTestCase import FlaskClientBaseTestCase
from datetime import datetime, time

from tests.errorDefinitions.errorConstants import PACKAGE_DOES_NOT_EXISTS_CODE, SKU_DOES_NOT_EXISTS_CODE
from tests.testCommons.testClients.testClientViewTestCase import create_test_client


class IntegracionOrbitaTestCase(FlaskClientBaseTestCase):
    CC_DOCUMENT_TYPE = u"CC"
    NIT_DOCUMENT_TYPE = u"NIT"
    TI_DOCUMENT_TYPE = u"TI"
    CE_DOCUMENT_TYPE = u"CE"
    PASSPORT_DOCUMENT_TYPE = u"PA"
    REGISTRO_CIVIL_DOCUMENT_TYPE = u"RC"
    NUIP_DOCUMENT_TYPE = u"NUIP"
    CARNE_DIPLOMATICO_DOCUMENT_TYPE = u"CD"
    NO_DOCUMENT_DOCUMENT_TYPE = u"NO_DOCUMENT"

    VALID_DOCUMENTS = {CC_DOCUMENT_TYPE,
                       NIT_DOCUMENT_TYPE,
                       TI_DOCUMENT_TYPE,
                       CE_DOCUMENT_TYPE,
                       PASSPORT_DOCUMENT_TYPE,
                       REGISTRO_CIVIL_DOCUMENT_TYPE,
                       NUIP_DOCUMENT_TYPE,
                       CARNE_DIPLOMATICO_DOCUMENT_TYPE,
                       NO_DOCUMENT_DOCUMENT_TYPE}

    A_CATEGORY = u"A"
    B_CATEGORY = u"B"
    C_CATEGORY = u"C"
    D_CATEGORY = u"D"

    GENDER_MALE = u"male"
    GENDER_FEMALE = u"female"

    COTIZANTE_AFFILIATION = u"COTIZANTE"
    BENEFICIARIO_AFFILIATION = u"BENEFICIARIO"

    DOCUMENT_TYPE_REGISTRY_NAME = U"TipoDocBeneficiario"
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

    TRANSACTION_NUMBER_SEPARATOR = u";"

    PREFIX_CATEGORY = u"Afiliado Cat. "
    RESPONSE_DATE_FORMAT = u"%Y/%m/%d"
    RESPONSE_TIME_FORMAT = u"%H:%M:%S"

    DOCUMENT_TYPES_BY_ID = {1: CC_DOCUMENT_TYPE,
                            2: NIT_DOCUMENT_TYPE,
                            3: TI_DOCUMENT_TYPE,
                            4: CE_DOCUMENT_TYPE,
                            5: PASSPORT_DOCUMENT_TYPE,
                            7: REGISTRO_CIVIL_DOCUMENT_TYPE,
                            8: NUIP_DOCUMENT_TYPE,
                            10: CARNE_DIPLOMATICO_DOCUMENT_TYPE}
    EXTERNAL_PACKAGES_CODES_TRANSLATIONS = {u"PASERVDOMA": u"PASER",
                                            u"PASERDOMNI": u"PASER",
                                            u"SINTRANPOR": u"PASA",
                                            u"PASANINIFA": u"PASA",
                                            u"PASADOFER": u"PASA",
                                            u"PASFELASOL": u"PASA"}
    EXTERNAL_PACKAGES_CODES = set(EXTERNAL_PACKAGES_CODES_TRANSLATIONS.values())
    TRANSPORT_EXTERNAL_CODES = {u"IDAYREGLAS", u"TRAIYRLASA"}
    EMPTY_PACKAGE_CODE = u"_TT"

    IDS_BY_DOCUMENT_TYPE = {document_type: id_document
                            for id_document, document_type in DOCUMENT_TYPES_BY_ID.iteritems()}
    TEST_ID_CLIENT = 1

    def setUp(self):
        super(IntegracionOrbitaTestCase, self).setUp()
        self.packages_by_external_code = dict()
        for index, external_package_code in enumerate(self.EXTERNAL_PACKAGES_CODES):
            self.packages_by_external_code[external_package_code] = Paquete.create(self.TEST_ID_CLIENT,
                                                                                   external_package_code, index * 100,
                                                                                   index * 1.5, external_package_code,
                                                                                   True, datetime(2000, 01, 01),
                                                                                   datetime(3000, 01, 01), 1,
                                                                                   external_package_code, True)
        self.skus_by_external_code = dict()
        for index, external_sku_code in enumerate(self.TRANSPORT_EXTERNAL_CODES):
            self.skus_by_external_code[external_sku_code] = SKU.create(external_sku_code, external_sku_code,
                                                                       100 * index, external_sku_code, None,
                                                                       0, external_sku_code)
        create_test_client(self)

    def tearDown(self):
        super(IntegracionOrbitaTestCase, self).tearDown()

    def test_empty_response_doesnt_create_any_reservation_or_topoffs(self):
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT, "")
        self.assertEqual(0, len(Reserva.list()))
        self.assertEqual(0, len(ReservaPersona.list()))
        self.assertEqual(0, len(AmountTopoff.list()))
        self.assertEqual(0, len(MoneyTopoff.list()))
        self.assertEqual(0, len(AccessTopoff.list()))

    def test_response_with_single_registry_and_valid_document_creates_person_if_person_with_given_document_doesnt_exist(self):
        test_document_type = self.CC_DOCUMENT_TYPE
        test_document_number = "123"
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, test_document_type, test_document_number, test_full_name, test_birthdate,
                                test_category, test_worker_document_number)

    def test_response_with_single_registry_and_empty_document_type_creates_person_with_no_document_type_if_it_doesnt_exists(self):
        test_document_type = ""
        test_document_number = "123"
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, self.NO_DOCUMENT_DOCUMENT_TYPE, test_document_number, test_full_name,
                                test_birthdate, test_category, test_worker_document_number)

    def test_response_with_single_registry_and_invalid_document_type_creates_person_with_no_document_type_if_it_doesnt_exists(self):
        test_document_type = "INVALID"
        test_document_number = "123"
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, self.NO_DOCUMENT_DOCUMENT_TYPE, test_document_number, test_full_name,
                                test_birthdate, test_category, test_worker_document_number)

    def test_response_with_single_registry_valid_document_and_different_worker_creates_person_if_person_with_given_document_doesnt_exist(self):
        test_document_type = self.CC_DOCUMENT_TYPE
        test_document_number = "123"
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = "1234"
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, test_document_type, test_document_number, test_full_name, test_birthdate,
                                test_category, test_worker_document_number)

    def test_response_with_multiple_registries_and_valid_document_creates_persons_if_they_dont_exist(self):
        test_document_type1 = self.CC_DOCUMENT_TYPE
        test_document_number1 = "123"
        test_full_name1 = "Test Full Name"
        test_birthdate_str1 = "1990/02/03"
        test_birthdate1 = datetime.strptime(test_birthdate_str1, self.RESPONSE_DATE_FORMAT).date()
        test_category1 = self.C_CATEGORY
        test_worker_document_number1 = test_document_number1
        test_purchase1 = self._create_sample_registry_for_person(test_document_type1, test_document_number1,
                                                                 test_full_name1, test_birthdate_str1, test_category1,
                                                                 test_worker_document_number1)
        test_document_type2 = self.TI_DOCUMENT_TYPE
        test_document_number2 = "1234"
        test_full_name2 = "Another Full Name"
        test_birthdate_str2 = "2005/02/03"
        test_birthdate2 = datetime.strptime(test_birthdate_str2, self.RESPONSE_DATE_FORMAT).date()
        test_category2 = self.D_CATEGORY
        test_worker_document_number2 = test_document_number1
        test_purchase2 = self._create_sample_registry_for_person(test_document_type2, test_document_number2,
                                                                 test_full_name2, test_birthdate_str2, test_category2,
                                                                 test_worker_document_number2)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: [test_purchase1, test_purchase2]})
        persons_list = Persona.list()
        self.assertEqual(2, len(persons_list))

        person1 = self._get_person_by_document(persons_list, test_document_type1, test_document_number1)
        self._check_person_data(person1, test_document_type1, test_document_number1, test_full_name1, test_birthdate1,
                                test_category1, test_worker_document_number1)

        person2 = self._get_person_by_document(persons_list, test_document_type2, test_document_number2)
        self._check_person_data(person2, test_document_type2, test_document_number2, test_full_name2, test_birthdate2,
                                test_category2, test_worker_document_number2)

    def test_response_with_single_registry_and_valid_document_updates_person_if_person_with_given_document_exist(self):
        test_document_type = self.CC_DOCUMENT_TYPE
        test_document_number = "123"
        original_person = Persona.create(self.TEST_ID_CLIENT, "Test Full", "Name", test_document_type,
                                         test_document_number, "tes@mail.com", self.GENDER_FEMALE,
                                         datetime(1980, 06, 20).date(), self.A_CATEGORY,
                                         self.BENEFICIARIO_AFFILIATION, "Colombiano", "Profesion", "Ciudad", "Empresa")
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, test_document_type, test_document_number, test_full_name, test_birthdate,
                                test_category, test_worker_document_number, original_person)

    def test_response_with_single_registry_and_invalid_document_type_updates_person_with_no_document_type_if_it_exists(self):
        test_document_type = "INVALID"
        test_document_number = "123"
        original_person = Persona.create(self.TEST_ID_CLIENT, "Test Full", "Name", self.NO_DOCUMENT_DOCUMENT_TYPE,
                                         test_document_number, "tes@mail.com", self.GENDER_FEMALE,
                                         datetime(1980, 06, 20).date(), self.A_CATEGORY,
                                         self.BENEFICIARIO_AFFILIATION, "Colombiano", "Profesion", "Ciudad", "Empresa")
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, self.NO_DOCUMENT_DOCUMENT_TYPE, test_document_number, test_full_name,
                                test_birthdate, test_category, test_worker_document_number, original_person)

    def test_response_with_single_registry_and_empty_document_type_updates_person_with_no_document_type_if_it_exists(self):
        test_document_type = ""
        test_document_number = "123"
        original_person = Persona.create(self.TEST_ID_CLIENT, "Test Full", "Name", self.NO_DOCUMENT_DOCUMENT_TYPE,
                                         test_document_number, "tes@mail.com", self.GENDER_FEMALE,
                                         datetime(1980, 06, 20).date(), self.A_CATEGORY,
                                         self.BENEFICIARIO_AFFILIATION, "Colombiano", "Profesion", "Ciudad", "Empresa")
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                test_full_name, test_birthdate_str, test_category,
                                                                test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchase})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, self.NO_DOCUMENT_DOCUMENT_TYPE, test_document_number, test_full_name,
                                test_birthdate, test_category, test_worker_document_number, original_person)

    def test_response_with_multiple_registries_and_valid_document_creates_non_existent_persons_and_update_those_who_already_exists(self):
        test_document_type1 = self.CC_DOCUMENT_TYPE
        test_document_number1 = "123"
        original_person = Persona.create(self.TEST_ID_CLIENT, "Test Full", "Name", test_document_type1,
                                         test_document_number1, "tes@mail.com", self.GENDER_FEMALE,
                                         datetime(1980, 06, 20).date(), self.A_CATEGORY,
                                         self.BENEFICIARIO_AFFILIATION, "Colombiano", "Profesion", "Ciudad", "Empresa")
        test_full_name1 = "Test Full Name"
        test_birthdate_str1 = "1990/02/03"
        test_birthdate1 = datetime.strptime(test_birthdate_str1, self.RESPONSE_DATE_FORMAT).date()
        test_category1 = self.C_CATEGORY
        test_worker_document_number1 = test_document_number1
        test_purchase1 = self._create_sample_registry_for_person(test_document_type1, test_document_number1,
                                                                 test_full_name1, test_birthdate_str1, test_category1,
                                                                 test_worker_document_number1)
        test_document_type2 = self.TI_DOCUMENT_TYPE
        test_document_number2 = "1234"
        test_full_name2 = "Another Full Name"
        test_birthdate_str2 = "2005/02/03"
        test_birthdate2 = datetime.strptime(test_birthdate_str2, self.RESPONSE_DATE_FORMAT).date()
        test_category2 = self.D_CATEGORY
        test_worker_document_number2 = test_document_number1
        test_purchase2 = self._create_sample_registry_for_person(test_document_type2, test_document_number2,
                                                                 test_full_name2, test_birthdate_str2, test_category2,
                                                                 test_worker_document_number2)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: [test_purchase1, test_purchase2]})
        persons_list = Persona.list()
        self.assertEqual(2, len(persons_list))

        person1 = self._get_person_by_document(persons_list, test_document_type1, test_document_number1)
        self._check_person_data(person1, test_document_type1, test_document_number1, test_full_name1, test_birthdate1,
                                test_category1, test_worker_document_number1, original_person)

        person2 = self._get_person_by_document(persons_list, test_document_type2, test_document_number2)
        self._check_person_data(person2, test_document_type2, test_document_number2, test_full_name2, test_birthdate2,
                                test_category2, test_worker_document_number2)

    def test_response_with_multiple_registries_for_the_same_person_creates_a_single_person_if_it_doesnt_exist(self):
        test_document_type = self.CC_DOCUMENT_TYPE
        test_document_number = "123"
        original_person = Persona.create(self.TEST_ID_CLIENT, "Test Full", "Name", test_document_type,
                                         test_document_number, "tes@mail.com", self.GENDER_FEMALE,
                                         datetime(1980, 06, 20).date(), self.A_CATEGORY,
                                         self.BENEFICIARIO_AFFILIATION, "Colombiano", "Profesion", "Ciudad", "Empresa")
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase1 = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                 test_full_name, test_birthdate_str, test_category,
                                                                 test_worker_document_number)
        test_purchase2 = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                 test_full_name, test_birthdate_str, test_category,
                                                                 test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: [test_purchase1, test_purchase2]})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, test_document_type, test_document_number, test_full_name, test_birthdate,
                                test_category, test_worker_document_number, original_person)

    def test_response_with_multiple_registries_for_the_same_person_updates_person_if_it_exist(self):
        test_document_type = self.CC_DOCUMENT_TYPE
        test_document_number = "123"
        test_full_name = "Test Full Name"
        test_birthdate_str = "1990/02/03"
        test_birthdate = datetime.strptime(test_birthdate_str, self.RESPONSE_DATE_FORMAT).date()
        test_category = self.C_CATEGORY
        test_worker_document_number = test_document_number
        test_purchase1 = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                 test_full_name, test_birthdate_str, test_category,
                                                                 test_worker_document_number)
        test_purchase2 = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                                 test_full_name, test_birthdate_str, test_category,
                                                                 test_worker_document_number)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: [test_purchase1, test_purchase2]})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        person = persons_list[0]
        self._check_person_data(person, test_document_type, test_document_number, test_full_name, test_birthdate,
                                test_category, test_worker_document_number)

    def test_response_with_multiple_registries_with_different_persons_and_valid_packages_creates_single_reservation_with_packages(self):
        test_transaction_number = "456"
        purchases_by_document, total_payment, test_purchases = self._create_purchases_registries_for_every_package(test_transaction_number)
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()
        self.assertEqual(len(test_purchases), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_purchases), len(person_reservations_list))
        expected_number_of_holders = 1

        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(0, len(amount_topoffs_list))
        self._check_persons_reservations_for_packages_data(persons_list, person_reservations_list,
                                                           purchases_by_document, expected_number_of_holders)

    def test_response_with_multiple_registries_with_different_persons_and_valid_packages_without_paying_user_has_one_holder(self):
        test_transaction_number = "456"
        purchases_by_document, total_payment, test_purchases = self._create_purchases_registries_for_every_package(test_transaction_number,
                                                                                                                   worker_document_number="123")
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()
        self.assertEqual(len(test_purchases), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_purchases), len(person_reservations_list))
        expected_number_of_holders = 1

        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(0, len(amount_topoffs_list))
        self._check_persons_reservations_for_packages_data(persons_list, person_reservations_list,
                                                           purchases_by_document, expected_number_of_holders)

    def test_response_with_multiple_registries_with_person_with_multiple_packages_creates_single_reservation_with_multiple_person_reservations_to_the_same_person(self):
        test_transaction_number = "456"
        purchases_by_document, total_payment, test_purchases = self._create_purchases_registries_for_every_package(test_transaction_number,
                                                                                                                   use_same_person_in_all_purchases=True)

        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()
        self.assertEqual(1, len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_purchases), len(person_reservations_list))
        expected_number_of_holders = len(test_purchases)

        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(0, len(amount_topoffs_list))
        self._check_persons_reservations_for_packages_data(persons_list, person_reservations_list,
                                                           purchases_by_document, expected_number_of_holders)

    def test_response_with_multiple_registries_with_multiple_persons_with_multiple_packages_creates_single_reservation_with_multiple_person_reservations_to_each_person(self):
        test_transaction_number1 = "456"
        test_transaction_number2 = "789"
        purchases_by_document1, total_payment1, test_purchases1 = self._create_purchases_registries_for_every_package(test_transaction_number1,
                                                                                                                      worker_document_number="123",
                                                                                                                      use_same_person_in_all_purchases=True)
        purchases_by_document2, total_payment2, test_purchases2 = self._create_purchases_registries_for_every_package(test_transaction_number2,
                                                                                                                      worker_document_number="1234",
                                                                                                                      use_same_person_in_all_purchases=True)
        total_payment = total_payment1 + total_payment2
        test_purchases = test_purchases1 + test_purchases2
        purchases_by_document = self._merge_purchases_by_document([purchases_by_document1, purchases_by_document2])
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()
        self.assertEqual(2, len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual({test_transaction_number1, test_transaction_number2},
                         set(reservation.numeroTransaccion.split(self.TRANSACTION_NUMBER_SEPARATOR)))
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_purchases), len(person_reservations_list))
        expected_number_of_holders = len(test_purchases1)

        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(0, len(amount_topoffs_list))
        self._check_persons_reservations_for_packages_data(persons_list, person_reservations_list,
                                                           purchases_by_document, expected_number_of_holders)

    def test_response_with_multiple_registries_with_different_persons_and_only_transports_creates_empty_package(self):
        purchases_by_document, test_purchases = self._create_purchases_registries_for_every_transport()
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        self.assertIsNotNone(Paquete.get_by_external_code(self.EMPTY_PACKAGE_CODE))

    def test_response_with_multiple_registries_with_different_persons_and_only_transports_creates_single_reservation_with_packages(self):
        purchases_by_document, test_purchases = self._create_purchases_registries_for_every_transport()
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()

        self.assertEqual(len(test_purchases), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(0, reservation.valorPagado)
        self.assertEqual("", reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_purchases), len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_purchases), len(amount_topoffs_list))

        empty_package = Paquete.get_by_external_code(self.EMPTY_PACKAGE_CODE)
        self._check_person_reservations_for_persons_without_package(empty_package, list(persons_list),
                                                                    person_reservations_list, purchases_by_document)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, purchases_by_document)

    def test_response_with_multiple_registries_with_different_persons_and_only_transports_without_paying_user_has_one_holder(self):
        purchases_by_document, test_purchases = self._create_purchases_registries_for_every_transport(worker_document_number="123")
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()

        self.assertEqual(len(test_purchases), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(0, reservation.valorPagado)
        self.assertEqual("", reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_purchases), len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_purchases), len(amount_topoffs_list))

        empty_package = Paquete.get_by_external_code(self.EMPTY_PACKAGE_CODE)
        self._check_person_reservations_for_persons_without_package(empty_package, list(persons_list),
                                                                    person_reservations_list, purchases_by_document)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, purchases_by_document)

    def test_response_with_multiple_registries_with_person_with_multiple_transports_creates_single_reservation_with_single_person_reservation(self):
        purchases_by_document, test_purchases = self._create_purchases_registries_for_every_transport(use_same_person_in_all_purchases=True)
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()

        self.assertEqual(1, len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(0, reservation.valorPagado)
        self.assertEqual("", reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(1, len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_purchases), len(amount_topoffs_list))

        empty_package = Paquete.get_by_external_code(self.EMPTY_PACKAGE_CODE)
        self._check_person_reservations_for_persons_without_package(empty_package, list(persons_list),
                                                                    person_reservations_list, purchases_by_document)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, purchases_by_document)

    def test_response_with_multiple_registries_with_multiple_persons_with_multiple_transports_creates_single_reservation_with_single_person_reservations_to_each_person(self):
        purchases_by_document1, test_purchases1 = self._create_purchases_registries_for_every_transport(worker_document_number="123",
                                                                                                        use_same_person_in_all_purchases=True)
        purchases_by_document2, test_purchases2 = self._create_purchases_registries_for_every_transport(worker_document_number="1234",
                                                                                                        use_same_person_in_all_purchases=True)
        test_purchases = test_purchases1 + test_purchases2
        purchases_by_document = self._merge_purchases_by_document([purchases_by_document1, purchases_by_document2])
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_purchases})
        persons_list = Persona.list()

        self.assertEqual(2, len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(0, reservation.valorPagado)
        self.assertEqual("", reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(2, len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_purchases), len(amount_topoffs_list))

        empty_package = Paquete.get_by_external_code(self.EMPTY_PACKAGE_CODE)
        self._check_person_reservations_for_persons_without_package(empty_package, list(persons_list),
                                                                    person_reservations_list, purchases_by_document)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, purchases_by_document)

    def test_response_with_person_with_multiple_persons_with_transports_and_single_package_creates_single_reservation_with_single_person_reservations_to_each_person(self):
        test_transaction_number = "456"
        packages_by_document, total_payment, test_packages = self._create_purchases_registries_for_every_package(test_transaction_number,
                                                                                                                 worker_document_number="123")
        transports_by_document, test_transports = self._create_purchases_registries_for_every_transport(worker_document_number="123")
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_packages + test_transports})
        persons_list = Persona.list()

        self.assertEqual(len(test_packages), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_packages), len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_transports), len(amount_topoffs_list))

        expected_number_of_holders = 1
        self._check_persons_reservations_for_packages_data(list(persons_list), list(person_reservations_list),
                                                           packages_by_document, expected_number_of_holders)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, transports_by_document,
                                                             all_have_topoffs=False)

    def test_response_with_person_with_multiple_persons_and_transports_with_different_valid_document_type_creates_person_reservations_for_each_package_mixes_by_document_number(self):
        test_transaction_number = "456"
        packages_by_document, total_payment, test_packages = self._create_purchases_registries_for_every_package(test_transaction_number,
                                                                                                                 worker_document_number="123",
                                                                                                                 test_document_type=self.CC_DOCUMENT_TYPE)
        transports_by_document, test_transports = self._create_purchases_registries_for_every_transport(worker_document_number="123",
                                                                                                        test_document_type=self.TI_DOCUMENT_TYPE)
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_packages + test_transports})
        persons_list = Persona.list()

        self.assertEqual(len(test_packages), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_packages), len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_transports), len(amount_topoffs_list))

        expected_number_of_holders = 1

        packages_by_document_corrected = dict()
        for (document_type, document_number), packages in packages_by_document.iteritems():
            if (self.TI_DOCUMENT_TYPE, document_number) in transports_by_document:
                packages_by_document_corrected[(self.TI_DOCUMENT_TYPE, document_number)] = packages
            else:
                packages_by_document_corrected[(document_type, document_number)] = packages
        self._check_persons_reservations_for_packages_data(list(persons_list), list(person_reservations_list),
                                                           packages_by_document_corrected, expected_number_of_holders)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, transports_by_document,
                                                             all_have_topoffs=False)

    def test_response_with_person_with_multiple_persons_and_transports_with_different_and_transports_invalid_document_type_for_transports_creates_person_reservations_for_each_package_mixes_by_document_number(self):
        test_transaction_number = "456"
        packages_by_document, total_payment, test_packages = self._create_purchases_registries_for_every_package(test_transaction_number,
                                                                                                                 worker_document_number="123",
                                                                                                                 test_document_type=self.CC_DOCUMENT_TYPE)
        transports_by_document, test_transports = self._create_purchases_registries_for_every_transport(worker_document_number="123",
                                                                                                        test_document_type="")
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_packages + test_transports})
        persons_list = Persona.list()

        self.assertEqual(len(test_packages), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_packages), len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_transports), len(amount_topoffs_list))

        expected_number_of_holders = 1

        self._check_persons_reservations_for_packages_data(list(persons_list), list(person_reservations_list),
                                                           packages_by_document, expected_number_of_holders)
        transports_by_document_corrected = dict()
        for (_, document_number), transports in transports_by_document.iteritems():
            transports_by_document_corrected[(self.CC_DOCUMENT_TYPE, document_number)] = transports
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, transports_by_document_corrected,
                                                             all_have_topoffs=False)

    def test_response_with_person_with_multiple_persons_and_transports_with_different_and_transports_invalid_document_type_for_packages_creates_person_reservations_for_each_package_mixes_by_document_number(self):
        test_transaction_number = "456"
        packages_by_document, total_payment, test_packages = self._create_purchases_registries_for_every_package(test_transaction_number,
                                                                                                                 worker_document_number="123",
                                                                                                                 test_document_type="")
        transports_by_document, test_transports = self._create_purchases_registries_for_every_transport(worker_document_number="123",
                                                                                                        test_document_type=self.CC_DOCUMENT_TYPE)
        parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                      {self.PURCHASES_REGISTRY_NAME: test_packages + test_transports})
        persons_list = Persona.list()

        self.assertEqual(len(test_packages), len(persons_list))

        reservations_list = Reserva.list()
        self.assertEqual(1, len(reservations_list))
        reservation = reservations_list[0]
        self.assertEqual(total_payment, reservation.valorPagado)
        self.assertEqual(test_transaction_number, reservation.numeroTransaccion)
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(len(test_packages), len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(len(test_transports), len(amount_topoffs_list))

        expected_number_of_holders = 1

        packages_by_document_corrected = dict()
        for (document_type, document_number), packages in packages_by_document.iteritems():
            if (self.CC_DOCUMENT_TYPE, document_number) in transports_by_document:
                packages_by_document_corrected[(self.CC_DOCUMENT_TYPE, document_number)] = packages
            else:
                packages_by_document_corrected[(document_type, document_number)] = packages
        self._check_persons_reservations_for_packages_data(list(persons_list), list(person_reservations_list),
                                                           packages_by_document_corrected, expected_number_of_holders)
        self._check_persons_reservations_for_transports_data(persons_list, person_reservations_list,
                                                             amount_topoffs_list, transports_by_document,
                                                             all_have_topoffs=False)

    def test_response_with_multiple_registries_with_different_persons_and_valid_packages_doesnt_creates_reservations_if_any_package_doesnt_exists(self):
        test_transaction_number = "456"
        purchases_by_document, total_payment, test_purchases = self._create_purchases_registries_for_every_package(test_transaction_number)
        Paquete.list()[1].key.delete()
        try:
            parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                          {self.PURCHASES_REGISTRY_NAME: test_purchases})
            self.fail(u"")
        except EntityDoesNotExists as error:
            self.assertEqual(PACKAGE_DOES_NOT_EXISTS_CODE, error.internal_code)

        reservations_list = Reserva.list()
        self.assertEqual(0, len(reservations_list))
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(0, len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(0, len(amount_topoffs_list))

    def test_response_with_multiple_registries_with_different_persons_and_valid_transports_doesnt_creates_reservations_if_any_sku_doesnt_exists(self):
        purchases_by_document, test_purchases = self._create_purchases_registries_for_every_transport()
        SKU.list()[1].key.delete()
        try:
            parse_compensars_data_and_create_reservations(self.TEST_ID_CLIENT,
                                                          {self.PURCHASES_REGISTRY_NAME: test_purchases})
            self.fail(u"")
        except EntityDoesNotExists as error:
            self.assertEqual(SKU_DOES_NOT_EXISTS_CODE, error.internal_code)

        reservations_list = Reserva.list()
        self.assertEqual(0, len(reservations_list))
        person_reservations_list = ReservaPersona.list()
        self.assertEqual(0, len(person_reservations_list))
        amount_topoffs_list = AmountTopoff.list()
        self.assertEqual(0, len(amount_topoffs_list))

    def _check_person_reservations_for_persons_without_package(self, empty_package, persons_list,
                                                               person_reservations_list, purchases_by_document,
                                                               expected_number_of_holders=1):
        number_of_holders = 0
        for person_reservation in person_reservations_list:
            person = self._get_person_by_id(persons_list, person_reservation.idPersona)
            person_key = (person.tipoDocumento, person.numeroDocumento)
            purchases_data = purchases_by_document[person_key]
            purchase_data = purchases_data[0]
            self.assertEqual(empty_package.key.id(), person_reservation.idPaquete)
            self.assertEqual(empty_package.idCompartido, person_reservation.idPaqueteCompartido)
            initial_date = datetime.combine(datetime.strptime(purchase_data[self.INITIAL_DATE_REGISTRY_NAME],
                                                              self.RESPONSE_DATE_FORMAT).date(),
                                            time.min)
            final_date = datetime.combine(datetime.strptime(purchase_data[self.FINAL_DATE_REGISTRY_NAME],
                                                            self.RESPONSE_DATE_FORMAT).date(),
                                          time.max)
            purchase_time = datetime.combine(datetime.strptime(purchase_data[self.PURCHASE_DATE_REGISTRY_NAME],
                                                               self.RESPONSE_DATE_FORMAT).date(),
                                             datetime.strptime(purchase_data[self.PURCHASE_TIME_REGISTRY_NAME],
                                                               self.RESPONSE_TIME_FORMAT).time())
            self.assertEqual(0, person_reservation.precio)
            self.assertEqual(0, person_reservation.impuesto)
            self.assertEqual(initial_date, person_reservation.fechaInicial)
            self.assertEqual(final_date, person_reservation.fechaFinal)
            self.assertEqual(purchase_time, person_reservation.fechaCompra)
            persons_list.remove(person)
            self.assertFalse(person_reservation.activo)
            self.assertEqual(0, len(person_reservation.fechasActivacion))
            self.assertEqual(0, len(person_reservation.fechasDesactivacion))
            if person_reservation.esPrincipal:
                number_of_holders += 1
        self.assertEqual(expected_number_of_holders, number_of_holders)
        self.assertEqual(0, len(persons_list))

    def _check_persons_reservations_for_packages_data(self, persons_list, person_reservations_list,
                                                      purchases_by_document, expected_number_of_holders,
                                                      all_have_packages=True):
        number_of_holders = 0
        for person_reservation in list(person_reservations_list):
            person = self._get_person_by_id(persons_list, person_reservation.idPersona)
            person_key = (person.tipoDocumento, person.numeroDocumento)
            purchases_data = purchases_by_document.get(person_key, [])
            for purchase_data in purchases_data:
                external_code = purchase_data[self.PRODUCT_REGISTRY_NAME]
                expected_package = self.packages_by_external_code[self.EXTERNAL_PACKAGES_CODES_TRANSLATIONS[external_code]]
                if expected_package.key.id() == person_reservation.idPaquete and expected_package.idCompartido == person_reservation.idPaqueteCompartido and float(purchase_data[self.PAYMENT_REGISTRY_NAME]) == person_reservation.precio:
                    initial_date = datetime.combine(datetime.strptime(purchase_data[self.INITIAL_DATE_REGISTRY_NAME],
                                                                      self.RESPONSE_DATE_FORMAT).date(),
                                                    time.min)
                    final_date = datetime.combine(datetime.strptime(purchase_data[self.FINAL_DATE_REGISTRY_NAME],
                                                                    self.RESPONSE_DATE_FORMAT).date(),
                                                  time.max)
                    purchase_time = datetime.combine(datetime.strptime(purchase_data[self.PURCHASE_DATE_REGISTRY_NAME],
                                                                       self.RESPONSE_DATE_FORMAT).date(),
                                                     datetime.strptime(purchase_data[self.PURCHASE_TIME_REGISTRY_NAME],
                                                                       self.RESPONSE_TIME_FORMAT).time())
                    self.assertEqual(float(purchase_data[self.PAYMENT_REGISTRY_NAME]), person_reservation.precio)
                    self.assertEqual(0, person_reservation.impuesto)
                    self.assertEqual(initial_date, person_reservation.fechaInicial)
                    self.assertEqual(final_date, person_reservation.fechaFinal)
                    self.assertEqual(purchase_time, person_reservation.fechaCompra)
                    self.assertEqual(Usuario.ANONYMOUS, person_reservation.usuario)
                    purchases_data.remove(purchase_data)
                    if len(purchases_data) == 0:
                        persons_list.remove(person)
                        person_reservations_list.remove(person_reservation)
                        del purchases_by_document[person_key]
                    break
            else:
                if all_have_packages:
                    self.fail(u"No se encontro registro de compra para la reserva persona {0}".format(str(person_reservation)))
                else:
                    self.assertEqual(0, person_reservation.precio)
                    self.assertEqual(0, person_reservation.impuesto)
            self.assertFalse(person_reservation.activo)
            self.assertEqual(0, len(person_reservation.fechasActivacion))
            self.assertEqual(0, len(person_reservation.fechasDesactivacion))
            if person_reservation.esPrincipal:
                number_of_holders += 1
        self.assertEqual(expected_number_of_holders, number_of_holders)
        self.assertEqual(0, len(purchases_by_document.keys()))
        if all_have_packages:
            self.assertEqual(0, len(persons_list))

    def _check_persons_reservations_for_transports_data(self, persons_list, person_reservations_list,
                                                        amount_topoffs_list, purchases_by_document,
                                                        all_have_topoffs=True):
        for amount_topoff in amount_topoffs_list:
            person_reservation = self._get_person_reservation_by_ids(person_reservations_list,
                                                                     amount_topoff.idReserva,
                                                                     amount_topoff.idReservacionPersona)
            self.assertIsNotNone(person_reservation)
            person = self._get_person_by_id(persons_list, person_reservation.idPersona)
            person_key = (person.tipoDocumento, person.numeroDocumento)
            purchases_data = purchases_by_document[person_key]
            for purchase_data in purchases_data:
                external_code = purchase_data[self.PRODUCT_REGISTRY_NAME]
                expected_sku = self.skus_by_external_code[external_code]
                if expected_sku.key.id() == amount_topoff.idSku:
                    self.assertEqual(self.TEST_ID_CLIENT, amount_topoff.cantidadIncluida)
                    self.assertEqual(1, amount_topoff.idCliente)
                    self.assertEqual(Usuario.ANONYMOUS, person_reservation.usuario)
                    self.assertEqual(purchase_data[self.TRANSACTION_NUMBER_REGISTRY_NAME],
                                     amount_topoff.numeroTransaccion)
                    self.assertIsNone(amount_topoff.idCategoriaSku)
                    self.assertIsNotNone(amount_topoff.tiempoTopoff)
                    purchases_data.remove(purchase_data)
                    if len(purchases_data) == 0:
                        persons_list.remove(person)
                        person_reservations_list.remove(person_reservation)
                        del purchases_by_document[person_key]
                    break
            else:
                self.fail(u"No se encontro registro de compra para el topoff {0}".format(str(amount_topoff)))
        self.assertEqual(0, len(purchases_by_document.keys()))
        if all_have_topoffs:
            self.assertEqual(0, len(persons_list))
            self.assertEqual(0, len(person_reservations_list))

    @staticmethod
    def _merge_purchases_by_document(purchases_by_document_list):
        purchases_by_document = dict()
        for purchases_dict in purchases_by_document_list:
            for key_person, purchases in purchases_dict.iteritems():
                if key_person not in purchases_by_document:
                    purchases_by_document[key_person] = []
                purchases_by_document[key_person] += purchases
        return purchases_by_document

    def _create_purchases_registries_for_every_package(self, transaction_number, worker_document_number=None,
                                                       use_same_person_in_all_purchases=False,
                                                       test_document_type=CC_DOCUMENT_TYPE):
        test_purchases = []
        total_payment = 0.0
        purchases_by_document = dict()
        using_given_worker_document = worker_document_number is not None
        if not using_given_worker_document:
            worker_document_number = "123"

        for index, package_code in enumerate(self.EXTERNAL_PACKAGES_CODES_TRANSLATIONS.keys()):
            if use_same_person_in_all_purchases:
                test_document_number = worker_document_number
            else:
                test_document_number = worker_document_number + str(index)
            test_full_name = "Test Full Name"
            test_birthdate_str = "1990/02/03"
            test_initial_date_str = "2018/05/06"
            test_final_date_str = "2018/05/07"
            test_category = self.C_CATEGORY
            if using_given_worker_document:
                test_worker_document_number = worker_document_number
            else:
                test_worker_document_number = test_document_number
            payment = index * 100
            total_payment += payment
            purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                               test_full_name, test_birthdate_str,
                                                               test_category, test_worker_document_number,
                                                               package_code, payment, test_initial_date_str,
                                                               test_final_date_str, transaction_number)
            test_purchases.append(purchase)
            if test_document_type not in self.VALID_DOCUMENTS:
                test_document_type = self.NO_DOCUMENT_DOCUMENT_TYPE
            person_key = (test_document_type, test_document_number)
            if person_key not in purchases_by_document:
                purchases_by_document[person_key] = []
            purchases_by_document[person_key].append(purchase)
        return purchases_by_document, total_payment, test_purchases

    def _create_purchases_registries_for_every_transport(self, worker_document_number=None,
                                                         use_same_person_in_all_purchases=False,
                                                         test_document_type=CC_DOCUMENT_TYPE):
        test_purchases = []
        purchases_by_document = dict()
        using_given_worker_document = worker_document_number is not None
        if not using_given_worker_document:
            worker_document_number = "123"

        for index, transport_code in enumerate(self.TRANSPORT_EXTERNAL_CODES):
            if use_same_person_in_all_purchases:
                test_document_number = worker_document_number
            else:
                test_document_number = worker_document_number + str(index)
            test_full_name = "Test Full Name"
            test_birthdate_str = "1990/02/03"
            test_initial_date_str = "2018/05/06"
            test_final_date_str = "2018/05/07"
            test_category = self.C_CATEGORY
            if using_given_worker_document:
                test_worker_document_number = worker_document_number
            else:
                test_worker_document_number = test_document_number
            payment = index * 100
            transaction_number = test_document_number + transport_code
            purchase = self._create_sample_registry_for_person(test_document_type, test_document_number,
                                                               test_full_name, test_birthdate_str,
                                                               test_category, test_worker_document_number,
                                                               transport_code, payment, test_initial_date_str,
                                                               test_final_date_str, transaction_number)
            test_purchases.append(purchase)
            if test_document_type not in self.VALID_DOCUMENTS:
                test_document_type = self.NO_DOCUMENT_DOCUMENT_TYPE
            person_key = (test_document_type, test_document_number)
            if person_key not in purchases_by_document:
                purchases_by_document[person_key] = []
            purchases_by_document[person_key].append(purchase)
        return purchases_by_document, test_purchases

    @staticmethod
    def _get_person_by_id(persons_list, id_person):
        for person in persons_list:
            if person.key.id() == id_person:
                return person
        return None

    @staticmethod
    def _get_person_reservation_by_ids(person_reservations_list, id_reservation, id_person_reservation):
        for person_reservation in person_reservations_list:
            if person_reservation.idReserva == id_reservation and person_reservation.key.id() == id_person_reservation:
                return person_reservation
        return None

    @staticmethod
    def _get_person_by_document(persons_list, document_type, document_number):
        for person in persons_list:
            if person.tipoDocumento == document_type and person.numeroDocumento == document_number:
                return person
        return None

    def _check_person_data(self, person, test_document_type, test_document_number, test_full_name, test_birthdate,
                           test_category, test_worker_document_number, original_person=None):
        self.assertEqual(self.TEST_ID_CLIENT, person.idCliente)
        self.assertEqual(test_document_type, person.tipoDocumento)
        self.assertEqual(test_document_number, person.numeroDocumento)
        self.assertEqual(test_birthdate, person.fechaNacimiento)
        self.assertEqual(test_category, person.categoria)
        self.assertEqual(False, person.esFantasma)
        if original_person is None:
            if test_document_number == test_worker_document_number:
                test_affiliation = self.COTIZANTE_AFFILIATION
            else:
                test_affiliation = self.BENEFICIARIO_AFFILIATION
            self.assertEqual("", person.nombre)
            self.assertEqual(Usuario.ANONYMOUS, person.usuario)
            self.assertEqual(test_full_name, person.apellido)
            self.assertEqual(test_affiliation, person.afiliacion)
            self.assertIsNone(person.empresa)
            self.assertIsNone(person.ciudad)
            self.assertIsNone(person.profesion)
            self.assertIsNone(person.nacionalidad)
            self.assertIsNone(person.sexo)
            self.assertIsNone(person.correo)
            self.assertIsNone(person.blobKey)
            self.assertIsNotNone(person.key.id())
        else:
            self.assertEqual(original_person.key.id(), person.key.id())
            self.assertEqual(original_person.nombre, person.nombre)
            self.assertEqual(original_person.apellido, person.apellido)
            self.assertEqual(original_person.afiliacion, person.afiliacion)
            self.assertEqual(original_person.empresa, person.empresa)
            self.assertEqual(original_person.ciudad, person.ciudad)
            self.assertEqual(original_person.profesion, person.profesion)
            self.assertEqual(original_person.nacionalidad, person.nacionalidad)
            self.assertEqual(original_person.sexo, person.sexo)
            self.assertEqual(original_person.correo, person.correo)
            self.assertEqual(original_person.blobKey, person.blobKey)

    @classmethod
    def _create_sample_registry_for_person(cls, document_type, document_number, full_name, test_birthdate,
                                           test_category, worker_document_number, test_package_external_code=None,
                                           test_payment=100, test_initial_date="2018/05/06",
                                           test_final_date="2018/05/07", test_transaction_number=u"456",
                                           test_purchase_date="2018/05/05", test_purchase_time="16:56:53"):
        if test_package_external_code is None:
            test_package_external_code = cls.EXTERNAL_PACKAGES_CODES_TRANSLATIONS.keys()[0]
        return {cls.DOCUMENT_TYPE_REGISTRY_NAME: str(cls.IDS_BY_DOCUMENT_TYPE.get(document_type, "")),
                cls.DOCUMENT_NUMBER_REGISTRY_NAME: document_number,
                cls.FULL_NAME_REGISTRY_NAME: full_name,
                cls.BIRTHDATE_REGISTRY_NAME: test_birthdate,
                cls.CATEGORY_REGISTRY_NAME: cls.PREFIX_CATEGORY + test_category,
                cls.WORKER_DOCUMENT_NUMBER_REGISTRY_NAME: worker_document_number,
                cls.PRODUCT_REGISTRY_NAME: test_package_external_code,
                cls.PAYMENT_REGISTRY_NAME: str(test_payment),
                cls.INITIAL_DATE_REGISTRY_NAME: test_initial_date,
                cls.PURCHASE_DATE_REGISTRY_NAME: test_purchase_date,
                cls.PURCHASE_TIME_REGISTRY_NAME: test_purchase_time,
                cls.FINAL_DATE_REGISTRY_NAME: test_final_date,
                cls.TRANSACTION_NUMBER_REGISTRY_NAME: test_transaction_number}


if __name__ == '__main__':
    unittest.main()
