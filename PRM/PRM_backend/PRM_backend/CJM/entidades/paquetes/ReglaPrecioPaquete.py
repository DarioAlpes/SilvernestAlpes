# -*- coding: utf-8 -*
import json
from google.appengine.ext import ndb

from CJM.entidades.paquetes.Paquete import Paquete
from CJM.services.validations import PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_ERROR_CODE, \
    PACKAGE_PRICE_RULE_INVALID_DUPLICATED_RULE_ERROR_CODE, CATEGORY_PROPERTY_NAME, AGE_GROUP_PROPERTY_NAME
from commons.entidades.locations.Ubicacion import Ubicacion
from commons.excepciones.apiexceptions import EntityDoesNotExists, ValidationError


class _ReglaPrecioPaqueteCompartido(ndb.Model):
    @classmethod
    def create(cls, package):
        new_rule = _ReglaPrecioPaqueteCompartido(parent=package.get_shared_key())
        new_rule.put()
        return new_rule


class ReglaPrecioPaquete(ndb.Model):
    ID_NAME = "id"
    HISTORIC_ID_NAME = "historic-id"
    BASE_PRICE_NAME = "base-price"
    TAX_RATE_NAME = "tax-rate"
    RULES_NAME = "rules"
    PROPERTY_NAME = "property"
    VALUE_NAME = "value"
    ACTIVE_NAME = "active"

    HISTORIC_PACKAGE_ID_NAME = Paquete.ID_PACKAGE_HISTORIC_NAME
    PACKAGE_ID_NAME = Paquete.ID_PACKAGE_NAME
    LOCATION_ID_NAME = Ubicacion.ID_UBICACION_NAME

    idCliente = ndb.IntegerProperty()
    idPaquete = ndb.IntegerProperty(indexed=True)
    idPaqueteCompartido = ndb.IntegerProperty(indexed=True)
    idCompartido = ndb.IntegerProperty(indexed=True)
    activo = ndb.BooleanProperty(indexed=True)
    precio = ndb.FloatProperty(indexed=True)
    impuesto = ndb.FloatProperty(indexed=True)

    @classmethod
    def create(cls, package, tax_rate, price, rule_restrictions):
        shared_rule = _ReglaPrecioPaqueteCompartido.create(package)
        id_rule = shared_rule.key.id()

        new_rule = ReglaPrecioPaquete(idCompartido=id_rule,
                                      idPaquete=package.key.id(),
                                      idPaqueteCompartido=package.idCompartido,
                                      precio=price,
                                      impuesto=tax_rate,
                                      activo=True,
                                      parent=shared_rule.key)

        new_rule.put()

        restrictions = [_RestriccionRegla.create_without_put(new_rule,
                                                             rule_restriction[cls.PROPERTY_NAME],
                                                             rule_restriction[cls.VALUE_NAME])
                        for rule_restriction in rule_restrictions]
        ndb.put_multi(restrictions)
        return _RulesDTO(new_rule, restrictions)

    def clone(self, new_package):
        return ReglaPrecioPaquete(idCompartido=self.idCompartido,
                                  idPaquete=new_package.key.id(),
                                  idPaqueteCompartido=self.idPaqueteCompartido,
                                  precio=self.precio,
                                  impuesto=self.impuesto,
                                  activo=True,
                                  parent=self.get_shared_key(new_package))

    @classmethod
    def get_by_package_and_fields_values(cls, package, category, age_group):
        rules = cls.list_by_package(package)

        applicable_rules = [rule for rule in rules if rule.is_applicable(category, age_group)]

        if len(applicable_rules) > 0:
            applicable_rules.sort(key=lambda current_rule: len(current_rule.restrictions), reverse=True)
            return applicable_rules[0].rule
        else:
            return None

    def get_base_price_and_tax_rate(self):
        return self.precio, self.impuesto

    def get_shared_key(self, package):
        return self._get_shared_key(package, self.idCompartido)

    @classmethod
    def _get_shared_key(cls, package, shared_id):
        return ndb.Key(_ReglaPrecioPaqueteCompartido, shared_id, parent=package.get_shared_key())

    @classmethod
    def get_by_package(cls, package, id_rule):
        try:
            rule = ReglaPrecioPaquete.query(ancestor=ReglaPrecioPaquete._get_shared_key(package, id_rule)) \
                .filter(ReglaPrecioPaquete.idPaquete == package.key.id()).get()
        except ValueError:
            rule = None
        if rule is None:
            raise EntityDoesNotExists(u"Price Rule[{0}]".format(id_rule),
                                      internal_code=PACKAGE_PRICE_RULE_DOES_NOT_EXISTS_ERROR_CODE)
        else:
            return _RulesDTO(rule, _RestriccionRegla.list_by_rule(rule))

    @classmethod
    def _list_by_package_raw(cls, package):
        return ReglaPrecioPaquete.query(ancestor=package.get_shared_key()).filter(ReglaPrecioPaquete.idPaquete == package.key.id())

    @classmethod
    def check_if_already_exists_overlapping_rule(cls, package, new_rules_restrictions):
        new_restrictions_set = {(restriction[cls.PROPERTY_NAME], restriction[cls.VALUE_NAME])
                                for restriction in new_rules_restrictions}
        existing_rules = cls.list_by_package(package)
        for rule in existing_rules:
            if rule.restricts_over_the_same_values(new_restrictions_set):
                raise ValidationError(u"The rule with id {0} restricts over the same values than the new rule".format(rule.rule.idCompartido),
                                      internal_code=PACKAGE_PRICE_RULE_INVALID_DUPLICATED_RULE_ERROR_CODE)

    @classmethod
    def clone_package_rules(cls, old_package, new_package):
        rules_dtos = ReglaPrecioPaquete.list_by_package(old_package)
        old_rules = []
        new_rules = []
        for rule_dto in rules_dtos:
            rule_dto.rule.activo = False
            old_rules.append(rule_dto.rule)
            new_rules.append(rule_dto.rule.clone(new_package))
        ndb.put_multi(old_rules + new_rules)
        new_restrictions = []
        for index, rule_dto in enumerate(rules_dtos):
            new_restrictions += rule_dto.clone_restrictions_without_put(new_rules[index])
        ndb.put_multi(new_restrictions)

    @classmethod
    def list_by_package(cls, package):
        rules = cls._list_by_package_raw(package)
        restrictions_by_id = dict()
        rules_restrictions = _RestriccionRegla.list_by_parent_key(package.get_shared_key()).fetch()
        for restriction in rules_restrictions:
            rule_key = restriction.get_rule_key()
            if rule_key not in restrictions_by_id:
                restrictions_by_id[rule_key] = list()
            restrictions_by_id[rule_key].append(restriction)
        return [_RulesDTO(rule, restrictions_by_id[rule.key]) for rule in rules]

    def to_dict(self):
        fields_dict = dict()
        fields_dict[ReglaPrecioPaquete.HISTORIC_ID_NAME] = self.key.id()
        fields_dict[ReglaPrecioPaquete.ID_NAME] = self.idCompartido
        fields_dict[ReglaPrecioPaquete.PACKAGE_ID_NAME] = self.idPaqueteCompartido
        fields_dict[ReglaPrecioPaquete.HISTORIC_PACKAGE_ID_NAME] = self.idPaquete
        fields_dict[ReglaPrecioPaquete.ACTIVE_NAME] = self.activo
        fields_dict[ReglaPrecioPaquete.BASE_PRICE_NAME] = self.precio
        fields_dict[ReglaPrecioPaquete.TAX_RATE_NAME] = self.impuesto
        return fields_dict


