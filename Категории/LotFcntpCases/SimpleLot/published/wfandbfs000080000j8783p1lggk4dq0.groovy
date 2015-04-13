/* import javax.jms.TextMessage; 

import org.apache.activemq.command.ActiveMQBytesMessage; 
import org.apache.commons.io.output.ByteArrayOutputStream; 

import ru.naumen.guic.components.forms.UIForm.UIFormUserException; 
import ru.naumen.integration.portal.LotStream; 
import ru.naumen.integration.service.IntegrationFacade; 
import ru.naumen.integration.transport.activemq.JMSServiceFacade; 
import org.apache.activemq.util.ByteSequence; 

def item = subject; 
def os = new ByteArrayOutputStream(); 
IntegrationFacade.serializeAsStream( item, LotStream.class, null, os); 

def byteSeq = new ByteSequence( os.toByteArray() ); 
ActiveMQBytesMessage msg = new ActiveMQBytesMessage(); 
msg.setContent( byteSeq ); 

TextMessage answed = (TextMessage)JMSServiceFacade.producer("servicebus://queue:portal.lot.notify").send(msg); 
if( answed != null ) 
{ 
switch( answed.getStringProperty("STATUS")) 
{ 
case "OK": 
break; 
case "ERROR": 
throw new UIFormUserException("Произошла ошибка передачи уведомления"); 
} 
} 
*/