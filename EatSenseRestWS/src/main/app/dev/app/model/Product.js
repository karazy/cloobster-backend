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
	            associationKey: 'choices', // read child data from child_groups,
	            store: {
	            	sorters: [
	            	{
	            		sorterFn: function(record1, record2){
	            			// console.log('sort choices id1: %s parent1: %s and id2: %s parent2: %s', record1.get('id'), record1.get('parent'), record2.get('id'), record2.get('parent'));
	            			var parent1 = record1.get('parent'),
	            				parent2 = record1.get('parent');
	            			if(parent1) {
	            				if(parent1 == record2.get('id')) {
	            					return 1;
	            				}
	            			}

	            			if(parent2) {
	            				if(parent2 == record1.get('id')) {
	            					return 1;
	            				}
	            			}

	            			return 0;
	            		}
	            	}]
	            }
	    }],
	    proxy: {
		   type: 'rest',
		   url : '/c/businesses/{pathId}/products',
		   enablePagingParams: false,
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
		var rawJson = {},
			choicesCount = this.choices().getCount(),
			index = 0;
		
		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.name = this.get('name');
		rawJson.shortDesc = this.get('shortDesc');
		rawJson.longDesc = this.get('longDesc');
		rawJson.price = this.get('price');
		
		rawJson.choices = new Array(this.choices().data.length);
		
		for( ; index < choicesCount; index++) {
			rawJson.choices[index] = this.choices().getAt(index).getRawJsonData();
		}		
		return rawJson;
	},
	/**
	*	Sets the data of this object based on a raw json object.
	*	
	*/	
	setRawJsonData: function(rawData) {
		var index = 0,
			choicesCount = rawData.choices.length,
			choice;

		if(!rawData) {
			return false;
		}

		for( ; index < choicesCount; index++) {
			//check if an option with given id exists
			choice = this.choices().getById(rawData.choices[index].id);
			if(choice) {
				console.log('option with id ' + rawData.choices[index].id);
				if(!choice.setRawJsonData(rawData.choices[index])) {
					return false;
				}
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