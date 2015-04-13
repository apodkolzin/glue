/**
 * Скрипт для справки о затратах внебюджетных средств
 * User: Alexey Yakovlev
 * Date: 29.11.2012
 * 
 * contractReport = helper.get('corebofs000080000jll9a2o2k27sq98')
 */
//package ru.naumen.fcntp.ui.tlc;

import org.apache.commons.lang.StringUtils;
import ru.naumen.ccamext.measurement.CoordinateSet;
import ru.naumen.ccamext.measurement.IAxis;
import ru.naumen.ccamext.measurement.axes.AxisCatalogItem;
import ru.naumen.ccamext.measurement.impl.Box;
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler;
import ru.naumen.common.containers.MapPropertySource;
import ru.naumen.common.utils.HTTPUtil;
import ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler;
import ru.naumen.core.catalogsengine.SimpleCatalogItem;
import ru.naumen.core.hibernate.HibernateUtil;
import ru.naumen.core.hibernate.bactions.BusinessActionBase;
import ru.naumen.core.ui.BKUIUtils;
import ru.naumen.fcntp.bobject.document.report.FcntpFactNonBudgetCostsReportDocument;
import ru.naumen.fcntp.measurement.ba.EditBoxWithLoggingBA;
import ru.naumen.fcntp.ui.formatters.Double10PrecisionFormatter;
import ru.naumen.fcntp.ui.transformers.Double10CellValueTransformer;
import ru.naumen.fcntp.ui.validators.CellErrorHook;
import ru.naumen.fcntp.ui.validators.CellValidatorBase;
import ru.naumen.guic.components.containers.UIActionContainer;
import ru.naumen.guic.components.forms.UIForm;
import ru.naumen.guic.components.lists.UITableListBaseControllerBase;
import ru.naumen.guic.components.lists.UITextFieldColumn;
import ru.naumen.wcf.exceptions.UIException;
import ru.naumen.wcf.interfaces.IUIComponent;
import ru.naumen.wcf.interfaces.IUIRequestEnvironment;
import ru.naumen.fcntp.bobject.contract.report.ContractBudgetUtil;
import ru.naumen.core.CoreBO;
import ru.naumen.ccam.bobject.stage.ContractStage;
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler;
import ru.naumen.ccamext.bobject.document.DocumentHibernateHandler;
import ru.naumen.fx.objectloader.PrefixObjectLoaderFacade;
import ru.naumen.common.utils.DateUtils;

contractReport = helper.get('corebofs000080000jll9a2o2k27sq98')
contract = contractReport.getContract()
contractBeginDate = contractReport.contract.getBeginDate() // карточка контракта поле  «Дата начала»;
contractNumber = contractReport.contract.getIdentifier() // карточка контракта поле  «Номер»;
stageNumber = contractReport.parent.getNumber() //карточка этапа дочерняя выбранному контракту поле «Номер»;
demandNumber = contractReport.contract.getDemand().getViewNumber() //карточка заявки дочерняя выбранному контракту  поле  «Номер»

def code = "1"
ownFunds = getBoxValueByAxis(code)//Собственные средства организации
code = "2"
credits = getBoxValueByAxis(code)//Кредиты
code = "3"
foreignInvestorFunds = getBoxValueByAxis(code)//Средства иностранных инвесторов
code = "4"
borrowingCosts = getBoxValueByAxis(code)//Заемные средства
code = "5"
otherAssets = getBoxValueByAxis(code)//Прочие
totalFunds = ownFunds + credits + foreignInvestorFunds + borrowingCosts + otherAssets//карточка отчета, поле «Привлечено на отчетном этапе, рублей»

HashMap<Integer, BigDecimal> attractedValuesByYears = new HashMap<Integer, BigDecimal>();
BigDecimal totalAttractedValue = BigDecimal.ZERO;
  for (ContractStage stage : ContractStageHibernateHandler.listContractStages(contract)) {
    final FcntpFactNonBudgetCostsReportDocument  nonBudgetReport = findNonBudgetReport(stage);
      if (null != nonBudgetReport) {
        final Integer stageYear = DateUtils.getYear(stage.getPlanEndDate());
        final BigDecimal oldValue = attractedValuesByYears.containsKey(stageYear) ? attractedValuesByYears.get(stageYear) : BigDecimal.ZERO;
        final BigDecimal totalAttractedValueForStage = totalFactNonBudget(nonBudgetReport);
        totalAttractedValue = totalAttractedValue.add(totalAttractedValueForStage);
        attractedValuesByYears.put(stageYear, oldValue.add(totalAttractedValueForStage));
      }
  }
  
BigDecimal plannedValue
BigDecimal attractedValue
Integer year = DateUtils.getYear(contractReport.parent.getPlanEndDate())//этап "Дата окончания"
plannedValue = ContractBudgetUtil.getOffBudget4Year(contract, year);//карточка отчета, поле «Привлечено, рублей» (брать за год родительского этапа поле "Дата окончания")
attractedValue = attractedValuesByYears.get(year);//карточка отчета, поле «Запланировано, рублей» (брать за год родительского этапа поле "Дата окончания")
if(null == plannedValue)
  plannedValue = BigDecimal.ZERO;
