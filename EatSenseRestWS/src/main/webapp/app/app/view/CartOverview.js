Ext.define('EatSense.view.CartOverview', {
	extend : 'Ext.Panel',
	xtype: 'cartoverview',
	config: {
		items: [
		{
			xtype: 'list',
			itemId: 'orderlist',
			styleHtmlContent: true,
			allowDeselect: true,
			onItemDisclosure: this.removeItem,
			itemTpl:  new Ext.XTemplate(
			"<div class='orderInCart''>" +
				"<h2>" +
					"{Product.name} - {amount} " +
				"</h2>" +
				"<tpl for='Product.choices'>" +				
					"<tpl if='this.checkSelections(values, xindex)'>" +
						"<h3>{text}</h3>" +
						"<ul>" +
							"<tpl for='options'>" +
								"<tpl if='selected === true'>" +
									"<li>{name}</li>" +
								"</tpl>" +
							"</tpl>" +
						"</ul>" +
					"</tpl>" +
				"</tpl>" +
				"<tpl if='comment!=\"\"'>" +
				"<h3>Kommentar:</h3>" +
				"<p>{comment}</p>" +
				"</tpl>" +
			"</div>"
				, {
				//checks if the current choice has selections. If not it will not be shown.
				//we need to pass the product as the choices object in this context is raw data
				checkSelections: function(values, xindex) {
					console.log('Cart Overview -> checkSelections(parent, values, xindex)');				
					var result = false;
					values.options().each(function(option) {
						if(option.get('selected') === true) {
							result = true;
						}
					});
					return result;
				}
			})
		}, {
			type: 'panel',
			docked: 'bottom',
			itemId: 'carttotalpanel',
//			styleHtmlContent: true,
			tpl: '<div class="cartTotal" style="text-align:center;"><h2>Total {0}</h2></div>'
		}
		]
	}
});

//backup from checkSelections
//var _hasSelections = parent.product.choices().getAt(index-1).hasSelections(); 
//return _hasSelections;	