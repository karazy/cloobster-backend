Ext.define('EatSense.data.proxy.CustomRestProxy', {
	override: 'Ext.data.proxy.Rest',
	  buildUrl: function(request) {		
	        var  me = this, _serviceUrl = globalConf.serviceUrl, 
	        	url = me.getUrl(request),
	        	params = request.getParams() || {};

	        if(params.pathId) {
	        	if(url.match(/(.*){pathId}(.*)/)) {
	        		var replacer = '$1'+params.pathId+'$2';
	        		url = url.replace(/(.*){pathId}(.*)/, replacer);
	        		delete params.pathId;
	        	}	        	
	        }
	        	
	        request.setUrl(_serviceUrl + url);

	        return me.callParent([request]);
	    }
});