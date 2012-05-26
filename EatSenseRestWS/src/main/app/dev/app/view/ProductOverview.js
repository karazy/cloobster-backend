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
			itemCls: 'productListItem',
			itemTpl : new Ext.XTemplate(
					"<table style='width:100%;'>"+
						"<td align='left'><h2 class='title'>{name}</h2></td><td align='right'><h2 class='price'>{[this.formatPrice(values.price)]}</h2></td>" +
					"</table>"+
					"<p>{shortDesc}</p>",
					{
						formatPrice: function(price) {
							return Karazy.util.formatPrice(price);
						}
					}
				),					
			listeners : {
				select : function(dv, index, target, record, e, eOpts) {					
					Ext.defer((function() {
						dv.deselectAll();
					}), 500, this);					
				}
			}
		} ]
	}
});