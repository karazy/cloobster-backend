package net.eatsense.persistence;

import org.mindrot.jbcrypt.BCrypt;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;

public class AccountRepository extends GenericRepository<Account> {

	public AccountRepository() {
		super();
		super.clazz = Account.class;
	}
	
	public Account createAndSaveAccount(String login, String password, String email, String role) {
		Account account = new Account();
		account.setLogin(login);
		account.setEmail(email);
		account.setRole(role);
		
		account.setHashedPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		
		if(saveOrUpdate(account) == null)
			return null;
		
		return account;
	}
}
