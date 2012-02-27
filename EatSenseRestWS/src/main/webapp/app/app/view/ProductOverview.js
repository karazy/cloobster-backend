/**
 * Displays products belonging to a menu.
 */
Ext.define('EatSense.view.ProductOverview', {
	extend : 'Ext.Container',
	xtype : 'productoverview',
	fullscreen : true,
	config : {
		items : [ 
		{
			xtype : 'list',
			itemId : 'productlist',
			ui: 'round',
			type : 'fit',
			allowDeselect : true,
			itemTpl : "<div class='productListItem'>" +
					"<h2>{name}</h2>  " +
					"<div class='price'>{price}€</div>" +
					"<div style='clear: both;'></div>"+
					"<p>{shortDesc}</p>"+
					"</div>",
			listeners : {
				select : function(dv, index, target, record, e, eOpts) {					
					Ext.defer((function() {
						dv.deselectAll();
					}), 100, this);					
				}
			}
		} ]
	}
});