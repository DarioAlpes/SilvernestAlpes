import { Component, OnInit } from '@angular/core';
import { Package } from './package';
import { PackageService } from './package.service';
import { Router, ActivatedRoute } from '@angular/router';
import { TableObject } from '../../views/table/table-object';
@Component({
  selector: 'package-list',
  templateUrl: 'app/templates/packages/package-list.html'
})
export class PackageListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private packageService: PackageService)
    { }

    selectedPackage: Package;

    packages: Package[];

    showPackage = false;

    error: any;

    idClient: number;

    subscriptions: any;
    public rows:Array<any> = [];
    public columns:Array<any> = [
        {header: 'ID', field: 'id', filter: false, filtermode:'contains', sortable:true, styleClass: 'idcol'},
        {header: 'Nombre', field: 'name', sort: true, filter: false, filtermode:'startsWith', sortable:true},
        {header: 'Precio', field: 'price', filter: false, filtermode:'startsWith', sortable:true, fieldtype:'currency'},
        {header: 'Válido Desde', field: 'valid-from', filter: false, filtermode:'contains', sortable:true, fieldtype:'date'},
        {header: 'Válido Hasta', field: 'valid-through', filter: false, filtermode:'contains', sortable:true, fieldtype:'date'}
    ];
    public page:number = 1;
    public itemsPerPage:number = 10;
    public maxSize:number = 5;
    public numPages:number = 1;
    public length:number = 0;

    public config:TableObject = {
            paging: true,
            sorting: {columns: this.columns},
            filtering: {filterString: '', columnName: 'name'},
            edit: true,
            delete: true
        };
    public TableData:Array<any> = [];
    public boundCallBack: Function;
    public boundCallBack2: Function;
    public boundCallBackDelete: Function;

    onSelect(pack: Package)
    {
        let link = ['/clients', this.idClient, 'packages', pack.id];
        this.router.navigate(link);
    }

    getPackages()
    {
        this.packageService
        .getPackages(this.idClient)
        .then(packages => this.packages = packages)
        .catch(error => this.error = error);
    }

    addPackage()
    {
      let link = ['/clients', this.idClient, 'packages', 'new'];
      this.router.navigate(link);
    }

    close(savedPackage: Package)
    {
        this.showPackage = false;
        if (savedPackage)
        {
            this.getPackages();
        }
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client']!==undefined)
                {
                    this.idClient = +params['id_client'];
                    this.getPackages();
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
        this.boundCallBack = this.addPackage.bind(this);
        this.boundCallBack2 = this.onSelect.bind(this);
        /**this.boundCallBackDelete = this.onDelete.bind(this);*/
    }

    goBack()
    {
        window.history.back();
    }
}
