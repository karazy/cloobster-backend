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
			tpl: 
				"<h2>{Product.name}</h2>" +
				//"+i18nPlugin.translate('amount')+"
				"<div class='amount'>({amount})</div>" + 
				"<div class='price'>{Product.price_calculated}€</div>"+
				"<div style='clear: both;'>",
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
		 // baseCls: 'di-baseCls'
		layout: {
			type: 'hbox',
			pack: 'center',
			align: 'start'
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