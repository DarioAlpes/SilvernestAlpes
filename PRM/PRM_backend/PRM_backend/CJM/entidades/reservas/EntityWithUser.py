# -*- coding: utf-8 -*
import json


class EntityWithUser:
    USERNAME_NAME = "username"

    def __init__(self, entity, children_names, single_children_names):
        self.entity = entity
        # TODO eliminar esta linea
        self.children = {child_name: [] for child_name in children_names}
        self.single_children = {child_name: None for child_name in single_children_names}

    def set_children(self, child_name, children):
        self.children[child_name] = children

    def set_single_child(self, child_name, child):
        self.single_children[child_name] = child

    def to_dict(self):
        fields_dict = self.entity.to_dict()
        for child_name, children in self.children.iteritems():
            fields_dict[child_name] = [child.to_dict() for child in children if child is not None]
        for child_name, child in self.single_children.iteritems():
            if child is None:
                fields_dict[child_name] = child
            else:
                fields_dict[child_name] = child.to_dict()
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())
