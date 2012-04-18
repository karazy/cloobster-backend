/**
 * Checkin Step 2 User has to confirm that he wants to checkin and has to choose
 * a nickname.
 */
Ext.define('EatSense.view.Checkinconfirmation', {
	extend : 'Ext.Panel',
	xtype : 'checkinconfirmation',
	fullscreen : false,
	requires: ['Ext.field.Toggle'],
	config : {
		items : [ {
			docked : 'top',
			xtype : 'toolbar',
			title : Karazy.i18n.translate('checkInTitle'),
			items : [ {
				xtype : 'button',
				id : 'cancelCheckInBt',
				text : Karazy.i18n.translate('cancel'),
				ui : 'back'
			}, ]
		}, {
			xtype : 'panel',
			layout : {
				type : 'vbox',
				pack : 'center',
				align : 'center'
			},
			defaults : {
				margin : 5,
			},
			items : [ {
				xtype : 'label',
				styleHtmlContent : true,
				id : 'checkInDlg1Label1',
				html : Karazy.i18n.translate('checkInStep1Label1')
			}, {
				xtype : 'textfield',
				id : 'nicknameTf',
				label : 'Nickname',
				width : 300,
				labelWidth : 100,
				labelAlign : 'top',
				required : true,
			},  {
				xtype : 'panel',
				layout : {
					type : 'hbox'
				},
				items : [ {
					xtype : 'button',
					id : 'regenerateNicknameBt',
					iconCls : 'refresh',
					iconMask : true,
					style : 'margin-right:10px;',
					ui : 'action'
				}, {
					xtype : 'button',
					id : 'confirmCheckInBt',
					text : Karazy.i18n.translate('checkInStep1Button'),
					ui : 'action'
				} ]
			},
			{
				xtype : 'togglefield',
				name : 'nicknameToggle',
				labelAlign: 'top',
				value : 0,
				label : Karazy.i18n.translate('saveNicknameToggle'),
			}
			]
		} ]
	}
});