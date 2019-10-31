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
var client_1 = require('./client');
var client_service_1 = require('./client.service');
var router_2 = require('@angular/router');
var ClientDetailsComponent = (function () {
    function ClientDetailsComponent(clientService, route) {
        this.clientService = clientService;
        this.route = route;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.client = new client_1.Client();
    }
    ClientDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id'] !== undefined) {
                _this.navigated = true;
                var id = +params['id'];
                _this.clientService.getClient(id)
                    .then(function (client) { return _this.client = client; })
                    .catch(function (error) { return _this.error = error; });
            }
            else {
                _this.navigated = false;
                _this.client = new client_1.Client();
            }
        });
    };
    ClientDetailsComponent.prototype.saveClient = function () {
        var _this = this;
        this.clientService.saveClient(this.client)
            .then(function (client) {
            _this.client = client;
            _this.goBack(client);
        })
            .catch(function (error) { return _this.error = error; });
    };
    ClientDetailsComponent.prototype.goBack = function (savedClient) {
        if (savedClient === void 0) { savedClient = null; }
        this.close.emit(savedClient);
        if (this.navigated) {
            window.history.back();
        }
    };
    ClientDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', client_1.Client)
    ], ClientDetailsComponent.prototype, "client", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], ClientDetailsComponent.prototype, "close", void 0);
    ClientDetailsComponent = __decorate([
        core_1.Component({
            selector: 'client-detail',
            templateUrl: 'app/templates/clients/client-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [client_service_1.ClientService, router_1.ActivatedRoute])
    ], ClientDetailsComponent);
    return ClientDetailsComponent;
}());
exports.ClientDetailsComponent = ClientDetailsComponent;
//# sourceMappingURL=client-details.component.js.map