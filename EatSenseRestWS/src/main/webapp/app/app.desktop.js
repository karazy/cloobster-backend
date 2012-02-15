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
	controllers : [ 'CheckIn', 'Menu', 'Cart' ],
	models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Choice', 'Option', 'Order', 'Error'],
	views : [ 'Main', 'Dashboard', 'Checkinconfirmation', 'CheckinWithOthers', 'MenuOverview', 'ProductOverview', 'ProductDetail', 'OptionDetail', 'Cart', 'Menu'],
	stores : [ 'CheckIn', 'User', 'Menu', 'Product' ],
	requires: ['EatSense.data.proxy.CustomRestProxy'],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch');
		Ext.create('EatSense.view.Main');
	}
});

