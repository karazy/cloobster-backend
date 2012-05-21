/**
 * Displays the businesses menu(s).
 * E. g.
 * Burgers, Drinks, Steaks, Salads
 * 
 */
Ext.define('EatSense.view.MenuOverview', {
	extend : 'Ext.Container',
	xtype : 'menuoverview',
	config : {
		items : [ 
		   {
			xtype : 'list',
			type : 'fit',
			ui: 'round',
			cls: 'std-background',
			onItemDisclosure: true,
			allowDeselect: true,
			itemTpl : '{title}',
			store: 'menuStore',
			listeners: {
				select : function(dv, index, target, record, e, eOpts) {					
					Ext.defer((function() {
						dv.deselectAll();
					}), 500, this);					
				}
			}
			
		} ]
	}
});
