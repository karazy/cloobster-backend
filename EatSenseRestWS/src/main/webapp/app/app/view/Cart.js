Ext.define('EatSense.view.Cart', {
	extend : 'Ext.Panel',
	xtype : 'cartview',
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : i18nPlugin.translate('cartviewTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'cartBackBt',
				text : i18nPlugin.translate('menuTitle'),
				ui : 'back'
			} ]

		} ]
	}

});