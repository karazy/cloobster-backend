package net.eatsense.representation;

import net.eatsense.domain.Account;

public class CustomerAccountDTO extends AccountDTO {
	private String nickname;
	private String password;
	
	public CustomerAccountDTO() {
		super();
	}

	public CustomerAccountDTO(Account account) {
		super(account);
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
