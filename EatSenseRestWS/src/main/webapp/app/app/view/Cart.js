Ext.define('EatSense.view.Cart', {
	extend : 'Ext.Panel',
	xtype : 'cart',
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
			xtype : 'panel',
			itemId : 'cartBottomBar',
			layout: {
				type: 'hbox',
				pack : 'center'
			},
			items: [
				{
					xtype: 'button',
				    title: 'Cancel',
				    iconCls: 'trash',
				    itemId: 'bottomTapCancel',
				    iconMask: true
				},
				{
					xtype: 'button',
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
				xtype: 'cartoverview',
				itemId: 'cartoverview',
				layout: 'fit'
			}, 
			{
				xtype: 'productdetail',
				itemId : 'cartProductdetail',
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