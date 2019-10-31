"use strict";
var event_list_component_1 = require('./event-list.component');
var event_details_component_1 = require('./event-details.component');
exports.EVENT_ROUTES = [
    {
        path: 'clients/:id_client/events',
        component: event_list_component_1.EventListComponent
    },
    {
        path: 'clients/:id_client/events/:id',
        component: event_details_component_1.EventDetailsComponent
    }
];
exports.EVENT_ROUTES_COMPONENTS = [event_list_component_1.EventListComponent, event_details_component_1.EventDetailsComponent];
//# sourceMappingURL=event.routes.js.map