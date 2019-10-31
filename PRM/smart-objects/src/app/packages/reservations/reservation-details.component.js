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
var reservation_1 = require('./reservation');
var reservation_service_1 = require('./reservation.service');
var router_2 = require('@angular/router');
var ReservationDetailsComponent = (function () {
    function ReservationDetailsComponent(reservationService, route, router) {
        this.reservationService = reservationService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.reservation = new reservation_1.Reservation();
    }
    ReservationDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    if (params['id'] !== undefined) {
                        _this.navigated = true;
                        var id = +params['id'];
                        _this.reservationService.getReservation(_this.idClient, _this.idPackage, id)
                            .then(function (reservation) { return _this.reservation = reservation; })
                            .catch(function (error) { return _this.error = error; });
                    }
                    else {
                        _this.navigated = false;
                        _this.reservation = new reservation_1.Reservation();
                    }
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
    ReservationDetailsComponent.prototype.saveReservation = function () {
        var _this = this;
        this.reservationService.saveReservation(this.idClient, this.idPackage, this.reservation)
            .then(function (reservation) {
            _this.reservation = reservation;
            _this.goBack(reservation);
        })
            .catch(function (error) { return _this.error = error; });
    };
    ReservationDetailsComponent.prototype.goBack = function (savedReservation) {
        if (savedReservation === void 0) { savedReservation = null; }
        this.close.emit(savedReservation);
        if (this.navigated) {
            window.history.back();
        }
    };
    ReservationDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', reservation_1.Reservation)
    ], ReservationDetailsComponent.prototype, "reservation", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], ReservationDetailsComponent.prototype, "close", void 0);
    ReservationDetailsComponent = __decorate([
        core_1.Component({
            selector: 'reservation-detail',
            templateUrl: 'app/templates/packages/reservations/reservation-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [reservation_service_1.ReservationService, router_1.ActivatedRoute, router_1.Router])
    ], ReservationDetailsComponent);
    return ReservationDetailsComponent;
}());
exports.ReservationDetailsComponent = ReservationDetailsComponent;
//# sourceMappingURL=reservation-details.component.js.map