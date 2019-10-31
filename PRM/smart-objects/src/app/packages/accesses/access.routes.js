"use strict";
var access_list_component_1 = require('./access-list.component');
var access_details_component_1 = require('./access-details.component');
exports.ACCESS_ROUTES = [
    {
        path: 'clients/:id_client/packages/:id_package/accesses',
        component: access_list_component_1.AccessListComponent
    },
    {
        path: 'clients/:id_client/packages/:id_package/accesses/:id',
        component: access_details_component_1.AccessDetailsComponent
    }
];
exports.ACCESS_ROUTES_COMPONENTS = [access_list_component_1.AccessListComponent, access_details_component_1.AccessDetailsComponent];
//# sourceMappingURL=access.routes.js.map