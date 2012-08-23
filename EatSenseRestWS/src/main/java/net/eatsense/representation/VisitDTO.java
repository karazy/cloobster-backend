package net.eatsense.representation;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import net.eatsense.domain.Bill;
import net.eatsense.domain.Business;
import net.eatsense.domain.CheckIn;

@XmlRootElement
public class VisitDTO {
	private String nickname;
	private String checkInId;
	private Date checkInTime;
	private long businessId;
	private String businessName;
	private long billId;
	private Date billTime;
	private double billTotal;
	private String paymentMethod;
	
	/**
	 * 
	 */
	public VisitDTO() {
	}
	
	/**
	 * Create the transfer object from the given source entities.
	 * 
	 * @param checkIn
	 * @param business
	 * @param bill
	 */
	public VisitDTO(CheckIn checkIn, Business business, Bill bill) {
		if(checkIn != null) {
			nickname = checkIn.getNickname();
			checkInId = checkIn.getUserId();
			checkInTime = checkIn.getCheckInTime();
		}
		if(business != null) {
			businessId = business.getId();
			businessName = business.getName();
		}
		if(bill != null) {
			billId = bill.getId();
			billTime = bill.getCreationTime();
			billTotal = bill.getTotal() / 100.0;
			paymentMethod = bill.getPaymentMethod().getName();
		}
	}
	
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getCheckInId() {
		return checkInId;
	}
	public void setCheckInId(String checkInId) {
		this.checkInId = checkInId;
	}
	public Date getCheckInTime() {
		return checkInTime;
	}
	public void setCheckInTime(Date checkInTime) {
		this.checkInTime = checkInTime;
	}
	public Long getBusinessId() {
		return businessId;
	}
	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public Long getBillId() {
		return billId;
	}
	public void setBillId(Long billId) {
		this.billId = billId;
	}
	public Date getBillTime() {
		return billTime;
	}
	public void setBillTime(Date billTime) {
		this.billTime = billTime;
	}
	public double getBillTotal() {
		return billTotal;
	}
	public void setBillTotal(double billTotal) {
		this.billTotal = billTotal;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	
}
