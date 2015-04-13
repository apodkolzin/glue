package resources.groovy.mon.singleContractXML

import javax.imageio.metadata.*
import javax.swing.tree.*
import javax.xml.parsers.*
import javax.xml.transform.*
import javax.xml.transform.dom.*
import javax.xml.transform.stream.*

import org.apache.commons.net.ftp.FTPClient
import org.w3c.dom.*
import org.xml.sax.*

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

import ru.naumen.common.utils.DateUtils
import ru.naumen.fcntp.bobject.contract.financing.ContractPaymentHibernateHandler
import ru.naumen.guic.formatters.DoubleFormatter

import org.apache.log4j.Logger
import ru.naumen.ccamcore.logging.CCAMLogUtil
import ru.naumen.common.mail.MimeMailWrapper
import ru.naumen.common.mail.SimpleMailSettings
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
import ru.naumen.common.mail.SimpleMimeMailDaemon
import ru.naumen.common.utils.StringUtilities
import ru.naumen.guic.components.forms.UIForm.UIFormUserException
import ru.naumen.wcf.engine.urls.URLCreator
import ru.naumen.ccam.bobject.stage.ContractStageHibernateHandler
import ru.naumen.fcntp.bobject.contract.requisites.ContractRequisitesHH
import ru.naumen.common.utils.CollectionUtils

import org.apache.commons.net.ftp.FTPClient
import java.io.FileInputStream
import java.io.IOException
import java.math.BigDecimal


/**
 * FCNTP-788 Доработка формирования xml-файла со сведениями о соглашении для ФИНМОН
 * 
 * @author aboronnikov
 * Date: 03.07.2014
 * Скрипт генерирует XML-файл с данными о контракте
 *
 */


String path = "/opt/fcntp/temp/"
def FILE_CASE(){ 'scan' }

def AGREEMENT_CASE(){ 'documentAgreement_IR14-20' }


def encoding = 'windows-1251'

def fileName = "BO_009_%s_%s.xml"
def xmlFileName = String.format(fileName, getYear(new Date()), getContractIdentifier(contract))

JAXBContext context = JAXBContext.newInstance(Bc.class)
Marshaller marshaller = context.createMarshaller()
    
marshaller.setProperty( Marshaller.JAXB_ENCODING , encoding )
marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)


return createContractPaymentXML(contract, marshaller)


def createContractPaymentXML(contract, marshaller)
{
	def integrationStage = 0

	DecimalFormat df = new DecimalFormat("#0.00")

	def data = createXmlData(contract, integrationStage)//собираем данные для XML файла

	ByteArrayOutputStream xmlReport = new ByteArrayOutputStream()
	marshaller.marshal(data, xmlReport)
	
	return xmlReport
}

Bc createXmlData(def contract, integrationStage)
{
    def bc = new Bc()
   
    //bc.yyyy = getYear(contract?.creationDate)
    bc.yyyy = getYear(new Date())
    
    bc.nnnnn = getContractIdentifier(contract)
    bc.fsource = 'DR'
    
    def file = getSingleOrFirstFileByCase(contract, FILE_CASE())
    if(file != null)
    {
        //bc.file_name = file.title
        bc.file_name = checkString((contract?.identifier)+'.pdf')
    }

    //def bc_dep = new BcDep()
    //bc_dep.code = checkString(contract?.shapeUnits?.code)
    //bc_dep.name = checkString(contract?.shapeUnits?.title)
    
    //if ((bc_dep.code != null)&&(bc_dep.name != null))
    //    bc.bc_dep = bc_dep
    bc.bc_dep = checkString(contract?.shapeUnits?.code)

    bc.bd_koment = checkString(contract?.fullTitle)
    if(bc.bd_koment!=null && bc.bd_koment.length()>90)
    {
         bc.bd_koment = bc.bd_koment.substring(0, 90)
    }

    if (integrationStage != 1)
    {
        def bd_type = new BdType()
        bd_type.code = '03'
        if (bd_type.code != null)
            bc.bd_type = bd_type

        bc.bd_num = checkString(contract?.identifier)
        bc.bd_date1 = timestampToDate(contract?.appendSignatureDate)
        bc.bd_date2 = timestampToDate(contract?.endDate)
    }
    
    bc.bd_typs = new BdType()
    bc.bd_typs.code = '13'
    
    bc.bd_typd = new BdType()
    
    //Если в Соглашении добавлен документ с категорией  «documentAgreement_IR14-20» в состоянии «документ сформирован» (идентификатор generated)
    def agreementCase = AGREEMENT_CASE()
    def agreement = helper.select("from FcntpDocumentContract where ccamBOCase.code='$agreementCase' and parent.id='$contract?.UUID'", 1)
    if(agreement!=null && "generated".equals(agreement.currentStage?.identificator))
    {
    	bc.bd_typd.code = 2
    }else
    {
    	bc.bd_typd.code = 1
    }

    bc.bd_sum = bigDecimal( contract?.budget )
    bc.bd_srok = getYear(contract?.endDate) - getYear(contract?.beginDate) + 1
    
    bc.prps = "Решение о результатах «творческого конкурса»" //checkString(contract?.lot?.contractSigningCause?.title)
    bc.np_prot = checkString(contract?.lot?.protocolNumberSummation)
    bc.data_prot = timestampToDate(contract?.lot?.protocolDateSummation)

    if (integrationStage != 1)
    {
        def subject_list = []
        def subject = new Subject()
        subject.typ_fin = checkString(contract?.financeType?.code)
        subject.kbk = checkString(("074" + contract?.financeType?.rzpr + contract?.financeType?.csr + contract?.financeType?.vr + contract?.financeType?.kosgu).replaceAll(" ", ""))
        subject.subject = checkString(contract?.fullTitle)
        //subject.okdp = checkString(contract?.lot?.codeByQualifier?.extCode)
        subject.price = bigDecimal( contract?.budget )
        subject.sum = bigDecimal( subject.price )
        subject_list.add(subject)
        bc.subject_list = subject_list

        bc.gp_list = createGpList(contract)
    }
    
    def ca = new Ca()
    
    ca.mkod = StringUtilities.isEmpty( contract?.performer?.machineCode ) ? null : contract?.performer?.machineCode.subSequence(0, 4);
    ca.inn = checkString(contract?.performer?.INN)
    
    if (integrationStage != 1)
    {
        def account = new Account()
        
        def curReqs = ContractRequisitesHH.getCurrentRequisites(contract, contract.performer)
        account.bkod = curReqs?.numberInMON?.substring(4,5)
      
        if (account.bkod != null)
            ca.account = account
    }

    bc.ca = ca
    
    return bc
}

