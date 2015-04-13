import ru.naumen.core.hibernate.HibernateUtil
import ru.naumen.core.usertypes.Status

//def org = helper.get('corebofs000080000hvur2133do0bopc')
def org = object

def orgTitle = org.title
def chief = org.chief?.title
def post = org.chief?.post

def qString = "select r from ResponsiblePerson r where r.parent.id = :orgUUID and r.status = '$Status.NOT_CONFIRMED'"
def query = session.createQuery(qString)
//  def query = session.createSQLQuery("select status from tbl_fcntp_resp_per where parent='corebofs000080000hvur2133do0bopc'")
query.setParameter("orgUUID", org.UUID)
def respList = query.list()

def respTable  = []

respList.each(){r->
    def respItem = new respItem(fio : r.displayableTitle, position : r.position, email : r.email)
    respTable.add(respItem)
}
//respTable

report.vars.orgTitle = orgTitle
report.vars.chief = chief
report.vars.post = post
report.vars.respTable = respTable

class respItem
{
    def fio
    def position
    def email
}