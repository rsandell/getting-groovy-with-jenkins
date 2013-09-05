
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
 * Sets the LogRotator number of builds to keep for jobs that doesn't have any log rotator configured
 */
import hudson.Util
import hudson.model.AbstractProject
import hudson.model.Job
import jenkins.model.Jenkins;
import hudson.tasks.LogRotator

println("Projects with bad rotation...");
def numbersToKeepTh = 100
def count = 0
def disabled = 0
def calcedSavedBuilds = 0
def actualRemovedBuilds = 0

Util.filter(Jenkins.instance.getItems(), Job.class).each{project ->
    def rotator = project.getBuildDiscarder()
    def thisIsIt = false
    if (rotator == null) {
        count = count + 1
        if (project instanceof AbstractProject && project.disabled) {
            disabled = disabled + 1
        }
        left = project.getBuilds().size() - numbersToKeepTh
        if (left > 0) {
            calcedSavedBuilds = calcedSavedBuilds + left
        }
        println("X  " + project.getFullName())
        thisIsIt = true
    } else if (rotator instanceof LogRotator &&
               rotator.getDaysToKeep() < 0 && rotator.getNumToKeep() < 0) {
        count = count + 1
        if (project instanceof AbstractProject && project.disabled) {
            disabled = disabled + 1
        }
        left = project.getBuilds().size() - numbersToKeepTh
        if (left > 0) {
            calcedSavedBuilds = calcedSavedBuilds + left
        }
        println("Y  " + project.getFullName())
        thisIsIt = true
    }
    if (thisIsIt && project.supportsLogRotator() && count <= 10) {
        if (rotator == null) {
            rotator = new LogRotator(-1, numbersToKeepTh, -1, -1)
        } else {
            rotator = new LogRotator(-1, numbersToKeepTh, rotator.getArtifactDaysToKeep(), rotator.getArtifactNumToKeep())
        }
        println("\tCreated: " + rotator)
        def numBuilds = project.getBuilds().size()
        project.setBuildDiscarder(rotator)
        project.save()
        println("\tSaved: " + project.getFullName())
        if (numBuilds > numbersToKeepTh) {
            println("\tPerforming Log rotation!!")
            project.logRotate()
        }
        def removedJobBuilds = numBuilds - project.getBuilds().size()
        println("\tRemoved " + removedJobBuilds)
        actualRemovedBuilds = actualRemovedBuilds + removedJobBuilds
    }
};
println("Done! Count: " + count);
println("DisabledCount: " + disabled);
println("TH: " + numbersToKeepTh + " would remove " + calcedSavedBuilds + " builds")
println("Actual Removed Builds: " + actualRemovedBuilds)