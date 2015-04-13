//package resources.groovy
import java.text.DecimalFormat

import org.apache.commons.lang.StringUtils;
import ru.naumen.common.utils.DateUtils
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.contract.financing.ContractPaymentHibernateHandler
import ru.naumen.guic.formatters.DoubleFormatter
import java.text.SimpleDateFormat
/**
 * @author ayakovlev
 * Date: 2.04.2013
 * Скрипт для генерации XML-файла, необходимого для интеграции с финансовой системой МОНа
 * 
 * Источники значений для тегов XML-файла находятся в файле, приложенном к задаче
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/14704
 */


def contractUUID = "corebofs000080000ifbjo4mus0sc1fo"

contract = helper.get(contractUUID)
DecimalFormat df = new DecimalFormat("#0.00")

bc = new Bc()

bc.yyyy = "isEmptyYet"
bc.nnnnn = "isEmptyYet"

bc_dep = new BcDep()
bc_dep.code = contract?.shapeUnits?.code
bc_dep.name = contract?.shapeUnits?.title
bc.bc_dep = bc_dep

bd_type = new BdType()
bd_type.code = strFormat(valueOfInt(contract?.lot?.documentType?.code))
if (bd_type.code != null) 
    bc.bd_type = bd_type

bc.bd_num = contract?.identifier
bc.bd_date1 = timestampToDate(contract?.appendSignatureDate)
bc.bd_date2 = contract?.endDate
bc.bd_sum = bigDecimal(contract?.demand?.demandFinances?.getBudgetRFTotal())
bc.bd_advance_sum = bc.bd_sum * 0.3
bc.bd_persent_nds = (parse(contract?.lot?.ndsAmount?.title))
bc.bd_nds = count(bc.bd_persent_nds * bc.bd_sum)//если 0, то не выводить в файл
bc.bd_advance_nds = count(bc.bd_advance_sum * bc.bd_persent_nds)//если 0, то не выводить в файл
bc.bd_persent_nds = decFormat(parse(contract?.lot?.ndsAmount?.title))
bc.bd_okdp_code = contract?.lot?.codeByQualifier?.code

documents = getDocumentAgreement(contractUUID)
bc.bd_ds_list = createBdDsList(documents)

bc.bd_person_name = contract?.managerFromRosNauka?.title
bc.bd_person_phone = contract?.managerFromRosNauka?.mobilePhoneNumber
bc.bd_person_email = contract?.managerFromRosNauka?.email
bc.bd_subject = contract?.fullTitle
bc.bd_srok = year(contract?.endDate) - year(contract?.beginDate) + 1
bc.za_num = contract?.lot?.notice?.announceNumber
bc.za_lot_num = contract?.lot?.inOfferNumber
    
za_type = new ZaType()
za_type.code = contract?.lot?.notice?.placingWay?.code
if (za_type.code != null)
    bc.za_type = za_type

bc.za_name = contract?.lot?.theme
bc.za_final_date = timestampToDate(contract?.lot?.protocolCloseDateSummation)
    
za_cb_doc_vid = new ZaCbDocVid()
za_cb_doc_vid.code = contract?.concludeReason?.code
if (bc.za_cb_doc_vid != null)
    bc.za_cb_doc_vid = za_cb_doc_vid        
    
bc.za_cb_doc_num = contract?.lot?.protocolNumberSummation
bc.za_cb_doc_date = contract?.lot?.protocolDateSummation

subject_list = []
subject = new Subject()
subject.typ_fin = contract?.lot?.financeType?.code
subject.kbk = "074" + contract?.lot?.financeType?.rzpr + contract?.lot?.financeType?.csr + contract?.lot?.financeType?.vr + contract?.lot?.financeType?.kosgu
subject.subject = contract?.fullTitle
subject.okdp = contract?.lot?.codeByQualifier?.code
subject.price = contract?.demand?.demandFinances?.budgetRFTotal
subject.sum = subject.price
subject_list.add(subject)
bc.subject_list = subject_list

bc.gp_list = createGpList(contract)

ca = new Ca() 

ca.mkod = contract?.performer?.machineCode
ca.fname = contract?.performer?.title
ca.inn = contract?.performer?.INN
ca.kpp = contract?.performer?.KPP

country = new Country()
ca.country = country

ca.address = contract?.performer?.juridicalAddress?.fullTitle
ca.address_p = contract?.performer?.factAddress?.fullTitle

