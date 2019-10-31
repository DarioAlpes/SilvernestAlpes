from CJM.entidades.eventos.Compra import Compra
from CJM.entidades.measures.measure_utils import extract_first_from_list, parse_to_int
from CJM.entidades.skus.SKU import SKU
from CJM.services.validations import validate_id_sku
from commons.validations import validate_datetime

INITIAL_TIME_NAME = "initial-time"
FINAL_TIME_NAME = "final-time"
SKU_ID_NAME = SKU.ID_SKU_NAME


def total_amount_purchased(id_person, **args):
    """
    Cantidad de unidades compradas por la persona del sku dado en el rango de tiempo dado
    Se supone que la persona existe
    :param id_person: Persona a calcular la medida
    :param args: Debe tener:
            id-sku: sku cuyas compras se quieren comprar
            initial-time: tiempo inicial desde el que se va a contar, opcional
            final-time: tiempo final desde el que se va a contar, opcional
    :return: cantidad de unidades compradas por la persona del sku dado en el rango de tiempo dado
    """
    id_sku = args.get(SKU_ID_NAME, None)
    if id_sku is not None:
        id_sku = extract_first_from_list(id_sku, SKU_ID_NAME, error_code=404)
        id_sku = parse_to_int(id_sku, SKU_ID_NAME, error_code=404)
    id_sku = validate_id_sku(id_sku)

    initial_time = args.get(INITIAL_TIME_NAME, None)
    if initial_time is not None:
        initial_time = extract_first_from_list(initial_time, INITIAL_TIME_NAME)
        initial_time = validate_datetime(initial_time, INITIAL_TIME_NAME)

    final_time = args.get(FINAL_TIME_NAME, None)
    if final_time is not None:
        final_time = extract_first_from_list(final_time, FINAL_TIME_NAME)
        final_time = validate_datetime(final_time, FINAL_TIME_NAME)

    purchases = Compra.get_purchases_of_sku_by_person_between_dates(id_person, id_sku, initial_time, final_time)

    if purchases is None:
        return 0
    else:
        return sum([sum([amount for (sku, amount)
                         in zip(purchase.idSkus, purchase.cantidades) if sku == id_sku])
                    for purchase in purchases])


def total_price_purchased(id_person, **args):
    """
    Precio total comprado por la persona del sku dado en el rango de tiempo dado
    Se supone que la persona existe
    :param id_person: Persona a calcular la medida
    :param args: Debe tener:
            id-sku: sku cuyas compras se quieren comprar
            initial-time: tiempo inicial desde el que se va a contar, opcional
            final-time: tiempo final desde el que se va a contar, opcional
    :return: precio total comprado por la persona del sku dado en el rango de tiempo dado
    """
    id_sku = args.get(SKU_ID_NAME, None)
    if id_sku is not None:
        id_sku = extract_first_from_list(id_sku, SKU_ID_NAME, error_code=404)
        id_sku = parse_to_int(id_sku, SKU_ID_NAME, error_code=404)
    id_sku = validate_id_sku(id_sku)

    initial_time = args.get(INITIAL_TIME_NAME, None)
    if initial_time is not None:
        initial_time = extract_first_from_list(initial_time, INITIAL_TIME_NAME)
        initial_time = validate_datetime(initial_time, INITIAL_TIME_NAME)

    final_time = args.get(FINAL_TIME_NAME, None)
    if final_time is not None:
        final_time = extract_first_from_list(final_time, FINAL_TIME_NAME)
        final_time = validate_datetime(final_time, FINAL_TIME_NAME)

    purchases = Compra.get_purchases_of_sku_by_person_between_dates(id_person, id_sku, initial_time, final_time)

    if purchases is None:
        return 0
    else:
        return sum([sum([amount * price for (sku, amount, price)
                         in zip(purchase.idSkus, purchase.cantidades, purchase.precios) if sku == id_sku])
                    for purchase in purchases])
