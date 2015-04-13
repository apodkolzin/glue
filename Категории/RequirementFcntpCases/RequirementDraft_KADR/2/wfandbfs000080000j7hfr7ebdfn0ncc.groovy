package resources.groovy 

import ru.naumen.fcntp.bobject.requirement.draft.ba.ValidateRequirementDraftAllFieldsSetBA; 

def validation = new ValidateRequirementDraftAllFieldsSetBA( subject, 
	ValidateRequirementDraftAllFieldsSetBA.VAL_ACCEPT_FORMATION_STATE |
        ValidateRequirementDraftAllFieldsSetBA.VAL_FILES_CATS |
        0x0A ); 

//категории "Пояснительная записка" и "Техническое задание.
validation.setFileCatsList([ "37", "35" ]);
validation.setAcceptEmptyField([
      "title",
//      "description",      
      "programAction",
      
      "projectView",      
      
      "authorOrg.title",
      "authorPerson.email",
      
    //  "wwLastName",
    //  "wwFirstName",
    //  "wwCityPhone",
    //  "wwEmail",
      
      "beginWork",
      "endWork",      

      "requirementDraftWorkPrice"
]);

helper.execute(){ s -> 
  validation.execute( s ); 

  def emptyFieldMsg = []; 
  def i = 0; 
  for ( emptyField in validation.getEmptyFields().entrySet()) 
  { 
    println emptyField.getValue().getKey(); 
    emptyFieldMsg[i++] = "<br>&nbsp;&nbsp;&nbsp;&nbsp; - " + emptyField.getValue().getKey(); 
  } 

  def value = '';
  if( emptyFieldMsg.size() > 0 ) 
    value = value + "<p class=\"error\"><font size=\"2\"><b><i>Перед завершением формирования заявки, должны быть заполнены все ключевые поля:</i></b></font>" + emptyFieldMsg.join(",") + "</p>"; 
  
  if( validation.getFilesMessage() ) 
    if( value )
      value = value + "<p class=\"error\">" + validation.getFilesMessage() + "</p>";
    else
      value = "<p class=\"error\"><font size=\"2\"><b><i>Перед завершением формирования заявки, должны быть добавлены необходимые файлы</i></b></font><br><br>" + validation.getFilesMessage() + "</p>";
     
  if( value.size() > 0 )
  {
    def exc = new ru.naumen.guic.components.forms.UIForm.UIFormUserException( value ) 
    exc.setHtmlMessage( true ); 
    throw exc; 
  } 
}
