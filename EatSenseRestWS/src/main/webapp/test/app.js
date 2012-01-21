Ext
		.application({
			launch : function() {

				var panel = Ext
						.create(
								'Ext.Panel',
								{
									fullscreen : true,
									id : 'testPanel',
									scrollable : 'vertical',
									layout : {
										type : 'vbox',
//										align : 'middle',
									},
									defaults : {
										styleHtmlContent : false,
									},
									items : [
											{
												docked : 'top',
												xtype : 'toolbar',
												// title : 'Product detail',
												// left : '100',
												items : [
														{
															xtype : 'button',
															text : 'back',
															ui : 'back'
														},
														{
															xtype : 'label',
															docked : 'right',
															html : '<span style="font-size: 2em;color:white;">Product detail</span>',

														} ]

											},
											{
												xtype : 'panel',
												docked : 'top',
												style : 'background-color:#B88A00;border-bottom-left-radius: 10px;border-bottom-right-radius: 10px;padding-bottom: 10em;',

												layout : {
													type : 'vbox',
//													pack : 'center'
												},
												items : [
														{
															xtype : 'label',
															html : '<div style="text-align:left;"><h2>Product Name</h2><p>Detailed product description.</p></div>'
														},
														{
															xtype : 'panel',
															layout : {
																type : 'hbox',
																align : 'strech'
															},
															defaults : {
															},
															items : [
																	{
																		xtype : 'spinnerfield',
																		increment : 1,
																		flex : 3,
																		value : 1,
																		style : 'background-color:white;'
																	},
																	{
																		xytpe : 'sapcer',
																		flex : 1

																	},

																	{
																		xtype : 'button',
																		text : 'Card',
																		flex : 2
																	} ]
														} ]
											},
											{
												xtype : 'panel',
												itemId : 'choicePanel',
												items : [
														{
															xtype : 'label',
															html : '<p>Options panel</p>'
														},
														{
															xtype : 'panel',
															itemId : 'radioButtonPanel',
															layout : {
																type : 'vbox',
															},
															items : [
																	{
																		xtype : 'label',

																		html : '<p>Mandatory options (radio buttons)</p>'
																	},
																	{
																		xtype : 'panel',
																		layout : {
																			type : 'vbox',
																		},
																		defaults : {
																			labelWidth : '50%'
																		},
																		items : [
																				{
																					xtype : 'radiofield',
																					value : '1',
																					name : 'group1',
																					label : 'Radio 1',
																					checked : true
																				},
																				{
																					xtype : 'radiofield',
																					value : '2',
																					name : 'group1',
																					label : 'Radio 2'
																				},
																				{
																					xtype : 'radiofield',
																					value : '3',
																					name : 'group1',
																					label : 'Radio 3'
																				} ]

																	}

															]
														},
														{
															xtype : 'panel',
															items : [
																	{
																		xtype : 'label',
																		html : '<p>Optional options (checkboxes)</p>'
																	},
																	{
																		xtype : 'panel',
																		// height
																		// : 40,
																		layout : {
																			type : 'vbox',
																		// align
																		// :
																		// 'stretch'
																		},
																		items : [
																				{
																					xtype : 'checkboxfield',
																					value : '1',
																					name : 'group2',
																					label : 'Checkbox 1',
																				},
																				{
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
				});

				panel.getComponent('choicePanel').getComponent(
						'radioButtonPanel').add(radioBt);

			}
		});