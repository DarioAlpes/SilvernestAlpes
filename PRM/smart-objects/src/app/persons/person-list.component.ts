import { Component, OnInit } from '@angular/core';
import { Person } from './person';
import { PersonService } from './person.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'person-list',
  templateUrl: 'app/templates/persons/person-list.html'
})
export class PersonListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private personService: PersonService)
    { }

    selectedPerson: Person;

    persons: Person[];

    showPerson = false;

    error: any;

    idClient: number;

    subscriptions: any;

    onSelect(person: Person)
    {
        let link = ['clients', this.idClient, 'persons', person.id];
        this.router.navigate(link);
    }

    onDelete(person: Person)
    {
        this.personService
        .deletePerson(this.idClient, person)
        .then(person => this.getPersons())
        .catch(error => this.error = error);
    }

    getPersons()
    {
        this.personService
        .getPersons(this.idClient)
        .then(persons => this.persons = persons)
        .catch(error => this.error = error);
    }

    addPerson()
    {
        this.showPerson = true;
        this.selectedPerson = new Person();
    }

    close(savedPerson: Person)
    {
        this.showPerson = false;
        if (savedPerson)
        {
            this.getPersons();
        }
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client'] !== undefined)
                {
                    this.idClient = +params['id_client'];
                    this.getPersons();
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
    }

    goBack()
    {
        window.history.back();
    }
}
