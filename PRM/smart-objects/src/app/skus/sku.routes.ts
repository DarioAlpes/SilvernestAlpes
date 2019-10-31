import { Route }  from '@angular/router';
import { SkuListComponent } from './sku-list.component';
import { SkuDetailsComponent } from './sku-details.component';
import { SKU_CATEGORY_ROUTES, SKU_CATEGORY_ROUTES_COMPONENTS } from './categories/sku-category.routes';

const SKU_SPECIFIC_ROUTES: Route[] = [
  {
    path: 'clients/:id_client/skus',
    component: SkuListComponent
  },
  {
    path: 'clients/:id_client/skus/:id',
    component: SkuDetailsComponent
  }
];

export const SKU_ROUTES: Route[] = SKU_SPECIFIC_ROUTES.concat(SKU_CATEGORY_ROUTES);

export const SKU_ROUTES_COMPONENTS: any[] = [SkuListComponent, SkuDetailsComponent]
                                            .concat(SKU_CATEGORY_ROUTES_COMPONENTS)