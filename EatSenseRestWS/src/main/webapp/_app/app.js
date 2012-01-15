var i18nPlugin = Karazy.i18n;
i18nPlugin.setLang('de');
var profile = Ext.os.deviceType.toLowerCase();
//i18nPlugin.init(function() {

	Ext.Loader.setConfig({
		enabled : true
	});
	Ext.application({
		name : 'EatSense',
		controllers : [ 'CheckIn', 'Menu' ],
		models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Error' ],
		init : function() {
			// i18nPlugin = Karazy.i18n;
			// i18nPlugin.setTranslations(de_translation);
			console.log('init');
		},
		launch : function() {
			console.log('launch');
		}
	});

//});
