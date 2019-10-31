# -*- coding: utf-8 -*
import json


class EntityWithTime:
    ENTITY_NAME = "entity"
    OPERATION_TIME_NAME = "operation-time"
    ENTITY_DELETED_NAME = "entity-deleted"

    def __init__(self, operation_time, entity):
        self.operation_time = operation_time
        self.entity = entity

    def _to_dict_entity(self):
        entity_dict = self.entity.to_dict()
        entity_dict[self.ENTITY_DELETED_NAME] = self.entity.deleted()
        return entity_dict

    def to_dict(self):
        from commons.validations import parse_datetime_to_string_on_default_format
        fields_dict = dict()
        fields_dict[self.ENTITY_NAME] = self._to_dict_entity()
        fields_dict[self.OPERATION_TIME_NAME] = parse_datetime_to_string_on_default_format(self.operation_time)
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
