Ext.define('EatSense.store.Menu', {
    extend  : 'Ext.data.Store',
    model   : 'EatSense.model.Menu',
    requires: ['EatSense.model.Menu'],
//    remoteFilter: true,
//    filters: [
//     {
//    	 property: 'id',
//    	 value: '1'
//     }          
//    ]
});