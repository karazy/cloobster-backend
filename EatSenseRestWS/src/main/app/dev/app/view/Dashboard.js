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
			xtype: 'panel',
			layout: {
				type: 'hbox',
				align: 'center',
				pack: 'center'
			},
			margin: '0 0 10 0',
			items: [
			{
				xtype : 'image',
				src : 'res/images/dashboard/hup.png',
				style : 'background-repeat:no-repeat; background-position:center center;',
				height : 56,
				width : 89,
				margin: '0 5 0 0'				
			},
			{
				xtype : 'image',
				src : 'res/images/dashboard/cloobster-logo-186.png',
				style : 'background-repeat:no-repeat; background-position:center center;',
				height : 80,
				width : 186,
				margin: '0 0 0 5'
			}
			]
		},
		// {	xtype : 'image',
		// 	src : 'res/images/dashboard/middle-v3.png',
		// 	style : 'background-repeat:no-repeat; background-position:center center;',
		// 	height : 230,
		// 	width : 277			
		// },		
		{
			xtype: 'label',
			cls: 'dashboard-description',
			style: 'text-align: center;',
			html: '<span style="font-weight: bold;">präsentieren</span><br/>"nie mehr Schlange stehen..."'
		},	
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
			html: '1. Einchecken<br/>2. Bestellen<br/>3. Genießen'
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
