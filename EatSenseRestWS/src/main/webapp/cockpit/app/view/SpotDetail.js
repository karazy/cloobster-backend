Ext.define('EatSense.view.SpotDetail', {
	extend: 'Ext.Panel',
	xtype: 'spotdetail',
	requires: ['EatSense.view.SpotDetailItem'],
	config: {
		modal: true,
		hideOnMaskTap: 'true',
		baseCls: 'spotdetail',
		top: '10%',
		left: '10%',
		right: '10%',
		bottom: '10%',
		layout: 'fit',
		fullscreen: true,
		//this should be initially hidden
		hidden: true,
		items: [{
			xtype: 'panel',
			layout:  {
				type: 'fit'
			},
			docked: 'left',
			width: 200,
			items: [{
				xtype: 'label',
				html: Karazy.i18n.translate('spotDetailCustomerLabel'),
				docked: 'top',
				cls: 'spotdetailitem-customer-label'
			},{
				xtype: 'list',
				itemId: 'checkInList', 
				itemTpl: new Ext.XTemplate(
					"<h2 class='spotdetail-customer-name'>{nickname}</h2>"+
					"<tpl if='status == \"ORDER_PLACED\"'>"+
						"<span class='spotdetail-customer-flag'>X</span>"+
					"</tpl>",
						{
							translateStatus: function(status) {
								return Karazy.i18n.translate(status);
							}
						}),
				store: 'checkInStore',
				ui: 'round'
			}
			]
		},
		{
			xtype: 'panel',
			layout: {
				type: 'fit'
			},
			// fullscreen: true,
			items: [
			{
				xtype: 'panel',
				docked: 'top',
				height: 100,
				items: [
				{
					xtype: 'button',
					action: 'close',
					baseCls: 'spotdetail-close',
					text: 'X'
				},
				{
					xtype: 'label',
					itemId: 'statistics',
					cls: 'spotdetail-statistics',
					tpl: new Ext.XTemplate('<p>'+Karazy.i18n.translate('statistic')+'</p><p>Check-In: {[this.formatTime(values.checkInTime)]}</p>',
						{
							formatTime: function(time) {
								return Ext.util.Format.date(time, 'H:i');
							}
						}
					)
				},
				{
					xtype: 'label',
					itemId: 'statusLabel',
					cls: 'spotdetail-status',
					tpl: new Ext.XTemplate('<p>Status:</p><p class="{[values.status.toLowerCase()]}">{[this.translateStatus(values.status)]}</p>',
						{
							translateStatus: function(status) {
								return Karazy.i18n.translate(status);
							}
						}
					)
				}]
			},
			 {
				xtype: 'dataview',
				itemId: 'spotDetailOrders',
				store: 'orderStore',
				useComponents: true,
				defaultType: 'spotdetailitem'
				
			}, 
			{
				xtype: 'toolbar',
				baseCls: 'spotdetail-toolbar',
				docked: 'bottom',
				layout: {
					type: 'hbox',
					align: 'middle',
					pack: 'center'
				},
				defaults: {
					ui: 'action',
					cls: 'spotdetail-toolbar-button'
				},
				items: [
				{
					text: 'Paid',
					action: 'pay',
				},
				{
					text: 'Redeem'
				}, 
				{
					text: 'User'
				}, 
				{
					text: 'Cancel'
				}
				]				
			}
			]
		}
		]
	}
});