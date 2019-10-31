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
var sku_category_1 = require('./sku-category');
var sku_category_details_component_1 = require('./sku-category-details.component');
var sku_category_service_1 = require('./sku-category.service');
var router_1 = require('@angular/router');
var SkuCategoryListComponent = (function () {
    function SkuCategoryListComponent(router, route, categoryService) {
        this.router = router;
        this.route = route;
        this.categoryService = categoryService;
        this.showCategory = false;
    }
    SkuCategoryListComponent.prototype.onSelect = function (category) {
        var link = ['/clients', this.idClient, '/sku-categories', category.id];
        this.router.navigate(link);
    };
    SkuCategoryListComponent.prototype.getCategories = function () {
        var _this = this;
        this.categoryService
            .getSkuCategories(this.idClient)
            .then(function (categories) { return _this.categories = categories; })
            .catch(function (error) { return _this.error = error; });
    };
    SkuCategoryListComponent.prototype.addCategory = function () {
        this.showCategory = true;
        this.selectedCategory = new sku_category_1.SkuCategory();
    };
    SkuCategoryListComponent.prototype.close = function (savedCategory) {
        this.showCategory = false;
        if (savedCategory) {
            this.getCategories();
        }
    };
    SkuCategoryListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                _this.getCategories();
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    SkuCategoryListComponent.prototype.goBack = function () {
        window.history.back();
    };
    SkuCategoryListComponent = __decorate([
        core_1.Component({
            selector: 'sku-category-list',
            templateUrl: 'app/templates/skus/categories/category-list.html',
            directives: [sku_category_details_component_1.SkuCategoryDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, sku_category_service_1.SkuCategoryService])
    ], SkuCategoryListComponent);
    return SkuCategoryListComponent;
}());
exports.SkuCategoryListComponent = SkuCategoryListComponent;
//# sourceMappingURL=sku-category-list.component.js.map