Ext.define('EatSense.store.Order', {
	extend: 'Ext.data.Store',
	config: {
		model: 'EatSense.model.Order',
		storeId: 'orderStore',
		sorters: [{
			property: 'orderTime',
			direction: 'DESC'
		}]
		//Because of Ticket #28
		// sorters: [{
		//   sorterFn: function(record1, record2) {
		// 	   var status1 = record1.data.status;
		// 	   var status2 = record2.data.status;
		// 	   //placed orders (new ones) are sorted on top
		// 	   if(status1 == Karazy.constants.Order.PLACED && status1 != status2) {
		// 			return -1;
		// 	   } else if(status1 != status2 && status2 == Karazy.constants.Order.PLACED) {
		// 			return 1;
		// 	   } else if(status1 != status2 && status1 == Karazy.constants.Order.CANCELED) {
		// 			return 1;
		// 	   } else if(status1 != status2 && status2 == Karazy.constants.Order.CANCELED) {
		// 			return -1;
		// 	   } else {
		// 	   		return (status1 > status2) ? 1 : (status1 == status2 ? 0 : -1);
		// 	   }               
  //           }
	 //   }]
	}			
});