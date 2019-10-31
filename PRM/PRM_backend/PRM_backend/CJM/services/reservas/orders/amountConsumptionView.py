# -*- coding: utf-8 -*
from flask import Blueprint

from CJM.entidades.reservas.orders.PersonConsumptionByAmount import PersonConsumptionByAmount
from CJM.services.reservas.reservaPersonaView import \
    get_reservation_and_person_reservation_on_namespace_with_permission
from commons.entidades.users import Role
from commons.utils import on_client_namespace
from commons.utils import with_json_bodyless
from commons.validations import validate_id_client

PERSON_AMOUNT_CONSUMPTIONS_VIEW_NAME = "person-amount-consumptions"
app = Blueprint(PERSON_AMOUNT_CONSUMPTIONS_VIEW_NAME, __name__)


@app.route('/clients/<int:id_client>/person-amount-consumptions/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_amount_consumptions(id_client):
    """
    Lista los consumos por cantidad existentes.
    :param id_client: id del cliente asociado
    :return: Lista de consumos existentes
    """
    id_client = validate_id_client(id_client)

    # noinspection PyUnusedLocal
    def list_amount_consumptions_on_namespace(id_current_client):
        return PersonConsumptionByAmount.list()
    return on_client_namespace(id_client,
                               list_amount_consumptions_on_namespace,
                               action=Role.READ_ACTION,
                               view=PERSON_AMOUNT_CONSUMPTIONS_VIEW_NAME)


@app.route('/clients/<int:id_client>/reservations/<int:id_reservation>/'
           'persons-reservations/<int:id_person_reservation>/person-amount-consumptions/',
           methods=['GET'], strict_slashes=False)
@with_json_bodyless
def list_amount_consumptions_by_person_reservation(id_client, id_reservation, id_person_reservation):
    """
    Lista los consumos por cantidad existentes para la reserva de persona dada.
    :param id_client: id del cliente asociado
    :param id_reservation: id de la reserva asociada
    :param id_person_reservation: id de la reserva de persona asociada
    :return: Lista de consumos existentes para la reserva de persona dada
    """
    id_client = validate_id_client(id_client)

    def list_amount_consumptions_by_person_reservation_on_namespace(id_current_client):
        reservation, person_reservation = get_reservation_and_person_reservation_on_namespace_with_permission(id_current_client,
                                                                                                              id_reservation,
                                                                                                              id_person_reservation,
                                                                                                              Role.READ_ACTION,
                                                                                                              PERSON_AMOUNT_CONSUMPTIONS_VIEW_NAME)
        return PersonConsumptionByAmount.list_by_ids_person_reservation(person_reservation.idReserva, person_reservation.key.id())
    return on_client_namespace(id_client,
                               list_amount_consumptions_by_person_reservation_on_namespace,
                               secured=False)
