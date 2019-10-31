import { Component, Input, EventEmitter, OnInit, OnDestroy, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SkuCategory } from './sku-category';
import { SkuCategoryService } from './sku-category.service';
import { Validators } from '@angular/forms';

@Component({
  selector: 'sku-category-detail',
  templateUrl: 'app/templates/skus/categories/category-details.html'
})
export class SkuCategoryDetailsComponent implements OnInit, OnDestroy
{
    @Input()
    category: SkuCategory;

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

    constructor(private categoryService: SkuCategoryService, private route: ActivatedRoute, private router: Router)
    {

    }

    ngOnInit()
    {
        this.subscriptions = this.route.params.subscribe(
            params =>
            {
                if(params['id_client']!==undefined)
                {
                    this.idClient = +params['id_client'];
                    if(params['id'] !== undefined && params['id'] !== 'new')
                    {
                        this.navigated = true;
                        let id = +params['id'];
                        this.categoryService.getSkuCategory(this.idClient, id)
                            .then(category => {this.category = category;
                            console.log(category);})
                            .catch(error => this.error = error);
                    }
                    else
                    {
                      if(params['id'] == 'new' )
                      {
                        this.navigated = true;
                        this.category = new SkuCategory();
                      }
                      else
                      {
                        this.navigated = false;
                      }
                    }
                    this.categoryService.getSkuCategories(this.idClient)
                        .then(categories =>
                                {
                                  this.categories = categories;
                                })
                          .then(() =>
                                {
                                  this.options = [];
                                  this.options.push({label:"Ninguno", value: null});
                                  for(var item of this.categories)
                                  {
                                      this.options.push({label: item.name, value: item.id});
                                  }
                                  this.fields = [
                                    {id: 'id',label: 'Id', type:'noedit'},
                                    {id: 'key',label: 'Llave', type:'noedit'},
                                    {id: 'name', label: 'Nombre', type:'input', validations: [Validators.required] },
                                    {id: 'id-parent-category', label: 'Padre', type:'dropdown', options: this.options, validations: [Validators.required] }
                                  ];})
                        .catch(error => this.error = error);
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
        this.boundCallBack = this.saveCategory.bind(this);
    }

    saveCategory()
    {
        console.log('ejem');
        this.categoryService.saveSkuCategory(this.idClient, this.category)
            .then
                (
                    category =>
                    {
                        this.category = category;
                        this.goBack(category);
                    }
                )
            .catch(error => this.error = error);
    }

    goBack(savedCategory: SkuCategory = null)
    {
        this.close.emit(savedCategory);
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
