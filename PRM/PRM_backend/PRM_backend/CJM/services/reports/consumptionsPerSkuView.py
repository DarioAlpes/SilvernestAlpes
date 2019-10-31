# -*- coding: utf-8 -*
from flask import request, Blueprint

from CJM.entidades.reports.consumptions_by_skus.SkuConsumptionsReport import SkuConsumptionsReport
from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.entidades.reservas.orders.PersonConsumptionByMoney import PersonConsumptionByMoney
from CJM.services.validations import CONSUMPTIONS_PER_SKU_INVALID_INITIAL_TIME_ERROR_CODE, \
    CONSUMPTIONS_PER_SKU_INVALID_FINAL_TIME_ERROR_CODE
from commons.entidades.users import Role
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless
from commons.validations import validate_id_client, validate_datetime

CONSUMPTIONS_PER_SKU_VIEW_NAME = "consumptions-per-sku"
app = Blueprint(CONSUMPTIONS_PER_SKU_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/consumptions-per-sku/', methods=['GET'], strict_slashes=False)
@with_json_bodyless
def get_transactions_per_user(id_client):
    """
    Da las cantidades y dinero consumidos por sku
        Parametros esperados en el query string:
            initial-time: Fecha y hora inicial a filtrar, opcional
            final-time: Fecha y hora inicial a filtrar, opcional
    :param id_client: id del cliente asociado
    :return: Lista de transacciones por usuario
    """
    id_client = validate_id_client(id_client)

    initial_time = request.args.get(SkuConsumptionsReport.INITIAL_TIME_NAME)
    if initial_time is not None:
        initial_time = validate_datetime(initial_time, SkuConsumptionsReport.INITIAL_TIME_NAME, allow_none=False,
                                         internal_code=CONSUMPTIONS_PER_SKU_INVALID_INITIAL_TIME_ERROR_CODE)

    final_time = request.args.get(SkuConsumptionsReport.FINAL_TIME_NAME)
    if final_time is not None:
        final_time = validate_datetime(final_time, SkuConsumptionsReport.FINAL_TIME_NAME, allow_none=False,
                                       internal_code=CONSUMPTIONS_PER_SKU_INVALID_FINAL_TIME_ERROR_CODE)

    # noinspection PyUnusedLocal
    def get_money_and_amount_consumptions_on_namespace(id_current_client):
        return PersonConsumptionByMoney.list_between_dates(initial_time, final_time), \
               PersonConsumptionByAmount.list_between_dates(initial_time, final_time)

    money_consumptions, amount_consumptions = on_client_namespace(id_client,
                                                                  get_money_and_amount_consumptions_on_namespace,
                                                                  action=Role.READ_ACTION,
                                                                  view=CONSUMPTIONS_PER_SKU_VIEW_NAME)
    report = SkuConsumptionsReport()
            
    for money_consumption in money_consumptions:
        report.add_money_consumption(money_consumption)

    for amount_consumption in amount_consumptions:
        report.add_amount_consumption(amount_consumption)
    return report.to_list()
