package edu.kit.cm.ivu.smartmeetings.logic.sparql.solutionHandler;

import com.hp.hpl.jena.query.QuerySolution;

/**
 * This interface defines a method that gets a {@link QuerySolution} and create
 * an object of the interface's generic type.
 * 
 * @author Andreas Eberle
 * 
 */
public interface IQuerySolutionHandler<T> {
	/**
	 * Uses the given {@link QuerySolution} to create a result of the generic
	 * type.
	 * 
	 * @param solution
	 *            The {@link QuerySolution} that will be used to create the
	 *            object.
	 * @return An object created with the data from the {@link QuerySolution}.<br>
	 *         Or null if the given QuerySolution can not be used to create an
	 *         object.
	 */
	T createResult(QuerySolution solution);
}
