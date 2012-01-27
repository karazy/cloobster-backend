Ext.define('EatSense.model.Option', {
	extend: 'Ext.data.Model',
	idProperty: 'id',
	fields: [
	    {name: 'id', type: 'string'},
		{name: 'name', type: 'string'},
		{name: 'price', type: 'number'},
	]
});