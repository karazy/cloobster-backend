/**
*	A single order item displayed in cart overview.
*	Item displays the name of the order. As well
*	as a button to edit and delete (remove) the order.
*/
Ext.define('EatSense.view.CartOverviewItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'cartoverviewitem',
	config: {

		name : {
			tpl: new Ext.XTemplate(
				"{Product.name} ({amount}x) {[this.formatPrice(values.Product.price_calculated)]}",
				{
					formatPrice: function(price) {
						console.log('CartOverviewItem formatPrice');
						return Karazy.util.formatPrice(price);
					}
				}
				),
				
				// "<h2>{Product.name}</h2>" +
				// "<div class='amount'>({amount}x)</div>" + 
				// "<div class='price'>{Product.price_calculated}€</div>",
			cls: 'name'
		},

		editButton : {
			iconCls : 'compose',
			iconMask : true,
			action : 'edit',
			ui: 'action',
			cls: 'edit'
		},

		cancelButton : {
			iconCls : 'trash',
			iconMask : true,
			action : 'cancel',
			ui: 'action',
			cls: 'cancel'
		},

		 cls: 'cartoverviewitem',
		layout: {
			type: 'hbox',
			pack: 'end',
			align: 'center'
		}
	},

	applyName: function(config) {
		return Ext.factory(config, Ext.Label, this.getName());
	},

	updateName: function(newItem, oldItem) {
		if(newItem) {
			this.add(newItem);
		}

		if(oldItem) {
			this.remove(oldItem);
		}
	},

	applyEditButton: function(config) {
		return Ext.factory(config, Ext.Button, this.getEditButton());
	},

	updateEditButton: function(newItem, oldItem) {
		if(newItem) {
			this.add(newItem);
		}

		if(oldItem) {
			this.remove(oldItem);
		}
	},

	applyCancelButton: function(config) {
		return Ext.factory(config, Ext.Button, this.getCancelButton());
	},

	updateCancelButton: function(newItem, oldItem) {
		if(newItem) {
			this.add(newItem);
		}

		if(oldItem) {
			this.remove(oldItem);
		}
	},

	updateRecord: function(newRecord) {
		if(!newRecord) {
			return;
		};

		var		name = this.getName();

		name.getTpl().overwrite(name.element, newRecord.getData(true));

		this.callParent([newRecord]);	
	}


})