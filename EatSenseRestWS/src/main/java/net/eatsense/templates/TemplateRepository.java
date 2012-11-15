package net.eatsense.templates;


import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;

public class TemplateRepository extends DAOBase {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	static {
		ObjectifyService.register(Template.class);
	}
	
	public Template getById(String id) {
		try {
			return ofy().get(Template.class, id);
		} catch (NotFoundException e) {
			logger.error("template not found: {}", id);
			return null;
		}
	}
	
	public List<Template> getAll() {
		return ofy().query(Template.class).list();
	}
	
	public Key<Template> saveOrUpdate(Template template) {
		return ofy().put(template);
	}
	
	public Map<Key<Template>, Template> saveOrUpdate(List<Template> template) {
		return ofy().put(template);
	}
}
