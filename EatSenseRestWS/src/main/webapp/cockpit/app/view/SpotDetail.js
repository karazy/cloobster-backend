Ext.define('EatSense.view.SpotDetail', {
	extend: 'Ext.Panel',
	xtype: 'spotdetail',
	requires: ['EatSense.view.SpotDetailItem'],
	config: {
		modal: true,
		hideOnMaskTap: 'true',
		top: '5%',
		left: '5%',
		right: '5%',
		height: '90%',
		layout: 'fit',
		items: [{
			xtype: 'panel',
			// layout:  {
			// 	type: 'fit'
			// },
			docked: 'left',
			width: 150,
			items: [{
				xtype: 'label',
				html: 'Guests',
				docked: 'top'
			},{
				xtype: 'list',
				itemId: 'checkInList',
				itemTpl: '{nickname}',
			}
			]
		},
		{
			xtype: 'panel',
			layout: {
				type: 'fit'
			},
			items: [{
				xtype: 'panel',
				docked: 'top',
				height: 100,
				html: 'UPPER PANEL'
			},
			 {
				xtype: 'dataview',
				fullscreen: true,
				// height: '100%'
				
			}, 
			{
				xtype: 'toolbar',
				docked: 'bottom',
				items: [
				{
					text: 'Paid'
				},
				{
					text: 'Reedem'
				}, {
					text: 'User'
				}, {
					text: 'Cancel'
				}
				]				
			}]
		}
		]
	}
});