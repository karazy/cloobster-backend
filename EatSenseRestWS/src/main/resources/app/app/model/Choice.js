Ext.define('EatSense.model.Choice', {
	extend: 'Ext.data.Model',
	idProperty: 'id',
	fields: [
	    {name: 'id', type: 'string'},
		{name: 'text', type: 'string'},
		{name: 'minOccurence', type: 'number'},
		{name: 'maxOccurence', type: 'number'},
		{name: 'price', type: 'number'},
		{name: 'included', type: 'number'},
		{name: 'overridePrice', type: 'string'},
	],
	hasMany : {
		model : 'EatSense.model.Option',
		name : 'options'
	}
});