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

//report.vars.gosContract = helper.getCatalogItem("СomplianceLettersContracts", report.vars.numberLetter).title