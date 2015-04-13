package resources.groovy

import ru.naumen.fcntp.bobject.requirement.draft.ba.ValidateRequirementDraftAllFieldsSetBA;

def validation = new ValidateRequirementDraftAllFieldsSetBA( subject,
ValidateRequirementDraftAllFieldsSetBA.VAL_ACCEPT_FORMATION_STATE );
helper.execute(){ s ->
validation.execute( s );

def emptyFieldMsg = [];
def i = 0;
for ( emptyField in validation.getEmptyFields().entrySet())
{
println emptyField.getValue().getKey();
emptyFieldMsg[i++] = "<br>&nbsp;&nbsp;&nbsp;&nbsp; - " + emptyField.getValue().getKey();
}

if( emptyFieldMsg.size() > 0 )
{
def value = "<p class=\"error\"><font size=\"2\"><b><i>Перед завершением формирования заявки, должны быть заполнены все ключевые поля:</i></b></font>" + emptyFieldMsg.join(",") + "</p>";
def exc = new ru.naumen.guic.components.forms.UIForm.UIFormUserException( value )
exc.setHtmlMessage( true );
throw exc;
}
}