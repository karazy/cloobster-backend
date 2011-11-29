Ext.define('EatSense.view.Main', {
	extend : 'Ext.Container',
	requires : [ 'EatSense.view.Dashboard', 'EatSense.view.MenuOverview' ],
	config : {
		fullscreen : false,
		layout : 'card',
		activeItem : 0,
		items : [ {
			layout : 'fit',
			xtype : 'dashboard'
		}, {
			layout : 'fit',
			xtype : 'menuoverview'
		} ]
	}
});
