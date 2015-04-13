import java.math.BigDecimal; 
import java.util.Calendar; 
import java.util.Date; 
import java.util.HashMap; 
import java.util.Map; 

import ru.naumen.ccamext.measurement.CoordinateSet; 
import ru.naumen.ccamext.measurement.CoordinateSystem; 
import ru.naumen.ccamext.measurement.IBox 
import ru.naumen.ccamext.measurement.impl.Box; 
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler; 
import ru.naumen.common.utils.DateUtils; 
import ru.naumen.core.catalogsengine.CoreCatalogRegistry; 
import ru.naumen.core.catalogsengine.ICoreCatalogItem; 
import ru.naumen.fcntp.bobject.contract.ContractFcntp 

ContractFcntp contract = helper.get(contractUUID); 
IBox box = BoxHibernateHandler.getBoxWithCode(contract, "contractFcntpExpenseBudgetCostItems"); 
if (box != null) { 

final CoordinateSystem coordinateSystem = box.getBoxDefinition().getCoordinateSystem(); 

Map<String, Object> coordsMap = new HashMap<String, Object>(); 

Calendar calendar = DateUtils.createCalendar(new Date()); 
if (year != null) 
calendar.set(Calendar.YEAR, year); 

coordsMap.put("year", year == null ? null : calendar.getTime()); 
CoordinateSet coordinateSet = new CoordinateSet(coordinateSystem.createCoordinates(coordsMap)); 

BigDecimal result = (BigDecimal)box.get(coordinateSet); 
return result != null ? (result / 1000000) : BigDecimal.ZERO; 
} 
return BigDecimal.ZERO;