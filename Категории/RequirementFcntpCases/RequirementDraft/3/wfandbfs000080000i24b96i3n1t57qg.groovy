/*	 import ru.naumen.fcntp.workflow.SendMailScript 
import ru.naumen.wcf.engine.urls.URLCreator 

def newPerson = helper.get("corebofs000080000im5cn45rhbe0jgk") 
person = ((ru.naumen.ccamcore.security.IAuthorHandler)ru.naumen.core.hibernate.bactions.BusinessActionBase.unproxy(subject)).getAuthor() 
website = ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler.getCatalogByCode("requirementDraftPrograms").getItem(ru.naumen.fcntp.FcntpActivator.MAIN_PROGRAM_UUID).getWebSite() 
vars = ["respectable":SendMailScript.getRespectablePhrase(person), "firstName":person.firstName, "middleName":person.middleName, "idDraft":subject.identifier, "numberDraft":subject.incomeNumber, "draftUrl":URLCreator.createFullLinkToPublishedObject(subject), "web-site":website] 

new SendMailScript().setFeedback(newPerson.email).execute(person, "DraftStageLetter/title", "DraftStageLetter/body_FARMA", vars) 
new SendMailScript().execute(newPerson, "DraftStageLetter/title", "DraftStageLetter/body_FARMA", vars)
*/

import ru.naumen.fcntp.workflow.SendMailScript 
import ru.naumen.wcf.engine.urls.URLCreator 

//Пользователь: Шуртаков Константин Владимирович 
//def newPerson = helper.get("corebofs000080000im5cn45rhbe0jgk") - для рабочего сервера
 def newPerson = helper.get("corebofs000080000h821no0do3sser4") //для тестовых 
person = ((ru.naumen.ccamcore.security.IAuthorHandler)ru.naumen.core.hibernate.bactions.BusinessActionBase.unproxy(subject)).getAuthor()
 website = ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler.getCatalogByCode("requirementDraftPrograms").getItem(ru.naumen.fcntp.FcntpActivator.MAIN_PROGRAM_UUID).getWebSite()
 vars = ["respectable":SendMailScript.getRespectablePhrase(person), "firstName":person.firstName, "middleName":person.middleName, "idDraft":subject.identifier, "numberDraft":subject.incomeNumber, "draftUrl":URLCreator.createFullLinkToPublishedObject(subject), "web-site":website]

new SendMailScript().setFeedback(newPerson.email).execute(person, "DraftStageLetter/DraftStageLetter_title", "DraftStageLetter/DraftStageLetter_body_FARMA", vars)
new SendMailScript().execute(newPerson, "DraftStageLetter/DraftStageLetter_title", "DraftStageLetter/DraftStageLetter_body_FARMA", vars)