list = helper.select("select r.id\
	from ExaminationMarkedReport r where \
	exists(select r1.id from ExaminationMarkedReport r1 where r1.object.id=r.object.id and r1.task.id=r.task.id and r1.id!=r.id) ")
  
 
helper.execute{s->
  for (u in list)
  {
	o = helper.get(u,s)
          o.doDelete(s)
  }

  
  }