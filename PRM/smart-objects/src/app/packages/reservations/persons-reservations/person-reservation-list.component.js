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
var person_reservation_1 = require('./person-reservation');
var person_reservation_details_component_1 = require('./person-reservation-details.component');
var person_reservation_service_1 = require('./person-reservation.service');
var router_1 = require('@angular/router');
var PersonReservationListComponent = (function () {
    function PersonReservationListComponent(router, route, personReservationService) {
        this.router = router;
        this.route = route;
        this.personReservationService = personReservationService;
        this.showPersonReservation = false;
    }
    PersonReservationListComponent.prototype.onSelect = function (personReservation) {
        var link = ['/clients', this.idClient, '/packages', this.idPackage, '/reservations',
            personReservation['id-reservation'], '/persons-reservations', personReservation.id];
        this.router.navigate(link);
    };
    PersonReservationListComponent.prototype.activate = function (personReservation) {
        var _this = this;
        this.personReservationService
            .activatePersonReservations(this.idClient, this.idPackage, personReservation, true)
            .then(function (personReservation) { return _this.getPersonReservations(); })
            .catch(function (error) { return _this.error = error; });
    };
    PersonReservationListComponent.prototype.deactivate = function (personReservation) {
        var _this = this;
        this.personReservationService
            .activatePersonReservations(this.idClient, this.idPackage, personReservation, false)
            .then(function (personReservation) { return _this.getPersonReservations(); })
            .catch(function (error) { return _this.error = error; });
    };
    PersonReservationListComponent.prototype.getPersonReservations = function () {
        var _this = this;
        this.personReservationService
            .getPersonReservations(this.idClient, this.idPackage)
            .then(function (personReservations) { return _this.personReservations = personReservations; })
            .catch(function (error) { return _this.error = error; });
    };
    PersonReservationListComponent.prototype.addPersonReservation = function () {
        this.showPersonReservation = true;
        this.selectedPersonReservation = new person_reservation_1.PersonReservation();
    };
    PersonReservationListComponent.prototype.close = function (savedPersonReservation) {
        this.showPersonReservation = false;
        if (savedPersonReservation) {
            this.getPersonReservations();
        }
    };
    PersonReservationListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    _this.getPersonReservations();
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
    PersonReservationListComponent.prototype.goBack = function () {
        window.history.back();
    };
    PersonReservationListComponent = __decorate([
        core_1.Component({
            selector: 'person-reservation-list',
            templateUrl: 'app/templates/packages/reservations/persons-reservations/person-reservation-list.html',
            directives: [person_reservation_details_component_1.PersonReservationDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, person_reservation_service_1.PersonReservationService])
    ], PersonReservationListComponent);
    return PersonReservationListComponent;
}());
exports.PersonReservationListComponent = PersonReservationListComponent;
//# sourceMappingURL=person-reservation-list.component.js.map