Ext.define('EatSense.view.Dashboard', {
    extend: 'Ext.Container',
    xtype: 'dashboard',
    fullscreen: true,
    config: {        
        items: [
        	{
        	docked: 'top',
        	xtype: 'toolbar',
        	title: 'EatSense'
        	},
        	{        	
        	xtype:'panel',
        	layout: {
            	type : 'vbox',
            	pack : 'center',
            	align: 'center',
        	},
        	defaults: {
                //flex  : 1,
                margin: 5
            },
         	items: [
            {
            xtype: 'button',
           	id: 'checkInBtn',
            text: 'CheckIn',
            ui: 'round'
            },
            {
                xtype: 'textfield',
                label: 'Barcode',
                name: 'barcodeTF'
                //value: 'serg2011'
            },
            {
            xtype: 'button',
           	itemId: 'settingsBtn',
            text: 'Settings',
            ui: 'round',
            }
            ]
        }]
    }
});

