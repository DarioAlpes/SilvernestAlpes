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
var LocationService = (function (_super) {
    __extends(LocationService, _super);
    function LocationService(http) {
        _super.call(this, http);
    }
    LocationService.getLocationsUrl = function (idClient) {
        var urlClient = client_service_1.ClientService.getClientUrl(idClient);
        return "" + urlClient + LocationService.locationsUrlPosfix;
    };
    LocationService.getLocationUrl = function (idClient, id) {
        return base_service_service_1.BaseService.getItemUrl(LocationService.getLocationsUrl(idClient), id);
    };
    LocationService.prototype.getLocations = function (idClient) {
        return this.getCollection(LocationService.getLocationsUrl(idClient));
    };
    LocationService.prototype.getLocation = function (idClient, id) {
        return this.getItem(LocationService.getLocationsUrl(idClient), id);
    };
    LocationService.prototype.saveLocation = function (idClient, location) {
        return this.saveItem(LocationService.getLocationsUrl(idClient), location);
    };
    LocationService.locationsUrlPosfix = 'locations/';
    LocationService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], LocationService);
    return LocationService;
}(base_service_service_1.BaseService));
exports.LocationService = LocationService;
exports.LOCATIONS_SERVICES = [LocationService];
//# sourceMappingURL=location.service.js.map