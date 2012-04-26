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

	   	var loginCtr = this.getController('Login');

	   	//try to restore credentials
	   	//if it fails will display the login mask
	   	loginCtr.restoreCredentials();
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
    *       message: message to show. If no message is set a default message will be displayed.
    *       can be either a common message for all status codes or a specialized message
    *       {403: 'message 1', 404: 'message 2'}
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
                    if(message) {
                        if(message[403]) {
                            errMsg = message[403];
                        } else {
                            errMsg = message;
                        }
                    } else {
                        errMsg = Karazy.i18n.translate('errorPermission');
                    }
                    
                    if(forceLogout && (forceLogout[403] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                    break;
                case 404:
                    //could not load resource or server is not reachable
                    if(message) {
                        if(message[404]) {
                            errMsg = message[404];
                        } else {
                            errMsg = message;
                        }
                    } else {
                        errMsg = Karazy.i18n.translate('errorResource');
                    }
                    if(forceLogout && (forceLogout[404] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }
                    break;
                default:
                    if(message) {
                        if(message[500]) {
                            errMsg = message[500];
                        } else {
                            errMsg = message;
                        }                       
                    } else {
                        try {
                         //TODO Bug in error message handling in some browsers
                        nestedError = Ext.JSON.decode(error.statusText);
                        errMsg = Karazy.i18n.translate(nestedError.errorKey,nestedError.substitutions);                        
                        } catch (e) {
                            errMsg = Karazy.i18n.translate('errorMsg');
                        }
                    }
                    if(forceLogout && (forceLogout[500] === true || forceLogout === true)) {
                        this.fireEvent('statusChanged', Karazy.constants.FORCE_LOGOUT);
                    }                                         
                    break;
            }
        }
        if(!hideMessage) {
            Ext.Msg.alert(Karazy.i18n.translate('errorTitle'), errMsg, Ext.emptyFn);    
        }
    }
});

