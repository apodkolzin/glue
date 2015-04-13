package resources.groovy

import java.text.SimpleDateFormat

//def remark = helper.get('anyr4dfs000080000kk6g6vu0mb9vo9k')
def remark = remarkReport
report.vars.remarkAuthorOrg = remark.remarkAuthorOrg
report.vars.contractNumber = remark.parent.parent.identifier
report.vars.reportVersion = remark.parent.docVersion
report.vars.reportPeriod = remark.parent.reportPeriod.title
report.vars.reportYear = remark.parent.reportYear
report.vars.contractTitle = remark.parent.parent.fullTitle
report.vars.contractCode = remark.parent.parent.demand.viewNumber
report.vars.remarkText = remark.remarkText
report.vars.remarkAuthorPost = remark.remarkAuthorPost
report.vars.creationDate = new SimpleDateFormat("dd.MM.yyyy").format(remark.creationDate)
def authorName = ''
authorName<<= remark.remarkAuthor?.person?.lastName
authorName<<" "<<remark.remarkAuthor?.person?.firstName?.substring(0,1)
authorName<<". "<<remark.remarkAuthor?.person?.middleName?.substring(0,1)
authorName<<". "
report.vars.remarkAuthor = authorName