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

/**
 * @author ayakovlev
 * Date: 13.05.2013
 * Скрипт генерирует XML-файл с данными о контракте
 *
 * Источники значений для тегов XML-файла находятся в файле, приложенном к задаче
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/14704
 *
 */
// http://test.fcntp.ru:19090/fx/fcntp/ru.naumen.fcntp.components.jsp.published_jsp?uuid=corebofs000080000j5anklto5v1qpmg&activeComponent=Item
DecimalFormat df = new DecimalFormat("#0.00")
data = new Bc()


data = GreateXmlData(contract)//собираем данные для XML файла

    ByteArrayOutputStream xmlReport = new ByteArrayOutputStream();
    JAXBContext context = JAXBContext.newInstance(Bc.class);
    Marshaller marshaller = context.createMarshaller();
    marshaller.marshal(data, xmlReport);
    return xmlReport




@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
class Bc
{
    String nnnnn
    BcDep bc_dep
    BdType bd_type
    String bd_num
    String bd_date1
    String bd_date2
    BigDecimal bd_sum//
    BigDecimal bd_advance_pr//
    String bd_advance_sum//
    String bd_nds//
    String bd_persent_nds//
    String bd_advance_nds//
      @XmlElementWrapper
      @XmlElement(name = "bd_ds")
    ArrayList<BdDs> bd_ds_list//
    String bd_person_name
    String bd_person_phone
    String bd_person_email
    String bd_subject
    String za_ktema
    String bd_srok
    String za_num
    Integer za_lot_num
    ZaType za_type
    String za_vidr
    String za_final_date
    ZaCbDocVid za_cb_doc_vid
    String za_cb_doc
    String za_cb_doc_num
    String za_cb_doc_date
    String singleCustomerReason
      @XmlElementWrapper
      @XmlElement(name = "subject")
    ArrayList<Subject> subject_list//list of subject
      @XmlElementWrapper
      @XmlElement(name = "gp")
    ArrayList<Gp> gp_list//list of gp//
    Ca ca
    Account account//
}
@XmlAccessorType( XmlAccessType.FIELD )
class BcDep
{
    String code
    String name
}
@XmlAccessorType( XmlAccessType.FIELD )
class BdType
{
    String code
}
@XmlAccessorType( XmlAccessType.FIELD )
class ZaType
{
    String code
    Integer ca_pnum
    
    String ca_fio
    String ca_email
    String ca_phone
    public boolean isEmpty()
    {
        code == null && ca_pnum == null && ca_fio == null && ca_email == null && ca_phone == null
    }
}
@XmlAccessorType( XmlAccessType.FIELD )
class ZaCbDocVid
{
    String code
}
@XmlAccessorType( XmlAccessType.FIELD )
class Subject
{
    int num = 1
    String typ_fin
    String kbk
    String subject
    String okdp
    int okei = 793
    BigDecimal price
    int much = 1
    BigDecimal sum
}
@XmlAccessorType( XmlAccessType.FIELD )
class Gp
{
    String stage
    int num = 1
    int porder
    String kbk
    String typ_fin
    int year
    int month
    BigDecimal sum_stage
    BigDecimal advance
    String sum_nds
    String sum_precent_nds
    String date_pay_plan
    BigDecimal sum_payment//??????
    
    String date_act
}
@XmlAccessorType( XmlAccessType.FIELD )
class Ca
{
    String mkod
    String fname
    String inn
    String kpp
    Country country
    String address
    String address_p
    Type type
    Person person
}

@XmlAccessorType( XmlAccessType.FIELD )
class Type
{
    String code = "U"
}
@XmlAccessorType( XmlAccessType.FIELD )
class Country
{
    int code = 643
    String name = "Российская Федерация"
}
@XmlAccessorType( XmlAccessType.FIELD )
class Person
{
    String phone
}
@XmlAccessorType( XmlAccessType.FIELD )
class Account
{
    String bkod
}
@XmlAccessorType( XmlAccessType.FIELD )
class BdDs
{
    String bd_ds
    String bd_ds_num
    String bd_ds_date
    String priceChangeReason
    String gk_change
    String gk_change_doc = "Дополнительное соглашение"
    String gk_change_doc_num
    String gk_change_doc_date
}

