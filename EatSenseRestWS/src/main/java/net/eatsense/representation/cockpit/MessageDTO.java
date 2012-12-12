package net.eatsense.representation.cockpit;

import com.google.common.base.Objects;

public class MessageDTO {
	String type;
	String action;
	Object content;
	private boolean silent;
	
	/**
	 * 
	 */
	public MessageDTO() {
		super();
	}
	
	/**
	 * @param type
	 * @param action
	 * @param content
	 */
	public MessageDTO(String type, String action, Object content) {
		super();
		this.type = type;
		this.action = action;
		this.content = content;
		this.silent = false;
	}
	
	/**
	 * @param type
	 * @param action
	 * @param content
	 * @param silent
	 */
	public MessageDTO(String type, String action, Object content, boolean silent) {
		this(type, action, content);
		this.silent = silent;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("type", type)
				.add("action", action)
				.add("content", content)
				.toString();
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}
}
