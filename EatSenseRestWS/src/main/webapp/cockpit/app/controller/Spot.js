Ext.define('EatSense.controller.Spot', {
	extend: 'Ext.app.Controller',
	requires: ['EatSense.view.Main'],
	config: {
		refs: {
			spotitem: 'spotitem button',
			spotsview: '#spotsview'
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
		var loginCtr = this.getApplication().getController('Login'),
		account = loginCtr.getAccount();

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
	*		A spot where only updated fields are set
	*/
	updateSpotIncremental: function(updatedSpot) {
		console.log('updateSpotIncremental');
		//load corresponding spot
		var dirtySpot, index, spotStore = this.getSpotsview().getStore();
		
		//TODO decode spot?

		index = spotStore.findExact('id', updatedSpot.get('id'));

		if(index > -1) {
			dirtySpot = spotStore.getAt(index);			
			//update fields
			for(prop in updatedSpot) {
				if(prop) {
					dirtySpot.set(prop, updatedSpot[prop]);					
				}
			}
			//test alternative
			// dirtySpot.mergeData(updatedSpot);
			// dirtySpot.setData(updatedSpot);
		}

	},
	/**
	*	Gets called when user taps on a spot. Shows whats going on at a particular spot.
	*   Like orders, payment requests ...
	*
	*/
	showSpotDetails: function(button, eventObj, eOpts) {
		console.log('showSpotDetails for ');
	},

})