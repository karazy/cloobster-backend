/**
*	
*/
Ext.define('EatSense.view.fragment.RequestMenu', {
	extend: 'Ext.Panel',
	xtype: 'requestmenu',
	config: {
		layout: 'vbox',
		// hidden: true,
		modal: true,
		hideOnMaskTap: true,
		defaults: {
			xtype: 'button'
		},
		items: [{
			text: 'Bedienung rufen',
			action: 'waiter'
		}]
	}
});