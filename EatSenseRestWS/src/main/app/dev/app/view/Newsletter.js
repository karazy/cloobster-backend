Ext.define('EatSense.view.Newsletter', {
	extend: 'Ext.form.Panel',
	xtype: 'newsletter',
	requires: ['Ext.field.Email'],
	config: {
		layout: {
			type: 'vbox',
			pack: 'center',
			align: 'middle'
		},
		//prevents also that the panel has a wrong size. Bug?
		scrollable: false,
		cls: 'newsletter',
		defaults: {
				width: '90%'
		},
		items: [
		{
			xtype: 'label',			
			cls: 'newsletter-label',
			html: Karazy.i18n.translate('newsletterLabel')
		},
		{
			xtype: 'emailfield',
			label: Karazy.i18n.translate('newsletterEmail'),
			name:'email',
			cls: 'newsletter-field'
		}, {
			xtype: 'button',
			action: 'register',
			ui: 'action',
			cls: 'newsletter-button',
			text: Karazy.i18n.translate('newsletterRegisterBt'),
			cls: 'newsletter-register-button',			
		}
		]
	}
});