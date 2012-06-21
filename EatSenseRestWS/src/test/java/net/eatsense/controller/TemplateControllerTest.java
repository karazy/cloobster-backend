package net.eatsense.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import net.eatsense.templates.Template;
import net.eatsense.templates.TemplateRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateControllerTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Mock
	private TemplateRepository templateRepo;
	private TemplateController ctrl;
	private String testTemplateText;
	private Template testTemplate;
	

	@Before
	public void setUp() throws Exception {
		ctrl = new TemplateController(templateRepo);
		
		testTemplate = new Template();
		testTemplateText = "This is a test\n{0}\n{1}";
		testTemplate.setTemplateText(testTemplateText);
		
	}
	
	@Test
	public void testLoadAndReplaceRepeatedOccurence() throws Exception {
		String templateId = "template1";
		testTemplate.setTemplateText("This is a test\n{0}\n{0}");
		when(templateRepo.getById(templateId)).thenReturn(testTemplate);
		String replacedText = ctrl.getAndReplace(templateId , "test1","test2");
		assertThat(replacedText, is("This is a test\ntest1\ntest1"));
	}
	
	@Test
	public void testLoadAndReplace() throws Exception {
		String templateId = "template1";
		when(templateRepo.getById(templateId)).thenReturn(testTemplate);
		String replacedText = ctrl.getAndReplace(templateId , "test1","test2");
		assertThat(replacedText, is("This is a test\ntest1\ntest2"));
	}
	
	@Test
	public void testLoadAndReplaceNotEnoughSubsitutions() throws Exception {
		String templateId = "template1";
		when(templateRepo.getById(templateId)).thenReturn(testTemplate);
		String replacedText = ctrl.getAndReplace(templateId , "test1");
		assertThat(replacedText, is("This is a test\ntest1\n{1}"));
	}
	
	@Test
	public void testLoadAndReplaceNullTemplateText() throws Exception {
		String templateId = "template1";
		testTemplate.setTemplateText(null);
		when(templateRepo.getById(templateId)).thenReturn(testTemplate);
		thrown.expect(IllegalArgumentException.class);
		ctrl.getAndReplace(templateId ,"test1");
		
	}
	
	@Test
	public void testLoadAndReplaceEmptyTemplateText() throws Exception {
		String templateId = "template1";
		testTemplate.setTemplateText("");
		when(templateRepo.getById(templateId)).thenReturn(testTemplate);
		
		thrown.expect(IllegalArgumentException.class);
		ctrl.getAndReplace(templateId , "test1");
	}
	
	@Test
	public void testLoadAndReplaceUnknownId() throws Exception {
		String templateId = "template1";
		
		String replacedText = ctrl.getAndReplace(templateId , "test1");
		assertThat(replacedText, nullValue());
	}
}
