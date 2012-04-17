Ext.define('EatSense.data.proxy.CustomRestProxy', {
	override: 'Ext.data.proxy.Rest',
	   buildUrl: function(request) {		
	        var  me = this, _serviceUrl = Karazy.config.serviceUrl, 
	        	url = me.getUrl(request),
	        	params = request.getParams() || {},
	        	defaultHeaders = Ext.Ajax.getDefaultHeaders() || {};;

	        if(params.pathId) {
	        	if(url.match(/(.*){pathId}(.*)/)) {
	        		var replacer = '$1'+params.pathId+'$2';
	        		url = url.replace(/(.*){pathId}(.*)/, replacer);
	        		delete params.pathId;
	        	}	        	
	        } else if(defaultHeaders.pathId) {
	        	if(url.match(/(.*){pathId}(.*)/)) {
	        		var replacer = '$1'+defaultHeaders.pathId+'$2';
	        		url = url.replace(/(.*){pathId}(.*)/, replacer);
	        	}	
	        }
	        	
	        request.setUrl(_serviceUrl + url);

	        return me.callParent([request]);
	    },
	    
	    doRequest: function(operation, callback, scope) {
	    	  var writer  = this.getWriter(),
	            request = this.buildRequest(operation);

	        request.setConfig({
	            headers        : this.getHeaders(),
	            timeout        : this.getTimeout(),
	            method         : this.getMethod(request),
	            callback       : this.createRequestCallback(request, operation, callback, scope),
	            scope          : this
	        });

	        if (operation.getWithCredentials() || this.getWithCredentials()) {
	            request.setWithCredentials(true);
	        }

	        // We now always have the writer prepare the request
	        request = writer.write(request);

	       
	        
	        if(request.getMethod().toUpperCase() === 'DELETE') {
	        	//prevent Sencha from sending payload to avoid BAD REQUEST on appengine
	        	 delete request._jsonData;	        	
	        }
	        
	        Ext.Ajax.request(request.getCurrentConfig());
	        
	        return request;
	    }
});