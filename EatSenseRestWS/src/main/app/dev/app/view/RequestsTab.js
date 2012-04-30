/*
*	Settings Tab Displayed in Lounge view.
*/
Ext.define('EatSense.view.RequestsTab', {
	extend : 'Ext.Panel',
	xtype : 'requeststab',
	config : {
		layout : {
			type: 'vbox',
			pack: 'center',
			align: 'middle'
		},				
		iconCls : 'requests',
		title: Karazy.i18n.translate('requestsButton'),
		iconMask : true,
		items : [
			{
			xtype : 'titlebar',
			docked : 'top',
			title : Karazy.i18n.translate('requestsTitle'),
			},
			{
				xtype: 'button',
				text: Karazy.i18n.translate('callWaiterButton'),
				action: 'waiter',
				ui: 'action'
				// badgeCls: 'call-waiter-badge'
			},
		]
	}
});