package net.eatsense.persistence;

import net.eatsense.domain.NicknameNoun;

public class NicknameNounRepository extends GenericRepository<NicknameNoun> {
	static {
		GenericRepository.register(NicknameNoun.class);
	}	
	
	public NicknameNounRepository() {
		super(NicknameNoun.class);
	}

}
