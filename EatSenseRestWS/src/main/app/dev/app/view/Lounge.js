Ext.define('EatSense.view.Lounge', {
	extend : 'Ext.tab.Panel',
	requires : [ 
		'EatSense.view.MyOrders', 
		'EatSense.view.Menu', 
		'EatSense.view.MenuOverview', 
		'EatSense.view.SettingsTab', 
		'EatSense.view.RequestsTab' 
	],
	xtype : 'lounge',
	config : {
		tabBarPosition: 'bottom',
		tabBar : {
			itemId : 'loungeTabBar',
		},
		activeItem : 0,
		items : [ 
			{
				xtype: 'menutab',
				tabName: 'menu'	
			},
			{
				xtype: 'carttab',
				tabName: 'cart'
			},
			{
				xtype: 'myorderstab',
				tabName: 'myorders'
			},
			{
				xtype: 'requeststab',
				tabName: 'requests'
			},
			{
				xtype: 'settingstab',
				tabName: 'settings'
			}
		],
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