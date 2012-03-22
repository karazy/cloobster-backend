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
				"<div> {amount} {Product.price_calculated}€</div>" +
				"<div>"+
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

		//mark order as processed
		confirmButton: {
			action: 'confirm',
			text: 'confirm'
		},

		// dataMap: {
	 //    	getName: {
	 //           setHtml: 'status'
	 //       	},

	 //    },

		layout: {
			type: 'hbox',
			align: 'center'
		}

	},

	applyName: function(config) {
		console.log('SpotDetailItem applyName');
		var obj = Ext.factory(config, Ext.Label, this.getName());
		return obj;
	},

	updateName: function(newName, oldName) {
		console.log('SpotDetailItem updateName');
		if(newName) {
			this.add(newName);
		}

		if(oldName) {
			this.remove(oldName);
		}
	},

	applyConfirmButton: function(config) {
		console.log('SpotDetailItem applyConfirmButton');
		var button = Ext.factory(config, Ext.Button, this.getConfirmButton());
		return button;
	},

	updateConfirmButton: function(newItem, oldItem) {
		console.log('SpotDetailItem updateConfirmButton');
		if(newItem) {
			this.add(newItem);
		}

		if(oldItem) {
			this.remove(oldItem);
		}
	},

	applyFlag: function(config) {
		console.log('LOGG');
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

	updateRecord: function(newRecord) {
		console.log('SpotDetailItem updateRecord');
		

		// this.getName().setHtml(newRecord.raw.product.name);
		this.getName().getTpl().overwrite(this.getName().element, newRecord.getData(true));

		if(newRecord.get('status') == Karazy.constants.Order.PLACED) {
			this.getFlag().setHtml(Karazy.i18n.translate('PLACED'));
			this.getFlag().show();			
			this.getConfirmButton().enable();
		} else {
			this.getFlag().hide();
			this.getConfirmButton().disable();
		}

		//overrides the default updateRecord, perhabs we remove this completely
		this.callParent([newRecord]);
	}

});