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
	controllers : [ 'CheckIn', 'Menu', 'Order' ],
	models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Choice', 'Option', 'Order', 'Error', 'Spot', 'Bill', 'PaymentMethod'],
	views : [ 'Main', 'Dashboard', 'Checkinconfirmation', 'CheckinWithOthers', 'MenuOverview', 'ProductOverview', 'ProductDetail', 'OptionDetail', 'Cart', 'Menu', 'CartOverview', 'Lounge'], 
	stores : [ 'CheckIn', 'User', 'Spot', 'AppState'],
	phoneStartupScreen: 'res/images/startup.png',
	tabletStartupScreen: 'res/images/startup.png',
	requires: ['EatSense.data.proxy.CustomRestProxy','EatSense.data.proxy.OperationImprovement'],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch');
		Ext.create('EatSense.view.Main');
	}
});

