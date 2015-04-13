package resources.groovy 
/* 
 * L-17692 IR. Скрипт для автоматичсекой смены состояний лотов, связанных с НКС http://...te.naumen.ru:10305/lab_labour/show/17692 
 * 
 * Скрипт, исполняемый при смене состояния заседания, меняющий состояния лотов заседания на заданное 
 * 
 * @author aboronnikov 
 * 
 * */ 

import java.util.HashMap 
import java.lang.IllegalStateException 
import ru.naumen.core.ui.BKUIUtils 
import ru.naumen.ccamcore.bobject.bactions.SetCCAMStageBusinessAction 

stateIdentificator = 'published' 

conference = subject 
//conference = helper.get("cnfrncfs000080000jmt8ssn0bq9b2mc") 

cases = helper.select("select distinct rel.lot.ccamBOCase from ConferenceLotRelation rel where rel.conference.id='$conference.UUID'") 
targetStates = new HashMap() 
for(caze in cases) 
{ 
 stateFound = false 
 for(state in caze.stagesWorkflowDefinition.getStates(session)) 
 { 
 if(stateIdentificator.equals(state.identificator)) 
 { 
 targetStates.put(caze, state) 
 stateFound = true 
 break 
 } 
 } 
 if(!stateFound) 
 { 
 throw new IllegalStateException("В категории '$caze' отсутствует состояние '$stateIdentificator'") 
 } 
} 

user = BKUIUtils.currentPerson 

lots = helper.select("select rel.lot from ConferenceLotRelation rel where rel.conference.id='$conference.UUID'") 
for(lot in lots) 
{ 
 new SetCCAMStageBusinessAction(lot, targetStates.get(lot.ccamBOCase), null, user).execute(session) 
}