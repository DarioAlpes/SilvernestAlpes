(function() {
  'use strict';

  angular
    .module('material-lite')
    .controller('TablesDataController', ['$http', '$scope', 'PlaceholderTextService', 'ngTableParams', '$filter', TablesDataController]);

  function TablesDataController($http, $scope, PlaceholderTextService, ngTableParams, $filter) {

    // adding demo data
    
	
	var data = [];
	$scope.data = data;

    /* jshint newcap: false */
    $scope.tableParams = new ngTableParams({
      page: 1,            // show first page
      count: 10,
      sorting: {
        EPC: 'asc'     // initial sorting
      }
    }, {
      filterDelay: 50,
      total: data.length, // length of data
      getData: function ($defer, params) {
        var searchStr = params.filter().search;
        var mydata = [];

        if (searchStr) {
          searchStr = searchStr.toLowerCase();
          mydata = data.filter(function (item) {
            return item.EPC.toLowerCase().indexOf(searchStr) > -1 || item.SKU.toLowerCase().indexOf(searchStr) > -1;
          });

        } else {
          mydata = data;
        }

        mydata = params.sorting() ? $filter('orderBy')(mydata, params.orderBy()) : mydata;
        $defer.resolve(mydata.slice((params.page() - 1) * params.count(), params.page() * params.count()));
      }
    });
//	$http.defaults.headers.common.Authorization = 'd07220752e57865db08c6e7e6741e00bc705ac0622b36451f260abdd2a992e39';
	$http({method: "GET", url: "https://smartobjectssas.appspot.com/item/list"}).then(function(response) {
		console.log(response.data);
		var data = response.data.valor;
		$scope.data = data;
		$scope.tableParams = new ngTableParams({
		  page: 1,            // show first page
		  count: 10,
		  sorting: {
			EPC: 'asc'     // initial sorting
		  }
		}, {
		  filterDelay: 50,
		  total: data.length, // length of data
		  getData: function ($defer, params) {
			var searchStr = params.filter().search;
			var mydata = [];
			if (searchStr) {
			  searchStr = searchStr.toLowerCase();
			  mydata = data.filter(function (item) {
				return item.EPC.toLowerCase().indexOf(searchStr) > -1 || item.SKU.toLowerCase().indexOf(searchStr) > -1;
			  });

			} else {
			  mydata = data;
			}

			mydata = params.sorting() ? $filter('orderBy')(mydata, params.orderBy()) : mydata;
			$defer.resolve(mydata.slice((params.page() - 1) * params.count(), params.page() * params.count()));
		  }
		});//$scope.myData = response.data.records;
	});
    
  }

})();
