package resources.groovy; 

//ДАННЫЙ СКРИПТ ПРЕДНАЗНАЧЕН ДЛЯ УВЕДОМЛЕНИЯ ИСПОЛНИТЕЛЯ О ТОМ, ЧТО С ним заключен контракт 

import org.apache.log4j.Logger 
import ru.naumen.ccamcore.logging.CCAMLogUtil 
import ru.naumen.common.mail.MimeMailWrapper; 
import ru.naumen.common.mail.SimpleMailSettings; 
import ru.naumen.core.bobjects.person.CorePerson 
import ru.naumen.core.hibernate.bactions.BusinessActionBase 
import ru.naumen.core.ui.BKUIUtils 
import ru.naumen.fcntp.bobject.contract.ContractFcntp 
import ru.naumen.fcntp.bobject.demand.DemandFcntp 
import ru.naumen.fcntp.bobject.lot.LotFcntp 
import ru.naumen.fcntp.ui.tlc.AssistantsTLC 
import ru.naumen.fcntp.workflow.SendMailScript 
import ru.naumen.stech.bobject.employee.EmployeeStech 
import ru.naumen.wcf.exceptions.UIException 
import ru.naumen.common.mail.SimpleMimeMailDaemon; 
import ru.naumen.common.utils.StringUtilities; 
import ru.naumen.guic.components.forms.UIForm.UIFormUserException; 

def log = Logger.getLogger("script"); 

//Настройки --------------------------------------------------------------------------------------------------------- 
// коды каталогов для заголовка и тела письма уведомления 

//тело по умолчанию 
def bodyTempl="KADR_2011/ContractLetterRRK/ContractLetterRRK_creative1.1_body2"; 

def mailCatalogCodes = [ 
'title': 'KADR_2012/ContractLetterRRK/ContractLetterRRK_title', 
'corebofs000080000hq2j2ce1ie1kikk': 'KADR_2011/ContractLetterRRK/ContractLetterRRK_creative1.3.1_body1', 
'corebofs000080000hq2j2skfsonfnu4': 'KADR_2011/ContractLetterRRK/ContractLetterRRK_creative1.3.2_body3'] 
//Methods ----------------------------------------------------------------------------------------------------------- 

def sendMailToEmployee = {Set employees, ContractFcntp contract, LotFcntp lot, DemandFcntp demand, ArrayList senderEmail -> 
def LinkedHashSet emails = new LinkedHashSet(); 

employees.each { EmployeeStech employee -> 
employee = BusinessActionBase.unproxy(employee); 

def parameters = [ 
'respectable': SendMailScript.getRespectablePhrase(employee), 
'respectableAp': SendMailScript.getRespectablePhrase(contract.applicant), 
'first-name': employee.firstName, 
'first-nameAp': contract.applicant.firstName, 
'second-name': employee.middleName, 
'second-nameAp': contract.applicant.middleName, 
'lot-number': lot.fullNumber, 
'lot-theme': lot.theme, 
'contract-number': contract.identifier, 
'applicantTitle': contract.applicant==null?contract.applicant.title:contract.applicant.title, 
'demand-number': demand.fullNumber, 
'workManager-title': contract.managerFromExecuter==null?contract.applicant.title:contract.managerFromExecuter.title, 
'manager': contract.manager.title, 
'programAction': contract.programActions[0].displayableIdentifier, 
'manager-email': contract.manager.email, 
'manager-phone': contract.manager.cityPhoneNumber 
] 

if(null != contract.performer) 
parameters['org-fullTitle'] = contract.performer.title; 

if(contract.programActions == null || contract.programActions.size()<1) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указано программное мероприятие'); 

String title = SendMailScript.loadTemplate(mailCatalogCodes.title, employee); 
if( null != mailCatalogCodes[contract.programActions[0].UUID]) 
bodyTempl=mailCatalogCodes[contract.programActions[0].UUID]; 
String body = SendMailScript.loadTemplate(bodyTempl, employee); 
SimpleMailSettings ms = SimpleMailSettings.instance(); 
MimeMailWrapper mailWrap = new MimeMailWrapper(); 

mailWrap.setSubject(SendMailScript.replaceVariables(title, parameters)); 
mailWrap.setContentType("text/html"); 
mailWrap.setText(SendMailScript.replaceVariables(body, parameters)); 

mailWrap.setFrom(ms.getSystemName(), ms.getMailFrom()); 

// если имя и email отправителя не заданы в скрипте, то берем эти значения указанные в настройках почты 
if (senderEmail != null && senderEmail.size == 2 
&& !StringUtilities.isEmpty(senderEmail[0]) && !StringUtilities.isEmpty(senderEmail[1])) 
mailWrap.addReplyTo(senderEmail[0], senderEmail[1]); 
else 
mailWrap.addReplyTo(ms.getSystemName(), ms.getMailFrom()); 

def emplEmails = employee.getMailList(); 
def boolean addcc = false; 
emplEmails.each { email -> 
if (!emails.contains(email)) 
{ 
emails.add(email); 
if(addcc) 
mailWrap.addCc(employee.getDisplayableTitle(), email.trim()); 
else 
mailWrap.addTo(employee.getDisplayableTitle(), email.trim()); 
addcc = true; 
} 
} 

if (null != contract.performer && employee == contract.performer.chief && !StringUtilities.isEmpty(contract.performer.email)) 
{ 
if (!emails.contains(contract.performer.email)) 
{ 
emails.add(contract.performer.email); 
mailWrap.addTo(employee.getDisplayableTitle(), contract.performer.email.trim()); 
} 
} 

if (mailWrap.getAllRecipients().length > 0) 
{ 
log.info("Send mail to " + employee.getDisplayableTitle() + "..."); 
SimpleMimeMailDaemon.sendMessage(mailWrap); 
log.info("mail send"); 
} 
} 

def LOG_MSG_CAPTION = "Рассылка автоуведомления победителям"; 
def LOG_MSG_CONTENT_TPL = "Рассылка уведомлений победителям на email адреса: <%s>"; 
helper.execute{session -> 
CCAMLogUtil.save2Log(contract.getUUID(), BKUIUtils.getCurrentPerson().getUUID(), LOG_MSG_CAPTION, session, String.format(LOG_MSG_CONTENT_TPL, emails.join(", "))) 
}; 
} 

def contract = BusinessActionBase.unproxy(subject) 
def LotFcntp lot = (LotFcntp) contract.lot 
def DemandFcntp demand = (DemandFcntp) contract.demand 

if (null == lot) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан Лот.') 
if (null == demand) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указана Заявка.') 
if (null == contract.managerFromExecuter && null == contract.applicant) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан руководитель работ') 
if (null == contract.manager) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан ответственный сотрудник дирекции') 

// персоны, которым необходимо высылать уведомления (названия свойств в Контракте, возвращающих нужные персоны) 
def persons = [] as LinkedHashSet; 
if( null != contract.managerFromExecuter) 
persons.add( contract.managerFromExecuter ); 
if( null != contract.performer && null != contract.performer.chief) 
persons.add(contract.performer.chief) 
if (null != contract.manager) 
persons.add(contract.manager); 
persons.addAll(AssistantsTLC.findAssistants(contract)); 
if( null != contract.applicant) 
persons.add(contract.applicant); 

if (null != demand) 
{ 
if(null != demand.workManager) 
persons.add(demand.workManager); 
persons.addAll(AssistantsTLC.findAssistants(demand)) 
} 

// имя и email отправителя 
def sender = [contract.manager.fullName, contract.manager.email ]; 

//Main --------------------------------------------------------------------------------------------------------------- 
sendMailToEmployee(persons, contract, lot, demand, sender);