/**
 * The dashboard represents the main screen of the application. From here the
 * user can navigate, access his order history or change his settings.
 */
Ext.define('EatSense.view.Dashboard', {
	extend : 'Ext.Panel',
	xtype : 'dashboard',
	requires: ['Ext.Img'],
	config : {
		layout : {
			type : 'vbox',
			pack : 'center',
			align : 'middle',
		},
		cls: 'dashboard',
		items : [ 
		{
			xtype : 'image',
			src : 'res/images/dashboard/eatsense-logo.png',
			style : 'background-image: url(res/images/eatsense-logo.png); background-repeat:no-repeat; background-position:center center;',
			height : 100,
			width : 150
		}, 
		{
			xtype: 'image',
			style : 'background-image: url(res/images/dashboard/hup-logo.png); background-repeat:no-repeat; background-position:center center;',
			src : 'res/images/dashboard/hup-logo.png',
		},
		{
			xtype: 'image',
			style : 'background-image: url(res/images/dashboard/drei-buttons.png); background-repeat:no-repeat; background-position:center center;',
			src : 'res/images/dashboard/drei-buttons.png',
		},
		{
					xtype : 'dashboardbutton',
					action: 'checkin',
					// text : Karazy.i18n.translate('checkInButton'),
					cls : 'checkInButton',
					labelCls: 'checkInButton-label'
		}

		// {
		// 	xtype : 'panel',
		// 	layout : 'hbox',
		// 	items : [ {
		// 		xtype : 'panel',
		// 		layout : {
		// 			type : 'vbox',
		// 			align : 'middle'
		// 		},
		// 		defaults: {
		// 			xtype : 'dashboardbutton',
		// 		},
		// 		items : [ {
		// 			cls: 'newRestaurantsButton',
		// 			text: Karazy.i18n.translate('newRestaurantsButton'),
		// 			badgeText: '6',					

		// 		}, {
		// 			cls: 'currentDealsButton',	
		// 			text: Karazy.i18n.translate('currentDealsButton')
		// 		} ]
		// 	}, {
		// 		xtype : 'panel',
		// 		layout : {
		// 			type : 'vbox',
		// 			align : 'middle'
		// 		},
		// 		items : [ {
		// 			xtype : 'label',
		// 			height: '75px',
		// 			html : 'Du hast <br/><span style="font-size:1.5em;">800</span><br/>Punkte!',
		// 		}, {
		// 			xtype : 'dashboardbutton',
		// 			action: 'checkin',
		// 			text : Karazy.i18n.translate('checkInButton'),
		// 			cls : 'checkInButton',
		// 			labelCls: 'checkInButton-label'
		// 		}, 
		// 		]
		// 	} ]
		// }
		// {
		// 	xtype: 'toolbar',
		// 	docked: 'bottom',
		// 	layout: {
		// 		type:'hbox',
		// 		pack: 'center'
		// 	},
		// 	items: [{
		// 		xtype : 'button',
		// 		action : 'settings',
		// 		iconCls : 'settings',
		// 		iconMask : true,
		// 	} ]
		// }

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
