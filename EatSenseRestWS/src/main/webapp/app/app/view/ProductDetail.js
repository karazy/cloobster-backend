/**
 * 
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',
	scrollable : 'vertical',
	layout : {
		type : 'vbox',
	// width : '200',
	// height : '200',
	// align : 'stretch',
	// pack : 'center',
	},
	defaults : {
		styleHtmlContent : false,
	},
	config : {
		// styleHtmlContent : true,
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId : 'toolbar',
			title : i18nPlugin.translate('menuTitle'),
			items : [ {
				xtype : 'button',
				id : 'prodDetailBackBt',
				ui : 'back'
			} ]

		}, {
			xtype : 'panel',
			docked : 'top',
			items : [ {
				xtype : 'label',
				id : 'prodDetailLabel',
				styleHtmlContent : true,
			}, {
				xtype : 'panel',
				layout : 'hbox',
				items : [
				{
					xtype : 'spinnerfield',
					increment : 1,
					itemdId : 'productSpinner',
					//FIXME not working in PR 3
					// minValue : '1',
					// maxValue : '10',
					cycle : true,

				}, {
					xtype : 'button',
					id : 'prodDetailCardBt',
					// iconCls: 'home',
					// iconMask: true,
					text : 'Card',
				} ]
			} ]
		}, {
			xtype : 'panel',
			itemId : 'choicesPanel'
		} ]
	}
});