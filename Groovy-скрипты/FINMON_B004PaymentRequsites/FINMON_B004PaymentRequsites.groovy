import javax.xml.bind.*
import javax.xml.bind.annotation.*
import java.text.SimpleDateFormat
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.contract.requisites.ContractRequisitesHH

/**
 * FCNTP-883 Формирование файла со сведениями о платежных реквизитах для ФИНМОН (BO_004_ИНН_КПП_МК_Account_N.xml)
 *
 * @author aboronnikov
 *
 * Скрипт генерирует XML-файлы с данными о платежных реквизитах организации
 *
 */

String encoding = 'windows-1251'
JAXBContext context = JAXBContext.newInstance(PaymanetReqisites.class)
Marshaller marshaller = context.createMarshaller()
marshaller.setProperty( Marshaller.JAXB_ENCODING , encoding )
marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)

def paymentReqs = binding.variables.containsKey("contract") ?
        getCurrentRequisites(contract) : paymentRequisites;
def org = paymentReqs.organization
report = getPaymentXML(paymentReqs, org, marshaller)

return report



def getPaymentXML(reqs, organization, marshaller)
{
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy")

    def pr = new PaymanetReqisites()
    pr.dateTime = dateFormat.format(new Date())
    pr.title = 'Сведения о банковском счете контрагента'

    pr.ca = new CA()
    pr.ca.mkod = organization.machineCode?:'0000'
    pr.ca.fname = organization.title
    pr.ca.inn = organization.INN
    pr.ca.kpp = organization.KPP
    pr.ca.id = (pr.ca.inn?:'') + (pr.ca.kpp?:'')

    pr.account = new Account()
    if(!StringUtilities.isEmpty(reqs?.numberInMON))
        pr.account.bkod = reqs?.numberInMON?.substring(4,5)

    pr.account.bank = !StringUtilities.isEmpty(reqs?.title)?reqs?.title:reqs?.titleOFK
    pr.account.bik = !StringUtilities.isEmpty(reqs?.BIK)?reqs?.BIK:reqs?.BIKOFK
    pr.account.ks = !StringUtilities.isEmpty(reqs?.correspondentAccount)?reqs?.correspondentAccount:reqs?.correspondentAccountOFK
    pr.account.rs = reqs?.settlementAccount
    pr.account.otd = !StringUtilities.isEmpty(reqs?.address)?reqs?.address:reqs?.simpleTitleOFK
    pr.account.codeTOFK = reqs?.codeTOFK
    pr.account.tofkName = reqs?.exchequerName
    pr.account.tofkLs = reqs?.personalAccountOFK
    pr.account.tofkRs = reqs?.settlementAccountOFK
    pr.account.typeBr = new CodeAndName()

    pr.account.id = reqs?.UUID

    //Поле exchequerName обязательно для бюджетных органихаций
    if(reqs?.exchequerName == null)
    {
        //Бюджетная организация
        pr.account.typeBr.code = 1
        pr.account.typeBr.name = "Коммерческий банк"
    }
    else
    {
        //Коммерческая организация
        pr.account.typeBr.code = 2
        pr.account.typeBr.name = "Лицевой счет в ТОФК"
    }

    ByteArrayOutputStream xmlReport = new ByteArrayOutputStream()
    marshaller.marshal(pr, xmlReport)
    return xmlReport
}

def getCurrentRequisites(contract)
{
    return ContractRequisitesHH.getCurrentRequisites(contract, contract.performer)
}


@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement(name="data")
class PaymanetReqisites
{
    @XmlAttribute(name = "datetime")
    String dateTime

    @XmlAttribute(name = "title")
    String title

    @XmlElement(name = "ca")
    CA ca

    @XmlElement(name = "account")
    Account account
}

@XmlAccessorType( XmlAccessType.FIELD )
class CA
{
    @XmlElement(name = "fname")
    String fname

    @XmlElement(name = "mkod")
    String mkod

    @XmlElement(name = "inn")
    String inn

    @XmlElement(name = "kpp")
    String kpp

    @XmlElement(name = "id")
    String id
}

@XmlAccessorType( XmlAccessType.FIELD )
class Account
{
    @XmlElement(name = "bkod")
    String bkod

    @XmlElement(name = "bank")
    String bank

    @XmlElement(name = "bik")
    String bik

    @XmlElement(name = "ks")
    String ks

    @XmlElement(name = "rs")
    String rs

    @XmlElement(name = "otd")
    String otd

    @XmlElement(name = "tofk_num")
    String codeTOFK

    @XmlElement(name = "tofk_name")
    String tofkName

    @XmlElement(name = "tofk_ls")
    String tofkLs

    @XmlElement(name = "tofk_rs")
    String tofkRs

    @XmlElement(name = "type_br")
    CodeAndName typeBr

    @XmlElement(name = "id")
    String id
}

@XmlAccessorType( XmlAccessType.FIELD )
class CodeAndName
{
    @XmlElement(name = "code")
    String code

    @XmlElement(name = "name")
    String name
}