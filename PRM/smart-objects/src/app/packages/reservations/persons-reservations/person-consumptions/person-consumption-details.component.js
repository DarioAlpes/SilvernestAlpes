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
var access_service_1 = require('../../../accesses/access.service');
var consumption_service_1 = require('../../../consumptions/consumption.service');
var sku_service_1 = require('../../../../skus/sku.service');
var person_consumption_1 = require('./person-consumption');
var person_consumption_service_1 = require('./person-consumption.service');
var router_2 = require('@angular/router');
var PersonConsumptionDetailsComponent = (function () {
    function PersonConsumptionDetailsComponent(personConsumptionService, skuService, accessService, consumptionService, route, router) {
        this.personConsumptionService = personConsumptionService;
        this.skuService = skuService;
        this.accessService = accessService;
        this.consumptionService = consumptionService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.personConsumption = new person_consumption_1.PersonConsumption();
    }
    PersonConsumptionDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    if ((params['id_reservation'] !== undefined) && (params['id_person_reservation'] !== undefined)) {
                        _this.idReservation = +params['id_reservation'];
                        _this.idPersonReservation = +params['id_person_reservation'];
                        if (params['id'] !== undefined) {
                            _this.navigated = true;
                            var id = +params['id'];
                            _this.personConsumptionService.getPersonConsumption(_this.idClient, _this.idPackage, _this.idReservation, _this.idPersonReservation, id)
                                .then(function (personConsumption) { return _this.personConsumption = personConsumption; })
                                .catch(function (error) { return _this.error = error; });
                        }
                        else {
                            _this.navigated = false;
                            _this.personConsumption = new person_consumption_1.PersonConsumption();
                        }
                        _this.accessService.getAccesses(_this.idClient, _this.idPackage)
                            .then(function (accesses) { return _this.accesses = accesses; })
                            .catch(function (error) { return _this.error = error; });
                        _this.consumptionService.getConsumptions(_this.idClient, _this.idPackage)
                            .then(function (consumptions) { return _this.consumptions = consumptions; })
                            .catch(function (error) { return _this.error = error; });
                        _this.skuService.getSkus(_this.idClient)
                            .then(function (skus) { return _this.skus = skus; })
                            .catch(function (error) { return _this.error = error; });
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
    PersonConsumptionDetailsComponent.prototype.savePersonConsumption = function () {
        var _this = this;
        this.personConsumptionService.savePersonConsumption(this.idClient, this.idPackage, this.idReservation, this.idPersonReservation, this.personConsumption)
            .then(function (personConsumption) {
            _this.personConsumption = personConsumption;
            _this.goBack(personConsumption);
        })
            .catch(function (error) { return _this.error = error; });
    };
    PersonConsumptionDetailsComponent.prototype.goBack = function (savedPersonConsumption) {
        if (savedPersonConsumption === void 0) { savedPersonConsumption = null; }
        this.close.emit(savedPersonConsumption);
        if (this.navigated) {
            window.history.back();
        }
    };
    PersonConsumptionDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', person_consumption_1.PersonConsumption)
    ], PersonConsumptionDetailsComponent.prototype, "personConsumption", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], PersonConsumptionDetailsComponent.prototype, "close", void 0);
    PersonConsumptionDetailsComponent = __decorate([
        core_1.Component({
            selector: 'person-consumption-detail',
            templateUrl: 'app/templates/packages/reservations/persons-reservations/person-consumptions/person-consumption-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [person_consumption_service_1.PersonConsumptionService, sku_service_1.SkuService, access_service_1.AccessService, consumption_service_1.ConsumptionService, router_1.ActivatedRoute, router_1.Router])
    ], PersonConsumptionDetailsComponent);
    return PersonConsumptionDetailsComponent;
}());
exports.PersonConsumptionDetailsComponent = PersonConsumptionDetailsComponent;
//# sourceMappingURL=person-consumption-details.component.js.map