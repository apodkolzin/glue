package resources.groovy.mon.contracts

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.ArrayList;

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue

import ru.naumen.common.utils.DateUtils
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.contract.financing.ContractPaymentHibernateHandler
import ru.naumen.guic.formatters.DoubleFormatter

import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.common.mail.SimpleMimeMailDaemon
import ru.naumen.common.mail.MimeMailWrapper;
import ru.naumen.common.mail.SimpleMailSettings;
import org.apache.log4j.Logger
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.core.ui.BKUIUtils

import ru.naumen.wcf.engine.urls.URLCreator;

import org.apache.commons.net.ftp.FTPClient
import java.io.FileInputStream;
import java.io.IOException;
/**
 * @author ayakovlev
 * Date: 13.05.2013
 * Скрипт генерирует XML-файл с данными о платежных реквизитах организации 
 * 
 * Источники значений для тегов XML-файла находятся в файле, приложенном к задаче
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/14704
 * 
 */



data_list = []//данные для xml файла
for (contract in contractList)
{
    data_list.add(GreateXmlData(contract))//собираем данные для XML файла       
}


if (data_list.size())//генерим файл по контрактам
{
    finData = new FinData()
    finData.data_list = data_list
    ByteArrayOutputStream xmlReport = new ByteArrayOutputStream();
    JAXBContext context = JAXBContext.newInstance(finData.class);
    Marshaller marshaller = context.createMarshaller();
    marshaller.marshal(finData, xmlReport);
    //helper.download(xmlReport.toByteArray(), "BO_002_KP_DM_Account_" + date.dateString + "_"+ date.timeString + ".xml")
    /*File newFile = new File(fileName);
    fileWriter = new FileWriter(newFile)
    fileWriter.write(xmlReport.toString())
    fileWriter.close()
    return newFile*/
    return xmlReport
    //fileFtpUpload(newFile, fileName)
}

/*def fileFtpUpload(def newFile, def fileName)
{
    FTPClient client = new FTPClient()
    FileInputStream fis = null;    
    try
    {
        client.connect("node4.net2ftp.ru")
        client.login("leha140486@yandex.ru", "ec13225090c8")
        //if (!client.isDirectory("PaymentDetails"))
            client.makeDirectory("PaymentDetails")
        
        client.changeWorkingDirectory("PaymentDetails")
        fis = new FileInputStream(newFile)
        client.storeFile(fileName, fis)
        client.logout();
    }
    catch (IOException e)
    {
        e.printStackTrace()
    }
    finally
    {
        try
        {
            if (fis != null)
                fis.close()
            client.disconnect()
        }
        catch (IOException e)
        {
            e.printStackTrace()
        }
    }
}
*/

Data GreateXmlData(def contract)
{
    data = new Data()
    ca = new Ca()
    if (contract?.performer != null)
    {
        ca.yyyy = checkString(contract?.yearBO)
        ca.nnnnn = checkString(contract?.uniqueNumberBO)
    
        if (checkString(contract?.performer?.INN))
        {
            inn = new Inn(inn : contract?.performer?.INN)
            ca.inn = inn
        }
        if (checkString(contract?.performer?.KPP))
        {
            kpp = new Kpp(kpp : contract?.performer?.KPP)
            ca.kpp = kpp
        }
        if (checkString(contract?.performer?.machineCode))
        {
            mkod = new Mkod(mkod : contract?.performer?.machineCode)
            ca.mkod = mkod
        }
    }
    if (!ca.isEmpty())
        data.ca = ca

        account = new Account()

    if (!StringUtilities.isEmptyTrim(ca?.mkod?.mkod)&&(ca?.mkod?.mkod?.length() >= 5))
    {
        bkod = new Bkod(bkod : ca.mkod.mkod.charAt(4))
        account.bkod = bkod
    }

    if (contract?.currentPayReqs != null)
    {
        if (contract?.currentPayReqs?.isCommercial())
        {//коммерческая организация    
            if (checkString(contract?.currentPayReqs?.title))
            {
                bank = new Bank(bank : contract?.currentPayReqs?.title)
                account.bank = bank
            }    
            if (checkString(contract?.currentPayReqs?.BIK))
            {
                bik = new Bik(bik : contract?.currentPayReqs?.BIK)
                account.bik = bik
            }    
            if (checkString(contract?.currentPayReqs?.correspondentAccount))
            {
                ks = new Ks(ks : contract?.currentPayReqs?.correspondentAccount)
                account.ks = ks
            }    
            if (checkString(contract?.currentPayReqs?.settlementAccount))
            {
                rs = new Rs(rs : contract?.currentPayReqs?.settlementAccount)
                account.rs = rs
            }    
            type_br = new TypeBr(code : 1, name : "Коммерческий банк")
            account.type_br = type_br    
        }else{//бюджетная организация    
            if (checkString(contract?.currentPayReqs?.titleOFK))
            {
                bank = new Bank(bank : contract?.currentPayReqs?.titleOFK)
                account.bank = bank
            }    
            if (checkString(contract?.currentPayReqs?.BIKOFK))
            {
                bik = new Bik(bik : contract?.currentPayReqs?.BIKOFK)
                account.bik = bik
            }
    
            if (checkString(contract?.currentPayReqs?.correspondentAccountOFK))
            {
                ks = new Ks(ks : contract?.currentPayReqs?.correspondentAccountOFK)
                account.ks = ks
            }
    
            if (checkString(contract?.currentPayReqs?.simpleTitleOFK))
            {
                otd = new Otd(otd : contract?.currentPayReqs?.simpleTitleOFK)
                account.otd = otd
            }    
            if (checkString(contract?.currentPayReqs?.codeTOFK))
            {
                tofk_num = new TofkNum(tofk_num : contract?.currentPayReqs?.codeTOFK)
                account.tofk_num = tofk_num
            }    
            if (checkString(contract?.currentPayReqs?.exchequerName))
            {
                tofk_name = new TofkName(tofk_name : contract?.currentPayReqs?.exchequerName)
                account.tofk_name = tofk_name
            }    
            if (checkString(contract?.currentPayReqs?.personalAccountOFK))
            {
                tofk_ls = new TofkLs(tofk_ls : contract?.currentPayReqs?.personalAccountOFK)
                account.tofk_ls = tofk_ls
            }    
            if (checkString(contract?.currentPayReqs?.settlementAccountOFK))
            {
                tofk_rs = new TofkRs(tofk_rs : contract?.currentPayReqs?.settlementAccountOFK)
                account.tofk_rs = tofk_rs
            }    
            type_br = new TypeBr(code : 2, name : "Лицевой счет в ТОФК")
            account.type_br = type_br        
        }
    }//if (contract?.currentPayReqs != null)
    if(!account.isEmpty())
        data.account = account

    return data
}    
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
class FinData
{
    @XmlElementWrapper
    @XmlElement(name = "data")
    ArrayList<Data> data_list
}    
    
