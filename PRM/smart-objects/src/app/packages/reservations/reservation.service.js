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
var person_reservation_service_1 = require('./persons-reservations/person-reservation.service');
var ReservationService = (function (_super) {
    __extends(ReservationService, _super);
    function ReservationService(http) {
        _super.call(this, http);
    }
    ReservationService.getReservationsUrl = function (idClient, idPackage) {
        var urlPackage = package_service_1.PackageService.getPackageUrl(idClient, idPackage);
        return "" + urlPackage + ReservationService.reservationsUrlPosfix;
    };
    ReservationService.getReservationUrl = function (idClient, idPackage, id) {
        return base_service_service_1.BaseService.getItemUrl(ReservationService.getReservationsUrl(idClient, idPackage), id);
    };
    ReservationService.prototype.getReservations = function (idClient, idPackage) {
        return this.getCollection(ReservationService.getReservationsUrl(idClient, idPackage));
    };
    ReservationService.prototype.getReservation = function (idClient, idPackage, id) {
        return this.getItem(ReservationService.getReservationsUrl(idClient, idPackage), id);
    };
    ReservationService.prototype.saveReservation = function (idClient, idPackage, reservation) {
        return this.saveItem(ReservationService.getReservationsUrl(idClient, idPackage), reservation);
    };
    ReservationService.reservationsUrlPosfix = 'reservations/';
    ReservationService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], ReservationService);
    return ReservationService;
}(base_service_service_1.BaseService));
exports.ReservationService = ReservationService;
var RESERVATIONS_SPECIFIC_SERVICES = [ReservationService];
exports.RESERVATIONS_SERVICES = RESERVATIONS_SPECIFIC_SERVICES.concat(person_reservation_service_1.PERSON_RESERVATIONS_SERVICES);
//# sourceMappingURL=reservation.service.js.map