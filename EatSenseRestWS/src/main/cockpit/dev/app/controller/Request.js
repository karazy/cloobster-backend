/**
*	Handles customer requests like "Call Waiter".
*	
*/
Ext.define('EatSense.controller.Request',{
	extend: 'Ext.app.Controller',
	config: {
		refs: {
			spotDetail : 'spotdetail',
			customerRequestDialog: {
		    	selector: 'customerrequest',
		    	xtype: 'customerrequest',
		    	// autoCreate: true
		    },
			dismissRequestsButton: 'customerrequest button[action=dismiss]',
			customerRequestList: 'customerrequest list'
		},
		control: {
			dismissRequestsButton : {
		 		tap: 'dismissCustomerRequests'
		 	},
		 	customerRequestList: {
		 		itemswipe : 'dissmissCustomerRequest',
		 		itemtap : 'dissmissCustomerRequest',
		 	}
		},	
	},
	/**
	*	Loads all customer request for the active spot.
	*
	*/
	loadRequests: function() {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				spotCtr = this.getApplication().getController('Spot'),
				restaurantId = loginCtr.getAccount().get('businessId'),
				requestStore = Ext.StoreManager.lookup('requestStore'),
				checkInStore = Ext.StoreManager.lookup('checkInStore'),
				assocCheckIn;

		//empty the store
		requestStore.removeAll();

		//check if customer requests for this spot exist and display them
		requestStore.load({
			params: {
				'pathId': restaurantId,
				'spotId': spotCtr.getActiveSpot().get('id')
			},
			 callback: function(records, operation, success) {
			 	if(success) { 
			 		if(records.length > 0) {
			 			Ext.Array.each(records, function(request) {
			 				assocCheckIn = checkInStore.getById(request.get('checkInId'));
			 				request.setCheckIn(assocCheckIn);
			 				//make sure data is uptodate
			 				request.getData(true);
			 			});
			 			me.showCustomerRequestMessages(records);
			 		}		
			 	} else {
			 		me.getApplication().handleServerError({
						'error': operation.error, 
						'forceLogout': {403: true}
					});
			 	}			
			 }
		});
	},

	/**
	*	Processes the given request message and shows it when the request belongs to active spot.
	*	@param action
	*
	*	@param request
	*/
	processCustomerRequest: function(action, request) {
		var 	me = this,
				detail = me.getSpotDetail(),
				requestModel = Ext.create('EatSense.model.Request'),
				requestStore = Ext.StoreManager.lookup('requestStore'),
				spotCtr = this.getApplication().getController('Spot'),
				checkInStore = Ext.StoreManager.lookup('checkInStore'),
				assocCheckIn,
				oldRequest;

		requestModel.setData(request);
		//this is no new object!
		requestModel.phantom = false;
		assocCheckIn = checkInStore.getById(requestModel.get('checkInId'));
		requestModel.setCheckIn(assocCheckIn);

		if(!detail.isHidden() && spotCtr.getActiveCustomer()) {
			//only show if the correct spot is active
			if(request.spotId == spotCtr.getActiveSpot().get('id')) {
				if(action == 'new') {
					requestStore.add(requestModel);
					me.showCustomerRequestMessages(requestModel);
				} else if(action == 'delete') {
					oldRequest = requestStore.getById(requestModel.get('id'));
					if(oldRequest) {
						requestStore.remove(oldRequest);
					}					
				}
			}
		}
	},

	/**
	*	Displays the given requests in a popup message.
	*	
	*/
	showCustomerRequestMessages: function(requests) {
		var 	dialog = this.getCustomerRequestDialog();

		//show detail view
		this.getCustomerRequestList().refresh();
	},

	/**
	* Deletes all customer requests from store and sends delete requests to server.
	*/
	dismissCustomerRequests: function() {
		var 	requestStore = Ext.StoreManager.lookup('requestStore');

		requestStore.setSyncRemovedRecords(true);
		requestStore.removeAll();
		requestStore.sync();
		requestStore.setSyncRemovedRecords(false);

		// this.getCustomerRequestDialog().hide();
	},
	/**
	*	Deletes a single customer request.
	*
	*/
	dissmissCustomerRequest: function(dv, index, target, record) {
		var 	requestStore = Ext.StoreManager.lookup('requestStore');

		requestStore.setSyncRemovedRecords(true);
		requestStore.remove(record);
		requestStore.sync();
		requestStore.setSyncRemovedRecords(false);
	},
	/**
	* Removes all existing requests which belong to given customer.
	*
	*/
	removeRequestsForCustomerById: function(checkIn) {
		var me = this,
			requestStore = Ext.StoreManager.lookup('requestStore'),
			filtered;

		if(!checkIn) {
			console.log('no checkin provided');
			return;
		}

		console.log('removeRequestsForCustomerById for %s', checkIn.getId());

		filtered = requestStore.queryBy(function(request){
			if(request.getCheckIn() == checkIn) {
				return true;
			}
		});

		filtered.each(function(requestToRemove) {
			requestStore.remove(requestToRemove);
		});

	}

});