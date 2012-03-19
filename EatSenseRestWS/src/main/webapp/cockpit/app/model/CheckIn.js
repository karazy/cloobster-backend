Ext.define('EatSense.model.CheckIn', {
	extend : 'Ext.data.Model',
	// requires: ['EatSense.model.PaymentMethod'],
	config : {
		idProperty : 'id',
		fields : [ 
		{
			name: 'id'
		},
		{
			name : 'id',
		}, {
			name : 'nickname',
			type : 'string'
		}, {
			name : 'status',
			type : 'string'
		}, {
			name : 'checkintime',
			type : 'date',
			dateFormat : 'time'
		}],
		proxy : {
			type : 'rest',
			url : '/b/businesses/{pathId}/checkins/'
		}
	}
});