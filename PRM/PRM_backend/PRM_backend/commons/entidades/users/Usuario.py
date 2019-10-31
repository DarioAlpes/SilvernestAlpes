# -*- coding: utf-8 -*
import json

from flask_login.mixins import UserMixin
from flask_login.utils import current_user
from google.appengine.ext import ndb

from commons.entidades.Cliente import Cliente
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.entidades.users.Role import GLOBAL_ADMIN, CLIENT_ADMIN
from commons.entidades.users.RolesByLocation import RolesByLocation
from commons.excepciones.apiexceptions import EntityAlreadyExists, ValidationError
from commons.utils import on_client_namespace


class Usuario(ndb.Model, UserMixin):

    USERNAME_NAME = "username"
    PASSWORD_NAME = "password"
    ROLE_NAME = "role"
    ANONYMOUS = "Anonymous"
    CLIENT_ID_NAME = Cliente.ID_CLIENT_NAME
    LOCATIONS_IDS_NAME = Ubicacion.IDS_UBICACIONES_NAME
    username = ndb.StringProperty()
    idCliente = ndb.IntegerProperty()
    password = ndb.StringProperty()

    @classmethod
    def list(cls):
        return Usuario.query().fetch()

    def __init__(self, new_user=False, *args, **kwargs):
        super(Usuario, self).__init__(*args, **kwargs)
        password = kwargs.get('password')
        if password is not None and new_user:
            # Initialize and encrypt password before first save.
            self.set_and_encrypt_password(password)

    def set_and_encrypt_password(self, password):
        from main import pwd_context
        self.password = pwd_context.hash(password)

    def validate_password(self, password):
        from main import pwd_context
        return pwd_context.verify(password, self.password)

    # noinspection PyTypeChecker
    def get_id(self):
        if self.idCliente is None:
            client_id = 0
        else:
            client_id = self.idCliente
        return str(client_id) + u"$" + self.username

    @classmethod
    def create(cls, id_client, username, password, roles_and_applicable_locations):
        entities_to_put = cls.create_without_put(id_client, username, password, roles_and_applicable_locations)
        ndb.put_multi(entities_to_put)
        return entities_to_put[0]

    @classmethod
    def create_without_put(cls, id_client, username, password, roles_and_applicable_locations):
        key = ndb.Key(Usuario, username)
        if key.get() is None:
            usuario = Usuario(key=cls.get_key_from(username), username=username, idCliente=id_client,
                              password=password, new_user=True)
            entities_to_put = [usuario]
            for role in roles_and_applicable_locations:
                ids_locations_by_role = roles_and_applicable_locations[role]
                if ids_locations_by_role is None:
                    entities_to_put.append(RolesByLocation.create(username, role, id_client, None))
                else:
                    entities_to_put.extend([RolesByLocation.create(username, role, id_client, id_location)
                                            for id_location in ids_locations_by_role])
            return entities_to_put
        else:
            from commons.validations import USER_ALREADY_EXISTS_ERROR_CODE
            raise EntityAlreadyExists(u"User[{0}]".format(username), internal_code=USER_ALREADY_EXISTS_ERROR_CODE)

    @classmethod
    def change_password(cls, username, password):
        user = Usuario.get_by_id(username)
        user.set_and_encrypt_password(password)
        user.put()
        return user

    @classmethod
    def delete(cls, username):
        user = Usuario.get_by_id(username)
        json_user = user.to_json()
        roles_by_location = RolesByLocation.list_by_username_and_client(username, user.idCliente)
        keys_to_delete = [role.key for role in roles_by_location]
        keys_to_delete.append(user.key)
        ndb.delete_multi(keys_to_delete)
        return json_user

    def to_dict(self):
        fields_dict = dict()

        if self.idCliente is not None:
            fields_dict[Usuario.CLIENT_ID_NAME] = self.idCliente
        fields_dict[Usuario.USERNAME_NAME] = self.username

        def get_roles_on_namespace(id_client):
            return RolesByLocation.get_roles_by_user(self.username, id_client)

        if self.idCliente is None:
            locations_by_role = get_roles_on_namespace(None)
        else:
            locations_by_role = on_client_namespace(self.idCliente, get_roles_on_namespace, secured=False)
        if len(locations_by_role) != 1:
            raise ValidationError(u"The user {0} has more than one role, please contact an administrator"
                                  .format(self.username))

        for role in locations_by_role:
            fields_dict[Usuario.ROLE_NAME] = role
            if locations_by_role[role] is None:
                fields_dict[Usuario.LOCATIONS_IDS_NAME] = locations_by_role[role]
            else:
                fields_dict[Usuario.LOCATIONS_IDS_NAME] = list(locations_by_role[role])

        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    @classmethod
    def create_admin_user(cls, id_client):
        default_username = "admin"
        default_password = "password"

        def create_admin_user_on_namespace(id_current_client):
            roles_and_applicable_locations = {CLIENT_ADMIN: None}
            return Usuario.create(id_client=id_current_client, username=default_username, password=default_password,
                                  roles_and_applicable_locations=roles_and_applicable_locations)

        return on_client_namespace(id_client, create_admin_user_on_namespace, secured=False)

    @classmethod
    def create_admin_user_without_put(cls, id_client):
        default_username = "admin"
        default_password = "password"
        roles_and_applicable_locations = {CLIENT_ADMIN: None}
        return Usuario.create_without_put(id_client=id_client, username=default_username, password=default_password,
                                          roles_and_applicable_locations=roles_and_applicable_locations)

    @classmethod
    def create_global_admin_user(cls):
        default_username = "admin"
        default_password = "password"
        global_admin = Usuario.get_by_id(default_username)
        if global_admin is None:
            roles_and_applicable_locations = {GLOBAL_ADMIN: None}
            return Usuario.create(id_client=None, username=default_username, password=default_password,
                                  roles_and_applicable_locations=roles_and_applicable_locations)

    @classmethod
    def load_current_user(cls, apply_timeout=True):
        """
        Load current user based on the result of get_current_user_data().
        """
        user_mixin = current_user
        if user_mixin == None or not user_mixin.is_authenticated:
            return None
        return user_mixin

    @classmethod
    def get_key_from(cls, username):
        return ndb.Key(Usuario, username)

    @classmethod
    def get_current_username(cls):
        user = cls.load_current_user()
        if user is None:
            return cls.ANONYMOUS
        else:
            return user.username
