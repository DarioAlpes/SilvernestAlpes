import { Component, OnInit } from '@angular/core';
import { SkuCategory } from './sku-category';
import { SkuCategoryService } from './sku-category.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'sku-category-list',
  templateUrl: 'app/templates/skus/categories/category-list.html'
})
export class SkuCategoryListComponent implements OnInit
{
    constructor(private router: Router, private route: ActivatedRoute, private categoryService: SkuCategoryService)
    { }

    public rows:Array<any> = [];
    public columns:Array<any> = [
        {header: 'ID', field: 'id', filter: false, filtermode:'contains', sortable:true, styleClass: 'idcol'},
        {header: 'Llave', field: 'key', filter: false,sort: true, filtermode:'contains', sortable:true, styleClass: 'idcol'},
        {header: 'Nombre', field: 'name', sort: true, filter: false, filtermode:'startsWith', sortable:true}
    ];

    selectedCategory: SkuCategory;

    categories: SkuCategory[];

    showCategory = false;

    error: any;

    idClient: number;

    subscriptions: any;

    public boundCallBack: Function;
    public boundCallBack2: Function;
    /**public boundCallBackDelete: Function;*/

    onSelect(category: SkuCategory)
    {
        let link = ['/clients', this.idClient, 'sku-categories', category.id];
        this.router.navigate(link);
    }

    getCategories()
    {
        this.categoryService
        .getSkuCategories(this.idClient)
        .then(categories => {this.categories = categories;
        console.log(categories);})
        .catch(error => this.error = error);
    }

    addCategory()
    {
      let link = ['/clients', this.idClient, 'sku-categories', 'new'];
      this.router.navigate(link);
    }

    close(savedCategory: SkuCategory)
    {
        this.showCategory = false;
        if (savedCategory)
        {
            this.getCategories();
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
                    this.getCategories();
                }
                else
                {
                    this.router.navigate(['/clients']);
                }
            }
        );
        this.boundCallBack = this.addCategory.bind(this);
        this.boundCallBack2 = this.onSelect.bind(this);
        /**this.boundCallBackDelete = this.onDelete.bind(this);*/
    }

    goBack()
    {
        window.history.back();
    }
}
