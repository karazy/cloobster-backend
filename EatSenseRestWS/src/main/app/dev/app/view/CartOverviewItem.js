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
				"<table style='width:100%;'>"+
					"<td align='left'><h2 class='title'>{amount} x {Product.name}</h2></td><td align='right'><h2 class='price'>{[this.formatPrice(values.Product.price_calculated)]}</td></h2>"+
					// "<td align='left'><h8>{amount}x</h8> <h9>{Product.name}</h9></td><td align='right'>{[this.formatPrice(values.Product.price_calculated)]}</td>"+
				"</table>",					
				{
					formatPrice: function(price) {
						return Karazy.util.formatPrice(price);
					}
				}
				),
			cls: 'name',
			flex: 4
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
		console.log('CartOverviewItem --> updateRecord');
		if(!newRecord) {
			return;
		};

		var		name = this.getName();

		name.getTpl().overwrite(name.element, newRecord.getData(true));

		this.callParent([newRecord]);	
	}


})