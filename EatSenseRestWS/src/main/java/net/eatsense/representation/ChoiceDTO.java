package net.eatsense.representation;

import java.util.Collection;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import net.eatsense.domain.Choice;
import net.eatsense.domain.OrderChoice;
import net.eatsense.domain.embedded.ChoiceOverridePrice;
import net.eatsense.domain.embedded.ProductOption;
import net.eatsense.validation.ImportChecks;

public class ChoiceDTO {
	Long id;
	
	@NotNull
	@NotEmpty
	String text;
	
	int minOccurence;
	int maxOccurence;
	
	private Long originalChoiceId;
	
	@Min(value=0)
	double price;
	
	int included;
	
	ChoiceOverridePrice overridePrice;
	
	@NotNull
	@NotEmpty
	@Valid
	List<ProductOption> options; 
	
	Collection<ProductOption> selected;
	
	private Integer order;
	
	private Long productId;
	
	public ChoiceDTO(Choice choice) {
		this(choice, null);
	}
	/**
	 * @param choice Entity
	 */
	public ChoiceDTO(Choice choice, Long choiceIdOverride) {
		this();
		
		if(choice == null)
			return;
		
		if(choiceIdOverride != null) {
			this.id = choiceIdOverride;
		}
		else {
			this.id = choice.getId();
		}
		
		this.originalChoiceId = choice.getId();
		this.included = choice.getIncludedChoices();
		this.maxOccurence = choice.getMaxOccurence();
		this.minOccurence = choice.getMinOccurence();
		this.overridePrice = choice.getOverridePrice();
		this.order = choice.getOrder();
		
		this.price = (choice.getPrice() == null ? 0 : choice.getPrice().doubleValue() / 100.0);
		this.text = choice.getText();
		
		if( choice.getOptions() != null && !choice.getOptions().isEmpty() ) {
			int i = 0;
			for (ProductOption option : choice.getOptions()) {
				// Try to set an artificial id for productoptions.
				if(option.getId() == null) {
					option.setId(String.format("%d-%d", this.id, i));
				}
				i++;
			}
			this.options = choice.getOptions();
		}
		
		if(choice.getProduct() != null)
			this.productId = choice.getProduct().getId();
	}
	
	public ChoiceDTO(OrderChoice orderChoice) {
		this(orderChoice.getChoice(), orderChoice.getId());
						
		// Set the id of the orderChoice instead of the original choice, because
		// this was an choice coming from a saved order.
	}
	
	/**
	 * @param choice Entity
	 * @param productId Override for productId in the Choice entity.
	 */
	public ChoiceDTO(Choice choice, long productId) {
		this(choice);
		this.productId = productId;
	}
	
	public ChoiceDTO() {
		super();
	}

	String group;
	
	boolean groupParent = false;
	
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isGroupParent() {
		return groupParent;
	}
	public void setGroupParent(boolean groupParent) {
		this.groupParent = groupParent;
	}
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
	
	@JsonIgnore
	public long getPriceMinor() {
		return Math.round(price * 100);
	}
		
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
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
	public List<ProductOption> getOptions() {
		return options;
	}
	public void setOptions(List<ProductOption> options) {
		this.options = options;
	}
	public Collection<ProductOption> getSelected() {
		return selected;
	}
	public void setSelected(Collection<ProductOption> selected) {
		this.selected = selected;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	/**
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}

	public Long getOriginalChoiceId() {
		return originalChoiceId;
	}

	public void setOriginalChoiceId(Long originalChoiceId) {
		this.originalChoiceId = originalChoiceId;
	}
	
	
	
}
