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
			title : i18nPlugin.translate('settingsTitle'),
		}, 
		{
			xtype: 'label',
			// height: '100px',
			// styleHtmlContent : true,
			html: Karazy.i18n.translate('nicknameDesc')
		},
		{
			xtype : 'textfield',
			label : i18nPlugin.translate('nickname'),
			itemId : 'nickname'
		} ]
	}
});