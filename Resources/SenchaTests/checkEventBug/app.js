
Ext.application({
		name : 'CheckboxEvent',
		launch : function() {
			var main = Ext.create('Ext.form.Panel', {
				items: [
						{
			            xtype: 'radiofield',
			            name : 'color',
			            value: 'red',
			            label: 'Red',
			            listeners: {
			            	check: function() {
			            		alert('checked red');
			            	},
			            	uncheck: function() {
			            		alert('unchecked red')
			            	}
			            }
			        },
			        {
			            xtype: 'radiofield',
			            name : 'color',
			            value: 'green',
			            label: 'Green',
			              listeners: {
			            	check: function() {
			            		alert('checked green');
			            	},
			            	uncheck: function() {
			            		alert('unchecked green')
			            	}
			            }
			        },
				]

			});

			Ext.Viewport.add(main);
		}
	});