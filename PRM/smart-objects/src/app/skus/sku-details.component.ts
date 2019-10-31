import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Sku } from './sku';
import { SkuService } from './sku.service';
import { SkuCategory } from './categories/sku-category';
import { SkuCategoryService } from './categories/sku-category.service';
import { Control } from "@angular/common";
import { Validators } from '@angular/forms';

@Component({
  selector: 'sku-detail',
  templateUrl: 'app/templates/skus/sku-details.html'
})
export class SkuDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    sku: Sku;

    @Output()
    close = new EventEmitter();

    subscriptions: any;
    navigated = false;
    error: any;
    idClient: number;
    categories: SkuCategory[];
    options:any;
    public boundCallBack: Function;
    public fields:Array<any>;

    constructor(private skuService: SkuService, private categoryService: SkuCategoryService,
                private route: ActivatedRoute, private router: Router)
    {
        this.sku = new Sku();
    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
              if(params['id_client']!==undefined)
              {
                this.idClient = +params['id_client'];
                  if(params['id_client']!==undefined && params['id'] !== 'new')
                  {
                      this.idClient = +params['id_client'];
                      if(params['id'] !== undefined)
                      {
                          this.navigated = true;
                          let id = +params['id'];
                          this.skuService.getSku(this.idClient, id)
                              .then(sku => this.sku = sku)
                              .catch(error => this.error = error);
                      }
                      else
                      {
                          this.navigated = false;
                          this.sku = new Sku();
                      }
                      this.categoryService.getSkuCategories(this.idClient)
                          .then(categories => this.categories = categories)
                          .catch(error => this.error = error);
                  }
                  else
                  {
                    if(params['id'] == 'new' )
                    {
                      this.navigated = true;
                      this.sku = new Sku();
                    }
                    else
                    {
                      this.navigated = false;
                    }
                      /*this.router.navigate(['/clients']);*/
                  }
                  this.categoryService.getSkuCategories(this.idClient)
                      .then(categories =>
                              {
                                this.categories = categories;
                              })
                        .then(() =>
                              {
                                this.options = [];
                                for(var item of this.categories)
                                {
                                    this.options.push({label: item.name, value: item.id});
                                }
                                this.fields = [
                                    {id: 'id',label: 'Id', type:'noedit'},
                                    {id: 'name', label: 'Nombre', type:'input', validations: [Validators.required] },
                                    {id: 'cost', label: 'Precio', type:'mask', fieldtype:'number', alias:'currency', validations: [Validators.required] },
                                    {id: 'id-sku-category', label: 'Categoría', options: this.options, type:'dropdown'},
                                    {id: 'measure', label: 'Medida', type:'input', validations: [Validators.required] }
                                ];
                                })
                      .catch(error => this.error = error);
                    }
            }
        );
            this.boundCallBack = this.saveSku.bind(this);
    }

    saveSku()
    {
        this.skuService.saveSku(this.idClient, this.sku)
            .then
                (
                    sku =>
                    {
                        this.sku = sku;
                        this.goBack(sku);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedSku: Sku = null)
    {
        this.close.emit(savedSku);
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
