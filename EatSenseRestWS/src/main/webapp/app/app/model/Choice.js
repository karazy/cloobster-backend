/**
 * A choice a user must/can meet to order the
 * product this choice is assigned to. 
 */
Ext.define('EatSense.model.Choice', {
	extend: 'Ext.data.Model',
	config : {
		idProperty: 'id',
		fields: [
		    {name: 'id', type: 'string'},
			{name: 'text', type: 'string'},
			{name: 'minOccurence', type: 'number'},
			{name: 'maxOccurence', type: 'number'},
			{name: 'price', type: 'number'},
			{name: 'included', type: 'number'},
			{name: 'overridePrice', type: 'string'},
		],
		hasMany : {
			model : 'EatSense.model.Option',
			name : 'options'
		}
	},	
	/**
	 * Validates the choice based on min- maxOccurence etc.
	 */
	validateChoice: function() {
		console.log('validate choice '+this.get('text'));
		
		//implement
		//return error message;
		var counter = 0, validationError = "";
		this.options().each(function(option) {
			if(option.get('selected') === true) {
				counter ++;
			}
		});
		if(this.get('minOccurence') <= 1 && this.get('maxOccurence') == 1 && counter != 1) {
			//radio button mandatory field
			validationError += "Bitte triff eine Wahl für "+this.get('text')+ "<br/>";
		}
		else if(counter < this.get('minOccurence')) {
			validationError += "Bitte wähle mindestens " + this.get('minOccurence') + " "+this.get('text')+ " aus. <br/>";
		}else if(counter > this.get('maxOccurence')) {
			validationError += "Du kannst maximal " + this.get('maxOccurence') + " "+this.get('text')+" auswählen. <br/>";
		}
		return (validationError.toString().length == 0) ? true : false;
	},
	/**
	 * Caluclates the price for this choice.
	 */
	calculate: function() {
		var calculationFunction;
		switch (this.get('overridePrice')) {
		case 'NONE':
			return this.calcNormal(); 
			break;
		case 'OVERRIDE_SINGLE_PRICE':
			return this.calcOverrideSinglePrice();
			break;
		case 'OVERRIDE_FIXED_SUM':
			return this.calcOverrideFixedSum();
			break;

		default: 
			return 0;
			break;
		}
	},
	
	calcNormal : function() {
		var  _total = 0, _included = this.get('included'), _count = 0;
		this.options().each(function(option, index) {
			if(option.get('selected') === true) {
				_count++;				
				if(_count > _included) {
					_total += parseFloat(option.data.price);
				}
			}
		});
		
		return _total;
	},
	calcOverrideSinglePrice : function() {
		var _price = this.get('price'), _total = 0, _included = this.get('included'), _count = 0;
		this.options().each(function(option, index) {
			if(option.get('selected') === true) {
				_count++;
				if(_count > _included) {
					_total += parseFloat(_price);
				}				
			}
		});
		
		return _total;
	},
	calcOverrideFixedSum: function() {		
		return this.get('price');
	},
	/**
	 * Returns true if any option of this particular choice is selected.
	 */
	hasSelections: function() {
		var _result = false;
		this.options().each(function(option) {
			if(option.get('selected') === true) {
				_result = true;
			}
		});
		return _result;
	},
	/**
	 * Sets selected status of all options back to false.
	 */
	resetOptions: function() {
		this.options().each(function(option) {
			option.set('selected', false);
		});
	}

});