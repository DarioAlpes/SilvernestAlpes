# -*- coding: utf-8 -*
import json
import os
import base64
from Crypto.Cipher import AES
from Crypto import Random

from google.appengine.ext import ndb

from commons.entidades.Generador import Generador
from config_loader import load_secret


class ClientKey(ndb.Model):
    IDS_GENERATORS = {Generador.ID_PRODUCTS,
                      Generador.ID_LOCATIONS,
                      Generador.ID_TRANSACTIONS,
                      Generador.ID_PERSONS,
                      Generador.ID_SKUS,
                      Generador.ID_EVENTS,
                      Generador.ID_SENSORS,
                      Generador.ID_BEACONS}
    ID_CLIENT_NAME = "id-client"
    TAG_KEY_NAME = "tag-key"
    KEY_LENGTH = 128

    llave = ndb.StringProperty()

    @classmethod
    def create_without_put(cls):
        cipher = _AESCipher(load_secret("aes_key"))
        generated_key = os.urandom(cls.KEY_LENGTH)
        client_key = ClientKey(llave=cipher.encrypt(generated_key))
        return client_key

    @classmethod
    def create(cls):
        client_key = cls.create_without_put()
        client_key.put()
        return client_key

    @classmethod
    def list(cls):
        return ClientKey.query().fetch()

    def to_dict(self):
        cipher = _AESCipher(load_secret("aes_key"))
        decrypted_key = cipher.decrypt(self.llave)
        bytes_key = bytearray(decrypted_key)

        fields_dict = dict()
        fields_dict[ClientKey.TAG_KEY_NAME] = "".join(['{:02x}'.format(byte_in_key) for byte_in_key in bytes_key])
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())


class _AESCipher:
    BS = 16

    def __init__(self, key):
        self.key = key

    @classmethod
    def pad(cls, raw):
        return raw + (cls.BS - len(raw) % cls.BS) * chr(cls.BS - len(raw) % cls.BS)

    @classmethod
    def unpad(cls, padded):
        return padded[:-ord(padded[len(padded)-1:])]

    def encrypt(self, raw):
        raw = _AESCipher.pad(raw)
        iv = Random.new().read(AES.block_size)
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return base64.b64encode(iv + cipher.encrypt(raw))

    def decrypt(self, encrypted):
        encrypted = base64.b64decode(encrypted)
        iv = encrypted[:AES.block_size]
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return _AESCipher.unpad(cipher.decrypt(encrypted[AES.block_size:]))
