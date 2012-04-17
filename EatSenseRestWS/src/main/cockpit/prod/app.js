Karazy.i18n.setLang('DE');
var profile = Ext.os.deviceType.toLowerCase();

Ext.Loader.setConfig({
	enabled : true
});

Ext.Loader.setPath('EatSense', 'app');

Ext.application({
	name : 'EatSense',
	controllers : ['Login','Spot', 'Message', 'Request'],
	models : ['Account','Spot', 'Business', 'CheckIn', 'Order', 'Product', 'Choice', 'Option', 'Bill', 'PaymentMethod', 'Request'],
	views : ['Login', 'ChooseBusiness', 'Main'], 
	stores : ['Account', 'AppState',  'Spot', 'Business', 'CheckIn', 'Order', 'Bill', 'Request' ],
	requires: [
		//require most common types
		'Ext.Container',
		'Ext.Panel',
		'Ext.dataview.List',
		'Ext.Label',
		'Ext.TitleBar',
		'EatSense.data.proxy.CustomRestProxy',
		'EatSense.data.proxy.OperationImprovement'],
		//used on iOS devices for homescreen
	icon: {
		57: 'res/images/icon.png',
   		72: 'res/images/icon-72.png',
   		114: 'res/images/icon-114.png'
	},
	glossOnIcon: false,
	init : function() {
		
	},
	launch : function() {
		console.log('launch cockpit ...');

	   	var loginCtr = this.getController('Login'),
	   		spotCtr = this.getController('Spot');

	   	//try to restore credentials
	   	loginCtr.restoreCredentials();
	}
});

