import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.wcf.engine.urls.URLCreator
import ru.naumen.core.files.DBFileHibernateHandler;
import ru.naumen.guic.components.forms.UIForm.UIFormUserException; 
import ru.naumen.ccamcore.logging.CCAMLogUtil;
import ru.naumen.core.ui.BKUIUtils;

person = ru.naumen.core.hibernate.bactions.BusinessActionBase.unproxy(subject).contactEmployee
website = ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler.getCatalogByCode("requirementDraftPrograms").getItem(ru.naumen.fcntp.FcntpActivator.MAIN_PROGRAM_UUID).getWebSite()
file = "resultFormalExper";
vars = ["respectable":SendMailScript.getRespectablePhrase(person),
        "firstName":person.firstName, 
        "middleName":person.middleName,
        "idDraft":subject.identifier,
        "numberDraft":subject.incomeNumber,
        "draftUrl":URLCreator.createFullLinkToPublishedObject(subject),
        "web-site":website,
       ]
  
if(file?.trim())
{
  request= "from DBFile f where f.parentWrapper.id=:parentUUID and f.fileCase.code=:case and f.parentFile is null and f.creationDate = (select max(ff.creationDate) from DBFile ff where ff.parentWrapper.id=:parentUUID and ff.fileCase.code=:case and ff.parentFile is null )";
  vars["attached-files"] = session.createQuery(request).setParameter("parentUUID", subject.getUUID()).setParameter("case", file).list();
}

if(vars["attached-files"] == null || vars["attached-files"].isEmpty())
    throw new UIFormUserException("Внимание, прежде чем изменить состояние, необходимо провести экспертизу Предложения");

new SendMailScript().execute(person, "DraftStageLetter/title", "DraftStageLetter/body", vars)

def LOG_MSG_CAPTION = "Рассылка уведомлений";
def LOG_MSG_CONTENT_TPL = "Рассылка уведомлений на email адреса: <%s>";
helper.execute()
{
  s ->
   CCAMLogUtil.save2Log(subject.getUUID(), BKUIUtils.getCurrentPerson().getUUID(), LOG_MSG_CAPTION, s, String.format(LOG_MSG_CONTENT_TPL, person.getMailList().join(", ")))
}