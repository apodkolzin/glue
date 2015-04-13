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
/**
 * @author ayakovlev
 * Date: 13.05.2013
 * Скрипт, ищущий негодные контракты и посылающий письмо 
 *
 */

def programmUUID = "corebofs000080000gte9v29hjmmodmk"
def contractCurrentStageCode = "active";
def contracts = helper.query("select c from ContractFcntp c where c.parent.id = '" + programmUUID + "' and c.currentStage.identificator = '" +
    contractCurrentStageCode + "' order by c.identifier asc")//контракты с соответствующим состоянием
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
    params = [:]
    params.put("contractsForLetter", contractsForLetter)
    params.put("employees", employees)
    helper.run("SingleSendLetter", params)//посылаем сообщение    
}
return contractForResult

String checkFields(def contract)
{
    StringBuilder sb = new StringBuilder();
    
    
    
    if (checkString(contract?.uniqueNumberBO) == null)
        sb.append("<br />").append("- ").append("Уникальный номер БО")
    
    if (contract?.shapeUnits == null)
        sb.append("<br />").append("- ").append("Профильное подразделение заказчика")
        
    if (checkString(contract?.lot?.documentType?.code) == null)
        sb.append("<br />").append("- ").append("Вид документа, заключаемого с победителем конкурса")
        
    if (checkString(contract?.identifier) == null)
        sb.append("<br />").append("- ").append("Номер контракта")
        
    if (contract?.appendSignatureDate == null)
        sb.append("<br />").append("- ").append("Дата подписания")
        
    if (contract?.endDate == null)
        sb.append("<br />").append("- ").append("Дата окончания")
        
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
    
    if (checkString(contract?.lot?.ndsAmount?.title) == null)
        sb.append("<br />").append("- ").append("Размер НДС(%)")
      
    str = checkStages(contract)
    if (checkString(str) != null)
        sb.append(str)
        
    if (checkString(contract?.performer?.postAddress?.fullTitle) == null)
        sb.append("<br />").append("- ").append("Почтовый адрес")
            
    if (checkString(contract?.performer?.OKFS?.code) == null)
        sb.append("<br />").append("- ").append("ОКФС")
            
    if (checkString(contract?.performer?.OKOPF?.code) == null)
        sb.append("<br />").append("- ").append("ОКОПФ")
            
    if (checkString(contract?.performer?.chief?.cityPhoneNumber) == null)
        sb.append("<br />").append("- ").append("Рабочий телефон")
        
    if (contract?.currentPayReqs == null)
        sb.append("<br />").append("- ").append("нет текущего платежного реквизита")
        
    return sb.toString()
}

String checkStages(def contract)
{
    StringBuilder sb = new StringBuilder();
    boolean budFlag = false
    boolean planFlag = false
    
        stages = ContractStageHibernateHandler.listContractStages(contract)
        for(def stage in stages)
        {
            if ((stage?.budget == null) && (!budFlag))
            {
                sb.append("<br />").append("- ").append("Бюджет в этапе контракта")
                budFlag = true
            }
            
            if ((stage?.plannedDatePayment == null) && (!planFlag))
            {
                sb.append("<br />").append("- ").append("Плановая дата предоставления документов на оплату")
                planFlag = true
            }
        }
    sb.toString()
}

def checkString(def str)
{
    StringUtilities.isEmptyTrim(str) == true ? null : str
}