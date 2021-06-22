package com.javabatch.payments.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.javamoney.moneta.Money;

import com.javabatch.payments.PaymentException;

public class Convert {

	public static Calendar yyyymmddToCalendar(String yyyymmdd) {
		Calendar cal = Calendar.getInstance();
		try {
		    DateFormat formatter ; 
		    Date date ; 
		    formatter = new SimpleDateFormat("yyyyMMdd");
		    date = (Date)formatter.parse(yyyymmdd); 
		    cal.setTime(date);
		} catch (ParseException pe) {
			throw new PaymentException("ParseException converting " + yyyymmdd + " to Calendar", pe);
	    } catch (Exception ex) {
		    throw new PaymentException("Unexpected exception converting " + yyyymmdd + " to Calendar", ex);
   	    }
		return cal;
	}
	
	public static Money stringToMoney(String value) {
		BigDecimal decimalValue = new BigDecimal(value);
		decimalValue = decimalValue.divide(new BigDecimal("100"));
		CurrencyUnit currencyUnit = Monetary.getCurrency("USD");
		return Money.of(decimalValue, currencyUnit);
	}
	
}
