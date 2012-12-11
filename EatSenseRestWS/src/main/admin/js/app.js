'use strict';

/* Config parameters */
var Karazy = {
		environment : "${karazy.environment}"
};

/* Cloobster namespace. Create if not exists.*/
var CloobsterAdmin =  {};

// Declare app level module which depends on filters, and services
CloobsterAdmin.module = angular.module('CloobsterAdmin', ['ngResource']).
  config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
  	$locationProvider.hashPrefix = '!';
  	$routeProvider.when('/main', {templateUrl: 'partials/main.html'});
    $routeProvider.when('/dummydata', {templateUrl: 'partials/dummydata.html'});
    $routeProvider.when('/templates', {templateUrl: 'partials/templates.html', controller: CloobsterAdmin.Templates});
    $routeProvider.when('/trash', {templateUrl: 'partials/trash.html', controller: CloobsterAdmin.TrashCan});
    $routeProvider.when('/configuration', {templateUrl: 'partials/configuration.html'});
    $routeProvider.otherwise({redirectTo: '/main'});
 }]);

CloobsterAdmin.module.directive('alert', function(){
    return {
      restrict: 'A',
      replace: true,
      transclude: false,
      scope: { alert:'=' },
      template: '<div class="alert alert-block fade in" ng-class="alert.type" ng-show="alert.show">'+
	  				'<h4 class="alert-heading" ng-bind="alert.title">Error!</h4>'+
	  				'<span ng-bind="alert.message"></span>'+
	  				'<p><button type="button" class="btn" ng-click="dismissAlert()" data-dismiss="alert" ng-bind="alert.buttonText"></button></p>'+
				'</div>',
      // The linking function will add behavior to the template
      link: function(scope, element, attrs) {
      	scope.dismissAlert = function() {
          scope.alert.show = false;
      		if(angular.isFunction(scope.alert.continueFn)) {
      			scope.alert.continueFn();
      		}
      	};
      }
    };
  });

CloobsterAdmin.module.factory('Template', ['$resource', function($resource){
    return $resource('/admin/services/templates/:id',
      {
        'id': '@id'
      },
      {
        save: {method:'PUT'},
        init: {method:'POST', isArray: true}
      }
    );
}]);

CloobsterAdmin.module.factory('TrashEntry', ['$resource', function($resource){
    return $resource('/admin/services/trash/:id',
      {
        'id': '@id'
      },
      {
        restore: {method:'DELETE', params: {'restore':'true'}}
      }
    );
}]);

/** 
*   @constructor
*   Factory function for the 'loadingService' service.
*   Returns the service.
* 
*   @author Frederik Reifschneider
* @inspiredby https://groups.google.com/forum/#!msg/angular/BbZ7lQgd1GI/GJBTXcJLQMkJ
*/
CloobsterAdmin.module.factory('loadingService', function() {

  var service = {
    /**
  * @name Cloobster.services.loadingService#requestCount
  * 
  * Number of running requests.
  */
    requestCount: 0,
    /**
  * @name Cloobster.services.loadingService#isLoading
  * 
  * Indicates if a request is running.
  * @return true if requestCount > 0, false otherwise
  */
    isLoading: function() {
      return service.requestCount > 0;
    }
  };
  return service;
});

/** 
*   @constructor
*   Factory function for the 'onStartInterceptor' service.
*   Returns the service. Acts as an interceptor for http requests.
* Increases the requestCount of loadingService when a new request starts and sets $rootScope.ajaxLoading  
*
*   @author Frederik Reifschneider
* @inspiredby https://groups.google.com/forum/#!msg/angular/BbZ7lQgd1GI/GJBTXcJLQMkJ
*/
CloobsterAdmin.module.factory('onStartInterceptor', ['loadingService', '$rootScope', function(loadingService, $rootScope) {
  return function (data, headersGetter) {
    loadingService.requestCount++;
    $rootScope.ajaxLoading = loadingService.isLoading();
    return data;
  };
}]);

/** 
*   @constructor
*   Factory function for the 'onCompleteInterceptor' service.
*   Returns the service. Acts as an interceptor for http requests.
* Decreases the requestCount of loadingService when a new request starts and sets $rootScope.ajaxLoading 
* 
*   @author Frederik Reifschneider
* @inspiredby https://groups.google.com/forum/#!msg/angular/BbZ7lQgd1GI/GJBTXcJLQMkJ
*/
CloobsterAdmin.module.factory('onCompleteInterceptor', ['loadingService', '$rootScope','$q', function(loadingService, $rootScope, $q) {
    return function (promise) {
            return promise.then(function (response) {
              loadingService.requestCount--;
                $rootScope.ajaxLoading = loadingService.isLoading();
                return response;

            }, function (response) {
                loadingService.requestCount--;
                $rootScope.ajaxLoading = loadingService.isLoading();
                return $q.reject(response);
            });
    };
}]);
