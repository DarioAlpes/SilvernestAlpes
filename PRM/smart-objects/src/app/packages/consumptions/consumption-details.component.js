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
var consumption_1 = require('./consumption');
var consumption_service_1 = require('./consumption.service');
var router_2 = require('@angular/router');
var location_service_1 = require('../../locations/location.service');
var sku_service_1 = require('../../skus/sku.service');
var ConsumptionDetailsComponent = (function () {
    function ConsumptionDetailsComponent(consumptionService, locationService, skuService, route, router) {
        this.consumptionService = consumptionService;
        this.locationService = locationService;
        this.skuService = skuService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.consumption = new consumption_1.Consumption();
    }
    ConsumptionDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    if (params['id'] !== undefined) {
                        _this.navigated = true;
                        var id = +params['id'];
                        _this.consumptionService.getConsumption(_this.idClient, _this.idPackage, id)
                            .then(function (consumption) { return _this.consumption = consumption; })
                            .catch(function (error) { return _this.error = error; });
                    }
                    else {
                        _this.navigated = false;
                        _this.consumption = new consumption_1.Consumption();
                    }
                    _this.locationService.getLocations(_this.idClient)
                        .then(function (locations) { return _this.locations = locations; })
                        .catch(function (error) { return _this.error = error; });
                    _this.skuService.getSkus(_this.idClient)
                        .then(function (skus) { return _this.skus = skus; })
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
    ConsumptionDetailsComponent.prototype.setSelectedSkus = function (selectedSkus) {
        var selectedSkuIds = Array.prototype.filter.call(selectedSkus, function (skuItem) { return skuItem.selected === true; })
            .map(function (skuItem) { return +skuItem.value; });
        this.consumption['id-skus'] = selectedSkuIds;
    };
    ConsumptionDetailsComponent.prototype.setSelectedLocations = function (selectedLocations) {
        var selectedLocationIds = Array.prototype.filter.call(selectedLocations, function (locationItem) { return locationItem.selected === true; })
            .map(function (locationItem) { return +locationItem.value; });
        this.consumption['id-locations'] = selectedLocationIds;
    };
    ConsumptionDetailsComponent.prototype.saveConsumption = function () {
        var _this = this;
        this.consumptionService.saveConsumption(this.idClient, this.idPackage, this.consumption)
            .then(function (consumption) {
            _this.consumption = consumption;
            _this.goBack(consumption);
        })
            .catch(function (error) { return _this.error = error; });
    };
    ConsumptionDetailsComponent.prototype.goBack = function (savedConsumption) {
        if (savedConsumption === void 0) { savedConsumption = null; }
        this.close.emit(savedConsumption);
        if (this.navigated) {
            window.history.back();
        }
    };
    ConsumptionDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', consumption_1.Consumption)
    ], ConsumptionDetailsComponent.prototype, "consumption", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], ConsumptionDetailsComponent.prototype, "close", void 0);
    ConsumptionDetailsComponent = __decorate([
        core_1.Component({
            selector: 'consumption-detail',
            templateUrl: 'app/templates/packages/consumptions/consumption-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [consumption_service_1.ConsumptionService, location_service_1.LocationService, sku_service_1.SkuService, router_1.ActivatedRoute, router_1.Router])
    ], ConsumptionDetailsComponent);
    return ConsumptionDetailsComponent;
}());
exports.ConsumptionDetailsComponent = ConsumptionDetailsComponent;
//# sourceMappingURL=consumption-details.component.js.map