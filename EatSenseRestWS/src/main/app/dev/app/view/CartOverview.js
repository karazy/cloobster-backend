Ext.define('EatSense.view.CartOverview', {
	extend : 'Ext.Panel',
	xtype: 'cartoverview',
	requires: ['EatSense.view.CartOverviewItem'],
	config: {
		items: [
		 {
				xtype: 'dataview',
				itemId: 'orderlist',
				useComponents: true,
				cls: 'cartoverview',
				defaultType: 'cartoverviewitem',
				grouped: true			
		}, 
		{
			type: 'panel',
			docked: 'bottom',
			itemId: 'carttotalpanel',
			items: [{
				xtype: 'label',		
				cls: 'cartTotal',		
				tpl: new Ext.XTemplate('<h1>Total {[this.formatPrice(values.price)]}</h1>',
					{
						formatPrice: function(price) {
							return Karazy.util.formatPrice(price);
						}
					}
				)
			}
			]			
		}
		]
	}
});