/**
 * The viewport. Defining the global layout for the application.
 */
Ext.define('EatSense.view.Main', {
	extend : 'Ext.Container',
	requires : [ 'EatSense.view.Dashboard', 'EatSense.view.MenuOverview', 'EatSense.view.Checkinconfirmation','EatSense.view.ProductOverview','EatSense.view.CheckinWithOthers' ],
	config : {
		fullscreen : true,
		layout : {
			type: 'card',
			animation: {
	            type: 'slide',
	            direction: 'left'
	        }			
		},
		activeItem : 0,
		items : [ {
			layout : 'fit',
			xtype : 'dashboard'
		}, {
			layout : 'fit',
			xtype : 'menuoverview'
		}, {
			layout : 'fit',
			xtype : 'checkinconfirmation'
		}, {
			layout: 'fit',
			xtype: 'checkinwithothers'
		},
		{
			layout: 'fit',
			xtype: 'productoverview'
		},
		{
			layout: 'fit',
			xtype: 'productdetail'
		} 
		]
	}
});
