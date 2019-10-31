# -*- coding: utf-8 -*
class APIException(Exception):
    status_code = 400
    INTERNAL_ERROR_CODE_NAME = "internal-code"
    MESSAGE_NAME = "message"

    def __init__(self, message, status_code=None, internal_code=None):
        super(APIException, self).__init__(message)
        self.message = message
        if status_code is not None:
            self.status_code = status_code
        self.internal_code = internal_code

    def to_dict(self):
        rv = dict()
        rv[self.MESSAGE_NAME] = self.message
        rv[self.INTERNAL_ERROR_CODE_NAME] = self.internal_code
        return rv


class InternalError(APIException):
    def __init__(self, message):
        super(InternalError, self).__init__(message, 500)
        self.message = message


class ValidationError(APIException):
    def __init__(self, message, error_code=None, internal_code=None):
        if error_code is None:
            error_code = 400
        super(ValidationError, self).__init__(message, error_code, internal_code)
        self.message = message


class UnauthorizedError(APIException):
    def __init__(self, message=None, error_code=403):
        if message is None:
            message = u"User not authenticated."
        super(UnauthorizedError, self).__init__(message, error_code)
        self.message = message


class NotLoggedInError(APIException):
    def __init__(self, message=None, error_code=401):
        if message is None:
            message = u"Action requires login."
        super(NotLoggedInError, self).__init__(message, error_code)
        self.message = message


class InvalidDataFormat(APIException):
    def __init__(self, expected_format):
        message = u"Request is not in expected format ({0}).".format(expected_format)
        super(InvalidDataFormat, self).__init__(message, 415)
        self.message = message


class ResourceIsNotChild(APIException):
    def __init__(self, parent_id, parent_name, child_id, child_name):
        message = u"The entity {0} with id {1} does not have a son {2} with id {3}."\
            .format(parent_name, parent_id, child_name, child_id)
        super(ResourceIsNotChild, self).__init__(message, 404)
        self.message = message


class EntityDoesNotExists(APIException):
    def __init__(self, entity_name, internal_code=None):
        message = u"The entity ({0}) does not exists.".format(entity_name)
        super(EntityDoesNotExists, self).__init__(message, 404, internal_code)
        self.message = message


class EntityAlreadyExists(APIException):
    def __init__(self, entity_name, internal_code=None):
        message = u"The entity ({0}) already exists.".format(entity_name)
        super(EntityAlreadyExists, self).__init__(message, internal_code=internal_code)
        self.message = message