if(null == attractedValue)
  attractedValue = BigDecimal.ZERO;
           
BigDecimal plannedValueMinusOneYear = ContractBudgetUtil.getOffBudget4Year(contract, year - 1)//карточка отчета, поле «Привлечено»  (брать за предшествующий  год родительского этапа, поле "Дата окончания")
BigDecimal attractedValueMinusOneYear = attractedValuesByYears.get(year - 1)
if(null == plannedValueMinusOneYear)
  plannedValueMinusOneYear = BigDecimal.ZERO;
if(null == attractedValueMinusOneYear)
  attractedValueMinusOneYear = BigDecimal.ZERO;  

BigDecimal plannedAttractedDifference = attractedValue - plannedValue
  //Разница между полями карточки отчета:    «Запланировано, рублей» (брать за год родительского этапа поле "Дата окончания")    «Привлечено, рублей» (брать за год родительского этапа поле "Дата окончания")
BigDecimal sumPlanned = plannedValueMinusOneYear + plannedValue//Сумма полей карточки отчета:   «Привлечено , рублей»  (брать за предшествующий  год родительского этапа, поле "Дата окончания");«Привлечено, рублей»  (брать за год родительского этапа поле "Дата окончания")
BigDecimal sumAttracted = attractedValueMinusOneYear + attractedValue//Сумма полей карточки отчета: «Запланировано, рублей»  (брать за предшествующий  год родительского этапа, поле "Дата окончания"); «Запланировано, рублей»  (брать за год родительского этапа поле "Дата окончания").
BigDecimal plannedAttractedDifferenceTotal = sumAttracted - sumPlanned
performerChief = contract.performer.chief
///////////////////////////////////////
report.vars.contractBeginDate = contractBeginDate
report.vars.contractNumber = contractNumber
report.vars.stageNumber = stageNumber
report.vars.demandNumber = demandNumber
report.vars.contractReport = contractReport
 
report.vars.ownFunds = ownFunds
report.vars.credits = credits
report.vars.foreignInvestorFunds = foreignInvestorFunds
report.vars.borrowingCosts = borrowingCosts
report.vars.otherAssets = otherAssets
report.vars.totalFunds = totalFunds
  
report.vars.plannedValue = plannedValue
report.vars.attractedValue = attractedValue
report.vars.plannedAttractedDifference = plannedAttractedDifference
report.vars.plannedValueMinusOneYear = plannedValueMinusOneYear
report.vars.sumPlanned = sumPlanned
report.vars.sumAttracted = sumAttracted
report.vars.plannedAttractedDifferenceTotal = plannedAttractedDifferenceTotal

report.vars.performerChief = performerChief
////////////////////////////////////////////////////////
////////////////////////////////////////////////////////
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

FcntpFactNonBudgetCostsReportDocument findNonBudgetReport(ContractStage iStage){
  Iterator iterator =  DocumentHibernateHandler.listDocumentByParent(iStage, FcntpFactNonBudgetCostsReportDocument.class);
  return iterator.hasNext() ? (FcntpFactNonBudgetCostsReportDocument)PrefixObjectLoaderFacade.getObjectByUUID((String)iterator.next()) : null;
}

def getBoxValueByAxis(code){
  Box box = (Box) BoxHibernateHandler.createBoxIfNeeded(contractReport, Constants.MAIN_BOX_CODE)
  final Map<IAxis, Object> coords = new HashMap<IAxis, Object>()
  def FIRST_AXIS_NAME = "SourcesNonBudgetSum"
  def FIRST_AXIS = AxisCatalogItem.getAxis(FIRST_AXIS_NAME)
  SimpleCatalogItem FIRST_AXIS_VALUE = listAxisItems(FIRST_AXIS_NAME).get(0)
  coords.put(FIRST_AXIS, FIRST_AXIS_VALUE);
  IAxis axis = AxisCatalogItem.getAxis(Constants.OFFBUDGET_AXIS);
  def item = getAxisItemByCode(code)
  if(item==null){
    return null
  }

  coords.put(axis, item);
  result =  box.get(new CoordinateSet(coords))
  return  result
}

List<SimpleCatalogItem> listAxisItems(String axisName){
  (List<SimpleCatalogItem>) CoreCatalogHibernateHandler.getCatalogItems(CoreCatalogHibernateHandler.getCatalogByCode(getAxisCatalogName(axisName)))
}

String getAxisCatalogName(String axisName){
  (String) ((MapPropertySource) ((AxisCatalogItem) AxisCatalogItem.getAxis(axisName)).getParameters()).getPropertyValue("catalog")
}

def getAxisItemByCode(code){
  for  (item in listAxisItems(Constants.OFFBUDGET_AXIS))
  {
    if (item.code.equals(code))
      return item
  }
}

class Constants{
  final static MAIN_BOX_CODE = "reportFactNonBudgetCost"
  final static OFFBUDGET_AXIS = "SourcesNonBudgetEntry"
}