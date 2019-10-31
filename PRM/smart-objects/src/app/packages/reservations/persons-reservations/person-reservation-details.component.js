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
var reservation_service_1 = require('../reservation.service');
var person_service_1 = require('../../../persons/person.service');
var person_reservation_1 = require('./person-reservation');
var person_reservation_service_1 = require('./person-reservation.service');
var router_2 = require('@angular/router');
var PersonReservationDetailsComponent = (function () {
    function PersonReservationDetailsComponent(personReservationService, reservationService, personService, route, router) {
        this.personReservationService = personReservationService;
        this.reservationService = reservationService;
        this.personService = personService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.personReservation = new person_reservation_1.PersonReservation();
    }
    PersonReservationDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    if ((params['id_reservation'] !== undefined) && (params['id'] !== undefined)) {
                        _this.navigated = true;
                        _this.idReservation = +params['id_reservation'];
                        var id = +params['id'];
                        _this.personReservationService.getPersonReservation(_this.idClient, _this.idPackage, _this.idReservation, id)
                            .then(function (personReservation) { return _this.personReservation = personReservation; })
                            .catch(function (error) { return _this.error = error; });
                    }
                    else {
                        _this.navigated = false;
                        _this.personReservation = new person_reservation_1.PersonReservation();
                    }
                    _this.reservationService.getReservations(_this.idClient, _this.idPackage)
                        .then(function (reservations) { return _this.reservations = reservations; })
                        .catch(function (error) { return _this.error = error; });
                    _this.personService.getPersons(_this.idClient)
                        .then(function (persons) { return _this.persons = persons.filter(function (person) { return !person['is-phantom']; }); })
                        .catch(function (error) { return _this.error = error; });
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
    PersonReservationDetailsComponent.prototype.savePersonReservation = function () {
        var _this = this;
        this.personReservationService.savePersonReservation(this.idClient, this.idPackage, this.personReservation)
            .then(function (personReservation) {
            _this.personReservation = personReservation;
            _this.goBack(personReservation);
        })
            .catch(function (error) { return _this.error = error; });
    };
    PersonReservationDetailsComponent.prototype.goBack = function (savedPersonReservation) {
        if (savedPersonReservation === void 0) { savedPersonReservation = null; }
        this.close.emit(savedPersonReservation);
        if (this.navigated) {
            window.history.back();
        }
    };
    PersonReservationDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', person_reservation_1.PersonReservation)
    ], PersonReservationDetailsComponent.prototype, "personReservation", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], PersonReservationDetailsComponent.prototype, "close", void 0);
    PersonReservationDetailsComponent = __decorate([
        core_1.Component({
            selector: 'person-reservation-detail',
            templateUrl: 'app/templates/packages/reservations/persons-reservations/person-reservation-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [person_reservation_service_1.PersonReservationService, reservation_service_1.ReservationService, person_service_1.PersonService, router_1.ActivatedRoute, router_1.Router])
    ], PersonReservationDetailsComponent);
    return PersonReservationDetailsComponent;
}());
exports.PersonReservationDetailsComponent = PersonReservationDetailsComponent;
//# sourceMappingURL=person-reservation-details.component.js.map