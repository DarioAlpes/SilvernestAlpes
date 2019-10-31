# -*- coding: utf-8 -*
import json
import collections
import logging

from google.appengine.ext import ndb
from flask import Response

from commons.validations import validate_datetime


class SuccessLog(ndb.Model):
    SEPARATOR = ";" * 5
    ANONYMOUS = "Anonymous"
    METHOD = "Method:"
    USER = "Username:"
    ID_CLIENT = "Id client:"
    URL = "Url:"
    RESULT = "Result:"
    BODY = "Body:"
    METHOD_CREATE_ENTITIES = "POST"
    entities_keys = ndb.KeyProperty(indexed=True, repeated=True)
    kinds = ndb.StringProperty(indexed=True, repeated=True)
    id_client = ndb.IntegerProperty(indexed=True)
    user_key = ndb.KeyProperty(indexed=True)
    server_date = ndb.DateTimeProperty(indexed=True)
    method = ndb.StringProperty(indexed=True)
    json_result = ndb.TextProperty()
    json_payload = ndb.TextProperty()
    url = ndb.StringProperty(indexed=True)

    # noinspection PyUnresolvedReferences
    @classmethod
    def create(cls, entities, json_entity, id_client, user_before, user_after, method, url, payload):
        log_entity = cls.create_without_put(entities, json_entity, id_client, user_before, user_after, method, url,
                                            payload)
        log_entity.put()
        return log_entity

    @classmethod
    def create_without_put(cls, entities, json_entity, id_client, user_before, user_after, method, url, payload):
        entities_keys = []
        if isinstance(entities, ndb.Model):
            entities_keys = [entities.key]
        elif isinstance(entities, collections.Iterable) and all([isinstance(entity, ndb.Model) for entity in entities]):
            entities_keys = [entity.key for entity in entities]
        kinds = {entity_keys.kind() for entity_keys in entities_keys}

        user_key = None
        if user_before is not None and not user_before.is_anonymous:
            user_key = user_before.key
            if user_before.idCliente is not None:
                id_client = user_before.idCliente
        if user_key is None and user_after is not None and not user_after.is_anonymous:
            user_key = user_after.key
            if user_after.idCliente is not None:
                id_client = user_after.idCliente
        if json_entity is not None:
            if isinstance(json_entity, Response):
                json_entity = unicode(json_entity.data)
            json_entity = (json_entity[:1000] + '..') if len(json_entity) > 10000 else json_entity
        if payload is not None:
            payload = (payload[:1000] + '..') if len(payload) > 10000 else payload
        log_entity = SuccessLog(entities_keys=entities_keys,
                                id_client=id_client,
                                user_key=user_key,
                                server_date=validate_datetime(None, None, allow_none=True),
                                method=method,
                                json_result=json_entity,
                                json_payload=payload,
                                url=url,
                                kinds=kinds)
        if user_key is None:
            user_str = cls.ANONYMOUS
        else:
            user_str = user_key.id()
        dict_info = {cls.URL: url,
                     cls.METHOD: method,
                     cls.USER: user_str,
                     cls.ID_CLIENT: id_client,
                     cls.RESULT: json_entity}
        logging.info(json.dumps(dict_info))
        return log_entity

    @classmethod
    def log_request_values(cls, id_client, user, method, url, payload):
        user_key = None
        if user is not None and not user.is_anonymous:
            user_key = user.key
            if user.idCliente is not None:
                id_client = user.idCliente
        if user_key is None:
            user_str = cls.ANONYMOUS
        else:
            user_str = user_key.id()
        dict_info = {cls.URL: url,
                     cls.METHOD: method,
                     cls.USER: user_str,
                     cls.ID_CLIENT: id_client,
                     cls.BODY: payload}
        logging.info(json.dumps(dict_info))

    @classmethod
    def list(cls):
        return SuccessLog.query().fetch()

    @classmethod
    def count(cls):
        return SuccessLog.query().count()

    @classmethod
    def get_by_client_and_methods_and_between_dates(cls, id_client, kinds, methods, initial_time, final_time):
        query_filter = ndb.AND(SuccessLog.id_client == id_client, SuccessLog.method.IN(methods),
                               SuccessLog.kinds.IN(kinds))
        if initial_time is not None:
            query_filter = ndb.AND(query_filter, SuccessLog.server_date >= initial_time)
        if final_time is not None:
            query_filter = ndb.AND(query_filter, SuccessLog.server_date <= final_time)
        return SuccessLog.query(query_filter)

    @classmethod
    def get_user_for_creation_by_client_and_entity_key_async(cls, id_client, key):
        return cls.query(cls.id_client == id_client)\
            .filter(cls.method == cls.METHOD_CREATE_ENTITIES)\
            .filter(cls.entities_keys == key)\
            .get_async(projection=[cls.user_key])
