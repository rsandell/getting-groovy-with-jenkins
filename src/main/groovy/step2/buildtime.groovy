
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
 * Calculates average build time of specific jobs
 */
import jenkins.model.*
import hudson.model.*
import java.io.*

File dir = new File("offBuild-averages/")
dir.mkdir()
File fAll = new File(dir, "all.csv")
File fTotals = new File(dir, "totals.csv");

PrintWriter pAll = new PrintWriter(new FileWriter(fAll))
PrintWriter pTotals = new PrintWriter(new FileWriter(fTotals))

totals = []

String header = "displayName,startTime,duration;"
println(header)
pAll.println(header)
Jenkins.instance.items.each { TopLevelItem project ->
    if (project.name.startsWith("offbuild")) {

        long total = 0
        int count = 0
        project.builds.each { build ->
            if (build.result == Result.SUCCESS) {
                total += build.duration
                count++
                String out = build.fullDisplayName + "," + build.getTimeInMillis()
                             + "," + build.duration + ";"
                println(out)
                pAll.println(out)
            }
        }
        if (project.builds != null && count > 0) {
            long average = total / count;
            totals.push(project.name + "," + total + "," + project.builds.size() +
                        "," + average + ";")
        }
    }
}
println("")
pAll.close()
println("Written to " + fAll.getAbsolutePath())
println("")
header = "projectName,total,buildsSize,average;"
println(header)
pTotals.println(header)

totals.each {
    println(it)
    pTotals.println(it)
}
println("")
pTotals.close()
println("Written to " + fTotals.getAbsolutePath())

println("Done")