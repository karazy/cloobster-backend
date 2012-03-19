Ext.define('EatSense.store.PaymentMethod', {
    extend  : 'Ext.data.Store',    
    requires: ['EatSense.model.PaymentMethod'],
    
    config : {
    	model   : 'EatSense.model.PaymentMethod',
    	storeId: 'paymentMethodStore',
  		data: [{
			   id: '1',
			   name: 'EC'
		   }, {
			   id:'2',
			   name: 'Bar'
		   }]
    }
});