package net.eatsense.controller;

import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.AccountDTO;

public class AccountController {
	private AccountRepository accountRepo;
	
	public AccountController(AccountRepository accountRepo) {
		super();
		this.accountRepo = accountRepo;
	}
	
	public AccountDTO authenticate(String login, String password) {
		AccountDTO accountData = new AccountDTO();
		accountRepo.getByProperty("login", login);
		return accountData;
	}
}
