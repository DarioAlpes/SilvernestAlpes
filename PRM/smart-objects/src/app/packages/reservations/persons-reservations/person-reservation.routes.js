"use strict";
var person_reservation_list_component_1 = require('./person-reservation-list.component');
var person_reservation_details_component_1 = require('./person-reservation-details.component');
var person_consumption_routes_1 = require('./person-consumptions/person-consumption.routes');
var PERSON_RESERVATION_SPECIFIC_ROUTES = [
    {
        path: 'clients/:id_client/packages/:id_package/persons-reservations',
        component: person_reservation_list_component_1.PersonReservationListComponent
    },
    {
        path: 'clients/:id_client/packages/:id_package/reservations/:id_reservation/persons-reservations/:id',
        component: person_reservation_details_component_1.PersonReservationDetailsComponent
    }
];
exports.PERSON_RESERVATION_ROUTES = PERSON_RESERVATION_SPECIFIC_ROUTES.concat(person_consumption_routes_1.PERSON_CONSUMPTION_ROUTES);
exports.PERSON_RESERVATION_ROUTES_COMPONENTS = [person_reservation_list_component_1.PersonReservationListComponent, person_reservation_details_component_1.PersonReservationDetailsComponent]
    .concat(person_consumption_routes_1.PERSON_CONSUMPTION_ROUTES_COMPONENTS);
//# sourceMappingURL=person-reservation.routes.js.map