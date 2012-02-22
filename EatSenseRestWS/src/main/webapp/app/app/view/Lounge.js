Ext.define('EatSense.view.Lounge', {
	extend : 'Ext.TabPanel',
	requires : [ 'EatSense.view.MyOrders', 'EatSense.view.Menu' ],
	xtype : 'lounge',
	config : {
		tabBarPosition : 'bottom',
		activeItem : 0,
		layout : {
		// type : 'fit'
		},
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			itemId : 'loungeTopBar',
			title : i18nPlugin.translate('loungeviewTitle')
		}, {
			xtype : 'panel',
			// layout : 'fit',
			title : i18nPlugin.translate('myOrdersTabBt'),
			iconCls : 'home',
			items : [ {
				xtype : 'myorders',
				itemId : 'myorderstab',
				layout : 'fit'
			} ]

		}, {
			xytpe : 'panel',
			title : 'Menu',
			iconCls : 'compose',
			html : 'TEST'
		}
		// {
		// xtype: 'menu',
		// itemdId: 'menutab',
		// title: i18nPlugin.translate('menuTitle'),
		// iconCls: 'compose',
		// }
		]
	},
	/**
	 * Change the direction of the slide animation.
	 * 
	 * @param direction
	 *            left or right
	 */
	switchTab : function(view, direction) {
		var cardpanel = this.getComponent('loungeCardPanel');
		cardpanel.getLayout().setAnimation({
			type : 'slide',
			direction : direction
		});
		cardpanel.setActiveItem(view);
	},
	/**
	 * Hides the back button in top toolbar.
	 */
	hideBackButton : function() {
		this.getComponent('loungeTopBar').getComponent('loungeBackBt').hide();
	},
	/**
	 * Shows the back button in top toolbar.
	 * 
	 * @param text
	 *            Label to display on button.
	 */
	showBackButton : function(text) {
		this.getComponent('loungeTopBar').getComponent('loungeBackBt').setText(
				text);
		this.getComponent('loungeTopBar').getComponent('loungeBackBt').show();
	}

});