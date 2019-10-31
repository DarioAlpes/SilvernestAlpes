import { Route }  from '@angular/router';
import { SkuCategoryListComponent } from './sku-category-list.component';
import { SkuCategoryDetailsComponent } from './sku-category-details.component';

export const SKU_CATEGORY_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/sku-categories',
    component: SkuCategoryListComponent
  },
  {
    path: 'clients/:id_client/sku-categories/:id',
    component: SkuCategoryDetailsComponent
  }
];

export const SKU_CATEGORY_ROUTES_COMPONENTS: any[] = [SkuCategoryListComponent, SkuCategoryDetailsComponent]