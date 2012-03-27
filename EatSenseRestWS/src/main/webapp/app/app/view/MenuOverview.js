/**
 * Displays the businesses menu(s).
 * E. g.
 * Burgers, Drinks, Steaks, Salads
 * 
 */
Ext.define('EatSense.view.MenuOverview', {
	extend : 'Ext.Container',
	xtype : 'menuoverview',
	fullscreen : false,
	config : {
		items : [ 
		   {
			xtype : 'list',
			id : 'menulist',
			type : 'fit',
			ui: 'round',
			allowDeselect: true,
			itemTpl : '<div>{title}</div>',
			listeners: {
				select : function(dv, index, target, record, e, eOpts) {					
					Ext.defer((function() {
						dv.deselectAll();
					}), 100, this);					
				}
			}
			
		} ]
	}
});
