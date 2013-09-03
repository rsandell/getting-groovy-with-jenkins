
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
 * Yank All Dead Executors
 * Is the tie matrix parent label expressions causing you pain again?
 * Restart all the dead executors with this little handy script.
 * But don't forget to fix the root cause by the help of my friend Find_bad_Labeled_Projects.groovy first.
 */
import hudson.model.*
import jenkins.model.*

Jenkins.getInstance().getComputers().each() { computer ->
    computer.getExecutors().each() {executor ->
        if (!executor.isAlive() && executor.getCauseOfDeath() != null) {
            println(computer.name + " #" + executor.number + " is dead ("+executor.getCauseOfDeath().getMessage()+")")
            computer.removeExecutor(executor)
        }
    }
}