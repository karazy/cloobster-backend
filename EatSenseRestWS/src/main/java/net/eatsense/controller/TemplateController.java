package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import net.eatsense.templates.Template;
import net.eatsense.templates.TemplateRepository;

/**
 * Loads templates and does the string substitution.
 * 
 * @author Nils Weiher
 *
 */
public class TemplateController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	TemplateRepository templateRepo;

	@Inject
	public TemplateController(TemplateRepository templateRepo) {
		super();
		this.templateRepo = templateRepo;
	}
	
	/**
	 * 
	 * 
	 * @param templates Identifier for the templates created in the datastore.
	 * @return The list of new Template entities.
	 */
	public List<Template> initTemplates(String ... templates) {
		checkArgument(templates.length > 0, "templates must at least have one element");
		
		ArrayList<Template> templateList = new ArrayList<Template>();
		
		for (String templateId : templates) {
			templateList.add(new Template(templateId));
		}
		
		templateRepo.saveOrUpdate(templateList);
		
		return templateList;
	}
	
	/**
	 * @return List of Template entities.
	 */
	public List<Template> getTemplates() {
		return templateRepo.getAll();
	}
	
	public Template saveOrUpdateTemplate(Template template) {
		checkNotNull(template, "template was null");
		checkArgument(!Strings.isNullOrEmpty(template.getId()), "template id was null or empty");
		templateRepo.saveOrUpdate(template);
		return template;
	}
	
	public String replace(Template template, String... substitutions) {
		checkNotNull(template , "template was null");
		checkArgument(!Strings.isNullOrEmpty(template.getTemplateText()), "templateText was null or empty");
		
		Pattern pattern = Pattern.compile("\\{(\\d+?)\\}");
        Matcher matcher = pattern.matcher(template.getTemplateText());
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
        	String replacement = null;
        	
        	try {
				replacement = substitutions[ Integer.parseInt(matcher.group(1)) ];
			}
        	catch(NumberFormatException e) {
        		logger.warn("invalid template {}, expected decimal between 0 and ", template.getId());
        	}
        	catch (ArrayIndexOutOfBoundsException e) {
				logger.warn("not enough substitution given for template: {}", template.getId());
				matcher.appendTail(buffer);
		        return buffer.toString();
			}
        	
            if (replacement != null) {
                matcher.appendReplacement(buffer, "");
                buffer.append(replacement);
            }
        }
        matcher.appendTail(buffer);
        
        return buffer.toString();
	}
	
	/**
	 * @param id Unique identifier of the Template entity to use.
	 * @param templateIndex Can be 0 or higher, dependent on if the Template has more than one text.
	 * @param substitutions Should match the amount of substitutions needed for the text.
	 * @return Template text with the substituted strings.
	 */
	public String getAndReplace(String id, String... substitutions) {
		checkArgument(!Strings.isNullOrEmpty(id), "id was null or empty");
		
		Template template = templateRepo.getById(id);
		if(template == null) {
			return null;
		}
		
        return replace(template, substitutions);
	}
}
