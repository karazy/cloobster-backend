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
				action: 'cancel-checkin',
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
				itemId : 'checkInDlg1Label1',
				html : Karazy.i18n.translate('checkInStep1Label1')
			}, {
				xtype : 'textfield',
				itemId : 'nicknameTf',
				label : Karazy.i18n.translate('nickname'),
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
					action: 'regenerate-nickname',
					iconCls : 'refresh',
					iconMask : true,
					style : 'margin-right:10px;',
					ui : 'action'
				}, {
					xtype : 'button',
					action: 'confirm-checkin',
					text : Karazy.i18n.translate('checkInStep1Button'),
					ui : 'action'
				} ]
			},
			{
				xtype : 'togglefield',
				action : 'toggle-nickname',
				labelAlign: 'top',
				value : 0,
				label : Karazy.i18n.translate('saveNicknameToggle'),
			}
			]
		} ]
	}
});