@XmlAccessorType( XmlAccessType.FIELD )
class Data
{
    Ca ca
    Account account
    @XmlAttribute
    String title
    public boolean isEmpty()
    {
        ca == null && account == null
    }
}
@XmlAccessorType( XmlAccessType.FIELD )
class Ca
{   
    String yyyy
    String nnnnn
    Inn inn
    Kpp kpp
    Mkod mkod   
    //@XmlTransient
    public boolean isEmpty()
    {
        inn == null && kpp == null && mkod == null && StringUtilities.isEmptyTrim(yyyy) && StringUtilities.isEmptyTrim(nnnnn)
    }
}
@XmlAccessorType( XmlAccessType.FIELD )
class Account 
{
    Bkod bkod
    Bank bank
    Bik bik
    Ks ks
    Rs rs
    Otd otd
    TofkNum tofk_num
    TofkName tofk_name
    TofkLs tofk_ls
    TofkRs tofk_rs
    TypeBr type_br
    //@XmlTransient
    public boolean isEmpty()
    {
        bkod == null && bank == null && bik == null && ks == null && rs == null && otd == null && tofk_num == null &&
        tofk_name == null && tofk_ls == null && tofk_rs == null && type_br == null
    }
}

@XmlAccessorType( XmlAccessType.FIELD )
class Bkod
{
    @XmlValue
    String bkod
    @XmlAttribute
    String title = "5-й символ машинного кода"    
}

@XmlAccessorType( XmlAccessType.FIELD )
class Bank
{
    @XmlValue
    String bank
    @XmlAttribute
    String title = "Наименование банка контрагента"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Bik
{
    @XmlValue
    String bik
    @XmlAttribute
    String title = "БИК банка контрагента"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Ks
{
    @XmlValue
    String ks
    @XmlAttribute
    String title = "Номер корреспондентского счета банка контрагента"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Rs
{
    @XmlValue
    String rs
    @XmlAttribute
    String title = "Номер банковского счета контрагента"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Otd
{
    @XmlValue
    String otd
    @XmlAttribute
    String title = "Наименование отделения банка"
}

@XmlAccessorType( XmlAccessType.FIELD )
class TofkNum
{
    @XmlValue
    String tofk_num
    @XmlAttribute
    String title = "Номер ТОФК"
}

@XmlAccessorType( XmlAccessType.FIELD )
class TofkName
{
    @XmlValue
    String tofk_name
    @XmlAttribute
    String title = "Наименование ТОФК"
}

@XmlAccessorType( XmlAccessType.FIELD )
class TofkLs
{
    @XmlValue
    String tofk_ls
    @XmlAttribute
    String title = "Лицевой счет в ТОФК"
}

@XmlAccessorType( XmlAccessType.FIELD )
class TofkRs
{
    @XmlValue
    String tofk_rs
    @XmlAttribute
    String title = "Расчетный счет ТОФК в банке"
}
@XmlAccessorType( XmlAccessType.FIELD )
class TypeBr 
{
    String code
    String name
    @XmlAttribute
    String title = "Тип банковского реквизита (СЛОВАРЬ Типы банковских реквизитов)"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Inn
{
    @XmlValue
    String inn
    @XmlAttribute
    String title = "ИНН контрагента"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Kpp
{
    @XmlValue
    String kpp
    @XmlAttribute
    String title = "КПП контрагента"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Mkod
{
    @XmlValue
    String mkod
    @XmlAttribute
    String title = "Машинный код организации"
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





