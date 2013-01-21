package net.eatsense.domain;

import java.util.Date;

import javax.persistence.Id;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;

@Cached
public class Channel {
	@Id
	private String clientId;
	private String lastChannelId;
	private Date creationTime;
	private Date lastOnlineCheck;
	@Parent
	private Key<Business> business;
	private Key<Account> account;
	private boolean warningSent;
	private int channelCount;
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	public Date getLastOnlineCheck() {
		return lastOnlineCheck;
	}
	public void setLastOnlineCheck(Date lastOnlineCheck) {
		this.lastOnlineCheck = lastOnlineCheck;
	}
	public Key<Business> getBusiness() {
		return business;
	}
	public void setBusiness(Key<Business> business) {
		this.business = business;
	}
	public Key<Account> getAccount() {
		return account;
	}
	public void setAccount(Key<Account> account) {
		this.account = account;
	}
	public boolean isWarningSent() {
		return warningSent;
	}
	public void setWarningSent(boolean warningSent) {
		this.warningSent = warningSent;
	}
	public int getChannelCount() {
		return channelCount;
	}
	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}
	public String getLastChannelId() {
		return lastChannelId;
	}
	public void setLastChannelId(String lastChannelId) {
		this.lastChannelId = lastChannelId;
	}
}
