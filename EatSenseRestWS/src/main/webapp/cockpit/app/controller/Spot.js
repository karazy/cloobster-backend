/**
*	Controlls actions for the spot view.
* 	- showing and updating status for spots (tables, rooms, ...)
*	- processing incoming orders, payment requests ...
*/
Ext.define('EatSense.controller.Spot', {
	extend: 'Ext.app.Controller',
	requires: ['EatSense.view.Main'],
	config: {
		refs: {
			spotitem: 'spotitem button',
			spotsview: '#spotsview',
			spotcard: 'spotcard',
			mainview: 'main',
			info: 'toolbar[docked=bottom] #info',
			spotDetail: {
		        selector: 'spotdetail',
		        xtype: 'spotdetail',
		        autoCreate: true
		    }
		},

		control : {
			spotitem: {
		 		tap:  'showSpotDetails'
		 	}
		}
	},

	init: function() {
		Ext.Logger.info('initializing Spot Controller');
		console.log('initializing Spot Controller');
	},
	/**
	*	Loads all spots and refreshes spot view.
	*
	*/
	loadSpots: function() {
		console.log('loadSpots');
		var 	loginCtr = this.getApplication().getController('Login'),
				account = loginCtr.getAccount(),
				info = this.getInfo();

		info.getTpl().overwrite(info.element, account.data);

		this.getSpotsview().getStore().load({
			 params: {
			 	pathId : account.get('businessId'),
			 },
			 callback: function(records, operation, success) {
			 	if(success) {
			 		this.getSpotsview().refresh();	 		
			 	} else {
			 		Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});	
	},
	/**
	*	Takes a spot and refreshes the associated item in view.
	*	
	*	@param updatedSpot
	*		A spot where only updated fields are set. (raw data)
	*/
	updateSpotIncremental: function(updatedSpot) {
		console.log('updateSpotIncremental');
		//load corresponding spot
		var dirtySpot, index, spotStore = this.getSpotsview().getStore();
		
		index = spotStore.findExact('id', updatedSpot.id);

		if(index > -1) {
			dirtySpot = spotStore.getAt(index);			
			//update fields
			for(prop in updatedSpot) {
				//TODO improve?!
				if(prop && (updatedSpot[prop] || typeof updatedSpot[prop] == "number")) {
					dirtySpot.set(prop, updatedSpot[prop]);					
				}
			}

			this.getSpotsview().refresh();
		}

	},
	/**
	*	Gets called when user taps on a spot. Shows whats going on at a particular spot.
	*   Like orders, payment requests ...
	*
	*/
	showSpotDetails: function(button, eventObj, eOpts) {
		console.log('showSpotDetails');
		var		me = this,
				loginCtr = this.getApplication().getController('Login'),
				detail = me.getSpotDetail(),
				data = button.getParent().getRecord(),
				checkInStore = Ext.StoreManager.lookup('checkInStore');

		//load checkins and orders and set lists
		checkInStore.load({
			params: {
				pathId: loginCtr.getAccount().get('businessId'),
				spotId: data.get('id')
			},
			 callback: function(records, operation, success) {
			 	if(success) {
			 		this.getSpotsview().refresh();	 		
			 		detail.
			 	} else {
			 		// Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});

		//show detail view
		Ext.Viewport.add(detail);
		detail.show();
		// me.getMainview().add(detail);
	},

})