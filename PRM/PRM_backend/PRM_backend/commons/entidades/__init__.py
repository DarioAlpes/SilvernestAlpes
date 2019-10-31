# -*- coding: utf-8 -*
def calculate_next_string(string_to_advance):
    """
    Retorna una cadena mayor a la cadena dada por parámetro para usarla en busquedas por prefijo
    :param string_to_advance:
    :return:
    """
    return string_to_advance + u'\ufffd'
