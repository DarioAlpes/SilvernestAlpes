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
var person_consumption_1 = require('./person-consumption');
var person_consumption_details_component_1 = require('./person-consumption-details.component');
var person_consumption_service_1 = require('./person-consumption.service');
var router_1 = require('@angular/router');
var PersonConsumptionListComponent = (function () {
    function PersonConsumptionListComponent(router, route, personConsumptionService) {
        this.router = router;
        this.route = route;
        this.personConsumptionService = personConsumptionService;
        this.showPersonConsumption = false;
    }
    PersonConsumptionListComponent.prototype.onSelect = function (personConsumption) {
        var link = ['/clients', this.idClient, '/packages', this.idPackage, '/reservations', this.idReservation,
            '/persons-reservations', this.idPersonReservation, '/person-consumptions', personConsumption.id];
        this.router.navigate(link);
    };
    PersonConsumptionListComponent.prototype.getPersonConsumptions = function () {
        var _this = this;
        this.personConsumptionService
            .getPersonConsumptions(this.idClient, this.idPackage, this.idReservation, this.idPersonReservation)
            .then(function (personConsumptions) { return _this.personConsumptions = personConsumptions; })
            .catch(function (error) { return _this.error = error; });
    };
    PersonConsumptionListComponent.prototype.addPersonConsumption = function () {
        this.showPersonConsumption = true;
        this.selectedPersonConsumption = new person_consumption_1.PersonConsumption();
    };
    PersonConsumptionListComponent.prototype.close = function (savedPersonConsumption) {
        this.showPersonConsumption = false;
        if (savedPersonConsumption) {
            this.getPersonConsumptions();
        }
    };
    PersonConsumptionListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    if ((params['id_reservation'] !== undefined) && (params['id_person_reservation'] !== undefined)) {
                        _this.idReservation = +params['id_reservation'];
                        _this.idPersonReservation = +params['id_person_reservation'];
                        _this.getPersonConsumptions();
                    }
                    else {
                        _this.router.navigate(['/clients', _this.idClient, '/packages', _this.idPackage, '/persons-reservations']);
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
    PersonConsumptionListComponent.prototype.goBack = function () {
        window.history.back();
    };
    PersonConsumptionListComponent = __decorate([
        core_1.Component({
            selector: 'person-consumption-list',
            templateUrl: 'app/templates/packages/reservations/persons-reservations/person-consumptions/person-consumption-list.html',
            directives: [person_consumption_details_component_1.PersonConsumptionDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, person_consumption_service_1.PersonConsumptionService])
    ], PersonConsumptionListComponent);
    return PersonConsumptionListComponent;
}());
exports.PersonConsumptionListComponent = PersonConsumptionListComponent;
//# sourceMappingURL=person-consumption-list.component.js.map