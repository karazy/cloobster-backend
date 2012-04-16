/**
*	Displays details of a spot.
*	Details are checkIns, orders and statistics.
*/
Ext.define('EatSense.view.SpotDetail', {
	extend: 'Ext.Panel',
	xtype: 'spotdetail',
	requires: ['EatSense.view.SpotDetailItem'],
	config: {
		modal: true,
		hideOnMaskTap: 'true',
		baseCls: 'spotdetail',
		top: '5%',
		left: '5%',
		right: '5%',
		bottom: '5%',
		layout: 'fit',
		fullscreen: true,
		//this should be initially hidden
		hidden: true,
		listeners: {
			'eatSense.customer-update': function(active) {
				//enable/disable action buttons depending on customer status
				try {
					this.down('button[action=switch-spot]').setDisabled(!active);
					this.down('button[action=paid]').setDisabled(!active);			
					this.down('button[action=cancel-all]').setDisabled(!active);
				} catch(e) {
					console.log(e);
				}
			}
		},
		items: [
		{
			xtype: 'titlebar',
			docked: 'top',
			cls: 'spotdetail-titlebar',
			items: [
			{
					xtype: 'button',
					action: 'close',
					// baseCls: 'spotdetail-close',
					text: Karazy.i18n.translate('close'),
					align: 'right'
			}
			]
		},
		{
			xtype: 'customerrequest',
			docked: 'right',
			layout:'fit',
			width: 200
		},
		{
			xtype: 'panel',
			layout:  {
				type: 'fit'
			},
			docked: 'left',
			width: 200,
			items: [{
				xtype: 'label',
				html: Karazy.i18n.translate('spotDetailCustomerLabel'),
				docked: 'top',
				cls: 'spotdetailitem-customer-label'
			},{
				xtype: 'list',
				itemId: 'checkInList', 
				itemTpl: new Ext.XTemplate(
						"<h2 class='spotdetail-customer-name'>{nickname}</h2>"+
						"<tpl if='status == \"ORDER_PLACED\" || status == \"PAYMENT_REQUEST\"'>"+
							"<span class='spotdetail-customer-flag'>X</span>"+
						"</tpl>"
						),
				store: 'checkInStore',
				ui: 'round'
			}
			]
		},
		{
			xtype: 'panel',
			layout: {
				type: 'fit'
			},
			// fullscreen: true,
			items: [
			{
				xtype: 'panel',
				docked: 'top',
				height: 100,
				items: [
				{
					xtype: 'panel',
					// itemId: 'statistics',
					cls: 'spotdetail-statistics',
					items: [
						{
							xtype: 'label',
							itemId: 'title',					
							html: '<p>'+Karazy.i18n.translate('statistic')+'</p>'
						},
						{
							xtype: 'label',
							itemId: 'checkInTime',
							tpl: new Ext.XTemplate(
								'<p>Check-In: {[this.formatTime(values.checkInTime)]}</p>',
								{
									formatTime: function(time) {
										return Ext.util.Format.date(time, 'H:i');
									}
								}
							)
						},				
						{
							xtype: 'label',
							itemId: 'total',
							tpl: new Ext.XTemplate('<p>Total: {[this.formatPrice(values.total)]}</p>',
								{
									formatPrice: function(price) {
										return Karazy.util.formatPrice(price);
									}
								}
							)
						}
					]
				},
				{
					xtype: 'label',
					itemId: 'statusLabel',
					cls: 'spotdetail-status',
					tpl: new Ext.XTemplate('<p>Status:</p><p class="{[values.status.toLowerCase()]}">{[this.translateStatus(values.status)]}</p>',
						{
							translateStatus: function(status) {
								return Karazy.i18n.translate(status);
							}
						}
					)
				},
				{
					xtype: 'label',
					itemId: 'paymentLabel',
					cls: 'spotdetail-status',
					hidden: true,
					tpl: new Ext.XTemplate('<p>'+Karazy.i18n.translate('paymentMethodLabel')+':</p><p class="payment">{paymentMethod}</p>',
						{
							translateStatus: function(status) {
								return Karazy.i18n.translate(status);
							}
						}
					)
				}]
			},
			 {
				xtype: 'dataview',
				itemId: 'spotDetailOrders',
				store: 'orderStore',
				useComponents: true,
				defaultType: 'spotdetailitem'				
			}, 
			{
				xtype: 'toolbar',
				baseCls: 'spotdetail-toolbar',
				docked: 'bottom',
				layout: {
					type: 'hbox',
					align: 'middle',
					pack: 'center'
				},
				defaults: {
					ui: 'action',
					cls: 'spotdetail-toolbar-button'
				},
				items: [
				{
					text: Karazy.i18n.translate('paidButton'),
					action: 'paid',
					disabled: true
				},
				{
					text: Karazy.i18n.translate('switchSpotButton'),
					action: 'switch-spot'
				},
				{
					text: Karazy.i18n.translate('cancelAllOrdersButton'),
					action: 'cancel-all',
					// iconCls: 'cancel-all'
					// icon: '../app/res/images/into_cart.png',
					// iconAlign: 'centered',
				}
				]				
			}
			]
		}
		]
	}
});