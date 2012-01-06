Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',
	layout: {
		type: 'fit',
		width: '200',
		height: '200',
		centered: true
	},
	config : {
		items : [ 
		{
			xtype: 'label',
			id : 'prodDetailLabel1',  
			html: 'test text'
		} ]
	}
});
