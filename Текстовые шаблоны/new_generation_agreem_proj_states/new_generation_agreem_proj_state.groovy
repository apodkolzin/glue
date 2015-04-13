reportType = 1
HAS_NO_MANAGER = "[нет куратора]"

docs = helper.query("SELECT d FROM ContractProjectDocument d WHERE d.parent.ccamBOCase.code='contractSimple_Creative'")
stateList = helper.getCatalogItem("CCAMDocumentCases", "contractProject_Creative").workflowStates
  
list = []
  
for(doc in docs){
	contract = doc.parent
	
	if(contract.manager==null) {
		manager = HAS_NO_MANAGER
	}else{
		manager=contract.manager.fio("fi.o.")
	}
        
	programAction=contract.programActions[0].originalIdentifier
        state = doc.workflowState.title

        handleEntry(list, programAction, manager, state)
}

report.vars.states = list

//конец главного метода

//Дальше идут классы и внутренние процедуры..

class Entry{
	String manager
        String programAction
        String state
        int count
}  

def findEntry(list, programAction, manager, state){
  for(entry in list){
    if(entry.programAction.equals(programAction)){
      if(entry.state.equals(state)){
        if( reportType == 1 || entry.manager.equals(manager) ){
      	  return entry
        }
      }
    }
  }
  return null
}

def handleEntry(list, programAction, manager, state){
	entry = findEntry(list, programAction, manager, state)
        if(entry == null){
            createEntries(list, programAction, manager)
        }
        entry = findEntry(list, programAction, manager, state)
        entry.count++
}

def createEntries(list, programAction, manager){
     for(state in stateList){
            entry = new Entry()
            entry.programAction = programAction
            entry.state = state.title
	    if(reportType == 2){	  
            	entry.manager = manager
	    }
            entry.count = 0
            list.add(entry)
     }
}