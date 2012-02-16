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
					"{product.data.name} - {amount} - {[values.product.calculate(values.amount)]}€" +
				"</h2>" +
				"<tpl for='product.choicesStore.data'>" +
					"<tpl if='this.checkSelections(parent, xindex)'>" +
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
				checkSelections: function(parent, index) {
					var _hasSelections = parent.product.choices().getAt(index-1).hasSelections(); 
					return _hasSelections;
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