Ext.define('EatSense.model.Account', {
	extend: 'Ext.data.Model',
	config: {
		// idProperty: 'login',
		fields: [{
			name: 'login'
		}, {
			name: 'email'
		},
		{
			name: 'passwordHash'
		}, {
			name: 'role'
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