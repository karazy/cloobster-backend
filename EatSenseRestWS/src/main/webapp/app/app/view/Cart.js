Ext.define('EatSense.view.Cart', {
	extend : 'Ext.Panel',
//	extend : 'EatSense.SpecializedCardPanel',
//	requires: ['EatSense.SpecializedCardPanel'],
	xtype : 'cartview',
	layout : {
		type : 'vbox',
		align : 'middle'
	},
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId: 'cartTopBar',
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
				    itemId: 'bottomTapCancel',
				    iconMask: true
				},
				{
				    title: 'Send',
				    iconCls: 'action',
				    itemId: 'bottomTapOrder',
				    iconMask: true
				}
			        ]
		}, {
			xtype: 'panel',
			itemId: 'cartCardPanel',
			layout : {
				type: 'card'
			},
			items: [ {
				xtype: 'list',
				itemId: 'orderlist',
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
				})
			}, 
			{
				xtype: 'productdetail',
				itemId : 'productdetail',
				layout: 'fit'
			}			        
			]
			
		} ]
	},
	
	removeItem: function() {
		console.log('disclosure');
	},
	
	/**
	 * Change the direction of the slide animation.
	 * @param direction
	 * 			left or right
	 */
	switchView : function(view, direction){
		var cardpanel = this.getComponent('cartCardPanel');
		cardpanel.getLayout().setAnimation({
			 type: 'slide',
	         direction: direction
		});
		cardpanel.setActiveItem(view);
	},
	/**
	 * Hides the back button in top toolbar.
	 */
	hideBackButton: function() {
		this.getComponent('cartTopBar').getComponent('cartBackBt').hide();
	},
	/**
	 * Shows the back button in top toolbar.
	 * @param text
	 * 		Label to display on button.
	 */
	showBackButton: function(text) {
		this.getComponent('cartTopBar').getComponent('cartBackBt').setText(text);
		this.getComponent('cartTopBar').getComponent('cartBackBt').show();
	}

});