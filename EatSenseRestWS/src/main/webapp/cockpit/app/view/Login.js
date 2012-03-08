Ext.define('EatSense.view.Login', {
	extend: 'Ext.Panel',
	xtype: 'login',
	config: {
		fullscreen: true,
		centered: true,
		width: 500,
		height:200,
		floatingCls: 'loginbox',
		// cls: 'loginbox',
		layout: {
			type: 'vbox',
			align: 'middle',
			pack: 'center'
		},
		items: [
		{
			xtype: 'textfield',
			label: 'Benutzername',
			labelWidth: '30%',
			width: '90%',
			name: 'login',
			cls: 'loginbox-field'
		}, {
			xtype: 'passwordfield',
			label: 'Passwort',
			labelWidth: '30%',
			width: '90%',
			name: 'password',
			cls: 'loginbox-field'
		}, {
			xtype: 'button',
			text: 'Login',
			action: 'login'
		}

		]
	}
});
