Ext.define('EatSense.view.SpotItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotitem',
	config: {

		spot : {
			tpl: '<div><h2>{name}</h2><p>Check-ins: {checkInCount}</p></div>',
			cls: 'spot-button',
			baseCls: 'spotitem',
			pressedCls: 'spotitem-pressed'
			// name: '',
			// status: '',
			// checkInTime: '',
			// currentTotal: ''
		},

		dataMap: {
			getSpot: {
				// setData: 'name'
				// setStatus: 'status',
				// setCheckInTime: 'checkInTime',
				// setCurrenttotal: 'currentTotal'
			}
		},
		 cls: 'di-cls',
		 baseCls: 'di-baseCls',
		 listeners: {
		 	updatedata: function(di, newdata, eOpts){
		 		console.log('spotitem updatedata');
		 		//TODO remove
		 	}
		 }

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
					
			if(newRecord.get('status') == 'ORDER_PLACED') {
				button.addCls('spotitem-placed');
			} else if(newRecord.get('checkInCount') >  0) {
				button.addCls('spotitem-checkedin');
			}  else {
				button.removeCls('spotitem-checkedin');
				button.removeCls('spotitem-placed');
			}
			
			button.getTpl().overwrite(this.getSpot().element, newRecord.getData());

		}		
	}


})