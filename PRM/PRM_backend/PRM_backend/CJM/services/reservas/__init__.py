from google.appengine.ext import ndb

from CJM.entidades.reservas.EntityWithUser import EntityWithUser

PERSONS_RESERVATIONS_KIND_NAME = "persons-reservations"
ACCESS_TOPOFFS_KIND_NAME = "access-topoffs"
AMOUNT_TOPOFFS_KIND_NAME = "amount-topoffs"
MONEY_TOPOFFS_KIND_NAME = "money-topoffs"
PERSON_ACCESSES_KIND_NAME = "person-accesses"
PERSON_ORDERS_KIND_NAME = "person-orders"
PERSON_AMOUNT_CONSUMPTIONS_KIND_NAME = "person-amount-consumptions"
PERSON_MONEY_CONSUMPTIONS_KIND_NAME = "person-money-consumptions"
PERSON_KIND_NAME = "person"


def get_map_function_from_entity_to_entity_with_user(children_names, single_children_names):

    @ndb.tasklet
    def map_entity_to_entity_with_user(entity):
        entity_with_user = EntityWithUser(entity, children_names, single_children_names)
        raise ndb.Return(entity_with_user)
    return map_entity_to_entity_with_user
