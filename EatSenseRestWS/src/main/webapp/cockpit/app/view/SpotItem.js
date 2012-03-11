Ext.define('EatSense.view.SpotItem', {
	extend: 'Ext.dataview.component.DataItem',
	xtype: 'spotitem',
	config: {

		spot : {
			tpl: '<div><h2>{name}</h2><p>Check in: {checkInCount}</p></div>',
			cls: 'spot-panel',
			baseCls: 'spotitem',
			pressedCls: 'spotitem-pressed'
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
		var panel = Ext.factory(config, Ext.Button, this.getSpot());
		panel.getTpl().overwrite(panel.element, this.getRecord().getData());

		if(this.getRecord().get('status') == 'ORDER_PLACED') {
			panel.addCls('spotitem-placed');
			// panel.setLabelCls('spot-placed');
		} else if(this.getRecord().get('checkInCount') >  0) {
			panel.addCls('spotitem-checkedin');
			// panel.setLabelCls('spot-checkedin');
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