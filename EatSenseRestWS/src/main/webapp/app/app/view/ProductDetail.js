/**
 * Displays details and options/extras of a product.
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',
	scrollable : 'vertical',
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	defaults : {
//		styleHtmlContent : false,
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
			layout : {
				type : 'hbox',
				align : 'middle'
			},
			
			items : [ {
				xtype : 'label',
				flex : 2,
				id : 'prodDetailLabel',
			}, {
				xtype : 'panel',
				layout : {
					type : 'vbox',
					align : 'stretch'
				},
				flex : 1,
				items : [ {
					xtype : 'spinnerfield',
					increment : 1,
					itemdId : 'productSpinner',
					value : 1,
					// FIXME not working in PR 3
					// minValue : '1',
					// maxValue : '10',
					cycle : true,

				}, {
					xtype : 'button',
					id : 'prodDetailCardBt',
					iconCls : 'home',
					iconMask : true,
				// text : 'Card',
				} ]
			} ]
		}, {
			xtype : 'panel',
			itemId : 'choicesWrapper',
			items : [
				{
					xtype : 'label',
					html :  i18nPlugin.translate('choicesPanelTitle')
				},
				{
					xtype : 'panel',
					itemId : 'choicesPanel'
				}
				
			]
		} ]
	}
});