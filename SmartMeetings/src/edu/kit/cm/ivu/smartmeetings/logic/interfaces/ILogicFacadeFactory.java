package edu.kit.cm.ivu.smartmeetings.logic.interfaces;

/**
 * This interface defines a factory for {@link IAsyncLogicFacade}
 * implementations.
 * 
 * @author Andreas Eberle
 * 
 */
public interface ILogicFacadeFactory {
	/**
	 * Creates a new {@link IAsyncLogicFacade} instance.
	 * 
	 * @return A new instance of the {@link IAsyncLogicFacade} interface.
	 */
	ISyncLogicFacade createLogicFacade();
}
