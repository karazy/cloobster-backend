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
		}, {
			name: 'token'
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