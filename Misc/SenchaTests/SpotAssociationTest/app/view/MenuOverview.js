/**
 * Displays the restaurants menu(s).
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
			allowDeselect: true,
			itemTpl : '<div>{title}</div>',
			listeners: {
				itemtap: function(dv, ix, item, e) {
					dv.deselect(ix);
				}
			}
			
		} ]
	}
});