person = new Person()
person.phone = contract?.managerFromExecuter?.cityPhoneNumber
if (person.phone != null)
    ca.person = person

bc.ca = ca

account = new Account()
if (!StringUtilities.isEmptyTrim(ca.mkod)&&(ca.mkod.length() >= 5))
    account.bkod = bc.ca?.mkod?.charAt(4)
if (account.bkod != null)    
    bc.account = account

report.vars.bc = bc

class Bc
{
    def yyyy
    def nnnnn
    BcDep bc_dep
    BdType bd_type
    def bd_num
    def bd_date1
    def bd_date2
    def bd_sum
    def bd_advance_pr = 30.00
    def bd_advance_sum
    def bd_nds
    def bd_persent_nds
    def bd_advance_nds
    def bd_okdp_code
    def bd_ds_list
    def bd_person_name
    def bd_person_phone
    def bd_person_email
    def bd_subject
    def bd_srok
    def za_num
    def za_lot_num
    ZaType za_type
    def za_name
    def za_final_date
    ZaCbDocVid za_cb_doc_vid
    def za_cb_doc_num
    def za_cb_doc_date
    def subject_list//list of subject
    def gp_list//list of gp
    Ca ca
    Account account
}

class BcDep
{
    def code
    def name    
}

class BdType
{
    def code
}

class ZaType
{
    def code
}

class ZaCbDocVid
{
    def code
} 

class Subject
{
    def num = 1
    def typ_fin
    def kbk
    def subject
    def okdp
    def okei = 793
    def price
    def much = 1
    def sum
}
class Gp
{
    def stage
    def num = 1
    def porder
    def kbk
    def typ_fin
    def year
    def month
    def sum_stage
    def advance
    def sum_nds
    def sum_precent_nds
    def date_pay_plan
    def sum_payment//??????
    def date_act
}

class Ca
{
    def mkod
    def fname
    def inn
    def kpp
    Country country
    def address
    def address_p
    Person person
}

class Country
{
    def code = 643
    def name = "Российская Федерация"
}
class Person
{
    def phone
}

class Account
{
    def bkod
}

class BdDs
{
    def bd_ds
    def bd_ds_num
    def bd_ds_date
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

def getDocumentAgreement(def contractUUID)
{  
    helper.query("select d from FcntpDocumentBase d where d.ccamBOCase.code = 'documentAgreement' and d.parent.id = '" + contractUUID + "'")
}

def createBdDsList(def documents)
{
   bd_ds_list = []   
   for (def doc in documents)
   {
       bdds = new BdDs()
       bdds.bd_ds = "01"
       bdds.bd_ds_num = doc.identifier
       bdds.bd_ds_date = timestampToDate(doc.signingDate)
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
             gp.stage = plan.stage?.number
             gp.porder = paymentNumber
             gp.kbk = "074" + contract?.lot?.financeType?.rzpr + contract?.lot?.financeType?.csr + contract?.lot?.financeType?.vr + contract?.lot?.financeType?.kosgu
             gp.typ_fin = contract?.lot?.financeType?.code
             gp.year = plan.year
             
             if (contract?.appendSignatureDate != null)
                 if ((month(contract?.appendSignatureDate) == 12)||((month(contract?.appendSignatureDate) == 11)&&(day(contract?.appendSignatureDate) >= 20)))
                     gp.month = 12
                 else
                     if(plan.displayableTitle.equals("АВАНС"))
                         gp.month = month(contract?.appendSignatureDate + 30)
                     else//этап
                         gp.month = month(plan.stage.planEndDate + 30)
             
             gp.sum_stage = plan.stage?.budget
             gp.advance = plan.budgetPlan
             gp.sum_precent_nds = (parse(contract?.lot?.ndsAmount?.title))
             
             if ((gp.sum_precent_nds != null) && (gp.sum_stage != null))
                 gp.sum_nds = count(gp.sum_stage * gp.sum_precent_nds)
             
             gp.sum_precent_nds = decFormat(parse(contract?.lot?.ndsAmount?.title))
             gp.date_pay_plan = "isEmptyYet"
             gp.sum_payment = plan.budgetPlan
             gp.date_act = timestampToDate(contract?.appendSignatureDate)
             gp_list.add(gp)
         }         
     }
     
     gp_list.size == 0 ? null : gp_list
}
