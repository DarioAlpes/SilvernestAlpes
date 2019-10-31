"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var core_1 = require('@angular/core');
var event_1 = require('./event');
var event_details_component_1 = require('./event-details.component');
var event_service_1 = require('./event.service');
var router_1 = require('@angular/router');
var EventListComponent = (function () {
    function EventListComponent(router, route, eventService) {
        this.router = router;
        this.route = route;
        this.eventService = eventService;
        this.showEvent = false;
    }
    EventListComponent.prototype.onSelect = function (event) {
        var link = ['/clients', this.idClient, '/events', event.id];
        this.router.navigate(link);
    };
    EventListComponent.prototype.getEvents = function () {
        var _this = this;
        this.eventService
            .getEvents(this.idClient)
            .then(function (events) { return _this.events = events; })
            .catch(function (error) { return _this.error = error; });
    };
    EventListComponent.prototype.addEvent = function () {
        this.showEvent = true;
        this.selectedEvent = new event_1.Event();
    };
    EventListComponent.prototype.close = function (savedEvent) {
        this.showEvent = false;
        if (savedEvent) {
            this.getEvents();
        }
    };
    EventListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                _this.getEvents();
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    EventListComponent.prototype.goBack = function () {
        window.history.back();
    };
    EventListComponent = __decorate([
        core_1.Component({
            selector: 'event-list',
            templateUrl: 'app/templates/packages/events/event-list.html',
            directives: [event_details_component_1.EventDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, event_service_1.EventService])
    ], EventListComponent);
    return EventListComponent;
}());
exports.EventListComponent = EventListComponent;
//# sourceMappingURL=event-list.component.js.map