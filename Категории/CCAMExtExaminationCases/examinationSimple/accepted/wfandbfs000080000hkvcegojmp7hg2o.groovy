//старый скрипт
//if ("corebofs000080000gte9v29hjmmodmk".equals(subject.parent.parent.UUID) || "corebofs000080000hgn46g4u83027lk".equals(subject.parent.parent.UUID) || "corebofs000080000hpkaknpb9ugsgto".equals(subject.parent.parent.UUID) || "corebofs000080000hfqib0fho5uol08".equals(subject.parent.parent.UUID))

//new ru.naumen.fcntp.bobject.examination.workflow.CreateResultActScript().create(workflowInstance ,oldState,newState,session);

//новый скрипт
//if ("corebofs000080000gte9v29hjmmodmk".equals(subject.parent.parent.UUID) || "corebofs000080000hgn46g4u83027lk".equals(subject.parent.parent.UUID) || "corebofs000080000hfqib0fho5uol08".equals(subject.parent.parent.UUID) || "corebofs000080000hpkaknpb9ugsgto".equals(subject.parent.parent.UUID)|| "corebofs000080000ieoe2mpplhe26ak".equals(subject.parent.parent.UUID))
 //       {
  //          if (subject.getCost() != null && subject.getCost().compareTo(0) != 0 && subject.getAgreementFile() == null)
  //          {
  //              new ru.naumen.fcntp.bobject.agreement.AddExpertAgreementBA(subject).execute(session);
  //          }
 //       }
 //скрипт с рабочего 
 if ("corebofs000080000gte9v29hjmmodmk".equals(subject.parent.parent.UUID) || "corebofs000080000hgn46g4u83027lk".equals(subject.parent.parent.UUID) 
|| "corebofs000080000hpkaknpb9ugsgto".equals(subject.parent.parent.UUID) 
|| "corebofs000080000hfqib0fho5uol08".equals(subject.parent.parent.UUID)) 
{ 
new ru.naumen.fcntp.bobject.examination.workflow.CreateResultActScript().create(workflowInstance ,oldState,newState,session); 
}