"use strict";
var package_list_component_1 = require('./package-list.component');
var package_details_component_1 = require('./package-details.component');
var access_routes_1 = require('./accesses/access.routes');
var consumption_routes_1 = require('./consumptions/consumption.routes');
var event_routes_1 = require('./events/event.routes');
var reservation_routes_1 = require('./reservations/reservation.routes');
var PACKAGE_SPECIFIC_ROUTES = [
    {
        path: 'clients/:id_client/packages',
        component: package_list_component_1.PackageListComponent
    },
    {
        path: 'clients/:id_client/packages/:id',
        component: package_details_component_1.PackageDetailsComponent
    }
];
exports.PACKAGE_ROUTES = PACKAGE_SPECIFIC_ROUTES.concat(access_routes_1.ACCESS_ROUTES, event_routes_1.EVENT_ROUTES, consumption_routes_1.CONSUMPTION_ROUTES, reservation_routes_1.RESERVATION_ROUTES);
exports.PACKAGE_ROUTES_COMPONENTS = [package_list_component_1.PackageListComponent, package_details_component_1.PackageDetailsComponent]
    .concat(access_routes_1.ACCESS_ROUTES_COMPONENTS, event_routes_1.EVENT_ROUTES_COMPONENTS, consumption_routes_1.CONSUMPTION_ROUTES_COMPONENTS, reservation_routes_1.RESERVATION_ROUTES_COMPONENTS);
//# sourceMappingURL=package.routes.js.map