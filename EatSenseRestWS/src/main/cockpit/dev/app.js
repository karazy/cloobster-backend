Karazy.i18n.setLang('DE');

Ext.Loader.setConfig({
	enabled : true,
    //WORKAORUND related to Android 3x Bug and Webview URL handling
    disableCaching: Karazy.config.disableCaching
});

Ext.Loader.setPath('EatSense', 'app');

Ext.application({
	name : 'EatSense',
	controllers : ['Login','Spot', 'Message', 'Request'],
	models : ['Account','Spot', 'Business', 'CheckIn', 'Order', 'Product', 'Choice', 'Option', 'Bill', 'PaymentMethod', 'Request'],
	views : ['Login', 'ChooseBusiness', 'Main'], 
	stores : ['Account', 'AppState',  'Spot', 'Business', 'CheckIn', 'Order', 'Bill', 'Request' ],
	requires: [
		//require most common types
		'Ext.Container',
		'Ext.Panel',
		'Ext.dataview.List',
		'Ext.Label',
		'Ext.TitleBar',
		//require custom types
		'EatSense.data.proxy.CustomRestProxy',
		'EatSense.data.proxy.OperationImprovement'],
	icon: {
		//used on iOS devices for homescreen
		57: 'res/images/icon.png',
   		72: 'res/images/icon-72.png',
   		114: 'res/images/icon-114.png'
	},
	glossOnIcon: false,

	init : function() {
		
	},
	launch : function() {
		console.log('launch cockpit ...');

        //global error handler
        window.onerror = function(message, url, lineNumber) {  
         console.error('unhandled error > ' + message +' in '+ url +' at '+ lineNumber);
             //to prevent firing of default handler (return true)
             return false;
        }; 

	   	var loginCtr = this.getController('Login');

	   	//try to restore credentials
	   	//if it fails will display the login mask
	   	loginCtr.restoreCredentials();
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
    *       can be either a common message for all status codes or a specialized message
    *       {403: 'message 1', 404: 'message 2'}
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

