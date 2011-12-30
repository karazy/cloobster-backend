Ext.define('EatSense.model.CheckIn', {
	extend: 'Ext.data.Model',
	idProperty: 'userId',
	fields: [
		{name: 'status', type: 'string'},
		{name: 'restaurantName', type: 'string'},
		{name: 'restaurantId', type: 'string'},
		{name: 'spot', type: 'string'},
		{name: 'userId', type: 'string'},
		{name: 'nickname', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: '/restaurant/spot/', 
		reader: {
			type: 'json',
		}
	}
});