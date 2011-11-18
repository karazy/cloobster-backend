Ext.define('EatSense.model.CheckIn', {
	extend: 'Ext.data.Model',
	fields: [
		{name: 'status', type: 'string'},
		{name: 'restaurantName', type: 'string'},
		{name: 'locationName', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: 'http://2.karazy-eatsense.appspot.com/restaurant/spot/', 
		reader: {
			type: 'json',
		}
	}
});