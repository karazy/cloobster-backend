/**
 * Displays details and options of a product.
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
		hideOnMaskTap: true,
		top: '8%',
		left: '5%',
		right: '5%',
		bottom: '5%',
		items : [
		{
			xtype : 'panel',
			cls: 'productDetailPanel',
			layout : {
				type : 'vbox',
			},
			items : [ 
			{
					xtype: 'button',
					action: 'close',
					baseCls: 'productDetail-close',
					text: 'X'
			},
			{
				xtype : 'label',
				itemId : 'prodDetailLabel',
				cls: 'productDetail',
				tpl: new Ext.XTemplate(
				 	'<h2>{product.data.name}</h2>'+
				 	'<div class="price">{[values.product.calculate(values.amount)]}</div>'+
				 	'<div style="clear: both;">'+
				 	'<p class="desc">{product.data.longDesc}</p>'
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
					increment : 1,
					style : 'background-color:white;',
					value : 1,
					width: 200,
					minValue : '1',
					maxValue : '10',
					height: '25px',
					cycle : true,
				}, 
				]
			}]
		}, 
		{
			xtype : 'formpanel',
			itemId : 'choicesPanel',
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
					icon: '../app/res/images/into_cart.png',
					iconAlign: 'centered',
					action: 'cart'
				}
			]
		}
		]		
	}
});