package net.eatsense.event;

import net.eatsense.domain.Business;
import net.eatsense.domain.Channel;

public class ChannelOnlineCheckTimeOutEvent {
	private final Channel channel;
	
	public ChannelOnlineCheckTimeOutEvent(Channel channel) {
		super();
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}
}
