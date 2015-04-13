import ru.naumen.fcntp.bobject.conference.ConferenceHibernateHandler; 
import ru.naumen.fcntp.bobject.contractstage.acceptancecommission.AddEditAcceptanceCommissionMeetingBA; 
import ru.naumen.fcntp.bobject.contractstage.acceptancecommission.AcceptanceCommissionMeeting; 
import ru.naumen.ccamcore.bobject.ccamlogevent.CCAMLogEvent; 
import ru.naumen.core.logging.LogUtils; 
import ru.naumen.common.utils.DateUtils; 
import ru.naumen.core.ui.BKUIUtils; 

conf=workflowInstance; 
rels= ConferenceHibernateHandler.listConferenceContractStageRelation(conf); 
helper.execute() 
{ 
s-> 
rels.each() 
{ 
rel-> 
meeting = getExistingMeeting(rel, s); 
save = meeting == null; 
if( save ) 
meeting = new AcceptanceCommissionMeeting(); 

meeting.commission = rel.conference.parent; 
meeting.conference = rel.conference; 
meeting.contractStage = rel.stage; 
meeting.meetingDate = rel.conference.conferenceDate; 
meeting.commissionsDecision = rel.commissionsDecision; 

if( save ) 
meeting.doSave(s); 
else 
meeting.doUpdate(s); 

logCreated(meeting, BKUIUtils.getCurrentPerson(), s); 
} 
} 

def logCreated(meeting, author, s) 
{ 
message = getMessage(meeting); 
event = new CCAMLogEvent(meeting.contractStage.getUUID(), "Изменение объекта", author.getUUID(), message); 
LogUtils.saveIntoLog(event, true, session); 
} 

def getMessage(meeting) 
{ 
commission = meeting.commission; 
commissionTitle = commission == null ? "" : commission.getFullNumber(); 
decision = meeting.commissionsDecision; 
commissionDecisionTitle = decision == null ? "" : decision.getTitle(); 
dateStr = DateUtils.date2StrSafe(meeting.meetingDate); 
return String.format("Добавлено заседание приёмочной комиссии:\n " + 
"Дата заседания: %s \n" + 
"Комиссия: %s \n" + 
"Решение комиссии: %s", dateStr, commissionTitle, commissionDecisionTitle); 
} 

def getExistingMeeting(rel, s) 
{ 
meetings = s.createQuery("From AcceptanceCommissionMeeting m where m.contractStage=:stage and m.commission=:commission and m.conference=:conference").setParameter('stage',rel.stage).setParameter('commission',rel.conference.parent).setParameter('conference',rel.conference).list(); 
return (meetings.size() > 0) ? meetings.get(0) : null; 

} 