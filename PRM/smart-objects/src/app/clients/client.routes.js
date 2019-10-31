"use strict";
var client_list_component_1 = require('./client-list.component');
var client_details_component_1 = require('./client-details.component');
exports.CLIENT_ROUTES = [
    {
        path: 'clients',
        component: client_list_component_1.ClientListComponent
    },
    {
        path: 'clients/:id',
        component: client_details_component_1.ClientDetailsComponent
    }
];
exports.CLIENT_ROUTES_COMPONENTS = [client_list_component_1.ClientListComponent, client_details_component_1.ClientDetailsComponent];
//# sourceMappingURL=client.routes.js.map