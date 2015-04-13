import ru.naumen.common.utils.StringUtilities 
import ru.naumen.core.hibernate.bactions.BusinessActionBase 
import ru.naumen.fcntp.bobject.contract.ContractFcntp 
import ru.naumen.fcntp.bobject.demand.DemandFcntp 
import ru.naumen.fcntp.bobject.lot.LotFcntp 
import ru.naumen.fcntp.workflow.SendMailScript 
import ru.naumen.stech.bobject.employee.EmployeeStech 
import ru.naumen.wcf.exceptions.UIException 

/** 
* Настройки ---------------------------------------------------------------------------------------------------------- 
*/ 

// персоны, которым необходимо высылать уведомления (названия свойств в Контракте, возвращающих нужные персоны) 
def recieverSettings = [ 
'manager','managerFromExecuter'] 

// коды каталогов для заголовка и тела письма уведомления 
def mailCatalogCodes = [ 
'title': 'RemarkOwnerDocumentLetter/titleRemark', 
'body': 'RemarkOwnerDocumentLetter/bodyRemark'] 

noRemarksCatalogCodes = [ 'uuid':null, 
'title': 'RemarkOwnerDocumentLetter/NoRemark_title', 
'body': 'RemarkOwnerDocumentLetter/NoRemark_body'] 

/** 
* Methods ----------------------------------------------------------------------------------------------------------- 
*/ 

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
'workManager-title': contract.managerFromExecuter.title, 
'Author-title': subject.remarkAuthor.person.title 
] 
if(null != contract.performer) 
parameters['org-fullTitle'] = contract.performer.title 

if(subject.hasNoRemarks){ 
mailCatalogCodes = noRemarksCatalogCodes 
} 

new SendMailScript().setFeedback(employee.email).execute(employee, mailCatalogCodes.title, mailCatalogCodes.body, parameters) 

} 
} 

/** 
* Methods ----------------------------------------------------------------------------------------------------------- 
*/ 

def contract = BusinessActionBase.unproxy(subject.parent.parent) 
recieverSettings.each {field -> 
def getterName = StringUtilities.getterName(field)[0] 
def recieverPerson = ContractFcntp.getMethod(getterName).invoke(contract) 
sendMailToEmployee(BusinessActionBase.unproxy(recieverPerson), contract) 
}
//Рассылка для замов
for(def recieverPerson in contract.getAssistants()){
    sendMailToEmployee(BusinessActionBase.unproxy(recieverPerson), contract)
}