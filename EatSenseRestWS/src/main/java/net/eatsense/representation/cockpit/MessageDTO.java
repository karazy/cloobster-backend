package net.eatsense.representation.cockpit;

public class MessageDTO {
	String type;
	String action;
	Object content;
	
	
	
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
}
