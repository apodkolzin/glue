package resources.groovy.mon.singleContractXML

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

data = new Ca()
data = GreateXmlData(contract)//собираем данные для XML файла    

ByteArrayOutputStream xmlReport = new ByteArrayOutputStream();
JAXBContext context = JAXBContext.newInstance(Ca.class);
Marshaller marshaller = context.createMarshaller();
marshaller.marshal(data, xmlReport);
return xmlReport

Ca GreateXmlData (def contract)
{
    ca = new Ca()

    
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
    
    person = new Person()
    
    person.f = checkString(contract?.managerFromExecuter?.lastName)
    person.i = checkString(contract?.managerFromExecuter?.firstName)
    person.o = checkString(contract?.managerFromExecuter?.middleName)
    person.mail = checkString(contract?.managerFromExecuter?.email)
    
    
    ca.phone = checkString(contract?.managerFromExecuter?.mobilePhoneNumber)
            
        
    return ca
}

@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement
class Ca
{   
    
    @XmlAttribute
    String title = "Реквизиты контрагента"
    String yyyy
    String nnnnn    
    Fname fname   
    Sname sname
    Inn inn
    Kpp kpp
    Mkod mkod
    Country country
    Address address
    AddressP address_p
    Type type
    Okfs okfs
    Okopf okopf
    Okato okato
    Okved okved
    Ogrn ogrn
    OgrnDate ogrn_date
    Okpo okpo
    Person person
    String phone
}

@XmlAccessorType( XmlAccessType.FIELD )
class Type
{
    String code = "U"
}

@XmlAccessorType( XmlAccessType.FIELD )
class Person
{
    String f
    String i
    String o
    String mail
    
    
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





