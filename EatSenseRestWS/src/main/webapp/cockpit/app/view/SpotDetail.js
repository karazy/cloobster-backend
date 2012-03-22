Ext.define('EatSense.view.SpotDetail', {
	extend: 'Ext.Panel',
	xtype: 'spotdetail',
	requires: ['EatSense.view.SpotDetailItem'],
	config: {
		modal: true,
		hideOnMaskTap: 'true',
		top: '10%',
		left: '10%',
		right: '10%',
		bottom: '10%',
		layout: 'fit',
		fullscreen: true,
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
				itemTpl: '<h2>{nickname}</h2>',
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
					itemId: 'statusLabel',
					cls: 'spotdetail-status',
					tpl: new Ext.XTemplate('<p class="{[values.status.toLowerCase()]}">{[this.translateStatus(values.status)]}</p>',
						{
							translateStatus: function(status) {
								console.log('translateStatus');
								return Karazy.i18n.translate(status);
							}
						}
					)
				}]
			},
			 {
				xtype: 'dataview',
				itemId: 'spotDetailOrders',
				// height: 300,
				// fullscreen: true,
				store: 'orderStore',
				useComponents: true,
				defaultType: 'spotdetailitem'
				
			}, 
			{
				xtype: 'toolbar',
				docked: 'bottom',
				items: [
				{
					text: 'Paid',
					action: 'pay'
				},
				{
					text: 'Reedem'
				}, {
					text: 'User'
				}, {
					text: 'Cancel'
				}
				]				
			}
			]
		}
		]
	}
});