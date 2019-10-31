"use strict";
var sku_list_component_1 = require('./sku-list.component');
var sku_details_component_1 = require('./sku-details.component');
var sku_category_routes_1 = require('./categories/sku-category.routes');
var SKU_SPECIFIC_ROUTES = [
    {
        path: 'clients/:id_client/skus',
        component: sku_list_component_1.SkuListComponent
    },
    {
        path: 'clients/:id_client/skus/:id',
        component: sku_details_component_1.SkuDetailsComponent
    }
];
exports.SKU_ROUTES = SKU_SPECIFIC_ROUTES.concat(sku_category_routes_1.SKU_CATEGORY_ROUTES);
exports.SKU_ROUTES_COMPONENTS = [sku_list_component_1.SkuListComponent, sku_details_component_1.SkuDetailsComponent]
    .concat(sku_category_routes_1.SKU_CATEGORY_ROUTES_COMPONENTS);
//# sourceMappingURL=sku.routes.js.map