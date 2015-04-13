package resources.groovy.mon.singleContractXML

import java.text.DecimalFormat
import org.apache.log4j.Logger
import java.text.SimpleDateFormat
import java.util.ArrayList;
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.wcf.engine.urls.URLCreator;

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

import ru.naumen.common.utils.DateUtils
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.contract.financing.ContractPaymentHibernateHandler
import ru.naumen.guic.formatters.DoubleFormatter
import ru.naumen.common.mail.MimeMailWrapper;
import ru.naumen.common.mail.SimpleMailSettings;
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.common.mail.SimpleMimeMailDaemon;

date = new Date()//текущая дата
DecimalFormat df = new DecimalFormat("#0.00")

data = new Data()
data = GreateXmlData(contract)//собираем данные для XML файла

    ByteArrayOutputStream xmlReport = new ByteArrayOutputStream();
    JAXBContext context = JAXBContext.newInstance(Data.class);
    Marshaller marshaller = context.createMarshaller();
    marshaller.marshal(data, xmlReport);
    return xmlReport


Data GreateXmlData(def contract)
{
    data = new Data()
    
    data.nnnnn = checkString(contract?.uniqueNumberBO)
    data.reason = checkString(contract?.contractTerminationReason?.code)
    return data
}

@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
class Data
{
    String nnnnn
    String reason
}

def checkString(def str)
{
    StringUtilities.isEmptyTrim(str) == true ? null : str
}
