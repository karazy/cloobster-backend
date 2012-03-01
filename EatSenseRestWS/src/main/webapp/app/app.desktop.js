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
		
    	//try to restore application state
	   	 var appStateStore = Ext.data.StoreManager.lookup('appStateStore'),
	   	 checkInCtr = this.getController('CheckIn'),
	   	 main = Ext.create('EatSense.view.Main');	   	 
	     appStateStore.load();
	     
	   	 if(appStateStore.getCount() == 1) {
	   		 console.log('app state found');	   		 
	   		checkInCtr.setAppState(appStateStore.getAt(0));
	   		 var restoredCheckInId = checkInCtr.getAppState().get('checkInId');
	   		 if(restoredCheckInId != null && restoredCheckInId != '') {
	   			 //reload old state
	   			 EatSense.model.CheckIn.load(restoredCheckInId, {
	   				scope: this,
	   				success : function(record, operation) {
	   					console.log('found existing checkin '+record);					
	   					if(record.get('status') == Karazy.constants.CHECKEDIN) {	   						
		   					checkInCtr.restoreState(record);
	   					} else {
	   						appStateStore.add(checkInCtr.getAppState());
		   		   		 	checkInCtr.showDashboard();
	   					}	   						   				
	   				},
	   				failure: function(record, operation) {
	   					Ext.create('EatSense.view.Main');
	   					console.log('error restoring state');
	   					appStateStore.removeAll();
	   					appStateStore.add(checkInCtr.getAppState());
	   		   		 	checkInCtr.showDashboard();
	   				},
	   				callback: function() {
	   					
	   				}
	   			 });	   			 
	   		 }	   		 
	   	 }	   	 
	   	 else {	   		 
	   		if (appStateStore.getCount() > 1){
		   		 console.log('Too many appStates!');
		   	 } else {
		   		console.log('no app state found. clear cache. this should never happen.');
		   		appStateStore.removeAll();
		   	 } 	
	   		 
	   		 appStateStore.add(checkInCtr.getAppState());
	   		 checkInCtr.showDashboard();
	   	 }
		
		
	}
});

