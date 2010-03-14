package com.google.gwt.ddmvc;

import com.google.gwt.ddmvc.model.Observer;

/**
 * Container class for information regarding exceptions encountered during a
 * run-loop
 * @author Kevin Dolan
 */
public class RunLoopException {

	private Exception exception;
	private Observer observer;
	private int iteration;
	
	public RunLoopException(Exception exception, Observer observer,
			int iteration) {
		
		super();
		this.exception = exception;
		this.observer = observer;
		this.iteration = iteration;
	}

	/**
	 * @return the exception encountered
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * @return the observer for which the exception was encountered
	 */
	public Observer getObserver() {
		return observer;
	}

	/**
	 * @return the run-loop iteration during which the exception was encountered
	 */
	public int getIteration() {
		return iteration;
	}
	
	@Override
	public String toString() {
		return exception + " from " + observer + " during iteration " + iteration;
	}
	
	
}
