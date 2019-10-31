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
var sku_category_1 = require('./sku-category');
var sku_category_service_1 = require('./sku-category.service');
var router_2 = require('@angular/router');
var SkuCategoryDetailsComponent = (function () {
    function SkuCategoryDetailsComponent(categoryService, route, router) {
        this.categoryService = categoryService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.category = new sku_category_1.SkuCategory();
    }
    SkuCategoryDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id'] !== undefined) {
                    _this.navigated = true;
                    var id = +params['id'];
                    _this.categoryService.getSkuCategory(_this.idClient, id)
                        .then(function (category) { return _this.category = category; })
                        .catch(function (error) { return _this.error = error; });
                }
                else {
                    _this.navigated = false;
                    _this.category = new sku_category_1.SkuCategory();
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
    SkuCategoryDetailsComponent.prototype.saveCategory = function () {
        var _this = this;
        this.categoryService.saveSkuCategory(this.idClient, this.category)
            .then(function (category) {
            _this.category = category;
            _this.goBack(category);
        })
            .catch(function (error) { return _this.error = error; });
    };
    SkuCategoryDetailsComponent.prototype.goBack = function (savedCategory) {
        if (savedCategory === void 0) { savedCategory = null; }
        this.close.emit(savedCategory);
        if (this.navigated) {
            window.history.back();
        }
    };
    SkuCategoryDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', sku_category_1.SkuCategory)
    ], SkuCategoryDetailsComponent.prototype, "category", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], SkuCategoryDetailsComponent.prototype, "close", void 0);
    SkuCategoryDetailsComponent = __decorate([
        core_1.Component({
            selector: 'sku-category-detail',
            templateUrl: 'app/templates/skus/categories/category-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [sku_category_service_1.SkuCategoryService, router_1.ActivatedRoute, router_1.Router])
    ], SkuCategoryDetailsComponent);
    return SkuCategoryDetailsComponent;
}());
exports.SkuCategoryDetailsComponent = SkuCategoryDetailsComponent;
//# sourceMappingURL=sku-category-details.component.js.map