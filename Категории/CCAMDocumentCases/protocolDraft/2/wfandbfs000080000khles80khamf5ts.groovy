import ru.naumen.ccam.bobject.employee.CcamEmployee
import ru.naumen.core.bobjects.email.CorePersonDAOUtils
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.core.relations.CoreBORelationHibernateHandler
import ru.naumen.fcntp.bobject.assignprotocolcomission.AssignProtocolCommissionStaff
import ru.naumen.fcntp.bobject.document.ProtocolDraft
import ru.naumen.fcntp.bobject.notice.Notice
import ru.naumen.fcntp.workflow.SendMailScript
import ru.naumen.fcntp.workflow.SendSimpleMailScript
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.wcf.engine.urls.URLCreator

/**
 * Created by azimka on 16.09.14.
 */


def sendMailToEmployee = { CcamEmployee person, ProtocolDraft protocolDraft ->
    if (null != person)
    {
        Notice notice = helper.get(protocolDraft.parent.UUID)
        def parameters = [
                'respectable'         : getRespectable(person),
                'first-name'          : person.firstName,
                'second-name'         : person.middleName,
                'notice-identifier'   : notice.identifier,
                'protocolDraftVersion': protocolDraft.protocolDraftVersion,
                'task-link'           : URLCreator.createFullLinkToPublishedObject(protocolDraft),
                "email"               : CorePersonDAOUtils.emailDAO(person).getPrimaryAddress()
        ]

        new SendSimpleMailScript().setFeedback("support@fcntp.ru").execute(person, "NotifyProtocolDraftSigners/NotifyProtocolDraftSigners_title", "NotifyProtocolDraftSigners/NotifyProtocolDraftSigners_body", parameters)
    }
}

def getRespectable(person)
{
    def sex = person.sex
    return (sex == null) ? (("Уважаемый(ая)")) : ("male".equalsIgnoreCase(sex.getCode())) ? (("Уважаемый")) : (("Уважаемая"));
}

//def employee = helper.get("corebofs002080000kgdjgpotoet063s")
def protocolDraft = BusinessActionBase.unproxy(subject)
def employeeStaffs = CoreBORelationHibernateHandler.findRightRelations(AssignProtocolCommissionStaff.class, protocolDraft, session)
for (AssignProtocolCommissionStaff employeeStaff in employeeStaffs)
{
    def employee = employeeStaff.getRightBO()
    if (CorePersonDAOUtils.emailDAO(employee).countEmails() > 0)
    {
        sendMailToEmployee(employee, protocolDraft)
    }
}
