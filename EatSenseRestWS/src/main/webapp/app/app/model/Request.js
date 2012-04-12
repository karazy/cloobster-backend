/**
*	A customer request like "Call Waiter"
*/
Ext.define('EatSense.model.Request', {
	extend: 'Ext.data.Model',
	config: {
		fields: [
		{
			name: 'type'
		},
		{
			name: 'checkInId'
		}],
		proxy: {
			type: 'rest',
			url: '/c/checkins/{pathId}/requests'
		}
	}
});