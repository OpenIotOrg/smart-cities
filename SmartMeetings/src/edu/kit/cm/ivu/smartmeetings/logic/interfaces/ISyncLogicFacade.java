package edu.kit.cm.ivu.smartmeetings.logic.interfaces;

import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IEndpointConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IGoogleConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IInternalSensorConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.IRoomManagementConnector;
import edu.kit.cm.ivu.smartmeetings.logic.integration.interfaces.ISmartDiscussionsConnector;

/**
 * This interface defines the methods an synchronous logic facade must supply.
 * 
 * @author Andreas Eberle
 * 
 */
public interface ISyncLogicFacade extends IRoomManagementConnector,
		IGoogleConnector, ISmartDiscussionsConnector, IInternalSensorConnector,
		IEndpointConnector {

}
