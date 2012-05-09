	Ext.Loader.setConfig({
		enabled : true
	});
	Ext.application({
		name : 'Scrollable Toolbar',
		controllers : [  ],
		models : [ ],
		init : function() {
		},
		launch : function() {
			console.log('launch');
			var main = Ext.create('Ext.Panel', {
				fullscreen: true,
				// height: '1024px',
				
				items: [
				{
					xtype: 'toolbar',
					scrollable: true,
					items: [
					{
						text: 'Salate',
						badgeText: '5',
						id: 'one'
					},
					{
						text: 'Rindfleischburger',
						badgeText: '1'
					},
					{
						text: 'Vegetarische Burger'
					},
					{
						text: 'Fischburger'
					},
					{
						text: 'Fingerfood'
					},
					{
						text: 'Getränke'
					},
					{
						text: 'eight'
					}, {
						text: 'nine'
					}, {
						text: 'ten'
					}]

				}, {
					xtype: 'toolbar',
					docked: 'bottom',
					zIndex: 2,
					height: 50
				}]
			});

			var info = Ext.create('Ext.Panel', {
				height: '150px',
				width: 200,
				defaults: {
					xtype: 'button'
				},
				items: [
				{ xtype: 'button', text: 'Call'},
				{ text:'Order'},
				{ text: 'Pay'}]
			});

			info.showBy(main.down('#one'));

			var drag = Ext.create('Ext.Container', {
				draggable: {
                    direction: 'vertical',            
                    initialOffset: {x: 0, y: 500}
        		},
        		height: 250,
        		width: 100,
        		right: 200,
        		bottom: 0,
        		zIndex: 1,

        		style: 'background-color: red; text-align: center;',
        		// styleHtmlContent: true,
        		html: '<h2>Cart</h2>',
        		items: [ 
        		]
			});
			Ext.Viewport.add(drag);

		}
	});