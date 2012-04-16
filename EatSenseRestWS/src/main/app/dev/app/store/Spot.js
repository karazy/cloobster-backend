Ext.define('EatSense.store.Spot', {
    extend  : 'Ext.data.Store',    
    requires: ['EatSense.model.Spot'],    
    config : {
    	storeId: 'spotStore',
    	model   : 'EatSense.model.Spot'
    }
});