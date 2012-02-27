Ext.define('EatSense.view.MyOrders', {
	extend : 'Ext.Panel',
	xtype: 'myorders',	
	layout: {
		type: 'fit'
	},
	config: {
		items: [
		{
			docked : 'top',
			xtype : 'toolbar',
			itemId: 'myOrdersTopBar',
			title : i18nPlugin.translate('myOrdersTitle'),
			items : [
			{
				xtype: 'spacer'
			},
			{
				xtype: 'button',
				text: i18nPlugin.translate('payRequestButton'),
				itemId: 'payRequestBt',
				ui: 'forward'
			}
			]
		},
		{
			xtype: 'list',
			id: 'myorderlist',
			ui: 'round',
			styleHtmlContent: true,
			allowDeselect: true,
			onItemDisclosure: this.removeItem,
			itemTpl:  new Ext.XTemplate(
			"<div class='orderListItem''>" +
				"<h2 style='float: left; width: 80%; margin: 0;'>{Product.name}</h2>" +
				"<div style='position: absolute; right: 0; width: 30%; text-align: right; padding-right: 10px;'>("+i18nPlugin.translate('amount')+" {amount}) {Product.price_calculated}€</div>" +
				"<div style='clear: both;'>"+
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
					"<p>Kommentar: {comment}</p>" +
					"</tpl>" +
				"</div>" +
			"</div>"
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
			listeners: {
				itemtap: function(dv, ix, item, e) {
					Ext.defer((function() {
						dv.deselect(ix);
	    			}), globalConf.msgboxHideTimeout, this);					
				}
			}
		}, {
			type: 'panel',
			docked: 'bottom',
			itemId: 'myorderstotalpanel',
			items: [{
				xtype: 'label',				
				tpl: '<div class="cartTotal" style="text-align:center; font-size: 1.5em;"><h1>Total {0}€</h1></div>'
			}
			]			
		}		
		]
	},
	/**
	 * Show a loading screen
	 * @param mask
	 */
    showLoadScreen : function(mask) {
    	if(mask) {
    		this.setMasked({
    			message : i18nPlugin.translate('loadingMsg'),
        		xtype: 'loadmask' 
    		});
    	} else {
    		this.setMasked(false);
    	}
    }
});