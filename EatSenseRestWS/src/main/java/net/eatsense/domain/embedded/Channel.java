package net.eatsense.domain.embedded;

import java.util.Date;

import com.google.common.base.Objects;

public class Channel {
	private String clientId;
	private Date creationDate;
	
	public Channel(String clientId) {
		this.clientId = clientId;
		this.creationDate = new Date();
	}
	
	public Channel() {
		super();
	}


	public String getClientId() {
		return clientId;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public static Channel fromClientId(String clientId) {
		return new Channel(clientId);
	}
	
		
	
	@Override
	public int hashCode() {
		return Objects.hashCode(clientId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Channel other = (Channel) obj;
		if (clientId == null) {
			if (other.clientId != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass()).
				add("clientId", clientId).
				add("creationDate", creationDate).
				toString();
	}
	
}
