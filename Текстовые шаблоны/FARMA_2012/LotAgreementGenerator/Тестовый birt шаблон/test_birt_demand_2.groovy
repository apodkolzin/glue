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