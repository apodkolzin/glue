package resources.groovy

import org.apache.log4j.Logger
import ru.naumen.common.utils.StringUtilities
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.fcntp.bobject.contract.ContractFcntp
import ru.naumen.fcntp.bobject.demand.DemandFcntp
import ru.naumen.fcntp.bobject.lot.LotFcntp
import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.wcf.exceptions.UIException

// персоны, которым необходимо высылать уведомления (названия свойств в Контракте, возвращающих нужные персоны)
def recieverSettings = [
'managerFromExecuter']

// коды каталогов для заголовка и тела письма уведомления
def mailCatalogCodes = [
'title': 'RemarkOwnerDocumentLetter/RODL_title',
'body': 'RemarkOwnerDocumentLetter/RODL_body']

final Logger logger = Logger.getLogger(this.class)

def sendMailToEmployee = {EmployeeStech employee, ContractFcntp contract ->
if (null != employee) {
def LotFcntp lot = (LotFcntp) contract.lot
def DemandFcntp demand = (DemandFcntp) contract.demand

def parameters = [
'respectable': SendMailScript.getRespectablePhrase(employee),
'contract-title': contract.fullTitle,
'first-name': employee.firstName,
'second-name': employee.middleName,
'contract-number': contract.identifier,
]

if (null != contract.performer)
parameters['org-fullTitle'] = contract.performer.title

if (null != lot) {
parameters['lot-number'] = lot.fullNumber
parameters['lot-theme'] = lot.theme
}
else
logger.getLogger(this.class).warn("[RemarkOwnerDocumentNotificationScript.groovy] E-mail notification text could have missed parameters. Contract ${contract.identifier} has no Lot.")

if (null != demand) {
parameters['demand-number'] = demand.fullNumber
if (null != contract.managerFromMonitorOrg)
parameters['managerFromMonitor'] = contract.managerFromMonitorOrg
else
logger.getLogger(this.class).warn("[RemarkOwnerDocumentNotificationScript.groovy] E-mail notification text could have missed parameters. Demand ${demand.fullNumber} has no Work manager.")
}
else
logger.getLogger(this.class).warn("[RemarkOwnerDocumentNotificationScript.groovy] E-mail notification text could have missed parameters. Contract ${contract.identifier} has no Demand.")

parameters['stageNumber'] = subject.number

parameters['titleProgramm'] = subject.parent.parent.title

new SendMailScript().execute(employee, mailCatalogCodes.title, mailCatalogCodes.body, parameters)
}
}
