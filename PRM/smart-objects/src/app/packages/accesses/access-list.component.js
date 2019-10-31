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
var access_1 = require('./access');
var access_details_component_1 = require('./access-details.component');
var access_service_1 = require('./access.service');
var router_1 = require('@angular/router');
var AccessListComponent = (function () {
    function AccessListComponent(router, route, accessService) {
        this.router = router;
        this.route = route;
        this.accessService = accessService;
        this.showAccess = false;
    }
    AccessListComponent.prototype.onSelect = function (access) {
        var link = ['/clients', this.idClient, '/packages', this.idPackage, '/accesses', access.id];
        this.router.navigate(link);
    };
    AccessListComponent.prototype.getAccesses = function () {
        var _this = this;
        this.accessService
            .getAccesses(this.idClient, this.idPackage)
            .then(function (accesses) { return _this.accesses = accesses; })
            .catch(function (error) { return _this.error = error; });
    };
    AccessListComponent.prototype.addAccess = function () {
        this.showAccess = true;
        this.selectedAccess = new access_1.Access();
    };
    AccessListComponent.prototype.close = function (savedAccess) {
        this.showAccess = false;
        if (savedAccess) {
            this.getAccesses();
        }
    };
    AccessListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id_package'] !== undefined) {
                    _this.idPackage = +params['id_package'];
                    _this.getAccesses();
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
    AccessListComponent.prototype.goBack = function () {
        window.history.back();
    };
    AccessListComponent = __decorate([
        core_1.Component({
            selector: 'accesses-list',
            templateUrl: 'app/templates/packages/accesses/access-list.html',
            directives: [access_details_component_1.AccessDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, access_service_1.AccessService])
    ], AccessListComponent);
    return AccessListComponent;
}());
exports.AccessListComponent = AccessListComponent;
//# sourceMappingURL=access-list.component.js.map