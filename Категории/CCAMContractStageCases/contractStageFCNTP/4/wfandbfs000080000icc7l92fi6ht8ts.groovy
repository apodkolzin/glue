def tasks = session.createQuery("select t from ExaminationTaskRM t where t.currentStage.identificator='accepted' AND t.object.id='${subject.UUID}'").list(); for (task in tasks) {new ru.naumen.fcntp.bobject.examination.workflow.CreateResultActScript().create(task ,null,null,session);}