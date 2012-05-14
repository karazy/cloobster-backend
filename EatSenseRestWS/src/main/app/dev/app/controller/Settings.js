/**
*	Handles save and restore of application settings.
*   Register for newsletter.
*/
Ext.define('EatSense.controller.Settings', {
    extend: 'Ext.app.Controller',
    requires: ['EatSense.view.NewsletterPopup', 'EatSense.view.Newsletter'],
    config: {
    	refs: {
    		settingsTab: 'lounge settingstab[tabName=settings]',
    		nicknameField: 'settingstab #nickname',
            newsletterView: 'settingstab newsletter',
            registerNewsletterBt: 'settingstab newsletter button[action=register]'
    	},

    	control: {
    		settingsTab : {
    			activate: 'loadSettings'
    		},
    		nicknameField : {
    			change: 'saveNickname'
    		},
            registerNewsletterBt: {
                tap: 'registerNewsletterBtTap'
            }
    	}
    },
    launch: function() {
        var me = this;

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
    *   Tap handler for register newsletter button.
    */
    registerNewsletterBtTap: function(button) {
        var me = this,
            newsletter = this.getNewsletterView(),
            values = newsletter.getValues();

        this.registerNewsletter(values);
    },
    /**
    * Submits the form to register a new newsletter.
    * @param record
    *   newsletter data to submit
    * @param successCallback
    *   callback function called on success
    */
    registerNewsletter: function(data, successCallback) {
        var me = this,
            checkInCtr = this.getApplication().getController('CheckIn'),
            appState = checkInCtr.getAppState(),
            record = Ext.create('EatSense.model.Newsletter'),
            errors;

        record.setData(data);
        //validate record
        errors = record.validate();

        if(!errors.isValid()) {
            Ext.Msg.alert(Karazy.i18n.translate('error'), Karazy.i18n.translate('newsletterInvalidEmail'));
            return;
        }

        record.save({
            success: function(record, operation) {
                //ensure PUT is always used when saving the mail
                record.phantom = true;

                appState.set('newsletterRegistered', true);
                
                if(Karazy.util.isFunction(successCallback)) {
                    successCallback();    
                }
                
                //show short success message
                Ext.Msg.show({
                    title : Karazy.i18n.translate('hint'),
                    'message' : Karazy.i18n.translate('newsletterRegisterSuccess', record.get('email')),
                    buttons : []
                });
                //show short alert and then hide
                Ext.defer((function() {
                    if(!Karazy.util.getAlertActive()) {
                        Ext.Msg.hide();
                    }                   
                }), Karazy.config.msgboxHideTimeout, this);
            },
            failure: function(record, operation) {
                me.getApplication().handleServerError({
                    'error': operation.error, 
                    'forceLogout': false,
                    'message' : {500: Karazy.i18n.translate('newsletterDuplicateEmail')}
                }); 
            }
        });
    },
    /**
    * Shows a popup to user asking for his email to register for newsletter.
    */
    registerNewsletterOnLeaving: function() {
        var me = this,
            checkInCtr = this.getApplication().getController('CheckIn'),
            appState = checkInCtr.getAppState(),
            popup = Ext.create('EatSense.view.NewsletterPopup');

        //setup button handler
        popup.on({
            delegate: 'button[action=register]',
            tap: function() {
                me.registerNewsletter(popup.down('newsletter').getValues(), 
                    //remove on success
                function() {
                    Ext.Viewport.remove(popup);
                });
            }
            
        });

        popup.on({
            delegate: 'button[action=dont-ask]',
            tap: function() {
                appState.set('newsletterRegistered', true);
               Ext.Viewport.remove(popup);
            }
        });

        popup.on('hide', function() {
             Ext.Viewport.remove(popup);
        });

        Ext.Viewport.add(popup);
        popup.show();

    }
});