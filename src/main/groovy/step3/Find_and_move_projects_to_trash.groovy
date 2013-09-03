
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
 * Finds suitable projects to move to trash
 */

import hudson.model.*
import jenkins.model.Jenkins

import java.util.Calendar.*
import java.util.LinkedList
import hudson.tasks.*
import hudson.plugins.jobConfigHistory.*
import java.text.SimpleDateFormat
import hudson.matrix.MatrixProject
import java.lang.Exception


// Set to null if you do not want to add the items to the trash-view.
// Note, adding to view does only work since 1.389
String viewName = "Trashcan"; // Disable by setting viewName = null;


int hits = 0;
LinkedList<String> alerts = new LinkedList<String>();

ListView trashView =  null;
if (viewName != null) {
    trashView = (ListView) Jenkins.instance.getView(viewName);
    if (trashView == null) {
        println("The view was not found, no jobs will be added to it\n\n");
    }
}

for (AbstractProject p : Jenkins.instance.projects) {
    alerts.clear();
    def daysSinceLastConfigured = daysSinceLastConfigured(p);
    def daysSinceLastBuild = daysSinceLastBuild(p);
    def daysSinceLastSuccessfulBuild = daysSinceLastSuccessfulBuild(p)
    rule_testRule(p, alerts);
    //rule_closedBranchName(p, alerts);
    //rule_misConfigured(p, alerts);
    //rule_temporary(p, alerts, daysSinceLastConfigured, daysSinceLastBuild);
    //rule_experimental(p, alerts, daysSinceLastConfigured, daysSinceLastBuild);
    //rule_noOneCares1(alerts, daysSinceLastBuild);
    //rule_noOneCares2(alerts, daysSinceLastConfigured, daysSinceLastSuccessfulBuild);

    if (alerts.size() > 0) {
        if (trashView != null) {
            addAlertsToDescription(p, alerts);
            addToListView(trashView, p);
        }
        hits++;
        println(p.name + "  [ " + p.absoluteUrl + " ]");
        alerts.each( { alert -> println("   [${alert}]") } );
        println();
    }
}

println("------------------------------");
println("Hits: " + hits);

/* =============== RULES ================== */
public static void rule_closedBranchName (AbstractProject p, List<String> alerts) {
    def closedBranchNames = ["projectOne", "projectOld", "projectOlder", "ginger-integration"];
    closedBranchNames.each{branchName ->
        if (p.name.toLowerCase().contains(branchName)) {
            alerts.add("Name of project contains a closed branch name: " + branchName);
        }
    };
}

public static void rule_testRule (AbstractProject p, List<String> alerts) {
    if(p.name.toLowerCase().equals("IAmOld_test")) {
        alerts.add("Name of project is " + p.name);
        alerts.add("I said so!");
    }
}

public static void rule_misConfigured (AbstractProject p, List<String> alerts) {
    int nrOfBuildSteps = numberOfBuildSteps(p);
    if (nrOfBuildSteps == 0) {
        if (p instanceof FreeStyleProject || p instanceof MatrixProject) {
            if (hoursSinceCreated(p) > 2) {
                alerts.add("Project has no build steps");
            }
        }
    }
}

public static void rule_temporary (AbstractProject p, List<String> alerts, int daysSinceLastConfigured, int daysSinceLastBuild) {
    def temporaryNames = ["tmp", "temp", "\$tst-", "\$test-"];
    temporaryNames.each{temp ->
        if (p.name.toLowerCase().contains(temp)) {
            if (daysSinceLastBuild > 14 && daysSinceLastConfigured > 14) {
                alerts.add("Name of project indicates it to be a temporary project: " + temp);
            }
        }
    };
}

public static void rule_experimental (AbstractProject p, List<String> alerts, int daysSinceLastConfigured, int daysSinceLastBuild) {
    if (p.name.toLowerCase().startsWith("experimental_")) {
        if (daysSinceLastBuild > 60 && daysSinceLastConfigured > 60) {
            alerts.add("Name of project indicates it to be Experimental");
        }
    }
}

public static void rule_noOneCares1 (List<String> alerts, int daysSinceLastBuild) {
    if (daysSinceLastBuild > 180) {
        alerts.add("Project has not been built for " + daysSinceLastBuild);
    }
}

