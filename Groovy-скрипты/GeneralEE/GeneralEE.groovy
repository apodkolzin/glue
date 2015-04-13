package resources.groovy.mon.contracts

//import Bc;

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.ArrayList;

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlValue
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlAttribute

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
 * Скрипт генерирует XML-файл с данными об общих реквизитах организации 
 * 
 * Источники значений для тегов XML-файла находятся в файле, приложенном к задаче
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/14704
 * 
 */


ca_list = []//данные для xml файла
for (contract in contractList)
{
    ca_list.add(GreateXmlData(contract))//собираем данные для XML файла    
}

if (ca_list.size())//генерим файл по контрактам
{
    data = new Data()
    data.ca_list = ca_list
    ByteArrayOutputStream xmlReport = new ByteArrayOutputStream();
    JAXBContext context = JAXBContext.newInstance(data.class);
    Marshaller marshaller = context.createMarshaller();
    marshaller.marshal(data, xmlReport);
    //helper.download(xmlReport.toByteArray(), "BO_002_KP_DM_CA_" + date.dateString + "_"+ date.timeString + ".xml")
    /*File newFile = new File(fileName);
    fileWriter = new FileWriter(newFile)
    fileWriter.write(xmlReport.toString())
    fileWriter.close()
    return newFile*/
    //fileFtpUpload(newFile, fileName)
    return xmlReport
}

/*def fileFtpUpload(def newFile, def fileName)
{
    FTPClient client = new FTPClient()
    FileInputStream fis = null;    
    try
    {
        client.connect("node4.net2ftp.ru")
        client.login("leha140486@yandex.ru", "ec13225090c8")
        //if (!client.isDirectory("OrganizationDetails"))
        client.makeDirectory("OrganizationDetails")        
        client.changeWorkingDirectory("OrganizationDetails")
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
}*/



   
Ca GreateXmlData (def contract)
{
    ca = new Ca()

    ca.yyyy = checkString(contract?.yearBO)
    ca.nnnnn = checkString(contract?.uniqueNumberBO)

    fname = new Fname()
    fname.fname = checkString(contract?.performer?.title)
    if (fname.fname != null)
        ca.fname = fname
    
    sname = new Sname()
    sname.sname = checkString(contract?.performer?.shortTitle)
    if (sname.sname != null)
        ca.sname = sname

    inn = new Inn()
    inn.inn = checkString(contract?.performer?.INN)
    if (inn.inn != null)
        ca.inn = inn

    kpp = new Kpp()
    kpp.kpp = checkString(contract?.performer?.KPP)
    if (kpp.kpp != null)
        ca.kpp = kpp    
    
    mkod = new Mkod()
    mkod.mkod = checkString(contract?.performer?.machineCode)
    if (mkod.mkod != null)
        ca.mkod = mkod    

    country = new Country()
    ca.country = country

    address = new Address()
    address.address = checkString(contract?.performer?.juridicalAddress?.fullTitle)
    if (address.address != null)
        ca.address = address

    address_p = new AddressP()
    address_p.address_p = checkString(contract?.performer?.postAddress?.fullTitle)
    if (address_p.address_p != null)
        ca.address_p = address_p
        
    okfs = new Okfs()
    okfs.okfs = checkString(contract?.performer?.OKFS?.code)
    if (okfs.okfs != null)
        ca.okfs = okfs

    okopf = new Okopf()
    okopf.okopf = checkString(contract?.performer?.OKOPF?.code)
    if (okopf.okopf != null)
        ca.okopf = okopf
    
    okato = new Okato()
    okato.okato = checkString(contract?.performer?.OKATO)
    if (okato.okato != null)
        ca.okato = okato
    
/*okved = new Okved()
okved.okved = checkString(contract?.performer?.OKVED.toArray()[0]?.title)
if (okved.okved != null)
    ca.okved = okved
*/
    ogrn = new Ogrn()
    ogrn.ogrn = checkString(contract?.performer?.OGRN)
    if (ogrn.ogrn != null)
        ca.ogrn = ogrn

    ogrn_date = new OgrnDate()
    ogrn_date.ogrn_date = timestampToDate(contract?.performer?.OGRNDate)
    if (ogrn_date.ogrn_date != null)
        ca.ogrn_date = ogrn_date
    
    okpo = new Okpo()
    okpo.okpo = checkString(contract?.performer?.OKPO)
    if (okpo.okpo != null)
        ca.okpo = okpo            
    
    lider_post = new LiderPost()
    lider_post.lider_post = checkString(contract?.performer?.chief?.post)
    if (lider_post.lider_post != null)
        ca.lider_post = lider_post

    lider_fio = new LiderFio()
    lider_fio.lider_fio = checkString(contract?.performer?.chief?.title)
    if (lider_fio.lider_fio != null)
        ca.lider_fio = lider_fio
    
    lider_phone = new LiderPhone()
    lider_phone.lider_phone = checkString(contract?.performer?.chief?.cityPhoneNumber)
    if (lider_phone.lider_phone != null)
        ca.lider_phone = lider_phone

    lider_email = new LiderEmail()
    lider_email.lider_email = checkString(contract?.performer?.chief?.email)
    if (lider_email.lider_email != null)
        ca.lider_email = lider_email
    
    return ca
}

