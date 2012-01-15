Ext.define('EatSense.model.User', {
	extend: 'Ext.data.Model',
	idProperty: 'userId',
	fields: [
	 		{name: 'userId', type: 'string'},
	 		{name: 'nickname', type: 'string'}
	 	],
	 	proxy: {
	 		type: 'rest',
	 		url: 'http://192.168.1.111:8888/user', 
	 		reader: {
	 			type: 'json',
	 		}
	 	}
});