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
var sku_1 = require('./sku');
var sku_details_component_1 = require('./sku-details.component');
var sku_service_1 = require('./sku.service');
var router_1 = require('@angular/router');
var SkuListComponent = (function () {
    function SkuListComponent(router, route, skuService) {
        this.router = router;
        this.route = route;
        this.skuService = skuService;
        this.showSku = false;
    }
    SkuListComponent.prototype.onSelect = function (sku) {
        var link = ['/clients', this.idClient, '/skus', sku.id];
        this.router.navigate(link);
    };
    SkuListComponent.prototype.getSkus = function () {
        var _this = this;
        this.skuService
            .getSkus(this.idClient)
            .then(function (skus) { return _this.skus = skus; })
            .catch(function (error) { return _this.error = error; });
    };
    SkuListComponent.prototype.addSku = function () {
        this.showSku = true;
        this.selectedSku = new sku_1.Sku();
    };
    SkuListComponent.prototype.close = function (savedSku) {
        this.showSku = false;
        if (savedSku) {
            this.getSkus();
        }
    };
    SkuListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                _this.getSkus();
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    SkuListComponent.prototype.goBack = function () {
        window.history.back();
    };
    SkuListComponent = __decorate([
        core_1.Component({
            selector: 'sku-list',
            templateUrl: 'app/templates/skus/sku-list.html',
            directives: [sku_details_component_1.SkuDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, sku_service_1.SkuService])
    ], SkuListComponent);
    return SkuListComponent;
}());
exports.SkuListComponent = SkuListComponent;
//# sourceMappingURL=sku-list.component.js.map