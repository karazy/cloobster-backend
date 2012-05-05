Ext.define('EatSense.view.Newsletter', {
	extend: 'Ext.form.Panel',
	xtype: 'newsletter',
	require: ['EatSense.model.Newsletter'],
	config: {
		layout: 'vbox',
		// record: Ext.create('EatSense.model.Newsletter'),
		items: [
		{
			xtype: 'label',
			html: 'Stay up-to-date und verpasse nicht den eatSense Big Bang! Melde dich für den Newsletter an.'
		},
		{
			xtype: 'emailfield',
			label: Karazy.i18n.translate('newsletterEmail'),
			name:'email'	
		}, {
			xtype: 'button',
			action: 'register',
			text: Karazy.i18n.translate('newsletterRegisterBt')
		}
		]
	}
});