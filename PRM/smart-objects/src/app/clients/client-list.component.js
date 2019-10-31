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
var client_1 = require('./client');
var client_details_component_1 = require('./client-details.component');
var client_service_1 = require('./client.service');
var router_1 = require('@angular/router');
var ClientListComponent = (function () {
    function ClientListComponent(router, clientService) {
        this.router = router;
        this.clientService = clientService;
        this.showClient = false;
    }
    ClientListComponent.prototype.onSelect = function (client) {
        var link = ['/clients', client.id];
        this.router.navigate(link);
    };
    ClientListComponent.prototype.onDelete = function (client) {
        var _this = this;
        this.clientService.deleteClient(client)
            .then(function (client) { return _this.close(client); })
            .catch(function (error) { return _this.error = error; });
    };
    ClientListComponent.prototype.getClients = function () {
        var _this = this;
        this.clientService.getClients()
            .then(function (clients) { return _this.clients = clients; })
            .catch(function (error) { return _this.error = error; });
    };
    ClientListComponent.prototype.addClient = function () {
        this.showClient = true;
        this.selectedClient = new client_1.Client();
    };
    ClientListComponent.prototype.close = function (savedClient) {
        this.showClient = false;
        if (savedClient) {
            this.getClients();
        }
    };
    ClientListComponent.prototype.ngOnInit = function () {
        this.getClients();
    };
    ClientListComponent = __decorate([
        core_1.Component({
            selector: 'client-list',
            templateUrl: 'app/templates/clients/client-list.html',
            directives: [client_details_component_1.ClientDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, client_service_1.ClientService])
    ], ClientListComponent);
    return ClientListComponent;
}());
exports.ClientListComponent = ClientListComponent;
//# sourceMappingURL=client-list.component.js.map