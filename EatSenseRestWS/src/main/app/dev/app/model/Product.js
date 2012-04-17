Ext.define('EatSense.model.Product', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id',
			type : 'string'
		},
		{
			name : 'genuineId',
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
			type: 'number'
		}, { //dont change, gets set automatically
			name: 'price_calculated',
			persist: false,
			type: 'number'
		}],		
		 associations: [{
	            type: 'hasMany',
	            model: 'EatSense.model.Choice',
	            primaryKey: 'id',
	            name: 'choices',
	            autoLoad: true,
	            associationKey: 'choices' // read child data from child_groups
	    }],
	    proxy: {
		   type: 'rest',
		   url : '/c/businesses/{pathId}/products',
		   reader: {
			   type: 'json'
	   		},
	   		writer: {
	   			type: 'json',
	   			writeAllFields: true
	   		}
	   }
	},
	
	validate: function() {
		
	},
	/**
	 * Calculates total cost of this product including choices, returns it and
	 * stores it in priceCalculated.
	 * @param amount
	 * 		How often this product is ordered.
	 * 
	 */
	calculate: function(amount) {
		var _total = this.get('price'), _amount = 1;
		this.choices().each(function(choice, index) {
			_total += choice.calculate();
		});
		if(amount) {
			_amount = amount;
		}
		// _total = Karazy.util.roundPrice(_total*_amount);
		_total = _total*_amount;
		this.set('price_calculated', _total);
		return _total;
	},
	/**
	*	Returns a deep raw json representation of this product.
	*
	*/	
	getRawJsonData: function() {
		var rawJson = {};
		
		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.name = this.get('name');
		rawJson.shortDesc = this.get('shortDesc');
		rawJson.longDesc = this.get('longDesc');
		rawJson.price = this.get('price');
		
		rawJson.choices = new Array(this.choices().data.length);
		for ( var int = 0; int < this.choices().data.length; int++) {
			rawJson.choices[int] = this.choices().getAt(int).getRawJsonData();
		}		
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

		for ( var int = 0; int < this.choices().data.length; int++) {
			if(!this.choices().getAt(int).setRawJsonData(rawData.choices[int])) {
				return false;
			}
		}

		this.set('id', rawData.id);
		this.set('name', rawData.name);
		this.set('shortDesc', rawData.shortDesc);
		this.set('longDesc', rawData.longDesc);
		this.set('price', rawData.price);
						
		return true;
	}
});