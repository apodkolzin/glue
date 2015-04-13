//Шведчиков А.: данный скрипт высылает электронное уведомление персоне, UUID которой указан в методе helper.get("<UUID>")
//тема и тело письма задаты в отдельных аргументах методов setFeedback и execute: "DraftStageLetter/title" и "DraftStageLetter/body"

import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.wcf.engine.urls.URLCreator

def newPerson = helper.get("corebofs000080000hcuv51h8is0h0q4")
person = ((ru.naumen.ccamcore.security.IAuthorHandler)ru.naumen.core.hibernate.bactions.BusinessActionBase.unproxy(subject)).getAuthor()
website = ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler.getCatalogByCode("requirementDraftPrograms").getItem(ru.naumen.fcntp.FcntpActivator.MAIN_PROGRAM_UUID).getWebSite()
vars = ["respectable":SendMailScript.getRespectablePhrase(person), "firstName":person.firstName, "middleName":person.middleName, "idDraft":subject.identifier, "numberDraft":subject.incomeNumber, "draftUrl":URLCreator.createFullLinkToPublishedObject(subject), "web-site":website]

new SendMailScript().setFeedback(newPerson.email).execute(person, "DraftStageLetter/title", "DraftStageLetter/body", vars)
new SendMailScript().execute(newPerson, "DraftStageLetter/title", "DraftStageLetter/body", vars)