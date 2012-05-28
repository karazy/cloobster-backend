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
			align : 'center',
		},
		cls: 'dashboard',
		items : [ 
		{
			xtype : 'image',
			src : 'res/images/dashboard/cloobster-logo-186.png',
			style : 'background-repeat:no-repeat; background-position:center center;',
			height : 80,
			width : 186
		},
		// {	xtype : 'image',
		// 	src : 'res/images/dashboard/middle-v3.png',
		// 	style : 'background-repeat:no-repeat; background-position:center center;',
		// 	height : 230,
		// 	width : 277			
		// },			
		{
			xtype : 'button',
			action: 'checkin',
			margin: '20 0',
			height : 110,
			width : 186,
			baseCls : 'dashboard-button',
			pressedCls: 'dashboard-button-pressed'
		},
		{
			xtype: 'label',
			cls: 'dashboard-description',
			html: '1. Check-In<br/>2. Bestellen<br/>3. Genießen'
		},
		{
			xtype: 'button',
			action: 'about',
			ui: 'confirm',
			iconCls: 'about',
			iconMask: true,
			styleHtmlContent: true,
			style: 'position: absolute; bottom: 10px; right: 10px;'
		},
		{
			xtype: 'image',
			src: 'res/images/dashboard/start_button_touch.png',
			hidden: true
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
