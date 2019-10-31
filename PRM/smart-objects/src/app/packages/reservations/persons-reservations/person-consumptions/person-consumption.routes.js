"use strict";
var person_consumption_list_component_1 = require('./person-consumption-list.component');
var person_consumption_details_component_1 = require('./person-consumption-details.component');
exports.PERSON_CONSUMPTION_ROUTES = [
    {
        path: 'clients/:id_client/packages/:id_package/reservations/:id_reservation/persons-reservations/:id_person_reservation/person-consumptions',
        component: person_consumption_list_component_1.PersonConsumptionListComponent
    },
    {
        path: 'clients/:id_client/packages/:id_package/reservations/:id_reservation/persons-reservations/:id_person_reservation/person-consumptions/:id',
        component: person_consumption_details_component_1.PersonConsumptionDetailsComponent
    }
];
exports.PERSON_CONSUMPTION_ROUTES_COMPONENTS = [person_consumption_list_component_1.PersonConsumptionListComponent, person_consumption_details_component_1.PersonConsumptionDetailsComponent];
//# sourceMappingURL=person-consumption.routes.js.map