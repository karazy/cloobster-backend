/**
* Popup dialog showing a form to register for newsletter.
*/
Ext.define('EatSense.view.NewsletterPopup', {
	extend: 'Ext.Panel',
	xtype: 'newsletterpopup',
	requires: ['EatSense.view.Newsletter'],
	config: {
		layout: {
			type: 'vbox',
			pack: 'center',
			align: 'middle'
		},
		top: '30%',
		left: '20px',
		right: '20px',
		hideOnMaskTap: true,
		modal: true,
		cls: 'newsletter-popup',
		floatingCls: 'newsletter-floating',
		items: [
		{
			xtype: 'titlebar',
			docked: 'top',
			title: Karazy.i18n.translate('newsletterPopupTitle')
		},
		{
			xtype: 'newsletter',
			width: '100%'
		},
		{
			xtype: 'button',
			ui: 'action',
			action: 'dont-ask',
			width: '80%',
			margin: '5 0 5 0',
			text: Karazy.i18n.translate('newsletterDontAskButton')
		}]
	}
});