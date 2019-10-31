# -*- coding: utf-8 -*


class TipoEvento(object):

    EVENT_TYPE_VISIT = 1
    STRING_VISIT = "Visit"
    EVENT_TYPE_PURCHASE = 2
    STRING_PURCHASE = "Purchase"
    EVENT_TYPE_FEEDBACK = 3
    STRING_FEEDBACK = "Feedback"
    EVENT_TYPE_ACTION = 4
    STRING_ACTION = "Action"
    EVENT_TYPE_ORDER = 5
    STRING_ORDER = "Order"
    EVENT_TYPE_REFUND = 6
    STRING_REFUND = "Refund"
    STRING_EVENT_TYPES = {EVENT_TYPE_VISIT: STRING_VISIT,
                          EVENT_TYPE_PURCHASE: STRING_PURCHASE,
                          EVENT_TYPE_FEEDBACK: STRING_FEEDBACK,
                          EVENT_TYPE_ACTION: STRING_ACTION,
                          EVENT_TYPE_ORDER: STRING_ORDER,
                          EVENT_TYPE_REFUND: STRING_REFUND}

    @classmethod
    def event_type_to_str(cls, event_type):
        return cls.STRING_EVENT_TYPES[event_type]
