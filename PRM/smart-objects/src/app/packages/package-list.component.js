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
var package_1 = require('./package');
var package_details_component_1 = require('./package-details.component');
var package_service_1 = require('./package.service');
var router_1 = require('@angular/router');
var PackageListComponent = (function () {
    function PackageListComponent(router, route, packageService) {
        this.router = router;
        this.route = route;
        this.packageService = packageService;
        this.showPackage = false;
    }
    PackageListComponent.prototype.onSelect = function (pack) {
        var link = ['/clients', this.idClient, '/packages', pack.id];
        this.router.navigate(link);
    };
    PackageListComponent.prototype.getPackages = function () {
        var _this = this;
        this.packageService
            .getPackages(this.idClient)
            .then(function (packages) { return _this.packages = packages; })
            .catch(function (error) { return _this.error = error; });
    };
    PackageListComponent.prototype.addPackage = function () {
        this.showPackage = true;
        this.selectedPackage = new package_1.Package();
    };
    PackageListComponent.prototype.close = function (savedPackage) {
        this.showPackage = false;
        if (savedPackage) {
            this.getPackages();
        }
    };
    PackageListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                _this.getPackages();
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    PackageListComponent.prototype.goBack = function () {
        window.history.back();
    };
    PackageListComponent = __decorate([
        core_1.Component({
            selector: 'package-list',
            templateUrl: 'app/templates/packages/package-list.html',
            directives: [package_details_component_1.PackageDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, package_service_1.PackageService])
    ], PackageListComponent);
    return PackageListComponent;
}());
exports.PackageListComponent = PackageListComponent;
//# sourceMappingURL=package-list.component.js.map