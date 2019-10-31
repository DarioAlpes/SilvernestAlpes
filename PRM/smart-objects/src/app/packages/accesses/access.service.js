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
var base_service_service_1 = require('../../utils/base-service.service');
var package_service_1 = require('../package.service');
var AccessService = (function (_super) {
    __extends(AccessService, _super);
    function AccessService(http) {
        _super.call(this, http);
    }
    AccessService.getAccessesUrl = function (idClient, idPackage) {
        var urlPackage = package_service_1.PackageService.getPackageUrl(idClient, idPackage);
        return "" + urlPackage + AccessService.accessesUrlPosfix;
    };
    AccessService.getAccessUrl = function (idClient, idPackage, id) {
        return base_service_service_1.BaseService.getItemUrl(AccessService.getAccessesUrl(idClient, idPackage), id);
    };
    AccessService.prototype.getAccesses = function (idClient, idPackage) {
        return this.getCollection(AccessService.getAccessesUrl(idClient, idPackage));
    };
    AccessService.prototype.getAccess = function (idClient, idPackage, id) {
        return this.getItem(AccessService.getAccessesUrl(idClient, idPackage), id);
    };
    AccessService.prototype.saveAccess = function (idClient, idPackage, access) {
        return this.saveItem(AccessService.getAccessesUrl(idClient, idPackage), access);
    };
    AccessService.accessesUrlPosfix = 'accesses/';
    AccessService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], AccessService);
    return AccessService;
}(base_service_service_1.BaseService));
exports.AccessService = AccessService;
exports.ACCESSES_SERVICES = [AccessService];
//# sourceMappingURL=access.service.js.map