Ext.define('EatSense.model.User', {
	extend: 'Ext.data.Model',
	config : {
		idProperty: 'userId',
		fields: [
		 		{name: 'userId', type: 'string'},
		 		{name: 'nickname', type: 'string'}
		 	],
		 	proxy: {
		 		type: 'rest',
		 		url: '/user/',
		 		enablePagingParams: false,
		 		reader: {
		 			type: 'json',
		 		}
		 	}
	}
});