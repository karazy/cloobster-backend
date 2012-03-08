package net.eatsense.controller;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.mindrot.jbcrypt.BCrypt;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;

import net.eatsense.domain.Account;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.AccountDTO;

public class AccountController {
	private AccountRepository accountRepo;
	
	public AccountController(AccountRepository accountRepo) {
		super();
		this.accountRepo = accountRepo;
	}
	
	public Account authenticate(String login, String password) {	
		Account account = accountRepo.getByProperty("login", login);
		if(account == null)
			throw new NotFoundException("invalid login data");
		
		if( BCrypt.checkpw(password, account.getHashedPassword() )) {
			return account;
		}			
		else {
			throw new NotFoundException("invalid login data");
		}
			
	}
	
	public AccountDTO getAccount(String login, String password) {
		Account account = authenticate(login, password);
		AccountDTO accountData = new AccountDTO();
		accountData.setLogin(account.getLogin());
		accountData.setRole(account.getRole());
		accountData.setEmail(account.getEmail());
		accountData.setPasswordHash(account.getHashedPassword());
		return accountData;
	}
}
