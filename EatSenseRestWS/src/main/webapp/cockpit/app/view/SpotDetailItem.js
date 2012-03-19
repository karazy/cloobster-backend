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

		//name of ordered product
		name: {
			// flex: 1
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
		
		this.getName().setHtml(newRecord.raw.product.name);

		if(newRecord.get('status') == Karazy.constants.Order.PLACED) {
			this.getFlag().setHtml(Karazy.i18n.translate('PLACED'));
			this.getFlag().show();
		} else {
			this.getFlag().hide();
		}

		//overrides the default updateRecord, perhabs we remove this completely
		//this.callParent([newRecord]);
	}

});