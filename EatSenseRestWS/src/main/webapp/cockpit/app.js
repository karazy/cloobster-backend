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
	models : ['Account','Spot', 'Business'],
	views : ['Login', 'ChooseBusiness', 'Main'], 
	stores : ['Account', 'AppState',  'Spot', 'Business' ],
	requires: ['EatSense.data.proxy.CustomRestProxy','EatSense.data.proxy.OperationImprovement'],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch cockpit ...');

	   	var loginCtr = this.getController('Login'),
	   		spotCtr = this.getController('Spot');

	   	//try to restore credentials
	   	loginCtr.restoreCredentials();

		// if( === true) {
		// 	Ext.create('EatSense.view.Main');
		// 	spotCtr.loadSpots();
		// } else {			
		// 	Ext.create('EatSense.view.Login');
		// }		
		
	}
});

