Ext.define('EatSense.model.User', {
	extend: 'Ext.data.Model',
	idProperty: 'userId',
	fields: [
	 		{name: 'userId', type: 'string'},
	 		{name: 'nickname', type: 'string'}
	 	],
	 	proxy: {
	 		type: 'rest',
	 		url: globalConf.serviceUrl+'/user/', 
	 		reader: {
	 			type: 'json',
	 		}
	 	}
});