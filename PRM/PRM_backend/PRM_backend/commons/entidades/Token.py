# -*- coding: utf-8 -*
import datetime
import hashlib
import time

from google.appengine.ext import ndb


class Token(ndb.Model):
    idCliente = ndb.IntegerProperty(indexed=False)
    username = ndb.StringProperty(indexed=False)
    ultimaModificacion = ndb.DateTimeProperty(indexed=False, auto_now=True)

    @classmethod
    def crearToken(cls, usuario):
        token = usuario.username + '_' + usuario.password + '1241_AST12834tp131OLQD(O/FRJT#0it2w3442 k$q5gqf' \
                + str(time.time())
        token = hashlib.sha256(token.encode('utf-8')).hexdigest()
        retorno = Token(key=ndb.Key(Token, token), idCliente=usuario.idCliente, username=usuario.username)
        retorno.put()
        return retorno

    def delete(self):
        self.key.delete()

    def token(self):
        return self.key.id()

    @classmethod
    def get(cls, token):
        if token is None:
            return None
        if token.startswith('Token '):
            token = token[6:]
        key = ndb.Key(Token, token)
        ret = key.get()
        if ret is None:
            return None
        if ret.ultimaModificacion is None or (datetime.datetime.now() - ret.ultimaModificacion).total_seconds() > 60000:
            key.delete()
            return None
        else:
            ret.put()
            return ret
