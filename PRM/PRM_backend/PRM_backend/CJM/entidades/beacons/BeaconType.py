# -*- coding: utf-8 -*


class BeaconType(object):

    MOBILE_BEACON_TYPE = 1
    STATIC_BEACON_TYPE = 2
    STRING_MOBILE = "mobile"
    STRING_STATIC = "static"
    STRING_BEACON_TYPES = {MOBILE_BEACON_TYPE: STRING_MOBILE,
                           STATIC_BEACON_TYPE: STRING_STATIC}

    @staticmethod
    def beacon_type_to_string(beacon_type):
        return BeaconType.STRING_BEACON_TYPES[beacon_type]
