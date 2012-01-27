var i18nPlugin = Karazy.i18n;
i18nPlugin.setLang('DE');
var globalConf = Karazy.config;
var profile = Ext.os.deviceType.toLowerCase();

// Special HttpProxy that sends no body on DELETE requests
Ext.data.proxy.GAEHttpProxy = Ext.extend(Ext.data.proxy.Rest, {
	doRequest : function(action, rs, params, reader, cb, scope, arg) {
		if (this.api[action]['method'].toLowerCase() == "delete") {
			delete params.jsonData;
		}

		Ext.data.proxy.GAEHttpProxy.superclass.doRequest.call(this, action, rs,
				params, reader, cb, scope, arg);
	}
});

// i18nPlugin.init(function() {

Ext.Loader.setConfig({
	enabled : true
});
Ext.application({
	name : 'EatSense',
	controllers : [ 'CheckIn', 'Menu' ],
	models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Choice', 'Option',
			'Error' ],
	init : function() {
		// i18nPlugin = Karazy.i18n;
		// i18nPlugin.setTranslations(de_translation);
		console.log('init');
	},
	launch : function() {
		console.log('launch');
	}
});

// });
