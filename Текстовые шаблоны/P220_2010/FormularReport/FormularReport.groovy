package resources.groovy

import java.text.SimpleDateFormat

/**
 * Created by azimka on 03.09.14.
 */

def remark = remarkReport
//def remark = helper.get('anyr4dfs000080000keluiv7m5512d7s')
//def remark = helper.get('anyr4dfs000080000kdsht7k28k9g954')
report.vars.remarkAuthorOrg = remark.remarkAuthorOrg
report.vars.contractNumber = remark.parent.parent.parent.identifier
report.vars.remarkText = remark.remarkText
report.vars.remarkAuthorPost = remark.remarkAuthorPost
report.vars.creationDate = new SimpleDateFormat("dd.MM.yyyy").format(remark.creationDate)
def authorName = ''
authorName <<= remark.remarkAuthor?.person?.lastName
authorName << " " << remark.remarkAuthor?.person?.firstName?.substring(0, 1)
authorName << ". " << remark.remarkAuthor?.person?.middleName?.substring(0, 1)
authorName << ". "
report.vars.remarkAuthor = authorName

//поле Дата окончания
def date = null;
def contractUUID = remark.parent.parent.parent.UUID
def contract = helper.get(contractUUID)

try
{
    date = remark.parent.parent.planEndDate
    report.vars.contractProjectInitiator = contract.projectInitiator?.title
    report.vars.contractLeadScientistFIO = contract.leadScientistForm?.getLeadScientistFIO()
    report.vars.contractAppendSignatureDate = new SimpleDateFormat("dd.MM.yyyy").format(contract.appendSignatureDate)
    def contractExtraResearchAreaSet = ""
    for (def item in contract.extraResearchAreaSet)
    {
        contractExtraResearchAreaSet <<= item?.title << ";\n"
    }
    report.vars.contractExtraResearchAreaSet = contractExtraResearchAreaSet
} catch (Exception ex)
{
    ex.printStackTrace()
}
if (date != null)
{
    Calendar calendar = Calendar.getInstance()
    calendar.setTime(new Date(date.getTime()))
    report.vars.planEndDate = calendar.get(Calendar.YEAR).toString()
} else
{
    report.vars.planEndDate = ""
}

report.vars.contractIdentifier = contract.identifier
report.vars.contractFullTitle = contract.fullTitle

def wdefID = remark.ccamBOCase.stagesWorkflowDefinition.UUID

def stage1 = helper.select("select title from CCAMStage where wfDefinition.id='$wdefID' and identificator='1'")[0]
def stage2 = helper.select("select title from CCAMStage where wfDefinition.id='$wdefID' and identificator='2'")[0]

def list = helper.query("select l from CoreLogEvent l where l.subjectUUID = '$remark.UUID' and l.message like 'Изменение состояния: $stage1 -> $stage2'")
def dateChangingStage = ""
if(list.size()>0)
{
    dateChangingStage = new SimpleDateFormat("dd.MM.yyyy").format(list.get(0).getDate())
}
report.vars.dateChangingStage = dateChangingStage

def listFiles = ru.naumen.core.ui.othercontrollers.SimpleFilesTreeListController.listFiles(remark.parent)
def files = ""
for (def item in listFiles)
{
    files <<= item.title << ";"<<"\n"
}
report.vars.files = files
report.vars.reportRemarks = remark.hasNoRemarks?"Замечаний нет":"Имеются следующие замечания: "
