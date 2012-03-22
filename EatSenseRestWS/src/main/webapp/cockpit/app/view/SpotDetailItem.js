/**
*	A single order item shown in SpotDetail.
*
*/
Ext.define('EatSense.view.SpotDetailItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotdetailitem',
	requires: [
        'Ext.Label',
        'Ext.Button'
    ],
	config: {

		//label containing product details
		name: {		
			cls: 'spotdetailitem-order',	
			tpl: new Ext.XTemplate(
			// "<div class='orderListItem'>" +
				"<h2 >{Product.name}</h2>" +
				"<div class='price'>{amount} - {Product.price_calculated}€</div>" +
				"<div class='choices'>"+
					"<tpl for='Product.choices'>" +				
						"<tpl if='this.checkSelections(values, xindex)'>" +
							//"<h3>{text}</h3>" +
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
				"</div>" 
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
			})
		},

		//flag showing if an order is new
		flag: {
			cls: 'spotdetailitem-flag' 
		},
		//cancel Order
		cancelButton: {
			action: 'cancel',
			iconCls: 'delete',
			ui: 'action',
			iconMask: true,
			cls: 'spotdetailitem-cancel' 
		},
		//mark order as processed
		confirmButton: {
			action: 'confirm',
			ui: 'action',
			iconCls: 'add',
			iconMask: true,
			cls: 'spotdetailitem-confirm' 
		},



		// dataMap: {
	 //    	getName: {
	 //           setHtml: 'status'
	 //       	},

	 //    },

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
		} else {
			this.getFlag().hide();
			this.getConfirmButton().disable();
			this.getCancelButton().disable();
		}

		//overrides the default updateRecord, perhabs we remove this completely
		this.callParent([newRecord]);
	}

});