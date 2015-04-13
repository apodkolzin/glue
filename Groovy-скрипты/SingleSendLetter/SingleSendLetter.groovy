package resources.groovy.mon.singleContractXML

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