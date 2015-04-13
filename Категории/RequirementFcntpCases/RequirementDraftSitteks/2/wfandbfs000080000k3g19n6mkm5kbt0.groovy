package resources.groovy 

import ru.naumen.fcntp.bobject.requirement.draft.ba.ValidateRequirementDraftAllFieldsSet1420BA; 
import org.apache.commons.lang3.StringUtils; 

def validation = new ValidateRequirementDraftAllFieldsSet1420BA( subject, 
ValidateRequirementDraftAllFieldsSet1420BA.VAL_EMPTY_FIELDS | 
ValidateRequirementDraftAllFieldsSet1420BA.VAL_EF_POST_CREATE | 
ValidateRequirementDraftAllFieldsSet1420BA.VAL_FILES_CATS | 
ValidateRequirementDraftAllFieldsSet1420BA.VAL_SORT_AFTER); 

helper.execute(){ s -> 
validation.execute( s ); 

def value = ""; 

def emptyFieldMsg = []; 
def i = 0; 
def blockTitles = [ 
contactPerson: "Представитель организации - контактное лицо", 
editMaster: "Представитель организации - уполномоченное лицо" 
]; 

for ( emptyField in validation.getEmptyFields().entrySet()) 
{ 
println emptyField.getValue().getKey(); 

def blockTitle = blockTitles[ ValidateRequirementDraftAllFieldsSet1420BA.getBlockName( emptyField.getKey() ) ]; 

println blockTitle; 
println ValidateRequirementDraftAllFieldsSet1420BA.getBlockName( emptyField.getKey() ); 

emptyFieldMsg[i++] = "<br>&nbsp;&nbsp;&nbsp;&nbsp; - " + emptyField.getValue().getKey() + 
( StringUtils.isNotEmpty( blockTitle ) ? "(" + blockTitle + ")" : "" ); 
} 

for ( emptyField in validation.getEmptyFieldsPostCreate().entrySet()) 
{ 
println emptyField.getValue(); 
emptyFieldMsg[i++] = "<br>&nbsp;&nbsp;&nbsp;&nbsp; - " + emptyField.getValue(); 
} 

def jj=0; 
def ii=0; 
uniqueEmptyFieldMsg = []; 
prev=""; 
for(ii=0;ii<emptyFieldMsg.size;ii++) 
{ 
if(!prev.equals(emptyFieldMsg[ii])) 
{ 
uniqueEmptyFieldMsg[jj++] = emptyFieldMsg[ii]; 
} 
prev = emptyFieldMsg[ii]; 
} 

if( uniqueEmptyFieldMsg.size() > 0 ) 
{ 
value = value + "<p class=\"error\"><font size=\"2\"><b><i>Перед завершением формирования заявки должны быть заполнены все ключевые поля:</i></b></font>" + uniqueEmptyFieldMsg.join("") + "</p>"; 
} 

def noMajorDocsMsg = []; 
def j = 0; 
def fileCategories = [ 
// "37", //код "Пояснительная записка" 
// "106" //код "Техническое задание" 
] 

for (majorDoc in validation.getAbsentMajorDocs(s, fileCategories)) 
{ 
println majorDoc; 
noMajorDocsMsg[j++] = "<br>&nbsp;&nbsp;&nbsp;&nbsp; - " + majorDoc; 
} 

if ( noMajorDocsMsg.size() > 0 ) 
{ 
value = value + "<p class=\"error\"><font size=\"2\"><b><i>К заявке должны быть прикреплены следующие документы:</i></b></font>" + noMajorDocsMsg.join(",") + "</p>"; 
} 

if (!StringUtils.isEmpty(value)) 
{ 
def exc = new ru.naumen.guic.components.forms.UIForm.UIFormUserException(value) 
exc.setHtmlMessage(true); 
throw exc; 
} 

}