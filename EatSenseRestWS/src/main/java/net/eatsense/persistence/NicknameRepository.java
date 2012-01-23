package net.eatsense.persistence;

import net.eatsense.domain.Nickname;

public class NicknameRepository extends GenericRepository<Nickname> {
	
	
	
	public NicknameRepository() {
		super();
		this.clazz = Nickname.class;
	}

}
