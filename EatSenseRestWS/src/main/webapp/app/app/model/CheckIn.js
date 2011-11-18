Ext.define('EatSense.model.CheckIn', {
	extend: 'Ext.data.Model',
	fields: [
		{name: 'status', type: 'string'},
		{name: 'restaurantName', type: 'string'},
		{name: 'locationName', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: '/restaurant/spot/', 
		reader: {
			type: 'json',
		}
	}
});