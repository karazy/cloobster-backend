Ext.define('EatSense.controller.Checkin', {
	extend : 'Ext.app.Controller',
	config : {
		profile : Ext.os.deviceType.toLowerCase()
	},

	views : [ 'Main', 'Dashboard', 'MenuOverview' ],
	stores : [ 'CheckIn' ],
	refs : [ {
		ref : 'main',
		selector : 'mainview',
		xtype : 'mainview',
		autoCreate : true
	}, {
		ref : 'searchfield',
		selector : 'dashboard textfield'
	}, {
		ref : 'menuoverview',
		selector : 'menuoverview'
	} ],
	init : function() {
		console.log('initialized CheckInController');
		this.getMainView().create();
		this.control({
			'#checkInBtn' : {
				tap : this.checkInIntent
			}
		});
	},

	checkInIntent : function(options) {
		console.log("CheckIn Controller -> checkInIntent");
		var barcode = "no code";
		var that = this;
		console.log("before scanning");
		window.plugins.barcodeScanner.scan(function(result, barcode) {
			barcode = result.text;
			console.log('scanned barcode ' + barcode);
			Ext.ModelManager.getModel('EatSense.model.CheckIn').load(
					barcode,
					{
						synchronous : true,
						success : function(model) {
							console.log("CheckIn Status: "
									+ model.get('status'));
							console.log("CheckIn Restaurant: "
									+ model.get('restaurantName'));
							console.log(this);
							Ext.Msg.confirm("CheckIn", "Bei "
									+ model.get('restaurantName')
									+ " einchecken?", function(status) {
								if (status == 'yes') {
									that.checkIn({
										model : model
									});
								} else {
									that.cancelCheckIn({
										model : model
									});
								}
							});
						},
						failure : function(record, operation) {
							Ext.Msg.alert("Failed loading barcode: " + barcode,
									Ext.emptyFn);
						}
					});
		}, function(error) {
			Ext.Msg.alert("Scanning failed: " + error, Ext.emptyFn);
		});
		console.log("after scanning");
	},

	checkIn : function(options) {
		console.log("CheckIn Controller -> checkIn");
		var that = this;
		options.model.save({
			success : function(model) {
				that.showMenu();
			}
		});
	},

	cancelCheckIn : function(options) {
		console.log("CheckIn Controller -> cancelCheckIn");
		options.model.destroy();
	},

	showMenu : function() {
		console.log("CheckIn Controller -> showMenu");
		var menu = this.getMenuoverview(), main = this.getMain();
		main.setActiveItem(menu);
	}

});
