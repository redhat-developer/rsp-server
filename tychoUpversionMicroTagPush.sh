#!/bin/sh
# Pass any argument to enable debug mode
echo "This script performs the following: "
echo "  1) upversions all bundles and tp to a .Final version"
echo "  2) runs a build, commits, and pushes to master"
echo "  3) Reminds you to wait for a green build"
echo "  4) Runs mvn clean deploy to nexus, and instructs you to follow up immediately"
echo "  5) creates a repo tag, and instructs you to do a release build on jenkins"
echo "  6) Create a github release with changelog details, but asks you to attach the binary"
echo "  7) Upversions to next-SNAPSHOT, and commits"
echo "  8) Creates a milestone on github for next version"

ghtoken=`cat ~/.keys/gh_access_token`
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


echo "Here are the commits since last release"

commits=`git lg | grep -n -m 1 "Upversion to " |sed  's/\([0-9]*\).*/\1/' | tail -n 1`
commitMsgs=`git log --color --pretty=format:'%h - %s' --abbrev-commit | head -n $commits`
echo "$commitMsgs"
read -p "Press enter to continue"



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
mvn org.eclipse.tycho:tycho-versions-plugin:1.3.0:set-version -DnewVersion=$newverFinal

# Handle target platform
tpFile=`ls -1 targetplatform | grep target`
cat targetplatform/$tpFile | sed "s/-target-$oldverRaw/-target-$newverFinal/g" > targetplatform/$tpFile.bak
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


echo "Go kick a build at https://github.com/redhat-developer/rsp-server/actions/workflows/gh-actions.yml"
read -p "Press enter to continue"


echo "Are you absolutely sure you are ready to tag?"
read -p "Press enter to continue"

newVerUnderscore=`echo $newver | sed 's/\./_/g'`
git tag tp$newVerUnderscore
if [ "$debug" -eq 0 ]; then
	git push origin tp$newVerUnderscore
else 
	echo git push origin tp$newVerUnderscore
fi

git tag v$newVerUnderscore
if [ "$debug" -eq 0 ]; then
	git push origin v$newVerUnderscore
else 
	echo git push origin v$newVerUnderscore
fi

echo "Go kick another build at https://github.com/redhat-developer/rsp-server/actions/workflows/gh-actions.yml"
read -p "Press enter to continue"

echo "Let's start with the target platform"
jbang repoflattener.java site
echo "Did jbang work? If not, cancel, debug, and start over."
read -p "Press enter to continue"


echo "Making a release on github for $newverFinal TargetPlatform"
createReleasePayload="{\"tag_name\":\"tp$newVerUnderscore\",\"target_commitish\":\"master\",\"name\":\"$newverFinal.targetplatform\",\"body\":\"Release of target platform for $newverFinal\",\"draft\":false,\"prerelease\":false,\"generate_release_notes\":false}"

if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/redhat-developer/rsp-server/releases \
	  -d "$createReleasePayload" | tee createReleaseResponse.json
else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/redhat-developer/rsp-server/releases \
	  -d "$createReleasePayload"
fi
echo "Please go verify the target platform release looks correct. We will add the asset next"
read -p "Press enter to continue"

assetUrl=`cat createReleaseResponse.json | grep assets_url | cut -c 1-17 --complement | rev | cut -c3- | rev | sed 's/api.github.com/uploads.github.com/g'`
rm createReleaseResponse.json
for filename in site/target/flat-repository/*; do
  nameOnly=`echo $filename | rev | cut -f 1 -d "/" | rev`
  echo $nameOnly
  if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$nameOnly \
	  --data-binary "@$filename"
 else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$nameOnly \
	  --data-binary "@$filename"
 fi
done




echo "Making a release on github for $newverFinal"
commitMsgsClean=`git log --color --pretty=format:'%s' --abbrev-commit | head -n $commits | awk '{ print " * " $0;}' | awk '{printf "%s\\\\n", $0}' | sed 's/"/\\"/g'`
createReleasePayload="{\"tag_name\":\"v$newVerUnderscore\",\"target_commitish\":\"master\",\"name\":\"v$newverFinal\",\"body\":\"Release of $newverFinal:\n\n"$commitMsgsClean"\",\"draft\":false,\"prerelease\":false,\"generate_release_notes\":false}"
  
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/redhat-developer/rsp-server/releases \
	  -d "$createReleasePayload" | tee createReleaseResponse.json
else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/redhat-developer/rsp-server/releases \
	  -d "$createReleasePayload"
fi



echo "Please go verify the release looks correct. We will add the asset next"
read -p "Press enter to continue"

assetUrl=`cat createReleaseResponse.json | grep assets_url | cut -c 1-17 --complement | rev | cut -c3- | rev | sed 's/api.github.com/uploads.github.com/g'`
rm createReleaseResponse.json
zipFileName=`ls -1 distribution/distribution/target/ | grep zip`
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$zipFileName \
	  --data-binary "@distribution/distribution/target/$zipFileName"
else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$zipFileName \
	  --data-binary "@distribution/distribution/target/$zipFileName"
fi


zipFileNameWFly=`ls -1 distribution/distribution.wildfly/target/ | grep zip`
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$zipFileNameWFly \
	  --data-binary "@distribution/distribution.wildfly/target/$zipFileNameWFly"
else 
	echo curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  -H "Content-Type: application/octet-stream" \
	  $assetUrl?name=$zipFileNameWFly \
	  --data-binary "@distribution/distribution.wildfly/target/$zipFileNameWFly"
fi

echo "Please go verify the release looks correct and the distribution was added correctly."
read -p "Press enter to continue"

echo "Need to update the LATEST file"
newLatestContent="org.jboss.tools.rsp.distribution.latest.version=$newverFinal\norg.jboss.tools.rsp.distribution.latest.url=https://github.com/redhat-developer/rsp-server/releases/download/v$newVerUnderscore/$zipFileName"
echo -e $newLatestContent > LATEST
echo "Updating LATEST release to $newverFinal"
git commit -a -m "Updating LATEST release to $newverFinal" --signoff
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
mvn org.eclipse.tycho:tycho-versions-plugin:1.3.0:set-version -DnewVersion=$nextverWithSnapshot

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
createMilestonePayload="{\"title\":\"v$nextver\",\"state\":\"open\",\"description\":\"Tracking milestone for version $nextver\"}"
if [ "$debug" -eq 0 ]; then
	curl -L \
	  -X POST \
	  -H "Accept: application/vnd.github+json" \
	  -H "Authorization: Bearer $ghtoken"\
	  -H "X-GitHub-Api-Version: 2022-11-28" \
	  https://api.github.com/repos/redhat-developer/rsp-server/milestones \
	  -d "$createMilestonePayload"
else 
	echo curl -L \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $ghtoken"\
  -H "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/repos/redhat-developer/rsp-server/milestones \
  -d "$createMilestonePayload"
fi


