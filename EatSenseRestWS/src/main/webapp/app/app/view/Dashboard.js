/**
 * The dashboard represents the main screen of the application.
 * From here the user can navigate, access his order history or change his settings.
 */
Ext.define('EatSense.view.Dashboard', {
    extend: 'Ext.Container',
    xtype: 'dashboard',
    fullscreen: true,
    config: {
        items: [
        	{
        	docked: 'top',
        	xtype: 'toolbar',
        	title: 'eatSense',
        	items: [{        		 
		            xtype: 'label',
		            docked: 'right',
		            html: '<img src="../app/res/images/eatSenseLogo.png" width="50" height="50"></img>',  	        
        		}
        	]
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
            text: i18nPlugin.translate('checkInButton'),
            ui: 'normal'
            },
            {
                xtype: 'textfield',
                label: i18nPlugin.translate('barcode'),
                name: 'barcodeTF',
                hidden: (profile == 'phone' && window.plugins.barcodeScanner)? true : false
            },
            {
            xtype: 'button',
           	itemId: 'settingsBtn',
            text: i18nPlugin.translate('settingsButton'),
            ui: 'normal',
            }
            ]
        }]
    },
    
    showLoadScreen : function(mask) {
    	if(mask) {
    		this.setMasked({
    			message : i18nPlugin.translate('loadingMsg'),
        		xtype: 'loadmask' 
    		});
    	} else {
    		this.setMasked(false);
    	}
    }
});

