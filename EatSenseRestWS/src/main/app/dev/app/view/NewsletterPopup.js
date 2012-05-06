/**
* Popup dialog showing a form to register for newsletter.
*/
Ext.define('EatSense.view.NewsletterPopup', {
	extend: 'Ext.Panel',
	requires: ['EatSense.view.Newsletter'],
	config: {
		layout: 'vbox',
		top: '20%',
		left: '20px',
		right: '20px',
		bottom: '15%',
		hideOnMaskTap: true,
		modal: true,
		cls: 'newsletter-popup',
		items: [
		{
			xtype: 'titlebar',
			docked: 'top',
			title: Karazy.i18n.translate('newsletterPopupTitle')
		},
		{
			xtype: 'newsletter'
		},
		{
			xtype: 'button',
			action: 'dont-ask',
			text: Karazy.i18n.translate('newsletterDontAskButton')
		}]
	}
});