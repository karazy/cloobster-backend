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
	stores : [ 'CheckIn', 'User', 'Spot', 'AppState', 'Menu', 'Product', 'Order', 'Bill'],
	phoneStartupScreen: 'res/images/startup.png',
	tabletStartupScreen: 'res/images/startup.png',
	requires: ['EatSense.data.proxy.CustomRestProxy','EatSense.data.proxy.OperationImprovement', 'EatSense.view.fragment.DashboardButton', 'EatSense.override.RadioOverride', 'EatSense.model.AppState'],
	init : function() {
		console.log('init');
	},
	launch : function() {
		console.log('launch');
		
    	//try to restore application state
	   	 var 	appStateStore = Ext.data.StoreManager.lookup('appStateStore'),
	   	 		checkInCtr = this.getController('CheckIn'),
	   	 		restoredCheckInId;	 
	   	 
	   	 //create main screen
	   	 Ext.create('EatSense.view.Main');
	   	 
	   	 try {
	   		appStateStore.load();
	   	 } catch (e) {
	   		appStateStore.removeAll();
	   	 }
	     
	     
	   	 if(appStateStore.getCount() == 1) {
	   		 console.log('app state found');	   		 
		   		checkInCtr.setAppState(appStateStore.getAt(0));
	   		restoredCheckInId = checkInCtr.getAppState().get('checkInId');
	   	 }
	   		
	   	 if(restoredCheckInId) {
	   			 //reload old state
	   			 EatSense.model.CheckIn.load(restoredCheckInId, {
	   				scope: this,
	   				success : function(record, operation) {
	   					console.log('found existing checkin '+record);					
	   					if(record.get('status') == Karazy.constants.CHECKEDIN || record.get('status') == Karazy.constants.ORDER_PLACED) {	   						
		   					checkInCtr.restoreState(record);
	   					} else {
	   						appStateStore.add(checkInCtr.getAppState());
		   		   		 	checkInCtr.showDashboard();
	   					}	   						   				
	   				},
	   				failure: function(record, operation) {
	   					console.log('error restoring state');
	   					appStateStore.removeAll();
	   					appStateStore.sync();
	   					appStateStore.add(checkInCtr.getAppState());
	   		   		 	checkInCtr.showDashboard();
	   				}
	   			 });	   			 
	   	 }	   		 	   	 	   	 
	   	 else {	   		 
	   		if (appStateStore.getCount() > 1){
		   		 console.log('Too many appStates! clear cache. this should never happen.');
		   	 } else {
		   		console.log('no app state found.');
		   		appStateStore.removeAll();
		   	 } 		   		 
	   		 appStateStore.add(checkInCtr.getAppState());
	   		 checkInCtr.showDashboard();
	   	 }
		
		
	}
});

