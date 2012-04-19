Ext.define('EatSense.view.MyOrders', {
	extend : 'Ext.Panel',
	xtype: 'myorderstab',	
	config: {
		layout: {
			type: 'fit'
		},
		iconCls : 'home',
		title: Karazy.i18n.translate('myOrdersTabBt'),
		iconMask : true,
		itemId : 'myorderstab',
		items: [
		{
			docked : 'top',
			xtype : 'titlebar',
			itemId: 'myOrdersTopBar',
			title : Karazy.i18n.translate('myOrdersTitle'),
			items : [
			{
				xtype: 'button',
				text: Karazy.i18n.translate('payRequestButton'),
				itemId: 'payRequestBt',
				ui: 'forward',
				action: 'pay',
				align: 'right'
			},
			{
				xtype: 'button',
				text: Karazy.i18n.translate('leaveButton'),
				ui: 'forward',
				action: 'leave',
				align: 'right'
			}
			]
		},
		{
			xtype: 'list',
			// id: 'myorderlist',
			store: 'orderStore',
			ui: 'round',
			styleHtmlContent: true,
			allowDeselect: true,
			onItemDisclosure: this.removeItem,
			itemCls: 'orderListItem',
			itemTpl:  new Ext.XTemplate(
			"<div class='orderListItem {[values.status.toLowerCase()]}'>" +
				"<h2 style='float: left; width: 80%; margin: 0;'>{Product.name}</h2>" +
				"<div style='position: absolute; right: 0; width: 30%; text-align: right; padding-right: 10px;'>("+Karazy.i18n.translate('amount')+" {amount}) {[this.formatPrice(values.Product.price_calculated)]}</div>" +
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
				},
				formatPrice: function(price) {
					return Karazy.util.formatPrice(price);
				}				
			}),
			listeners: {
				select: function(dv, ix, item, e) {
					Ext.defer((function() {
						dv.deselect(ix);
	    			}), 100, this);					
				}
			}
		}, {
			type: 'panel',
			docked: 'bottom',
			itemId: 'myorderstotalpanel',
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
			},
			{
				type: 'panel',
				docked: 'bottom',
				itemId: 'myorderscompletepanel',
				hidden: true,
				items: [{
					xtype: 'button',
					text: Karazy.i18n.translate('leave'),
					ui: 'confirm',
					action: 'complete',
					height: '50px'
				}
				]
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
    			message : Karazy.i18n.translate('loadingMsg'),
        		xtype: 'loadmask' 
    		});
    	} else {
    		this.setMasked(false);
    	}
    }
});