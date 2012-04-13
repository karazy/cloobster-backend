Ext.define('EatSense.model.CheckIn', {
	extend : 'Ext.data.Model',
	// requires: ['EatSense.model.PaymentMethod'],
	config : {
		idProperty : 'id',
		fields : [ 
		{
			name : 'id',
		}, 
		{
			name : 'nickname',
			type : 'string'
		}, 
		{
			name : 'status',
			type : 'string'
		}, 
		{
			name: 'spotId'
		},
		{
			name : 'checkInTime',
			type : 'date',
			dateFormat : 'time'
		},
		{	//foreign key for checkIn. Don't send to server.
			name: 'request_id',
			persist: false
		}],
		proxy : {
			type : 'rest',
			url : '/b/businesses/{pathId}/checkins/'
		},
		syncRemovedRecords: false
	}
});