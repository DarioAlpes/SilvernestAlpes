# -*- coding: utf-8 -*
import ConfigParser
import os
import base64


def load_secret(secret_name="secret"):
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return base64.b64decode(config.get('Security', secret_name))


def is_prod():
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return config.get('Security', "environment") == "PROD"


def is_pre_prod():
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return config.get('Security', "environment") == "PRE_PROD"


def is_test():
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return config.get('Security', "environment") == "TEST"


def get_pass_compensar_orbita():
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return config.get('Security', "pass_compensar_orbita")


def get_pass_logs_mail():
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return config.get('Security', "pass_logs_mail")


def get_pass_compensar_cliente():
    config = ConfigParser.ConfigParser()
    config.read(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'app.config'))
    return config.get('Security', "pass_compensar_cliente")
