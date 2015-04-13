/*import org.apache.log4j.Logger 
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
def mailCatalogCodes = [ 
'title': 'KADR_2011/ContractLetterRRK/ContractLetterRRK_title', 
'default': 'KADR_2011/ContractLetterRRK/ContractLetterRRK_body2']; 
def paCodes = [ 
'2.1': 'KADR_2011/ContractLetterRRK/ContractLetterRRK_body1']; 

//Methods ----------------------------------------------------------------------------------------------------------- 

def contract = BusinessActionBase.unproxy(subject) 
def LotFcntp lot = (LotFcntp) contract.lot 
def DemandFcntp demand = (DemandFcntp) contract.demand 

if (null == lot) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан Лот.') 
if (null == demand) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указана Заявка.') 
if (null == contract.managerFromExecuter) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан руководитель работ') 
if (null == contract.manager) 
throw new UIFormUserException('Невоможно составить текст e-mail уведомления, так как в Контракте не указан ответственный сотрудник дирекции') 

// персоны, которым необходимо высылать уведомления (названия свойств в Контракте, возвращающих нужные персоны) 
def persons = [] as LinkedHashSet; 
persons.add( contract.managerFromExecuter ); 
if( null != contract.performer && null != contract.performer.chief) 
persons.add(contract.performer.chief) 
persons.addAll(AssistantsTLC.findAssistants(contract)); 
if (null != demand) 
{ 
if(null != demand.workManager) 
persons.add(demand.workManager); 
persons.addAll(AssistantsTLC.findAssistants(demand)) 
} 

// имя и email отправителя 
def sender = [contract.manager.fullName, contract.manager.email ]; 

//Main --------------------------------------------------------------------------------------------------------------- 
emails = [];
persons.each 
{ 
employee -> 
  employee = BusinessActionBase.unproxy(employee); 
  def parameters = [ 
                     'respectable': SendMailScript.getRespectablePhrase(employee), 
                     'first-name': employee.firstName, 
                     'second-name': employee.middleName, 
                     'lot-number': lot.fullNumber, 
                     'lot-theme': lot.theme, 
                     'contract-number': contract.identifier, 
                     'demand-number': demand.fullNumber, 
                     'workManager-title': contract.managerFromExecuter.title, 
                     'manager': contract.manager.title, 
                     'manager-email': contract.manager.email, 
                     'manager-phone': contract.manager.cityPhoneNumber ];

    if(null != contract.performer) 
        parameters['org-fullTitle'] = contract.performer.title; 
  
    bodyTemplate = mailCatalogCodes.default; 
    if (!contract.getProgramActions().isEmpty()) 
    { 
      tmpl = paCodes[contract.getProgramActions().get(0).displayableIdentifier]; 
      if (tmpl != null) 
      { 
        bodyTemplate = tmpl; 
      } 
    } 
    if (bodyTemplate == null) 
    { 
       throw new UIFormUserException('Не удалось сформировать тело сообщения.Не найден соответсвующий ПМ шаблон.');
    } 
    title = SendMailScript.loadTemplate(mailCatalogCodes.title, employee); 
    body = SendMailScript.loadTemplate(bodyTemplate, employee); 

    SimpleMailSettings ms = SimpleMailSettings.instance(); 
    MimeMailWrapper mailWrap = new MimeMailWrapper(); 
    mailWrap.setSubject(SendMailScript.replaceVariables(title, parameters)); 
    mailWrap.setContentType("text/html"); 
    mailWrap.setText(SendMailScript.replaceVariables(body, parameters)); 
    mailWrap.setFrom(ms.getSystemName(), ms.getMailFrom()); 
   if (sender != null && sender.size == 2 && !StringUtilities.isEmpty(sender[0]) && !StringUtilities.isEmpty(sender[1])) 
     mailWrap.addReplyTo(sender[0], sender[1]); 
   else 
     mailWrap.addReplyTo(ms.getSystemName(), ms.getMailFrom()); 
  
  
    emplEmails = employee.getMailList(); 
    addcc = false; 
    emplEmails.each 
    { 
      email -> 
        if(!emails.contains(email))
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
      // SimpleMimeMailDaemon.sendMessage(mailWrap); 
      log.info("mail send"); 
    } 
}

def LOG_MSG_CAPTION = "Рассылка автоуведомления победителям"; 
def LOG_MSG_CONTENT_TPL = "Рассылка уведомлений победителям на email адреса: <%s>"; 
helper.execute()
{
  s-> 
    CCAMLogUtil.save2Log(contract.getUUID(), BKUIUtils.getCurrentPerson().getUUID(), LOG_MSG_CAPTION, s, String.format(LOG_MSG_CONTENT_TPL, emails.join(", "))) 
} 
*/