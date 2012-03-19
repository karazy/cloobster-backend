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
			//<spot detail>
			spotDetail: {
		        selector: 'spotdetail',
		        xtype: 'spotdetail',
		        autoCreate: true
		    },

		    spotDetailCustomerList: 'spotdetail #checkInList',
		    spotDetailOrderList: 'spotdetail #spotDetailOrders',
		    confirmOrderButton: 'spotdetail button[action=confirm]',
		    chargeButton: 'spotdetail button[action=pay]'		    

		    //</spot-detail>
		},

		control : {
			spotitem: {
		 		tap:  'showSpotDetails'
		 	},
		 	spotDetailCustomerList: {
		 		itemtap: 'showCustomerOrders'
		 	},
		 	confirmOrderButton: {
		 		tap: 'confirmOrder'
		 	},
		 	chargeButton: {
		 		tap: 'chargeCustomer'
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
			 		//explicit refresh should not be necessary
			 		//this.getSpotsview().refresh();	 		
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
			//explicit refresh should not be necessary
			// this.getSpotsview().refresh();
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
				checkInList = detail.down('#checkInList'),
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
			 		// checkInList.refresh();
			 	} else {
			 		Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailCheckInLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});

		//show detail view
		Ext.Viewport.add(detail);
		detail.show();
		// me.getMainview().add(detail);
	},
	/**
	*	Shows orders of a customer.
	*	Fired when customer in checkInList in spot detail view is tapped.
	*/
	showCustomerOrders: function(dataview, index, target, record) {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				orderStore = Ext.StoreManager.lookup('orderStore');


		orderStore.load({
			params: {
				pathId: loginCtr.getAccount().get('businessId'),
				checkInId: record.get('id'),
				//currently not evaluated
				// spotId: 
			},
			 callback: function(records, operation, success) {
			 	if(success) { 		
			 		// checkInList.refresh();
			 		// me.getSpotDetailOrderList().refresh();
			 	} else {
			 		Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailOrderLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});

	},
	/**
	*	Marks a single order as confirmed. This indicates that the business received 
	*	the order and starts to process it.
	*
	*/
	confirmOrder: function(button, eventObj, eOpts) {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				orderStore = Ext.StoreManager.lookup('orderStore'),
				order = button.getParent().getRecord();

		if(order.get('status') == Karazy.constants.Order.RECEIVED) {
			console.log('order already confirmed')
			//you can confirm an order only once
			return;
		};

		//update order status
		order.set('status', Karazy.constants.Order.RECEIVED);

		//persist changes
		order.save({
			params: {
				pathId: loginCtr.getAccount().get('businessId'),
			},
			succes: function(record, operation) {
				console.log('order saved');
			},
			failure: function(record, operation) {
				Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
			}

		});
	}

})