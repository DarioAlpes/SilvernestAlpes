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
var consumption_1 = require('./consumption');
var consumption_details_component_1 = require('./consumption-details.component');
var consumption_service_1 = require('./consumption.service');
var router_1 = require('@angular/router');
var ConsumptionListComponent = (function () {
    function ConsumptionListComponent(router, route, consumptionService) {
        this.router = router;
        this.route = route;
        this.consumptionService = consumptionService;
        this.showConsumption = false;
    }
    ConsumptionListComponent.prototype.onSelect = function (consumption) {
        var link = ['/clients', this.idClient, '/packages', this.idPackage, '/consumptions', consumption.id];
        this.router.navigate(link);
    };
    ConsumptionListComponent.prototype.getConsumptions = function () {
        var _this = this;
        this.consumptionService
            .getConsumptions(this.idClient, this.idPackage)
            .then(function (consumptions) { return _this.consumptions = consumptions; })
            .catch(function (error) { return _this.error = error; });
    };
    ConsumptionListComponent.prototype.addConsumption = function () {
        this.showConsumption = true;
        this.selectedConsumption = new consumption_1.Consumption();
    };
    ConsumptionListComponent.prototype.close = function (savedConsumption) {
        this.showConsumption = false;
        if (savedConsumption) {
            this.getConsumptions();
        }
    };
    ConsumptionListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    _this.getConsumptions();
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
    ConsumptionListComponent.prototype.goBack = function () {
        window.history.back();
    };
    ConsumptionListComponent = __decorate([
        core_1.Component({
            selector: 'consumptions-list',
            templateUrl: 'app/templates/packages/consumptions/consumption-list.html',
            directives: [consumption_details_component_1.ConsumptionDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, consumption_service_1.ConsumptionService])
    ], ConsumptionListComponent);
    return ConsumptionListComponent;
}());
exports.ConsumptionListComponent = ConsumptionListComponent;
//# sourceMappingURL=consumption-list.component.js.map