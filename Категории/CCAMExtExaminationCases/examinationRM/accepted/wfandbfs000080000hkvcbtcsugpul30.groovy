if (subject.object.currentStage.identificator=="4" || subject.object.currentStage.identificator=="5" || subject.object.currentStage.identificator=="examinationStart" || subject.object.currentStage.identificator=="examinationEnd") new ru.naumen.fcntp.bobject.examination.workflow.CreateResultActScript().create(workflowInstance ,oldState,newState,session);