def bigDecimal(def number)
{
    number == null ? 0 : number
}

def decFormat(def number)
{
    DecimalFormat df = new DecimalFormat("#0.00")
    number == null ? null : df.format(number)
}

def year(def date)
{
    date == null ? 0 : DateUtils.getYear(date)
}

def parse(def str)
{
    StringUtilities.isEmptyTrim(str) == true ? 0 : DoubleFormatter.parseBigDecimal(str)
}

def month(def date)
{
    date == null ? 0 : (DateUtils.getMonth(date) + 1)
}

def day(def date)
{
    date == null ? 0 : DateUtils.getDay(date)
}

def count(def product)
{
    product == 0 ? null : product/118
}

def valueOfInt (def str)
{
    StringUtilities.isEmptyTrim(str) == true ? null : Integer.valueOf(str)
}

def strFormat(def number)
{
    number == null ? null : String.format("%02d", number)
}

def timestampToDate(def date)
{
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    date == null ? null : sdf.format(date).toString()
}

def checkString(def str)
{
    StringUtilities.isEmptyTrim(str) == true ? null : str
}

def getDocumentAgreement(def contractUUID)
{
    helper.query("select d from FcntpDocumentBase d where d.ccamBOCase.code = 'documentAgreement' and d.parent.id = '" + contractUUID + "'")
}

def getDocumentAct(def stageUUID)
{
    helper.query("select d from FcntpDocumentBase d where d.ccamBOCase.code = 'documentAct' and d.signingDate is not null and d.parent.id = '" + stageUUID + "'")
}

def createBdDsList(def documents, def contract)
{
   bd_ds_list = []
   for (def doc in documents)
   {
       bdds = new BdDs()
       bdds.bd_ds = "01"
       bdds.bd_ds_num = checkString(doc.identifier)
       bdds.bd_ds_date = timestampToDate(doc.signingDate)
       
       if (checkString(doc.agreementContinuation?.code).equals("09") || checkString(doc.agreementContinuation?.code).equals("10"))
       {
           if (checkString(contract?.lot?.notice?.placingWay?.code).equals("OA") || checkString(contract?.lot?.notice?.placingWay?.code).equals("OK"))
               bdds.priceChangeReason = "2"
           if (checkString(contract?.lot?.notice?.placingWay?.code).equals("OKNM"))
               bdds.priceChangeReason = "4"
       }   
       
       
       bdds.gk_change = checkString(doc.agreementContinuation?.title)
       
       bdds.gk_change_doc_num = checkString(doc.identifier)
       bdds.gk_change_doc_date = timestampToDate(doc.signingDate)
       
       
       bd_ds_list.add(bdds)
   }
   bd_ds_list.size == 0 ? null : bd_ds_list
}

