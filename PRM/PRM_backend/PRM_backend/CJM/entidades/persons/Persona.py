# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from commons.entidades.Cliente import Cliente
from commons.entidades.optionalities.FieldOptionality import FieldOptionality
from commons.entidades.users.Usuario import Usuario
from dateutil.relativedelta import relativedelta


class Persona(ndb.Model):
    FIRST_NAME_NAME = "first-name"
    LAST_NAME_NAME = "last-name"
    DOCUMENT_TYPE_NAME = "document-type"
    DOCUMENT_NUMBER_NAME = "document-number"
    MAIL_NAME = "mail"
    GENDER_NAME = "gender"
    BIRTHDATE_NAME = "birthdate"
    CATEGORY_NAME = "category"
    AFFILIATION_NAME = "affiliation"
    NATIONALITY_NAME = "nationality"
    PROFESSION_NAME = "profession"
    COMPANY_NAME = "company"
    CITY_NAME = "city"
    IS_PHANTOM_PERSON_NAME = "is-phantom"
    ID_NAME = "id"
    CLIENT_ID_NAME = Cliente.ID_CLIENT_NAME
    ID_PERSON_NAME = "id-person"
    IMAGE_KEY_NAME = "image-key"
    USERNAME_NAME = "username"

    idCliente = ndb.IntegerProperty()
    nombre = ndb.StringProperty()
    apellido = ndb.StringProperty()
    correo = ndb.StringProperty()
    sexo = ndb.StringProperty(indexed=True)
    categoria = ndb.StringProperty(indexed=True)
    afiliacion = ndb.StringProperty(indexed=True)
    nacionalidad = ndb.StringProperty()
    profesion = ndb.StringProperty()
    ciudad = ndb.StringProperty()
    empresa = ndb.StringProperty()
    fechaNacimiento = ndb.DateProperty()
    numeroDocumento = ndb.StringProperty(indexed=True)
    tipoDocumento = ndb.StringProperty(indexed=True)
    esFantasma = ndb.BooleanProperty(indexed=True)
    tokenId = ndb.StringProperty(indexed=True)
    blobKey = ndb.BlobKeyProperty()
    usuario = ndb.StringProperty(indexed=True)

    def is_phantom_person(self):
        if self.esFantasma is None:
            return False
        else:
            return self.esFantasma

    def to_dict(self):
        fields_dict = dict()
        fields_dict[Persona.ID_NAME] = self.key.id()
        fields_dict[Persona.IS_PHANTOM_PERSON_NAME] = self.is_phantom_person()
        fields_dict[self.USERNAME_NAME] = self.usuario
        if self.nombre is not None:
            fields_dict[Persona.FIRST_NAME_NAME] = self.nombre
        if self.apellido is not None:
            fields_dict[Persona.LAST_NAME_NAME] = self.apellido
        if self.tipoDocumento is not None:
            fields_dict[Persona.DOCUMENT_TYPE_NAME] = self.tipoDocumento
        if self.numeroDocumento is not None:
            fields_dict[Persona.DOCUMENT_NUMBER_NAME] = self.numeroDocumento
        if self.sexo is not None:
            fields_dict[Persona.GENDER_NAME] = self.sexo
        if self.correo is not None:
            fields_dict[Persona.MAIL_NAME] = self.correo
        if self.categoria is not None:
            fields_dict[Persona.CATEGORY_NAME] = self.categoria
        if self.afiliacion is not None:
            fields_dict[Persona.AFFILIATION_NAME] = self.afiliacion
        if self.nacionalidad is not None:
            fields_dict[Persona.NATIONALITY_NAME] = self.nacionalidad
        if self.profesion is not None:
            fields_dict[Persona.PROFESSION_NAME] = self.profesion
        if self.ciudad is not None:
            fields_dict[Persona.CITY_NAME] = self.ciudad
        if self.empresa is not None:
            fields_dict[Persona.COMPANY_NAME] = self.empresa
        if self.fechaNacimiento is not None:
            from commons.validations import DEFAULT_DATE_FORMAT
            try:
                # noinspection PyUnresolvedReferences
                str_birthdate = self.fechaNacimiento.strftime(DEFAULT_DATE_FORMAT)
                fields_dict[Persona.BIRTHDATE_NAME] = str_birthdate
            except ValueError:
                pass
        if self.blobKey is None:
            str_blob_key = None
        else:
            str_blob_key = repr(self.blobKey)
        fields_dict[Persona.IMAGE_KEY_NAME] = str_blob_key
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create_without_put(cls, id_client, nombre, apellido, tipo_documento, numero_documento, correo, sexo,
                           fecha_nacimiento, categoria, afiliacion, nacionalidad, profesion, ciudad, empresa,
                           username=None):
        if username is None:
            username = Usuario.get_current_username()
        nueva_persona = Persona(
            idCliente=id_client,
            nombre=nombre,
            apellido=apellido,
            tipoDocumento=tipo_documento,
            numeroDocumento=numero_documento,
            correo=correo,
            sexo=sexo,
            fechaNacimiento=fecha_nacimiento,
            categoria=categoria,
            afiliacion=afiliacion,
            nacionalidad=nacionalidad,
            profesion=profesion,
            ciudad=ciudad,
            empresa=empresa,
            usuario=username,
            esFantasma=False
        )
        return nueva_persona

    @classmethod
    def create(cls, id_client, nombre, apellido, tipo_documento, numero_documento, correo, sexo, fecha_nacimiento,
               categoria, afiliacion, nacionalidad, profesion, ciudad, empresa, username=None):
        nueva_persona = cls.create_without_put(id_client, nombre, apellido, tipo_documento, numero_documento, correo,
                                               sexo, fecha_nacimiento, categoria, afiliacion, nacionalidad, profesion,
                                               ciudad, empresa, username)
        nueva_persona.put()
        return nueva_persona

    def update_without_put(self, nombre, apellido, tipo_documento, numero_documento, correo, sexo, fecha_nacimiento,
                           categoria, afiliacion, nacionalidad, profesion, ciudad, empresa):
        self.nombre = nombre
        self.apellido = apellido
        self.tipoDocumento = tipo_documento
        self.numeroDocumento = numero_documento
        self.correo = correo
        self.sexo = sexo
        self.fechaNacimiento = fecha_nacimiento
        self.categoria = categoria
        self.afiliacion = afiliacion
        self.nacionalidad = nacionalidad
        self.profesion = profesion
        self.ciudad = ciudad
        self.empresa = empresa
        self.esFantasma = False

    @classmethod
    def update(cls, id_person, nombre, apellido, tipo_documento, numero_documento, correo, sexo, fecha_nacimiento,
               categoria, afiliacion, nacionalidad, profesion, ciudad, empresa):
        person = Persona.get_by_id(id_person)
        person.nombre = nombre
        person.apellido = apellido
        person.tipoDocumento = tipo_documento
        person.numeroDocumento = numero_documento
        person.correo = correo
        person.sexo = sexo
        person.fechaNacimiento = fecha_nacimiento
        person.categoria = categoria
        person.afiliacion = afiliacion
        person.nacionalidad = nacionalidad
        person.profesion = profesion
        person.ciudad = ciudad
        person.empresa = empresa
        person.esFantasma = False
        person.put()
        return person

    @classmethod
    def _get_person_by_document_query(cls, document_type, document_number):
        return cls.query(ndb.AND(Persona.tipoDocumento == document_type,
                                 Persona.numeroDocumento == document_number))

    @classmethod
    def list_by_document_number_async(cls, document_number):
        return cls.query(Persona.numeroDocumento == document_number).fetch_async()

    @classmethod
    def get_person_by_document_async(cls, document_type, document_number):
        return cls._get_person_by_document_query(document_type, document_number).get_async()

    @classmethod
    def get_person_by_document(cls, document_type, document_number):
        return cls._get_person_by_document_query(document_type, document_number).get()

    @classmethod
    def create_phantom_person(cls, id_client):
        new_person = Persona(
            idCliente=id_client,
            esFantasma=True
        )
        new_person.put()
        return new_person

    @classmethod
    def list(cls):
        return Persona.query().fetch()

    @classmethod
    def get_key_from_id(cls, id_person):
        return ndb.Key(Persona, id_person)

    def get_age_group(self):
        from CJM.services.validations import INFANT_AGE_GROUP, KID_AGE_GROUP, ADULT_AGE_GROUP
        age = self.get_age()
        if age is None:
            return None
        elif age <= 2:
            return INFANT_AGE_GROUP
        elif age <= 11:
            return KID_AGE_GROUP
        else:
            return ADULT_AGE_GROUP

    def get_age(self):
        if self.fechaNacimiento is None:
            return None
        from commons.validations import validate_date
        current_date = validate_date(None, "Current Date", allow_none=True)
        return relativedelta(current_date, self.fechaNacimiento).years

    @classmethod
    def create_fields_optionalities_without_put(cls, id_client):
        from CJM.services.persons.personaView import PERSONS_VIEW_NAME
        optionalities = [FieldOptionality.create(id_client, PERSONS_VIEW_NAME,
                                                 cls.FIRST_NAME_NAME,
                                                 FieldOptionality.MANDATORY_NAME,
                                                 FieldOptionality.STRING_TYPE,
                                                 allow_change=False),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.LAST_NAME_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.DOCUMENT_TYPE_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.DOCUMENT_NUMBER_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client,
                                                             PERSONS_VIEW_NAME,
                                                             cls.GENDER_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.BIRTHDATE_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.DATE_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.MAIL_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.CATEGORY_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.AFFILIATION_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.NATIONALITY_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.PROFESSION_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.COMPANY_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, PERSONS_VIEW_NAME,
                                                             cls.CITY_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE)]
        return optionalities

    @classmethod
    def create_fields_optionalities(cls, id_client):
        optionalities = cls.create_fields_optionalities_without_put(id_client)
        ndb.put_multi(optionalities)
