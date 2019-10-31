# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from commons.entidades.users.Usuario import Usuario
from commons.validations import parse_datetime_to_string_on_default_format


class PastTransactionKey(ndb.Model):
    ACCESS_NAME = "access"
    ORDER_NAME = "order"
    MONEY_TOPOFF_NAME = "money-topoff"
    AMOUNT_TOPOFF_NAME = "amount-topoff"
    ACCESS_TOPOFF_NAME = "access-topoff"
    _SEPARATOR = " "

    @classmethod
    def create(cls, id_reservation, id_person_reservation, transaction_time, transaction_kind):
        new_transaction_key = cls.create_without_put(id_reservation, id_person_reservation, transaction_time,
                                                     transaction_kind)
        new_transaction_key.put()
        return new_transaction_key

    @classmethod
    def create_without_put(cls, id_reservation, id_person_reservation, transaction_time, transaction_kind):
        key = cls.get_key_from_fields(id_reservation, id_person_reservation, transaction_time, transaction_kind)
        new_transaction_key = PastTransactionKey(key=key)
        return new_transaction_key

    @classmethod
    def create_without_put_from_key(cls, key):
        new_transaction_key = PastTransactionKey(key=key)
        return new_transaction_key

    @classmethod
    def get_key_from_fields(cls, id_reservation, id_person_reservation, transaction_time, transaction_kind):
        str_key = Usuario.get_current_username() + cls._SEPARATOR + \
                  transaction_kind + cls._SEPARATOR + \
                  str(id_reservation) + cls._SEPARATOR + \
                  str(id_person_reservation) + cls._SEPARATOR + \
                  parse_datetime_to_string_on_default_format(transaction_time) + cls._SEPARATOR
        return ndb.Key(PastTransactionKey, str_key)
