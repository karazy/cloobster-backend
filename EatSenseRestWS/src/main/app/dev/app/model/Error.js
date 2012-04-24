/**
 * General purpose error object.
 * - errorKey is used to get the correct translation 
 * - substitutions contains values for placeholders in translation string
 */
Ext.define('EatSense.model.Error', {
	extend: 'Ext.data.Model',
	config : {
		idProperty: 'errorKey',
		fields: [
			{name: 'errorKey', type: 'string'},
			{name: 'substitutions'}
		]
	}

});