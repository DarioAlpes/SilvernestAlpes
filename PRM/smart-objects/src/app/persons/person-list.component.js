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
var person_1 = require('./person');
var person_details_component_1 = require('./person-details.component');
var person_service_1 = require('./person.service');
var router_1 = require('@angular/router');
var PersonListComponent = (function () {
    function PersonListComponent(router, route, personService) {
        this.router = router;
        this.route = route;
        this.personService = personService;
        this.showPerson = false;
    }
    PersonListComponent.prototype.onSelect = function (person) {
        var link = ['/clients', this.idClient, '/persons', person.id];
        this.router.navigate(link);
    };
    PersonListComponent.prototype.onDelete = function (person) {
        var _this = this;
        this.personService
            .deletePerson(this.idClient, person)
            .then(function (person) { return _this.getPersons(); })
            .catch(function (error) { return _this.error = error; });
    };
    PersonListComponent.prototype.getPersons = function () {
        var _this = this;
        this.personService
            .getPersons(this.idClient)
            .then(function (persons) { return _this.persons = persons; })
            .catch(function (error) { return _this.error = error; });
    };
    PersonListComponent.prototype.addPerson = function () {
        this.showPerson = true;
        this.selectedPerson = new person_1.Person();
    };
    PersonListComponent.prototype.close = function (savedPerson) {
        this.showPerson = false;
        if (savedPerson) {
            this.getPersons();
        }
    };
    PersonListComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                _this.getPersons();
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    PersonListComponent.prototype.goBack = function () {
        window.history.back();
    };
    PersonListComponent = __decorate([
        core_1.Component({
            selector: 'person-list',
            templateUrl: 'app/templates/persons/person-list.html',
            directives: [person_details_component_1.PersonDetailsComponent]
        }), 
        __metadata('design:paramtypes', [router_1.Router, router_1.ActivatedRoute, person_service_1.PersonService])
    ], PersonListComponent);
    return PersonListComponent;
}());
exports.PersonListComponent = PersonListComponent;
//# sourceMappingURL=person-list.component.js.map