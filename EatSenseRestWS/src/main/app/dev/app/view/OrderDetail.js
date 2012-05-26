/**
 * Displays details and options of an order.
 * Very similar to productdetail.
 */
Ext.define('EatSense.view.OrderDetail', {
	extend : 'Ext.Panel',
	xtype : 'orderdetail',	
	layout : {
		type : 'vbox',
		align : 'stretch',
	},	
	config : {
    	scrollable : 'vertical',
    	modal: true,
		// hideOnMaskTap: true,
		top: '5%',
		left: '3%',
		right: '3%',
		bottom: '3%',
		floatingCls: 'orderdetail-floating',
		items : [
		{
			xtype: 'titlebar',
			docked: 'top'
		},
		{
			xtype : 'panel',
			cls: 'productDetailPanel',
			layout : {
				type : 'vbox',
			},
			items : [ 
			{
				xtype : 'label',
				itemId : 'prodDetailLabel',
				cls: 'productDetail',
				tpl: '{product.data.longDesc}'
			}, {
				xtype : 'panel',
				docked: 'right',
				width: 110,
				layout : {
					type : 'vbox',
					align : 'stretch',
					pack: 'center'
				},
				items : [ {
					xtype : 'spinnerfield',
					label: Karazy.i18n.translate('amountspinnerLabel'),
					labelCls: 'productdetail-spinner-label',
					inputCls: 'productdetail-spinner-input',
					labelAlign: 'top',
					increment : 1,
					value : 1,
					minValue : '1',
					maxValue : '10',
					cycle : true,
				},
				{
					xtype: 'label',
					cls: 'productPrice',
					margin: '5 0 0 0',
					itemId : 'prodPriceLabel',
					tpl: new Ext.XTemplate(
					'{[this.formatPrice(values.product.calculate(values.amount))]}',
					{
						formatPrice: function(price) {
							return Karazy.util.formatPrice(price);
						}
					}
					)
				} 
				]
			}]
		}, 
		{
			xtype : 'formpanel',
			itemId : 'choicesPanel',
			cls: 'choice-panel',
			layout: 'vbox',
			// minHeight: '200px',
			scrollable : false,
			items: [
				{
					xtype: 'label',
					docked: 'top',
					cls: 'choice-panel-title',
					html: Karazy.i18n.translate('choicesPanelTitle')
				}
			]
		},
		{
			xtype: 'toolbar',
			docked: 'bottom',
			layout: {
   				type: 'hbox',
   				align: 'middle',
   				pack: 'center'
			},
			items: [
				{
					xtype: 'button',
					// ui: 'confirm',
					text: Karazy.i18n.translate('change'),
					action: 'edit'
				},
				{
					xtype: 'button',
					// ui: 'confirm',
					text: Karazy.i18n.translate('cancel'),
					action: 'undo'
				}
			]
		}
		]		
	}
});