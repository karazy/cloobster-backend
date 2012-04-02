Ext.define('EatSense.store.Order', {
	extend: 'Ext.data.Store',
	requires: ['EatSense.model.Order'],
	config: {
		storeId: 'orderStore',
		model: 'EatSense.model.Order',
		filters: [
			{ 
				filterFn: function(record, id) {
					return (record.get('status') == Karazy.constants.Order.PLACED || record.get('status') == Karazy.constants.Order.RECEIVED);
	 			}
	 	}
	 	]
	}
})