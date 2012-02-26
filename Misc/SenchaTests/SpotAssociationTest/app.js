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
	//controllers : [ 'CheckIn', 'Menu', 'Order' ],
	models : ['Spot','PaymentMethod'],
	//views : [ 'Main', 'Dashboard', 'Checkinconfirmation', 'CheckinWithOthers', 'MenuOverview', 'ProductOverview', 'ProductDetail', 'OptionDetail', 'Cart', 'Menu', 'CartOverview', 'Lounge'], 
	stores : [ 'Spot'],
	init : function() {
		console.log('init');
	},
	launch : function() {
		//Ext.create('EatSense.view.Main');
		
		Ext.ModelManager.getModel('EatSense.model.Spot').load('hup001', {
    	        		 success: function(record, operation) {
    	        			 console.log(record);
     	        	    },
     	        	    failure: function(record, operation) {
							console.log('error')   	
     	        	    }
    	});
		
	}
});

