/**
 * Created by IntelliJ IDEA.
 * User: Andrew F. Podkolzin
 * Date: 15.11.11
 * Time: 13:22
 * Since: 
 * ru.naumen.bk.BKConfiguration.get().setTextTemplateRemoteReportEngineUrl("http://192.168.212.164:8282/reporter/birt")
def task = helper.get('corebofs000080000inbm7ps55jiq8go')
def object = helper.get('corebofs000080000im695mv75iu2qjk')
 */

import ru.naumen.ccamext.bobject.examination.report.ExaminationReportHibernateHandler
import ru.naumen.ccamext.bobject.examination.template.ExaminationTemplateGroup
import ru.naumen.core.reports.IRXTable
import ru.naumen.fcntp.examination.LotDemandGenerator

exam = ExaminationReportHibernateHandler.getReportByTaskAndObject(task, object);
gen = new LotDemandGenerator(exam, false)

report.vars.task_id=task.identifier
report.vars.object_id=object.fullNumber
report.vars.lot_id=object.parent.fullNumber
report.vars.expert=task.expert
report.vars.comment=gen.comment
  
void  printGroup(IRXTable groups, IRXTable questions, IRXTable answers, ExaminationTemplateGroup group)
{
    if (!group.pdfable)
    {
        groups.add(["index":group.index, "title":group.title])
        return
    }

    def subGroups = group.filterChilds( ExaminationTemplateGroup.class );
    if( subGroups.empty )
    {
        for (def item in group.items)
            if (item.pdfable)
                for (def row in gen.createQuestionRows(item))
                {
                    if(row.answerMark.size()==0)
                        answers.add(["group_id":item.index, "question_id":row.index, "title":"-", "max":"-", "mark":"-", "wmark":"-"])
                      
                    for (def a in row.answerMark)
                        answers.add(["question_id":row.index, "title":a.key, "max":a.value.max, "mark":a.value.mark, "wmark":a.value.weightMark])
                                     
                    questions.add(["group_index":group.index, "comment":row.comment, "comment_title":row.commentTitle, \
                        "exist_comment":row.existComment, "qindex":row.index, "title":row.question,\
                         "max":row.questionMark.max, "mark":row.questionMark.mark, "wmark":row.questionMark.weightMark])
                }
        groups.add(["group_index":group.index, "title":group.title, "max":gen.getStringMax(group), "mark": gen.getStringPureMark(group), "wmark": gen.getStringWeightedMark(group)])
    }

    for ( ExaminationTemplateGroup subGroup : subGroups)
        printGroup(groups, questions, answers, subGroup)
}

report.tables["groups"] = report.createList()
report.tables["questions"] = report.createList()
report.tables["answers"] = report.createList()
for (def group  in exam.template.groups)
{
    printGroup(report.tables["groups"], report.tables["questions"], report.tables["answers"], group);    
}

data = report.toXML()