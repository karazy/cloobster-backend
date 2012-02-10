package net.eatsense.representation;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;

import net.eatsense.domain.ChoiceOverridePrice;
import net.eatsense.domain.ProductOption;

public class ChoiceDTO {
	Long id;
	
	@NotNull
	@NotEmpty
	String text;
	
	int minOccurence;
	
	int maxOccurence;
	@Min(0)
	float price;
	
	int included;
	
	ChoiceOverridePrice overridePrice;
	
	@NotNull
	@NotEmpty
	@Valid
	Collection<ProductOption> options; 
	
	Collection<ProductOption> selected;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getMinOccurence() {
		return minOccurence;
	}
	public void setMinOccurence(int minOccurence) {
		this.minOccurence = minOccurence;
	}
	public int getMaxOccurence() {
		return maxOccurence;
	}
	public void setMaxOccurence(int maxOccurence) {
		this.maxOccurence = maxOccurence;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public int getIncluded() {
		return included;
	}
	public void setIncluded(int included) {
		this.included = included;
	}
	public ChoiceOverridePrice getOverridePrice() {
		return overridePrice;
	}
	public void setOverridePrice(ChoiceOverridePrice overridePrice) {
		this.overridePrice = overridePrice;
	}
	public Collection<ProductOption> getOptions() {
		return options;
	}
	public void setOptions(Collection<ProductOption> options) {
		this.options = options;
	}
	public Collection<ProductOption> getSelected() {
		return selected;
	}
	public void setSelected(Collection<ProductOption> selected) {
		this.selected = selected;
	}
	
}
