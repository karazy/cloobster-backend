package net.eatsense.persistence;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.googlecode.objectify.Key;

import net.eatsense.controller.AccountController;
import net.eatsense.domain.Account;
import net.eatsense.domain.Restaurant;

public class AccountRepository extends GenericRepository<Account> {

	public AccountRepository() {
		super();
		super.clazz = Account.class;
	}
	
	public Account createAndSaveAccount(String login, String password, String email, String role, List<Key<Restaurant>> restaurantKeys) {
		Account account = new Account();
		account.setLogin(login);
		account.setEmail(email);
		account.setRole(role);
		account.setRestaurants(restaurantKeys);
		
		account.setHashedPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
		
		if(saveOrUpdate(account) == null)
			return null;
		
		return account;
	}
}
