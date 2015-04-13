import ru.naumen.common.utils.StringUtilities 
import ru.naumen.core.hibernate.bactions.BusinessActionBase 
import ru.naumen.fcntp.bobject.contract.ContractFcntp 
import ru.naumen.fcntp.bobject.demand.DemandFcntp 
import ru.naumen.fcntp.bobject.lot.LotFcntp 
import ru.naumen.fcntp.workflow.SendMailScript 
import ru.naumen.stech.bobject.employee.EmployeeStech 
import ru.naumen.wcf.exceptions.UIException 

//Настройки --------------------------------------------------------------------------------------------------------- 

// персоны, которым необходимо высылать уведомления (названия свойств в Контракте, возвращающих нужные персоны) 
def recieverSettings = [ 
'manager', 
'managerFromMonitorOrg'] 

// коды каталогов для заголовка и тела письма уведомления 
def mailCatalogCodes = [ 
'title': 'EOR_2011/ReportLetter/Report_ResWork_title', 
'body': 'EOR_2011/ReportLetter/Report_ResWork_body'] 

//Methods ----------------------------------------------------------------------------------------------------------- 

def sendMailToEmployee = {EmployeeStech employee, ContractFcntp contract -> 
if (null != employee) { 
def LotFcntp lot = (LotFcntp) contract.lot 
def DemandFcntp demand = (DemandFcntp) contract.demand 

if (null == lot) 
throw new UIException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан Лот.') 
if (null == demand) 
throw new UIException('Невоможно составить текст e-mail уведомления, так как в Контракте не указана Заявка.') 
if (null == contract.managerFromExecuter) 
throw new UIException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан руководитель работ') 

def parameters = [ 
'respectable': SendMailScript.getRespectablePhrase(employee), 
'first-name': employee.firstName, 
'second-name': employee.middleName, 
'lot-number': lot.fullNumber, 
'lot-theme': lot.theme, 
'contract-number': contract.identifier, 
'demand-number': demand.fullNumber, 
'email-org': contract.managerFromExecuter.email, 
'UUID': subject.UUID, 
'title': subject.title, 
'workManager-title': contract.managerFromExecuter.title 
] 
if(null != contract.performer) 
parameters['org-fullTitle'] = contract.performer.title 

new SendMailScript().setFeedback(contract.managerFromExecuter.email).execute(employee, mailCatalogCodes.title, mailCatalogCodes.body, parameters) 
} 
} 

//Main --------------------------------------------------------------------------------------------------------------- 

def contract = BusinessActionBase.unproxy(subject.parent) 
recieverSettings.each {field -> 
def getterName = StringUtilities.getterName(field)[0] 
def recieverPerson = ContractFcntp.getMethod(getterName).invoke(contract) 
sendMailToEmployee(BusinessActionBase.unproxy(recieverPerson), contract) 
}