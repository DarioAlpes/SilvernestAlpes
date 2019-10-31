import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Person } from './person';
import { PersonService } from './person.service';
import {UploadFileService} from "../utils/upload-file.service";
@Component({
  selector: 'person-detail',
  templateUrl: 'app/templates/persons/person-details.html',
  providers: [UploadFileService]
})
export class PersonDetailsComponent implements OnInit, OnDestroy
{
  @Input()
  person: Person;

  @Output()
  close = new EventEmitter();

  subscriptions: any;
  navigated = false;
  error: any;
  file: any;
  idClient: number;
  fileUrl: string;
  imageUrl: string;
  public boundCallBack: Function;
  public boundImageCallback: Function;

  documentTypes: any[] = [{ value: "CC", label: "Cédula de ciudadania" },
    { value: "TI", label: "Tarjeta de identidad" },
    { value: "CE", label: "Cédula de extranjería" },
    {value: "PP", label: "Pasaporte" }];

  genders: any[] = [{ value: "male", label: "Masculino" },
    { value: "female", label: "Femenino" }];

  types: any[] = [{ value: "ADULTO", label: "Adulto" },
    { value: "BEBE", label: "Bebe" },
    { value: "NIÑO", label: "Niño" },
    { value: "ADULTO MAYOR", label: "Adulto Mayor" }];

  categories: any[] = [{ value: "A", label: "Categoría A" },
    { value: "B", label: "Categoría B" },
    { value: "C", label: "Categoría C" },
    { value: "D", label: "Categoría D" }];

  affiliations: any[] = [{ value: "COTIZANTE", label: "Cotizante" },
    { value: "BENEFICIARIO", label: "Baeneficiario" }];

  fields:Array<any> = [
    {id: 'id', label: 'Id', type:'noedit'},
    {id: 'name', label: 'Nombre', type:'input'},
    {id: 'document-type', label: 'Tipo documento', type:'dropdown', options:this.documentTypes},
    {id: 'document-number', label: 'Número documento', type:'input'},
    {id: 'mail', label: 'Email', type:'input'},
    {id: 'nationality', label: 'Nacionalidad', type:'input'},
    {id: 'profession', label: 'Profession', type:'input'},
    {id: 'city', label: 'Ciudad', type:'input'},
    {id: 'company', label: 'Compañia', type:'input'},
    {id: 'birthdate', label: 'Fecha nacimiento', type:'input'},
    {id: 'gender', label: 'Genero', type:'dropdown', options:this.genders},
    {id: 'type', label: 'Tipo', type:'dropdown', options:this.types},
    {id: 'category', label: 'Categoria', type:'dropdown', options:this.categories},
    {id: 'affiliation', label: 'Afiliacion', type:'dropdown', options:this.affiliations}
  ];

  constructor(private personService: PersonService, private uploadService: UploadFileService, private route: ActivatedRoute, private router: Router)
  {
    this.person = new Person();
  }

  ngOnInit()
  {
    this.subscriptions = this.route.params.subscribe(
      params =>
      {
        if(params['id_client']!==undefined)
        {
          this.idClient = +params['id_client'];
          if(params['id'] !== undefined)
          {
            this.navigated = true;
            let id = +params['id'];
            this.personService.getPerson(this.idClient, id)
              .then(person => this.person = person)
              .then(() => this.imageUrl = PersonService.getPersonUrl(this.idClient, this.person.id) + 'image/')
              .catch(error => this.error = error);
          }
          else
          {
            this.navigated = false;
            this.person = new Person();
          }
        }
        else
        {
          this.router.navigate(['clients']);
        }
      }
    );
    this.boundCallBack = this.savePerson.bind(this);
    this.boundImageCallback = this.setFile.bind(this);
  }

  savePerson()
  {
    if(this.file)
    {
      this.personService.savePerson(this.idClient, this.person)
        .then
        (
          person =>
          {
            this.person = person;
            this.personService.getImagetUrl(this.idClient, this.person)
              .then
              (
                urlObject =>
                {
                  this.fileUrl = urlObject.url;
                  this.uploadService.makeFileRequest(this.fileUrl, this.file).subscribe(() => {
                    this.goBack(person);
                  });
                }
              )
              .catch(error => this.error = error);
          }
        )
        .catch(error => this.error = error);
    }
    else {
      alert("Se debe escoger una imagen");
    }
  }

  setFile(event)
  {
    if(event.target && event.target.files && event.target.files.length>0) {
      this.file = event.target.files[0];
    }
    else {
      this.file = undefined;
    }
  }

  goBack(savedPerson: Person = null)
  {
    this.close.emit(savedPerson);
    if (this.navigated)
    {
      window.history.back();
    }
  }

  ngOnDestroy()
  {
    this.subscriptions.unsubscribe();
  }
}
