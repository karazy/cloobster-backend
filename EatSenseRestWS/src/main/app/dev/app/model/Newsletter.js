/**
* Represents data to register a user for a newsletter.
* 
*/
Ext.define('EatSense.model.Newsletter', {
	extend: 'Ext.data.Model',
	config: {
		idProperty: 'id',
		fields: [
			{
				name: 'id',
				persist: false
			},
			{
			//email to register
			name: 'email'
			}
		],
		validations: [
		{
			type: 'email', field: 'email'
		}],
		proxy: {
			type: 'rest',
			url: '/newsletter'
		}
	}
});