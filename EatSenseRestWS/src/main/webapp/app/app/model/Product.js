Ext.define('EatSense.model.Product', {
	extend: 'Ext.data.Model',
	idProperty: 'id',
	fields: [
	    {name: 'id', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'shortDesc', type: 'string'},
		{name: 'price', type: 'string'}
	],
	proxy: {
		type: 'rest',
		url: '/restaurant/menu/product/', 
		reader: {
			type: 'json',
		}
	},
	belongsTo: 'Menu'
});