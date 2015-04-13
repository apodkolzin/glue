package resources.groovy
/*
 *L-17699 Доработать скрипт рассылки уведомления членам заседания НКС http://ssh-gate.naumen.ru:10305/lab_labour/show/17699
 *
 * Скрипт, генерирующий pdf отчет по лотам в заседании, и производящий рассылку уведомлений по электронной почте с приаттаченным отчетом, который только что сгенерил.
 * Если у заседания уже был одноименный файл отчета, он будет удален и заменен новым.
 *
 * @author aboronnikov
 *
 * */ 
 
import ru.naumen.common.mail.MimeMailWrapper
import ru.naumen.fcntp.bobject.conference.ui.SendNotificationConferenceMembersBC
import ru.naumen.fcntp.bobject.conference.acceptance.ui.SendAcceptanceNotificationConferenceMembersBC
import ru.naumen.core.ui.BKUIUtils
import ru.naumen.fcntp.bobject.conference.ConferenceLotsMailNotificationReport
import ru.naumen.core.bobjects.person.CorePerson
import org.apache.cxf.jaxrs.ext.multipart.InputStreamDataSource
import ru.naumen.fx.objectloader.PrefixObjectLoaderFacade
import ru.naumen.common.mail.ByteArrayDataSource
import javax.activation.DataSource
import ru.naumen.common.utils.DateUtils
import ru.naumen.core.files.DBFileHibernateHandler
import ru.naumen.core.files.DeleteDBFileBA
  

conference = subject
//Для дебага в консоли:
//conference = helper.get('cnfrncfs000080000ju3sufhtqj6atlg')
templateId = 'txttmpfs000080000k8g176p4i8hkji8'
person = BKUIUtils.currentPerson
 
fileName = getFileName(conference)
report = new ConferenceLotsMailNotificationReport(conference, (CorePerson)person, 'pdf', templateId, fileName)
  
//Удаляем файлы с этим именем, если вдруг они уже есть
for(existingFile in DBFileHibernateHandler.listDBFilesWithFilename(session, conference, fileName))
{
       new DeleteDBFileBA(existingFile).execute(session)
}

report.createFile(session)
file = report.file
  
controller = getController(conference, file)
controller.sendNotifications(person, conference)

//Вовзращает название отчета
def getFileName(conference)
{
	return "Материалы для рассмотрения_№" + conference.identifier + "_" + DateUtils.date2Str(conference.conferenceDate) + ".pdf"
}

// В зависимости от категории заседания возвращает соответствующие инстансы контроллеров
def getController(conference, file)
{
	if('AcceptanceCommissionConference'.equals(conference.ccamBOCase.code))
        {
        	return new AcceptanceAttachingFileController(file)
        }
	return new AttachingFileController(file)  	
}

class AttachingFileController extends SendNotificationConferenceMembersBC
{
    private _file

    public AttachingFileController(file)
    {
	   _file = file
    }
  
    protected MimeMailWrapper createMailWrap()
    {
       def mailWrap = super.createMailWrap()
       byte[] fileData = _file.getFileData()
       DataSource source = new ByteArrayDataSource( fileData, "content/type")
       mailWrap.attachFile(source, _file.getFileName())
       return mailWrap
    }
}

class AcceptanceAttachingFileController extends SendAcceptanceNotificationConferenceMembersBC
{
    private _file

    public AcceptanceAttachingFileController(file)
    {
	    _file = file
    }
  
    protected MimeMailWrapper createMailWrap()
    {
       def mailWrap = super.createMailWrap()
       byte[] fileData = _file.getFileData()
       DataSource source = new ByteArrayDataSource( fileData, "content/type")
       mailWrap.attachFile(source, _file.getFileName())
       return mailWrap
    }
}