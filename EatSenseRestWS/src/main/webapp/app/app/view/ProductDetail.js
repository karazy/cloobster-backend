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
		//scrollable : 'vertical',
		// styleHtmlContent : true,
		items : [
//		         {
//			docked : 'top',
//			xtype : 'toolbar',
//			itemId : 'toolbar',
//			title : i18nPlugin.translate('menuTitle'),
//			items : [ {
//				xtype : 'button',
//				id : 'prodDetailBackBt',
//				ui : 'back'
//			},
//			{        		 
//	            xtype: 'label',
//	            docked: 'right',
//	            html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
//    		}
//			]
//
//		},
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
					 '<div class="prodDetailWrapper" style="font-size:1em, margin-bottom: 10px">'+
					 	'<div style="position: relative;">'+
					 		'<h2 style="float: left; width: 80%; margin: 0, font-size:1.5em;">{name}</h2>'+
					 		//right: 0 , top : 50%
					 		'<div style="position: absolute; right: 0; top: 10; width: 20%; text-align: right; font-size:1.5em;">{price}</div>'+
					 		'<div style="clear: both;">'+
					 	'</div><p>{longDesc}</p>'+
					 '</div>'
				
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
//				{
//					xtype : 'label',
//					itemId : 'choicesPanelTitle',
//					flex : 1
//				},
				{
					xtype : 'formpanel',
//					flex: 2,
					itemId : 'choicesPanel',
					scrollable : 'vertical',
					//layout : 'fit'
//					height: 200
				}
				
			]
		} ]
	}
});