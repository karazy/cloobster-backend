Karazy.i18n.setLang('DE');

Ext.Loader.setConfig({
	enabled : true,
	//WORKAORUND related to Android 3x Bug and Webview URL handling
	disableCaching: false 
}); 
//WORKAORUND related to Android 3x Bug and Webview URL handling
//Ext.Ajax.setDisableCaching(false); 

Ext.application({
	name : 'EatSense',
	controllers : [ 'CheckIn', 'Menu', 'Order', 'Settings', 'Request' ],
	models : [ 'CheckIn', 'User', 'Menu', 'Product', 'Choice', 'Option', 'Order', 'Error', 'Spot', 'Bill', 'PaymentMethod', 'Request'],
	views : [ 'Main', 'Dashboard', 'Checkinconfirmation', 'CheckinWithOthers', 'MenuOverview', 'ProductOverview', 'ProductDetail', 'OrderDetail', 'OptionDetail', 'Cart', 'Menu', 'CartOverview', 'Lounge'], 
	stores : [ 'CheckIn', 'User', 'Spot', 'AppState', 'Menu', 'Product', 'Order', 'Bill'],
	phoneStartupScreen: 'res/images/startup.png',
	tabletStartupScreen: 'res/images/startup.png',
	requires: [
		//require most common types
		'Ext.Container',
		'Ext.Panel',
		'Ext.dataview.List',
		'Ext.Label',
		'Ext.TitleBar',
		//require custom types
		'EatSense.override.CustomRestProxy',
		'EatSense.override.OperationImprovement', 
		'EatSense.view.fragment.DashboardButton', 
		'EatSense.override.RadioOverride', 
		'EatSense.model.AppState'
	],
	launch : function() {
    	this.launched = true;
        this.mainLaunch();
	},
    mainLaunch: function() {
        if (cordovaInit == false || !this.launched) {
        	return;
        }

        console.log('mainLaunch');
		
    	//try to restore application state
	   	 var appStateStore = Ext.data.StoreManager.lookup('appStateStore'),
	   	 checkInCtr = this.getController('CheckIn'),
	   	 main = Ext.create('EatSense.view.Main'),
	   	 restoredCheckInId;	 
	   	 
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

