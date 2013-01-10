package net.eatsense.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.googlecode.objectify.Key;

import net.eatsense.domain.Business;
import net.eatsense.domain.InfoPage;
import net.eatsense.localization.LocalizationProvider;
import net.eatsense.persistence.InfoPageRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.InfoPageDTO;

public class InfoPageGenerator {
	private final LocalizationProvider localizationProvider;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public static String[] countryList=new String[]{"Abkhazia","Afghanistan","Akrotiri and Dhekelia","Åland Islands","Albania","Algeria","American Samoa","Andorra","Angola","Anguilla",
		"Antigua and Barbuda","Argentina ","Armenia ","Aruba","Ascension Island",
		"Australia","Austria","Azerbaijan","Bahamas","Bahrain","Bangladesh","Barbados",
		"Belarus","Belgium","Belize","Benin ","Bermuda","Bhutan","Bolivia"," Bosnia","Botswana","Brazil",
		"Brunei"," Bulgaria","Burkina Faso","Burundi","Cambodia","Cameroon","Canada","Cape Verde",
		"Cayman Islands","Central African Republic","Chad","Chile","China","ChristmasIsland",
		"Cocos","Colombia","Comoros","Congo","Cook Islands","Costa Rica","Côte d'Ivoire",
		"Croatia","Cuba","Cyprus","Czech","Denmark","Djibouti","Dominica","Ecuador","Egypt",
		"El Salvador","Equatorial Guinea","Eritrea","Estonia","Ethiopia","Falkland Islands",
		"Faroe Islands","Fiji","Finland","France","Gabon","Gambia","Georgia","Germany",
		"Ghana","Gibraltar","Greece","Greenland","Grenada","Guam","Guatemala","Guernsey","Guinea",
		"Guinea-Bissau","Guyana","Haiti","Honduras","Hong Kong","Hungary","Iceland","India","Indonesia",
		"Iran","Iraq","Ireland","Isle of Man","Israel","Italy","Jamaica","Japan","Jersey",
		"Jordan","Kazakhstan","Kenya","Kiribati","Korea","Kosovo","Kuwait","Kyrgyzstan","Laos","Latvia",
		"Lebanon","Lesotho","Liberia","Libya","Liechtenstein","Lithuania","Luxembourg","Macao",
		"Macedonia","Madagascar","Malawi","Malaysia","Maldives","Mali","Malta","Marshall Islands",
		"Mauritania","Mauritius","Mayotte","Mexico","Micronesia","Moldova","Monaco","Mongolia",
		"Montenegro","Montserrat","Morocco","Mozambique","Myanmar","Nagorno-Karabakh","Namibia","Nauru",
		"Nepal","Netherlands","New Caledonia","New Zealand","Nicaragua","Niger","Nigeria","Niue","Norfolk Island",
		"Norway","Oman","Pakistan","Palau","Palestine","Panama","Papua New Guinea","Paraguay","Peru",
		"Philippines","Pitcairn","Poland","Portugal","Pridnestrovie","Puerto Rico","Qatar",
		"Romania","Russia","Rwanda","Saint-Barthélemy","Saint Helena","Saint Kitts and Nevis",
		"Saint Lucia","Saint Martin","Saint-Pierre and	Miquelon",
		"Saint Vincent and the Grenadines","Samoa","San Marino","São Tomé and Príncipe",
		"Saudi Arabia","Senegal","Serbia","Seychelles","Sierra Leone","Singapore","Slovakia",
		"Slovenia","Solomon	Islands","Somalia","Somaliland","South Africa","South Ossetia",
		"Spain","SriLanka","Sudan","Suriname","Svalbard","Swaziland","Sweden",
		"Switzerland","Syria","Taiwan","Tajikistan","Tanzania","Thailand","Timor",
		"Togo","Tokelau","Tonga","Trinidad and Tobago","Tristan da Cunha",
		"Tunisia","Turkey","Turkmenistan","Turks and Caicos","Tuvalu","Uganda",
		"Ukraine","United Arab Emirates","United Kingdom","United States",
		"Uruguay","Uzbekistan","Vanuatu","Vatican City","Venezuela","Vietnam",
		"Virgin Islands","Wallis and Futuna","Western Sahara","Yemen",
		"Zambia","Zimbabwe"};
	
