Ext.define('EatSense.controller.CheckIn', {
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
				tap : this.checkIn
			}
		});
	},

	checkIn : function(options) {
		console.log('checkIn attempt');
		var barcode = "no code";
		var that = this;
		console.log("before scanning");
		window.plugins.barcodeScanner.scan(function(result, barcode) {
			barcode = result.text;
			console.log('scanned barcode ' + barcode);
			Ext.ModelManager.getModel('EatSense.model.CheckIn').load(
					barcode,
					{
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
									that.showMenu();
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

	showMenu : function() {
		console.log("CheckIn Controller -> showMenu");
		var menu = this.getMenuoverview(), main = this.getMain();
		main.setActiveItem(menu);
	}

});
