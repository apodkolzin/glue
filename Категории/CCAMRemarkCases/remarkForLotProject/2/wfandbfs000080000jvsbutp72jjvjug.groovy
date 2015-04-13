import org.apache.log4j.Logger
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.fcntp.bobject.lot.LotFcntp
import ru.naumen.fcntp.bobject.lot.LotFcntpManagerRelation
import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.wcf.exceptions.UIException

/**
* Настройки ----------------------------------------------------------------------------------------------------------
*/

// персоны, которым необходимо высылать уведомления (названия свойств в Лоте, возвращающих нужные персоны)
def recieverSettings = [
'manager'
]

// коды каталогов для заголовка и тела письма уведомления
def mailCatalogCodes = [
'title' : 'RemarkOwnerDocumentLetter/remarkProjectLotTitle',
'body' : 'RemarkOwnerDocumentLetter/remarkProjectLotBody']

noRemarksCatalogCodes = [ 'uuid':null,
'title': 'RemarkOwnerDocumentLetter/NoRemarkProjectLot_title',
'body': 'RemarkOwnerDocumentLetter/NoRemarkProjectLot_body']

/**
* Methods -----------------------------------------------------------------------------------------------------------
*/

final Logger logger = Logger.getLogger(this.class)


def sendMailToEmployee = {EmployeeStech employee, LotFcntp lot ->
if (null != employee)
{

def parameters = [
'respectable': SendMailScript.getRespectablePhrase(employee),
'first-name': employee.firstName,
'second-name': employee.middleName,

]

if (null != lot.stateCustomer) parameters['org-fullTitle'] = lot.stateCustomer.title

if (null != lot)
{
parameters['lot-number'] = lot.fullNumber
parameters['lot-theme'] = lot.theme
parameters['UUID'] = subject.UUID
parameters['Author-title'] = subject.remarkAuthor.person.title


} else
logger.getLogger(this.class).warn("[LotProjectNotificationScript.groovy] E-mail notification text could have missed parameters. The Lot Object is NULL.")

if(subject.hasNoRemarks){
	mailCatalogCodes = noRemarksCatalogCodes
}

new SendMailScript().setFeedback(employee.email).execute(employee, mailCatalogCodes.title, mailCatalogCodes.body, parameters)
}
}

/**
* Main ---------------------------------------------------------------------------------------------------------------
*/


def LotFcntp lot
try
{
lot = (LotFcntp) BusinessActionBase.unproxy(subject.parent)
} catch (e)
{
try
{
lot = (LotFcntp) BusinessActionBase.unproxy(subject.parent.parent)
} catch (Exception e1)
{
throw new UIException('Невоможно получить лот', e1)
}
}

def recieverManagers = helper.printQuery("from "+LotFcntpManagerRelation.class.getName()+" lm where leftBO ='" +lot.getUUID()+"'");
recieverManagers.each { recieverPerson ->
if (((LotFcntpManagerRelation)recieverPerson).getRightBO())
sendMailToEmployee(BusinessActionBase.unproxy(((LotFcntpManagerRelation)recieverPerson).getRightBO()), lot)

}