Ext.define('EatSense.view.CartOverview', {
	extend : 'Ext.Panel',
	xtype: 'cartoverview',
	config: {
		items: [
		{
			xtype: 'list',
			itemId: 'orderlist',
			ui: 'round',
			// styleHtmlContent: true,
			allowDeselect: true,
			onItemDisclosure: this.removeItem,
			itemCls: 'orderListItem',
			itemTpl:  new Ext.XTemplate(
			// "<div class='orderListItem''>" +
				"<h2>{Product.name}</h2>" +
				"<div class='price'>("+i18nPlugin.translate('amount')+" {amount}) {Product.price_calculated}€</div>"+
				"<div style='clear: both;'>"
				// 	"<tpl for='Product.choices'>" +				
				// 		"<tpl if='this.checkSelections(values, xindex)'>" +
				// 			"<h3>{text}</h3>" +
				// 			"<ul>" +
				// 				"<tpl for='options'>" +
				// 					"<tpl if='selected === true'>" +
				// 						"<li>{name}</li>" +
				// 					"</tpl>" +
				// 				"</tpl>" +
				// 			"</ul>" +
				// 		"</tpl>" +
				// 	"</tpl>" +
				// 	"<tpl if='comment!=\"\"'>" +
				// 	"<p>Kommentar: {comment}</p>" +
				// 	"</tpl>" +
				// "</div>" +
			// "</div>"
				, {
				//checks if the current choice has selections. If not it will not be shown.
				//we need to pass the product as the choices object in this context is raw data
				checkSelections: function(values, xindex) {
					console.log('Cart Overview -> checkSelections');				
					var result = false;
					Ext.each(values.options,
							function(option) {
						if(option.selected === true) {
							result = true;
						}
					});
					
					return result;
				}
			}),
			listeners : {
				select : function(dv, index, target, record, e, eOpts) {					
					Ext.defer((function() {
						dv.deselectAll();
					}), 100, this);					
				}
			}
		}, {
			type: 'panel',
			docked: 'bottom',
			itemId: 'carttotalpanel',
			items: [{
				xtype: 'label',				
				tpl: '<div class="cartTotal" style="text-align:center; font-size: 1.5em;"><h1>Total {0}€</h1></div>'
			}
			]			
		}
		]
	}
});