Ext.define('EatSense.model.Product', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id',
			type : 'string'
		}, {
			name : 'name',
			type : 'string'
		}, {
			name : 'shortDesc',
			type : 'string'
		}, {
			name : 'longDesc',
			type : 'string'
		}, {
			name : 'price',
			type : 'string'
		} ],
		hasMany : {
			model : 'EatSense.model.Choice',
			name : 'choices'
		}
	},
	
	validate: function() {
		
	},
	
	calculate: function() {
		
	}
});