#!/bin/sh
# Pass any argument to enable debug mode
argsPassed=$#
echo "args: " $argsPassed
if [ "$argsPassed" -eq 1 ]; then
	debug=1
	echo "YOU ARE IN DEBUG MODE. Changes will NOT be pushed upstream"
else
	echo "The script is live. All changes will be pushed, deployed, etc. Live."
	debug=0
fi
read -p "Press enter to continue"

apiStatus=`git status -s | wc -l`
if [ $apiStatus -ne 0 ]; then
   echo "This repository has changes and we won't be able to auto upversion. Please commit or stash your changes and try again"
   exit 1
fi


oldverRaw=`cat pom.xml  | grep "version" | head -n 2 | tail -n 1 | cut -f 2 -d ">" | cut -f 1 -d "<" |  awk '{$1=$1};1'`
oldver=`echo $oldverRaw | sed 's/\.Final//g' | sed 's/-SNAPSHOT//g'`
oldverHasSnapshot=`cat pom.xml  | grep "version" | head -n 2 | tail -n 1 | cut -f 2 -d ">" | cut -f 1 -d "<" | grep -i snapshot | awk '{$1=$1};1' | wc -c`


if [ "$oldverHasSnapshot" -eq 0 ]; then
	newLastSegment=`echo $oldver | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
	newverPrefix=`echo $oldver | cut -f 1,2 -d "."`
	newver=$newverPrefix.$newLastSegment
else 
	newver=$oldver
fi
newverFinal=$newver.Final

echo "Old version is $oldverRaw"
echo "New version is $newverFinal"
echo "Updating pom.xml and target platform with new version"
read -p "Press enter to continue"
mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$newverFinal

# Handle target platform
tpFile=`ls -1 targetplatform | grep target`
cat targetplatform/$tpFile | sed "s/-target-$oldver/-target-$newver/g" > targetplatform/$tpFile.bak
mv targetplatform/$tpFile.bak targetplatform/$tpFile

echo "Running a quick build to make sure everything's ok..."
read -p "Press enter to continue"
mvn clean install -DskipTests

echo "Did it succeed?"
read -p "Press enter to continue"

curBranch=`git rev-parse --abbrev-ref HEAD`
echo "Committing and pushing to $curBranch"
git commit -a -m "Upversion to $newver for release" --signoff
if [ "$debug" -eq 0 ]; then
	git push origin $curBranch
else 
	echo git push origin $curBranch
fi


echo "Go kick a Jenkins Job please. Let me know when it's DONE and green."
read -p "Press enter to continue"


echo "Time to deploy to nexus. The release process begins NOW. ARE YOU READY?"
read -p "Press enter to continue"
if [ "$debug" -eq 0 ]; then
	mvn clean deploy
else 
	echo mvn clean deploy
fi


echo "Go to nexus https://repository.jboss.org/nexus/#stagingRepositories and find the repo"
echo "Then do close and release on that repository!"
read -p "Press enter to continue"


echo "Are you absolutely sure you are ready to tag?"
read -p "Press enter to continue"

newVerUnderscore=`echo $newver | sed 's/\./_/g'`
git tag v$newVerUnderscore
if [ "$debug" -eq 0 ]; then
	git push origin v$newVerUnderscore
else 
	echo git push origin v$newVerUnderscore
fi

echo "Go kick another jenkins job with a release flag."
read -p "Press enter to continue"


echo "Make sure to go create a release on github"
echo "Here are the commits since last release"

commits=`git lg | grep -n -m 2 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`
git lg | head -n $commits

echo "You need to act on the above. In the future we'll automate it. Do it NOW"
read -p "Press enter to continue"

echo "We are released. It's time to move the repo to next-SNAPSHOT"
read -p "Press enter to continue"


nextLastSegment=`echo $newver | cut -f 3 -d "." | awk '{ print $0 + 1;}' | bc`
nextverPrefix=`echo $newver | cut -f 1,2 -d "."`
nextver=$nextverPrefix.$nextLastSegment
nextverWithSnapshot=$nextver-SNAPSHOT

echo "New version is $newver"
echo "Next version is $nextver"
echo "Updating pom.xml and target platform with next version snapshot. Ready?"
read -p "Press enter to continue"
mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=$nextverWithSnapshot

# Handle target platform
tpFile=`ls -1 targetplatform | grep target`
cat targetplatform/$tpFile | sed "s/-target-$newver.Final/-target-$nextverWithSnapshot/g" > targetplatform/$tpFile.bak
mv targetplatform/$tpFile.bak targetplatform/$tpFile


echo "Running a quick build to make sure everything's ok..."
read -p "Press enter to continue"
mvn clean install -DskipTests

echo "Did it succeed?"
read -p "Press enter to continue"

curBranch=`git rev-parse --abbrev-ref HEAD`
echo "Committing and pushing to $curBranch"
git commit -a -m "Move to $nextverWithSnapshot" --signoff
if [ "$debug" -eq 0 ]; then
	git push origin $curBranch
else 
	echo git push origin $curBranch
fi



echo "Creating a milestone on gh for $nextver. Ready?"
read -p "Press enter to continue"
token=`cat ~/.keys/gh_access_token`
createMilestonePayload="{\"title\":\"v$nextver\",\"state\":\"open\",\"description\":\"Tracking milestone for version $nextver\"}"
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $token"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/redhat-developer/rsp-server/milestones \
	  -d "$createMilestonePayload"
else 
	echo curl -L \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $token"\
  -H "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/repos/redhat-developer/rsp-server/milestones \
  -d "$createMilestonePayload"
fi


