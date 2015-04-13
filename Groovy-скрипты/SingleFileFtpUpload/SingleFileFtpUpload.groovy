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

import org.apache.commons.net.ftp.FTPClient
import java.io.FileInputStream;
import java.io.IOException;
File newFile = new File(fileName);
fileWriter = new FileWriter(newFile)
fileWriter.write(xmlReport.toString())
fileWriter.close()

FTPClient client = new FTPClient()
FileInputStream fis = null;
client.connect("node4.net2ftp.ru")
client.login("leha140486@yandex.ru", "ec13225090c8")

//client.connect("85.142.20.106")
//client.login("dirirmont", "LbhBHKjuby")

String[] directories = workDir.split("/");
for (String dir : directories ) 
{

    if (!dir.isEmpty() ) 
    {  
        client.makeDirectory(dir)
        client.changeWorkingDirectory(dir)
    }
}

fis = new FileInputStream(newFile)
client.storeFile(fileName, fis)
client.logout();
if (fis != null)
    fis.close()
client.disconnect()
