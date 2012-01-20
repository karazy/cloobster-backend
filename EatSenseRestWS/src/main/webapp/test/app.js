/**
 * 
 */

Ext.application({
	launch : function() {

		var panel = Ext.create('Ext.Panel', {
			fullscreen : true,
			id : 'testPanel',
			scrollable : 'vertical',
			layout : {
				type : 'vbox'
			},
			defaults : {
				styleHtmlContent : false,
			},
			items : [ {
				docked : 'top',
				xtype : 'toolbar',
				title : 'Product detail',
				items : [ {
					xtype : 'button',
					text : 'back',
					ui : 'back'
				} ]

			}, {
				xtype : 'panel',
				docked : 'top',
//				
				items : [ {
					xtype : 'label',
					html : '<div><h2>Product Name</h2><p>Detailed product description.</p></div>'
				}, {
					xtype : 'panel',
					layout : 'hbox',
					flex : 1,
					// html: 'spinner + button panel',
					items : [ {
						xtype : 'spinnerfield',
						increment : 1
					}, {
						xtype : 'button',
						text : 'Card'
					} ]
				} ]
			}, {
				xtype : 'panel',
				itemId : 'choicePanel',
				// scrollable : 'vertical',
				items : [ {
					xtype : 'label',
					html : '<p>Options panel</p>'
				}, {
					xtype : 'panel',
					itemId : 'radioButtonPanel',
					layout : {
						type : 'vbox',
						align : 'stretch'
					},
					items : [ {
						xtype : 'label',

						html : '<p>Mandatory options (radio buttons)</p>'
					}, {
						xtype : 'panel',
						// height : 40,
						layout : {
							type : 'vbox',
							// align : 'stretch'
							pack : 'center'
						},
						items : [ {
							xtype : 'radiofield',
							value : '1',
							name : 'group1',
							label : 'Radio 1',
							checked : true
						}, {
							xtype : 'radiofield',
							value : '2',
							name : 'group1',
							label : 'Radio 2'
						}
						, {
							xtype : 'radiofield',
							value : '3',
							name : 'group1',
							label : 'Radio 3'
						}]

					}

					]
				}, {
					xtype : 'panel',
					items : [ {
						xtype : 'label',
						html : '<p>Optional options (checkboxes)</p>'
					}, {
						xtype : 'panel',
						// height : 40,
						layout : {
							type : 'vbox',
							align : 'stretch'
						},
						items : [ {
							xtype : 'checkboxfield',
							value : '1',
							name : 'group2',
							label : 'Checkbox 1',
						}, {
							xtype : 'checkboxfield',
							value : '2',
							name : 'group2',
							label : 'Checkbox 2'
						} ]

					} ]
				} ]
			}

			]

		});
		
		var radioBt = Ext.create('Ext.field.Radio', {
			 name : 'group1',
			 value : '4',
			 label : 'Dynamic radio button',
			 styleHtmlContent : true,
		 });
		
		panel.getComponent('choicePanel').getComponent('radioButtonPanel').add(radioBt);
		

	}
});

/*
 * 
 * layout : { type : 'vbox', // width : '200', // height : '200', align :
 * 'stretch', pack : 'center', }, defaults : {
 * 
 * margin : 5, type : 'fit' }, config : { // styleHtmlContent : true, items : [ {
 * docked : 'top', xtype : 'toolbar', itemId : 'toolbar', title : 'title', items : [ {
 * xtype : 'button', ui : 'back', text : 'back' } ] }, { xtype : 'label', html : '<p>DUMMY
 * TEXT</p>', styleHtmlContent : true, }, // { // xtype : 'button', // id :
 * 'prodDetailCardBt', // // iconCls: 'home', // // iconMask: true, // text :
 * 'In den Warenkorb', // }, { xtype : 'spinnerfield', increment : 1, itemdId :
 * 'productSpinner', // minValue : '1', // maxValue : '10', cycle : true,
 * styleHtmlContent : true, }, { xtype : 'panel', layout : { type : 'vbox',
 * width : '150', height : '200' }, itemId : 'choicesPanel' } ] }
 * 
 */