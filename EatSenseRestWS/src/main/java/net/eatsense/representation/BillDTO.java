package net.eatsense.representation;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnore;

import net.eatsense.domain.Bill;
import net.eatsense.domain.embedded.PaymentMethod;

public class BillDTO {
	private Long id;
	@NotNull
	@Valid
	private PaymentMethod paymentMethod;
	private Date time;
	private Double total;
	@JsonIgnore
	private String billnumber;
	private boolean cleared;
	@NotNull
	@Min(0)
	private Long checkInId;
	
	public BillDTO() {
	}
	
	public BillDTO(Bill bill) {
		if(bill == null)
			return;
		
		// convert from stored minor values to decimal
		if(bill.getTotal() != null) {
			this.setTotal( (bill.getTotal() == 0) ? 0 : bill.getTotal() / 100d );
		}
		this.setCheckInId(bill.getCheckIn().getId());
		this.setCleared(bill.isCleared());
		this.setId(bill.getId());
		this.setTime(bill.getCreationTime());
		this.setPaymentMethod(bill.getPaymentMethod());
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		this.total = total;
	}
	
	public String getBillnumber() {
		return billnumber;
	}
	public void setBillnumber(String billnumber) {
		this.billnumber = billnumber;
	}
	public boolean isCleared() {
		return cleared;
	}
	public void setCleared(boolean cleared) {
		this.cleared = cleared;
	}
	public Long getCheckInId() {
		return checkInId;
	}
	public void setCheckInId(Long checkInId) {
		this.checkInId = checkInId;
	}
}