class _RulesDTO:
    def __init__(self, rule, restrictions):
        self.rule = rule
        self.restrictions = restrictions

    def to_dict(self):
        fields_dict = self.rule.to_dict()
        fields_dict[ReglaPrecioPaquete.RULES_NAME] = [restriction.to_dict() for restriction in self.restrictions]
        return fields_dict

    def to_json(self):
        return json.dumps(self.to_dict())

    def restricts_over_the_same_values(self, new_restrictions_set):
        restrictions_set = {(restriction.propiedad, restriction.valor) for restriction in self.restrictions}
        return restrictions_set == new_restrictions_set

    def clone_restrictions_without_put(self, new_rule):
        return [restriction.clone(new_rule) for restriction in self.restrictions]

    def delete(self):
        self.restrictions = self.restrictions.fetch()
        ndb.delete_multi([self.rule.key] + [restriction.key for restriction in self.restrictions])

    def is_applicable(self, category, age_group):
        matches_category = self._check_restrictions_by_name_and_value(CATEGORY_PROPERTY_NAME, category)
        matches_age_group = self._check_restrictions_by_name_and_value(AGE_GROUP_PROPERTY_NAME, age_group)
        return matches_category and matches_age_group

    def _check_restrictions_by_name_and_value(self, name, value):
        has_restriction = any(restriction.propiedad == name
                              for restriction in self.restrictions)
        matches_value = any(restriction.propiedad == name and restriction.valor == value
                            for restriction in self.restrictions)
        return (not has_restriction) or matches_value


class _RestriccionRegla(ndb.Model):
    propiedad = ndb.StringProperty(indexed=True)
    valor = ndb.StringProperty(indexed=True)

    @classmethod
    def create_without_put(cls, rule, property_name, value):
        return _RestriccionRegla(propiedad=property_name,
                                 valor=value,
                                 parent=rule.key)

    def clone(self, new_rule):
        return self.create_without_put(new_rule,
                                       self.propiedad,
                                       self.valor)

    def get_rule_key(self):
        return self.key.parent()

    @classmethod
    def create(cls, rule, property_name, value):
        new_restriction = cls.create_without_put(rule, property_name, value)
        new_restriction.put()
        return new_restriction

    @classmethod
    def list_by_parent_key(cls, parent_key):
        return _RestriccionRegla.query(ancestor=parent_key)

    @classmethod
    def list_by_rule(cls, rule):
        return cls.list_by_parent_key(rule.key)

    def to_dict(self):
        fields_dict = dict()
        fields_dict[ReglaPrecioPaquete.PROPERTY_NAME] = self.propiedad
        fields_dict[ReglaPrecioPaquete.VALUE_NAME] = self.valor
        return fields_dict
