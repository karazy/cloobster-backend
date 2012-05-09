/**
 * A choice a user must/can meet to order the
 * product this choice is assigned to. 
 */
Ext.define('EatSense.model.Choice', {
	extend: 'Ext.data.Model',
	config : {
		idProperty: 'id',
		fields: [
			{name : 'genuineId', type : 'string'},
		    {name: 'id', type: 'string'},
			{name: 'text', type: 'string'},
			{name: 'minOccurence', type: 'number'},
			{name: 'maxOccurence', type: 'number'},
			{name: 'price', type: 'number'},
			{name: 'included', type: 'number'},
			{name: 'overridePrice', type: 'string'},
			{name : 'parent', type: 'number'},
			{name : 'active', type: 'boolean', persist: false, defaultValue: false}
		],
		hasMany : {
			model : 'EatSense.model.Option',
			name : 'options'
		},
		associations : {
			type : 'hasOne',
			model : 'EatSense.model.Choice',
			associatedName: 'parentChoice'
		}
	},	
	/**
	 * Validates the choice based on min- maxOccurence etc. 
	 */
	validateChoice: function() {
		console.log('validateChoide ' + this.get('text'));
		var 	counter = 0, 
				validationError = "",
				minOccurence = this.get('minOccurence'),
				maxOccurence = this.get('maxOccurence');

		this.options().each(function(option) {
			if(option.get('selected') === true) {
				counter ++;
			}
		});

		if(minOccurence == 1 && maxOccurence == 1 && counter != 1) {
			//radio button mandatory field
			validationError += Karazy.i18n.translate('choiceValErrMandatory', this.get('text')) + "<br/>";
		}
		else if(counter < minOccurence) {
			validationError += Karazy.i18n.translate('choiceValErrMin', minOccurence, this.get('text')) + "<br/>";
		}else if(counter > maxOccurence && maxOccurence > 0) {
			validationError += Karazy.i18n.translate('choiceValErrMin', maxOccurence, this.get('text')) + "<br/>";
		}
		return (validationError.toString().length == 0) ? true : false;
	},
	/**
	* If a choice has selected options it is considered active.
	* @return 
	* 	true if active
	*/
	isActive: function() {
		var result = false;
		this.options().each(function(option) {
			if(option.get('selected') === true) {
				result = true;
				//stop iteration
				return false;
			}
		});
		this.fireEvent('activeChanged', result);
		return result;
	},
	/**
	 * Caluclates the price for this choice.
	 */
	calculate: function() {
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
		if(this.hasSelections()) {
			return this.get('price');
		}
		return 0;
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
	},
	/**
	*	Returns a raw json representation of this objects data.
	*/
	getRawJsonData: function() {
		var rawJson = {},
			optionsLength = this.options().getCount(),
			index = 0;		
		
		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.text = this.get('text');
		rawJson.maxOccurence = this.get('maxOccurence');
		rawJson.minOccurence = this.get('minOccurence');
		rawJson.price = this.get('price');
		rawJson.included = this.get('included');
		rawJson.overridePrice = this.get('overridePrice');
		// rawJson.active = this.get('active');
		rawJson.parent = this.get('parent');
		
		rawJson.options = new Array(this.options().data.length);
		for ( ; index < optionsLength; index++) {
			rawJson.options[index] = this.options().getAt(index).getRawJsonData();
		}
		return rawJson;
	},
	/**
	*	Sets the data of this object based on a raw json object.
	*
	*/	
	setRawJsonData: function(rawData) {
		var index = 0,
			optionsLength = rawData.options.length,
			option;

		if(!rawData) {
			return false;
		}
		
		for ( ; index < optionsLength; index++) {
			if(!this.options().getAt(index).setRawJsonData(rawData.options[index])) {
				return false;
			}
		}
		
		this.set('id', rawData.id);
		this.set('text', rawData.text);
		this.set('maxOccurence', rawData.maxOccurence);
		this.set('minOccurence', rawData.minOccurence);
		this.set('price', rawData.price);
		this.set('included', rawData.included);
		this.set('overridePrice', rawData.overridePrice);
		// this.set('active', rawData.active);
		this.set('parent', rawData.parent);

		return true;			
	}

});