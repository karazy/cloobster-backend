/**
 * Displays details and options of a product.
 */
Ext.define('EatSense.view.ProductDetail', {
	extend : 'Ext.Panel',
	xtype : 'productdetail',	
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	config : {
		items : [
		{
			xtype : 'panel',
			docked : 'top',
			style : 'background-image: -webkit-linear-gradient(bottom, rgb(53,127,184) 4%, rgb(26,214,214) 87%);border-bottom:5px;border-bottom-left-radius: 10px;border-bottom-right-radius: 10px; padding: 10px',
			layout : {
				type : 'vbox',
			},
			items : [ {
				xtype : 'label',
				id : 'prodDetailLabel',
				tpl: 
					 '<div class="prodDetailWrapper" style="font-size:1em; margin-bottom: 10px;">'+
					 	'<div style="position: relative;">'+
					 		'<h2 style="float: left; width: 80%; margin: 0;">{product.data.name}</h2>'+
					 		//right: 0 , top : 50%
					 		'<div style="position: absolute; right: 0; top: 10; width: 20%; text-align: right;">{[values.product.calculate(values.amount)]}</div>'+
					 		'<div style="clear: both;">'+
					 	'</div><p style="font-size:0.8em;">{product.data.longDesc}</p>'+
					 '</div>'
				
			}, {
				xtype : 'panel',
				layout : {
					type : 'hbox',
					align : 'stretch'
				},
				defaults: {
					//height: '30px'
				},
				items : [ {
					xtype : 'spinnerfield',
					increment : 1,
					id : 'productAmountSpinner',
					style : 'background-color:white;',
					value : 1,
					flex : 3,
					//TODO Bug?
					maxHeight: '30px',
					minValue : '1',
					maxValue : '10',
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
			layout : {
				type: 'fit'
			},
			items : [
				{
					xtype : 'formpanel',
					itemId : 'choicesPanel',
					scrollable : 'vertical'
				}				
			]
		}
		]
	}
});