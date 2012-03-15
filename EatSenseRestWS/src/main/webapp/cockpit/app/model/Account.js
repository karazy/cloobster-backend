Ext.define('EatSense.model.Account', {
	extend: 'Ext.data.Model',
	config: {
		fields: [
		{
			name: 'login'
		}, 
		{
			name: 'email'
		},
		{
			name: 'passwordHash'
		}, {
			name: 'role'
		}, 
		{
			name: 'businessId'
		},	
		//TODO maybe store an association?
		{
			name: 'business'
		}	
		],
		proxy : {
			type : 'rest',
			url : '/accounts/',
			reader : {
				type : 'json',
			}
		}
	}
});