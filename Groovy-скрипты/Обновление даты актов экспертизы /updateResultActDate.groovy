uuids=[]
helper.execute(){s->
  l = s.createQuery("select ra, t  from ResultAct ra, ExaminationTaskFcntpBase t where t.resultAct=ra and ra.documentDate>'01.01.2013' and t.endDate<'01.01.2013'").list()
  for (i in l)
  {
    ra = i[0]
    uuids.add(ra.UUID)
    t = i[1]
    ra.documentDate=i[1].agreement.documentDate  
    ra.doUpdate(s)  
  }  
  
}
q = session.createQuery("select ra.UUID, ra.documentDate, t.endDate, t from ResultAct ra, ExaminationTaskFcntpBase t where t.resultAct=ra and ra.UUID in (:uuids)")
q.setParameterList("uuids", uuids)  
 q.list() 
