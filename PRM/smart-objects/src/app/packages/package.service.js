"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
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
var http_1 = require('@angular/http');
var base_service_service_1 = require('../utils/base-service.service');
var client_service_1 = require('../clients/client.service');
var access_service_1 = require('./accesses/access.service');
var consumption_service_1 = require('./consumptions/consumption.service');
var event_service_1 = require('./events/event.service');
var reservation_service_1 = require('./reservations/reservation.service');
var PackageService = (function (_super) {
    __extends(PackageService, _super);
    function PackageService(http) {
        _super.call(this, http);
    }
    PackageService.getPackagesUrl = function (idClient) {
        var urlClient = client_service_1.ClientService.getClientUrl(idClient);
        return "" + urlClient + PackageService.packagesUrlPosfix;
    };
    PackageService.getPackageUrl = function (idClient, id) {
        return base_service_service_1.BaseService.getItemUrl(PackageService.getPackagesUrl(idClient), id);
    };
    PackageService.prototype.getPackages = function (idClient) {
        return this.getCollection(PackageService.getPackagesUrl(idClient));
    };
    PackageService.prototype.getPackage = function (idClient, id) {
        return this.getItem(PackageService.getPackagesUrl(idClient), id);
    };
    PackageService.prototype.savePackage = function (idClient, pack) {
        return this.saveItem(PackageService.getPackagesUrl(idClient), pack);
    };
    PackageService.packagesUrlPosfix = 'packages/';
    PackageService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], PackageService);
    return PackageService;
}(base_service_service_1.BaseService));
exports.PackageService = PackageService;
var PACKAGE_SPECIFIC_SERVICE = [PackageService];
exports.PACKAGES_SERVICES = PACKAGE_SPECIFIC_SERVICE.concat(access_service_1.ACCESSES_SERVICES, event_service_1.EVENTS_SERVICES, consumption_service_1.CONSUMPTIONS_SERVICES, reservation_service_1.RESERVATIONS_SERVICES);
//# sourceMappingURL=package.service.js.map