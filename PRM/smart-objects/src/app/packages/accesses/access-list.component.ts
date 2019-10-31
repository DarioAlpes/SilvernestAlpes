import { Component, OnInit } from '@angular/core';
import { Access } from './access';
import { AccessService } from './access.service';
import { Router, ActivatedRoute } from '@angular/router';
import { LocationService } from '../../locations/location.service';
import { Location } from '../../locations/location';


@Component({
  selector: 'accesses-list',
  templateUrl: 'app/templates/packages/accesses/access-list.html'
})
export class AccessListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private accessService: AccessService, private locationService: LocationService)
    { }

    selectedAccess: Access;

    accesses: Access[];

    showAccess = false;

    error: any;

    locations: Location[];

    idClient: number;

    idPackage: number;

    subscriptions: any;

    columns: Array<any>;

    data: Array<any>;

    public boundCallBack: Function;

    public boundCallBack2: Function;

    public boundCallBackDelete: Function;

    onSelect(access: Access)
    {
        let link = ['/clients', this.idClient, '/packages', this.idPackage, '/accesses', access.id];
        this.router.navigate(link);
    }

    getAccesses()
    {
        this.accessService
        .getAccesses(this.idClient, this.idPackage)
        .then(accesses => {this.accesses = accesses;
        this.data = this.accesses;
        this.locationService.getLocations(this.idClient)
            .then(locations => this.locations = locations)
        .then(() =>
              {
                for(var i=0; i<this.data.length; i++)
                {
                    var index = this.locations.findIndex(x => x.id==this.data[i]['id-location']);
                    this.data[i].location = this.locations[index].name;
                    if(this.data[i]['unlimited-amount'])
                    {
                      this.data[i].qty = 'ilimitado';
                    }
                    else{
                      this.data[i].qty = this.data[i]['amount-included'];
                    }
                }
                })})
        .catch(error => this.error = error);
    }


    addAccess()
    {
        this.showAccess = true;
        this.selectedAccess = new Access();
    }

    close(savedAccess: Access)
    {
        this.showAccess = false;
        if (savedAccess)
        {
            this.getAccesses();
        }
    }

    ngOnInit()
    {
      this.columns = [
      {field:'qty', class:'bottom w100', decorator:'X'},
      {field:'location', class:'top left w80'},
      {field:'id', class:'top right w20', edit:true}
      ];
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client'] !== undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id_package'] !== undefined)
                    {
                        this.idPackage = +params['id_package'];
                        this.getAccesses();
                    }
                    else
                    {
                        this.router.navigate(['/clients', this.idClient, '/packages']);
                    }
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );

        this.boundCallBack = this.addAccess.bind(this);
        this.boundCallBack2 = this.onSelect.bind(this);
        /*this.boundCallBackDelete = this.onDelete.bind(this);*/
    }

    goBack()
    {
        window.history.back();
    }
}
