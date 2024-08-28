#!/bin/bash

MIGRATE_FILES="migrate.sql ddpadmin_migrate.sql"

echo "INFO: checkout Release"
#git checkout Release || git checkout -b Release
git checkout master
echo "INFO: reset"
#git reset --hard gc/Release

#VERSION=$(egrep SNAPSHOT pom.xml | sed -e 's/^[^0-9]*//' -e 's/-SNAP.*//')
echo "INFO: Version 1.0.57"

export PYTHON_HOME=/proj/ciexadm100/tools/python-3.5.1
export ENM_PY=/proj/ciexadm100/tools/enm_py-3.5.1_reqmnts-1.0
export PYTHONPATH=$ENM_PY/usr/lib/python3.5/site-packages
$PYTHON_HOME/bin/python3 build/build.py --version "DDP-1.0.57"

git add CSLTroubleshootingTool/src/com/ericsson/csltroubleshooting/changelog.json
FILE_UPDATED=1

for MIGRATE_FILE in ${MIGRATE_FILES} ; do
	    FILENAME="./CSLTroubleshootingTool/src/com/ericsson/csltroubleshooting/${MIGRATE_FILE}"
	        egrep "^-- END DDP-$VERSION" $FILENAME > /dev/null
		    if [ $? -eq 0 ] ; then
			   echo "INFO: Version $VERSION already in $FILENAME"
			   else
			   echo "INFO: Updating $FILENAME"
			   echo "-- END DDP-${VERSION}" >> $FILENAME
			    git add $FILENAME
			    FILE_UPDATED=1
		fi
	 done

	    if [ ${FILE_UPDATED} -eq 1 ] ; then
	        git commit -m "appending build version $VERSION to migrate files"
		    git pull
	        git push  $GERRIT_CENTRAL/OSS/com.ericsson.ci.test/onboardingSeven master
		    git pull
        echo "INFO: Appended DDP-${VERSION}"
    fi


