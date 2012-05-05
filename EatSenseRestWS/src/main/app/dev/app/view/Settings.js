/**
*	Settings section. Show when navigating from dashboard.
*/
Ext.define('EatSense.view.Settings', {
	extend : 'Ext.Panel',
	xtype : 'settings',
	config : {
		layout: {
			type: 'vbox',
		},
		items : [ {
			xtype : 'titlebar',
			docked : 'top',
			title : Karazy.i18n.translate('settingsTitle'),
			items : [ {
				text: Karazy.i18n.translate('back'),
				action: 'back',
				ui: 'back',
				align: 'left'
			} ]
		}, 
		{
			xtype: 'panel',
			layout: 'vbox',
			items: [
				{
					xtype: 'label',
					html: Karazy.i18n.translate('nicknameDesc')
				},
				{
					xtype : 'textfield',
					label : Karazy.i18n.translate('nickname'),
					itemId : 'nicknameSetting',
				}
			]
		},
		// {
		// 	xtype: 'newsletter',
		// 	height: 200
		// }
		]
	}
});