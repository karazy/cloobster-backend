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
			src : 'res/images/dashboard/cloobster-logo-186.png',
			style : 'background-image: url(res/images/dashboard/cloobster-logo-186.png); background-repeat:no-repeat; background-position:center center;',
			height : 80,
			width : 186
		}, 
		
		{	xtype : 'image',
			src : 'res/images/dashboard/middle-v2.png',
			style : 'background-image: url(res/images/dashboard/middle-v2.png); background-repeat:no-repeat; background-position:center center;',
			height : 230,
			width : 277
			
			},
			
		{ 	xtype : 'dashboardbutton',
			action: 'checkin',
			src : 'res/images/dashboard/start_button.png',
			style : 'background-image: url(res/images/dashboard/start_button.png); background-repeat:no-repeat; background-position:center center;',
			height : 110,
			width : 186
			
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
