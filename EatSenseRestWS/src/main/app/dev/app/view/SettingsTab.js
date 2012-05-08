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
			xtype: 'formpanel',
			//prevents also that the panel has a wrong size. Bug?
			scrollable: false,
			padding: 5,
			layout: {
				type: 'vbox',
				// align: 'center'
			},
			items: [		
				{
					xtype: 'label',
					html: Karazy.i18n.translate('nicknameDesc')
				},
				{
					xtype : 'textfield',
					label : Karazy.i18n.translate('nickname'),
					itemId : 'nickname'
				}
			]
		},
		{			
			xtype: 'newsletter',			
			padding: 5
		}]
	}
});