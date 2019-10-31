# -*- coding: utf-8 -*
import itertools
import json

from google.appengine.ext import ndb

from commons.entidades.Generador import Generador
from commons.utils import on_client_namespace


class Cliente(ndb.Model):
    IDS_GENERATORS = {Generador.ID_PRODUCTS,
                      Generador.ID_LOCATIONS,
                      Generador.ID_TRANSACTIONS,
                      Generador.ID_PERSONS,
                      Generador.ID_SKUS,
                      Generador.ID_EVENTS,
                      Generador.ID_SENSORS,
                      Generador.ID_BEACONS}
    CLIENT_NAME_NAME = "name"
    ID_NAME = "id"
    ID_CLIENT_NAME = "id-client"
    CLIENT_REQUIRES_LOGIN_NAME = "requires-login"
    EXTERNAL_PERSON_SERVICE_NAME = "external-person-service"
    EXTERNAL_RESERVATIONS_SERVICE_NAME = "external-reservations-service"

    nombre = ndb.StringProperty()
    servicoPersonasExterno = ndb.StringProperty(indexed=True)
    servicoReservasExterno = ndb.StringProperty(indexed=True)
    requiereLogin = ndb.BooleanProperty()

    @classmethod
    def create(cls, name, requires_login, external_person_service, external_reservations_service):
        """
        Crea un nuevo cliente con el nombre dado.
        :param name: Nombre del cliente a create.
        :param requires_login: Indica si los usuarios requieren login para acceder a los servicios de este cliente.
        :param external_person_service: Nombre del servicio externo a usar para obtener datos de personas.
        :param external_reservations_service: Nombre del servicio externo a usar para obtener datos de reservas.
        :return: Cliente creado
        """
        cliente = Cliente(key=ndb.Key(Cliente, Generador.get_next_client_id()), nombre=name,
                          servicoPersonasExterno=external_person_service,
                          servicoReservasExterno=external_reservations_service,
                          requiereLogin=requires_login)
        cliente.put()
        on_client_namespace(cliente.key.id(), cliente.create_aditional_elements, secured=False)
        return cliente

    @classmethod
    def update(cls, id_client, name, requires_login, external_person_service, external_reservations_service):
        client = Cliente.get_by_id(id_client)
        client.nombre = name
        client.requiereLogin = requires_login
        client.servicoPersonasExterno = external_person_service
        client.servicoReservasExterno = external_reservations_service
        client.put()
        return client

    @classmethod
    def delete(cls, id_client):
        client = Cliente.get_by_id(id_client)
        client.key.delete()
        return client

    @classmethod
    def list(cls):
        return Cliente.query().fetch()

    @classmethod
    def create_aditional_elements(cls, id_client):
        from commons.entidades.locations.Ubicacion import Ubicacion
        Generador.create_multi(cls.IDS_GENERATORS)
        location_entities = Ubicacion.create_basic_locations_without_put(id_client)
        from commons.entidades.users.Usuario import Usuario
        user_entities = Usuario.create_admin_user_without_put(id_client)
        from CJM.entidades.Moneda import Moneda
        money_entities = [Moneda.create_default_currency_without_put(id_client)]
        from commons.entidades.ClientKey import ClientKey
        client_keys_entities = [ClientKey.create()]
        location_optionalities_entities = Ubicacion.create_fields_optionalities_without_put(id_client)
        from CJM.entidades.persons.Persona import Persona
        person_optionalities_entities = Persona.create_fields_optionalities_without_put(id_client)
        ndb.put_multi(list(itertools.chain(location_entities, user_entities, money_entities, client_keys_entities,
                                           location_optionalities_entities, person_optionalities_entities)))

    def to_dict(self):
        fields_dict = dict()
        fields_dict[Cliente.ID_NAME] = self.key.id()
        fields_dict[Cliente.CLIENT_NAME_NAME] = self.nombre
        fields_dict[Cliente.CLIENT_REQUIRES_LOGIN_NAME] = self.requiereLogin
        fields_dict[Cliente.EXTERNAL_PERSON_SERVICE_NAME] = self.servicoPersonasExterno
        fields_dict[Cliente.EXTERNAL_RESERVATIONS_SERVICE_NAME] = self.servicoReservasExterno
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def list_by_external_reservations_service(cls, external_service):
        return cls.query(cls.servicoReservasExterno == external_service).fetch()
