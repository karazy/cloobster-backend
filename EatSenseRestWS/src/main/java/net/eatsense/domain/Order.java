/**
 * 
 */
package net.eatsense.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import net.eatsense.domain.embedded.OrderStatus;
import net.eatsense.representation.ImageDTO;

import org.apache.bval.constraints.NotEmpty;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

/**
 * @author Nils Weiher
 *
 */
@Cached
public class Order extends GenericEntity<Order> {
	@Min(1)
	int amount;
	private boolean archived;
	
	Key<Bill> bill;
	
	@Parent
	@NotNull
	Key<Business> business;


	@NotNull
	Key<CheckIn> checkIn;
	
	String comment;

	@NotNull
	Date orderTime;
	
	@NotNull
	Key<Product> product;
	
	@NotNull
	OrderStatus status;
	
	@NotNull
	@NotEmpty
	private String productName;
	
	@Unindexed
	private String productShortDesc;
	
	@Unindexed
	private String productLongDesc;
	
	@Min(0)
	private long productPrice;
	
	@Unindexed
	private boolean productSpecial;
	
	@Embedded
	@Unindexed
	private ImageDTO image;
	
	@Unindexed
	private List<Key<OrderChoice>> choices = new ArrayList<Key<OrderChoice>>();

	public int getAmount() {
		return amount;
	}


	public Key<Bill> getBill() {
		return bill;
	}


	public Key<Business> getBusiness() {
		return business;
	}
	
	public Key<CheckIn> getCheckIn() {
		return checkIn;
	}


	public String getComment() {
		return comment;
	}


	@Transient
	@JsonIgnore
	public Key<Order> getKey() {
		return getKey(getBusiness(), super.getId());
	}
	
	@Transient
	@JsonIgnore
	public static Key<Order> getKey(Key<Business> parent, Long id) {
		
		return new Key<Order>(parent, Order.class, id);
	}
	
	public Date getOrderTime() {
		return orderTime;
	}


	public Key<Product> getProduct() {
		return product;
	}


	public OrderStatus getStatus() {
		return status;
	}


	public boolean isArchived() {
		return archived;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}


	public void setArchived(boolean archived) {
		this.archived = archived;
	}


	public void setBill(Key<Bill> bill) {
		this.bill = bill;
	}


	public void setBusiness(Key<Business> business) {
		this.business = business;
	}


	public void setCheckIn(Key<CheckIn> checkIn) {
		this.checkIn = checkIn;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}


	public void setProduct(Key<Product> product) {
		this.product = product;
	}


	public void setStatus(OrderStatus status) {
		this.status = status;
	}


	public List<Key<OrderChoice>> getChoices() {
		return choices;
	}


	public void setChoices(List<Key<OrderChoice>> choices) {
		this.choices = choices;
	}


	public long getProductPrice() {
		return productPrice;
	}


	public void setProductPrice(long productPrice) {
		this.productPrice = productPrice;
	}


	public String getProductLongDesc() {
		return productLongDesc;
	}


	public void setProductLongDesc(String productLongDesc) {
		this.productLongDesc = productLongDesc;
	}


	public String getProductShortDesc() {
		return productShortDesc;
	}


	public void setProductShortDesc(String productShortDesc) {
		this.productShortDesc = productShortDesc;
	}


	public String getProductName() {
		return productName;
	}


	public void setProductName(String productName) {
		this.productName = productName;
	}


	public ImageDTO getProductImage() {
		return image;
	}


	public void setProductImage(ImageDTO productImage) {
		this.image = productImage;
	}


	public boolean isProductSpecial() {
		return productSpecial;
	}

	public void setProductSpecial(boolean productSpecial) {
		this.productSpecial = productSpecial;
	}
	
	
}
