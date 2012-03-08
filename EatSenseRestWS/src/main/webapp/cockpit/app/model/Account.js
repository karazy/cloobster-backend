Ext.define('EatSense.model.Account', {
	extend: 'Ext.data.Model',
	config: {
		idProperty: 'login',
		fields: [{
			name: 'login',
			type: 'string'
		}, {
			name: 'email',
			type: 'string'
		},
		{
			name: 'passwordHash',
			type: 'string'
		}, {
			name: 'role',
			type: 'string'
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