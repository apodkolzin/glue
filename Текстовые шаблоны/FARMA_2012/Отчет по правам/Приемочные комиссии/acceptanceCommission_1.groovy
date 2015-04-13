package resources.groovy.birt

import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.fcntp.bobject.relations.EmployeeAcceptanceCommissionRelation
import ru.naumen.fcntp.bobject.program.acceptancecommission.ProgramAcceptanceCommission
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.core.roles.Role
import ru.naumen.common.utils.StringUtilities
import ru.naumen.core.roles.caches.RolesCache

/*
 vars['programUUID'] = 'corebofs000080000gte9v29hjmmodmk'
 
 */

    def program = helper.get(programUUID)
    Role role = RolesCache.getInstance().getById("AcceptanceCommissionMember");
    //выводим всех пользователей с ролью 'Член конкурсной комиссии', но конкурсные комиссии выводятся только относящиеся к данной программе
    def roleRels = helper.select("SELECT rcRel FROM RoleCapabilityRelation rcRel " +
       " WHERE rcRel.rightBO.id = '$role.UUID'"  
       )
    
    def String SEPARATOR = ";\n"
        
    int i = 0
    def values = report.createList()
    for (def RoleCapabilityRelation roleRel in roleRels)
    {
        def EmployeeStech memberEmployee = helper.get(roleRel.getLeftBO().getUUID())
        def row = report.createObject()
        i++;
        row.no = i
        
        row.name = "member"
        row.email = memberEmployee.getEmail()
        row.fio =  memberEmployee.getDisplayableTitle()
        
        row.role = roleRel.getRightBO().getDisplayableTitle()
        row.createRole = StringUtilities.toReverseDateFormat(roleRel.getCreationDate())
        
        def List<String> acceptanceCommissionTiltes = helper.select("select rel.rightBO.title from EmployeeAcceptanceCommissionRelation as rel  " +
            " where rel.leftBO.id = '$memberEmployee.UUID' " +
            "   and rel.rightBO.id in (select com.commission.id from ProgramAcceptanceCommission as com where com.parent.id='$program.UUID')" +
            "   and rel.typeKey='$EmployeeAcceptanceCommissionRelation.TYPE_KEY'"
        )        
        row.commisstion = StringUtilities.join(acceptanceCommissionTiltes, SEPARATOR)
        
        def boolean createNewRow = false;
        def StringBuilder sb = new StringBuilder();
        def jobs = report.createList()
        def listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
        if (listPosts.size() > 0)
        {
            for(def CorePost post in listPosts)
            {
                //Если у пользователя несколько мест работы, то все кроме первого выводятся в отдельной строке
                if (createNewRow)
                {
                    values.add(row);
                    row = report.createObject()
                   // i++;
                   // row.no = i
                    row.name = "member"
                    row.email = ""
                    row.fio = ""
                    row.role = ""
                    row.createRole = ""
                    row.commisstion = ""
                }
                createNewRow = true;
                
                row.post = post.getTitle()
                if (post.getParent() != null)
                {
                    row.job = post.getParent().getDisplayableTitle()
                }
                else
                {
                    row.job = ""
                }
            }
        }
        else
        {
            row.post = ""
            row.job = ""
        }
        
        values.add(row)
        
   }
    
    
def xc = report.createObject().grabObject(program, "title")
xc.members=values
report.rootObject = xc

report