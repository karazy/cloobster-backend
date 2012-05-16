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
		cls: 'request-panel',
		title: Karazy.i18n.translate('requestsButton'),
		iconMask : true,
		items : [
			{
			xtype : 'titlebar',
			docked : 'top',
			title : Karazy.i18n.translate('requestsTitle'),
			},
			{
				xtype: 'label',
				cls: 'general-label',
				html: Karazy.i18n.translate('callWaiterCallHint')
			},
			{
				xtype: 'button',
				text: Karazy.i18n.translate('callWaiterButton'),
				action: 'waiter',
				ui: 'action',
				margin: '10 0 0 0'
			},
		]
	}
});