import ru.naumen.guic.formatters.CollectionLineFormatter; 
import ru.naumen.guic.formatters.CodeFormatter; 
import ru.naumen.guic.formatters.CollectionSortCommaLineFormatter; 

res = CollectionSortCommaLineFormatter._instance.setFormatter(new CodeFormatter()).format(OKVED); 
CollectionSortCommaLineFormatter._instance.setFormatter(null); 
res;