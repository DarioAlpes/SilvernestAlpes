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
var base_service_service_1 = require('../../../utils/base-service.service');
var package_service_1 = require('../../package.service');
var reservation_service_1 = require('../reservation.service');
var person_consumption_service_1 = require('./person-consumptions/person-consumption.service');
var PersonReservationService = (function (_super) {
    __extends(PersonReservationService, _super);
    function PersonReservationService(http) {
        _super.call(this, http);
    }
    PersonReservationService.getPersonReservationsUrl = function (idClient, idPackage) {
        var urlPackage = package_service_1.PackageService.getPackageUrl(idClient, idPackage);
        return "" + urlPackage + PersonReservationService.personReservationsUrlPosfix;
    };
    PersonReservationService.getPersonReservationsBaseUrl = function (idClient, idPackage, idReservation) {
        var urlReservation = reservation_service_1.ReservationService.getReservationUrl(idClient, idPackage, idReservation);
        return "" + urlReservation + PersonReservationService.personReservationsUrlPosfix;
    };
    PersonReservationService.getPersonReservationUrl = function (idClient, idPackage, idReservation, id) {
        var urlReservation = PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation);
        return base_service_service_1.BaseService.getItemUrl(urlReservation, id);
    };
    PersonReservationService.prototype.getPersonReservations = function (idClient, idPackage) {
        return this.getCollection(PersonReservationService.getPersonReservationsUrl(idClient, idPackage));
    };
    PersonReservationService.prototype.getPersonReservation = function (idClient, idPackage, idReservation, id) {
        return this.getItem(PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation), id);
    };
    PersonReservationService.prototype.savePersonReservation = function (idClient, idPackage, personReservation) {
        if (personReservation.id) {
            var idReservation = personReservation['id-reservation'];
            return this.updateItem(PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation), personReservation);
        }
        else {
            return this.createItem(PersonReservationService.getPersonReservationsUrl(idClient, idPackage), personReservation);
        }
    };
    PersonReservationService.prototype.activatePersonReservations = function (idClient, idPackage, personReservation, activate) {
        var idReservation = personReservation['id-reservation'];
        var activationBody = { active: activate };
        return this.patchItem(PersonReservationService.getPersonReservationsBaseUrl(idClient, idPackage, idReservation), activationBody, personReservation.id);
    };
    PersonReservationService.personReservationsUrlPosfix = 'persons-reservations/';
    PersonReservationService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], PersonReservationService);
    return PersonReservationService;
}(base_service_service_1.BaseService));
exports.PersonReservationService = PersonReservationService;
var PERSON_RESERVATIONS_SPECIFIC_SERVICES = [PersonReservationService];
exports.PERSON_RESERVATIONS_SERVICES = PERSON_RESERVATIONS_SPECIFIC_SERVICES.concat(person_consumption_service_1.PERSON_CONSUMPTIONS_SERVICES);
//# sourceMappingURL=person-reservation.service.js.map