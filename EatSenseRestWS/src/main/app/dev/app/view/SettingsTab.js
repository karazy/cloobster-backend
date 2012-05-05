/*
*	Settings Tab Displayed in Lounge view.
*/
Ext.define('EatSense.view.SettingsTab', {
	extend : 'Ext.Panel',
	xtype : 'settingstab',
	config : {
		layout : 'vbox',				
		iconCls : 'settings',
		title: Karazy.i18n.translate('settingsButton'),
		iconMask : true,
		items : [ {
			xtype : 'titlebar',
			docked : 'top',
			title : Karazy.i18n.translate('settingsTitle'),
		}, 
		{
			xtype: 'label',
			html: Karazy.i18n.translate('nicknameDesc')
		},
		{
			xtype : 'textfield',
			label : Karazy.i18n.translate('nickname'),
			itemId : 'nickname'
		},
		{
			xtype: 'newsletter',
			height: 200
		} ]
	}
});