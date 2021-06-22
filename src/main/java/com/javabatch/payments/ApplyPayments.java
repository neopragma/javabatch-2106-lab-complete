package com.javabatch.payments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.javabatch.payments.util.Convert;

public class ApplyPayments implements Tasklet {
	
	private String paymentData;
	private String paymentSummaryData;
	private String paymentErrorData;
	private FormattedPaymentData formattedPaymentData;
	private BufferedReader paymentsReceived;
	private BufferedWriter paymentSummaryWriter;
	private BufferedWriter paymentErrorWriter;
	private final static CurrencyUnit currencyUnit = Monetary.getCurrency("USD");
	private final static int nextDueDateInterval = 30;
    private final String CHECK_CUSTOMER_REGISTRATION = 
    		"SELECT CUSTID FROM CUSTOMER WHERE CUSTID = ?";
    private final String CHECK_INVOICE = 
    		"SELECT INVNUM, CUSTID, DUEDATE, LASTPAY, AMTDUE, AMTPAID, TAXPAID, STATUS " 
          + "FROM INVOICE " 
    	  + "WHERE INVNUM = ? AND CUSTID = ?";	
    private final String UPDATE_INVOICE_DATA = 
    		"UPDATE INVOICE " 
    	  + "SET LASTPAY = ?, "
    	  + "DUEDATE = ?, "
    	  + "AMTPAID = ?, "
    	  + "TAXPAID = ?, " 
    	  + "STATUS  = ? " 
    	  + "WHERE INVNUM = ? AND CUSTID = ?";
    
    private final static String STATUS_GOOD_STANDING = " ";
    private final static String STATUS_OVERPAID = "O";
    private final static String STATUS_PAID = "P";

    private Connection conn;
	
	public ApplyPayments(
			String paymentData,
			String paymentSummaryData,
			String paymentErrorData) {
        this.paymentData = paymentData;	
        this.paymentSummaryData = paymentSummaryData;
        this.paymentErrorData = paymentErrorData;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		init();
        String inputLine;
        try {
        	while ((inputLine = paymentsReceived.readLine()) != null) {
                formattedPaymentData = paymentDataFromInputLine(inputLine);
                processPayments(formattedPaymentData);                
        	}
        } catch (IOException ioe) {
        	throw new PaymentException("IOException reading payments file " + paymentData, ioe);
        } catch (SQLException sqle) {
        	throw new PaymentException("Unexpected exception accessing payments database", sqle);
        } catch (Exception ex) {
        	throw new PaymentException("Unexpected exception reading payments file " + paymentData, ex);
        } finally {
        	housekeeping();
        }
		return RepeatStatus.FINISHED;
	} 
	
	private void init() throws Exception {
		// Payments database
        conn = DriverManager.
                getConnection("jdbc:h2:~/test", "sa", "");
        conn.setAutoCommit(true);

        // Payment input data 
	    paymentsReceived = new BufferedReader(new FileReader(paymentData));
	    
	    // Payment summary output file 
	    paymentSummaryWriter = new BufferedWriter(new FileWriter(paymentSummaryData));
	    
	    // Payment error output file 
	    paymentErrorWriter = new BufferedWriter(new FileWriter(paymentErrorData));
	}
	
	private void housekeeping() throws Exception {
    	paymentsReceived.close();
    	paymentSummaryWriter.close();
    	paymentErrorWriter.close();
        conn.close();		
	}
	
	private void processPayments(FormattedPaymentData formattedPaymentData) throws Exception {		
		checkCustomerRegistration(formattedPaymentData);
	} 
	
