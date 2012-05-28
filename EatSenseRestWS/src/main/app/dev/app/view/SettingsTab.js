/*
*	Settings Tab Displayed in Lounge view.
*/
Ext.define('EatSense.view.SettingsTab', {
	extend : 'Ext.Panel',
	xtype : 'settingstab',
	config : {
		layout: {
			type: 'vbox',
			pack: 'center',
			align: 'middle'
		},
		scrollable: 'vertical',				
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
			// padding: 5,	
			margin: '10 0 0 0',
			width: '80%',
			items: [
				{
					xtype: 'label',
					cls: 'general-label',
					margin: '7 0 5 0',
					html: Karazy.i18n.translate('nicknameDesc')
				},
				{
					xtype : 'textfield',
					label : Karazy.i18n.translate('nickname'),
					labelWidth: '40%',
					itemId : 'nickname',
					cls: 'general-textfield',
					labelCls: 'general-field-label-horizontal'
				}
			]
		},
		{	
			xtype: 'newsletter',			
			// padding: 5,
			width: '80%'
		},
		{
			xtype: 'button',
			text: 'Impressum',
			ui: 'action',
			action: 'about',
			margin: '7 0 5 0',
			width: '80%'
		}
		]
	}
});