//package resources.groovy.birt.actAssumeObligations

import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.core.catalogsengine.SimpleCatalogItem
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.ccamext.measurement.CoordinateSet
import ru.naumen.ccamext.measurement.IAxis
import ru.naumen.ccamext.measurement.axes.AxisCatalogItem
import ru.naumen.ccamext.measurement.impl.Box
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler
import ru.naumen.common.containers.MapPropertySource
import ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler
import ru.naumen.core.catalogsengine.SimpleCatalogItem
import ru.naumen.fcntp.measurement.utils.CellAdapter;
import ru.naumen.fcntp.measurement.utils.CoordinateAdapter;
import ru.naumen.fcntp.measurement.utils.PackCoordinateAdapter
/**
 * @author ayakovlev
 * 07.10.2013
 * Скрипт для извлечения данных из таблицы "Распределение затрат внебюджетных средств по видам источников"
 * Входной параметр - этап контракта
 * 
 */
def result = [:]
def ownFunds
def credits
def foreignInvestorFunds
def borrowingCosts
def otherAssets
try{
	def nonBudgetReport
	for (def item in ContractStageHibernateHandler.listWorkResults(stage))
		if (item.BOCase.title.trim().equals("Отчет о затратах внебюджетных средств"))
			nonBudgetReport = item
			
	def box = BoxHibernateHandler.getBoxWithCode(nonBudgetReport, "reportFactNonBudgetCost", session)
	CellAdapter adapter = CellAdapter.get().set( box ).set(PackCoordinateAdapter.get())
	CoordinateAdapter coordinate = adapter.coordinate();

	coordinate.append( "SourcesNonBudgetEntry", "1" );
	ownFunds = 	adapter.getValue() 
	
	coordinate.append( "SourcesNonBudgetEntry", "2" );
	credits = adapter.getValue()
	
	coordinate.append( "SourcesNonBudgetEntry", "3" );
	foreignInvestorFunds = adapter.getValue()
	
	coordinate.append( "SourcesNonBudgetEntry", "4" );
	borrowingCosts = adapter.getValue()
	
	coordinate.append( "SourcesNonBudgetEntry", "5" );
	otherAssets = adapter.getValue()
	
}catch (Exception e){
	log.error("exception has been thrown", e)
}finally
{
	result.put("ownFunds", ownFunds)//Собственные средства организации
	result.put("credits", credits)//Кредиты
	result.put("foreignInvestorFunds", foreignInvestorFunds)//Средства иностранных инвесторов
	result.put("borrowingCosts", borrowingCosts)//Заемные средства
	result.put("otherAssets", otherAssets)//Прочие
	result.put("totalFunds", ownFunds + credits + foreignInvestorFunds + borrowingCosts + otherAssets)//Привлечено на отчетном этапе, рублей
	return result
}
