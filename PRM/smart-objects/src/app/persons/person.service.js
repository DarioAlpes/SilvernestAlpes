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
var PersonService = (function (_super) {
    __extends(PersonService, _super);
    function PersonService(http) {
        _super.call(this, http);
    }
    PersonService.getPersonsUrl = function (idClient) {
        var urlClient = client_service_1.ClientService.getClientUrl(idClient);
        return "" + urlClient + PersonService.personsUrlPosfix;
    };
    PersonService.getPersonUrl = function (idClient, id) {
        return base_service_service_1.BaseService.getItemUrl(PersonService.getPersonsUrl(idClient), id);
    };
    PersonService.prototype.getPersons = function (idClient) {
        return this.getCollection(PersonService.getPersonsUrl(idClient));
    };
    PersonService.prototype.getPerson = function (idClient, id) {
        return this.getItem(PersonService.getPersonsUrl(idClient), id);
    };
    PersonService.prototype.savePerson = function (idClient, person) {
        return this.saveItem(PersonService.getPersonsUrl(idClient), person);
    };
    PersonService.prototype.deletePerson = function (idClient, person) {
        return this.deleteItem(PersonService.getPersonsUrl(idClient), person);
    };
    PersonService.personsUrlPosfix = 'persons/';
    PersonService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], PersonService);
    return PersonService;
}(base_service_service_1.BaseService));
exports.PersonService = PersonService;
exports.PERSON_SERVICES = [PersonService];
//# sourceMappingURL=person.service.js.map