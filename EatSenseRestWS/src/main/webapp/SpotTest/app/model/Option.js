Ext.define('EatSense.model.Option', {
	extend : 'Ext.data.Model',
	config : {
//		idProperty : 'id',
		fields : [
//		          {
//			name : 'id',
//			type : 'string'
//		},
		{
			name : 'name',
			type : 'string'
		}, {
			name : 'price',
			type : 'number'
		}, {
			name : 'selected',
			type : 'boolean',
			defaultValue: false
		} ]
	},

	getRawJsonData : function() {
		var rawJson = {};

//		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.name = this.get('name');
		rawJson.price = this.get('price');
		rawJson.selected = this.get('selected');

		return rawJson;
	}
});