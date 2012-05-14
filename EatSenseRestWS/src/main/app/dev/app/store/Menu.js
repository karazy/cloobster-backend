Ext.define('EatSense.store.Menu', {
	extend: 'Ext.data.Store',
	requires: ['EatSense.model.Menu'],
	config: {		
		model: 'EatSense.model.Menu',		
		storeId: 'menuStore',
		sorters: [
			{
				property: 'order',
				direction: 'ASC'
			}
	    ]
	}
});