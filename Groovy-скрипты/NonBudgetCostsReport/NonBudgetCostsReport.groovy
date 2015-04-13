//package resources.groovy.birt.actAssumeObligations

import ru.naumen.ccam.bobject.stage.ContractStage
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.ccamext.bobject.document.DocumentHibernateHandler
import ru.naumen.ccamext.measurement.CoordinateSet
import ru.naumen.ccamext.measurement.IAxis
import ru.naumen.ccamext.measurement.axes.AxisCatalogItem
import ru.naumen.ccamext.measurement.impl.Box
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler
import ru.naumen.common.utils.DateUtils
import ru.naumen.core.CoreBO
import ru.naumen.fcntp.bobject.contract.report.ContractBudgetUtil
import ru.naumen.fcntp.bobject.document.report.FcntpFactNonBudgetCostsReportDocument
import ru.naumen.fx.objectloader.PrefixObjectLoaderFacade


/**
 * @author ayakovlev
 * 07.10.2013
 * Скрипт для извлечения данных из таблицы "Отчет о затратах внебюджетных средств"
 * Входной параметр - контракт
 *
 */


def result = [:]
def planData = []
def attractedData = []
try{
	HashMap<Integer, BigDecimal> attractedValuesByYears = new HashMap<Integer, BigDecimal>();
	BigDecimal totalAttractedValue = BigDecimal.ZERO;
	for (ContractStage contractStage : ContractStageHibernateHandler.listContractStages(contract)) {
		final FcntpFactNonBudgetCostsReportDocument report = findNonBudgetReport(contractStage);
		if (null != report) {
			final Integer stageYear = DateUtils.getYear(contractStage.getPlanEndDate());
			final BigDecimal oldValue = attractedValuesByYears.containsKey(stageYear) ? attractedValuesByYears.get(stageYear) : BigDecimal.ZERO;
			final BigDecimal totalAttractedValueForStage = totalFactNonBudget(report);
			totalAttractedValue = totalAttractedValue.add(totalAttractedValueForStage);
			attractedValuesByYears.put(stageYear, oldValue.add(totalAttractedValueForStage));
		}
	}

	planData.add(new NonBudgetData(title: "Всего", value: ContractBudgetUtil.getOffBudget4Year(contract, null)));
	attractedData.add(new NonBudgetData(title: "Всего",value: totalAttractedValue));

	final List<Integer> contractYears = DateUtils.getYears(contract.getDateRangeSafe().getBeginDate(), contract.getDateRangeSafe().getEndDate());
	for (Integer year : contractYears) {
		BigDecimal plannedValue = ContractBudgetUtil.getOffBudget4Year(contract, year);
		BigDecimal attractedValue = attractedValuesByYears.get(year);
		if(null == plannedValue)
			plannedValue = BigDecimal.ZERO;
		if(null == attractedValue)
			attractedValue = BigDecimal.ZERO;

		planData.add(new NonBudgetData(title : "за " + year.toString() + " год", value: plannedValue));
		attractedData.add(new NonBudgetData(title : "за " + year.toString() + " год",value: attractedValue));
	}
}catch(Exception e){
	log.error("exception has been thrown", e)
}finally{
	result.put("planData", planData)
	result.put("attractedData", attractedData)
	return result
}

def findNonBudgetReport(ContractStage iStage) {
	Iterator iterator =  DocumentHibernateHandler.listDocumentByParent(iStage, FcntpFactNonBudgetCostsReportDocument.class);
	return iterator.hasNext() ? (FcntpFactNonBudgetCostsReportDocument)PrefixObjectLoaderFacade.getObjectByUUID((String)iterator.next()) : null;
}
BigDecimal totalFactNonBudget(CoreBO doc){
	Box box = BoxHibernateHandler.getBoxWithCode(doc, "reportFactNonBudgetCost");
	BigDecimal result = BigDecimal.ZERO;
	if (box != null){
		Map<IAxis, Object> axes = new HashMap<IAxis, Object>();
		axes.put(AxisCatalogItem.getAxis("SourcesNonBudgetSum"), null);
		axes.put(AxisCatalogItem.getAxis("SourcesNonBudgetEntry"), null);
		final BigDecimal v = (BigDecimal) box.get(new CoordinateSet(axes));
		if (v != null)
			result = result.add(v);
	}
	return result;
}

class NonBudgetData {
	String title;
	String value;
}
