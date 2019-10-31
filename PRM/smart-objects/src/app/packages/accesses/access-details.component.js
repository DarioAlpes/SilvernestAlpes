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
var access_1 = require('./access');
var access_service_1 = require('./access.service');
var router_2 = require('@angular/router');
var location_service_1 = require('../../locations/location.service');
var AccessDetailsComponent = (function () {
    function AccessDetailsComponent(accessService, locationService, route, router) {
        this.accessService = accessService;
        this.locationService = locationService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.access = new access_1.Access();
    }
    AccessDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    if (params['id'] !== undefined) {
                        _this.navigated = true;
                        var id = +params['id'];
                        _this.accessService.getAccess(_this.idClient, _this.idPackage, id)
                            .then(function (access) { return _this.access = access; })
                            .catch(function (error) { return _this.error = error; });
                    }
                    else {
                        _this.navigated = false;
                        _this.access = new access_1.Access();
                    }
                    _this.locationService.getLocations(_this.idClient)
                        .then(function (locations) { return _this.locations = locations; })
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
    AccessDetailsComponent.prototype.saveAccess = function () {
        var _this = this;
        this.accessService.saveAccess(this.idClient, this.idPackage, this.access)
            .then(function (access) {
            _this.access = access;
            _this.goBack(access);
        })
            .catch(function (error) { return _this.error = error; });
    };
    AccessDetailsComponent.prototype.goBack = function (savedAccess) {
        if (savedAccess === void 0) { savedAccess = null; }
        this.close.emit(savedAccess);
        if (this.navigated) {
            window.history.back();
        }
    };
    AccessDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', access_1.Access)
    ], AccessDetailsComponent.prototype, "access", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], AccessDetailsComponent.prototype, "close", void 0);
    AccessDetailsComponent = __decorate([
        core_1.Component({
            selector: 'access-detail',
            templateUrl: 'app/templates/packages/accesses/access-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [access_service_1.AccessService, location_service_1.LocationService, router_1.ActivatedRoute, router_1.Router])
    ], AccessDetailsComponent);
    return AccessDetailsComponent;
}());
exports.AccessDetailsComponent = AccessDetailsComponent;
//# sourceMappingURL=access-details.component.js.map