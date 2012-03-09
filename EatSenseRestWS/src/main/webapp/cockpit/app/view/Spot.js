Ext.define('EatSense.view.Spot', {
	extend: 'Ext.Panel',
	requires: ['EatSense.view.SpotItem'],
	xtype: 'spotcard',
	config: {
		id: 'spotcard',
		title: 'Spots',
		fullscreen: true,
		layout: 'fit',
		items: [		
				{
					xtype: 'dataview',
					itemId: 'spotsview',
					store: 'spotStore',
					// itemTpl: '<div><h2>{name}</h2><p>Check in: {checkInTime}</p><p>Value: {currentTotal}</p></div>',
					baseCls: 'dv-baseCls',
					itemCls: 'spot',
					// pressedCls: 'spotPressed',
					// selectedCls: 'spotSelected',
					// allowSelect: false,
					useComponents: true,
    				defaultType: 'spotitem',
    				// listeners: {
    				// 	itemtab: function(dv, index, target, record) {},
    				// 	select: function(dv, index, target, record) {alert('select '+record.get('name'))},
    				// 	itemsingletab: function(dv, index, target, record) {alert('itemsingletab '+record.get('name'))},
    				// }
    				initialize: function() {
						Ext.Logger.info('Spot initialize');

						this.on('itemtap', function(view, index, node, e) {
							    alert('itemtap '+record.get('name'))
							}, this);

						this.on('select', function(view, index, node, e) {
							    alert('select '+record.get('name'))
							}, this);

						this.on('itemsingletab', function(view, index, node, e) {
							    alert('itemsingletab '+record.get('name'))
							}, this);

					}
				}
							
		]
	}

	
})