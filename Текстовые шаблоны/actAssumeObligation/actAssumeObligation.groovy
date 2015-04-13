//package resources.groovy.birt

import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.fcntp.bobject.acceptancecommission.AcceptanceCommissionStaffHibernateHandler
import ru.naumen.fcntp.bobject.document.report.IndicatorWrapper
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.common.utils.StringUtilities
/**
 * @author ayakovlev
 * 07.10.2013
 * Скрипт для генерации акта приемки обязательств
 * ActAssumeObligations.rptdesign
 */

//act = object
//def act = helper.get('corebofs000080000k35us6010h4diq4')
def act = helper.get('corebofs000080000k3pclvm791j1kro')

def indicatorsListForXML = []
def indReport
def nonBudgetReport
def contract = act.contract
def stage = act.parent
def params = [:]

report.vars.contract =  contract
report.vars.demand =  contract?.demand
report.vars.stage =  stage
report.vars.acceptanceCommission = stage?.acceptanceCommission

workKinds = ""
def i = 0
for (def item in contract?.workKindSet)
{
	if (i != 0)
		workKinds += " и " + item.title
	else
		workKinds = item.title
	i++	
}
report.vars.workKindSet = workKinds

//члены приемочной комиссии
acceptanceCommissionStaff = []
if (stage?.acceptanceCommission != null)
{
	for (def item in AcceptanceCommissionStaffHibernateHandler.listActualCommissionMembersStaff(stage?.acceptanceCommission))
	{
        def post = OrgRelationHibernateHandler.listPostsByEmployee(session, item.person)[0];
        def org = OrgRelationHibernateHandler.listOrganizationsByEmployee(session, item.person)[0]
		acs = new AcceptanceCommissionStaff(status : item.status?.code, post: post?.title, fio: item.person.genitiveName.trim(), 
			nominativefio: item.person.title, firstName: item.person.firstName, middleName: item.person.middleName, lastName: item.person.lastName,
            postGenitive: post?.genitiveTitle,
            orgGenitive: org?.shortTitleInGenitiveCase)
        //genitiveTitle
		acceptanceCommissionStaff.add(acs)
	}
}
report.vars.acceptanceCommissionStaff = acceptanceCommissionStaff

//Наименование организации в творительном падеже
report.vars.orgTitleInInstrumentalCase = contract.performer?.titleInInstrumentalCase

for (def item in ContractStageHibernateHandler.listWorkResults(stage))
	if (item.BOCase.title.trim().equals("Отчет об индикаторах"))
		indReport = item

def indList = []
def startYear
def endYear
if (indReport != null)
{
	indList = indReport.listIndicators()
	startYear = indReport.getStartYear()
	endYear = indReport.getEndYear()
}


indicatorsListForXML = report.createList()
for (item in indList)
{
	indVals = report.createObject()
	def iw = new IndicatorWrapper(item, indReport)
	if (item.extTitle == null)
		indVals.indicatorExtTitle = item.title
	
	indVals.indicatorExtTitle = StringUtilities.isEmptyTrim(item.extTitle?.replaceAll("[\\u00A0\\s]+","")) ? item.title : item.extTitle    
	indVals.indicatorTitle = item.title
	indVals.indicatorUnit = item.unit
	indVals.indicatorIncrement = indReport.getIncrementByIndicator(item)
						
	def contractValues = []
	def reachedValues = []
	def contractValueTotal = 0
	def reachedValueTotal = 0
	for (def year = startYear; year <= endYear;year++)
	{
		def contractValueDouble = Double.parseDouble(checkString(iw.getContractValue(year)).replaceAll("[\\u00A0\\s]+",""))
		def reachedValueDouble = Double.parseDouble(checkString(iw.getReached(year)).replaceAll("[\\u00A0\\s]+",""))
		
		contractValues.add(new ContractValues(year : year, value: contractValueDouble))
		reachedValues.add(new ReachedValues(year : year, value : reachedValueDouble))
		
		contractValueTotal += contractValueDouble
		reachedValueTotal += reachedValueDouble
	}	
	indVals.contractValues = contractValues
	indVals.reachedValues = reachedValues
	indVals.contractValueTotal = contractValueTotal
	indVals.reachedValueTotal = reachedValueTotal
	
	indicatorsListForXML.add(indVals)	
}
report.tables.indicatorsList = indicatorsListForXML

//Распределение затрат внебюджетных средств по видам источников
params.put("stage", stage)
def listOffbudgetSources = helper.run("ListOffbudgetSources", params)//посылаем сообщение
params.clear()
if (listOffbudgetSources != null)
{
	report.vars.ownFunds = listOffbudgetSources.get("ownFunds")
	report.vars.credits = listOffbudgetSources.get("credits")
	report.vars.foreignInvestorFunds = listOffbudgetSources.get("foreignInvestorFunds")
	report.vars.borrowingCosts = listOffbudgetSources.get("borrowingCosts")
	report.vars.otherAssets = listOffbudgetSources.get("otherAssets")
	report.vars.totalFunds = listOffbudgetSources.get("totalFunds")
}
//Отчет о затратах внебюджетных средств
report.vars.offbudget = stage.offbudget
params.put("contract", contract)
def nonBudgetCosts = helper.run("NonBudgetCostsReport", params)//посылаем сообщение
params.clear()

if (nonBudgetCosts !=null)
{
	report.vars.planData = nonBudgetCosts.get("planData")
	report.vars.attractedData = nonBudgetCosts.get("attractedData")
}
if (stage?.planEndDate != null && stage?.notifyDate != null)
	report.vars.delay = (stage?.planEndDate - stage?.notifyDate - 10) < 0 ? Math.abs(stage?.planEndDate - stage?.notifyDate - 10) : 0
else
	report.vars.delay = 0
	
report.vars.isLastStage = stage.isLastStage()

class ContractValues{
	def year
	def value	
}
class ReachedValues{
	def year
	def value	
}

String checkString(Object s)
{
  s = s==null ? "0" : s.toString().trim();
  return s.equals("-")? "0" : s;
}

class AcceptanceCommissionStaff
{
	def status
	def post
	def fio
	def nominativefio
	def firstName
	def middleName
	def lastName
    def postGenitive
    def orgGenitive
	
}

