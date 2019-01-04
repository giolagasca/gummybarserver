package com.accenture.tcf.bars.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.accenture.tcf.bars.domain.Account;
import com.accenture.tcf.bars.domain.Billing;
import com.accenture.tcf.bars.domain.Customer;
import com.accenture.tcf.bars.domain.Record;
import com.accenture.tcf.bars.domain.Request;
import com.accenture.tcf.bars.exception.BarsException;
import com.accenture.tcf.bars.service.AccountService;
import com.accenture.tcf.bars.service.BillingService;
import com.accenture.tcf.bars.service.CustomerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RestController
@CrossOrigin(origins = {"http://localhost:4200", "http://10.242.34.109:4200"})
public class BarsController {
	
	@Autowired
	BillingService billingService;
	@Autowired
	AccountService accountService;
	@Autowired
	CustomerService customerService;
	
	@GetMapping("/")
	public List<Billing> eyy() {
		return billingService.findAllBillings();
	}
	
	@PostMapping("/csv")
	public List<Record> displayRecords(@RequestBody String requests) throws BarsException, ParseException {
		System.out.println("Requests :" + requests);
		List<Request> requestss = new ArrayList<>();
		List<Record> records = new ArrayList<>();
		SimpleDateFormat sdf;
		String regex = "([0-9]{2})/([0-9]{2})/([0-9]{4})";
		for(String line : requests.split("\n")) {
			String[] data = line.trim().split(",");
			requestss.add(new Request(Integer.parseInt(data[0]),data[1],data[2]));
		}
		//if("csv".equals(fileType)) {
			sdf = new SimpleDateFormat("MM/dd/yyyy");
		//}
		/*else if("txt".equals(fileType)) {
			sdf = new SimpleDateFormat("MMddyyyy");
		}
		else {
			throw new BarsException(BarsException.NO_SUPPORTED_FILE);
		}*/
			int counter = 0;
		for(Request request: requestss) {
			counter++;
			if(request.getBillingCycle() < 0 || request.getBillingCycle() > 12) {
				System.out.println(request.getBillingCycle());
				throw new BarsException(BarsException.BILLING_CYCLE_NOT_ON_RANGE + counter);
			}
			int billingCycle = request.getBillingCycle();
			if(!(request.getStartDate().matches(regex))) {
				System.out.println(request.getStartDate());
				throw new BarsException(BarsException.INVALID_START_DATE_FORMAT + counter);
			}
			Date startDate = sdf.parse(request.getStartDate());
			if(!(request.getEndDate().matches(regex))) {
				System.out.println(request.getEndDate());
				throw new BarsException(BarsException.INVALID_END_DATE_FORMAT + counter);
			}
			Date endDate = sdf.parse(request.getEndDate());
			Billing billing = billingService.findByBillingCycleAndStartDateAndEndDate(
					billingCycle, startDate, endDate);
			Account account = accountService.findById(billing.getAccountId()).orElseThrow(() -> new BarsException(BarsException.NO_RECORDS_TO_READ));
			Customer customer = customerService.findById(account.getCustomerId()).orElseThrow(() -> new BarsException(BarsException.NO_RECORDS_TO_READ));
			
			records.add(new Record(billingCycle, startDate, 
					endDate,account.getAccountName(),customer.getFirstName(),
					customer.getLastName(),billing.getAmount()));
		}
		return records;
	}
	
	@PostMapping("/txt")
	public List<Record> displayTxtRecords(@RequestBody String requests) throws BarsException, ParseException {
		List<Request> requestss = new ArrayList<>();
		List<Record> records = new ArrayList<>();
		SimpleDateFormat sdf;
		String regex = "([0-9]{8})";
		for(String line : requests.split("\n")) {
			requestss.add(new Request(Integer.parseInt(line.substring(0,2)),line.substring(2,10),line.substring(10,18)));
		}
		
		sdf = new SimpleDateFormat("MMddyyyy");
		int counter = 0;
		for(Request request: requestss) {
			counter++;
			if(request.getBillingCycle() < 0 || request.getBillingCycle() > 12) {
				System.out.println(request.getBillingCycle());
				throw new BarsException(BarsException.BILLING_CYCLE_NOT_ON_RANGE + counter);
			}
			System.out.println("Start :" + request.getStartDate() + "\t" + (request.getStartDate().matches(regex)));
			System.out.println("End :" + request.getEndDate() + "\t" + (request.getEndDate().matches(regex)));
			int billingCycle = request.getBillingCycle();
			if(!(request.getStartDate().matches(regex))) {
				System.out.println(request.getStartDate());
				throw new BarsException(BarsException.INVALID_START_DATE_FORMAT + counter);
			}
			Date startDate = sdf.parse(request.getStartDate());
			if(!(request.getEndDate().matches(regex))) {
				System.out.println(request.getEndDate());
				throw new BarsException(BarsException.INVALID_END_DATE_FORMAT + counter);
			}
			Date endDate = sdf.parse(request.getEndDate());
			Billing billing = billingService.findByBillingCycleAndStartDateAndEndDate(
					billingCycle, startDate, endDate);
			Account account = accountService.findById(billing.getAccountId()).orElseThrow(() -> new BarsException(BarsException.NO_RECORDS_TO_READ));
			Customer customer = customerService.findById(account.getCustomerId()).orElseThrow(() -> new BarsException(BarsException.NO_RECORDS_TO_READ));
			
			records.add(new Record(billingCycle, startDate, 
					endDate,account.getAccountName(),customer.getFirstName(),
					customer.getLastName(),billing.getAmount()));
		}
		return records;
	}
}
