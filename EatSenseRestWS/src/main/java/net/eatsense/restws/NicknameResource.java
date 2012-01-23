package net.eatsense.restws;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import net.eatsense.domain.Nickname;
import net.eatsense.persistence.NicknameRepository;

import com.google.inject.Inject;

@Path("/nickname")
public class NicknameResource {
	
	private NicknameRepository nnRepo;

	@Inject
	public NicknameResource(NicknameRepository repository) {
		this.nnRepo = repository;
	}
	
	@PUT
	@Path("/list")
	@Consumes("application/json; charset=UTF-8")
	public void addNicknames(List<Nickname> nicknames) {
		nnRepo.ofy().put(nicknames);
	}

}
