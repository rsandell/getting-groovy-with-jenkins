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
 * Replaces all Shell command build steps containing 'curl -s $BUILD_URL/stop' with the POST equivalent
 */
import hudson.model.*
import jenkins.model.*
import hudson.tasks.*

Jenkins j = Jenkins.instance

j.items.each { item ->
    if (item instanceof AbstractProject && !(item instanceof hudson.maven.MavenModuleSet)) {
        item.buildersList.each { builder ->
            if (builder instanceof Shell) {
                int index = builder.command.indexOf('curl -s $BUILD_URL/stop')
                if (index >= 0) {
                    int otherIndex = builder.command.indexOf('curl -s $BUILD_URL/stop -d')
                    if (otherIndex < 0) {
                        println(item.name + " " + index)
                        Shell newShell = new Shell(builder.command.replace('curl -s $BUILD_URL/stop', 'curl -s $BUILD_URL/stop -d ""'))
                        item.buildersList.replace(builder, newShell)
                        item.save()
                        println("   fixed")
                    }
                }
            }
        }
    }
}

return 0
