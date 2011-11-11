Ext.create('Ext.data.Store', {
	model: 'CheckIn',
	proxy: {
		type: 'ajax',
		url: 'http://localhost:8888/restaurant/spot/serg2011',
		reader: 'json'
	}
});