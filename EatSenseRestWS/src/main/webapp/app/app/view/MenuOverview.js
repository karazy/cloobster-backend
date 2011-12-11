Ext.define('EatSense.view.MenuOverview', {
	extend : 'Ext.Container',
	xtype : 'menuoverview',
	fullscreen : true,
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : 'Menu'
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				pack : 'center',
				align : 'center',
			},
			defaults : {
				margin : 5
			},
			items : [ {
				xtype : 'button',
				id : 'subMenu1',
				text : 'Vorspeisen',
				ui : 'round'
			}, {
				xtype : 'button',
				itemId : 'subMenu2',
				text : 'Hauptspeisen',
				ui : 'round',
			}, {
				xtype : 'button',
				itemId : 'subMenu3',
				text : 'Desert',
				ui : 'round',
			}, {
				xtype : 'button',
				itemId : 'subMenu4',
				text : 'Getränke',
				ui : 'round',
			} ]
		} ]
	}
});
