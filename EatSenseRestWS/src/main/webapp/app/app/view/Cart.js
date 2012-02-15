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
				id: 'orderlist',
				styleHtmlContent: true,
				allowDeselect: true,
				onItemDisclosure: this.removeItem,
				itemTpl:  new Ext.XTemplate(
				"<div class='orderInCart''>" +
					"<h2>" +
						"{product.data.name} - {amount} - {[values.product.calculate(values.amount)]}€" +
					"</h2>" +
					"<tpl for='product.choicesStore.data'>" +
						"<tpl if='this.checkSelections(parent, xindex)'>" +
							"<h3>{text}</h3>" +
							"<ul>" +
								"<tpl for='options'>" +
									"<tpl if='selected === true'>" +
										"<li>{name}</li>" +
									"</tpl>" +
								"</tpl>" +
							"</ul>" +
						"</tpl>" +
					"</tpl>" +
					"<tpl if='comment!=\"\"'>" +
					"<h3>Kommentar:</h3>" +
					"<p>{comment}</p>" +
					"</tpl>" +
				"</div>"
					, {
					//checks if the current choice has selections. If not it will not be shown.
					//we need to pass the product as the choices object in this context is raw data
					checkSelections: function(parent, index) {
						var _hasSelections = parent.product.choices().getAt(index-1).hasSelections(); 
						return _hasSelections;
					}
				}
				)
			}
			        
			        ]
			/*
			 * 
			  
			 */
				
				
			
		} ]
	},
	
	removeItem: function() {
		console.log('disclosure');
	}

});