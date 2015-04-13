package resources.groovy.mon.competitions
import java.text.DecimalFormat
import java.text.SimpleDateFormat

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

import ru.naumen.common.utils.DateUtils
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.contract.financing.ContractPaymentHibernateHandler
import ru.naumen.guic.formatters.DoubleFormatter

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
import ru.naumen.wcf.engine.urls.URLCreator;
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler;
/**
 * @author ayakovlev
 * Date: 13.05.2013
 * Скрипт, ищущий негодные контракты и посылающий письмо 
 *
 */
def employee = helper.get("corebofs000080000jm61b6t1otnn998")//кому посылать письмо
def programmUUID = "corebofs000080000gte9v29hjmmodmk"
def contractCurrentStageNumber = "5";
def contracts = helper.query("select c from ContractFcntp c where c.parent.id = '" + programmUUID + "' and c.currentStage.number = '" +
    contractCurrentStageNumber + "' order by c.identifier asc")//контракты с соответствующим состоянием
date = new Date()//текущая дата
params = [:]
contractsForLetter = [:]//мапа, где хранятся контракты с незаполненными обязательными полями и сами поля
contractForResult = []
String requiredFields
for (contract in contracts)
{
    //проверяем, послано ла уже сообщение по этому контракту
    if(helper.query("select l from CoreLogEvent l where l.subjectUUID = '" + contract.UUID +
        "' and l.category = 'Изменение состояния' and l.date > '" +
        date.minus(1) + "'").size() == 0)
        continue//по данному контракту файл уже был сгенерен в прошлый день
        
    requiredFields = checkFields(contract)//проверяем, все ли обязательные поля для контракта заполнены
    if (requiredFields.length() == 0)
        contractForResult.add(contract)
    else 
        contractsForLetter.put(contract, requiredFields)//кладем контракт с пустыми полями в мапу
}

if (contractsForLetter.size() != 0)
{
    employees = []
    employees.add(employee)
    sendLetter(contractsForLetter, employees)//посылаем сообщение
}
return contractForResult

String checkFields(def contract)
{
    StringBuilder sb = new StringBuilder();
    
    if (contract?.shapeUnits == null)
        sb.append("<br />").append("- ").append("Профильное подразделение заказчика")
        
    if (contract?.budget == null)
        sb.append("<br />").append("- ").append("Итого. Бюджет РФ за все годы")
        
    if (contract?.lot?.notice?.contactPerson == null)
        sb.append("<br />").append("- ").append("Контактное лицо")
    
    if (checkString(contract?.lot?.notice?.contactPerson?.cityPhoneNumber) == null)
        sb.append("<br />").append("- ").append("Рабочий телефон")
        
    if (checkString(contract?.lot?.notice?.contactPerson?.email) == null)
        sb.append("<br />").append("- ").append("Адрес электронной почты")
    
    if (checkString(contract?.lot?.notice?.announceNumber) == null)
        sb.append("<br />").append("- ").append("№ объявления в конкурсных торгах")
        
    if (contract?.lot?.inOfferNumber == null)
        sb.append("<br />").append("- ").append("Номер лота в очереди")
    
    if (checkString(contract?.lot?.notice?.placingWay?.codeMon) == null)
        sb.append("<br />").append("- ").append("Способ размещения заказа")
        
    if (contract?.lot?.protocolCloseDateSummation == null)
        sb.append("<br />").append("- ").append("Дата и время закрытия протокола оценки и сопоставления заявок")
    
    if (checkString(contract?.concludeReason?.codeMon) == null)
        sb.append("<br />").append("- ").append("Основание для заключения")
        
    if (checkString(contract?.lot?.protocolNumberSummation) == null)
        sb.append("<br />").append("- ").append("№ протокола оценки и сопоставления заявок")
    
    if (contract?.lot?.protocolDateSummation == null)
        sb.append("<br />").append("- ").append("Дата оценки и сопоставления заявок")
        
    if (checkString(contract?.lot?.financeType?.code) == null)
        sb.append("<br />").append("- ").append("Тип финансирования")
    
    if (checkString(contract?.fullTitle) == null)
        sb.append("<br />").append("- ").append("Тема")
    
    if (checkString(contract?.lot?.codeByQualifier?.extCode) == null)
        sb.append("<br />").append("- ").append("Код по классификатору")
        
    if (checkString(contract?.performer?.title) == null)
        sb.append("<br />").append("- ").append("Наименование организации")
    
    if (checkString(contract?.performer?.INN) == null)
        sb.append("<br />").append("- ").append("ИНН")
    
    if (checkString(contract?.performer?.KPP) == null)
        sb.append("<br />").append("- ").append("КПП")
        
    if (checkString(contract?.performer?.juridicalAddress?.fullTitle) == null)
        sb.append("<br />").append("- ").append("Юридический адрес")
        
    return sb.toString()
}

def sendLetter(def contractsForLetter, def employees)
{
    employee = helper.get("corebofs000080000jm61b6t1otnn998")
    def log = Logger.getLogger("script");
    def mailCatalogCodes = [
        'title': 'FCNTP_2013/MonIntegration/ContractFields_title',
        'body': 'FCNTP_2013/MonIntegration/ContractFields_body']
    
    employees.each { EmployeeStech employee ->
        employee = BusinessActionBase.unproxy(employee);
    
    
    StringBuilder vars = new StringBuilder();
    
    Set s = contractsForLetter.entrySet();
    Iterator it = s.iterator();
    while(it.hasNext())
    {
        Map.Entry m =(Map.Entry)it.next();
        def c = m.getKey()
        def number = String.format("<a href=\"%s\">%s</a>",
            URLCreator.createFullLinkToPublishedObject(c),
            c.identifier)
        vars.append("По Контракту ").append(number).append(" необходимо заполнить следующие поля: ").append((String)m.getValue()).append("<br />").append("<br />")
    }
    
    def parameters = ['required-fields': vars]
    
    String title = SendMailScript.loadTemplate(mailCatalogCodes.title, employee);
    String body = SendMailScript.loadTemplate(mailCatalogCodes.body, employee);
            
    SimpleMailSettings ms  = SimpleMailSettings.instance();
    MimeMailWrapper mailWrap = new MimeMailWrapper();
    mailWrap.setSubject(SendMailScript.replaceVariables(title, parameters));
    mailWrap.setContentType("text/html");
    mailWrap.setText(SendMailScript.replaceVariables(body, parameters));
    mailWrap.setFrom(ms.getSystemName(false), ms.getMailFrom());
    mailWrap.addReplyTo(ms.getSystemName(false), ms.getMailFrom());
    
    def emplEmails = employee.getMailList();
    def LinkedHashSet emails = new LinkedHashSet();
    def boolean addcc = false;
    for(email in emplEmails)
    {
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
    if (mailWrap.getAllRecipients().length > 0)
    {
        log.info("Send mail to " + employee.getDisplayableTitle() + "...");
        SimpleMimeMailDaemon.sendMessage(mailWrap);
        log.info("mail send");
    }
    
    }
}

def checkString(def str)
{
    StringUtilities.isEmptyTrim(str) == true ? null : str
}