/**
 * Скрипт для генерации отчета об индикаторах
 * User: Alexey Yakovlev
 * Date: 29.11.2012
 * 
 * contractReport = helper.get('corebofs000080000jlapj2r4gts4s5c')
 */

 
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
import ru.naumen.fcntp.bobject.document.ContractReport;
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler;
import ru.naumen.ccamext.bobject.document.DocumentHibernateHandler;
import ru.naumen.fx.objectloader.PrefixObjectLoaderFacade;
import ru.naumen.common.utils.DateUtils;
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import org.apache.lucene.queryParser.ParseException;

//ContractReport contractReport = object
contractReport = helper.get('corebofs000080000jlapj2r4gts4s5c')
contract = contractReport.getContract()


report.vars.reportCategory = contractReport.getCcamBOCase().getTitle() //название категории с карточки отчета дочернего этапу
report.vars.contractBeginDate = contractReport.getContract().getBeginDate() // карточка контракта поле  «Дата начала»;
report.vars.contractNumber = contractReport.getContract().getIdentifier() // карточка контракта поле  «Номер»;
report.vars.contractSigningDate = contractReport.getContract().getAppendSignatureDate() // карточка контракта поле  «Дата подписания»;
report.vars.contractStageEndDate = contractReport.getContractStage().getPlanEndDate() // карточка этапа поле  «Дата окончания»;



report.vars.stageNumber = contractReport.getContractStage().getNumber() //карточка этапа дочерняя выбранному контракту поле «Номер»;

demand = BusinessActionBase.ensureReload(session, contractReport.getContract().getDemand())
report.vars.demandNumber = demand.getViewNumber() //карточка заявки дочерняя выбранному контракту  поле  «Номер»

report.vars.reportDateForSigning = contractReport.getReportDate() //Карточка отчета, поле "Дата отчета"  

indicators = []
def indList = contractReport.listIndicators()

date = contractReport.getContractStage().getPlanEndDate()
calendar = new java.util.GregorianCalendar()
calendar.setTime(date)
year = calendar.get(java.util.Calendar.YEAR)

indicatorsListForXML = []

def i = 0
for (item in indList)
{
  indVals = new IndVals()
  indicators.add(item)

  indVals.indicatorExtTitle = indicators[i].getExtTitle() //карточка индикатора, поле "Обозначение"
  indVals.indicatorTitle =   indicators[i].getTitle() //карточка индикатора, поле "Название"
  indVals.indicatorUnit =   indicators[i].getUnit() //карточка индикатора, поле "Единица измерения"
  indVals.indicatorSetYear =   contractReport.createIndicatorWrapper(indicators[i]).getContractValue(year).toString()//Карточка отчета, поле "Заданно"
  indVals.reportDate = new java.sql.Timestamp(contractReport.getReportDate().time) //Карточка отчета, поле "Дата отчета"  
  
  //indVals.indicatorIncrease =  indVals.indicatorGetYear - indVals.indicatorIncrement//Разница между полем "Достигнуто" и полем "Достигнуто на отчетном этапе"
  indVals.indicatorIncrease = contractReport.createIndicatorWrapper(indicators[i]).getIncrement().toString()//Достигнуто на отчетном этапе
  
  try {
      indVals.indicatorIncrease =  ru.naumen.guic.formatters.DoubleFormatter.parseDouble(indVals.indicatorIncrease)
  }
  catch (RuntimeException e){
      indVals.indicatorIncrease = BigDecimal.ZERO
  }
  
  //indVals.indicatorIncrement = contractReport.createIndicatorWrapper(indicators[i]).getIncrement().toString()//Достигнуто на отчетном этапе
  indVals.indicatorIncrement = contractReport.createIndicatorWrapper(indicators[i]).getReached(year - 1).toString()// карточки отчета из "достигнуто" за год, предшествующий текущему году(за котрый сдается отчетность),

  try {
      indVals.indicatorIncrement =  ru.naumen.guic.formatters.DoubleFormatter.parseDouble(indVals.indicatorIncrement)
  }
  catch (RuntimeException e){
      indVals.indicatorIncrement = BigDecimal.ZERO
      }

  
  //indVals.indicatorGetYear =  contractReport.createIndicatorWrapper(indicators[i]).getReached(year).toString()//Карточка отчета, поле "Достигнуто"
  indVals.indicatorGetYear =  indVals.indicatorIncrease + indVals.indicatorIncrement//Карточка отчета, поле "Достигнуто"
   
      
  indicatorsListForXML.add(indVals)

  i++
}
report.vars.indicatorsList = indicatorsListForXML

def StringBuilder programsQuery = new StringBuilder()
programsQuery.append("select rel.rightBO from ru.naumen.fcntp.bobject.contract.ContractFcntpAssistantRelation as rel where rel.leftBO ='" ).append( 'corebofs000080000i3u1fv8umfj6o9k').append("'").append(" and rel.typeKey = 'fcntp_assistantToContractRel'")
def programs = helper.select(programsQuery.toString())
//report.vars.person = programs


/*performer = BusinessActionBase.ensureReload(session, contract.getManagerFromExecuter())
if (performer)
    performerChief = performer
else{
    performer = BusinessActionBase.ensureReload(session, contract.performer)
    performerChief = performer.chief
}*/

performer = BusinessActionBase.ensureReload(session, contract.performer)
 if (performer)
     performerChief = performer.chief
 else{
     performer = BusinessActionBase.ensureReload(session, contract.getManagerFromExecuter())
     performerChief = performer
 }
 

report.vars.person = performerChief

class IndVals{
    def indicatorExtTitle
        def indicatorTitle
    def indicatorUnit
    def indicatorSetYear
        def reportDate
    def indicatorIncrement
    def indicatorIncrease
    def indicatorGetYear
}

