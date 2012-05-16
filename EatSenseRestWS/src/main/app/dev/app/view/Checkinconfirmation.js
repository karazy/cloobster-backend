/**
 * Checkin Step 2 User has to confirm that he wants to checkin and has to choose
 * a nickname.
 */
Ext.define('EatSense.view.Checkinconfirmation', {
	extend : 'Ext.Panel',
	xtype : 'checkinconfirmation',
	requires: ['Ext.field.Toggle'],
	config : {
		layout : {
			type : 'vbox',
			pack : 'center',
			align : 'center'
		},
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : Karazy.i18n.translate('checkInTitle'),
			items : [ {
				xtype : 'button',
				action: 'cancel-checkin',
				text : Karazy.i18n.translate('cancel'),
				ui : 'back'
			}, ]
		}, 
		{
			xtype : 'label',
			itemId : 'checkInDlg1Label1',
			html : Karazy.i18n.translate('checkInStep1Label1'),
			margin : 5,
			cls: 'checkin-confirm-nickname-label'
		}, 
		{
			xtype : 'textfield',
			itemId : 'nicknameTf',
			width : '80%',
			required : true,
			maxLength: 25,
			margin : 5,
			cls: 'checkin-confirm-nickname-field'
		},  
		{
			xtype : 'panel',
			layout : {
				type : 'hbox'
			},
			items : [ {
				xtype : 'button',
				action: 'regenerate-nickname',
				text : Karazy.i18n.translate('refreshNicknameBt'),
				// iconCls : 'refresh',
				// iconMask : true,
				style : 'margin-right:10px;',
				ui : 'action',
				margin : 5
			},
			{
				xtype : 'button',
				action: 'confirm-checkin',
				text : Karazy.i18n.translate('checkInStep1Button'),
				ui : 'action',
				margin : 5
			} ]
		},
		{
			xtype : 'togglefield',
			action : 'toggle-nickname',
			cls: 'checkin-confirm-nickname-toggle',
			labelCls: 'checkin-confirm-nickname-toggle-label',
			labelAlign: 'top',
			html: Karazy.i18n.translate('nicknameToggleHint'),
			width: '80%',
			value : 0,
			label : Karazy.i18n.translate('saveNicknameToggle')			
		}
		]
	},
	
	showLoadScreen : function(mask) {
		if (mask) {
			this.setMasked({
				message : Karazy.i18n.translate('loadingMsg'),
				xtype : 'loadmask'
			});
		} else {
			this.setMasked(false);
		}
	}
});