package resources.groovy.birt
import ru.naumen.core.util.hquery.HCriteria
import ru.naumen.core.util.hquery.HRestrictions
import ru.naumen.core.hibernate.HibernateUtil
import ru.naumen.fcntp.bobject.demand.DemandFcntp
import ru.naumen.fcntp.bobject.lot.LotFcntp
import ru.naumen.fcntp.bobject.contract.ContractFcntp
import ru.naumen.fcntp.bobject.demand.DemandFcntpFinances
import ru.naumen.stech.bobject.org.OrganizationStech
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.fcntp.ui.tlc.AssistantsTLC
import ru.naumen.wcf.engine.urls.URLCreator

/*
   Скрипт для генерации отчета для руководителя группы кураторов
   ТЗ: http://ssh-gate.naumen.ru:10305/lab_labour/show/12290 
   Автор: aboronnikov
  */

  
GENERAL_PROGRAM = "КАДРЫ"
programActions = ["1.4", "1.2.2", "1.2.1"]
  
DEMAND_CATEGORY = "DemandStaff_Creative"
DEMAND_STAGE_ID = "contract"
  
actions=[]
for(actId in programActions){
	prog = helper.query("select d from ProgramAction d where d.originalIdentifier='" + actId + "' and d.parent.identifier='" + GENERAL_PROGRAM + "'")
	actions.add(prog[0])
}

entries = []
for(action in actions){
     List<DemandFcntp> demands = getDemands(action, DEMAND_CATEGORY, DEMAND_STAGE_ID)

     for(demand in demands){  
     	createTemplateVariableEntries(entries, action, demand)
     } 
}
report.vars.table = entries
  
def getDemands(action, category, stage){
     HCriteria c = new HCriteria()
     c.addSource(DemandFcntp.class, "d")
     c.addInnerJoin("d.parent", "l")
     c.add(HRestrictions.eq("l.programAction.id", action.getUUID()))
     c.add(HRestrictions.eq("d.currentStage.identificator", stage))
     c.add(HRestrictions.eq("d.ccamBOCase.code", category))
     c.addColumn("d")
     return c.createQuery(HibernateUtil.currentSession()).list()
}

