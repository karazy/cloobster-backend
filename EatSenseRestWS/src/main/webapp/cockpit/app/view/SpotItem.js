/**
*	A single spot in spot view.
* 	Displays spot name and number of checkIns.
*/
Ext.define('EatSense.view.SpotItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotitem',
	config: {

		spot : {
			tpl: '<div><h2>{name}</h2><p>Check-ins: {checkInCount}</p></div>',
			cls: 'spot-button',
			baseCls: 'spotitem',
			pressedCls: 'spotitem-pressed'
		},

		 cls: 'di-cls',
		 baseCls: 'di-baseCls'
	},

	applySpot: function(config) {
		var button = Ext.factory(config, Ext.Button, this.getSpot());
		button.getTpl().overwrite(button.element, this.getRecord().getData());

		if(this.getRecord().get('status') == Karazy.constants.ORDER_PLACED || this.getRecord().get('status') == Karazy.constants.PAYMENT_REQUEST) {
			button.addCls('spotitem-placed');
		} else if(this.getRecord().get('checkInCount') >  0) {
			button.addCls('spotitem-checkedin');
		} else {
			button.removeCls('spotitem-checkedin');
			button.removeCls('spotitem-placed');
		}

		return button;
	},

	updateSpot: function(newSpot, oldSpot) {
		if(newSpot) {
			this.add(newSpot);
		}

		if(oldSpot) {
			this.remove(oldSpot);
		}
	},

	updateRecord: function(newRecord) {
		if(!newRecord) {
			return;
		};
		
		var button = this.getSpot();
			if(this.getSpot()) {						
				if(newRecord.get('status') == Karazy.constants.ORDER_PLACED  || newRecord.get('status') == Karazy.constants.PAYMENT_REQUEST) {
					button.addCls('spotitem-placed');
					button.removeCls('spotitem-checkedin');
				} else if(newRecord.get('checkInCount') >  0) {
					button.addCls('spotitem-checkedin');
					button.removeCls('spotitem-placed');
				}  else {
					button.removeCls('spotitem-checkedin');
					button.removeCls('spotitem-placed');
				}
				
				button.getTpl().overwrite(this.getSpot().element, newRecord.getData());
		}		
	}


})