/**
 * Makes the lamp go all disco.
 */
import jenkins.model.*
import org.jenkinsci.plugins.extremefeedback.*
import org.jenkinsci.plugins.extremefeedback.model.*

Jenkins j = Jenkins.instance
Lamps lampsPlugin = j.getPlugin(Lamps)

Lamp lamp = lampsPlugin.lamps.find()

XfRunListener run = j.getExtensionList(XfRunListener.class).find()


States.Color.values().each { States.Color color ->
    States.Action.values().each { States.Action action ->
        run.sendColorNotification(lamp.ipAddress, color, action)
        sleep(1500)
    }

}
run.sendColorNotification(lamp.ipAddress, States.Color.GREEN, States.Action.SOLID)