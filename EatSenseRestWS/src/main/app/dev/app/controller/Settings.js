/**
*	Handles save and restore of application settings.
*
*/
Ext.define('EatSense.controller.Settings', {
    extend: 'Ext.app.Controller',
    config: {
    	refs: {
    		settingsTab: 'lounge settingstab[tabName=settings]',
    		nicknameField: 'settingstab #nickname'
    	},

    	control: {
    		settingsTab : {
    			activate: 'loadSettings'
    		},
    		nicknameField : {
    			change: 'saveNickname'
    		}
    	}
    },
    /**
    *	Loads the settings and sets the corresponding fields.
    */
    loadSettings: function(tab, options) {
    	var 	checkInCtr = this.getApplication().getController('CheckIn'),
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
});