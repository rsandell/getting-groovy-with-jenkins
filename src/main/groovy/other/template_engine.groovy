import com.sun.nio.zipfs.ZipFileSystem
import hudson.matrix.Axis
import hudson.matrix.MatrixProject
import hudson.matrix.TextAxis
import hudson.model.Job
import jenkins.model.Jenkins

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
 * A Groovy man's template engine
 */

def startNumber = FROMSTART as Integer
def endNumber = TOEND as Integer

if (startNumber > endNumber || (endNumber - startNumber < 1000)) {
    println("Need at least a start to end gap of 1000")
    return false
}

Jenkins j = Jenkins.instance
Job template = j.getItem("Template_Sleep")

for (int major = startNumber; major < endNumber; major += 1000) {
    String generatedName = "Matrix${major}"
    if (j.getItem(generatedName) == null) {
        MatrixProject job = j.copy(template,generatedName)
        println("Created ${generatedName}")
        Axis axis = job.axes.find("workload")
        job.axes.remove(axis);
        String values = ""

        for(int minor = major; minor < major+1000; minor+=200) {
            values = values + " " + minor
        }

        axis = new TextAxis("workload", values);
        job.axes.add(axis)
        job.save()
        println("Saved ${generatedName}")
    } else {
        println("A job with the name '${generatedName}' already exists!")
    }
}


