/**
 * Displays products belonging to a menu.
 */
Ext.define('EatSense.view.ProductOverview', {
	extend : 'Ext.Container',
	xtype : 'productoverview',
	fullscreen : true,
	config : {
		items : [ 
//		          {
//			docked : 'top',
//			xtype : 'toolbar',
//			itemId: 'toolbar',
//			title : i18nPlugin.translate('menuTitle'),
//			items : [ {
//				xtype : 'button',
//				id : 'productOvBackBt',
//				text : i18nPlugin.translate('back'),
//				ui : 'back'
//			},
//			{        		 
//	            xtype: 'label',
//	            docked: 'right',
//	            html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
//    		}
//			]
//
//		}, {
//			docked : 'bottom',
//			xtype : 'toolbar',
//			itemId : 'menuBottomBar',
//			layout: {
//				type: 'hbox',
//				pack : 'center'
//			},
//			items: [
//				{
//				    title: 'Menu',
//				    iconCls: 'compose',
//				    id: 'bottomTapToMenu',
//				    iconMask: true
//				},
//				{
//				    title: 'Card',
//				    iconCls: 'organize',
//				    id: 'menuCartBt',
//				    iconMask: true,
//				}
//			        ]
//		},
		{
			xtype : 'list',
			layout : {
				align : 'center'
			},
			id : 'productlist',
			type : 'fit',
			allowDeselect : true,
			itemTpl : "<div class='productListItem'>" +
					"<h2 style='float: left; width: 80%; margin: 0;'>{name}</h2>  " +
					"<div style='position: absolute; right: 0; top: 50%; width: 20%; text-align: right; padding-right: 10px;'>{price}€</div>" +
					"<div style='clear: both;'></div>"+
					"<p style='clear: both;'>{shortDesc}</p>"+
					"</div>",
			listeners : {
				itemtap : function(dv, ix, item, e) {					
					dv.deselect(ix);
				}
			}
		} ]
	}
});