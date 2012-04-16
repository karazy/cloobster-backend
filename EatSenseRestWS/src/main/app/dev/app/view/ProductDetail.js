/**
 * Displays details and options of a product.
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',	
	layout : {
		type : 'vbox',
		align : 'stretch',
	},	
	config : {
    	scrollable : 'vertical',
    	modal: true,
		hideOnMaskTap: true,
		top: '5%',
		left: '3%',
		right: '3%',
		bottom: '3%',
		floatingCls: 'productdetail-floating',
		items : [
		{
			xtype: 'titlebar',
			docked: 'top',
			// items: [
			// {
			// 		xtype: 'button',
			// 		action: 'close',
			// 		baseCls: 'productDetail-close',
			// 		text: 'X',
			// 		align: 'right'
			// }]
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
				tpl: new Ext.XTemplate(
				 	// '<div class="price">{[this.formatPrice(values.product.calculate(values.amount))]}</div>'+
				 	// '<div style="clear: both;">'+
				 	'<p class="desc">{product.data.longDesc}</p>',
				 	{
						formatPrice: function(price) {
							return Karazy.util.formatPrice(price);
						}
					}
				)
			}, {
				xtype : 'panel',
				layout : {
					type : 'hbox',
					align : 'stretch',
					pack: 'center'
				},
				items : [ {
					xtype : 'spinnerfield',
					itemId : 'productAmountSpinner',
					increment : 1,
					value : 1,
					minValue : '1',
					maxValue : '10',
					cycle : true,
				},
				{
					xtype: 'label',
					cls: 'productPrice',
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
			minHeight: '200px',
			scrollable : false,
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
					text: Karazy.i18n.translate('putIntoCartButton'),
					// ui: 'confirm',
					// icon: '../app/res/images/into_cart.png',
					// iconCls: 'cart-button',
					action: 'cart'
				}, 
				{
					xtype: 'button',
					ui: 'confirm',
					action: 'close',
					text: Karazy.i18n.translate('close')
				}
			]
		}
		]		
	}
});