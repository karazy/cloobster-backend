Ext.define('EatSense.model.Error', {
	extend: 'Ext.data.Model',
	idProperty: 'errorKey',
	fields: [
		{name: 'errorKey', type: 'string'},
		{name: 'substitutions'}
	]
});