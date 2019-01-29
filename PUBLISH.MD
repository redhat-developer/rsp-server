# Releasing The Runtime Server Protocol 

## Summary

To release the rsp-server, a few steps are required, with verification at each step. 

## Deploy

Run the following command to publish

    $ mvn clean deploy

## Nexus

The above command should cause a new staging repository to be formed. Go to [https://repository.jboss.org/nexus/](https://repository.jboss.org/nexus/) to begin browsing for it. Then, on the left, click _staging repositories_. Sort by _updated_ to find the correct repository. Select that item.

Then, in the bottom view, browse to the _content_ tab. Traverse the tree manually and verify that some (or most) of the expected artifacts exist. Whene verything looks ok, you may _close_ the repository. 

If the close fails due to a non-unique item, it is most likely the targetplatforms folder. In that case, you should edit _targetplatform/rsp-target.target_ and change a comment near the end of the file listing the version. And try again. 

If everything has worked so far, copy the url for the closed repository. 

## Testing the staging repository

You can execute the following commands to test. 
PLEASE MODIFY THE COMMAND.  
The value REPONUM should be replaced with the number of the repository found in nexus. 
The value NEWVERSION should be replaced with the new target version that you have just released. 

    rm -rf ~/.m2/repository/org/jboss/tools/rsp/
    git clone git@github.com:robstryker/testRSP.git
    cd testRSP/
    cat pom.xml  | sed 's/profile-14851/profile-REPONUM/g' | sed 's/0.12.1.Final/NEWVERSION/g' > pom2.xml; 
    mv pom2.xml pom.xml
    mvn clean install


If the staging repository looks good, you can click the 'release' button. 

# Verifying

Verify that nexus now includes all expected files and versions. https://repository.jboss.org/nexus/content/repositories/releases/org/jboss/tools/rsp/

The 'release' should have dropped the staging repository, so you need to run the following in your testRSP folder once more:

    rm -rf ~/.m2/repository/org/jboss/tools/rsp
    mvn -U clean install

Verify build is green again.

Finally, to ensure everything was fully released, ensure that the distribution file with the approporiate version exists [here](http://download.jboss.org/jbosstools/adapters/snapshots/)


