Ext.define('EatSense.view.Lounge', {
	extend : 'Ext.tab.Panel',
	requires : [ 'EatSense.view.MyOrders', 'EatSense.view.Menu', 'EatSense.view.MenuOverview' ],
	xtype : 'lounge',
	config : {
		tabBar : {
			itemId: 'loungeTabBar',
			docked : 'bottom',
		},
		activeItem : 0,
		items : [ 
		{
			title : i18nPlugin.translate('menuTitle'),
			itemId : 'menutab',
			iconCls : 'compose',
			layout : 'fit',
			items : [ {
				xtype : 'menu',
				itemId : 'menu',
				layout : 'fit'
			} ]
		}, {
			title : i18nPlugin.translate('myOrdersTabBt'),
			iconCls : 'home',
			itemId : 'myorderstab',
			layout : 'fit',
			items : [ {
				xtype : 'myorders',
				itemId : 'myorders',
				layout : 'fit'
			} ]
		}, {
			title : i18nPlugin.translate('cartTabBt'),
			itemId : 'carttab',
			iconCls : 'organize',
			layout : 'fit',
			items : [ {
				xtype : 'cart',
				itemId : 'cart',
				layout : 'fit'
			} ]
		}

		]

	},
	/**
	 * Change the direction of the slide animation.
	 * 
	 * @param direction
	 *            left or right
	 */
	switchTab : function(view, direction) {
//		var cardpanel = this.getComponent('loungeCardPanel');
//		cardpanel.getLayout().setAnimation({
//			type : 'slide',
//			direction : direction
//		});
//		cardpanel.setActiveItem(view);
		this.setActiveItem(view);
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
		this.getComponent('loungeTopBar').getComponent('loungeBackBt').setText(text);
		this.getComponent('loungeTopBar').getComponent('loungeBackBt').show();
	}

});