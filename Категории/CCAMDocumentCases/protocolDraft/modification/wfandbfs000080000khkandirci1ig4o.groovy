package resources.groovy

/**
 * http://gpjira.naumen.ru/browse/FCNTP-1140
 * Написать скрипт при переводе состояния (Согласование -> Доработка)
 *
 * Выставляет значение поля currentVersion объекта класса ProtocolDraft в false.
 *
 * @author sshatalkin
 */

import ru.naumen.ccamcore.bobject.bactions.EditCCAMMetaBOBA
import ru.naumen.common.containers.MapPropertySource
import ru.naumen.core.ui.BKUIUtils

valueMap = new HashMap<String, Object>()
valueMap.put("currentVersion", false)

props = new MapPropertySource(valueMap)
author = BKUIUtils.currentPerson
ba = new EditCCAMMetaBOBA(subject, author, props)
ba.execute(session)
