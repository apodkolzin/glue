import javax.xml.bind.*
import javax.xml.bind.annotation.*
import java.text.SimpleDateFormat
import ru.naumen.common.utils.CollectionUtils


/**
 * FCNTP-864 Формирование файла со сведениями о реквизитах Организации для ФИНМОН (BO_004_ИНН_КПП_МК_CA.xml)
 * 
 * @author aboronnikov
 *
 * Скрипт генерирует XML-файлы с данными об организациях и упаковывает их в zip-архив
 *
 */


String encoding = 'windows-1251'
JAXBContext context = JAXBContext.newInstance(OrganizationReqisites.class)
Marshaller marshaller = context.createMarshaller()
marshaller.setProperty( Marshaller.JAXB_ENCODING , encoding )
marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
  

   def xmlReport = getOrganizationXML(organization, marshaller)
return xmlReport

def getOrganizationXML(organization, marshaller)
{
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy")

    def reqs = new OrganizationReqisites()
    reqs.dateTime = dateFormat.format(new Date())
    reqs.title = 'Сведения о контрагенте'
    reqs.ca = new Ca()
    reqs.ca.fsource = 'DR'
    reqs.ca.mkod = organization.machineCode?:'0000'
    reqs.ca.fname = organization.title
    reqs.ca.sname = organization.shortTitle
    reqs.ca.inn = organization.INN
    reqs.ca.kpp = organization.KPP
    reqs.ca.id = (reqs.ca.inn?:'') + (reqs.ca.kpp?:'')
    reqs.ca.country = new CodeAndName()
    reqs.ca.country.code = '643'
    reqs.ca.country.name = 'Российская Федерация'
    reqs.ca.type = new CodeAndName()
    reqs.ca.type.code = 'U'
    reqs.ca.type.name = 'Юридическое лицо РФ'
    reqs.ca.address = organization.juridicalAddress?.fullTitle
    reqs.ca.address_p = organization.postAddress?.fullTitle
    reqs.ca.opf = new CodeAndName()
    
    reqs.ca.opf.code = organization.OKOPF?.code?.replace(' ', '')
    if(reqs.ca.opf.code?.length()<5)
    {
    	reqs.ca.opf.code = null
    }
    
    reqs.ca.opf.name = organization.OKOPF?.title
    reqs.ca.okfs = organization.OKFS?.code
    reqs.ca.okato = organization.OKATO
    if(!CollectionUtils.isEmptyCollection(organization.OKVED))
    {
       reqs.ca.okved = organization.OKVED.toArray()[0].code
    }
    
    reqs.ca.ogrn = organization.OGRN
    reqs.ca.ogrnDate = organization.OGRNDate!=null?dateFormat.format(organization.OGRNDate):null
    reqs.ca.okpo = organization.OKPO
    reqs.ca.kodVed = organization.govDepartment?.code
    reqs.ca.okogu = organization.OKOGU?.code
    reqs.ca.liderPost = organization.chief?.post
    reqs.ca.liderFio = organization.chief?.displayableTitle
    reqs.ca.liderPhone = organization.chief?.cityPhoneNumber
    reqs.ca.liderEmail = organization.chief?.email
    reqs.ca.personEmail = organization.website
    
    ByteArrayOutputStream xmlReport = new ByteArrayOutputStream()
    
    marshaller.marshal(reqs, xmlReport)

    return xmlReport
}

@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement(name="data")
class OrganizationReqisites
{
	@XmlAttribute(name = "datetime")
    String dateTime
    
    @XmlAttribute(name = "title")
    String title

	@XmlElement(name = "ca")
	Ca ca
}

  
@XmlAccessorType( XmlAccessType.FIELD )
@XmlRootElement(name="ca")
class Ca
{
    @XmlElement(name = "fsource")
    String fsource
    
    @XmlElement(name = "mkod")
    String mkod
    
    @XmlElement(name = "fname")
    String fname
    
    @XmlElement(name = "sname")
    String sname
    
    @XmlElement(name = "type")
    CodeAndName type
    
    @XmlElement(name = "country")
    CodeAndName country
    
    @XmlElement(name = "inn")
    String inn
    
    @XmlElement(name = "kpp")
    String kpp
    
    @XmlElement(name = "id")
    String id
    
    @XmlElement(name = "address")
    String address
    
    @XmlElement(name = "address_p")
    String address_p
    
    @XmlElement(name = "opf")
    CodeAndName opf
    
    @XmlElement(name = "okfs")
    String okfs
    
    @XmlElement(name = "okato")
    String okato
    
    @XmlElement(name = "okved")
    String okved
    
    @XmlElement(name = "ogrn")
    String ogrn
    
    @XmlElement(name = "ogrn_date")
    String ogrnDate
    
    @XmlElement(name = "okpo")
    String okpo
    
    @XmlElement(name = "kod_ved")
    String kodVed
    
    @XmlElement(name = "okogu")
    String okogu
    
    @XmlElement(name = "lider_post")
    String liderPost
    
    @XmlElement(name = "lider_fio")
    String liderFio
    
    @XmlElement(name = "lider_phone")
    String liderPhone
    
    @XmlElement(name = "lider_email")
    String liderEmail
    
    @XmlElement(name = "person_email")
    String personEmail
}

@XmlAccessorType( XmlAccessType.FIELD )
class CodeAndName
{
   @XmlElement(name = "code")
   String code
   
   @XmlElement(name = "name")
   String name
}