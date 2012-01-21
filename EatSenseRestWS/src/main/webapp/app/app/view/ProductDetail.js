/**
 * Displays details and options/extras of a product.
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',	
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	defaults : {
//		styleHtmlContent : false,
	},
	config : {
		scrollable : 'vertical',
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
			style : 'background-color:#B88A00;border-bottom:5px;border-bottom-left-radius: 10px;border-bottom-right-radius: 10px; padding: 10px',
			layout : {
				type : 'vbox',
//				align : 'middle'
			},
			
			items : [ {
				xtype : 'label',
				id : 'prodDetailLabel',
			}, {
				xtype : 'panel',
				layout : {
					type : 'hbox',
					align : 'stretch'
				},
				items : [ {
					xtype : 'spinnerfield',
					increment : 1,
					itemdId : 'productSpinner',
					style : 'background-color:white;',
					value : 1,
					flex : 3,
					// FIXME not working in PR 3
					// minValue : '1',
					// maxValue : '10',
					cycle : true,

				},
				{
					xtype: 'spacer',
					flex: 1
				},
				{
					xtype : 'button',
					id : 'prodDetailCardBt',
					iconCls : 'compose',
					iconMask : true,
					flex : 2
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