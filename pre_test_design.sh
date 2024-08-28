#!/bin/bash
set -xv
      
     if [ "${JOB_NAME}" != "test_push_master" ] ; then
		exit 0
	fi
			        
	VERSION=$(egrep SNAPSHOT pom.xml | sed -e 's/^[^0-9]*//' -e 's/-SNAP.*//')
	echo "INFO: Version $VERSION"
	echo "INFO: GIT_PREVIOUS_SUCCESSFUL_COMMIT: $GIT_PREVIOUS_SUCCESSFUL_COMMIT"
				         
	if [ "${GIT_PREVIOUS_SUCCESSFUL_COMMIT}" = "null" ] ; then
		echo "WARN: GIT_PREVIOUS_SUCCESSFUL_COMMIT not set, changelog not updated"
			else
				export PYTHON_HOME=/proj/ciexadm200/tools/python-3.5.1
				export ENM_PY=/proj/ciexadm200/tools/enm_py-3.5.1_reqmnts-1.0
				export PYTHONPATH=$ENM_PY/usr/lib/python3.5/site-packages
				${PYTHON_HOME}/bin/python build/build.py --version "DDP-${VERSION}"
		        if [ $? -ne 0 ] ; then
					echo "ERROR: Non-zero exit for build.py"
					exit 1
				fi
				git checkout master
				git branch
				git add CSLTroubleshootingTool/src/com/ericsson/csltroubleshooting/changelog.json
				if [ $? -ne 0 ] ; then
					echo "ERROR: Non-zero exit for git add changelog.json"
					exit 1
				fi
			fi
																								    
			MIGRATE_FILES="migrate.sql ddpadmin_migrate.sql"
			for MIGRATE_FILE in ${MIGRATE_FILES} ; do
			FILENAME="./CSLTroubleshootingTool/src/com/ericsson/csltroubleshooting/${MIGRATE_FILE}"
			egrep "^-- END DDP-$VERSION" $FILENAME > /dev/null
			if [ $? -eq 0 ] ; then
				echo "INFO: Version $VERSION already in $FILENAME"
			else
				echo "INFO: Updating $FILENAME"
			    echo "-- END DDP-${VERSION}" >> $FILENAME
				git add $FILENAME
				if [ $? -ne 0 ] ; then
					echo "ERROR: Non-zero exit for git add ${FILENAME}"
				   exit 1
		        fi
		    fi
		    done
		    #gitdir=$(git rev-parse --git-dir); scp -p -P 29418 enmadm100@gerrit.ericsson.se:hooks/commit-msg ${gitdir}/hooks/
		#	git commit --amend																																													       
			git commit -m "[appending build version $VERSION to migrate files]"
		    if [ $? -ne 0 ] ; then
			    echo "ERROR: Non-zero exit for git commit"
			    exit 1
			fi
			
						
			
	#		git push origin ${GERRIT_CENTRAL}/${REPO}
	#		GIT_COMMIT=$(git log --format=%H -n 1)
	#		echo $GIT_COMMIT
####            #ssh -p 29418 gerrit.ericsson.se gerrit review --verified +1 --code-review +2 --submit --project OSS/com.ericsson.ci.test/onboardingSeven -m '"Automatic +2/Submit from Release job"' $GIT_COMMIT			
																																																	       
			 git push origin ${GERRIT_CENTRAL}/${REPO} 

			 if [ $? -ne 0 ] ; then
			        echo "ERROR: Non-zero exit for git push"
			        exit 1
			fi
				      
			exit 0