def createTemplateVariableEntries(entries, action, demand){
	LotFcntp lot = demand.getParent()
        ContractFcntp contract = demand.getContract()
        DemandFcntpFinances fins = HibernateUtil.currentSession().get(DemandFcntpFinances.class, demand.getUUID())
        OrganizationStech executor = demand.getExecutor()
        EmployeeStech workManager = demand.getWorkManager()
        demandId = demand.getUUID()
        projState = ""
        projCode = ""
        projs = helper.query("select d from ContractProjectDocument d where d.parent.id='"+contract.UUID+"'")
        if(projs.size()==1){
        	projState = projs[0].workflowState.title
            projCode = projs[0].ccamBOCase.title
        }
        assistants = AssistantsTLC.findAssistants(demand)
          
        assist_fios = ""
        assist_emails = ""
        assist_phones = ""
        assist_faxs = ""
	assistants.each()
	{
  		ass->
    		if(ass.title != null)
    		{
      			if(assist_fios != "")
       				assist_fios += ","
      			assist_fios += ass.title
    		}
          	if(ass.email != null)
    		{
      			if(assist_emails != "")
       				assist_emails += ","
      			assist_emails += ass.email
    		}
          	if(ass.cityPhoneNumber != null)
    		{
      			if(assist_phones != "")
       				assist_phones +=","
      			assist_phones += ass.cityPhoneNumber
    		}
                if(ass.fax != null)
    		{
      			if(assist_faxs != "")
       				assist_faxs += ","
      			assist_faxs += ass.fax
    		}
	    }

		i = 1
        addEntry(entries, demandId, i++, "Куратор", contract.manager!=null?contract.manager.fio("fi.o."):"")
        addEntry(entries, demandId, i++, "Шифр лота", lot.fullNumber)
        addEntry(entries, demandId, i++, "Номер очереди", lot.queueNumber)
        addEntry(entries, demandId, i++, "Номер лота в очереди", lot.inOfferNumber)
        addEntry(entries, demandId, i++, "Тема лота", lot.theme)
        addEntry(entries, demandId, i++, "Номер протокола", lot.protocolNumberSummation)
        addEntry(entries, demandId, i++, "Дата протокола", lot.protocolDateSummation)
        addEntry(entries, demandId, i++, "Программное мероприятие", action.displayableIdentifier)
        addEntry(entries, demandId, i++, "Регистрационный номер заявки на портале", demand.getPortalNumber())
        addEntry(entries, demandId, i++, "Номер заявки", demand.getFullNumber())
          
        addEntry(entries, demandId, i++, "Тема заявки", demand.getTopic())
        addEntry(entries, demandId, i++, "Итого. Бюджет РФ", fins.getBudgetRFTotal())
        addEntry(entries, demandId, i++, "Наименование организации", (executor != null) ? executor.getTitle() : "")
        addEntry(entries, demandId, i++, "Направление", (lot.branch!=null)?lot.branch.title:"")
        addEntry(entries, demandId, i++, "Поднаправление", (demand.subCluster!=null)?demand.subCluster.title:"")
        addEntry(entries, demandId, i++, "UUID заявки", URLCreator.createFullLinkToPublishedObject(demand.getUUID()))
        addEntry(entries, demandId, i++, "Руководитель проекта. ФИО", (workManager != null) ? workManager.title : "")
        addEntry(entries, demandId, i++, "Руководитель проекта. Должность", (workManager != null) ? workManager.post : "")
        addEntry(entries, demandId, i++, "Руководитель проекта. Эл.почта", (workManager != null) ? workManager.email : "")
        addEntry(entries, demandId, i++, "Руководитель проекта. Раб.телефон", (workManager != null) ? workManager.cityPhoneNumber : "")
        addEntry(entries, demandId, i++, "Руководитель проекта. Факс", (workManager != null) ? workManager.fax : "")
          
        addEntry(entries, demandId, i++, "Ответственный исполнитель проекта. ФИО", assist_fios)
        addEntry(entries, demandId, i++, "Ответственный исполнитель проекта. Эл.почта", assist_emails)
        addEntry(entries, demandId, i++, "Ответственный исполнитель проекта. Раб.телефон", assist_phones)
        addEntry(entries, demandId, i++, "Ответственный исполнитель проекта. Факс", assist_faxs)
         
        addEntry(entries, demandId, i++, "Номер соглашения", contract.identifier)
        addEntry(entries, demandId, i++, "UUID соглашения", URLCreator.createFullLinkToPublishedObject(contract.UUID))
        addEntry(entries, demandId, i++, "Дата начала", contract.beginDate)
        addEntry(entries, demandId, i++, "Дата окончания", contract.endDate)
        addEntry(entries, demandId, i++, "Распорядитель",  (contract.fundsManager != null) ?contract.fundsManager.title:"")
        
        addEntry(entries, demandId, i++, "Статус соглашения", (demand.contract.workflowState!=null)?demand.contract.workflowState.title:"")
        addEntry(entries, demandId, i++, "Статус проекта соглашения", projState)
        addEntry(entries, demandId, i++, "Категория проекта соглашения", projCode)
        
        addEntry(entries, demandId, i++, "Вуз mail", (executor != null) ? executor.email : "")
        addEntry(entries, demandId, i++, "Вуз ТЕЛЕФОН", (executor != null) ? executor.phoneNumber : "")
}

def addEntry(entries, demandId, varCode, variable, value){
	    entry = new Entry()
        entry.variable = variable
        entry.value = value
        entry.demandId = demandId
        entry.varCode = varCode
        
        entries.add(entry)
}

class Entry{
  	String demandId
        String variable
        String value
        int varCode
}