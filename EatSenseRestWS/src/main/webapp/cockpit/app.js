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
	controllers : ['Login','Spot'],
	models : ['Account','Spot'],
	views : ['Login', 'Main'], 
	stores : ['Account', 'AppState',  'Spot' ],
	phoneStartupScreen: 'res/images/startup.png',
	tabletStartupScreen: 'res/images/startup.png',
	requires: ['EatSense.data.proxy.CustomRestProxy','EatSense.data.proxy.OperationImprovement'],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch');

		//try to restore application state
	   	var loginCtr = this.getController('Login');


		if(loginCtr.restoreCredentials() === true) {
			EatSense.model.Account.load('admin', {});

			Ext.create('EatSense.view.Main');
		} else {
			Ext.create('EatSense.view.Login');
		}		
		
	}
});

