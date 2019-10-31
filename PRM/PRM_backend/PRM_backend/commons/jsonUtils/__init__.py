# -*- coding: utf-8 -*
from flask import jsonify, Response


def to_json(object_to_encode):
    if isinstance(object_to_encode, str) or isinstance(object_to_encode, unicode):
        return Response(object_to_encode, mimetype='application/json')
    elif isinstance(object_to_encode, dict):
        return jsonify(object_to_encode)
    elif hasattr(object_to_encode, '__iter__'):
        return jsonify([item.to_dict() for item in object_to_encode])
    else:
        return jsonify(object_to_encode.to_dict())
