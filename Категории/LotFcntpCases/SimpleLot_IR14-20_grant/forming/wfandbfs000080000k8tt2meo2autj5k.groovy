package resources.groovy

import org.apache.log4j.Logger

import ru.naumen.common.mail.MimeMailWrapper
import ru.naumen.common.mail.SimpleMailSettings
import ru.naumen.common.mail.SimpleMimeMailDaemon
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.wcf.engine.urls.URLCreator

/**
 * @author ayakovlev
 * 19.02.2014
 * L-17959 IR. Скрипт рассылки уведомлений о смене состояний лота
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/17959    
 */

def emails = [
    "smelnik@naumen.ru",
    ]

def tilePath = "FCNTP_2014/LotMessageDirection/title"
def bodyPath = "FCNTP_2014/LotMessageDirection/body"

def mailCatalogCodes = [
    'title': tilePath,
    'body': bodyPath]

def log = Logger.getLogger("script");

String title = SendMailScript.loadTemplate(mailCatalogCodes.title, null);
String body = SendMailScript.loadTemplate(mailCatalogCodes.body, null);

String param1 = String.format("<a href=\"%s\">%s</a>",
                            URLCreator.createFullLinkToPublishedObject(subject),
                            subject.fullNumber);

def parameters = ['lotNumber': param1]
for(email in emails) {
    SimpleMailSettings ms  = SimpleMailSettings.instance();
    MimeMailWrapper mailWrap = new MimeMailWrapper();
    mailWrap.setSubject(SendMailScript.replaceVariables(title, parameters));
    mailWrap.setContentType("text/html");
    mailWrap.setText(body);
    mailWrap.setFrom(ms.getSystemName(false), ms.getMailFrom());
    mailWrap.addReplyTo(ms.getSystemName(false), ms.getMailFrom());
    mailWrap.addTo(null, email.trim());
    if (mailWrap.getAllRecipients().length > 0) {
        log.info("Send mail to " + email + "...");
        SimpleMimeMailDaemon.sendMessage(mailWrap);
        log.info("mail send");
    }
}
