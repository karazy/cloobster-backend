package net.eatsense.restws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import net.eatsense.util.NicknameGenerator;

import com.google.inject.Inject;

@Path("nicknames")
public class NicknameResource {
	private NicknameGenerator nicknameGenerator;

	@Inject
	public NicknameResource(NicknameGenerator nnGen) {
		this.nicknameGenerator = nnGen;
	}
	
	@GET	
	@Produces	
	public String generateNickname(@QueryParam("random") String random) {
		String nickname = "";
		if(random != null) {
			nickname = nicknameGenerator.generateNickname(); 
		}
		return nickname;
	}
}
