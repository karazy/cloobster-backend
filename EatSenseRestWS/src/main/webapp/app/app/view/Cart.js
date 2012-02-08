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
				id : 'cartBackBt',
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
				styleHtmlContent: true,
				itemTpl:  new Ext.XTemplate(
				"<div class='orderInCart''>" +
					"<h2>" +
						"{product.data.name} - {amount} - {[values.product.calculate(values.amount)]}€" +
					"</h2>" +
//				"<div class='choicesInCart>'" +
				"<tpl for='product.data.choices'>" +
				"<h3>{text}</h3>" +
				"<ul><tpl for='options'>" +
				"<tpl if='selected === true'>" +
				"<li>{name}</li>" +
				"</tpl>" +
				"</tpl></ul>" +
				"</tpl>" +
//				"</div>" +
				"</div>"
				)
			}
			        
			        ]
			/*
			 * 
			  
			 */
				
				
			
		} ]
	}

});