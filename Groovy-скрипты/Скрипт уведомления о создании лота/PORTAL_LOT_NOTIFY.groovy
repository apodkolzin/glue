/* --------------------------------------- */ 
// subject - lot                              
// URL - direct link to lot in the portal     
// program - program of the lot               
// helper - GroovyHelper                      
// log - system logger                        
/* -----------------------------------------*/

import ru.naumen.fcntp.workflow.SendSimpleMailScript
import ru.naumen.wcf.engine.urls.URLCreator

def action = subject.programAction != null ? 
             subject.programAction : (!subject.programActions.isEmpty() ? 
             subject.programActions[0] : null )

new SendSimpleMailScript().execute( 
   subject, 
   "LotCreateNotifyLetter/title", 
   "LotCreateNotifyLetter/body", 
   [
       email:["oromanova@naumen.ru"],
       lotIdentifier:subject.fullNumber,
       programActionTitle: action == null ? "[не указанно]" : action.title,
       programTitle:program.title, 
       portalLotUrl:lotLink, 
   ]); 
