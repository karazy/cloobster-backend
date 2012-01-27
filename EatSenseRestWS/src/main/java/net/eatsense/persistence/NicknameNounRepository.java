package net.eatsense.persistence;

import net.eatsense.domain.NicknameAdjective;
import net.eatsense.domain.NicknameNoun;

public class NicknameNounRepository extends GenericRepository<NicknameNoun> {
	
	
	
	public NicknameNounRepository() {
		super();
		this.clazz = NicknameNoun.class;
	}

}
