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
	controllers : [  ],
	models : [ ],
	views : [ ], 
	stores : [],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch');
	
		
		
	}
});

