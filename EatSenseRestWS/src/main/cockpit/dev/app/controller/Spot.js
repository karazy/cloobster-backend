/**
*	Controlls actions for the spot view.
* 	- showing and updating status for spots (tables, rooms, ...)
*	- processing incoming orders, payment requests ...
*/
Ext.define('EatSense.controller.Spot', {
	extend: 'Ext.app.Controller',
	requires: ['EatSense.view.Main', 'EatSense.view.SpotSelectionDialog', 'EatSense.view.CustomerRequestDialog'],
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
		    cancelAllButton: 'spotdetail button[action=cancel-all]',    
		    switchSpotButton: 'spotdetail button[action=switch-spot]', 
		    spotDetailStatistic: 'spotdetail #statistics',
		    spotSelectionDialog: {
		    	selector: 'spotselection',
		    	xtype: 'spotselection',
		    	autoCreate: true
		    },
		    switchSpotList: 'spotselection list',
		    //</spot-detail>
		},

		control : {
			spotitem: {
		 		tap:  'showSpotDetails'
		 	},
		 	spotDetailCustomerList: {
		 		select: 'showCustomerDetail'
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
		 	spotDetail: {
		 		hide: 'hideSpotDetail'
		 	},
		 	paidSpotDetailButton: {
		 		tap: 'confirmPayment'
		 	},
		 	cancelAllButton: {
		 		tap: 'cancelAll'
		 	},
		 	switchSpotButton: {
		 		tap: 'showSpotSelection'
		 	}, 
		 	switchSpotList: {
		 		select: 'switchSpot'
		 	},
		 	dismissRequestsButton : {
		 		tap: 'deleteCustomerRequests'
		 	}
		},

		//the active spot, when spot detail view is visible
		activeSpot: null,
		//active customer in detail spot view
		activeCustomer: null,
		//active Bill of active Customer
		activeBill : null
	},

	init: function() {
		console.log('initializing Spot Controller');

		//add listeners for message events
		var messageCtr = this.getApplication().getController('Message');
		messageCtr.on('eatSense.spot', this.updateSpotIncremental, this);
	},

	// <LOAD AND SHOW DATA>
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
			 	if(!success) {
			 		loginCtr.logout();
			 		Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});	
	},

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
				requestCtr = this.getApplication().getController('Request'),
				detail = me.getSpotDetail(),
				checkInList = detail.down('#checkInList'),
				data = button.getParent().getRecord(),
				checkInStore = Ext.StoreManager.lookup('checkInStore'),
				restaurantId = loginCtr.getAccount().get('businessId'),
				titlebar = detail.down('titlebar'),
				requestStore = Ext.StoreManager.lookup('requestStore');

		//add listeners for channel messages
		messageCtr.on('eatSense.checkin', this.updateSpotDetailCheckInIncremental, this);
		messageCtr.on('eatSense.order', this.updateSpotDetailOrderIncremental, this);
		messageCtr.on('eatSense.bill', this.updateSpotDetailBillIncremental, this);
		messageCtr.on('eatSense.request', requestCtr.processCustomerRequest, requestCtr);


		//load checkins and orders and set lists
		checkInStore.load({
			params: {
				pathId: restaurantId,
				spotId: data.get('id')
			},
			 callback: function(records, operation, success) {
			 	if(success) { 		
			 		me.setActiveSpot(data);
			 		titlebar.setTitle(data.get('name'));
			 		requestCtr.loadRequests();
			 		if(records.length > 0) {			 			
			 			//selects the first customer. select event of list gets fired and calls showCustomerDetail	 	
			 			me.getSpotDetailCustomerList().select(0);
			 		}
			 	} else {
			 		Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotDetailCheckInLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});


		//show detail view
		Ext.Viewport.add(detail);
		detail.show();
	},

	/**
	*	Shows details (orders, bills, requests) of a customer.
	*	Fired when customer in checkInList in spot detail view is tapped.
	*	Loads all orders based on the passed checkin.
	*
	*	@param record
	*			selected checkIn
	*
	*/
	showCustomerDetail: function(dataview, record, options) {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				orderStore = Ext.StoreManager.lookup('orderStore'),
				billStore = Ext.StoreManager.lookup('billStore'),				
				detail = me.getSpotDetail(),
				restaurantId = loginCtr.getAccount().get('businessId'),
				bill,
				paidButton = this.getPaidSpotDetailButton();
		
		if(!record) {
			return;
		}

		me.setActiveCustomer(record);
		me.getSpotDetail().fireEvent('eatSense.customer-update', true);

		orderStore.load({
			params: {
				pathId: restaurantId,
				checkInId: record.get('id'),
				//currently not evaluated
				// spotId: 
			},
			 callback: function(records, operation, success) {
			 	if(success) { 		
			 		this.updateCustomerStatusPanel(record);
			 		this.updateCustomerTotal(records);
			 	} else {
			 		Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotDetailOrderLoading'), Ext.emptyFn);
			 	}				
			 },
			 scope: this
		});

		if(me.getActiveCustomer().get('status') == Karazy.constants.PAYMENT_REQUEST) {
			paidButton.enable();
			billStore.load({
				params: {
					pathId: restaurantId,
					checkInId: record.get('id'),
				},
				 callback: function(records, operation, success) {
				 	if(success && records.length == 1) { 
				 		me.setActiveBill(records[0]);
				 		me.updateCustomerPaymentMethod(records[0].getPaymentMethod().get('name'));
				 	} else {
				 		console.log('could not load bill');
			    		me.updateCustomerPaymentMethod();
				 	}				
				 },
				 scope: this
			});
		} else {
			//make sure to hide payment method label
			me.updateCustomerPaymentMethod();
			paidButton.disable();
		}
	},

	// </LOAD AND SHOW DATA>

	//<PUSH MESSAGE HANDLERS>

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

			if(updatedSpot.status) {
				dirtySpot.set('status', updatedSpot.status);
			}

			if(updatedSpot.checkInCount || typeof updatedSpot.checkInCount == "number") {
				dirtySpot.set('checkInCount', updatedSpot.checkInCount);
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
				orders = Ext.StoreManager.lookup('orderStore'),
				customerList = this.getSpotDetailCustomerList(),
				dirtyCheckIn,
				index,
				listElement,
				updatedCheckIn = Ext.create('EatSense.model.CheckIn', updatedCheckIn),
				requestCtr = this.getApplication().getController('Request'),
				customerIndex;
				
		//check if spot detail is visible and if it is the same spot the checkin belongs to
		if(!detail.isHidden() && me.getActiveSpot()) {
			if(updatedCheckIn.get('spotId') == me.getActiveSpot().get('id')) {
				if(action == 'new') {
					store.add(updatedCheckIn);
					if(store.getCount() == 1) {
						//only one checkIn exists so set this checkIn as selected
						customerList.select(0);
					}
					//make sure to load new request so they exist
					requestCtr.loadRequests();
				} else if (action == 'update') {
					dirtyCheckIn = store.getById(updatedCheckIn.get('id'));
					if(dirtyCheckIn) {
						//update existing checkin
						dirtyCheckIn.setData(updatedCheckIn.getData());

						//always refresh list to flag incoming orders on non active customers
						me.getSpotDetailCustomerList().refresh();

						if(me.getActiveCustomer() && me.getActiveCustomer().get('id') == updatedCheckIn.get('id')) {
							//update status only if this is the active customer
							me.updateCustomerStatusPanel(updatedCheckIn);
						}
					} else {
						Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorGeneralCommunication'), Ext.emptyFn);
					}
				} else if (action == "delete") {
					dirtyCheckIn = store.getById(updatedCheckIn.get('id'));
					if(dirtyCheckIn) {
						customerIndex = store.indexOf(dirtyCheckIn);
						store.remove(dirtyCheckIn);	
						//make sure to load new request so they exist
						requestCtr.loadRequests();					

						//clear status panel if deleted checkin is activeCustomer or select another checkin
						if(me.getActiveCustomer() && updatedCheckIn.get('id') == me.getActiveCustomer().get('id')) {
							if(store.getCount() > 0) {
								if(store.getAt(customerIndex)) {
									customerList.select(customerIndex);	
								} else {
									customerList.select(customerIndex-1);
								}
								
							} else {
								me.getSpotDetail().fireEvent('eatSense.customer-update', false);
								orders.removeAll();
								me.setActiveCustomer(null);
								me.updateCustomerStatusPanel();
								me.updateCustomerTotal();
								me.updateCustomerPaymentMethod();
							}
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
	updateSpotDetailBillIncremental: function(action, billData) {
		var		me = this,
				detail = this.getSpotDetail(),
				paymentLabel = detail.down('#paymentLabel'),
				paidButton = this.getPaidSpotDetailButton(),
				bill;

				//check if spot detail is visible and if it is the same spot the checkin belongs to
		if(!detail.isHidden() && me.getActiveSpot()) {
			if(me.getActiveCustomer() && billData.checkInId == me.getActiveCustomer().get('id')) {
				bill = Ext.create('EatSense.model.Bill');
				bill.setData(billData);
				bill.setId(billData.id);
				//this is an already persistent object!
				bill.phantom = false;

				if(action == 'new') {
					this.setActiveBill(bill);
					paidButton.enable();
					me.updateSpotDetailBillIncremental(bill.getPaymentMethod());
					// paymentLabel.getTpl().overwrite(paymentLabel.element, {'paymentMethod' : bill.getPaymentMethod().get('name')});
					// paymentLabel.show();
				} else if (action == 'update') {
					//currently no action needed. update occurs when a bill is cleared
					//since we also receive a checkin delete method no further action required
				}
			}
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

	// </PUSH MESSAGE HANDLERS>

	//<VIEW UPDATE METHODS>

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
				sum = Karazy.util.roundPrice(sum);
			} catch(e) {
				console.log('failed calculating total price ' + e);
				sum = '0.00€'
			}
			totalLabel.getTpl().overwrite(totalLabel.element, {'total': sum});	
		} else {
			totalLabel.getTpl().overwrite(totalLabel.element, {'total': '0.00€'});
		}
	},
	/**
	*	Displays the chose paymentMethod when a payment request is active.
	*	@param paymentMethod
	* 		if empty hides the payment label, otherwise shows the paymentMethod
	*/
	updateCustomerPaymentMethod: function(paymentMethod) {
		var		me = this,
				detail = this.getSpotDetail(),
				paymentLabel = detail.down('#paymentLabel');

				//check if spot detail is visible and if it is the same spot the checkin belongs to
		if(paymentMethod) {
			paymentLabel.getTpl().overwrite(paymentLabel.element, {'paymentMethod' : paymentMethod});
			paymentLabel.show();
		} else {
			paymentLabel.getTpl().overwrite(paymentLabel.element, {'paymentMethod' : ''});
			paymentLabel.hide();
		}
	},
	//</VIEW UPDATE METHODS>

	// <ACTIONS>

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
		// 		Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
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
				Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
	   	    }
		});
	},
	/**
	* Marks a bill as paid.
	*
	*/
	confirmPayment: function(button, eventObj, eOpts){
		var 	me = this,
				orderStore = Ext.StoreManager.lookup('orderStore'),
				customerStore = Ext.StoreManager.lookup('checkInStore'),
				unprocessedOrders,
				loginCtr = this.getApplication().getController('Login'),
				bill = this.getActiveBill(),
				customerList = this.getSpotDetailCustomerList(),
				customerIndex;

		if(!bill) {
			console.log('cannot confirm payment because no bill exists');
			return;
		}

		//check if all orders are processed
		unprocessedOrders = orderStore.queryBy(function(record, id) {
			if(record.get('status') == Karazy.constants.Order.PLACED) {
				return true;
			}

		});

		if(unprocessedOrders.getCount() > 0 ) {
			Ext.Msg.alert(Karazy.i18n.translate('hint'), Karazy.i18n.translate('processOrdersFirst'), Ext.emptyFn);
		} else {
			bill.set('cleared', true);
			bill.save({
				params: {
					pathId: loginCtr.getAccount().get('businessId')
				},
				success: function(record, operation) {
					//remove customer
					customerIndex = customerStore.indexOf(me.getActiveCustomer());
					customerStore.remove(me.getActiveCustomer());
					if(customerStore.getCount() > 0) {
						customerList.select(customerIndex);
					} else {
						orderStore.removeAll();
						me.updateCustomerStatusPanel();
						me.updateCustomerTotal();
						me.updateCustomerPaymentMethod();
					}
					me.setActiveBill(null);		
				},
				failure: function(record, operation) {
					console.log('saving bill failed');
					button.enable();
				}
			});			
		}

	},
	/**
	*	Marks a single order as canceled. 
	*
	*/
	cancelOrder: function(button, event, eOpts) {
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
				Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorSpotDetailOrderSave'), Ext.emptyFn);
	   	    }
		});
	},
	/**
	*	Cancels all orders and removes customer from spot.
	*/
	cancelAll: function(button, event) {
		var 	me = this,
				loginCtr = this.getApplication().getController('Login'),
				orders = Ext.StoreManager.lookup('orderStore'),
				checkins = Ext.StoreManager.lookup('checkInStore'),
				customerList = this.getSpotDetailCustomerList(),
				prevStatus,
				customerIndex;

		if(!this.getActiveCustomer()) {
			return;
		}

		Ext.Msg.show({
			title: Karazy.i18n.translate('hint'),
			message: Karazy.i18n.translate('cancelAllOrders'),
			buttons: [{
				text: 'Ja',
				itemId: 'yes',
				ui: 'action'
			}, {
				text: 'Nein',
				itemId: 'no',
				ui: 'action'
			}],
			scope: this,
			fn: function(btnId, value, opt) {
				if(btnId=='yes') {
					customerIndex = checkins.indexOf(me.getActiveCustomer());

					if(customerIndex < 0) {
						return;
					}

					me.getActiveCustomer().erase({
						callback: function(records, operation) {
							if(!operation.success) {
								Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('errorMsg'), Ext.emptyFn);
							} else {
								//although a message will be received we update the view directly
								checkins.remove(me.getActiveCustomer());

								if(checkins.getCount() > 0) {
									if(checkins.getAt(customerIndex)) {
										customerList.select(customerIndex);	
									} else {
										customerList.select(customerIndex-1);
									}								
								} else {
									orders.removeAll();
									me.setActiveCustomer(null);
									me.updateCustomerStatusPanel();
									me.updateCustomerTotal();
									me.updateCustomerPaymentMethod();
									me.getSpotDetail().fireEvent('eatSense.customer-update', false);
								}
							}					
						}
					});
				}
			}
		});		

	},
	/**
	*	Shows a list of all available spots.
	*	A select triggers switchSpot.
	*/
	showSpotSelection: function(button, event) {
		var 	me = this,
				spotStore = Ext.StoreManager.lookup('spotStore'),
				spotSelectionDlg = this.getSpotSelectionDialog(),
				spotList = this.getSwitchSpotList(),
				filteredSpots;

		//don't show active spot
		// filteredSpots = spotStore.queryBy(function(record, id) {
		// 	if(record.getId() != me.getActiveSpot().getId()) {
		// 		return true;
		// 	}
		// });
		// spotList.setData(filteredSpots);

		// filteredSpots.each(function(spot) {
		// 	spotList.add(spot);	
		// });
		spotSelectionDlg.showBy(button, 'br-tc?');
	},
	/**
	*	Switch user to another table.	
	*
	*/
	switchSpot: function(list, record, options) {
		var 	me = this,
				activeCustomer = this.getActiveCustomer(),
				loginCtr = this.getApplication().getController('Login'),
				requestCtr = this.getApplication().getController('Request');

		if(activeCustomer) {
			//set new spot id
			activeCustomer.set('spotId', record.get('id'));
			activeCustomer.save({
				params: {
					pathId: loginCtr.getAccount().get('businessId')
				},
				success: function(record, operation) {
					//TODO refactor!
					requestCtr.loadRequests();
				},
				failure: function(record, operation) { 
					Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('switchSpotError'));
				}
			})
		}

		list.getParent().hide();
		//prevent list selection
		return false;
	},
	// </ACTIONS>

	// <MISC VIEW ACTIONS>
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
		var		messageCtr = this.getApplication().getController('Message'),
				requestCtr = this.getApplication().getController('Request'),
				requestStore = Ext.StoreManager.lookup('requestStore');

		this.getSpotDetailCustomerList().deselectAll();	
		this.getSpotDetailOrderList().getStore().removeAll();
		requestStore.removeAll(true);
		this.updateCustomerStatusPanel();
		this.updateCustomerTotal();
		this.setActiveSpot(null);
		this.setActiveCustomer(null);
		this.setActiveBill(null);

		messageCtr.un('eatSense.checkin', this.updateSpotDetailCheckInIncremental, this);
		messageCtr.un('eatSense.order', this.updateSpotDetailOrderIncremental, this);
		messageCtr.un('eatSense.request', requestCtr.updateSpotDetailOrderIncremental, requestCtr);
	},

	// setActiveCustomerWrapper: function(customer) {
	// 	var 	me = this;

	// 	if(customer) {

	// 	} else {
	// 		me.setActiveCustomer(null);
	// 	}

	// 	me.fireEv
	// }

	// </MISC VIEW ACTIONS>

})