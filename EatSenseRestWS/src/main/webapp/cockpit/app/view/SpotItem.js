Ext.define('EatSense.view.SpotItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotitem',
	config: {

		spot : {
			tpl: '<div><h2>{name}</h2><p>Check in: {checkInTime}</p><p>Value: {currentTotal}</p></div>',
			cls: 'spot-panel'
			// name: '',
			// status: '',
			// checkInTime: '',
			// currentTotal: ''
		},

		dataMap: {
			getSpot: {
				setData: 'name'
				// setStatus: 'status',
				// setCheckInTime: 'checkInTime',
				// setCurrenttotal: 'currentTotal'
			}
		},
		 cls: 'di-cls',
		 baseCls: 'di-baseCls',
		 listeners: {
		 	tab: function(){alert('tab')}
		 }

	},

	applySpot: function(config) {
		// Ext.Logger.log('applySpot');
		var panel = Ext.factory(config, Ext.Panel, this.getSpot());
		panel.getTpl().overwrite(panel.element, this.getRecord().getData());

		if(this.getRecord().get('status') == 'PLACED') {
			panel.addCls('spot-placed');
		} else if(this.getRecord().get('status') == 'CHECKEDIN') {
			panel.addCls('spot-checkedin');
		}

		return panel;
	},

	updateSpot: function(newSpot, oldSpot) {
		// Ext.Logger.log('updateSpot newSpot' + newSpot + ' oldSpot '+oldSpot);
		if(newSpot) {
			this.add(newSpot);
		}

		if(oldSpot) {
			this.remove(oldSpot);
		}
	}
})