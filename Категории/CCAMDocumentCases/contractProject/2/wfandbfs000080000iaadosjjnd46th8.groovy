package resources.groovy

import org.apache.log4j.Logger
import ru.naumen.ccamcore.logging.CCAMLogUtil
import ru.naumen.common.utils.StringUtilities
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.core.ui.BKUIUtils
import ru.naumen.fcntp.bobject.contract.ContractFcntp
import ru.naumen.fcntp.bobject.demand.DemandFcntp
import ru.naumen.fcntp.bobject.document.ContractProjectDocument
import ru.naumen.fcntp.bobject.lot.LotFcntp
import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.wcf.exceptions.UIException

/**
 * Настройки ----------------------------------------------------------------------------------------------------------
 */

// персоны, которым необходимо высылать уведомления (названия свойств в Контракте, возвращающих нужные персоны)
def recieverSettings = [
        'manager',
        'managerFromMonitorOrg',
        'managerFromRosNauka']

// коды каталогов для заголовка и тела письма уведомления
def mailCatalogCodes = [
        'title': 'RemarkOwnerDocumentLetter/title_creative',
'body': 'RemarkOwnerDocumentLetter/body_creative']

/**
 * Methods -----------------------------------------------------------------------------------------------------------
 */

final Logger logger = Logger.getLogger(this.class)


def sendMailToEmployee = {EmployeeStech employee, ContractFcntp contract, ContractProjectDocument contractProject ->
  if (null != employee) {
    def LotFcntp lot = (LotFcntp) contract.lot
    def DemandFcntp demand = (DemandFcntp) contract.demand

    def parameters = [
            'respectable': SendMailScript.getRespectablePhrase(employee),
            'first-name': employee.firstName,
            'second-name': employee.middleName,
            'contract-number': contract.number,
    ]

    if (null != contract.performer)
      parameters['org-fullTitle'] = contract.performer.title

    if (null != lot) {
      parameters['lot-number'] = lot.fullNumber
      parameters['lot-theme'] = lot.theme
    }
    else
      logger.getLogger(this.class).warn("[RemarkOwnerDocumentNotificationScript.groovy] E-mail notification text could have missed parameters. Contract  ${contract.identifier} has no Lot.")

    if (null != demand) {
      parameters['demand-number'] = demand.fullNumber
      if (null != demand.workManager)
        parameters['workManager-title'] = demand.workManager.title
      else
        logger.getLogger(this.class).warn("[RemarkOwnerDocumentNotificationScript.groovy] E-mail notification text could have missed parameters. Demand  ${demand.fullNumber} has no Work manager.")
    }
    else
      logger.getLogger(this.class).warn("[RemarkOwnerDocumentNotificationScript.groovy] E-mail notification text could have missed parameters. Contract  ${contract.identifier} has no Demand.")

    new SendMailScript().execute(employee, mailCatalogCodes.title, mailCatalogCodes.body, parameters)

    def LOG_MSG_CAPTION = "Рассылка автоуведомления победителям";
    def LOG_MSG_CONTENT_TPL = "Рассылка уведомлений победителям на email адреса: <%s>";
    helper.execute {session ->
      if (employee.getMailList().size() > 0)
      {
        CCAMLogUtil.save2Log(contractProject.getUUID(), BKUIUtils.getCurrentPerson().getUUID(), LOG_MSG_CAPTION, session, String.format(LOG_MSG_CONTENT_TPL, employee.getMailList().join(", ")))
      }
    };
  }
}

/**
 * Main ---------------------------------------------------------------------------------------------------------------
 */


def ContractFcntp contract
try {
  contract = (ContractFcntp) BusinessActionBase.unproxy(subject.parent)
} catch (e) {
  try {
    contract = (ContractFcntp) BusinessActionBase.unproxy(subject.parent.parent)
  } catch (Exception e1) {
    throw new UIException('Невоможно получить контракт', e1)
  }
}
recieverSettings.each {field ->
  def getterName = StringUtilities.getterName(field)[0]
  def recieverPerson = null
  try {
    recieverPerson = ContractFcntp.getMethod(getterName).invoke(contract)
  } catch (Exception e) {

    logger.error("[RemarkOwnerDocumentNotificationScript.groovy] invoke ${field} on ${contract.title}: " + e.message, e)
  }
  if (recieverPerson)
    sendMailToEmployee(BusinessActionBase.unproxy(recieverPerson), contract, subject)

}