def createGpList(def contract)
{
    gp_list = []
    contractYears = DateUtils.getYears(contract?.dateRangeSafe?.beginDate, contract?.dateRangeSafe?.endDate)
    def paymentNumber = 0
     for (def year in contractYears)
     {
         plans = ContractPaymentHibernateHandler.listContractPaymentPlansByYear(contract, year)
         for(def plan in plans)
         {
             gp = new Gp()
             paymentNumber++
             gp.stage = plan.stage?.number.toString()
             gp.porder = paymentNumber
             gp.kbk = checkString("074" + contract?.lot?.financeType?.rzpr + contract?.lot?.financeType?.csr + contract?.lot?.financeType?.vr + contract?.lot?.financeType?.kosgu)
             gp.typ_fin = checkString(contract?.lot?.financeType?.code)
             gp.year = plan.year
             
             if (contract?.appendSignatureDate != null)
                 if ((month(contract?.appendSignatureDate) == 12)||((month(contract?.appendSignatureDate) == 11)&&(day(contract?.appendSignatureDate) >= 20)))
                     gp.month = 12
                 
                     if(plan.displayableTitle.equals("АВАНС"))
                         if ((month(contract?.appendSignatureDate) == 12)||((month(contract?.appendSignatureDate) == 11)&&(day(contract?.appendSignatureDate) >= 20)))
                             gp.month = 12
                         else
                             if (contract?.appendSignatureDate != null)
                                 gp.month = month(contract?.appendSignatureDate + 30)
                     else//этап
                         if ((month(plan.stage?.planEndDate) == 12)||((month(plan.stage?.planEndDate) == 11)&&(day(plan.stage?.planEndDate) >= 20)))
                             gp.month = 12
                         else
                             if (plan.stage?.planEndDate != null)
                                 gp.month = month(plan.stage?.planEndDate + 30)
             
             gp.sum_stage = plan.stage?.budget
             if(plan.displayableTitle.equals("АВАНС"))
                 gp.advance = plan.budgetPlan
             BigDecimal sum_precent_nds = (parse(contract?.lot?.ndsAmount?.title))
             
             if ((sum_precent_nds != null) && (gp.sum_stage != null))
                 gp.sum_nds = decFormat(count(gp.sum_stage * sum_precent_nds))
             
             gp.sum_precent_nds = decFormat(parse(contract?.lot?.ndsAmount?.title))
             gp.sum_payment = plan.budgetPlan
             if(!plan.displayableTitle.equals("АВАНС"))
             {
                 gp.date_pay_plan = timestampToDate(plan.stage?.plannedDatePayment)
                 def doc = getDocumentAct(plan.stage?.UUID)//может быть только один документ
                 gp.date_act = timestampToDate(doc[0]?.signingDate)
             }
             gp_list.add(gp)
         }
     }
     
     gp_list.size == 0 ? null : gp_list
}

