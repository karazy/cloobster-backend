/**
*	A single order item shown in SpotDetail.
*	Displays the status, name and possible actions.
*/
Ext.define('EatSense.view.SpotDetailItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotdetailitem',
	requires: [
        'Ext.Label',
        'Ext.Button'
    ],
	config: {
		//flag showing if an order is new
		flag: {
			cls: 'spotdetailitem-flag' 
		},
		//label containing product details
		name: {
			cls: 'spotdetailitem-order',	
			tpl: new Ext.XTemplate(
				"<h2>{amount}x {Product.name}</h2> {[this.formatPrice(values.Product.price_calculated)]}" +
				"<div class='choices'>"+
					"<tpl for='Product.choices'>" +				
						"<tpl if='this.checkSelections(values, xindex)'>" +
							"<tpl if='!parent'><h3>{text}</h3></tpl>" +
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
					"<p>"+Karazy.i18n.translate('comment')+": {comment}</p>" +
					"</tpl>" +
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
			})
		},

		//cancel Order
		cancelButton: {
			action: 'cancel',
			iconCls: 'trash',
			ui: 'action',
			iconMask: true,
			cls: 'spotdetailitem-cancel' 
		},
		//mark order as processed
		confirmButton: {
			action: 'confirm',
			ui: 'action',
			iconCls: 'spotdetailitem-confirm',
			// icon: 'res/images/check_icon.png',
			iconMask: true,
			cls: 'spotdetailitem-confirm' 
		},

		layout: {
			type: 'hbox',
			pack: 'end',
			align: 'start'
		}

	},

	applyName: function(config) {
		var obj = Ext.factory(config, Ext.Label, this.getName());
		return obj;
	},

	updateName: function(newName, oldName) {
		if(newName) {
			this.add(newName);
		}

		if(oldName) {
			this.remove(oldName);
		}
	},

	applyCancelButton: function(config) {
		var button = Ext.factory(config, Ext.Button, this.getCancelButton());
		return button;
	},

	updateCancelButton: function(newItem, oldItem) {		
		if(newItem) {
			this.add(newItem);
		}

		if(oldItem) {
			this.remove(oldItem);
		}
	},

	applyConfirmButton: function(config) {
		var button = Ext.factory(config, Ext.Button, this.getConfirmButton());
		return button;
	},

	updateConfirmButton: function(newItem, oldItem) {
		if(newItem) {
			this.add(newItem);
		}

		if(oldItem) {
			this.remove(oldItem);
		}
	},

	applyFlag: function(config) {
		return Ext.factory(config, Ext.Label, this.getFlag());
	},

	updateFlag: function(newItem, oldItem) {
		if(newItem) {
			this.add(newItem);
		}
		if(oldItem) {
			this.remove(oldItem);
		}
	}, 

	/**
	*	Overrides the private updateRecord method. Does some special actions
	*	which could not be done in dataMap. 
	*	 
	*/
	updateRecord: function(newRecord) {
		console.log('SpotDetailItem updateRecord');
		if(!newRecord) {
			return;
		}
		
		//make sure prices are calculated before displaying
		newRecord.calculate();
		// this.getName().setHtml(newRecord.raw.product.name);
		this.getName().getTpl().overwrite(this.getName().element, newRecord.getData(true));

		if(newRecord.get('status') == Karazy.constants.Order.PLACED) {
			this.getFlag().setHtml(Karazy.i18n.translate('PLACED'));
			this.getFlag().show();			
			this.getConfirmButton().enable();
			this.getCancelButton().enable();
		} else if(newRecord.get('status') == Karazy.constants.Order.RECEIVED) {
			this.getFlag().hide();		
			this.getConfirmButton().disable();
			this.getCancelButton().enable();
		} else if(newRecord.get('status') == Karazy.constants.Order.CANCELED) {
			// this.getName().addCls('spotdetailitem-order-cancel');
			this.getFlag().hide();
			this.getConfirmButton().disable();
			this.getCancelButton().disable();
		}

		(newRecord.get('status') == Karazy.constants.Order.CANCELED) ? 
			this.getName().addCls('spotdetailitem-order-cancel') : this.getName().removeCls('spotdetailitem-order-cancel');

		//overrides the default updateRecord, so we need to call ist (perhabs we can remove this call completely?!)
		this.callParent([newRecord]);
	}

});