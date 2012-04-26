/**
*	A customer request like "Call Waiter"
*/
Ext.define('EatSense.model.Request', {
	extend: 'Ext.data.Model',
	config: {
		fields: [
		{
			name: 'id',
			type: 'number'
		},
		{
			name: 'type'
		},
		{
			name: 'checkInId'
		},
		{
			name: 'spotId'
		}],
		proxy: {
			type: 'rest',
			url: '/c/checkins/{checkInId}/requests',
			enablePagingParams: false
		}
	}
});