/**
* Represents data to register a user for a newsletter.
* 
*/
Ext.define('EatSense.model.Newsletter', {
	extend: 'Ext.data.Model',
	config: {
		fields: [
			{
			//email to register
			name: 'email'
		}
		],
		validations: [
		{
			type: 'email', field: 'email'
		}]
	}
});