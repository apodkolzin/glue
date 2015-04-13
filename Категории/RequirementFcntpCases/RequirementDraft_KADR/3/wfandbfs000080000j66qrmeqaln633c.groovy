import ru.naumen.fcntp.workflow.SendMailScript 
import ru.naumen.wcf.engine.urls.URLCreator 

def newPerson = helper.get("corebofs000080000h14jj5m8v4e12fc")

person = ((ru.naumen.ccamcore.security.IAuthorHandler)ru.naumen.core.hibernate.bactions.BusinessActionBase.unproxy(subject)).getAuthor() 
website = ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler.getCatalogByCode("requirementDraftPrograms").getItem(ru.naumen.fcntp.FcntpActivator.MAIN_PROGRAM_UUID).getWebSite() 
vars = ["respectable":SendMailScript.getRespectablePhrase(person), "firstName":person.firstName, "middleName":person.middleName, "idDraft":subject.identifier, "numberDraft":subject.incomeNumber, "draftUrl":URLCreator.createFullLinkToPublishedObject(subject), "web-site":website] 

new SendMailScript().setFeedback(newPerson.email).execute(person, "DraftStageLetter/title", "DraftStageLetter/body", vars)