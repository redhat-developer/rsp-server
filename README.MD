# The Simple Server Protocol 

## Summary

This protocol will allow an IDE or other client to control various types of application servers. The IDE will communicate with any number of servers whose job is to discover, start, stop, and eventually publish to these application servers. 

The protocol is based on LSP4J, though the messages that can be sent or received are completely unrelated to the language server protocol itself. In short, the base protocol is the same as LSP, but the specification of the messages is different. 

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification). 

## Install

This client / server code can be found for the time being at this location:  https://github.com/robstryker/org.jboss.tools.ssp/

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/),
and then clone your fork:

    $ git clone git@github.com:<you>/org.jboss.tools.ssp.git
    $ cd org.jboss.tools.ssp
    $ git remote add upstream git://github.com/robstryker/org.jboss.tools.ssp.git

At any time, you can pull changes from the upstream and merge them onto your master:

    $ git checkout master               # switches to the 'master' branch
    $ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
    $ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the
'upstream/master'.

## Building The client and server

This command will run the build:

    $ mvn clean verify

If you just want to check if things compiles/builds you can run:

    $ mvn clean verify -DskipTest=true

But *do not* push changes without having the new and existing unit tests pass!

## Running the Server

To run the server, first build the entire project.

    [org.jboss.tools.ssp] $ mvn clean verify

Next, run the following command to launch the server:

    [org.jboss.tools.ssp] $ cd server-wildfly
    [server-wildfly] $ mvn exec:java

To customize the hosts or ports, you can either edit the pom.xml for these modules or pass flags. See https://www.mojohaus.org/exec-maven-plugin/usage.html  for more information. 

## Using the CLI

The CLI is a proof-of-concept only, currently demonstrating only that the SSP connection and ability to pass communications back and forth works as expected. 


To run the client, in a new shell, run the following:

    [org.jboss.tools.ssp] $ cd client-cli
    [client-cli] $ mvn exec:java


The following commands are supported:

    1) list paths
    2) add path /some/path
    3) remove path /some/path
    4) search path /some/path

See this example session:

        list paths
        Paths:
        add path /home/rob/garbage
        list paths
        Paths:
           /home/rob/garbage
        search path /home/rob/garbage
        Beans:
           garbage,UNKNOWN,,/home/rob/garbage
        add path /home/rob/apps/jboss/unzipped/wildfly-11.0.0.Final.zip.expanded/
        list paths
        Paths:
           /home/rob/garbage
           /home/rob/apps/jboss/unzipped/wildfly-11.0.0.Final.zip.expanded/
        search path /home/rob/apps/jboss/unzipped/wildfly-11.0.0.Final.zip.expanded/
        Beans:
           wildfly-11.0.0.Final.zip.expanded,WildFly,11.0,/home/rob/apps/jboss/unzipped/wildfly-11.0.0.Final.zip.expanded
        remove path /home/rob/apps/jboss/unzipped/wildfly-11.0.0.Final.zip.expanded/
        remove path /home/rob/garbage
        list paths
        Paths:
        exit



## Contribute fixes and features

_SSP_ is open source, and we welcome anybody that wants to
participate and contribute!

If you want to fix a bug or make any changes, please log an issue in
the [JBoss Tools JIRA](https://issues.jboss.org/browse/JBIDE)
describing the bug or new feature and give it a component type of
`stack-server-protocol`. Then we highly recommend making the changes on a
topic branch named with the JIRA issue number. For example, this
command creates a branch for the JBIDE-1234 issue:

	$ git checkout -b jbide-1234

After you're happy with your changes and a full build (with unit
tests) runs successfully, commit your changes on your topic branch
(with good comments). Then it's time to check for any recent changes
that were made in the official repository:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git checkout jbide-1234           # switches to your topic branch
	$ git rebase master                 # reapplies your changes on top of the latest in master
	                                      (i.e., the latest from master will be the new base for your changes)

If the pull grabbed a lot of changes, you should rerun your build with
tests enabled to make sure your changes are still good.

You can then push your topic branch and its changes into your public fork repository:

	$ git push origin jbide-1234         # pushes your topic branch into your public fork of SSP

And then [generate a pull-request](http://help.github.com/pull-requests/) where we can
review the proposed changes, comment on them, discuss them with you,
and if everything is good merge the changes right into the official
repository.

