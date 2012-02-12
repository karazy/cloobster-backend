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
			type : 'number'
		}],
		hasMany : {
			model : 'EatSense.model.Choice',
			name : 'choices'
		},
//		proxy: {
//			type: 'rest',
//			url: globalConf.serviceUrl+'/restaurants/products/', 
//			reader: {
//				type: 'json',
//			} 
//		}
	},
	
	validate: function() {
		
	},
	/**
	 * Calculates total cost of this product including choices, returns it and
	 * stores it in priceCalculated.
	 */
	calculate: function(amount) {
		var _total = this.get('price'), _amount = 1;
		this.choices().each(function(choice, index) {
			_total += choice.calculate();
		});
		if(amount) {
			_amount = amount;
		}
		_total = Math.round(_total*_amount*100)/100;
		return _total;
	},
	/**
	 * Returns a deep copy of this product containing only data.
	 */
	deepCopy: function() {
		var _productCopy, _choiceCopy;
		_productCopy = Ext.create('EatSense.model.Product', {
//			id: this.get('id'),
			name: this.get('name'),
			shortDesc: this.get('shortDesc'),
			longDesc: this.get('longDesc'),
			price: this.get('price')
		});
//		_productCopy.choices().removeAll();
//		_productCopy.data.choices = new Array();
		/*
		 * 	    {name: 'id', type: 'string'},
			{name: 'text', type: 'string'},
			{name: 'minOccurence', type: 'number'},
			{name: 'maxOccurence', type: 'number'},
			{name: 'price', type: 'number'},
			{name: 'included', type: 'number'},
			{name: 'overridePrice', type: 'string'},
		 */
		this.choices().each(function(choice) {
			_choiceCopy = Ext.create('EatSense.model.Choice', {
				text: choice.get('text'),
				price: choice.get('price'),
				overridePrice: choice.get('overridePrice')
			});
//			_choiceCopy.options().removeAll();
//			_choiceCopy.data.options = new Array();
			choice.options().each(function(option) {
				_choiceCopy.options().add(option.copy());
			});
			_productCopy.choices().add(_choiceCopy);
		});
		return _productCopy;
	}
});