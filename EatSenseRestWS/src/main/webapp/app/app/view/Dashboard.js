/**
 * The dashboard represents the main screen of the application. From here the
 * user can navigate, access his order history or change his settings.
 */
Ext.define('EatSense.view.Dashboard', {
	extend : 'Ext.Panel',
	xtype : 'dashboard',
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
					margin: 10
				},
				items : [ {
					xtype : 'button',
					// text : i18nPlugin.translate('settingsButton'),
					iconCls : 'home',
					iconMask : true,
					ui : 'action',
					width : '90px',
					height : '70px'
				}, {
					xtype : 'button',
					itemId : 'settingsBtn',
					action : 'settings',
					// text : i18nPlugin.translate('settingsButton'),
					iconCls : 'settings',
					iconMask : true,
					ui : 'action',
					width : '90px',
					height : '70px'
				} ]
			}, {
				xtype : 'panel',
				layout : {
					type : 'vbox',
					align : 'middle'
				},
				items : [ {
					xtype : 'label',
					html : 'Du hast <br/>800<br/>Punkte!'
				}, {
					xtype : 'button',
					id : 'checkInBtn',
					// text : i18nPlugin.translate('checkInButton'),
					// icon: 'res/images/checkIn_icon.png',
					// iconCls: 'checkInButtonIcon',
					cls : 'checkInButton',
				// baseCls: 'checkInButton',
				// ui : 'action',
				// height: '105px',
				// width : '150px'
				}, {
					xtype : 'textfield',
					label : i18nPlugin.translate('barcode'),
					labelAlign : 'top',
//					labelWidth : 100,
					width : 200,
					name : 'barcodeTF',
					hidden : (profile == 'phone' && window.plugins.barcodeScanner) ? true : false
				} ]
			} ]
		}

		]
	},

	showLoadScreen : function(mask) {
		if (mask) {
			this.setMasked({
				message : i18nPlugin.translate('loadingMsg'),
				xtype : 'loadmask'
			});
		} else {
			this.setMasked(false);
		}
	}
});
