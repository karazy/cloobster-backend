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
				tpl: '<p class="desc">{product.data.longDesc}</p>'
			}, {
				xtype : 'panel',
				layout : {
					type : 'hbox',
					align : 'stretch',
					pack: 'center'
				},
				items : [ {
					xtype : 'spinnerfield',
					increment : 1,
					// style : 'background-color:white;',
					value : 1,
					// width: 200,
					minValue : '1',
					maxValue : '10',
					// height: '25px',
					cycle : true,
				},
				{
					xtype: 'label',
					cls: 'productPrice',
					itemId : 'prodPriceLabel',
					tpl: '{[values.product.calculate(values.amount)]}'
				} 
				]
			}]
		}, 
		{
			xtype : 'formpanel',
			itemId : 'choicesPanel',
			cls: 'choicePanel',
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
					ui: 'confirm',
					// icon: '../app/res/images/into_cart.png',
					// iconAlign: 'centered',
					text: Karazy.i18n.translate('change'),
					action: 'edit'
				},
				{
					xtype: 'button',
					ui: 'confirm',
					text: Karazy.i18n.translate('cancel'),
					action: 'undo'
				}
			]
		}
		]		
	}
});