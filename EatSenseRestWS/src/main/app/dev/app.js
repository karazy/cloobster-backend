Ext.Loader.setConfig({
	enabled : true,
	//WORKAORUND related to Android 3x Bug and Webview URL handling
	disableCaching: Karazy.config.disableCaching
});

Ext.Loader.setPath('EatSense', 'app');

Ext.application({
	name : 'EatSense',
	controllers : [ 'CheckIn', 'Menu', 'Order', 'Settings', 'Request', 'Message' ],
	models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Choice', 'Option', 'Order', 'Error', 'Spot', 'Bill', 'PaymentMethod', 'Request'],
	views : [ 'Main', 'Dashboard', 'Checkinconfirmation', 'CheckinWithOthers', 'MenuOverview', 'ProductOverview', 'ProductDetail', 'OrderDetail', 'OptionDetail', 'Cart', 'Menu', 'Lounge'], 
	stores : [ 'CheckIn', 'User', 'Spot', 'AppState', 'Menu', 'Product', 'Order', 'Bill'],
	phoneStartupScreen: 'res/images/startup.png',
	tabletStartupScreen: 'res/images/startup.png',
	requires: [
		//require most common types
		'Ext.Container',
		'Ext.Panel',
		'Ext.dataview.List',
		'Ext.Label',
		'Ext.TitleBar',
		//require custom types
		'EatSense.override.CustomRestProxy',
		'EatSense.override.OperationImprovement', 
		'EatSense.view.fragment.DashboardButton', 
		'EatSense.override.RadioOverride', 
		'EatSense.model.AppState'
	],
	launch : function() {
		console.log('launch');		
    	this.launched = true;
        this.mainLaunch();
	},
	mainLaunch : function() {
		if (cordovaInit == false || !this.launched) {
        	return;
        }

		console.log('mainLaunch');
		
		var app = this,
	   		appStateStore = Ext.data.StoreManager.lookup('appStateStore'),
	 		checkInCtr = this.getController('CheckIn'),
	 		restoredCheckInId,
	 		profile = Ext.os.deviceType.toLowerCase();	 

		//global error handler
		window.onerror = function(message, url, lineNumber) {  
			var messageCtr = app.getController('Message');
			console.error('unhandled error > %s in %s at %s', message, url, lineNumber);
		  	//messageCtr.reopenChannel();
		  	//prevent firing of default handler (return true)
		  	return false;
		}; 

		//timeout for requests
		Ext.Ajax.timeout = 1200000;
		
    	//try to restore application state
	   	 
	   	 //create main screen
	   	 Ext.create('EatSense.view.Main');
	   	 
	   	 try {
	   		appStateStore.load();
	   	 } catch (e) {
	   		appStateStore.removeAll();
	   	 }
	     
	     
	   	 if(appStateStore.getCount() == 1) {
	   		 console.log('app state found');	   		 
		   	checkInCtr.setAppState(appStateStore.getAt(0));
	   		restoredCheckInId = checkInCtr.getAppState().get('checkInId');
	   	 }
	   		
	   	 if(restoredCheckInId) {
	   			 //reload old state
	   			 EatSense.model.CheckIn.load(restoredCheckInId, {
	   				scope: this,
	   				success : function(record, operation) {
	   					console.log('found existing checkin '+record);	
	   					checkInCtr.restoreState(record);

	   					// if(record.get('status') == Karazy.constants.CHECKEDIN || record.get('status') == Karazy.constants.ORDER_PLACED) {	   						
		   				// 	checkInCtr.restoreState(record);
	   					// } else {
	   					// 	appStateStore.add(checkInCtr.getAppState());
		   		  		//checkInCtr.showDashboard();
	   					// }	   						   				
	   				},
	   				failure: function(record, operation) {
	   					console.log('error restoring state');
	   					appStateStore.removeAll();
	   					appStateStore.sync();
	   					appStateStore.add(checkInCtr.getAppState());
	   		   		 	checkInCtr.showDashboard();
	   				}
	   			 });	   			 
	   	 }	   		 	   	 	   	 
	   	 else {	   		 
	   		if (appStateStore.getCount() > 1){
		   		 console.log('Too many appStates! Clearing cache. this should never happen.');
		   		 appStateStore.removeAll();
		   	 } else {
		   		console.log('no app state found.');
		   	 } 		   		 
	   		 appStateStore.add(checkInCtr.getAppState());
	   		 checkInCtr.showDashboard();
	   	 }	
	},
	/**
    *   Gloabl handler that can be used to handle errors occuring from server requests.
    *   @param options
    *       Configuration object
    *      
    *       error: error object containing status and statusText.
    *       forceLogout: a critical permission error occured and the user will be logged out
    *       true to logout on all errors 
    *       OR
    *       {errorCode : true|false} e.g. {403: true, 404: false}
    *       hideMessage: true if you don't want do display an error message
    *       message: message to show. If no message is set a default message will be displayed
    */
    handleServerError: function(options) {
        var    errMsg,
               nestedError,
               loginCtr = this.getController('Login'),
               error = options.error,
               forceLogout = options.forceLogout,
               hideMessage = options.hideMessage,
               message = options.message;
        if(error && error.status) {
            switch(error.status) {
                case 403:
                    //no access
                    errMsg = Karazy.i18n.translate('errorPermission');
                    if(forceLogout[403] === true || forceLogout === true) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                    break;
                case 404:
                    //could not load resource or server is not reachable
                    errMsg = Karazy.i18n.translate('errorResource');
                    if(forceLogout[404] === true || forceLogout === true) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                    break;
                default:
                    try {
                         //TODO Bug in error message handling in some browsers
                        nestedError = Ext.JSON.decode(error.statusText);
                        errMsg = Karazy.i18n.translate(nestedError.errorKey,nestedError.substitutions);                        
                    } catch (e) {
                        errMsg = Karazy.i18n.translate('errorMsg');
                    }
                    if(forceLogout[500] === true || forceLogout === true) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }                                         
                    break;
            }
        }
        if(!hideMessage) {
        	Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), (message) ? message : errMsg, Ext.emptyFn);	
        }
    }
});

