/*
*	Settings Tab Displayed in Lounge view.
*/
Ext.define('EatSense.view.SettingsTab', {
	extend : 'Ext.Panel',
	xtype : 'settingstab',
	config : {
		layout : 'vbox',				
		iconCls : 'settings',
		cls: 'setting-panel',
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
			margin: '10 0 0 0',
			padding: 5,
			layout: {
				type: 'vbox',
				pack: 'center',
				align: 'middle'
			},
			defaults: {
				width: '80%'
			},
			items: [		
				{
					xtype: 'label',
					cls: 'general-label',
					margin: '5 0 5 0',
					html: Karazy.i18n.translate('nicknameDesc')
				},
				{
					xtype : 'textfield',
					label : Karazy.i18n.translate('nickname'),
					itemId : 'nickname',
					cls: 'general-textfield',
					labelCls: 'general-field-label-horizontal'
				}
			]
		},
		{	
			xtype: 'newsletter',			
			padding: 5
		}]
	}
});