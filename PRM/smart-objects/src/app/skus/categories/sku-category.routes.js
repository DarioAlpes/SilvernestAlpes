"use strict";
var sku_category_list_component_1 = require('./sku-category-list.component');
var sku_category_details_component_1 = require('./sku-category-details.component');
exports.SKU_CATEGORY_ROUTES = [
    {
        path: 'clients/:id_client/sku-categories',
        component: sku_category_list_component_1.SkuCategoryListComponent
    },
    {
        path: 'clients/:id_client/sku-categories/:id',
        component: sku_category_details_component_1.SkuCategoryDetailsComponent
    }
];
exports.SKU_CATEGORY_ROUTES_COMPONENTS = [sku_category_list_component_1.SkuCategoryListComponent, sku_category_details_component_1.SkuCategoryDetailsComponent];
//# sourceMappingURL=sku-category.routes.js.map