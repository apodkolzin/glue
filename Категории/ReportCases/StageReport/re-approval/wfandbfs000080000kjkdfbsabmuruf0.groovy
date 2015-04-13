import ru.naumen.integration.formular.bobject.AddFormularReportVersionBA 
import ru.naumen.common.containers.MapPropertySource 
import ru.naumen.fcntp.bobject.report.bo.FormularReport 
import ru.naumen.core.hibernate.bactions.BusinessActionBase 

MapPropertySource ps(Map map) 
{ 
return new MapPropertySource(map) 
} 

def versionBA = new AddFormularReportVersionBA(ps(["currentReport":subject]), user, subject.parent); 
FormularReport report = versionBA.newInstance(); 
FormularReport reloadedReport = BusinessActionBase.ensureReload(session, report) 
AddFormularReportVersionBA.sendCopyRequest(reloadedReport);