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
		// top: '30%',
		// left: '30%',
		// right: '30%',
		// bottom: '30%',
		items : [
			{
			xtype : 'titlebar',
			docked : 'top',
			title : i18nPlugin.translate('requestsTitle'),
			},
			{
				xtype: 'button',
				text: 'Bedienung rufen',
				action: 'waiter'
			}
		]
	}
});