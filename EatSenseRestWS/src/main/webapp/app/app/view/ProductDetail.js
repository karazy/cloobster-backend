/**
 * Displays details and options of a product.
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',	
	layout : {
		type : 'vbox',
		align : 'stretch'
	},
	config : {
    	scrollable : 'vertical',
		items : [
		{
			xtype : 'panel',
			cls: 'productDetailPanel',
			layout : {
				type : 'vbox',
			},
			items : [ {
				xtype : 'label',
				itemId : 'prodDetailLabel',
				cls: 'productDetail',
				tpl: new Ext.XTemplate(
					 	'<div>'+
					 		'<h2>{product.data.name}</h2>'+
					 		'<div class="price">{[values.product.calculate(values.amount)]}</div>'+
					 		'<div style="clear: both;">'+
					 	'</div><p class="desc">{product.data.longDesc}</p>'
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
					itemId : 'productAmountSpinner',
					style : 'background-color:white;',
					value : 1,
					width: 200,
					minValue : '1',
					maxValue : '10',
					height: '25px',
					cycle : true,
				}, 
				]
			} ]
		}, 
		{
			xtype : 'formpanel',
			itemId : 'choicesPanel',
			layout: 'vbox',
			minHeight: '200px',
			scrollable : false,
		}
		]		
	}
});