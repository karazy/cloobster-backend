var i18nPlugin = Karazy.i18n;
i18nPlugin.setLang('DE');
var globalConf = Karazy.config;
var profile = Ext.os.deviceType.toLowerCase();

Ext.Loader.setConfig({
	enabled : true
});

Ext.Loader.setPath('EatSense', 'app');

Ext.application({
	name : 'EatSense',
	controllers : ['Login','Spot', 'Message'],
	models : ['Account','Spot', 'Business', 'CheckIn', 'Order', 'Product', 'Choice', 'Option', 'Bill', 'PaymentMethod'],
	views : ['Login', 'ChooseBusiness', 'Main'], 
	stores : ['Account', 'AppState',  'Spot', 'Business', 'CheckIn', 'Order', 'Bill' ],
	requires: ['EatSense.data.proxy.CustomRestProxy','EatSense.data.proxy.OperationImprovement'],
	//used on iOS devices for homescreen
	icon: 'res/images/icon.png',
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch cockpit ...');

	   	var loginCtr = this.getController('Login'),
	   		spotCtr = this.getController('Spot');

	   	//try to restore credentials
	   	loginCtr.restoreCredentials();
	}
});

