Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',
	layout: {
		type: 'fit',
		width: '200',
		height: '200',
		align: 'center'
	},
	config : {
		items : [ 
//		          {
//			docked : 'top',
//			xtype : 'toolbar',
//			title : i18nPlugin.translate('menuTitle'),
//			items : [ {
//				xtype : 'button',
//				id : 'productOvBackBt',
//				text : i18nPlugin.translate('back'),
//				ui : 'back'
//			} ]
//
//		},
		{
			xtype: 'label',
			id : 'prodDetailLabel1',  
			html: 'product description'
		} ]
	}
});