	/**
	 * See if the customer exists in the database 
	 * 
	 * @param customerId
	 * @throws SQLException
	 */
	private void checkCustomerRegistration(FormattedPaymentData formattedPaymentData)throws Exception {
        PreparedStatement ps;
		ps = conn.prepareStatement(CHECK_CUSTOMER_REGISTRATION);
		ps.setString(1, formattedPaymentData.getCustomerId());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
        	checkInvoiceStatus(formattedPaymentData);
        } else {
        	customerIsNotRegistered(formattedPaymentData);
        }
	}
	
	/**
	 * See if this invoice has been issued for this customer
	 * 
	 * @param formattedPaymentData
	 * @throws Exception
	 */
	
	private void checkInvoiceStatus(FormattedPaymentData formattedPaymentData) throws Exception {
        PreparedStatement ps;
        ps = conn.prepareStatement(CHECK_INVOICE);
        ps.setString(1,  formattedPaymentData.getInvoiceNumber());
        ps.setString(2,  formattedPaymentData.getCustomerId());
        ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			applyPayment(formattedPaymentData, rs);
		} else { 
        	noSuchInvoiceForThisCustomer(formattedPaymentData);			
		}
	}
	
	private void applyPayment(FormattedPaymentData formattedPaymentData, ResultSet rs) throws Exception {
		Money cumulativeAmountPaid = Money.of(rs.getBigDecimal("AMTPAID"), currencyUnit);
		cumulativeAmountPaid = cumulativeAmountPaid.add(formattedPaymentData.getAmountPaid());

		Money cumulativeTaxPaid = Money.of(rs.getBigDecimal("TAXPAID"), currencyUnit);
		cumulativeTaxPaid = cumulativeTaxPaid.add(formattedPaymentData.getTaxPaid());

		Date lastPayDate = rs.getDate("LASTPAY");

		Calendar nextDueDate = Calendar.getInstance();
        nextDueDate.setTime(lastPayDate);
        nextDueDate.add(Calendar.DAY_OF_MONTH, nextDueDateInterval);
        
        Money totalAmountDue = Money.of(rs.getBigDecimal("AMTDUE"), currencyUnit);
        String newStatus = STATUS_GOOD_STANDING;
        int amountComparison = totalAmountDue.compareTo(cumulativeAmountPaid);
        switch(amountComparison) {
            case -1: newStatus = STATUS_OVERPAID;
                     break; 
            case  0: newStatus = STATUS_PAID; 
                     break; 
            default: newStatus = STATUS_GOOD_STANDING; 
        }    
                
        PreparedStatement ps = conn.prepareStatement(UPDATE_INVOICE_DATA);
        ps.setDate(1, new java.sql.Date(lastPayDate.getTime()));
        ps.setDate(2, new java.sql.Date(nextDueDate.getTime().getTime()));
        ps.setBigDecimal(3, cumulativeAmountPaid.getNumberStripped());
        ps.setBigDecimal(4, cumulativeTaxPaid.getNumberStripped()); 
        ps.setString(5, newStatus);
        ps.setString(6, rs.getString("INVNUM"));
        ps.setString(7, rs.getString("CUSTID"));
        ps.executeUpdate();
        
        writePaymentSummaryRecord(formattedPaymentData, newStatus);
	}
	
	private void writePaymentSummaryRecord(
			FormattedPaymentData formattedPaymentData, String newStatus) throws Exception {
		StringBuilder rec = new StringBuilder();
		rec.append(fixedLength(formattedPaymentData.getCustomerId(), 17));
		rec.append(fixedLength(formattedPaymentData.getInvoiceNumber(), 13));
		rec.append(fixedLength(formattedPaymentData.getAmountPaid().getNumberStripped().toPlainString(), 18));
		rec.append(fixedLength(formattedPaymentData.getTaxPaid().getNumberStripped().toPlainString(), 18));
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
		rec.append(dateFormatter.format(formattedPaymentData.getDatePaid().getTime()));
		rec.append(fixedLength(newStatus, 1));
		
		paymentSummaryWriter.write(rec.toString());
	}
	
	private void customerIsNotRegistered(FormattedPaymentData formattedPaymentData) throws Exception {
		writeErrorRecord(formattedPaymentData, "C");
	}
	
	private void noSuchInvoiceForThisCustomer(FormattedPaymentData formattedPaymentData) throws Exception {
		writeErrorRecord(formattedPaymentData, "I");
	}
	
	private void writeErrorRecord(FormattedPaymentData formattedPaymentData, String errorCode) throws Exception {
		String message = "";
		StringBuilder rec = new StringBuilder();
		rec.append(fixedLength(formattedPaymentData.getCustomerId(), 17));
		rec.append(fixedLength(formattedPaymentData.getInvoiceNumber(), 13));
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
		rec.append(dateFormatter.format(formattedPaymentData.getDatePaid().getTime()));
        if (errorCode.equals("C")) {
        	message = "Customer is not registered";
        } else {
        	if (errorCode.equals("I") ) {
        		message = "Invoice number is not associated with this customer.";
        	}
        }
        rec.append(fixedLength(message, 80));
        
        paymentErrorWriter.write(rec.toString());
	}

	private String fixedLength(String value, int length) {
	    return String.format("%1$-" + length + "s", value).substring(0, length);	
    }
	
	private FormattedPaymentData paymentDataFromInputLine(String inputLine) {
		return new FormattedPaymentData(
        		inputLine.substring(0,17),
        		inputLine.substring(17,30),
        		Convert.yyyymmddToCalendar(inputLine.substring(31,38)),
        		Convert.yyyymmddToCalendar(inputLine.substring(38,46)),
        		Convert.stringToMoney(inputLine.substring(46,64)),
        		Convert.stringToMoney(inputLine.substring(64, 78)));
	}
}
