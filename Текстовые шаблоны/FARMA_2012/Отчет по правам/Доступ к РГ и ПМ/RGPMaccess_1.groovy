package resources.groovy.birt

import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.core.CoreBO
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.core.roles.Role
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.relations.PersonProgramActionRelation
import ru.naumen.fcntp.bobject.relations.PersonWorkgroupRelation
import ru.naumen.core.catalogsengine.CoreCatalogItem
import ru.naumen.fcntp.bobject.program.workgroup.ProgramWorkGroup

import java.util.List

/*
 vars['programUUID'] = 'corebofs000080000gte9v29hjmmodmk'
 */
	def program = helper.get(programUUID)
		
	//выводим всех пользователей с ролью 'Секретать рабочей группы','Член  рабочей группы'  
	def employees = helper.select(" select distinct es " +
	   " FROM RoleCapabilityRelation rcRel, EmployeeStech es " +
	   " WHERE rcRel.rightBO in (select r from Role r where r.Id in ('workGroupSecretary', 'workGroupMember')) " +
	   "  and rcRel.leftBO.id = es.id "
	)
	 
	
	def String SEPARATOR = ";\n"
	    
	int indexRow = 0
	def values = report.createList()
	for (def EmployeeStech memberEmployee in employees)
	{
		def row = report.createObject()
		indexRow++;
		row.no = indexRow
		
		row.name = "member"
		row.email = memberEmployee.getEmail()
		row.fio =  memberEmployee.getDisplayableTitle()
		    	
	    def StringBuilder sb = new StringBuilder();
        def List<String> progActions = helper.select("select rel.rightBO.persistedDisplayableTitle from " + PersonProgramActionRelation.class.getName() + 
                " as rel where rel.leftBO.id = '$memberEmployee.UUID' and rel.rightBO.parent.id='$program.UUID' and rel.typeKey='" + 
                EmployeeStech.PROGRAM_ACTION_TYPE_KEY + "'")
	    
	    row.programActions = StringUtilities.join(progActions, SEPARATOR);
	    
                
        def List<String> workGroupsByProgram = helper.select("select rel.rightBO.title from PersonWorkgroupRelation as rel  " +
            " where rel.leftBO.id = '$memberEmployee.UUID' " +
            "   and rel.rightBO.id in (select wgroup.workGroup.id from ProgramWorkGroup as wgroup where wgroup.parent.id='$program.UUID')" +
            "   and rel.typeKey='" +EmployeeStech.WORK_GROUP_TYPE_KEY + "'"
        )
        row.workGroups = StringUtilities.join(workGroupsByProgram, SEPARATOR);
	    
        
        def roleRels = helper.select("select rcRel FROM RoleCapabilityRelation rcRel" +
            " WHERE rcRel.rightBO in (select r from Role r where r.Id in ('workGroupSecretary', 'workGroupMember')) " +
            "  and rcRel.leftBO.id = '$memberEmployee.UUID' "
        )
        
        def List<CorePost> listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
        
        int maxCountRowInMember = (listPosts.size() > roleRels.size()) ? listPosts.size() : roleRels.size();
        maxCountRowInMember = (maxCountRowInMember > 0) ? maxCountRowInMember : 1;
         
        //Каждые место работы или роль в отдельной строке
        for (int i = 0; i < maxCountRowInMember; i++)
        {
            if (i > 0)
            {
                values.add(row);
                row = report.createObject()
                //indexRow++;
                //row.no = indexRow;
                row.name = "member"
                row.email = ""
                row.fio = ""
                row.programActions = ""
                row.workGroups = ""
            }
            
            //Добавление места работы
            if (i < listPosts.size())
            {
                def CorePost post = listPosts.get(i);
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
            else
            {
                row.post = ""
                row.job = ""
            }
            //Добавление роли
            if (i < roleRels.size())
            {
                def roleRel = roleRels.get(i);
                row.role = roleRel.getRightBO().getDisplayableTitle()
                row.createRole = StringUtilities.toReverseDateFormat(roleRel.getCreationDate())
            }
            else
            {
                row.role = ""
                row.createRole = ""
            }
        }
		
		values.add(row)
	    	
	}
	    
	def xc = report.createObject().grabObject(program, "title")
	xc.members=values
	report.rootObject = xc

report