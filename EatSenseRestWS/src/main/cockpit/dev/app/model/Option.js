Ext.define('EatSense.model.Option', {
	extend : 'Ext.data.Model',
	config : {
		fields : [
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
		rawJson.name = this.get('name');
		rawJson.price = this.get('price');
		rawJson.selected = this.get('selected');

		return rawJson;
	},
	/**
	*	Sets the data of this object based on a raw json object.
	*
	*/	
	setRawJsonData: function(rawData) {
		if(!rawData) {
			return false;
		}
		
		this.set('name', rawData.name);
		this.set('price', rawData.price);
		this.set('selected', rawData.selected);	

		return true;		
	}
});