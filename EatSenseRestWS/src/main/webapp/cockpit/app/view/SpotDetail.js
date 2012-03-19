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
			// layout: {
			// 	type: 'fit'
			// },
			// fullscreen: true,
			items: [
			{
				xtype: 'panel',
				docked: 'top',
				height: 100,
				html: 'UPPER PANEL'
			},
			 {
				xtype: 'dataview',
				itemId: 'spotDetailOrders',
				width: 300,
				height: 300,
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