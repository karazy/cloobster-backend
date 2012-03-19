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
//			docked : 'top',
			style : 'background-image: -webkit-linear-gradient(bottom, rgb(53,127,184) 4%, rgb(26,214,214) 87%);border-bottom:5px;border-bottom-left-radius: 10px;border-bottom-right-radius: 10px; padding: 10px',
			layout : {
				type : 'vbox',
			},
			items : [ {
				xtype : 'label',
				itemId : 'prodDetailLabel',
				tpl: new Ext.XTemplate(
					 '<div class="prodDetailWrapper" style="font-size:1em; margin-bottom: 10px;">'+
					 	'<div style="position: relative;">'+
					 		'<h2 style="float: left; width: 80%; margin: 0;">{product.data.name}</h2>'+
					 		//right: 0 , top : 50%
					 		'<div style="position: absolute; right: 0; top: 10; width: 30%; text-align: right;">{[values.product.calculate(values.amount)]}</div>'+
					 		'<div style="clear: both;">'+
					 	'</div><p style="font-size:0.7em;">{product.data.longDesc}</p>'+
					 '</div>'	
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
//				{
//					xtype: 'spacer',
//					width: 20
//				},
//				{
//					xtype : 'button',
//					itemId : 'prodDetailcartBt',
//					iconCls : 'compose',
//					iconMask : true,
//				} 
				]
			} ]
		}, 
		{
//			xtype : 'panel',
//			itemId : 'choicesWrapper',
//			height: '200px',
//			layout : {
//				type: 'fit'
//			},
//			items : [
//				{
					xtype : 'formpanel',
					itemId : 'choicesPanel',
					layout: 'vbox',
					minHeight: '200px',
					scrollable : false,
//				}				
//			]
		}
		]		
	}
});