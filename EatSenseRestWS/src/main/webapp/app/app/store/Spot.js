Ext.define('EatSense.store.Spot', {
    extend  : 'Ext.data.Store',    
    requires: ['EatSense.model.Spot'],
    storeId: 'spotStore',
    config : {
    	model   : 'EatSense.model.Spot'
    }
});