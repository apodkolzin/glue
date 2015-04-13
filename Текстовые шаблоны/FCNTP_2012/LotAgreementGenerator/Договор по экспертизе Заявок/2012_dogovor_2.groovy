/*
   L-15088 Договор ИР 07-13 - экспертиза заявок http://ssh-gate.naumen.ru:10305/lab_labour/show/15088   
   
   Автор: aboronnikov
*/

def task = helper.get('corebofs000080000im3o6pjmk7vnf9g')
report.vars.report=report
report.vars.expert=task.expert
report.vars.task=task

demands = []  
def demList=task.demandsList
for(item in demList){
   demands.add(item.title)
}

report.vars.demands=demands
report.vars.demand_declension = ru.naumen.ccamext.docgen.PdfUtil.getDeclension(Integer.valueOf( demList.size() ), 0, ru.naumen.ccamext.docgen.PdfUtil.demandsDeclension )
report.vars.lot_code=task.lot.fullNumber==null ? task.lot.identifier : task.lot.fullNumber
report.vars.numberLetter=task.numdogovorPAdemand