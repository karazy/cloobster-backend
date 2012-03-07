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
	controllers : [ ],
	models : [],
	views : [ 'Main'], 
	stores : [ ],
	phoneStartupScreen: 'res/images/startup.png',
	tabletStartupScreen: 'res/images/startup.png',
	requires: ['EatSense.data.proxy.CustomRestProxy',],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch');
		Ext.create('EatSense.view.Main');
		
		
	}
});