def getSingleOrFirstFileByCase(object, fileCase)
{
   def list = helper.select("from DBFile f where f.fileCase.code='$fileCase' and f.parentWrapper.id='$object.UUID' and f.parentFile is null order by creationDate desc", 1)
   if(CollectionUtils.isEmptyCollection(list))
     return null
     
   return list[0]
}

def getContractIdentifier(contract)
{
	def cIdentifier = contract?.identifier
	if(!StringUtilities.isEmpty(cIdentifier)){
		cIdentifier = cIdentifier.replace(".", "")
  		cIdentifier = cIdentifier.replace("-", "")
	}
	return cIdentifier
}

def createBdDsList(def documents, def contract)
{
   def bd_ds_list = []
   for (def doc in documents)
   {
       def bdds = new BdDs()
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
    def gp_list = []
    def contractYears = DateUtils.getYears(contract?.dateRangeSafe?.beginDate, contract?.dateRangeSafe?.endDate)
    
    def firstYear = contractYears[0]
    
    def paymentNumber = 0
    for (def year in contractYears)
    {
         def plans = ContractPaymentHibernateHandler.listContractPaymentPlansByYear(contract, year)
         def previosBudget
         for(def plan in plans)
         {
             if(plan.displayableTitle.equals("АВАНС")){
				previosBudget = plan.budgetPlan
				continue
			 }
         
             def gp = new Gp()
             paymentNumber++
             if (plan.stage?.number != null)
                 gp.stage = plan.stage?.number.toString()
             gp.porder = paymentNumber
             gp.kbk = checkString(("074" + contract?.financeType?.rzpr + contract?.financeType?.csr + contract?.financeType?.vr + contract?.financeType?.kosgu).replaceAll(" ", ""))
             gp.typ_fin = checkString(contract?.financeType?.code)
             
             gp.sum_stage = bigDecimal( plan.stage?.budget )
             
             def date_pay
			 if(plan.stage.number == 1)
			 {
				date_pay = DateUtils.addDays(contract?.appendSignatureDate, 30)
			 }
			 else
			 {
				date_pay = DateUtils.addDays(plan.stage?.planBeginDate, 30)
			 }
			 gp.sum_payment = previosBudget
			 gp.month = month(date_pay)
			 gp.year = getYear(date_pay)
			 gp.date_pay = timestampToDate(date_pay) 
          
			 previosBudget = plan.budgetPlan
             
             if(gp.sum_payment == null)
             {
                gp.sum_payment = 0
             }
             
             gp_list.add(gp)
         }
     }
     
     gp_list.size == 0 ? null : gp_list
}

@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
class Bc
{
    String yyyy
    String nnnnn
    String fsource
    String file_name
    //BcDep bc_dep
    String bc_dep
    String bd_koment
    BigDecimal bd_sum
    BdType bd_type
    BdType bd_typs
    BdType bd_typd
    String bd_num
    String bd_date1
    String bd_date2
    String bd_srok
    String prps
    String np_prot
    String data_prot

    
    @XmlElementWrapper
    @XmlElement(name = "subject")
    ArrayList<Subject> subject_list//list of subject
    
    @XmlElementWrapper
    @XmlElement(name = "gp")
    ArrayList<Gp> gp_list//list of gp//
    
    Ca ca
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
    public boolean isEmpty()
    {
        code == null
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
    BigDecimal price
    int much = 1
    BigDecimal sum
}
@XmlAccessorType( XmlAccessType.FIELD )
class Gp
{
    int num = 1
    String typ_fin
    String kbk
    String stage
    int porder
    int year
    int month
    BigDecimal sum_stage
    BigDecimal sum_payment//??????
    String date_pay
}
@XmlAccessorType( XmlAccessType.FIELD )
class Ca
{
    String mkod
    String inn

    Account account
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
    if( number == null )
        return null;

    def decimal = new BigDecimal( number ).setScale(2, BigDecimal.ROUND_HALF_EVEN);        
    return decimal == new BigDecimal( 0.00 ) ? null : decimal; 
}

def decFormat(def number)
{
    DecimalFormat df = new DecimalFormat("#0.00")
    number == null ? null : df.format(number)
}

def getYear(def date)
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
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    date == null ? null : sdf.format(date).toString()
}

def checkString(def str)
{
    StringUtilities.isEmptyTrim(str) == true ? null : str
}

def getDocumentAct(def stageUUID)
{
    helper.query("select d from FcntpDocumentBase d where d.ccamBOCase.code = 'documentAct' and d.signingDate is not null and d.parent.id = '" + stageUUID + "'")
}
 
def nds( def sum, def persent_nds)
{
    def persent = bigDecimal( persent_nds )
    return  persent == null ? null : sum * persent_nds / (100 + persent_nds);
}