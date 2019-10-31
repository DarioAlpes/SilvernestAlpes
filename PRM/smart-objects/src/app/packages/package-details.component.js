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
var package_1 = require('./package');
var location_service_1 = require('../locations/location.service');
var package_service_1 = require('./package.service');
var event_service_1 = require('./events/event.service');
var router_2 = require('@angular/router');
var PackageDetailsComponent = (function () {
    function PackageDetailsComponent(packageService, locationService, eventService, route, router) {
        this.packageService = packageService;
        this.locationService = locationService;
        this.eventService = eventService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.package = new package_1.Package();
    }
    PackageDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id'] !== undefined) {
                    _this.navigated = true;
                    var id = +params['id'];
                    _this.packageService.getPackage(_this.idClient, id)
                        .then(function (pack) { return _this.package = pack; })
                        .catch(function (error) { return _this.error = error; });
                }
                else {
                    _this.navigated = false;
                    _this.package = new package_1.Package();
                }
                _this.locationService.getLocations(_this.idClient)
                    .then(function (locations) { return _this.locations = locations; })
                    .catch(function (error) { return _this.error = error; });
                _this.eventService.getEvents(_this.idClient)
                    .then(function (events) { return _this.events = events; })
                    .catch(function (error) { return _this.error = error; });
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    PackageDetailsComponent.prototype.savePackage = function () {
        var _this = this;
        this.packageService.savePackage(this.idClient, this.package)
            .then(function (pack) {
            _this.package = pack;
            _this.goBack(pack);
        })
            .catch(function (error) { return _this.error = error; });
    };
    PackageDetailsComponent.prototype.goBack = function (savedPackage) {
        if (savedPackage === void 0) { savedPackage = null; }
        this.close.emit(savedPackage);
        if (this.navigated) {
            window.history.back();
        }
    };
    PackageDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', package_1.Package)
    ], PackageDetailsComponent.prototype, "package", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], PackageDetailsComponent.prototype, "close", void 0);
    PackageDetailsComponent = __decorate([
        core_1.Component({
            selector: 'package-detail',
            templateUrl: 'app/templates/packages/package-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [package_service_1.PackageService, location_service_1.LocationService, event_service_1.EventService, router_1.ActivatedRoute, router_1.Router])
    ], PackageDetailsComponent);
    return PackageDetailsComponent;
}());
exports.PackageDetailsComponent = PackageDetailsComponent;
//# sourceMappingURL=package-details.component.js.map