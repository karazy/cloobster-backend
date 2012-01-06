/**
 * 
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',
	layout : {
		type : 'fit',
		width : '200',
		height : '200',
		centered : true
	},
	config : {
		styleHtmlContent : true,
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId: 'toolbar',
			title : i18nPlugin.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				id : 'prodDetailBackBt',
				ui : 'back'
			} ]

		},
		{
			xtype : 'label',
			id : 'prodDetailLabel',
		} ]
	}
});
