Ext.define('EatSense.model.Order', {
	extend : 'Ext.data.Model',
	config : {
		idProperty : 'id',
		fields : [ {
			name : 'id',
			type : 'string'
		}, {
			name : 'status',
			type : 'string'
		}, {
			name : 'amount',
			type : 'number'
		}, {
			name : 'comment',
			type : 'string'
		}, {
			name : 'orderTime',
			type : 'date',
			dateFormat: 'time'
		} ],
		associations : {
			type : 'hasOne',
			model : 'EatSense.model.Product',
		},
		proxy: {
			type: 'rest',
			enablePagingParams: false,
			url : '/c/businesses/{pathId}/orders',
			reader: {
				type: 'json'
		   	}
	 	},
	 	// current state of this order. used for store and restore
	 	state: null
	},

	calculate : function() {
		var _amount = parseFloat(this.get('amount'));
		return this.getProduct().calculate(_amount);
	},
	/**
	*	Saves the state of this order.
	*	The state can be restored after changes.
	*	An existing state will be overriden.
	*/
	saveState: function() {
		if(this.getState()) {
			console.log('override existing order state');
		}

		this.setState(this.getRawJsonData());
	},
	/**
	*	If a state exists it will be restored.
	*/
	restoreState: function() {
		if(this.getState()) {
			this.setRawJsonData(this.getState());
			this.setState(null);
		}
	},
	/**
	*	Returns a deep raw json representation of this object.
	*
	*/	
	getRawJsonData: function() {
		var rawJson = {};
		
		rawJson.id = (this.phantom === true) ? this.get('genuineId') : this.get('id');
		rawJson.status = this.get('status');
		rawJson.amount = this.get('amount');
		rawJson.comment = this.get('comment');
		rawJson.orderTime = this.get('orderTime');
		
		rawJson.product = this.getProduct().getRawJsonData();
		console.log('Order id: ' + this.get('id') +' genuineId: '+this.get('genuineId')+ ' getRawJsonData id: ' + rawJson.id);
		return rawJson;
	},
	/**
	*	Sets the data of this object based on a raw json object.
	*	@param rawData
	*		data to set
	*	@param shallow
	*		don't set nested data
	*/	
	setRawJsonData: function(rawData, shallow) {
		if(!rawData) {
			return false;
		}

		if(!shallow && !this.getProduct().setRawJsonData(rawData.product)) {
			return false;
		}

		this.set('id', rawData.id);
		this.set('status', rawData.status);
		this.set('amount', rawData.amount);
		this.set('comment', rawData.comment);
		this.set('orderTime', rawData.orderTime);

		return true;
		
			
	}

});