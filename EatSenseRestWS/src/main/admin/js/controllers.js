/** @module CloobsterAdmin */
'use strict';

/**
* 	@name CloobsterAdmin.Navigation 
*	@requires $location
*
* 	Checks the active location path.
* 	@constructor
*/
CloobsterAdmin.Navigation = function($scope, $location) {
	var location;

	/**
	*
	*/
	$scope.getActive = function(path) {

		location = $location.path();
		return (location.indexOf(path) === 0) ? "active" : "";
	};
};
CloobsterAdmin.Navigation.$inject = ['$scope', '$location'];

/**
* 	@name CloobsterAdmin.Import
*	@requires $http
*
* 	Manages data import.
* 	@constructor
*/
CloobsterAdmin.Import = function($scope, $http, $anchorScroll) {
	
	function showAlert( type, title, message, buttonText, continueFn) {
		$scope.importAlert.type = type;
		$scope.importAlert.show = true;
		$scope.importAlert.message = message;
		$scope.importAlert.title = title;
		$scope.importAlert.buttonText = buttonText;
		$scope.importAlert.continueFn =  continueFn;
	}

	function dismissAlert() {
		$scope.importAlert = { show: false, type: "alert-error", message: "", title: "", buttonText:"Action", continueFn: dismissAlert};
	}

	function setError(message) {
		showAlert("alert-error", "Error!", message, "Try again", resetForm);
	}

	function setProgress( progress ) {
		$scope.importProgressStyle = { width: progress };
	}

	function importError(data, status) {
		var message = (data.message) ?
			data.message
			: "An unknown error occured, check the server and your connection.";
		setError(message);
	}

	function importSuccess() {
		setProgress("100%");
		showAlert("alert-success", "Done!", "Import done.", "Import more", resetForm);
	}

	function resetForm() {
		$scope.jsonData = "";
		$scope.importProgress = false;
		setProgress("0%");
	}

	$scope.import = function() {
		var dto;
		$scope.importProgress = true;
		try {
			dto = angular.fromJson($scope.jsonData);	
		}
		catch(err) {
			setProgress("30%");
			setError("JSON parsing error: " + err.message);
			return;
		}
		$http.post($scope.importUrl, dto).success(importSuccess)
		.error(importError);
	}

	dismissAlert();
	setProgress("0%");
	resetForm();

	$anchorScroll();
}
CloobsterAdmin.Import.$inject = ['$scope', '$http', '$anchorScroll'];

CloobsterAdmin.Functions = function($scope, $http) {
	$scope.deleteFunctionsDisabled = (Karazy.environment === "prod")? true : false;
	$scope.confirmDeleteAllDisabled = false;
	$scope.confirmDeleteLiveDisabled = false;

	$scope.deleteAllData = function() {
		$scope.confirmDeleteAllText = "Deleting ...";
		$scope.confirmDeleteAllDisabled = true;
		$http.delete('/admin/services/datastore/all').success(function() {
				$scope.confirmDeleteAllText = "All data deleted.";
			}).error(function (data, status) {
				$scope.confirmDeleteAllText = status + " error.";
			});
	};

	$scope.deleteLiveData = function() {
		$scope.confirmDeleteLiveText = "Deleting ...";
		$scope.confirmDeleteLiveDisabled = true;
		$http.delete('/admin/services/datastore/live').success(function() {
				$scope.confirmDeleteLiveText = "Live data deleted.";
			}).error(function (data, status) {
				$scope.confirmDeleteLiveText = status + " error.";
			});
	};

	$scope.createDummieAccounts = function() {
		$scope.createDummieAccountsText = "Creating ...";
		$scope.createDummieAccountsDisabled = true;
		$http.post('/admin/services/accounts/dummies', {}).success(function() {
				$scope.createDummieAccountsText = "Accounts created.";
			}).error(function (data, status) {
				$scope.createDummieAccountsText = status + " error.";
			});	
	};
}

CloobsterAdmin.Functions.$inject = ['$scope', '$http'];

CloobsterAdmin.SelectBusiness = function($scope, $http) {
	$scope.businessSelected = false;
	$scope.message = "Loading businesses ...";
	$scope.business = {};

	$http.get('/admin/services/businesses').success( function(data ) {
		delete $scope.message;
		$scope.businesses = data;
		$scope.$watch('business', function(newVal, old, scope) {
			if(newVal.hasOwnProperty('id')) {
				scope.businessSelected = true;
			scope.importUrl = '/admin/services/businesses/'+newVal.id+'/feedbackforms';	
			}
		});
		$scope.business = $scope.businesses[0];
	}).error(function(status, data ) {
		$scope.message = status + " Error loading businesses."
	});
}
CloobsterAdmin.SelectBusiness.$inject = ['$scope', '$http'];

CloobsterAdmin.Templates = function($scope, Template) {

	function showAlert( type, title, message, buttonText, continueFn) {
		$scope.importAlert.type = type;
		$scope.importAlert.show = true;
		$scope.importAlert.message = message;
		$scope.importAlert.title = title;
		$scope.importAlert.buttonText = buttonText;
		$scope.importAlert.continueFn =  continueFn;
	}

	function dismissAlert() {
		$scope.importAlert = { show: false, type: "alert-error", message: "", title: "", buttonText:"Action", continueFn: dismissAlert};
	}

	function setTemplate() {
		if($scope.templates.length > 0) {
			$scope.template = $scope.templates[0];
		}
	}

	$scope.templates = Template.query(setTemplate);

	$scope.initTemplates = function() {
		$scope.templates = Template.init({}, setTemplate);
	};

	$scope.saveTemplate = function() {
		if($scope.editTemplateForm.$valid) {
			$scope.template.$save({}, angular.noop, function(data, status) {
				// Error callback.
				showAlert("alert-error", "Error code"+ status, "Error", "Close");
			});
		}
	};

	dismissAlert();
}
CloobsterAdmin.Templates.$inject = ['$scope', 'Template'];
