Ext.define('EatSense.view.Cart', {
	extend : 'Ext.Panel',
	xtype : 'cartview',
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : i18nPlugin.translate('cartviewTitle'),
			items : [ {
				xtype : 'button',
				itemId : 'cartBackBt',
				text : i18nPlugin.translate('back'),
				ui : 'back'
			} ]
		}, {
			docked : 'bottom',
			xtype : 'toolbar',
			itemId : 'cartBottomBar',
			layout: {
				type: 'hbox',
				pack : 'center'
			},
			items: [
				{
				    title: 'Cancel',
				    iconCls: 'trash',
				    id: 'bottomTapCancel',
				    iconMask: true
				},
				{
				    title: 'Send',
				    iconCls: 'action',
				    id: 'bottomTapOrder',
				    iconMask: true
				}
			        ]
		}, {
			xtype: 'panel',
			itemId: 'cartPanel',
			layout : {
				type: 'fit'
			},
			items: [ {
				xtype: 'list',
				type : 'fit',
				itemId: 'orderlist',
				itemTpl:  "<div class='orderInCart'>" +
				"<div>" +
				"Menge {amount}" +
				"</div>" +
//				"<div class='choicesInCart>'" +
//				"<tpl for='product.choices'>" +
//				"<tpl for='options'>" +
//				"<tpl if='selected == true>'" +
//				"{name}" +
//				"</tpl>" +
//				"</tpl>" +
//				"</tpl>" +
//				"</div>" +
				"</div>"
			}
			        
			        ]
			/*
			 * 
			  
			 */
				
				
			
		} ]
	}

});