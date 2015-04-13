import ru.naumen.wcf.engine.urls.URLCreator
import ru.naumen.fcntp.workflow.SendSimpleMailScript

def feedbackPerson = helper.get("corebofs000080000i7o35c6a0hjo0qg")
def notificationPerson = ((ru.naumen.ccamcore.security.IAuthorHandler)ru.naumen.core.hibernate.bactions.BusinessActionBase.unproxy(subject)).getContactPerson()

new SendSimpleMailScript().setFeedback(feedbackPerson.email).execute(null, "DraftStageLetter/title", "DraftStageLetter/body_KADR", getVars(notificationPerson))

new SendSimpleMailScript().execute(null, "DraftStageLetter/title", "DraftStageLetter/body_KADR", getVars(feedbackPerson))

def getVars(person)
{
  def vars = ["respectable":getRespectable(person), "firstName":person.firstName, "middleName":person.middleName, "idDraft":subject.identifier, 
        "numberDraft":subject.incomeNumber, "draftUrl":URLCreator.createFullLinkToPublishedObject(subject), 
        "web-site":ru.naumen.core.catalogsengine.CoreCatalogHibernateHandler.getCatalogByCode("requirementDraftPrograms").getItem(ru.naumen.fcntp.FcntpActivator.MAIN_PROGRAM_UUID).getWebSite(),
        "email":person.email]
  
  if (person.additionalEmail != null && !person.additionalEmail.isEmpty())
  {
     vars.put("email", [person.email, person.additionalEmail])
  }
  
  return vars
}

def getRespectable(person)
{
  def sex = person.sex
  return (sex == null) ? (("Уважаемый(ая)")) : ("male".equalsIgnoreCase(sex.getCode())) ? (("Уважаемый")) : (("Уважаемая"));
}