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
        	items: [
//        	        {        		 
//		            xtype: 'label',
//		            docked: 'right',
//		            html: '<img src="../app/res/images/cart.png" width="50" height="50"></img>',  	        
//        		}
        	]
        	},
        	{        	
        	xtype:'panel',    
        	style: 'background-image: url(../app/res/images/eatSenseLogo_big.png); background-repeat:no-repeat; background-position:center center;',
        	layout: {
            	type : 'vbox',
            	pack : 'center',
            	align: 'middle',            	
        	},
        	defaults: {
                margin: 5,
            },
         	items: [
            {
            xtype: 'button',
           	id: 'checkInBtn',
            text: i18nPlugin.translate('checkInButton'),
            ui: 'action',
            height: '100px',
            width: '150px'
            },
            {
                xtype: 'textfield',
                label: i18nPlugin.translate('barcode'),
                labelWidth: 100,
                width: 300,                
                name: 'barcodeTF',
                hidden: (profile == 'phone' && window.plugins.barcodeScanner)? true : false
            },
            {
            xtype: 'button',
           	itemId: 'settingsBtn',
           	action: 'settings',
            text: i18nPlugin.translate('settingsButton'),
           	iconCls: 'settings',
           	iconMask: true,
            ui: 'action',
            width: '150px'
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

