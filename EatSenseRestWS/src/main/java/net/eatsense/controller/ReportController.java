package net.eatsense.controller;

import com.google.inject.Inject;

import net.eatsense.counter.CounterRepository;

public class ReportController {
	
	private final CounterRepository counterRepo;

	@Inject
	public ReportController(CounterRepository counterRepo) {
		super();
		this.counterRepo = counterRepo;
	}
	
	
}
