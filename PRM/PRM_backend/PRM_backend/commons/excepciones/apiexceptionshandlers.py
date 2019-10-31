# -*- coding: utf-8 -*
from flask import jsonify
import logging


def handle_api_exception(error):
    error_dictionary = error.to_dict()
    logging.error(str(error_dictionary))
    response = jsonify(error_dictionary)
    response.status_code = error.status_code
    return response
