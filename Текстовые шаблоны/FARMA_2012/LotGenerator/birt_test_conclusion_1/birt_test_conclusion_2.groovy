package resources.groovy.birt

/**
 * Script generating xml data for aggregate conclusion 
 * 
 * User: Anton Boronnikov
 * Date: 09.07.2012
 * 
 * def task = helper.get('corebofs000080000inbm7ps55jiq8go')
 * def object = helper.get('corebofs000080000im695mv75iu2qjk')
 */

import ru.naumen.ccamext.bobject.examination.report.ExaminationReportHibernateHandler
import ru.naumen.ccamext.bobject.examination.template.ExaminationTemplateGroup
import ru.naumen.core.reports.IRXObject
import ru.naumen.fcntp.examination.LotGenerator
import ru.naumen.guic.formatters.DoubleFormatter

void fulfillGroupList(ArrayList groups, ExaminationTemplateGroup group)
{
	groups.add( group )						
	for ( subGroup in group.filterChilds( ExaminationTemplateGroup.class )){
		fulfillGroupList(groups, subgroup)
	}
}
 

exam = ExaminationReportHibernateHandler.getReportByTaskAndObject(task, object)
gen = new LotGenerator(exam, false)

report.vars.expert = task.expert
report.vars.doc_number = document.documentNumber
report.vars.comment = gen.comment
report.vars.task_id = task.identifier
report.vars.object_id = object.fullNumber
report.vars.theme = object.theme

groups = []

for (def group in exam.template.groups)
{
	fulfillGroupList(groups, group )
}

report.tables["demands"] = report.createList()
report.tables["groups"] = report.createList()


for (def group  in exam.template.groups)
{
    report.tables["groups"].add(["index":group.index, "title":group.title])
}

i=0
for(def row in gen.getRows(task))
{
    //marks = report.createList()
    marks=[]
	for(def group in groups)
	{
        str = group.index.toString();
        def record = report.createObject()
        record.mark = row.getMark(group)
        record.criterion = DoubleFormatter.parseBigDecimal(str).intValue();
        
        marks.add(record)
	}
	
	i++
    def demand = report.createObject()
    demand.title = row.getTitle()
    demand.place = i
    demand.marks = marks
	report.tables["demands"].add(demand)
}

data = report.toXML()