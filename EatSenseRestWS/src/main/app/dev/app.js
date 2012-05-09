Ext.Loader.setConfig({
	enabled : true,
	//WORKAORUND related to Android 3x Bug and Webview URL handling
	disableCaching: Karazy.config.disableCaching
});

Ext.Loader.setPath('EatSense', 'app');

Ext.application({
	name : 'EatSense',
	controllers : [ 'CheckIn', 'Menu', 'Order', 'Settings', 'Request', 'Message' ],
	models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Choice', 'Option', 'Order', 'Cart', 'Error', 'Spot', 'Bill', 'PaymentMethod', 'Request', 'Newsletter'],
	views : [ 'Main', 'Dashboard', 'Checkinconfirmation', 'CheckinWithOthers', 'MenuOverview', 'ProductOverview', 'ProductDetail', 'OrderDetail', 'OptionDetail', 'Cart', 'Menu', 'Lounge', 'Newsletter'], 
	stores : [ 'CheckIn', 'User', 'Spot', 'AppState', 'Menu', 'Product', 'Order', 'Bill', 'Request'],
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
		var me = this;

    me.androidBackHandler = new Array();
		
		if (cordovaInit == false || !this.launched) {
        	return;
        }

		console.log('mainLaunch');
		
		var appStateStore = Ext.data.StoreManager.lookup('appStateStore'),
	 		checkInCtr = this.getController('CheckIn'),
	 		restoredCheckInId; 

		//global error handler
		// window.onerror = function(message, url, lineNumber) {  
		// 	console.error('unhandled error > ' + message +' in '+ url +' at '+ lineNumber);
		//   	//prevent firing of default handler (return true)
		//   	return false;
		// };

  		//timeout for requests
  		Ext.Ajax.timeout = 1200000;

      //Android specific behaviour
      if (Ext.os.is.Android) {
        document.addEventListener('backbutton', onBackKeyDown, false);
        function onBackKeyDown() {            
            if(me.androidBackHandler && me.androidBackHandler.length > 0) {
              console.log('fire backbutton event');
              me.androidBackHandler.pop()();
            }
        };
      }
		
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
	   	 	//TODO refactor
	   	 	Ext.Ajax.setDefaultHeaders({
            	'checkInId': restoredCheckInId
       		});

   			 //reload old state
   			 EatSense.model.CheckIn.load(restoredCheckInId, {
   				scope: this,
   				success : function(record, operation) {
   					console.log('found existing checkin '+record);	
   					checkInCtr.restoreState(record);  						   				
   				},
   				failure: function(record, operation) {
   					console.log('error restoring state');
   					//TODO refactor
   					Ext.Ajax.setDefaultHeaders({});
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

	//Global utility methods
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
    *       message: message to show. If no message is set a default message will be displayed.
    *		can be either a common message for all status codes or a specialized message
    *		{403: 'message 1', 404: 'message 2'}
    */
    handleServerError: function(options) {
        var    errMsg,
               nestedError,
               error = options.error,
               forceLogout = options.forceLogout,
               hideMessage = options.hideMessage,
               message = options.message;
        if(error && typeof error.status == 'number') {
        	console.log('handle error: '+ error.status + ' ' + error.statusText);
        	if(!hideMessage) {
        		Karazy.util.toggleAlertActive(true);
        	}
            switch(error.status) {
                case 403:
                    //no permission
                    if(typeof message == "object" && message[403]) {
                    	errMsg = message[403];
                    } else {
                    	errMsg = (typeof message == "string") ? message : Karazy.i18n.translate('errorPermission');
                    }
                    
                    if(forceLogout && (forceLogout[403] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                    break;
                case 404:
                    //could not load resource or server is not reachable
                    if(typeof message == "object" && message[404]) {
                    	errMsg =  message[404];
                    } else {
                    	errMsg = (typeof message == "string") ? message : Karazy.i18n.translate('errorResource');
                    }
                    if(forceLogout && (forceLogout[404] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                    break;
                case 0:
                	//communication failure, could not contact server
                	if(typeof message == "object" && message[0]) {
                		errMsg = message[0];
                    } else {
                    	errMsg = (typeof message == "string") ? message : Karazy.i18n.translate('errorCommunication');
                    }
                    if(forceLogout && (forceLogout[0] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                	break;
                default:
                    if(typeof message == "object" && message[500]) {
                    	errMsg = message[500];                    
                    } else {
                    	try {
                         //TODO Bug in error message handling in some browsers
                        nestedError = Ext.JSON.decode(error.statusText);
	                    errMsg = Karazy.i18n.translate(nestedError.errorKey,nestedError.substitutions);                        
	                    } catch (e) {
	                        errMsg = (typeof message == "string") ? message : Karazy.i18n.translate('errorMsg');
	                    }
                    }
                    if(forceLogout && (forceLogout[500] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }                                         
                    break;
            }
        }
        if(!hideMessage) {
        	Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), errMsg, function() {
        		Karazy.util.toggleAlertActive(false);
        	});	
        }
    }
});

