# -*- coding: utf-8 -*
def format_hex(numero, longitud=1):
    return format(numero, 'X').rjust(longitud, '0')


def calculate_length(bits):
    return (bits-96)/8


def get_length(longitud):
    return int(longitud, 16)*8 + 96


class EPC(object):
    TIPO_ITEM = 1
    TIPO_UBICACION = 2
    TIPO_ACTIVO = 3

    @staticmethod
    def get_epc_item(id_client, id_product, id_item):
        hex_version = format_hex(1)                       # 1
        hex_id_client = format_hex(id_client, 4)          # 4
        hex_type = format_hex(EPC.TIPO_ITEM)              # 1
        hex_id_product = format_hex(id_product, 6)        # 6
        hex_id_item = format_hex(id_item, 12)              # 12
        return hex_version + hex_id_client + hex_type + hex_id_product + hex_id_item

    @staticmethod
    def get_epc_location(id_client, id_location):
        hex_version = format_hex(1)                       # 1
        hex_id_client = format_hex(id_client, 4)          # 4
        hex_type = format_hex(EPC.TIPO_UBICACION)         # 1
        hex_id_location = format_hex(id_location, 18)     # 18
        return hex_version + hex_id_client + hex_type + hex_id_location



