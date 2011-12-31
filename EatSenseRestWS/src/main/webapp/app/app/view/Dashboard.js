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
                margin: 5,
                type : 'fit'
            },
         	items: [
            {
            xtype: 'button',
           	id: 'checkInBtn',
            text: 'CheckIn',
            ui: 'normal'
            },
            {
                xtype: 'textfield',
                label: 'Barcode',
                name: 'barcodeTF'
            },
            {
            xtype: 'button',
           	itemId: 'settingsBtn',
            text: 'Settings',
            ui: 'normal',
            }
            ]
        }]
    }
});

