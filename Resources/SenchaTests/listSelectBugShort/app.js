			Ext.application({
			    launch: function () {


			        Ext.define('ListTest.model.Person', {
			            extend: 'Ext.data.Model',
			            fields: [{
			                name: 'firstName',
			                type: 'string'
			            }, {
			                name: 'lastName',
			                type: 'string'
			            }]
			        });
			        var myStore = Ext.create('Ext.data.Store', {
			            model: 'ListTest.model.Person',
			            data: [{
			                firstName: 'Tommy',
			                lastName: 'Maintz'
			            }, {
			                firstName: 'Rob',
			                lastName: 'Dougan'
			            }, {
			                firstName: 'Ed',
			                lastName: 'Spencer'
			            }, {
			                firstName: 'Jamie',
			                lastName: 'Avins'
			            }, {
			                firstName: 'Aaron',
			                lastName: 'Conran'
			            }, {
			                firstName: 'Dave',
			                lastName: 'Kaneda'
			            }]
			        });

			        var panel = Ext.create('Ext.Panel', {
			            fullscreen: true,
			            id: 'testPanel',
			            layout: {
			                type: 'fit'
			            },
			            items: [{
			                docked: 'top',
			                xtype: 'toolbar',
			                title: 'This is a list'
			            }, {
			                xtype: 'list',
			                id: 'thelist',
			                allowDeselect: true,
			                store: myStore,
			                itemTpl: '<div>{firstName} {lastName}</div>',
			                listeners: {
			                    itemtap: function (dv, ix, item, e) {
			                        dv.deselect(ix);
			                        dv.deselect(item);
			                    }
			                }

			            }]

			        });
			    }

			});