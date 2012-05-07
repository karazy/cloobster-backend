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
		// bottom: '15%',
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