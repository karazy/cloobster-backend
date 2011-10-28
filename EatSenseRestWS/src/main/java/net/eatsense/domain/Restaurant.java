package net.eatsense.domain;

import java.util.List;

import com.vercer.engine.persist.annotation.Child;
import com.vercer.engine.persist.annotation.Key;
import com.vercer.engine.persist.annotation.Parent;


public class Restaurant {
	
	@Key
	private Long id;
	
	private String name;
	
	@Child
	private List<Area> areas;

}
