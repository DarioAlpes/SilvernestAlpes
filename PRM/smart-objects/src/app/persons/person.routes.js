"use strict";
var person_list_component_1 = require('./person-list.component');
var person_details_component_1 = require('./person-details.component');
exports.PERSON_ROUTES = [
    {
        path: 'clients/:id_client/persons',
        component: person_list_component_1.PersonListComponent
    },
    {
        path: 'clients/:id_client/persons/:id',
        component: person_details_component_1.PersonDetailsComponent
    }
];
exports.PERSON_ROUTES_COMPONENTS = [person_list_component_1.PersonListComponent, person_details_component_1.PersonDetailsComponent];
//# sourceMappingURL=person.routes.js.map