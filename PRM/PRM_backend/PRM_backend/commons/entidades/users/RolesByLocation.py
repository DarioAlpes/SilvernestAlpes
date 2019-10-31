# -*- coding: utf-8 -*
from google.appengine.ext import ndb


class RolesByLocation(ndb.Model):

    idUsuario = ndb.StringProperty(indexed=True)
    idCliente = ndb.IntegerProperty(indexed=True)
    idUbicacion = ndb.IntegerProperty(indexed=True)
    rol = ndb.StringProperty(indexed=True)

    @classmethod
    def create(cls, id_user, role, id_client, id_location):
        role_by_location = RolesByLocation(idUsuario=id_user,
                                           idUbicacion=id_location,
                                           idCliente=id_client,
                                           rol=role)
        role_by_location.put()
        return role_by_location

    @classmethod
    def list_by_username_and_client(cls, username, id_client):
        return RolesByLocation.query(ndb.AND(RolesByLocation.idUsuario == username,
                                             RolesByLocation.idCliente == id_client)).fetch()

    @classmethod
    def user_has_role_on_client(cls, id_user, id_client, role):
        try:
            # noinspection PyComparisonWithNone
            return RolesByLocation.query(ndb.AND(RolesByLocation.idUsuario == id_user,
                                                 RolesByLocation.rol == role,
                                                 RolesByLocation.idCliente == id_client)).get() is not None
        except ValueError:
            return False

    @classmethod
    def get_roles_by_user(cls, username, id_client):
        try:
            roles_by_location = dict()
            # noinspection PyComparisonWithNone
            roles_by_location_list = cls.list_by_username_and_client(username, id_client)
            for role_by_location in roles_by_location_list:
                if role_by_location.idUbicacion is None:
                    roles_by_location[role_by_location.rol] = None
                elif role_by_location.rol not in roles_by_location:
                    roles_by_location[role_by_location.rol] = {role_by_location.idUbicacion}
                elif roles_by_location[role_by_location.rol] is not None:
                    roles_by_location[role_by_location.rol].add(role_by_location.idUbicacion)
            return roles_by_location
        except ValueError:
            return dict()
