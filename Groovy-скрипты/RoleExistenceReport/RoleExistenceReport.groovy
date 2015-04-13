package resources.groovy.birt

import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.core.roles.Role
import ru.naumen.common.utils.StringUtilities
import ru.naumen.core.hibernate.HibernateUtil
import java.text.SimpleDateFormat

import org.hibernate.Query
    
    def StringBuilder sb = new StringBuilder()
    sb.append(" select distinct es ")
      .append(" FROM RoleCapabilityRelation rcRel, EmployeeStech es ")
      .append(" WHERE rcRel.rightBO.Id in ('advlistAdmin', 'admin', 'RequirementExpertiseReader') ")
      .append("  and rcRel.leftBO = es.id ")
    //выводим всех пользователей с ролями 'Администратор адвлистов', 'Администратор', 'Читатель экспертизы предложений '
    
      if (!StringUtilities.isEmptyTrim(email))
      {
          sb.append(" and lower(es.email) like :email")
      }
      
      if (!StringUtilities.isEmptyTrim(workName))
      {
          sb.append(" and es.id in (select post.employee.id from CorePost post where lower(post.parent.persistedDisplayableTitle) like :workName)")
      }
      
      //выводим всех пользователей с ролями определенными выше
      Query q = HibernateUtil.currentSession().createQuery(sb.toString())
      if (!StringUtilities.isEmptyTrim(email))
      {
          q.setParameter("email", "%$email%".toLowerCase())
      }
      if (!StringUtilities.isEmptyTrim(workName))
      {
          q.setParameter("workName", "%$workName%".toLowerCase())
      }
    def employees = q.list();
            
    def int indexRow = 0
    def values = report.createList()
    dFormat = new SimpleDateFormat("dd.MM.yyyy")
    
    for (def EmployeeStech memberEmployee in employees)
    {
        def List<RoleCapabilityRelation> roleRels = helper.select("select rcRel FROM RoleCapabilityRelation rcRel" +
            " WHERE rcRel.rightBO in (select r from Role r where r.Id in ('advlistAdmin', 'admin', 'RequirementExpertiseReader')) " +
            "  and rcRel.leftBO.id = '$memberEmployee.UUID' "
        )
        
        def List<CorePost> listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
                        
        //Каждые место работы или роль в отдельной строке 
        for (int i = 0; i < roleRels.size(); i++)
        {
        	def roleRel = roleRels.get(i).getRightBO().getDisplayableTitle()
        	
        	createRole = dFormat.format(roleRels.get(i).getCreationDate())
            //Добавление мест работы
            for(int j = 0; j < listPosts.size(); j++){
                def row = report.createObject()
	        	if(j==0){
	        		indexRow++
        			row.email = memberEmployee.getEmail()
        			row.fio =  memberEmployee.getDisplayableTitle()
        			row.createRole = createRole
        			row.no = indexRow
        		}else{
                	row.email = ""
                	row.fio = ""
                	row.no = ""
            	}

        		row.name = "member"
        		row.role = roleRel
            	
               	def CorePost post = listPosts.get(j)
               	row.post = post.getTitle()
               	if (post.getParent() != null)
               	{
                   	row.job = post.getParent().getDisplayableTitle()
               	}
               	else
               	{
                   	row.job = ""
               	}
            	
            	values.add(row)
            }
            if(listPosts.size()==0){
            	def row = report.createObject()
	        	indexRow++
    	    	row.no = indexRow

        		row.name = "member"
        		row.email = memberEmployee.getEmail()
        		row.fio =  memberEmployee.getDisplayableTitle()
        		row.role = roleRel
        		row.createRole = createRole
        		
        		values.add(row)
            }
        }
    }
        
    def xc = report.createObject()
    xc.members=values
    report.rootObject = xc

report