package resources.groovy.birt

import ru.naumen.core.CoreBOHibernateHandler
import ru.naumen.fcntp.bobject.competitivecommission.CompetitiveCommissionStaff
import ru.naumen.ccamcore.roles.capability.RoleCapabilityRelation
import ru.naumen.core.CoreBO
import ru.naumen.stech.bobject.employee.EmployeeStech
import ru.naumen.orgstruct.bobject.OrgRelationHibernateHandler
import ru.naumen.orgstruct.bobject.CorePost
import ru.naumen.core.roles.Role
import ru.naumen.common.utils.StringUtilities
import org.hibernate.Query


 /*
 programUUID = 'corebofs000080000hpkaknpb9ugsgto'
 */

	def program = helper.get(programUUID)
        
	//выводим всех пользователей с ролью 'Член конкурсной комиссии', но конкурсные комиссии выводятся только относящиеся к данной программе  
	queries = new Queries2(session)
	relQuery = queries.relations
	def roleRels = relQuery.list() 
  	
	def String SEPARATOR = "\n"
	    
    int i = 0
	def values = report.createList()
	for (def RoleCapabilityRelation roleRel in roleRels)
    {
    	def EmployeeStech memberEmployee = helper.get(roleRel.getLeftBO().getUUID())
    	
    	comQuery = queries.commissions
    	comQuery.setParameter('memberId', memberEmployee.UUID)
    	comQuery.setParameter('programId', program.UUID)
    	comQuery.setParameter('typeKey', CompetitiveCommissionStaff.TYPE_KEY)
        def List<String> competitiveCommissionTiltes = comQuery.list()
        
        if(competitiveCommissionTiltes.size()==0) continue
       
    	def row = report.createObject()
    	i++
    	row.no = i
    	
    	row.name = "member"
    	row.email = memberEmployee.getEmail()
    	row.fio =  memberEmployee.getDisplayableTitle()
    	
        row.role = roleRel.getRightBO().getDisplayableTitle()
    	row.createRole = StringUtilities.toReverseDateFormat(roleRel.getCreationDate())
    	    	
    	
    	row.commisstion = StringUtilities.join(competitiveCommissionTiltes, "\n")
    	
        def StringBuilder sb = new StringBuilder()
    	def boolean createNewRow = false
    	def jobs = report.createList()
    	def listPosts = OrgRelationHibernateHandler.listPostsByEmployee(memberEmployee)
    	if (listPosts.size() > 0)
    	{
	    	for(def CorePost post in listPosts)
	    	{
	    	    //Если у пользователя несколько мест работы, то все кроме первого выводятся в отдельной строке
	    	    if (createNewRow)
	    	    {
	    	    	values.add(row)
	    	    	row = report.createObject()
	    			//i++; Для одного пользователя не надо несколько номеров
	    			//row.no = i
	    			row.name = "member"
	    			row.email = ""
	    			row.fio = ""
	    			row.role = ""
	    			row.createRole = ""
	    			row.commisstion = ""
	    	    }
	    	    createNewRow = true
	    	    
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

class Queries2{
	Query relations
	Query commissions
	
	Queries2(session){
		relations = session.createQuery("SELECT rcRel FROM RoleCapabilityRelation rcRel WHERE rcRel.rightBO.Id = 'CompetitiveCommissionMember'")
		
		commissions = session.createQuery("select rel.leftBO.persistedDisplayableTitle from CompetitiveCommissionStaff as rel " +
            " where rel.rightBO.id=:memberId and rel.typeKey=:typeKey and rel.leftBO.parent.id=:programId")
        }
}