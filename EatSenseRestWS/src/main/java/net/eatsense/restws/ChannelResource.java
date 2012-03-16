package net.eatsense.restws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.google.inject.Inject;

import net.eatsense.controller.ChannelController;

@Path("_ah/channel")
public class ChannelResource {
	private ChannelController channelCtrl;
	
	@Inject
	public ChannelResource( ChannelController channelCtrl) {
		super();
		this.channelCtrl = channelCtrl;
	}

	@POST
	@Path("disconnected")
	public void handleDisconnected(@Context HttpServletRequest request) {
		channelCtrl.handleDisconnected(request);
	}
	
	@POST
	@Path("connected")
	public void handleconnected(@Context HttpServletRequest request) {
		channelCtrl.handleConnected(request);
	}
	
}
