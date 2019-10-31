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
var base_service_service_1 = require('../utils/base-service.service');
var http_1 = require('@angular/http');
var ClientService = (function (_super) {
    __extends(ClientService, _super);
    function ClientService(http) {
        _super.call(this, http);
    }
    ClientService.getClientUrl = function (id) {
        return base_service_service_1.BaseService.getItemUrl(ClientService.clientsUrl, id);
    };
    ClientService.prototype.getClients = function () {
        return this.getCollection(ClientService.clientsUrl);
    };
    ClientService.prototype.getClient = function (id) {
        return this.getItem(ClientService.clientsUrl, id);
    };
    ClientService.prototype.updateClient = function (client) {
        return this.updateItem(ClientService.clientsUrl, client);
    };
    ClientService.prototype.saveClient = function (client) {
        return this.saveItem(ClientService.clientsUrl, client);
    };
    ClientService.prototype.createClient = function (client) {
        return this.createItem(ClientService.clientsUrl, client);
    };
    ClientService.prototype.deleteClient = function (client) {
        return this.deleteItem(ClientService.clientsUrl, client);
    };
    ClientService.clientsUrl = 'https://smartobjectssas.appspot.com/clients/';
    ClientService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http])
    ], ClientService);
    return ClientService;
}(base_service_service_1.BaseService));
exports.ClientService = ClientService;
exports.CLIENTS_SERVICES = [ClientService];
//# sourceMappingURL=client.service.js.map