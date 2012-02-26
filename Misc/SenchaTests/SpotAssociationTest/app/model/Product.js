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
			type : 'number'
		}, { //dont change, gets set automatically
			name: 'price_calculated',
			type: 'number'
		}],
		hasMany : {
			model : 'EatSense.model.Choice',
			name : 'choices'
		},
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
		_total = Math.round(_total*_amount*100)/100;
		this.set('price_calculated', _total);
		return _total;
	},
	/**
	 * Returns a deep copy of this product containing only data.
	 */
	deepCopy: function() {
		console.log('Product -> deepCopy')
		var _productCopy, _choiceCopy, _optionCopy;
		_productCopy = Ext.create('EatSense.model.Product', {
//			id: this.get('id'),
			name: this.get('name'),
			shortDesc: this.get('shortDesc'),
			longDesc: this.get('longDesc'),
			price: this.get('price')
		});

		this.choices().each(function(choice) {
			_choiceCopy = Ext.create('EatSense.model.Choice', {
				text: choice.get('text'),
				price: choice.get('price'),
				minOccurence: choice.get('minOccurence'),
				maxOccurence: choice.get('maxOccurence'),
				included: choice.get('included'),
				overridePrice: choice.get('overridePrice')
			});
			choice.options().each(function(option) {
				_optionCopy = Ext.create('EatSense.model.Option', {
					name : option.get('name'),
					price : option.get('price'),
					selected : option.get('selected'),
				});
				_choiceCopy.options().add(option.copy());
			});
			_productCopy.choices().add(_choiceCopy);
		});
		return _productCopy;
	},
	
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
	}
});