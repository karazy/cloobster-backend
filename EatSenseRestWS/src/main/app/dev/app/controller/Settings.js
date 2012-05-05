/**
*	Handles save and restore of application settings.
*
*/
Ext.define('EatSense.controller.Settings', {
    extend: 'Ext.app.Controller',
    config: {
    	refs: {
    		settingsTab: 'lounge settingstab[tabName=settings]',
    		nicknameField: 'settingstab #nickname',
            newsletterView: 'newsletter',
            registerNewsletterBt: 'newsletter button[action=register]'
    	},

    	control: {
    		settingsTab : {
    			activate: 'loadSettings'
    		},
    		nicknameField : {
    			change: 'saveNickname'
    		},
            registerNewsletterBt: {
                tap: 'registerNewsletter'
            }
    	}
    },
    launch: function() {
        var me = this;

        //setup newsletter record and listeners
        this.getNewsletterView().setRecord(Ext.create('EatSense.model.Newsletter'));

        this.getNewsletterView().on({
            delegate: 'field',
            change: function(field, newVal, oldVal) {
                console.log('set field ' + newVal);
                me.getNewsletterView().getRecord().set(field.getName(), newVal);
            }
        });
    },
    /**
    *	Loads the settings and sets the corresponding fields.
    */
    loadSettings: function(tab, options) {
    	var checkInCtr = this.getApplication().getController('CheckIn'),
    		appState = checkInCtr.getAppState();

    	this.getNicknameField().setValue(appState.get('nickname'));    	
    },

    /**
	 * Saves the nickname in local store.
	 */
	saveNickname: function(component, newData, oldValue, eOpts) {
    	var 	checkInCtr = this.getApplication().getController('CheckIn'),
		appState = checkInCtr.getAppState();

		appState.set('nickname', newData);
	},
    /**
    * Submits the form to register a new newsletter.
    */
    registerNewsletter: function(button) {
        var me = this,
            newsletter = this.getNewsletterView(),
            record = newsletter.getRecord();

        console.log('register new newsletter %s', newsletter.getValues().email);
        //validate record
        record.validate();

        //get data from email field
        Ext.Ajax.request({
            url: Karazy.config.serviceUrl+'/newsletter',
            method: 'POST',
            jsonData: record.getData(),
            success: function(response) {
                //show short success message
                Ext.Msg.show({
                    title : Karazy.i18n.translate('hint'),
                    'message' : Karazy.i18n.translate('newsletterRegisterSuccess', newsletter.getValues()),
                    buttons : []
                });
                //show short alert and then hide
                Ext.defer((function() {
                    if(!Karazy.util.getAlertActive()) {
                        Ext.Msg.hide();
                    }                   
                }), Karazy.config.msgboxHideTimeout, this);
            },
            failure: function(response) {
                me.getApplication().handleServerError({
                    'error': { 'status' : response.status, 'statusText' : response.statusText}, 
                    'forceLogout': false,
                    'message' : {500: 'E-Mail schon vorhanden oder nicht gültig!'}
                }); 
            }
        });

    }
});