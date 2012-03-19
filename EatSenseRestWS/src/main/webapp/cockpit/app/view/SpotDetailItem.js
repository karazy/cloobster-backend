/**
*	A single order item shown in SpotDetail.
*
*/
Ext.define('EatSense.view.SpotDetailItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotdetailitem',
	config: {

		name: {

		},

		flag: true,

		confirmButton: {
			action: 'confirm'
		},

		layout: {
			type: 'hbox'
		},

		dataMap: {
	    	getName: {
	           setHtml: 'name'
	       	}
	    }

	},

	applyName: function(config) {
		// var label = Ext.factory(config, Ext.Label, this.getName());

		// button.getTpl().overwrite(button.element, this.getRecord().getData());

		// if(this.getRecord().get('status') == Karazy.constants.ORDER_PLACED || this.getRecord().get('status') == Karazy.constants.PAYMENT_REQUEST) {
		// 	button.addCls('spotitem-placed');
		// } else if(this.getRecord().get('checkInCount') >  0) {
		// 	button.addCls('spotitem-checkedin');
		// } else {
		// 	button.removeCls('spotitem-checkedin');
		// 	button.removeCls('spotitem-placed');
		// }

		// return button;

		return Ext.factory(config, Ext.Label, this.getName());
	},

	updateName: function(newName, oldName) {
		if(newName) {
			this.add(newName);
		}

		if(oldName) {
			this.remove(oldName);
		}
	}

});