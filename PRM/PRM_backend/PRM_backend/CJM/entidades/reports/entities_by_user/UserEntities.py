# -*- coding: utf-8 -*
import json

import itertools

from CJM.entidades.reports.entities_by_user.EntityWithTime import EntityWithTime
from commons.entidades.logs.SuccessLog import SuccessLog
from commons.entidades.users.Usuario import Usuario


class UserEntities:
    USERNAME_NAME = Usuario.USERNAME_NAME
    ENTITIES_NAME = "entities"
    KIND_NAME = "kind"
    INCLUDE_DELETED_NAME = "include-deleted"
    INITIAL_TIME_NAME = "initial-time"
    FINAL_TIME_NAME = "final-time"

    def __init__(self, user_key):
        if user_key is None:
            self.username = SuccessLog.ANONYMOUS
        else:
            self.username = user_key.id()
        self.entities = []
        self.total = 0

    def add_entities(self, log, entities, include_deleted):
        self.entities = itertools.chain(self.entities, (EntityWithTime(log.server_date, entity) for entity in entities
                                                        if entity is not None and (include_deleted or not entity.deleted())))

    def to_dict(self):
        fields_dict = dict()
        fields_dict[self.USERNAME_NAME] = self.username
        self.entities = list(self.entities)
        self.entities.sort(key=lambda current_entity: current_entity.operation_time)
        fields_dict[self.ENTITIES_NAME] = [entity.to_dict() for entity in self.entities]
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