	public final static String HTML = "<h2>Lorem</h2><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sed nulla in tellus mollis eleifend a quis risus. Sed nibh magna, blandit vel molestie at, adipiscing fringilla justo. Pellentesque molestie euismod ante vitae vestibulum. In lobortis suscipit quam ut interdum. Nunc vitae ligula purus, euismod faucibus nibh. Nam et arcu ut ligula viverra consectetur. Pellentesque cursus dui dapibus purus vestibulum lacinia. Aliquam gravida mollis est sit amet malesuada. Etiam id tortor at leo dignissim placerat. Nulla vel massa arcu, vitae volutpat libero. Sed id magna quam. Pellentesque sit amet nisi non libero pretium euismod quis vitae arcu. Integer porta, enim sed mollis iaculis, felis velit accumsan lacus, sed laoreet orci tellus eu tellus. Nunc pretium fringilla ornare.</p>"+
		"<h2>Ipsum</h2><p>Curabitur blandit auctor augue, ac eleifend turpis accumsan eu. Phasellus lacus quam, vestibulum et condimentum in, vestibulum mollis orci. Nam tellus est, lobortis at pharetra et, tincidunt quis metus. Cras vestibulum turpis nec nisl mollis ultrices. Pellentesque vitae libero eros, tincidunt suscipit tellus. Sed rutrum consequat pharetra. Aenean nunc erat, egestas ut ullamcorper in, placerat et libero. Curabitur a felis sed lorem porttitor semper. Morbi neque magna, sagittis in elementum id, gravida vitae libero. Proin id ipsum lacinia lorem vulputate gravida sed sit amet augue.</p>"+
		"<h2>Dolor</h2><p>Nunc blandit dolor ac sapien varius cursus. Nunc sit amet vehicula est. Mauris ac ligula mollis lorem mollis porttitor. Praesent in lacus elit. In hac habitasse platea dictumst. Nullam nec purus lorem, vitae consectetur velit. Cras fringilla, nisl eu mollis semper, tellus nisi tempus ipsum, non iaculis dui sapien ac leo.</p>";
	public final static String HTML_EN = "<h2>PUCK</h2><blockquote><A NAME=2.1.18>The king doth keep his revels here to-night:</A><br><A NAME=2.1.19>Take heed the queen come not within his sight;</A><br><A NAME=2.1.20>For Oberon is passing fell and wrath,</A><br><A NAME=2.1.21>Because that she as her attendant hath</A><br><A NAME=2.1.22>A lovely boy, stolen from an Indian king;</A><br><A NAME=2.1.23>She never had so sweet a changeling;</A><br><A NAME=2.1.24>And jealous Oberon would have the child</A><br><A NAME=2.1.25>Knight of his train, to trace the forests wild;</A><br><A NAME=2.1.26>But she perforce withholds the loved boy,</A><br><A NAME=2.1.27>Crowns him with flowers and makes him all her joy:</A><br><A NAME=2.1.28>And now they never meet in grove or green,</A><br><A NAME=2.1.29>By fountain clear, or spangled starlight sheen,</A><br><A NAME=2.1.30>But, they do square, that all their elves for fear</A><br><A NAME=2.1.31>Creep into acorn-cups and hide them there.</A><br></blockquote>";
	public final static String SHORT_TEXT_EN = "Excerpt from Midsummer Night's Dream";
	public final static String SHORT_TEXT ="Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer sed nulla in tellus mollis eleifend a quis risus. Sed nibh magna, blandit vel molestie at, adipiscing fringilla justo.";
	
	public static final String IMAGEURL = "http://robohash.org/";
	private final InfoPageRepository repo;

	@Inject
	public InfoPageGenerator(InfoPageRepository repo, LocalizationProvider localizationProvider) {
		super();
		this.repo = repo;
		this.localizationProvider = localizationProvider;
	}
	
	public List<InfoPageDTO> generate(Key<Business> businessKey, int count , Locale localeOverride) {
		
		List<InfoPageDTO> infoPageDtos = new ArrayList<InfoPageDTO>();
		List<InfoPage> infoPages = new ArrayList<InfoPage>();
		for (int i = 0; i < count; i++) {
			InfoPage infoPage = new InfoPage();
			infoPage.setBusiness(businessKey);
			infoPage.setHtml(HTML);
			infoPage.setShortText(SHORT_TEXT);
			infoPage.setTitle(countryList[i % countryList.length]);
			
			ImageDTO imageDTO = new ImageDTO();
			infoPage.setImages(new ArrayList<ImageDTO>());
			imageDTO.setId("image");
			imageDTO.setUrl(IMAGEURL + infoPage.getTitle());
			infoPage.getImages().add(imageDTO);
			
			infoPages.add(infoPage);
			infoPageDtos.add(new InfoPageDTO(infoPage));
		}
		repo.saveOrUpdate(infoPages);
		
		Locale locale = localeOverride != null ? localeOverride : localizationProvider.getContentLanguage();		
		if(locale != null) {
			logger.info("Generating translation, lang={}", locale.getLanguage());
			for (InfoPage infoPage : infoPages) {
				infoPage.setTitle(infoPage.getTitle() + " " + locale);
				infoPage.setShortText("Lang: "+ locale + SHORT_TEXT_EN);
				infoPage.setHtml(HTML_EN);
				repo.saveOrUpdateTranslation(infoPage, locale);
			}
		}
		
		return infoPageDtos;
	}
}