@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
class Data
{
    @XmlElementWrapper
    @XmlElement(name = "ca")
    ArrayList<Ca> ca_list
    @XmlAttribute
    String title = "Сведения о контрагенте бюджетного обязательства (IKasCA)"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Ca
{   
    
    @XmlAttribute
    String title = "Реквизиты контрагента"
    String yyyy
    String nnnnn
    String type = "U"
    Fname fname   
    Sname sname
    Inn inn
    Kpp kpp
    Mkod mkod
    Country country
    Address address
    AddressP address_p
    Okfs okfs
    Okopf okopf
    Okato okato
    Okved okved
    Ogrn ogrn
    OgrnDate ogrn_date
    Okpo okpo
    LiderPost lider_post
    LiderFio lider_fio
    LiderPhone lider_phone
    LiderEmail lider_email
}

@XmlAccessorType( XmlAccessType.FIELD )
class Fname
{
    @XmlValue
    String fname
    @XmlAttribute
    String title = "Полное наименование контрагента: наименование юридического лица или фамилия, имя, отчество физического лица – поставщика товаров, работ, услуг в соответствии с государственным контрактом или договором"
    
}

@XmlAccessorType( XmlAccessType.FIELD )
class Sname
{
    @XmlValue
    String sname
    @XmlAttribute
    String title = "Краткое наименование контрагента: по уставу для юридического лица или фамилия и инициалы для физического лица"    
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

@XmlAccessorType( XmlAccessType.FIELD )
class Address
{
    @XmlValue
    String address
    @XmlAttribute
    String title = "Юридический адрес для юридических лиц и место регистрации для физических лиц"
}

@XmlAccessorType( XmlAccessType.FIELD )
class AddressP
{
    @XmlValue
    String address_p
    @XmlAttribute
    String title = "Почтовый адрес"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Okfs
{
    @XmlValue
    String okfs
    @XmlAttribute
    String title = "Код формы собственности"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Okopf
{
    @XmlValue
    String okopf
    @XmlAttribute
    String title = "Код организационно-правовой формы"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Okato
{
    @XmlValue
    String okato
    @XmlAttribute
    String title = "Код ОКАТО"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Okved
{
    @XmlValue
    String okved
    @XmlAttribute
    String title = "Код ОКВЭД (основной)"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Ogrn
{
    @XmlValue
    String ogrn
    @XmlAttribute
    String title = "ОГРН"
}

@XmlAccessorType( XmlAccessType.FIELD )
class OgrnDate
{
    @XmlValue
    String ogrn_date
    @XmlAttribute
    String title = "Дата назначения ОГРН"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Okpo
{
    @XmlValue
    String okpo
    @XmlAttribute
    String title = "Код ОКПО"
}

@XmlAccessorType( XmlAccessType.FIELD )
class LiderPost
{
    @XmlValue
    String lider_post
    @XmlAttribute
    String title = "Должность руководителя"
}

@XmlAccessorType( XmlAccessType.FIELD )
class LiderFio
{
    @XmlValue
    String lider_fio
    @XmlAttribute
    String title = "ФИО руководителя"
}

@XmlAccessorType( XmlAccessType.FIELD )
class LiderPhone
{
    @XmlValue
    String lider_phone
    @XmlAttribute
    String title = "Телефон руководителя"
}

@XmlAccessorType( XmlAccessType.FIELD )
class LiderEmail
{
    @XmlValue
    String lider_email
    @XmlAttribute
    String title = "Адрес электронной почты руководителя"
}



@XmlAccessorType( XmlAccessType.FIELD )
class Country
{
    int code = 643
    String name = "Российская Федерация"
    @XmlAttribute
    String title = "Страна регистрации"
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





