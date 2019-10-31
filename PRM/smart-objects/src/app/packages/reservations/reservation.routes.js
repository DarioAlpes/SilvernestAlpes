"use strict";
var reservation_list_component_1 = require('./reservation-list.component');
var reservation_details_component_1 = require('./reservation-details.component');
var person_reservation_routes_1 = require('./persons-reservations/person-reservation.routes');
var RESERVATION_SPECIFIC_ROUTES = [
    {
        path: 'clients/:id_client/packages/:id_package/reservations',
        component: reservation_list_component_1.ReservationListComponent
    },
    {
        path: 'clients/:id_client/packages/:id_package/reservations/:id',
        component: reservation_details_component_1.ReservationDetailsComponent
    }
];
exports.RESERVATION_ROUTES = RESERVATION_SPECIFIC_ROUTES.concat(person_reservation_routes_1.PERSON_RESERVATION_ROUTES);
exports.RESERVATION_ROUTES_COMPONENTS = [reservation_list_component_1.ReservationListComponent, reservation_details_component_1.ReservationDetailsComponent]
    .concat(person_reservation_routes_1.PERSON_RESERVATION_ROUTES_COMPONENTS);
//# sourceMappingURL=reservation.routes.js.map