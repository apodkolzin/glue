program = getProgram(object)
if(program == null){
      throw new java.lang.IllegalArgumentException("Не могу извлечь программу из параметра 'object', который имеет тип: "+object.class.canonicalName)
}
return program
        
//Находит у данного объекта или у объекта-предка метод 'getProgram' и возвращает результат его выполнения. Иначе возвращает null.
def getProgram(object){
	if(object.metaClass.respondsTo(object, "getProgram")){
  		return object.program
	}else if(object.metaClass.respondsTo(object, "getParent")){
   		return getProgram(object.parent)
	} else {
		return null
	}
}