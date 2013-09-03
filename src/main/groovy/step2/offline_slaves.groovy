
/*
 * The MIT License
 *
 * Copyright 2013 Sony Mobile Communications AB. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * List offline slaves and the reason for it.
 */
import hudson.model.*
import jenkins.model.*
import hudson.slaves.*
import java.util.Properties
import java.io.File
import java.util.LinkedList

def jenkins = Jenkins.getInstance()

def otherCauses = [:]
//TODO Fill in more stuff like package uppgrade
otherCauses["ManuallyDisconnected"]="Disconnected by \\d+.+"
otherCauses["PackageUpgrade"]="\\d{4}-\\d{2}-\\d{2}.+Packages are being upgraded.+"

statsByCategory = [:]
slavesByCategory = [:]
statsByLabel = [:]
statsTotal = 0
slavesProp = new Properties()

jenkins.computers.each { computer ->
    offlineCause = computer.getOfflineCause()
    if (offlineCause != null) {
        String category = "other"
        if (offlineCause instanceof OfflineCause.SimpleOfflineCause || offlineCause instanceof OfflineCause.ByCLI) {
            //Do some regexp digging fo known texts
            ofcTxt = offlineCause.toString()
            for (it in otherCauses.entrySet()) {
                if (ofcTxt =~ it.value) {
                    category = it.key
                    break
                }
            }
        } else {
            category = offlineCause.getClass().getSimpleName()
        }
        print(computer.getName())
        print(": ")
        println(category)

        if (slavesByCategory[category] == null) {
            slavesByCategory[category] = new LinkedList()
        }
        slavesByCategory[category].add(computer.getName())

        slavesProp.setProperty(computer.getName(), category)
        statsTotal = statsTotal + 1
        if (statsByCategory[category] == null) {
            statsByCategory[category] = 0
        }
        statsByCategory[category] = statsByCategory[category] + 1
        try {
            computer.node.getAssignedLabels().each {
                labelName = it.getName()
                if (!it.isSelfLabel()
                        && labelName.indexOf("Ubuntu") < 0
                        && labelName.indexOf("10.04") < 0
                        && labelName.indexOf("8.10") < 0
                        && labelName.indexOf("amd64") < 0
                        && labelName.indexOf("i386") < 0) {
                    if (statsByLabel[it.getName()] == null) {
                        statsByLabel[it.getName()] = 0
                    }
                    statsByLabel[it.getName()] = statsByLabel[it.getName()] + 1
                }
            }
        } catch(e) {
            println("ERR: Failed to get labels for " + computer.getName())
        }
    }
}
File storeDir = new File("offlineSlaves/")
storeDir.mkdirs()
File propsFile = new File(storeDir, "offlineSlaves.properties")
slavesProp.store(new FileWriter(propsFile), "Offline slaves")

println("")
println("Total: " + statsTotal)
println("")
csvStrColumns = new StringBuilder()
csvStrValues = new StringBuilder()
println("###Categories###")
statsByCategory.each {
    print(it.key)
    print(":")
    println(it.value)
    csvStrColumns.append(it.key).append(",")
    csvStrValues.append(it.value).append(",")
}
outFile = new PrintWriter(new FileWriter(new File(storeDir, "statsByCategory.csv")))
outFile.println(csvStrColumns.toString())
outFile.println(csvStrValues.toString())
outFile.flush()
outFile.close()
println("")

csvStrColumns = new StringBuilder()
csvStrValues = new StringBuilder()

println("###Labels###")
statsByLabel.each {
    print(it.key)
    print(":")
    println(it.value)
    csvStrColumns.append(it.key).append(",")
    csvStrValues.append(it.value).append(",")
}
outFile = new PrintWriter(new FileWriter(new File(storeDir, "statsByLabel.csv")))
outFile.println(csvStrColumns.toString())
outFile.println(csvStrValues.toString())
outFile.flush()
outFile.close()

props = new Properties()
props.setProperty("YVALUE", String.valueOf(statsTotal))
props.store(new FileWriter(new File(storeDir, "total.properties")), "Offline slaves per label")

categories = slavesByCategory.keySet()

StringBuilder html = new StringBuilder("<html><head><title>Offline Slaves By Category</title></head>\n<body>\n<table border=1 cellspacing=0 cellpadding=1>\n<tr>")
categories.each() { c ->
    html.append("<th>").append(c).append("</th>")
}
html.append("<tr>\n")
def allSlavesListed = false
def row = 0
while (!allSlavesListed) {
    def someCategoryLeft = false
    html.append("<tr>\n")
    categories.each() { c ->
        html.append("<td>")
        if (slavesByCategory[c].size() > row) {
            slave = slavesByCategory[c].get(row)
            html.append("<a href=\"/computer/").append(slave).append("\">").append(slave).append("</a>")
            someCategoryLeft = true
        }
        html.append(" </td>")
    }
    html.append("</tr>\n")
    row = row + 1
    if (!someCategoryLeft) {
        allSlavesListed = true
    }
}
html.append("</table></body></html>")

outFile = new PrintWriter(new FileWriter(new File(storeDir, "slavesByCategory.html")))
outFile.println(html.toString())
outFile.flush()
outFile.close()

