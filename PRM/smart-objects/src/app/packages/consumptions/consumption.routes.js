"use strict";
var consumption_list_component_1 = require('./consumption-list.component');
var consumption_details_component_1 = require('./consumption-details.component');
exports.CONSUMPTION_ROUTES = [
    {
        path: 'clients/:id_client/packages/:id_package/consumptions',
        component: consumption_list_component_1.ConsumptionListComponent
    },
    {
        path: 'clients/:id_client/packages/:id_package/consumptions/:id',
        component: consumption_details_component_1.ConsumptionDetailsComponent
    }
];
exports.CONSUMPTION_ROUTES_COMPONENTS = [consumption_list_component_1.ConsumptionListComponent, consumption_details_component_1.ConsumptionDetailsComponent];
//# sourceMappingURL=consumption.routes.js.map