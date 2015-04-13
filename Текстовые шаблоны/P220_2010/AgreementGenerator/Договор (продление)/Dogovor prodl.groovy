//package resources.groovy.birt

//task = helper.get('corebofs000080000k2rddva75ef5nq4')
report.vars.task = task
report.vars.expert = task.expert

StringBuilder demands = new StringBuilder();
boolean firstElementFlag = true
for (def demand in task.demandsList)
{
	if (firstElementFlag)
		demands.append(demand.fullNumber)
	else
		demands.append(", ").append(demand.fullNumber)
	firstElementFlag = false
}
		
report.vars.demands = demands