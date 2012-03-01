Ext.define('EatSense.view.Settings', {
	extend : 'Ext.Panel',
	xtype : 'settings',
	config : {
		items : [ {
			xtype : 'titlebar',
			docked : 'top',
			title : i18nPlugin.translate('settingsTitle'),
			items : [ {
				text: i18nPlugin.translate('back'),
				action: 'back',
				ui: 'back',
				align: 'left'
			} ]
		}, {
			xtype : 'textfield',
			label : i18nPlugin.translate('nickname'),
			itemId : 'nicknameSetting'
		} ]
	}
});