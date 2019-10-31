# -*- coding: utf-8 -*
from google.appengine.ext import ndb

from commons.entidades import calculate_next_string
from commons.entidades.Generador import Generador
from commons.excepciones.apiexceptions import ValidationError
from commons.utils import update_descendants_key


class CategoriaSKU(ndb.Model):
    SKU_CATEGORY_NAME_NAME = "name"
    PARENT_NAME = "id-parent-category"
    KEY_NAME = "key"
    ID_NAME = "id"
    ID_SKU_CATEGORY_NAME = "id-sku-category"
    ID_SKU_CATEGORIES_NAME = "id-sku-categories"

    llave = ndb.StringProperty(indexed=True)
    nombre = ndb.StringProperty()
    idPadre = ndb.IntegerProperty(indexed=True)

    def to_dict(self):
        fields_dict = dict()
        if self.idPadre is not None:
            fields_dict[self.PARENT_NAME] = self.idPadre
        fields_dict[self.ID_NAME] = self.key.id()
        fields_dict[self.SKU_CATEGORY_NAME_NAME] = self.nombre
        fields_dict[self.KEY_NAME] = self.llave
        return fields_dict

    @classmethod
    def create(cls, name, category_parent_id):
        id_category = Generador.get_next_sku_category_id()
        new_category = CategoriaSKU(key=ndb.Key(CategoriaSKU, id_category),
                                    nombre=name)
        new_category.idPadre = category_parent_id
        new_category.update_key_by_parent()
        new_category.put()
        return new_category

    @classmethod
    def list(cls):
        return CategoriaSKU.query().fetch()

    def get_hierarchy_ids(self):
        return [int(str_id) for str_id in self.llave.split(":")[:-1]]

    @classmethod
    def list_by_parent_id(cls, category_parent_id):
        if category_parent_id is None:
            return list()
        else:
            parent = CategoriaSKU.get_by_id(category_parent_id)
            next_key = calculate_next_string(parent.llave)
            return CategoriaSKU.query(ndb.AND(CategoriaSKU.llave >= parent.llave,
                                              CategoriaSKU.llave < next_key)).fetch()

    @classmethod
    def update(cls, id_sku_category, name, id_parent):
        sku_category = CategoriaSKU.get_by_id(id_sku_category)
        original_parent = sku_category.idPadre
        sku_category.idPadre = id_parent
        try:
            from CJM.services.validations import SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE
            categories_to_update = update_descendants_key(cls, sku_category,
                                                          internal_code=SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE)
        except ValidationError:
            from CJM.services.validations import SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE
            sku_category.idPadre = original_parent
            raise ValidationError(u"Can not change the parent of category {0} to {1} because it creates a cicle"
                                  u" on the hirearchy"
                                  .format(id_sku_category, id_parent),
                                  internal_code=SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE)
        sku_category.nombre = name
        ndb.put_multi(categories_to_update)
        return sku_category

    def update_key_by_parent(self, parent=None):
        if self.idPadre is None:
            self.llave = str(self.key.id()) + ':'
        else:
            if parent is None:
                parent = CategoriaSKU.get_by_id(self.idPadre)
            if (':' + str(self.key.id()) + ':') in (':' + parent.llave):
                from CJM.services.validations import SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE
                raise ValidationError(u"The sku category {0} is ancestor of the sku category {1}. Can not create "
                                      u"cicles on the hirearchy".format(self.key.id(), self.idPadre),
                                      internal_code=SKU_CATEGORY_INVALID_HIREARCHY_NAME_ERROR_CODE)
            self.llave = parent.llave + str(self.key.id()) + ':'
