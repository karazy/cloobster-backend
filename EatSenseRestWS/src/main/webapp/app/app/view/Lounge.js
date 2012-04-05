Ext.define('EatSense.view.Lounge', {
	extend : 'Ext.tab.Panel',
	requires : [ 'EatSense.view.MyOrders', 'EatSense.view.Menu', 'EatSense.view.MenuOverview', 'EatSense.view.SettingsTab' ],
	xtype : 'lounge',
	config : {
		tabBar : {
			itemId : 'loungeTabBar',
			docked : 'bottom',
		},
		activeItem : 0,
		items : [ {
			title : i18nPlugin.translate('menuTitle'),
			itemId : 'menutab',
			name: 'menu',			
			iconCls : 'compose',
			layout : 'fit',
			items : [ {
				xtype : 'menu',
				itemId : 'menu',
				layout : 'fit'
			} ]
		}, {
			title : i18nPlugin.translate('cartTabBt'),
			itemId : 'carttab',
			iconCls : 'cart',
			layout : 'fit',
			items : [ {
				xtype : 'cart',
				itemId : 'cart',
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
			},
			]
		},
		{
			xtype: 'settingstab',
			tabName: 'settings'
		}
		]
	},
	/**
	 * Switch the tab.
	 * 
	 * @param direction
	 *            left or right
	 */
	switchTab : function(view, direction) {
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
	},

});