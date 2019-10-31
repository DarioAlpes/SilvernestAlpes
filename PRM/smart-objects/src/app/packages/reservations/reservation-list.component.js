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
var reservation_1 = require('./reservation');
var reservation_details_component_1 = require('./reservation-details.component');
var reservation_service_1 = require('./reservation.service');
var router_1 = require('@angular/router');
var ReservationListComponent = (function () {
    function ReservationListComponent(router, route, reservationService) {
        this.router = router;
        this.route = route;
        this.reservationService = reservationService;
        this.showReservation = false;
    }
    ReservationListComponent.prototype.onSelect = function (reservation) {
        var link = ['/clients', this.idClient, '/packages', this.idPackage, '/reservations', reservation.id];
        this.router.navigate(link);
    };
    ReservationListComponent.prototype.getReservations = function () {
        var _this = this;
        this.reservationService
            .getReservations(this.idClient, this.idPackage)
            .then(function (reservations) { return _this.reservations = reservations; })
            .catch(function (error) { return _this.error = error; });
    };
    ReservationListComponent.prototype.addReservation = function () {
        this.showReservation = true;
        this.selectedReservation = new reservation_1.Reservation();
    };
    ReservationListComponent.prototype.close = function (savedReservation) {
        this.showReservation = false;
        if (savedReservation) {
            this.getReservations();
        }
    };
    ReservationListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    _this.getReservations();
                }
                else {
                    _this.router.navigate(['/clients', _this.idClient, '/packages']);
                }
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    ReservationListComponent.prototype.goBack = function () {
        window.history.back();
    };
    ReservationListComponent = __decorate([
        core_1.Component({
            selector: 'reservation-list',
            templateUrl: 'app/templates/packages/reservations/reservation-list.html',
            directives: [reservation_details_component_1.ReservationDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, reservation_service_1.ReservationService])
    ], ReservationListComponent);
    return ReservationListComponent;
}());
exports.ReservationListComponent = ReservationListComponent;
//# sourceMappingURL=reservation-list.component.js.map