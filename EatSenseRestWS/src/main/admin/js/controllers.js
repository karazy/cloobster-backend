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

/**
* 	@name CloobsterAdmin.Configuration
*	@requires $http
*
* 	Manages data import.
* 	@constructor
*/
CloobsterAdmin.Configuration = function($scope, $http, $anchorScroll, $timeout) {
	
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

	function importSuccess(data) {
		setProgress("100%");
		showAlert("alert-success", "Done!", "Import done.");
		$scope.defaultFeedbackForm = angular.toJson(data, true);

		$timeout(function() {
			resetForm();
		});
	}

	function resetForm() {
		$scope.importProgress = false;
		setProgress("0%");
	}

	$scope.saveDefaultFeedback = function() {
		var dto;
		$scope.importProgress = true;
		try {
			dto = angular.fromJson($scope.defaultFeedbackForm);
		}
		catch(err) {
			setProgress("30%");
			setError("JSON parsing error: " + err.message);
			return;
		}
		$http.put("/admin/services/configuration/defaultfeedbackform", dto).success(importSuccess)
		.error(importError);
	}

	dismissAlert();
	setProgress("0%");
	resetForm();

	$scope.defaultFeedbackForm = "Loading ...";
	$http.get("/admin/services/configuration/defaultfeedbackform").success(function(data) {
		$scope.defaultFeedbackForm = angular.toJson(data, true);
	});


	$anchorScroll();
}
CloobsterAdmin.Configuration.$inject = ['$scope', '$http', '$anchorScroll', '$timeout'];

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

	$scope.sendCockpitUpdateMessage = function() {
		$scope.sendCockpitUpdateMessageText = "Creating ...";
		$scope.sendCockpitUpdateMessageDisabled = true;
		$http.post('/admin/services/channels/messages', {'type':'application', 'action': 'update'}).success(function() {
				$scope.sendCockpitUpdateMessageText = "Message sent.";
			}).error(function (data, status) {
				$scope.sendCockpitUpdateMessageText = status + " error.";
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


CloobsterAdmin.TrashCan = function($scope, TrashEntry) {
	$scope.restoring = [];
	$scope.trashEntries = TrashEntry.query( function() {
		angular.forEach($scope.trashEntries, function(value, index) {
			$scope.restoring[index] = false;
		});
	});
	
	

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
		showAlert("alert-error", "Error!", message, "Close");
	}


	$scope.restore = function(index) {
		var entry = $scope.trashEntries[index];
		$scope.restoring[index] = true;
		entry.$restore(function() {
			$scope.restoring[index] = false;
			$scope.trashEntries.splice(index,1);
		}, function(response) {
			$scope.restoring[index] = true;
			setError("Could not restore entity, http code:" + response.status+ ", message: " + response.message);
		});
	};

	dismissAlert();
};
CloobsterAdmin.TrashCan.$inject = ['$scope','TrashEntry'];

CloobsterAdmin.Fixes = function($scope, $http) {
	
	$scope.fixBusinessesTypo = function() {
		$http.put('/admin/services/accounts/fixbusinesses')
		.success(function(data, status) {
			alert('Successfully applied fix!');
		}).error(function(data, status) {
			alert('fix failed! ' + status);
		});
	};
	
};
CloobsterAdmin.Fixes.$inject = ['$scope', '$http'];

/**
* 	@name CloobsterAdmin.Import
*	@requires $http
*
* 	Manages data import.
* 	@constructor
*/
CloobsterAdmin.InfoPages = function($scope, $http, $anchorScroll) {
	
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
		showAlert("alert-success", "Done!", "All pages generated.", "Okay");
	}

	function resetForm() {
		$scope.infoPageCount = 30;
		$scope.generateProgress = false;
		setProgress("0%");
	}

	$scope.generate = function() {
		var promise,
			config = {};
		$scope.generateProgress = true;

		if($scope.useLang) {
			if(!$scope.lang) {
				// Error no language entered.
				setError("Enter a language key.");
				return;
			}

			// Set language header.
			config['headers'] = {
				'Content-Language': $scope.lang
			}
		}
		$http.post('/admin/services/businesses/'+$scope.business.id+'/infopages/'+$scope.infoPageCount, {}, config)
			.success(importSuccess)
			.error(importError);
	}

	dismissAlert();
	resetForm();

	$anchorScroll();
}
CloobsterAdmin.InfoPages.$inject = ['$scope', '$http', '$anchorScroll'];
