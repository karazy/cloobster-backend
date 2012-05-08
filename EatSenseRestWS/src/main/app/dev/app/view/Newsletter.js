Ext.define('EatSense.view.Newsletter', {
	extend: 'Ext.form.Panel',
	xtype: 'newsletter',
	require: ['Ext.field.Email'],
	config: {
		layout: {
			type: 'vbox',
			pack: 'center',
			align: 'middle'
		},
		//prevents also that the panel has a wrong size. Bug?
		scrollable: false,
		cls: 'newsletter',
		items: [
		{
			xtype: 'label',			
			cls: 'newsletter-label',
			html: 'Stay up-to-date und verpasse nicht den eatSense Big Bang! Melde dich für den Newsletter an.'
		},
		{
			xtype: 'emailfield',
			label: Karazy.i18n.translate('newsletterEmail'),
			name:'email',
			cls: 'newsletter-field',
			minWidth: 200
		}, {
			xtype: 'button',
			action: 'register',
			cls: 'newsletter-button',
			text: Karazy.i18n.translate('newsletterRegisterBt'),
			cls: 'newsletter-register-button',			
		}
		]
	}
});