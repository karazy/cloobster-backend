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
			//<spot-detail>
			spotDetail: {
		        selector: 'spotdetail',
		        xtype: 'spotdetail',
		        autoCreate: true
		    },
		    spotDetailCustomerList: 'spotdetail #checkInList',
		    spotDetailOrderList: 'spotdetail #spotDetailOrders',		    
		    confirmOrderButton: 'spotdetail button[action=confirm]',
		    cancelOrderButton: 'spotdetail button[action=cancel]',
		    closeSpotDetailButton: 'spotdetail button[action=close]',
		    paidSpotDetailButton: 'spotdetail button[action=paid]',
		    chargeButton: 'spotdetail button[action=pay]',		    
		    spotDetailStatistic: 'spotdetail #statistics'
		    //</spot-detail>
		},

		control : {
			spotitem: {
		 		tap:  'showSpotDetails'
		 	},
		 	spotDetailCustomerList: {
		 		select: 'showCustomerOrders'
		 	},
		 	confirmOrderButton: {
		 		tap: 'confirmOrder'
		 	},
		 	cancelOrderButton: {
		 		tap: 'cancelOrder'
		 	},
		 	closeSpotDetailButton: {
		 		tap: 'closeSpotDetail'
		 	},
		 	chargeButton: {
		 		tap: 'chargeCustomer'
		 	},
		 	spotDetail: {
		 		hide: 'hideSpotDetail'
		 	}
		},

		//the active spot, when spot detail view is visible
		activeSpot: {},
		//active customer in detail spot view
		activeCustomer: {}
	},

	init: function() {
		Ext.Logger.info('initializing Spot Controller');
		console.log('initializing Spot Controller');

		//add listeners for message events
		var messageCtr = this.getApplication().getController('Message');
		messageCtr.on('eatSense.spot', this.updateSpotIncremental, this);
	},
	/**
	*	Loads all spots and refreshes spot view.
	*	Called after a successful login or credentials restore.
	*	If spot loading fails user will be logged out
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
			 		loginCtr.logout();
			 		Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});	
	},

	//<push-message-handlers>

	/**
	*	Takes a spot and refreshes the associated item in view.
	*	
	*	@param updatedSpot
	*		A spot where only updated fields are set. (raw data)
	*/
	updateSpotIncremental: function(action, updatedSpot) {
		console.log('updateSpotIncremental');
		//load corresponding spot
		var 	dirtySpot, 
				index, 
				spotStore = this.getSpotsview().getStore();
				// spotData = updatedSpot.getData();
		
		//don't use getById, because barcode is the id
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
		}
	},
	/**
	*	Updates spotdetail view when a checkIn change at this spot occurs.
	*
	*/
	updateSpotDetailCheckInIncremental: function(action, updatedCheckIn) {
		var		me = this,
				detail = this.getSpotDetail(),
				store = this.getSpotDetailCustomerList().getStore(),
				orders = Ext.StoreManager.lookup('orderStore').getData(),
				dirtyCheckIn,
				index,
				listElement,
				updatedCheckIn = Ext.create('EatSense.model.CheckIn', updatedCheckIn);
				
		//check if spot detail is visible and if it is the same spot the checkin belongs to
		if(!detail.isHidden() && me.getActiveSpot()) {
			if(updatedCheckIn.get('spotId') == me.getActiveSpot().get('id')) {
				if(action == 'new') {
					store.add(updatedCheckIn);	
				} else if (action == 'update') {
					dirtyCheckIn = store.getById(updatedCheckIn.get('id'));
					index = store.indexOf(dirtyCheckIn);
					if(dirtyCheckIn) {
						//update existing checkin
						dirtyCheckIn.setData(updatedCheckIn.getData());

						//always refresh list to flag incoming orders on non active customers
						me.getSpotDetailCustomerList().refresh();

						if(me.getActiveCustomer().get('id') == updatedCheckIn.get('id')) {
							//update status only if this is the active customer
							me.updateCustomerStatusPanel(updatedCheckIn);
						}
												
						//BUGGY
						// listElement = me.getSpotDetailCustomerList().getAt(index);
						// listElement.getTpl().overwrite(listElement.element, dirtyCheckIn);
					} else {
						Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorGeneralCommunication'), Ext.emptyFn);
					}
				} else if (action == "delete") {
					dirtyCheckIn = store.getById(updatedCheckIn.get('id'));
					//TODO check if orders for this checkin exist? Normally this should not occur.
					if(dirtyCheckIn) {
						store.remove(dirtyCheckIn);
						//clear status panel if deleted checkin is activeCustomer
						if(updatedCheckIn.get('id') == me.getActiveCustomer().get('id')) {
							me.updateCustomerStatusPanel();
						}						
					} else { 
						console.log('delete failed: no checkin with id ' + updatedCheckIn.get('id') + ' exist');
					}
				}
			}
		}
	},
	/**
	*	Updates spotdetail view when a new/changed bill arrives.
	*
	*/
	updateSpotDetailBillIncremental: function(action, bill) {
		var		me = this,
				detail = this.getSpotDetail(),
				paymentLabel = detail.down('#paymentLabel'),
				paidButton = this.getPaidSpotDetailButton();

				//check if spot detail is visible and if it is the same spot the checkin belongs to
		if(!detail.isHidden() && me.getActiveSpot()) {
			// if(bill.checkInId == me.getActiveCustomer().get('id')) {
				paidButton.enable();
				paymentLabel.getTpl().overwrite(paymentLabel.element, {'paymentMethod' : bill.paymentMethod.name});
				paymentLabel.show();
			// }
		}
	},
	/**
	*	Updates spotdetail view when a new/changed order.
	*
	*/
	updateSpotDetailOrderIncremental: function(action, updatedOrder) {
		var		me = this,
				detail = me.getSpotDetail(),
				store = me.getSpotDetailOrderList().getStore(),
				oldOrder,
				statisticsLabel = detail.down('#total'),
				sum = 0,
				totalLabel = detail.down('#total');
		//Be careful! updatedOrder is not yet a model

		//check if spot detail is visible and if it is the same spot the checkin belongs to
		//and if the order belongs to current selected checkin		
		if(!detail.isHidden() && me.getActiveCustomer()) {
			if(updatedOrder.checkInId == me.getActiveCustomer().get('id')) {
				if(action == 'new') {
					
				} else if(action == 'update') {
					oldOrder = store.getById(updatedOrder.id);
					if(oldOrder) {
						store.remove(oldOrder);
					}
					store.add(updatedOrder);
				}

				//update total sum 
				me.updateCustomerTotal(store.getData().items);
			}
		}
	},

	//</push-message-handlers>

	/**
	*	Gets called when user taps on a spot. Shows whats going on at a particular spot.
	*   Like incoming orders, payment requests ...
	*
	*/
	showSpotDetails: function(button, eventObj, eOpts) {
		console.log('showSpotDetails');
		var		me = this,
				loginCtr = this.getApplication().getController('Login'),
				messageCtr = this.getApplication().getController('Message'),
				detail = me.getSpotDetail(),
				checkInList = detail.down('#checkInList'),
				data = button.getParent().getRecord(),
				checkInStore = Ext.StoreManager.lookup('checkInStore');

		messageCtr.on('eatSense.checkin', this.updateSpotDetailCheckInIncremental, this);
		messageCtr.on('eatSense.order', this.updateSpotDetailOrderIncremental, this);
		messageCtr.on('eatSense.bill', this.updateSpotDetailBillIncremental, this);

		//load checkins and orders and set lists
		checkInStore.load({
			params: {
				pathId: loginCtr.getAccount().get('businessId'),
				spotId: data.get('id')
			},
			 callback: function(records, operation, success) {
			 	if(success) { 		
			 		me.setActiveSpot(data);
			 		if(records.length > 0) {			 	
			 			me.getSpotDetailCustomerList().select(0);
			 		}
			 	} else {
			 		Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailCheckInLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});

		//show detail view
		Ext.Viewport.add(detail);
		detail.show();
	},
	/**
	*	Updates the status panel of selected customer in spotdetail view.
	*	@param checkIn
	*		contains the checkin information. If none provided, fields will be reseted.
	*/
	updateCustomerStatusPanel: function(checkIn) {
		var 	me = this,
				detail = me.getSpotDetail(),
				statusLabel = detail.down('#statusLabel'),
				checkInTimeLabel = detail.down('#checkInTime'),
				sum = 0;

		if(checkIn) {
			//render order status					
			statusLabel.getTpl().overwrite(statusLabel.element, checkIn.getData());
			checkInTimeLabel.getTpl().overwrite(checkInTimeLabel.element, {'checkInTime': checkIn.get('checkInTime')});
		} else {
			//pass dummy objects with no data
			statusLabel.getTpl().overwrite(statusLabel.element, {status: ''});
			checkInTimeLabel.getTpl().overwrite(checkInTimeLabel.element, {'checkInTime' : ''});
		}
	},
	/**
	*	Updates the displayed total sum of selected customer in spotdetail view.
	*	@param orders
	*		all orders for the current checkin
	*/
	updateCustomerTotal: function(orders) {
		var 	me = this,
				detail = me.getSpotDetail(),
				totalLabel = detail.down('#total'),
				sum = 0;

		if(orders) {
			//if orders exist calculate total sum 
			try {
				Ext.each(orders, function(o) {
					if(o.get('status') != Karazy.constants.Order.CANCELED) {
						sum += o.calculate();
					}					
				});
				sum = Math.round(sum*100)/100;
			} catch(e) {
				console.log('failed calculating total price ' + e);
				sum = 0
			}
			totalLabel.getTpl().overwrite(totalLabel.element, {'total': sum});	
		} else {
			totalLabel.getTpl().overwrite(totalLabel.element, {'total': '0'});
		}
	},
	/**
	*	Shows orders of a customer.
	*	Fired when customer in checkInList in spot detail view is tapped.
	*	Loads all orders based on the passed checkin.
	*
	*	@param record
	*			selected checkIn
	*
	*/
	//itemTap
	// showCustomerOrders: function(dataview, index, target, record) {
	showCustomerOrders: function(dataview, record, options) {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				orderStore = Ext.StoreManager.lookup('orderStore'),
				detail = me.getSpotDetail();
		
		if(!record) {
			return;
		}

		me.setActiveCustomer(record);

		orderStore.load({
			params: {
				pathId: loginCtr.getAccount().get('businessId'),
				checkInId: record.get('id'),
				//currently not evaluated
				// spotId: 
			},
			 callback: function(records, operation, success) {
			 	if(success) { 		
			 		this.updateCustomerStatusPanel(record);
			 		this.updateCustomerTotal(records);
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
				order = button.getParent().getRecord(),
				prevStatus = order.get('status');

		if(order.get('status') == Karazy.constants.Order.RECEIVED) {
			console.log('order already confirmed')
			//you can confirm an order only once
			return;
		};

		//update order status
		order.set('status', Karazy.constants.Order.RECEIVED);
		order.getData(true);

		//persist changes
		// order.save({
		// 	params: {
		// 		pathId: loginCtr.getAccount().get('businessId'),
		// 	},
		// 	success: function(record, operation) {
		// 		console.log('order confirmed');
		// 	},
		// 	failure: function(record, operation) {
		// 		order.set('status', Karazy.constants.Order.PLACED);
		// 		Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
		// 	}
		// });

		//same approach as in eatSense App. Magic lies in getRawJsonData()
		//still kind of a workaround
		Ext.Ajax.request({				
    	    url: Karazy.config.serviceUrl+'/b/businesses/'+loginCtr.getAccount().get('businessId')+'/orders/'+order.getId(),
    	    method: 'PUT',    	    
    	    jsonData: order.getRawJsonData(),
    	    scope: this,
    	    success: function(response) {
    	    	console.log('order confirmed');
    	    },
    	    failure: function(response) {
    	    	order.set('status', prevStatus);
				Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
	   	    }
		});
	},
	/**
	*	Marks a single order as canceled. 
	*
	*/
	cancelOrder: function(button, eventObj, eOpts) {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				orderStore = Ext.StoreManager.lookup('orderStore'),
				order = button.getParent().getRecord(),
				prevStatus = order.get('status');

		if(order.get('status') == Karazy.constants.Order.CANCELED) {
			console.log('order already canceled')
			//you can cancel an order only once
			return;
		};

		//update order status
		order.set('status', Karazy.constants.Order.CANCELED);

		//same approach as in eatSense App. Magic lies in getRawJsonData()
		//still kind of a workaround
		Ext.Ajax.request({				
    	    url: Karazy.config.serviceUrl+'/b/businesses/'+loginCtr.getAccount().get('businessId')+'/orders/'+order.getId(),
    	    method: 'PUT',    	    
    	    jsonData: order.getRawJsonData(),
    	    scope: this,
    	    success: function(response) {
    	    	console.log('order confirmed');
    	    	me.updateCustomerTotal(orderStore.getData().items);
    	    },
    	    failure: function(response) {
    	    	order.set('status', prevStatus);
				Ext.Msg.alert(i18nPlugin.translate('error'), i18nPlugin.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
	   	    }
		});
	},

	/**
	*	Close spot detail.
	*
	*/
	closeSpotDetail: function(button) {
		this.getSpotDetail().hide();
	},

	/**
	*	Called when spotdetail panel gets hidden.
	*	This is a place to cleanup the panel.
	*/
	hideSpotDetail: function(spotdetail) {
		var		messageCtr = this.getApplication().getController('Message');

		this.getSpotDetailCustomerList().deselectAll();	
		this.getSpotDetailOrderList().getStore().removeAll();
		this.updateCustomerStatusPanel();
		this.updateCustomerTotal();
		this.setActiveSpot(null);
		this.setActiveCustomer(null);

		messageCtr.un('eatSense.checkin', this.updateSpotDetailCheckInIncremental, this);
		messageCtr.un('eatSense.order', this.updateSpotDetailOrderIncremental, this);
	}

})