# -*- coding: utf-8 -*
import json

from google.appengine.ext import ndb

from commons.entidades import calculate_next_string
from commons.entidades.EPC import EPC
from commons.entidades.Generador import Generador
from commons.entidades.locations.TipoUbicacion import TipoUbicacion
from commons.entidades.optionalities.FieldOptionality import FieldOptionality
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import update_descendants_key


class Ubicacion(ndb.Model):

    IDS_UBICACIONES_NAME = "ids-locations"
    PARENT_NAME = "id-parent-location"
    DESCRIPTION_NAME = "description"
    LATITUDE_NAME = "latitude"
    LONGITUDE_NAME = "longitude"
    WEB_URL_NAME = "web"
    SUBTYPE_NAME = "subtype"
    PHONE_NAME = "phone"
    ADDRESS_NAME = "address"
    MAIL_NAME = "mail"
    TAGS_NAME = "tags"
    ACTIVE_NAME = "active"
    LOCATION_NAME_NAME = "name"
    KEY_NAME = "key"
    TYPE_NAME = "type"
    EPC_NAME = "EPC"
    ID_NAME = "id"
    ID_UBICACION_NAME = "id-location"
    IMAGE_KEY_NAME = "image-key"
    DEFAULT_PLACES = ((u'Colombia', None, TipoUbicacion.COUNTRY),
                      (u'Amazonas', u'Colombia', TipoUbicacion.REGION),
                      (u'Antioquia', u'Colombia', TipoUbicacion.REGION),
                      (u'Arauca', u'Colombia', TipoUbicacion.REGION),
                      (u'Atlántico', u'Colombia', TipoUbicacion.REGION),
                      (u'Bogotá DC', u'Colombia', TipoUbicacion.REGION),
                      (u'Bolívar', u'Colombia', TipoUbicacion.REGION),
                      (u'Boyacá', u'Colombia', TipoUbicacion.REGION),
                      (u'Caldas', u'Colombia', TipoUbicacion.REGION),
                      (u'Caquetá', u'Colombia', TipoUbicacion.REGION),
                      (u'Casanare', u'Colombia', TipoUbicacion.REGION),
                      (u'Cauca', u'Colombia', TipoUbicacion.REGION),
                      (u'Cesar', u'Colombia', TipoUbicacion.REGION),
                      (u'Chocó', u'Colombia', TipoUbicacion.REGION),
                      (u'Córdoba', u'Colombia', TipoUbicacion.REGION),
                      (u'Cundinamarca', u'Colombia', TipoUbicacion.REGION),
                      (u'Guainía', u'Colombia', TipoUbicacion.REGION),
                      (u'Guaviare', u'Colombia', TipoUbicacion.REGION),
                      (u'Huila', u'Colombia', TipoUbicacion.REGION),
                      (u'La Guajira', u'Colombia', TipoUbicacion.REGION),
                      (u'Magdalena', u'Colombia', TipoUbicacion.REGION),
                      (u'Meta', u'Colombia', TipoUbicacion.REGION),
                      (u'Nariño', u'Colombia', TipoUbicacion.REGION),
                      (u'Norte de Santander', u'Colombia', TipoUbicacion.REGION),
                      (u'Putumayo', u'Colombia', TipoUbicacion.REGION),
                      (u'Quindío', u'Colombia', TipoUbicacion.REGION),
                      (u'Risaralda', u'Colombia', TipoUbicacion.REGION),
                      (u'San Andrés y Providencia', u'Colombia', TipoUbicacion.REGION),
                      (u'Santander', u'Colombia', TipoUbicacion.REGION),
                      (u'Sucre', u'Colombia', TipoUbicacion.REGION),
                      (u'Tolima', u'Colombia', TipoUbicacion.REGION),
                      (u'Valle del Cauca', u'Colombia', TipoUbicacion.REGION),
                      (u'Vaupés', u'Colombia', TipoUbicacion.REGION),
                      (u'Vichada', u'Colombia', TipoUbicacion.REGION),
                      (u'Leticia', u'Amazonas', TipoUbicacion.CITY),
                      (u'Medellín', u'Antioquia', TipoUbicacion.CITY),
                      (u'Arauca', u'Arauca', TipoUbicacion.CITY),
                      (u'Barranquilla', u'Atlántico', TipoUbicacion.CITY),
                      (u'Bogotá', u'Bogotá DC', TipoUbicacion.CITY),
                      (u'Cartagena de Indias', u'Bolívar', TipoUbicacion.CITY),
                      (u'Tunja', u'Boyacá', TipoUbicacion.CITY),
                      (u'Manizales', u'Caldas', TipoUbicacion.CITY),
                      (u'Florencia', u'Caquetá', TipoUbicacion.CITY),
                      (u'Yopal', u'Casanare', TipoUbicacion.CITY),
                      (u'Popayán', u'Cauca', TipoUbicacion.CITY),
                      (u'Valledupar', u'Cesar', TipoUbicacion.CITY),
                      (u'Quibdó', u'Chocó', TipoUbicacion.CITY),
                      (u'Montería', u'Córdoba', TipoUbicacion.CITY),
                      (u'Inírida', u'Guainía', TipoUbicacion.CITY),
                      (u'San José del Guaviare', u'Guaviare', TipoUbicacion.CITY),
                      (u'Neiva', u'Huila', TipoUbicacion.CITY),
                      (u'Riohacha', u'La Guajira', TipoUbicacion.CITY),
                      (u'Santa Marta', u'Magdalena', TipoUbicacion.CITY),
                      (u'Villavicencio', u'Meta', TipoUbicacion.CITY),
                      (u'Pasto', u'Nariño', TipoUbicacion.CITY),
                      (u'San José de Cúcuta', u'Norte de Santander', TipoUbicacion.CITY),
                      (u'Mocoa', u'Putumayo', TipoUbicacion.CITY),
                      (u'Armenia', u'Quindío', TipoUbicacion.CITY),
                      (u'Pereira', u'Risaralda', TipoUbicacion.CITY),
                      (u'San Andrés', u'San Andrés y Providencia', TipoUbicacion.CITY),
                      (u'Bucaramanga', u'Santander', TipoUbicacion.CITY),
                      (u'Sincelejo', u'Sucre', TipoUbicacion.CITY),
                      (u'Ibagué', u'Tolima', TipoUbicacion.CITY),
                      (u'Cali', u'Valle del Cauca', TipoUbicacion.CITY),
                      (u'Mitú', u'Vaupés', TipoUbicacion.CITY),
                      (u'Puerto Carreño', u'Vichada', TipoUbicacion.CITY))

    llave = ndb.StringProperty(indexed=True)
    EPC = ndb.StringProperty(indexed=True)
    tipo = ndb.StringProperty(indexed=True)
    nombre = ndb.StringProperty(indexed=False)
    idCliente = ndb.IntegerProperty(indexed=False)
    idPadre = ndb.IntegerProperty(indexed=True)
    descripcion = ndb.StringProperty(indexed=False)
    activo = ndb.BooleanProperty(indexed=True)
    latitud = ndb.FloatProperty(indexed=True)
    longitud = ndb.FloatProperty(indexed=True)
    web = ndb.StringProperty(indexed=False)
    telefono = ndb.StringProperty(indexed=False)
    direccion = ndb.StringProperty(indexed=False)
    correo = ndb.StringProperty(indexed=False)
    tags = ndb.StringProperty(indexed=True, repeated=True)
    subtipo = ndb.StringProperty(indexed=True)
    blobKey = ndb.BlobKeyProperty()

    @classmethod
    def trim_default_locations(cls, num_places):
        cls.DEFAULT_PLACES = cls.DEFAULT_PLACES[:num_places]

    @classmethod
    def list_without_fetch(cls):
        return cls.query()

    @classmethod
    def list(cls):
        return cls.list_without_fetch().fetch()

    @classmethod
    def list_by_type(cls, location_type):
        return Ubicacion.query(Ubicacion.tipo == location_type).fetch()

    @staticmethod
    def create_basic_locations_without_put(id_client):
        id_location = Generador.get_next_location_id(len(Ubicacion.DEFAULT_PLACES))
        created_locations = {}
        batch = []
        for place in Ubicacion.DEFAULT_PLACES:
            location_name = place[0]
            location_type = place[2]
            parent_location = None if place[1] is None else created_locations[place[1]]
            new_location = Ubicacion(key=ndb.Key(Ubicacion, id_location),
                                     nombre=location_name,
                                     tipo=location_type,
                                     EPC=EPC.get_epc_location(id_client, id_location),
                                     activo=True)
            if parent_location is not None:
                new_location.idPadre = parent_location.key.id()
                TipoUbicacion.is_valid_relation(parent_location.tipo, location_type)
                new_location.llave = parent_location.llave + str(id_location) + ':'
            else:
                new_location.llave = str(id_location) + ':'
            batch.append(new_location)
            created_locations[location_name] = new_location
            id_location += 1
        return batch

    @staticmethod
    def create_basic_locations(id_client):
        batch = Ubicacion.create_basic_locations_without_put(id_client)
        ndb.put_multi(batch)

    @classmethod
    def create(cls, id_client, location_name, location_type, location_parent_id, description, active, latitude,
               longitude, web, phone, address, mail, tags, subtype):
        id_location = Generador.get_next_location_id()
        new_location = Ubicacion(key=ndb.Key(Ubicacion, id_location),
                                 idPadre=location_parent_id,
                                 nombre=location_name,
                                 tipo=location_type,
                                 EPC=EPC.get_epc_location(id_client, id_location),
                                 descripcion=description,
                                 activo=active,
                                 latitud=latitude,
                                 longitud=longitude,
                                 web=web,
                                 telefono=phone,
                                 direccion=address,
                                 correo=mail,
                                 tags=tags,
                                 subtipo=subtype)
        if location_parent_id is not None:
            padre = Ubicacion.get(location_parent_id)
            TipoUbicacion.is_valid_relation(padre.tipo, location_type)
            new_location.llave = padre.llave + str(id_location) + ':'
        else:
            new_location.llave = str(id_location) + ':'
        new_location.put()
        return new_location

    @classmethod
    def list_by_parent_id(cls, location_parent_id):
        if location_parent_id is None:
            return list()
        else:
            parent = cls.get(location_parent_id)
            return cls.query(ndb.AND(cls.llave >= parent.llave,
                                     cls.llave < calculate_next_string(parent.llave))).fetch()

    def id_location(self):
        return self.key.id()

    @classmethod
    def get(cls, id_location):
        return Ubicacion.get_by_id(id_location)

    @classmethod
    def get_id_parent_from_key(cls, location_key):
        if location_key is None:
            return None
        last_separator_index = location_key[:len(location_key) - 1].rfind(":")
        if last_separator_index == -1:
            return None
        else:
            second_last_separator_index = location_key[:last_separator_index - 1].rfind(":")
            return location_key[second_last_separator_index + 1:last_separator_index]

    @classmethod
    def get_id_from_key(cls, location_key):
        if location_key is None:
            return None
        last_separator_index = location_key[:len(location_key) - 1].rfind(":")
        return location_key[last_separator_index + 1:len(location_key) - 1]

    # noinspection PyUnresolvedReferences
    def get_hierarchy_ids(self):
        return [int(str_id) for str_id in self.llave.split(":")[:-1]]

    def update(self, location_name, location_type, location_parent_id, description, active, latitude,
               longitude, web, phone, address, mail, tags, subtype):
        original_parent = self.idPadre
        self.idPadre = location_parent_id
        try:
            from commons.validations import LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE
            locations_to_update = update_descendants_key(Ubicacion, self,
                                                         internal_code=LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE)
        except ValidationError as error:
            self.idPadre = original_parent
            from commons.validations import LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE
            if error.internal_code == LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE:
                raise ValidationError(u"Can not change the parent of location {0} to {1} because it creates a cicle"
                                      u" on the hirearchy"
                                      .format(self.key.id(), location_parent_id),
                                      internal_code=LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE)
            else:
                raise error
        self.descripcion = description
        self.nombre = location_name
        self.tipo = location_type
        self.activo = active
        self.latitud = latitude
        self.longitud = longitude
        self.web = web
        self.telefono = phone
        self.direccion = address
        self.correo = mail
        self.tags = tags
        self.subtipo = subtype
        ndb.put_multi(locations_to_update)

    # noinspection PyTypeChecker
    def to_dict(self):
        fields_dict = dict()

        fields_dict[Ubicacion.ID_NAME] = self.id_location()
        fields_dict[Ubicacion.LOCATION_NAME_NAME] = self.nombre
        fields_dict[Ubicacion.KEY_NAME] = self.llave
        fields_dict[Ubicacion.TYPE_NAME] = self.tipo
        fields_dict[Ubicacion.EPC_NAME] = self.EPC
        fields_dict[Ubicacion.ACTIVE_NAME] = self.activo

        id_parent = Ubicacion.get_id_parent_from_key(self.llave)
        if id_parent is not None:
            fields_dict[Ubicacion.PARENT_NAME] = int(id_parent)
        if self.descripcion is not None:
            fields_dict[Ubicacion.DESCRIPTION_NAME] = self.descripcion
        if self.subtipo is not None:
            fields_dict[Ubicacion.SUBTYPE_NAME] = self.subtipo
        if self.web is not None:
            fields_dict[Ubicacion.WEB_URL_NAME] = self.web
        if self.telefono is not None:
            fields_dict[Ubicacion.PHONE_NAME] = self.telefono
        if self.direccion is not None:
            fields_dict[Ubicacion.ADDRESS_NAME] = self.direccion
        if self.correo is not None:
            fields_dict[Ubicacion.MAIL_NAME] = self.correo
        if self.latitud is not None and self.longitud is not None:
            fields_dict[Ubicacion.LATITUDE_NAME] = self.latitud
            fields_dict[Ubicacion.LONGITUDE_NAME] = self.longitud
        if self.tags is not None and len(self.tags) > 0:
            fields_dict[Ubicacion.TAGS_NAME] = self.tags
        elif TipoUbicacion.requires_tags(self.tipo):
            fields_dict[Ubicacion.TAGS_NAME] = []
        if self.blobKey is None:
            str_blob_key = None
        else:
            str_blob_key = repr(self.blobKey)
        fields_dict[Ubicacion.IMAGE_KEY_NAME] = str_blob_key
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def list_active_locations(cls):
        return Ubicacion.query(Ubicacion.activo == True).fetch()

    @classmethod
    def create_fields_optionalities_without_put(cls, id_client):
        from commons.services.locations.ubicacionView import LOCATIONS_VIEW_NAME
        optionalities = [FieldOptionality.create_without_put(id_client,
                                                             LOCATIONS_VIEW_NAME,
                                                             cls.LOCATION_NAME_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.TYPE_NAME, FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.PARENT_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             allow_change=False),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.DESCRIPTION_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.ACTIVE_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.BOOLEAN_TYPE,
                                                             allow_change=False,
                                                             default_value=True),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.LATITUDE_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.FLOAT_TYPE),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.LONGITUDE_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.FLOAT_TYPE),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.WEB_URL_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             applicable_subtypes=TipoUbicacion.LIST_DEFAULT_LOCATIONS_TYPES_WITH_WEB),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.PHONE_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             applicable_subtypes=TipoUbicacion.LIST_DEFAULT_LOCATIONS_TYPES_WITH_PHONE),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.ADDRESS_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             applicable_subtypes=TipoUbicacion.LIST_DEFAULT_LOCATIONS_TYPES_WITH_ADDRESS),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.MAIL_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             applicable_subtypes=TipoUbicacion.LIST_DEFAULT_LOCATIONS_TYPES_WITH_MAIL),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.TAGS_NAME,
                                                             FieldOptionality.OPTIONAL_NAME,
                                                             FieldOptionality.STRING_ARRAY_TYPE,
                                                             applicable_subtypes=TipoUbicacion.LIST_DEFAULT_LOCATIONS_TYPES_WITH_TAGS),
                         FieldOptionality.create_without_put(id_client, LOCATIONS_VIEW_NAME,
                                                             cls.SUBTYPE_NAME,
                                                             FieldOptionality.MANDATORY_NAME,
                                                             FieldOptionality.STRING_TYPE,
                                                             applicable_subtypes=TipoUbicacion.LIST_DEFAULT_LOCATIONS_TYPES_WITH_SUBTYPE)]
        return optionalities

    @classmethod
    def create_fields_optionalities(cls, id_client):
        optionalities = cls.create_fields_optionalities_without_put(id_client)
        ndb.put_multi(optionalities)

    def update_key_by_parent(self, parent=None):
        if self.idPadre is None:
            self.llave = str(self.key.id()) + ':'
        else:
            if parent is None:
                parent = Ubicacion.get_by_id(self.idPadre)
            if (':' + str(self.key.id()) + ':') in (':' + parent.llave):
                from commons.validations import LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE
                raise ValidationError(u"The location {0} is ancestor of the location {1}. Can not create "
                                      u"cicles on the hirearchy".format(self.key.id(), self.idPadre),
                                      internal_code=LOCATION_INVALID_HIREARCHY_NAME_ERROR_CODE)
            TipoUbicacion.is_valid_relation(parent.tipo, self.tipo)
            self.llave = parent.llave + str(self.key.id()) + ':'