public static void rule_noOneCares2 (List<String> alerts, int daysSinceLastConfigured, int daysSinceLastSuccessfulBuild) {
    if (daysSinceLastSuccessfulBuild > 180 && daysSinceLastConfigured > 90) {
        alerts.add("Project has not been built for " + daysSinceLastSuccessfulBuild + " and not configured since " + daysSinceLastConfigured);
    }
}


public static void rule_120_days_Since_Last_Build (AbstractProject p, List<String> alerts) {
    int days = daysSinceLastBuild(p);
    if (days > 120)  {
        alerts.add("120+ days since last build (" + days + ")");
    }
}

public static void rule_No_Successful_Build_for_180_days (AbstractProject p, List<String> alerts) {
    int days = daysSinceLastSuccessfulBuild(p);
    if (days > 180)  {
        alerts.add("180+ days since last successful build (" + days + ")");
    }
}

public void rule_No_Build_Steps_and_No_Builds_for_30_days (AbstractProject p, LinkedList<String> alerts) {
    if (p instanceof FreeStyleProject) {
        FreeStyleProject f = (FreeStyleProject) p;
        int days = daysSinceLastBuild(p)
        if (f.builders.size() == 0 &&  days > 30) {
            alerts.add("No build steps and 30+ days since last build (" + days + " days)");
        }
    }
}

public void rule_Experimental_and_No_Builds_for_60_days(AbstractProject p, LinkedList<String> alerts) {
    if (p.name.toUpperCase().startsWith("EXPERIMENTAL_")) {
        int days = daysSinceLastBuild(p)
        if (days > 60) {
            alerts.add("EXPERIMENTAL and no build for 60 + days (" + days + " days)");
        }
    }
}


/* =============== HELPERS ================== */

public static int daysSinceLastSuccessfulBuild(AbstractProject p) {
    if (p.lastSuccessfulBuild == null) {
        return hoursSinceCreated(p) / 24;
    } else {
        return (Calendar.instance.timeInMillis - p.lastSuccessfulBuild.timeInMillis) / 1000 / 60 / 60 / 24;
    }
}

public static int daysSinceLastBuild(AbstractProject p) {
    if (p.lastBuild == null) {
        return hoursSinceCreated(p) / 24;
    } else {
        return (Calendar.instance.timeInMillis - p.lastBuild.timeInMillis) / 1000 / 60 / 60 / 24;
    }
}

public static int hoursSinceCreated(AbstractProject p) {
    def format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    def action = p.getAction(JobConfigHistoryProjectAction.class)
    if (action != null) {
        for(def conf : action.getJobConfigs()) {
            if (conf.operation == "created") {
                def date = format.parse(conf.date);
                return (Calendar.instance.timeInMillis - date.getTime()) / 1000 / 60 / 60;
            }
        }
    }
    return  (Calendar.instance.timeInMillis - p.configFile.file.lastModified()) / 1000 / 60 / 60;
}

public static int daysSinceLastConfigured(AbstractProject p) {
    def format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    def action = p.getAction(JobConfigHistoryProjectAction.class)
    if (action != null) {
        try {
            for(def conf : action.getJobConfigs()) {
                if (conf.userID == "SYSTEM" || conf.userID == "23052130") { //23052130 ran the logrotator update, so to eliminate that edit we don't count that user
                    continue;
                }
                def date = format.parse(conf.date);
                if (date != null) {
                    return  (Calendar.instance.timeInMillis - date.getTime()) / 1000 / 60 / 60 / 24;
                }
            }
        } catch(Exception e) {
            e.printStackTrace()
        }
    }
    return 0;
}

public static int numberOfBuildSteps(AbstractProject p) {
    if (p instanceof FreeStyleProject) {
        FreeStyleProject f = (FreeStyleProject) p;
        return f.builders.size();
    }
    if (p instanceof MatrixProject) {
        MatrixProject m = (MatrixProject) p;
        return m.builders.size();
    }
    return 0;
}

public static addToListView(ListView listView, AbstractProject p) {
    listView.jobNames.add(p.name);
    listView.save();
}

public static addAlertsToDescription(AbstractProject p, LinkedList<String> alerts) {
    String alertsDescription = "<span class=\"error\" style=\"font-size:larger\">This project will be removed due to the following reasons:<ul>\n";
    String description = p.getDescription();
    alerts.each{alert ->
        alertsDescription+="<li>" + alert + "</li>\n";

    };
    alertsDescription += "</ul></span>";
    description = alertsDescription + description;
    p.setDescription(description);
}
