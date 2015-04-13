/*package resources.groovy

import ru.naumen.fcntp.bobject.requirement.draft.ba.ValidateRequirementDraftAllFieldsSetBA
import org.apache.commons.lang.StringUtils;

def validation = new ValidateRequirementDraftAllFieldsSetBA( subject, ValidateRequirementDraftAllFieldsSetBA.VAL_ACCEPT_FORMATION_STATE | ValidateRequirementDraftAllFieldsSetBA.VAL_FILES_CATS );

helper.execute(){ s ->
	validation.execute( s ); 
	 
	def emptyFieldMsg = [];
	def i = 0;
    for ( emptyField in validation.getEmptyFields().entrySet())
    {
    	println emptyField.getValue().getKey();
        emptyFieldMsg[i++] = "<br>&nbsp;&nbsp;&nbsp;&nbsp; - " + emptyField.getValue().getKey();
    }    

    def value = "";

    if( emptyFieldMsg.size() > 0 )
    {
	    value = value + "<p class=\"error\"><font size=\"2\"><b><i>Перед завершением формирования заявки, должны быть заполнены все ключевые поля:</i></b></font>" + emptyFieldMsg.join(",") + "</p>";
    }

    if ( !StringUtils.isEmpty(validation.getFilesMessage()))
    {
        value = value + "<p class=\"error\"><font size=\"2\"><b><i>К заявке должны быть прикреплены следующие документы:</i></b></font>" + validation.getFilesMessage() + "</p>";
    }

    if (!StringUtils.isEmpty(value))
    {
        def exc = new ru.naumen.guic.components.forms.UIForm.UIFormUserException( value )
        exc.setHtmlMessage( true );
	    throw exc;
    }
}*/