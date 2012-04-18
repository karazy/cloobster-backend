/**
 * A choice a user must/can meet to order the
 * product this choice is assigned to. 
 */
Ext.define('EatSense.model.Choice', {
	extend: 'Ext.data.Model',
	config : {
		idProperty: 'id',
		fields: [
			{
				name : 'genuineId',
				type : 'string'
			},
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
			validationError += "Bitte triff eine Wahl für "+this.get('text')+ "<br/>";
		}
		else if(counter < minOccurence) {
			validationError += "Bitte wähle mindestens " + minOccurence + " "+this.get('text')+ " aus. <br/>";
		}else if(counter > maxOccurence && maxOccurence > 0) {
			validationError += "Du kannst maximal " + maxOccurence + " "+this.get('text')+" auswählen. <br/>";
		}
		return (validationError.toString().length == 0) ? true : false;
	},
	/**
	*	Validates choice based on given options which are not yet set. 
	*
	*/
	preValidateChoice: function(options) {
		var counter = 0, validationError = "";
		Ext.each(options, function(o) {
			if(option.get('selected') === true) {
				counter ++;
			}
		});

		if(this.get('minOccurence') == 1 && this.get('maxOccurence') == 1 && counter != 1) {
			//radio button mandatory field
			validationError += "Bitte triff eine Wahl für "+this.get('text')+ "<br/>";
		}
		else if(counter < this.get('minOccurence')) {
			validationError += "Bitte wähle mindestens " + this.get('minOccurence') + " "+this.get('text')+ " aus. <br/>";
		}else if(counter > this.get('maxOccurence') && this.get('maxOccurence') > 0) {
			validationError += "Du kannst maximal " + this.get('maxOccurence') + " "+this.get('text')+" auswählen. <br/>";
		}
		return (validationError.toString().length == 0) ? true : false;
	},
	setSelectedOptions: function(options) {
		if(options.length != this.options().getCount()) {
			return;
		}

		Ext.Array.each(options, function(selected, index) {
			this.options().getAt(index).set('selected', selected);
		});

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
	
	getRawJsonData: function() {
		var rawJson = {};		
		
		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.text = this.get('text');
		rawJson.maxOccurence = this.get('maxOccurence');
		rawJson.minOccurence = this.get('minOccurence');
		rawJson.price = this.get('price');
		rawJson.included = this.get('included');
		rawJson.overridePrice = this.get('overridePrice');
		
		rawJson.options = new Array(this.options().data.length);
		for ( var int = 0; int < this.options().data.length; int++) {
			rawJson.options[int] = this.options().getAt(int).getRawJsonData();
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

		for ( var int = 0; int < this.options().data.length; int++) {
			if(!this.options().getAt(int).setRawJsonData(rawData.options[int])) {
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

		return true;			
	}

});