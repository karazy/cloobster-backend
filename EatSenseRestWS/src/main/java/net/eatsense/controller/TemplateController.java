package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	public List<Template> initAllTemplate() {
		return initTemplates("account-confirm-email",
				"newsletter-email-registered",
				"account-confirmed",
				"account-forgotpassword-email",
				"account-setup-email",
				"account-notice-password-update",
				"account-confirm-email-update",
				"account-notice-email-update",
				"customer-account-confirm-email",
				"location-upgrade-request",
				"infopage-demo-de.json");
	}
	
	/**
	 * 
	 * 
	 * @param templates Identifier for the templates created in the datastore.
	 * @return The list of new Template entities.
	 */
	public List<Template> initTemplates(String ... templates) {
		//TODO: Add default Templates.
		checkArgument(templates.length > 0, "templates must at least have one element");
		
		ArrayList<Template> templateList = new ArrayList<Template>();
		
		for (String templateId : templates) {
			if(templateRepo.getById(templateId) == null) {
				templateList.add(new Template(templateId));
			}
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
		
		if(substitutions.length == 0) {
			return template.getTemplateText();
		}
		
		Pattern pattern = Pattern.compile("\\{(\\d+?)\\}");
        Matcher matcher = pattern.matcher(template.getTemplateText());
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
        	String replacement = null;
        	
        	try {
				replacement = substitutions[ Integer.parseInt(matcher.group(1)) ];
			}
        	catch(NumberFormatException e) {
        		logger.warn("invalid template {}, expected decimal betwwen '{' and '}' got {}", template.getId(), matcher.group(1));
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
		if(template == null || Strings.isNullOrEmpty(template.getTemplateText())) {
			logger.info("{} Template substitutions supplied: {}" , id, Arrays.toString(substitutions));
			return null;
		}
		
		return replace(template, substitutions);
	}
}
