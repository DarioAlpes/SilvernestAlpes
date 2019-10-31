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
var router_1 = require('@angular/router');
var event_1 = require('./event');
var event_service_1 = require('./event.service');
var router_2 = require('@angular/router');
var EventDetailsComponent = (function () {
    function EventDetailsComponent(eventService, route, router) {
        this.eventService = eventService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.event = new event_1.Event();
    }
    EventDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id'] !== undefined) {
                    _this.navigated = true;
                    var id = +params['id'];
                    _this.eventService.getEvent(_this.idClient, id)
                        .then(function (event) { return _this.event = event; })
                        .catch(function (error) { return _this.error = error; });
                }
                else {
                    _this.navigated = false;
                    _this.event = new event_1.Event();
                }
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    EventDetailsComponent.prototype.saveEvent = function () {
        var _this = this;
        this.eventService.saveEvent(this.idClient, this.event)
            .then(function (event) {
            _this.event = event;
            _this.goBack(event);
        })
            .catch(function (error) { return _this.error = error; });
    };
    EventDetailsComponent.prototype.goBack = function (savedEvent) {
        if (savedEvent === void 0) { savedEvent = null; }
        this.close.emit(savedEvent);
        if (this.navigated) {
            window.history.back();
        }
    };
    EventDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', event_1.Event)
    ], EventDetailsComponent.prototype, "event", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], EventDetailsComponent.prototype, "close", void 0);
    EventDetailsComponent = __decorate([
        core_1.Component({
            selector: 'event-detail',
            templateUrl: 'app/templates/packages/events/event-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [event_service_1.EventService, router_1.ActivatedRoute, router_1.Router])
    ], EventDetailsComponent);
    return EventDetailsComponent;
}());
exports.EventDetailsComponent = EventDetailsComponent;
//# sourceMappingURL=event-details.component.js.map