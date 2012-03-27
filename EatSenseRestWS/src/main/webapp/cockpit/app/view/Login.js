Ext.define('EatSense.view.Login', {
	extend: 'Ext.Panel',
	xtype: 'login',
	config: {
		fullscreen: true,
		centered: true,
		width: 500,
		height: 400,
		layout: {
			type: 'card',
			animation: 'fade'
		},
		activeItem: 0,
		// height:200,
		floatingCls: 'loginbox',
		// cls: 'loginbox',
		items: [
		{
			xtype: 'panel',
			padding: '15',
			layout: {
				type: 'vbox',
				align: 'middle',
				pack: 'center'
			},
			items: [
					{
					xtype: 'textfield',
					label: 'Benutzername',
					labelWidth: '40%',
					width: '100%',
					name: 'login',
					cls: 'loginbox-field'
				}, {
					xtype: 'passwordfield',
					label: 'Passwort',
					labelWidth: '40%',
					width: '100%',
					name: 'password',
					cls: 'loginbox-field'
				}, 
				{
					xtype : 'togglefield',
					name : 'savePasswordToggle',
					value : 0,
					labelWidth: '40%',
					width: '100%',
					label : i18nPlugin.translate('savePasswordToggle'),
				},
				{
					xtype: 'button',
					text: 'Login',
					action: 'login'
				}
			]
		},
		{
			xtype: 'choosebusiness'
		}
		]
	}
});
