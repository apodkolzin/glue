/**
 * Скрипт для генерации данных для договора на экспертизу заявок
 * Предполагает входной параметр task, являющийся экземпляром класса ExaminationTaskDemand
 *
 */
 

report.vars.report=report
report.vars.expert=task.expert
report.vars.task=task

demands = []  
def demList=task.demandsList
for(item in demList){
   demands.add(item.title)
}

report.vars.demands=demands
report.vars.lot_code=task.lot.fullNumber==null ? task.lot.identifier : task.lot.fullNumber
report.vars.numberLetter=task.numdogovorPAdemand

report.vars.gosContract = helper.getCatalogItem("ComplianceLettersContracts", report.vars.numberLetter).title