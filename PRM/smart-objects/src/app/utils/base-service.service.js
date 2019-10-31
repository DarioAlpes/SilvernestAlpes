"use strict";
var http_1 = require('@angular/http');
require('rxjs/add/operator/toPromise');
var BaseService = (function () {
    function BaseService(http) {
        this.http = http;
    }
    BaseService.prototype.handleApiError = function (error) {
        var bodyMessage = "";
        try {
            bodyMessage = error.json().message || error.toString();
        }
        catch (err) {
            bodyMessage = error.toString();
        }
        var message = "C\u00F3digo de error " + error.status + ". " + bodyMessage;
        console.error('An error occurred', message);
        return Promise.reject(message);
    };
    BaseService.prototype.getCollection = function (url) {
        return this.http.get(url)
            .toPromise()
            .then(function (response) { return response.json(); })
            .catch(this.handleApiError);
    };
    BaseService.prototype.getItem = function (baseUrl, id) {
        return this.http.get(BaseService.getItemUrl(baseUrl, id))
            .toPromise()
            .then(function (response) { return response.json(); })
            .catch(this.handleApiError);
    };
    BaseService.prototype.updateItem = function (baseUrl, item, id) {
        if (id === void 0) { id = undefined; }
        if (id === undefined) {
            id = item.id;
        }
        var headers = new http_1.Headers({
            'Content-Type': 'application/json' });
        return this.http
            .put(BaseService.getItemUrl(baseUrl, id), JSON.stringify(item), { headers: headers })
            .toPromise()
            .then(function (response) { return response.json(); })
            .catch(this.handleApiError);
    };
    BaseService.prototype.patchItem = function (baseUrl, body, id) {
        var headers = new http_1.Headers({
            'Content-Type': 'application/json' });
        return this.http
            .patch(BaseService.getItemUrl(baseUrl, id), JSON.stringify(body), { headers: headers })
            .toPromise()
            .then(function (response) { return response.json(); })
            .catch(this.handleApiError);
    };
    BaseService.prototype.createItem = function (baseUrl, item) {
        var headers = new http_1.Headers({
            'Content-Type': 'application/json' });
        return this.http
            .post(baseUrl, JSON.stringify(item), { headers: headers })
            .toPromise()
            .then(function (response) { return response.json(); })
            .catch(this.handleApiError);
    };
    BaseService.prototype.deleteItem = function (baseUrl, item, id) {
        if (id === void 0) { id = undefined; }
        if (id === undefined) {
            id = item.id;
        }
        var headers = new http_1.Headers({
            'Content-Type': 'application/json' });
        return this.http
            .delete(BaseService.getItemUrl(baseUrl, id), headers)
            .toPromise()
            .then(function (response) { return response.json(); })
            .catch(this.handleApiError);
    };
    BaseService.prototype.saveItem = function (baseUrl, item, id) {
        if (id === void 0) { id = undefined; }
        if (id === undefined) {
            if (item.id) {
                id = item.id;
            }
        }
        if (id === undefined) {
            return this.createItem(baseUrl, item);
        }
        else {
            return this.updateItem(baseUrl, item, id);
        }
    };
    BaseService.getItemUrl = function (baseUrl, id) {
        return "" + baseUrl + id + "/";
    };
    return BaseService;
}());
exports.BaseService = BaseService;
//# sourceMappingURL=base-service.service.js.map