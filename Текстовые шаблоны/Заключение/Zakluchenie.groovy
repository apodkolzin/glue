package resources.groovy.birt

import ru.naumen.core.files.DBFile
import ru.naumen.core.files.DBFileHibernateHandler
import ru.naumen.fcntp.bobject.document.StageReportDocumentation
import ru.naumen.fcntp.bobject.document.StageReportDocumentationHibernateHandler


/**
 * @author ayakovlev
 * 07.10.2013
 * Скрипт для генерации заключения в акте
 */


act = object
//helper.get('corebofs000080000k1ovovula0pl6ac')

contract = act.contract
stage = act.parent

report.vars.contractIdentifier = contract?.identifier

report.vars.stageNumber = stage?.number
report.vars.stageTitle = stage?.title
report.vars.manager = contract?.manager

report.vars.contractAppendSignatureDate = contract?.appendSignatureDate
  //Дата обновления файла
report.vars.actUpdatedAt  = act.updatedAt
  //Номер заявки
report.vars.demandFullNumber =  contract?.demand?.fullNumber
  //тема
report.vars.contractFullTitle = contract?.fullTitle
//Плановый срок окончания этапа
report.vars.planEndDate = stage?.planEndDate
//организация
report.vars.performer = contract?.performer.title
//Инициатор проекта
report.vars.projectInitiator = contract?.projectInitiator?.title
report.vars.budget = stage?.budget
report.vars.offbudget = stage?.offbudget
  
StageReportDocumentation doc = StageReportDocumentationHibernateHandler.findCurrentDocument(act.parent)
List<DBFile> files = new ArrayList<DBFile>(DBFileHibernateHandler.listTopLevelFiles(doc))
for (DBFile file in files)
  if (!file.BOCase.title.equals("Отчет о НИР"))
	files.remove(file)
	  
  
report.vars.fileCreationDate = files[0].creationDate
report.vars.delay = (stage?.planEndDate - stage?.notifyDate - 10) > 0 ? (stage?.planEndDate - stage?.notifyDate - 10) : 0

report.vars.acceptanceCommissionFullNumber = stage?.acceptanceCommission?.fullNumber
  

  
  
/*class MakingActScript {

}*/