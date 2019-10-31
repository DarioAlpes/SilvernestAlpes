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
var person_1 = require('./person');
var person_service_1 = require('./person.service');
var router_2 = require('@angular/router');
var PersonDetailsComponent = (function () {
    function PersonDetailsComponent(personService, route, router) {
        this.personService = personService;
        this.route = route;
        this.router = router;
        this.close = new core_1.EventEmitter();
        this.navigated = false;
        this.documentTypes = [{ value: "CC", text: "Cédula de ciudadania" },
            { value: "TI", text: "Tarjeta de identidad" },
            { value: "CE", text: "Cédula de extranjería" }];
        this.genders = [{ value: "male", text: "Masculino" },
            { value: "female", text: "Femenino" }];
        this.person = new person_1.Person();
    }
    PersonDetailsComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.subscriptions = this.route.params.subscribe(function (params) {
            if (params['id_client'] !== undefined) {
                _this.idClient = +params['id_client'];
                if (params['id'] !== undefined) {
                    _this.navigated = true;
                    var id = +params['id'];
                    _this.personService.getPerson(_this.idClient, id)
                        .then(function (person) { return _this.person = person; })
                        .catch(function (error) { return _this.error = error; });
                }
                else {
                    _this.navigated = false;
                    _this.person = new person_1.Person();
                }
            }
            else {
                _this.router.navigate(['/clients']);
            }
        });
    };
    PersonDetailsComponent.prototype.savePerson = function () {
        var _this = this;
        this.personService.savePerson(this.idClient, this.person)
            .then(function (person) {
            _this.person = person;
            _this.goBack(person);
        })
            .catch(function (error) { return _this.error = error; });
    };
    PersonDetailsComponent.prototype.goBack = function (savedPerson) {
        if (savedPerson === void 0) { savedPerson = null; }
        this.close.emit(savedPerson);
        if (this.navigated) {
            window.history.back();
        }
    };
    PersonDetailsComponent.prototype.ngOnDestroy = function () {
        this.subscriptions.unsubscribe();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', person_1.Person)
    ], PersonDetailsComponent.prototype, "person", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', Object)
    ], PersonDetailsComponent.prototype, "close", void 0);
    PersonDetailsComponent = __decorate([
        core_1.Component({
            selector: 'person-detail',
            templateUrl: 'app/templates/persons/person-details.html',
            directives: [router_2.ROUTER_DIRECTIVES]
        }), 
        __metadata('design:paramtypes', [person_service_1.PersonService, router_1.ActivatedRoute, router_1.Router])
    ], PersonDetailsComponent);
    return PersonDetailsComponent;
}());
exports.PersonDetailsComponent = PersonDetailsComponent;
//# sourceMappingURL=person-details.component.js.map