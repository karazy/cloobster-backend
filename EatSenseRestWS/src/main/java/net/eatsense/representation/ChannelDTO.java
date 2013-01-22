package net.eatsense.representation;

import java.util.Date;

import com.google.common.base.Function;

import net.eatsense.domain.Channel;

public class ChannelDTO {

	private String id;
	private Date lastOnlineCheck;
	private String lastChannelId;
	private Date creationTime;
	private int channelCount;
	private long locationId;
	private String locationName;
	private long accountId;
	
	public ChannelDTO() {
	}
	
	public ChannelDTO(Channel channel) {
		this.id = channel.getClientId();
		this.accountId = channel.getAccount().getId();
		this.locationId = channel.getBusiness().getId();
		this.locationName = channel.getLocationName();
		this.channelCount =  channel.getChannelCount();
		this.creationTime = channel.getCreationTime();
		this.lastChannelId = channel.getLastChannelId();
		this.lastOnlineCheck = channel.getLastOnlineCheck();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getLastOnlineCheck() {
		return lastOnlineCheck;
	}

	public void setLastOnlineCheck(Date lastOnlineCheck) {
		this.lastOnlineCheck = lastOnlineCheck;
	}

	public String getLastChannelId() {
		return lastChannelId;
	}

	public void setLastChannelId(String lastChannelId) {
		this.lastChannelId = lastChannelId;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}

	public long getAccountId() {
		return accountId;
	}

	public void setAccountId(long accountId) {
		this.accountId = accountId;
	}

	public long getLocationId() {
		return locationId;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public final static Function<Channel, ChannelDTO> toDTO = 
			new Function<Channel, ChannelDTO>() {
				@Override
				public ChannelDTO apply(Channel input) {
					return new ChannelDTO(input);
				}
		    };
}
