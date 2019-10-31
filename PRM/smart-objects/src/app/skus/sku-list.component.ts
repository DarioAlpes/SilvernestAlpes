import { Component, OnInit } from '@angular/core';
import { Sku } from './sku';
import { SkuService } from './sku.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'sku-list',
  templateUrl: 'app/templates/skus/sku-list.html'
})
export class SkuListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private skuService: SkuService)
    { }

    selectedSku: Sku;

    skus: Sku[];

    showSku = false;

    error: any;

    idClient: number;

    subscriptions: any;

    public rows:Array<any> = [];
    public columns:Array<any> = [
        {header: 'I', field: 'image-key', sort: true, filter: false, filtermode:'startsWith', sortable:true},
        {header: 'ID', field: 'id', filter: false, filtermode:'contains', sortable:true, styleClass: 'idcol'},
        {header: 'Nombre', field: 'name', sort: true, filter: false, filtermode:'startsWith', sortable:true},
        {header: 'Precio', field: 'cost', sort: true, filter: false, filtermode:'startsWith', sortable:true},
        {header: 'Categoría', field: 'id-sku-category', sort: true, filter: false, filtermode:'contains', sortable:true},
        {header: 'Medida', field: 'measure', sort: true, filter: false, filtermode:'startsWith', sortable:true}
    ];
    public boundCallBack: Function;
    public boundCallBack2: Function;
    public boundCallBackDelete: Function;

    onSelect(sku: Sku)
    {
        let link = ['/clients', this.idClient, 'skus', sku.id];
        this.router.navigate(link);
    }

    getSkus()
    {
        this.skuService
        .getSkus(this.idClient)
        .then(skus => this.skus = skus)
        .catch(error => this.error = error);
    }

    addSku()
    {
      let link = ['/clients', this.idClient, 'skus', 'new'];
      this.router.navigate(link);
    }

    close(savedSku: Sku)
    {
        this.showSku = false;
        if (savedSku)
        {
            this.getSkus();
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
                    this.getSkus();
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
        this.boundCallBack = this.addSku.bind(this);
        this.boundCallBack2 = this.onSelect.bind(this);
        /*this.boundCallBackDelete = this.onDelete.bind(this);*/
    }

    goBack()
    {
        window.history.back();
    }
}
