package net.eatsense.controller;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.mindrot.jbcrypt.BCrypt;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.Responses;

import net.eatsense.domain.Account;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.AccountDTO;

public class AccountController {
	private AccountRepository accountRepo;
	
	@Inject
	public AccountController(AccountRepository accountRepo) {
		super();
		this.accountRepo = accountRepo;
	}
	
	public Account authenticateHashed(String login, String hashedPassword) {	
		Account account = accountRepo.getByProperty("login", login);
		if(account == null)
			return null;
		
		if( account.getHashedPassword().equals(hashedPassword) ) {
			return account;
		}			
		else {
			return null;
		}
			
	}
	
	public Account authenticate(String login, String password) {	
		Account account = accountRepo.getByProperty("login", login);
		if(account == null)
			return null;
		
		if( BCrypt.checkpw(password, account.getHashedPassword() )) {
			return account;
		}			
		else {
			return null;
		}
			
	}
	public Account createAndSaveAccount(String login, String password, String email, String role) {
		return accountRepo.createAndSaveAccount(login, password, email, role);
	}
	
	public AccountDTO getAccount(String login, String password) {
		Account account = authenticate(login, password);
		if(account == null)
			return null;
		AccountDTO accountData = new AccountDTO();
		accountData.setLogin(account.getLogin());
		accountData.setRole(account.getRole());
		accountData.setEmail(account.getEmail());
		accountData.setPasswordHash(account.getHashedPassword());
		return accountData;
	}
}
