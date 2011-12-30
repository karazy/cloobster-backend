Ext.define('EatSense.model.Menu', {
	extend: 'Ext.data.Model',
	idProperty: 'id',
	fields: [
	    {name: 'id', type: 'string'},
		{name: 'title', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: '/restaurant/menu/', 
		reader: {
			type: 'json',
		}
	},
	hasMany: 'Product'
});