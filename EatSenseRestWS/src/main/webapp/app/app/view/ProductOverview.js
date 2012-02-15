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
			type : 'fit',
			allowDeselect : true,
			itemTpl : "<div class='productListItem'>" +
					"<h2 style='float: left; width: 80%; margin: 0;'>{name}</h2>  " +
					"<div style='position: absolute; right: 0; top: 50%; width: 20%; text-align: right; padding-right: 10px;'>{price}€</div>" +
					"<div style='clear: both;'></div>"+
					"<p style='clear: both;'>{shortDesc}</p>"+
					"</div>",
			listeners : {
				itemtap : function(dv, ix, item, e) {					
					dv.deselect(ix);
				}
			}
		} ]
	}
});