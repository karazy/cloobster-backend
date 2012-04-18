/**
 * The dashboard represents the main screen of the application. From here the
 * user can navigate, access his order history or change his settings.
 */
Ext.define('EatSense.view.Dashboard', {
	extend : 'Ext.Panel',
	xtype : 'dashboard',
	requires: ['Ext.Img'],
	config : {
		style : 'background-color: white;',
		layout : {
			type : 'vbox',
			pack : 'center',
			align : 'middle',
		},
		items : [ {
			xtype : 'image',
			src : 'res/images/eatSenseLogo_small.png',
			style : 'background-image: url(res/images/eatSenseLogo_small.png); background-repeat:no-repeat; background-position:center center;',
			height : 100,
			width : 150
		}, {
			xtype : 'panel',
			layout : 'hbox',
			items : [ {
				xtype : 'panel',
				layout : {
					type : 'vbox',
					align : 'middle'
				},
				defaults: {
					xtype : 'dashboardbutton',
				},
				items : [ {
					cls: 'newRestaurantsButton',
					text: Karazy.i18n.translate('newRestaurantsButton'),
					badgeText: '6',					

				}, {
					cls: 'currentDealsButton',	
					text: Karazy.i18n.translate('currentDealsButton')
				} ]
			}, {
				xtype : 'panel',
				layout : {
					type : 'vbox',
					align : 'middle'
				},
				items : [ {
					xtype : 'label',
					height: '75px',
					html : 'Du hast <br/><span style="font-size:1.5em;">800</span><br/>Punkte!',
				}, {
					xtype : 'dashboardbutton',
					id : 'checkInBtn',
					 text : Karazy.i18n.translate('checkInButton'),
					// icon: 'res/images/checkIn_icon.png',
//					 iconCls: 'checkInButtonIcon',
					cls : 'checkInButton',
					labelCls: 'checkInButton-label'
					
				// baseCls: 'checkInButton',
				// ui : 'action',
				// height: '105px',
				// width : '150px'
				}, 
				// {
				// 	xtype : 'textfield',
				// 	label : Karazy.i18n.translate('barcode'),
				// 	labelAlign : 'top',
				// 	width : 100,
				// 	name : 'barcodeTF',
				// 	hidden : (profile == 'phone' && window.plugins && window.plugins.barcodeScanner) ? true : false
				// } 
				]
			} ]
		},
		{
			xtype: 'toolbar',
			docked: 'bottom',
			layout: {
				type:'hbox',
				pack: 'center'
			},
			items: [{
				xtype : 'button',
				itemId : 'settingsBtn',
				action : 'settings',
				// text : Karazy.i18n.translate('settingsButton'),
				iconCls : 'settings',
				iconMask : true,
//				ui : 'action',
//				width : '90px',
//				height : '70px'
			} ]
		}

		]
	},

	showLoadScreen : function(mask) {
		if (mask) {
			this.setMasked({
				message : Karazy.i18n.translate('loadingMsg'),
				xtype : 'loadmask'
			});
		} else {
			this.setMasked(false);
		}
	}
});