Bc GreateXmlData(def contract)
{
    bc = new Bc()
    
       
        bc.nnnnn = checkString(contract?.uniqueNumberBO)
    
        bc_dep = new BcDep()
        bc_dep.code = checkString(contract?.shapeUnits?.code)
        bc_dep.name = checkString(contract?.shapeUnits?.title)
        if ((bc_dep.code != null)&&(bc_dep.name != null))
            bc.bc_dep = bc_dep
    
        bd_type = new BdType()
        bd_type.code = strFormat(valueOfInt(contract?.lot?.documentType?.code))
        if (bd_type.code != null)
            bc.bd_type = bd_type
    
        bc.bd_num = checkString(contract?.identifier)
        bc.bd_date1 = timestampToDate(contract?.appendSignatureDate)
        bc.bd_date2 = timestampToDate(contract?.endDate)
        bc.bd_sum = contract?.budget
        if (bc.bd_sum != null)
        {
            bc.bd_advance_pr = 30.00
            //BigDecimal bd_sum = bigDecimal(contract?.budget) 
            BigDecimal bd_advance_sum = bc.bd_sum * 0.3
            bc.bd_advance_sum = decFormat(bd_advance_sum)
            
            if (contract?.lot?.ndsAmount?.title != null)
            {
                BigDecimal bd_persent_nds = (parse(contract?.lot?.ndsAmount?.title))
                bc.bd_nds = decFormat(count(bd_persent_nds * bc.bd_sum))//если 0, то не выводить в файл
                bc.bd_advance_nds = decFormat(count(bd_advance_sum * bd_persent_nds))//если 0, то не выводить в файл
                bc.bd_persent_nds = decFormat(bd_persent_nds)
            }
            
        }
        
        
        documents = getDocumentAgreement(contract.UUID)
        bc.bd_ds_list = createBdDsList(documents, contract)
    
        bc.bd_person_name = checkString(contract?.lot?.notice?.contactPerson?.title)
        bc.bd_person_phone = checkString(contract?.lot?.notice?.contactPerson?.cityPhoneNumber)
        bc.bd_person_email = checkString(contract?.lot?.notice?.contactPerson?.email)
        bc.bd_subject = checkString(contract?.fullTitle)
        bc.bd_srok = year(contract?.endDate) - year(contract?.beginDate) + 1
        bc.za_num = checkString(contract?.lot?.notice?.announceNumber)
        
        bc.za_lot_num = contract?.lot?.inOfferNumber
        
        za_type = new ZaType()
        za_type.code = checkString(contract?.lot?.notice?.placingWay?.codeMon)
        za_type.ca_pnum = contract?.demand?.number
        za_type.ca_fio = checkString(contract?.managerFromExecuter?.title)
        za_type.ca_email = checkString(contract?.managerFromExecuter?.email)
        za_type.ca_phone = checkString(contract?.managerFromExecuter?.mobilePhoneNumber)
        
        if (!za_type.isEmpty())
            bc.za_type = za_type
    
            
            
        def workKinds = contract?.workKindSet
        
        //for (def i = 0; i < workKinds.size(); i++)
        boolean nir = false
        boolean okr = false
        for (def wk in workKinds)
        {
            if (wk.code.equals("1"))
                nir = true
            if (wk.code.equals("2"))
                okr = true
        }
        if (nir && okr)
            bc.za_vidr = "1"
        else if (nir)
            bc.za_vidr = "2"
        else if (okr)
            bc.za_vidr = "3"
        
        bc.za_final_date = timestampToDate(contract?.lot?.protocolCloseDateSummation)
        
        if (checkString(contract?.concludeReason?.code).equals("item1") ||(checkString(contract?.concludeReason?.code).equals("item2"))||
            (checkString(contract?.concludeReason?.code).equals("item3")))
                za_cb_doc = "Конкурсная комиссия Минобрнауки России №2012-1.8-5.2-ИР1"
        
        za_cb_doc_vid = new ZaCbDocVid()
        za_cb_doc_vid.code = checkString(contract?.concludeReason?.codeMon)
        if (za_cb_doc_vid.code != null)
            bc.za_cb_doc_vid = za_cb_doc_vid
        
        bc.za_cb_doc_num = checkString(contract?.lot?.protocolNumberSummation)
        bc.za_cb_doc_date = timestampToDate(contract?.lot?.protocolDateSummation)
        if (checkString(contract?.concludeReason?.code).equals("item2"))
            bc.singleCustomerReason = "12"
    
        subject_list = []
        subject = new Subject()
        subject.typ_fin = checkString(contract?.lot?.financeType?.code)
        subject.kbk = checkString("074" + contract?.lot?.financeType?.rzpr + contract?.lot?.financeType?.csr + contract?.lot?.financeType?.vr + contract?.lot?.financeType?.kosgu)
        subject.subject = checkString(contract?.fullTitle)
        subject.okdp = checkString(contract?.lot?.codeByQualifier?.extCode)
        subject.price = contract?.budget
        subject.sum = subject.price
        subject_list.add(subject)
        bc.subject_list = subject_list
    
        bc.gp_list = createGpList(contract)
    
        ca = new Ca()
    
        ca.mkod = checkString(contract?.performer?.machineCode)
        ca.fname = checkString(contract?.performer?.title)
        ca.inn = checkString(contract?.performer?.INN)
        ca.kpp = checkString(contract?.performer?.KPP)
    
        country = new Country()
        ca.country = country
    
        ca.address = checkString(contract?.performer?.juridicalAddress?.fullTitle)
        ca.address_p = checkString(contract?.performer?.factAddress?.fullTitle)
    
        person = new Person()
        person.phone = checkString(contract?.managerFromExecuter?.cityPhoneNumber)
        if (person.phone != null)
            ca.person = person
    
        bc.ca = ca
    
        account = new Account()
        if (!StringUtilities.isEmptyTrim(ca?.mkod)&&(ca?.mkod?.length() >= 5))
            account.bkod = bc.ca?.mkod?.charAt(4)
        if (account.bkod != null)
            bc.account = account
        
        return bc
}

 
