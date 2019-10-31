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
var sku_1 = require('./sku');
var sku_service_1 = require('./sku.service');
var sku_category_service_1 = require('./categories/sku-category.service');
var router_2 = require('@angular/router');
var SkuDetailsComponent = (function () {
    function SkuDetailsComponent(skuService, categoryService, route, router) {
        this.skuService = skuService;
        this.categoryService = categoryService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.sku = new sku_1.Sku();
    }
    SkuDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id'] !== undefined) {
                    _this.navigated = true;
                    var id = +params['id'];
                    _this.skuService.getSku(_this.idClient, id)
                        .then(function (sku) { return _this.sku = sku; })
                        .catch(function (error) { return _this.error = error; });
                }
                else {
                    _this.navigated = false;
                    _this.sku = new sku_1.Sku();
                }
                _this.categoryService.getSkuCategories(_this.idClient)
                    .then(function (categories) { return _this.categories = categories; })
                    .catch(function (error) { return _this.error = error; });
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    SkuDetailsComponent.prototype.saveSku = function () {
        var _this = this;
        this.skuService.saveSku(this.idClient, this.sku)
            .then(function (sku) {
            _this.sku = sku;
            _this.goBack(sku);
        })
            .catch(function (error) { return _this.error = error; });
    };
    SkuDetailsComponent.prototype.goBack = function (savedSku) {
        if (savedSku === void 0) { savedSku = null; }
        this.close.emit(savedSku);
        if (this.navigated) {
            window.history.back();
        }
    };
    SkuDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', sku_1.Sku)
    ], SkuDetailsComponent.prototype, "sku", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], SkuDetailsComponent.prototype, "close", void 0);
    SkuDetailsComponent = __decorate([
        core_1.Component({
            selector: 'sku-detail',
            templateUrl: 'app/templates/skus/sku-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [sku_service_1.SkuService, sku_category_service_1.SkuCategoryService, router_1.ActivatedRoute, router_1.Router])
    ], SkuDetailsComponent);
    return SkuDetailsComponent;
}());
exports.SkuDetailsComponent = SkuDetailsComponent;
//# sourceMappingURL=sku-details.component.js.map