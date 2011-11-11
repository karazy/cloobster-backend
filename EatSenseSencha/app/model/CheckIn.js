Ext.define('CheckIn', {
	extend: 'Ext.data.Model',
	fields: [
		{name: 'status', type: 'string'},
		{name: 'restaurantName', type: 'string'},
		{name: 'locationName', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: 'http://192.168.1.111:8888/restaurant/spot/serg2011', 
		reader: {
			type: 'json',
			root: 'spot'
		}
	}
});