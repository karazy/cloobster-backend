package net.eatsense.representation;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import net.eatsense.domain.PaymentMethod;

public class BillDTO {
	private Long id;
	private PaymentMethod paymentMethod;
	private Date time;
	private Float total;
	@JsonIgnore
	private String billnumber;
	
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
	public Float getTotal() {
		return total;
	}
	public void setTotal(Float total) {
		this.total = total;
	}
	
	public String getBillnumber() {
		return billnumber;
	}
	public void setBillnumber(String billnumber) {
		this.billnumber = billnumber;
	}
	
	
}
