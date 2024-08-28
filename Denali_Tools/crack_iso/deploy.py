import ConfigParser
import getpass
import os
from os.path import expanduser
import platform
import re
import shutil
import socket
from subprocess import call
import sys
import urllib2
import zipfile

class RestException(Exception):
    pass

class ShellExecutionException(Exception):
    pass

class CommandLineHelper():
    def runCommand(self, command):
        changeDirCommand = 'cd '
        returnCode = 0
        print 'INFO: Executing Command: ' + command
        if command.startswith(changeDirCommand):
            os.chdir(command[len(changeDirCommand):])
        else:
            try:
                returnCode = call(command)
            except Exception, e:
                raise ShellExecutionException(e)

        return returnCode

    def runSubprocess(self, command):
        returnCode = 0

        try:
            returnCode = os.system(command)
        except Exception, e:
            raise ShellExecutionException(e)

        return returnCode

    def getOperatingSystem(self):
        return platform.system()

class FileHelper():
    def readFileToString(self, fileName):
        file = open(fileName, 'r')
        return file.read()

    def checkDirectoryExists(self, directory):
        dirExists = os.path.isdir(directory)
        return dirExists

    def checkFileExists(self, fileName):
        fileExists = os.path.isfile(fileName)
        return fileExists

    def unzipFile(self, fileName, ):
        zip_ref = zipfile.ZipFile(fileName, 'r')
        zip_ref.extractall()
        zip_ref.close()

    def getFullFileName(self, directory, fileNamePart):
        dirList = os.listdir(directory)

        fullFileName = ''
        for file in dirList:
            if file.startswith(fileNamePart):
                fullFileName = file

        return fullFileName

    def deleteDirectory(self, directory):
        try:
            shutil.rmtree(directory)
        except Exception, e:
            raise Exception(e)

    def listFileTypeInDirectory(self, directory, fileType):
        dirList = os.listdir(directory)
        for file in dirList:
            if file.endswith(fileType):
                print 'INFO: ', file

    def getHomeDirectory(self):
        homeDirectory = expanduser("~")

        return homeDirectory

class UrlHelper():
    def __init__(self):
        self._RESPONSE_TIMEOUT = 60000

    def downloadFileFromUrl(self, url):
        fileName = url.split('/')[-1]
        urlInfo = urllib2.urlopen(url)
        with open(fileName, 'wb') as downloadedFile:
            urlMetaData = urlInfo.info()
            fileSize = int(urlMetaData.getheaders("Content-Length")[0])
            print "INFO: Downloading: %s Bytes: %s" % (fileName, fileSize)
            self._downloadFile(downloadedFile, fileSize, urlInfo)

    def _downloadFile(self, downloadedFile, fileSize, urlInfo):
        fileSizeDownloaded = 0
        blockSize = 8192
        while True:
            downloadBuffer = urlInfo.read(blockSize)
            if not downloadBuffer:
                break

            fileSizeDownloaded += len(downloadBuffer)
            downloadedFile.write(downloadBuffer)
            status = r"%10d  [%3.2f%%]" % (fileSizeDownloaded, fileSizeDownloaded * 100. / fileSize)
            status = status + chr(8)*(len(status)+1)
            print 'INFO: ', status

    def getResponse(self, url):
        try:
            response = urllib2.urlopen(url, timeout = self._RESPONSE_TIMEOUT).read()
        except urllib2.URLError, e:
            print type(e)
            raise RestException("ERROR: RestException: There was an error: %r" % e)
        except socket.timeout, e:
            print type(e)
            raise RestException("ERROR: RestException: There was an error: %r" % e)

        return response

class Repository():
    def __init__(self):
        self._userName = getpass.getuser()
        self._DEFAULT_CONFIGURATION_ENTITIES_REPOSITORY_NAME = 'defaultConfigurableEntities'
        self._DEPLOYMENT_DESCRIPTION_REPOSITORY_NAME = 'deploymentDescriptions'
        self.cliHelper = CommandLineHelper()
        self.fileHelper = FileHelper()

    def _getDefaultConfigurableEntitiesCloneCommand(self):
        repository = 'git clone ssh://' +  self._userName + '@gerrit.ericsson.se:29418/OSS/com.ericsson.oss.itpf.deployment.tools/defaultConfigurableEntities'

        return repository

    def _getDeploymentDescriptionsCloneCommand(self):
        repository = 'git clone ssh://' + self._userName + '@gerrit.ericsson.se:29418/OSS/com.ericsson.oss.itpf.deployment.descriptions/deploymentDescriptions'

        return repository

    def _checkDefaultConfigurableEntitiesExists(self):
        dirExists = self.fileHelper.checkDirectoryExists(self._DEFAULT_CONFIGURATION_ENTITIES_REPOSITORY_NAME)

        return dirExists

    def _checkDeploymentDescriptorExists(self):
        dirExists = self.fileHelper.checkDirectoryExists(self._DEPLOYMENT_DESCRIPTION_REPOSITORY_NAME)

        return dirExists

    def _getGitTagCommand(self, repository, version):

        gitTagCommand = 'git checkout tags/' + repository + '-' + version

        return gitTagCommand

    def _updateRepository(self, directory, version):
        command = self._getGitTagCommand(directory, version)
        print '\n\n\nINFO: Updating repository with tag for version: ' + version
        print 'INFO: Fetching ' + directory + ' repository from git'
        self.cliHelper.runCommand('cd ' + directory)
        self.cliHelper.runSubprocess(command)
        self.cliHelper.runCommand('cd ..')

        print '\n\n\nINFO: Skip this step if it is not applicable to your product'
        print 'INFO: You need to update the  "' + directory + '" repository:'        
        raw_input("Press Enter when the necessary Changes have been made in the repository: ")
        
        self._mavenCleanInstall(directory)
    
    def _mavenCleanInstall(self, directory):
        print 'INFO: Running Mvaen Clean Install on Repository: ' + directory
        
        mavenCleanInstallCommand = 'mvn clean install -U'
        self.cliHelper.runCommand('cd ' + directory)
        self.cliHelper.runSubprocess(mavenCleanInstallCommand)
        self.cliHelper.runCommand('cd ..')

    def cloneRepositoryDefaultConfigurableEntities(self, version):
        if not self._checkDefaultConfigurableEntitiesExists():
            cloneCommand = self._getDefaultConfigurableEntitiesCloneCommand()
            print cloneCommand
            self.cliHelper.runSubprocess(cloneCommand)

        self._updateRepository(self._DEFAULT_CONFIGURATION_ENTITIES_REPOSITORY_NAME, version)

    def cloneRepositoryDeploymentDescriptions(self, version):
        if not self._checkDeploymentDescriptorExists():
            cloneCommand = self._getDeploymentDescriptionsCloneCommand()
            self.cliHelper.runSubprocess(cloneCommand)

        self._updateRepository(self._DEPLOYMENT_DESCRIPTION_REPOSITORY_NAME, version)


class ContinousIntegrationRestHelper():
    def __init__(self):
        self.urlHelper = UrlHelper()

    def getLatestEnmProductNumber(self, enmVersion):
        # may need to include a check for cloud or physical later on.
        url='https://cifwk-oss.lmera.ericsson.se/getLastGoodProductSetVersion/?drop=' + enmVersion + '&productSet=ENM'
        return self.urlHelper.getResponse(url)

    def getMasterSedVersion(self):
        url = 'https://cifwk-oss.lmera.ericsson.se/getMasterSedVersion/'
        return self.urlHelper.getResponse(url)

    def getAomNumber(self, enmVersion):
        url = 'https://cifwk-oss.lmera.ericsson.se/getAOMRstate/?product=ENM&drop=' + enmVersion
        return self.urlHelper.getResponse(url).split(' ', 1)[0]

    def getRState(self, enmVersion):
        url = 'https://cifwk-oss.lmera.ericsson.se/getAOMRstate/?product=ENM&drop=' + enmVersion
        return self.urlHelper.getResponse(url).split(' ', 1)[1]

    def getEnmDropVersion(self, enmVersion):
        url = 'https://cifwk-oss.lmera.ericsson.se/getlatestisover/?drop=' + enmVersion + '&product=ENM'
        return self.urlHelper.getResponse(url)

    def getLitpDropVersion(self, enmVersion):
        url = 'https://cifwk-oss.lmera.ericsson.se/getlatestisover/?drop=' + enmVersion + '&product=LITP'
        return self.urlHelper.getResponse(url)

    def getIsoContents(self, enmVersion):
        url = 'https://cifwk-oss.lmera.ericsson.se/getDropContents/?drop=' + enmVersion +'&product=ENM&pretty=true'
        return self.urlHelper.getResponse(url)

    def generateFeatureDownloadLink(self, feature, version, location, fileType):
        url = 'https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/com/ericsson/oss/' + location + '/' + feature + '/' + version + '/' + feature + '-' + version + '.' + fileType

        return url

class DeploymentSupportToolingHelper():

    def __init__(self):
        self._DEPLOYMENTS_TEMPLATE_FEATURE_NAME = 'ERICenmdeploymenttemplates_CXP9031758'
        self._DEPLOYMENT_SUPPORT_TOOLING_CLI_URL = 'https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/service/local/repositories/releases/content/com/ericsson/oss/itpf/deployment/deployment-support-tooling-cli/'
        self.continousIntegrationRestHelper = ContinousIntegrationRestHelper()
        self.urlHelper = UrlHelper()
        self.fileHelper = FileHelper()
        self.cliHelper = CommandLineHelper()
        self.sevenZipCommand = r'C:\"Program Files"\7-Zip\7z x %s -o%s'
        self.tmpDirectory = 'tmp'
        self.ericssonDirectory = 'ericsson'
        self.currentDirectory = '.'
        self.WINDOWS = 'Windows'
        self.LINUX = 'Linux'


    def generateCommandUseLatestVersions(self):
        command = 'dst.sh generate -g com.ericsson.oss.itpf.deployment.descriptions:enm-full-cdb-deployment -t cloud -s 2 -m update-snapshots'

        return command

    def generateCommandFullEnmInstall(self, deploymentDescriptionsVersion, dceVersion):
        command = 'dst.sh generate -g com.ericsson.oss.itpf.deployment.descriptions:enm-full-cdb-deployment:' + deploymentDescriptionsVersion + ' -t cloud -s 2 -m update-snapshots -dce ' + dceVersion

        return command

    def _getFeatureVersion(self, isoContents, feature):
        nameText = '"name":"' + feature + '",'
        versionText = '"version":"'

        version = self._getFeatureVersionFromBlock(isoContents, nameText, versionText)

        return version

    def _getFeatureVersionFromBlock(self, isoContents, nameText, versionText):
        version = ''
        nameFound = False

        # the name is before the version
        # if the name is found it will check the version
        for line in isoContents.splitlines():
            if not nameFound:
                if line == nameText:
                    nameFound = True
            else:
                if line.startswith(versionText):
                    version = line.replace(versionText, '')
                    version = version[:-1]
                    break
        return version

    def getDeploymentTemplateVersion(self, isoContents):
        deploymentTemplateVersion = self._getFeatureVersion(isoContents, self._DEPLOYMENTS_TEMPLATE_FEATURE_NAME)
        return deploymentTemplateVersion

    def downloadDeploymentTemplate(self, isoContents):
        deploymentTemplateVersion = self.getDeploymentTemplateVersion(isoContents)
        deploymentsTemplateLocation = 'itpf/deployment/descriptions'
        deploymentTemplatesUrl = self.continousIntegrationRestHelper.generateFeatureDownloadLink(self._DEPLOYMENTS_TEMPLATE_FEATURE_NAME, deploymentTemplateVersion, deploymentsTemplateLocation, 'rpm')

        self.urlHelper.downloadFileFromUrl(deploymentTemplatesUrl)

        self._extractDeploymentTemplatesRpmFile()

    def _extractDeploymentTemplatesRpmFile(self):
        print 'INFO: Extracting Deployment Templates RPM File'
        deploymentsTemplateRpmFileName = self.fileHelper.getFullFileName(self.currentDirectory, self._DEPLOYMENTS_TEMPLATE_FEATURE_NAME)
        operatingSystem = self.cliHelper.getOperatingSystem()

        if operatingSystem == self.WINDOWS:
            self._extractDeploymentTemplatesWindows(deploymentsTemplateRpmFileName)
        elif operatingSystem == self.LINUX:
            self._extractDeploymentTemplatesLinux(deploymentsTemplateRpmFileName)

    def _extractDeploymentTemplatesWindows(self, deploymentsTemplateRpmFileName):

        if self.fileHelper.checkDirectoryExists(self.tmpDirectory):
            print 'INFO: Deleting directory: ' +self.tmpDirectory
            self.fileHelper.deleteDirectory(self.tmpDirectory)

        extractDeploymentsTemplateCommand = self.sevenZipCommand % (deploymentsTemplateRpmFileName, self.tmpDirectory)
        self.cliHelper.runSubprocess(extractDeploymentsTemplateCommand)
        self._extractDeploymentsTemplateCpioFile()

    def _extractDeploymentTemplatesLinux(self, deploymentsTemplateRpmFileName):
        extractDeploymentsTemplateCommand = 'rpm2cpio ' + deploymentsTemplateRpmFileName + ' |cpio -idmv'
        self.cliHelper.runSubprocess(extractDeploymentsTemplateCommand)

    def _extractDeploymentsTemplateCpioFile(self):
        if self.fileHelper.checkDirectoryExists(self.ericssonDirectory):
            print 'INFO: Deleting directory: ' +self.ericssonDirectory
            self.fileHelper.deleteDirectory(self.ericssonDirectory)

        deploymentsTemplateCpioFileName = self.fileHelper.getFullFileName(self.tmpDirectory, self._DEPLOYMENTS_TEMPLATE_FEATURE_NAME)
        extractDeploymentsTemplateCpioCommand = self.sevenZipCommand % (self.tmpDirectory  + '\\' + deploymentsTemplateCpioFileName, self.currentDirectory)
        self.cliHelper.runSubprocess(extractDeploymentsTemplateCpioCommand)

    def getBomFileWithDstVersions(self, location = 'ericsson/deploymentDescriptions/'):
        dirList = os.listdir(location)
        bomFileEnding = '.txt'
        fileName = ''
        for file in dirList:
            if file.endswith(bomFileEnding):
                fileName = file
                break

        bomFile = self.fileHelper.readFileToString(location + fileName)

        return bomFile

    def _getVersion(self, searchText, input):
        version = ''
        for line in input.splitlines():
            if line.startswith(searchText):
                version = line.split(' ', 1)[1]
                break

        return version

    def getDeploymentSupportToolingVersion(self, input):
        dstText = 'dst-version'
        dstVersion = self._getVersion(dstText, input)

        return dstVersion

    def getDefaultConfigurableEntitiesVersion(self, input):
        dceText = 'dce-version'
        dceVersion = self._getVersion(dceText, input)

        return dceVersion

    def downloadDeploymentSupportTooling(self, dstVersion):
        fileName = 'deployment-support-tooling-cli-' + dstVersion + '-bin.zip'
        url = self._DEPLOYMENT_SUPPORT_TOOLING_CLI_URL + dstVersion + '/' + fileName

        self.urlHelper.downloadFileFromUrl(url)
        self.fileHelper.unzipFile(fileName)

    def runDeploymentSupportTooling(self, deploymentDescriptionsVersion, dstVersion, dceVersion):
        deploymentSupportToolingDir = 'deployment-support-tooling-cli-' + dstVersion + '/bin'

        command = self.generateCommandFullEnmInstall(deploymentDescriptionsVersion, dceVersion)
        cliHelper = CommandLineHelper()
        cliHelper.runCommand('cd ' + deploymentSupportToolingDir)
        cliHelper.runSubprocess('sh ' + command)
        cliHelper.runCommand('cd ../../')

        self._printDeploymentDescriptionsXmlFile()

    def _printDeploymentDescriptionsXmlFile(self):
        homeDirectory = self.fileHelper.getHomeDirectory()
        xmlFileType = '_dd.xml'
        print '\n\nINFO: Upload the following file to the "pre-commit" using the following instructions: http://confluence-nam.lmera.ericsson.se/x/mAxaBg'
        print 'INFO: Current location: https://cifwk-oss.lmera.ericsson.se/dmt/uploadSnapshot/'
        self.fileHelper.listFileTypeInDirectory(homeDirectory, xmlFileType)
        print '\n\nINFO: OPTIONAL: Add the link that is generated by the link into install.properties file'
        print '    e.g. xmlfile = https://cifwk-oss.lmera.ericsson.se/static/tmpUploadSnapshot/2016-03-14_17-37-55/2svc_enm-full-cdb-deployment_cloud_test_dd.xml'
        raw_input('When this is done press Enter: ')


class InstallHelper():

    def __init__(self, enmVersion):
        self._enmVersion = enmVersion
        self.fileHelper = FileHelper()

    def generateInstallCommand(self, enmProductVersion):

        while True:
            configFile = raw_input("\n\n\nEnter Y/y if you are using a config file.\nEnter N/n to enter the values manually: ")

            command = ''

            if configFile.lower() == 'n':
                command = self._generateInstallCommandFromUserInput(enmProductVersion)
                break
            elif configFile.lower() == 'y':
                command = self._generateInstallCommandConfigFile(enmProductVersion)
                break

        return command

    def _generateInstallCommandConfigFile(self, enmProductVersion):
        propertiesFile = 'install.properties'

        propertiesFileExists = self.fileHelper.checkFileExists(propertiesFile)
        command = ''
        if propertiesFileExists:
            section = 'INSTALL'
            sedUrlProperty = 'sedurl'
            xmlFileProperty = 'xmlfile'
            deployPackageProperty = 'deploypackage'

            config = ConfigParser.RawConfigParser()
            config.read(propertiesFile)

            sedUrl = config.get(section, sedUrlProperty)
            xmlFile = config.get(section, xmlFileProperty)
            deployPackages = config.get(section, deployPackageProperty)

            command = self._generateInstallCommandSyntax(enmProductVersion, sedUrl, xmlFile, deployPackages)
        else:
            print "ERROR: Properties File Doesn't Exist"
            print "INFO: Create the file or download a fresh copy from http://confluence-nam.lmera.ericsson.se/x/OQ1LCQ"
            raw_input('Press enter when the install.properties file has been created: ')
            command = self._generateInstallCommandConfigFile(enmProductVersion)
        return command

    def _generateInstallCommandFromUserInput(self, enmProductVersion):
        sedUrl = raw_input('Enter the SED URL: ')
        xmlFile = raw_input('Enter the url for the uploaded XML file: ')
        deployPackages = self._getPackageInfoFromUserInput()

        command = self._generateInstallCommandSyntax(enmProductVersion, sedUrl, xmlFile, deployPackages)

        return command

    def _getPackageInfoFromUserInput(self):
        packages = []
        print 'INFO: Enter Package name and Version Number or Package URL'
        while True:
            packageName = raw_input('Enter Package Name - Leave Blank to finish entering Packages: ')

            if packageName == '':
                break

            packageVersion = raw_input('Enter Version Number/URL to file or leave blank to use the latest version: ')

            if packageVersion == '':
                packageVersion = 'Latest'

            packages.append(packageName + '::' + packageVersion)

        deployPackages = self._generateDeploymentPackageList(packages)

        return deployPackages

    def _generateInstallCommandSyntax(self, enmProductVersion, sedUrl, xmlFile, deployPackages):
        clusterId = '239'

        command = '''nohup /proj/lciadm100/cifwk/latest/bin/cicmd deployment --clusterid ''' + clusterId + ''' --productSet ''' + self._enmVersion + '''::''' + enmProductVersion + ''' --product ENM --installType initial_install --setSED ''' + sedUrl + ''' --xmlFile ''' + xmlFile + ''' --deployPackage ''' + deployPackages + ''' &'''

        return command

    def _generateDeploymentPackageList(self, packages):
        joinText = '@@'
        deploymentPackages = joinText.join(packages)

        return deploymentPackages

class Deploy():

    def __init__(self, enmVersion):
        self._enmVersion = enmVersion
        self.continousIntegrationRestHelper = ContinousIntegrationRestHelper()
        self.dstHelper = DeploymentSupportToolingHelper()
        self.installHelper = InstallHelper(self._enmVersion)

    def _setEnmVersions(self):
        self._enmProductVersion = self.continousIntegrationRestHelper.getLatestEnmProductNumber(self._enmVersion)

    def _printVersionInformation(self):
        print 'INFO: ENM Version Entered:', self._enmVersion
        print 'INFO: Latest ENM Version:', self._enmProductVersion
        print 'INFO: Drop Version:', self.continousIntegrationRestHelper.getEnmDropVersion(self._enmVersion)
        print 'INFO: AOM Number:', self.continousIntegrationRestHelper.getAomNumber(self._enmVersion)
        print 'INFO: R-State:', self.continousIntegrationRestHelper.getRState(self._enmVersion)
        print 'INFO: Master SED Version:', self.continousIntegrationRestHelper.getMasterSedVersion()

    def _setIsoContents(self):
        print 'INFO: Retrieving ISO Contents'
        self._isoContents = self.continousIntegrationRestHelper.getIsoContents(self._enmVersion).replace(' ', '')

    def _downloadDeploymentTemplates(self):
        print 'INFO: Downloading Deployment Templates'
        self._setIsoContents()
        self.dstHelper.downloadDeploymentTemplate(self._isoContents)

    def _setBomFile(self):
        self._bomFile = self.dstHelper.getBomFileWithDstVersions();

    def _setVersionOfDeploymentSupportToolingRepo(self):
        self._deploymentSupportToolingVersion = self.dstHelper.getDeploymentSupportToolingVersion(self._bomFile)
        print '\n\nINFO: DST Version:', self._deploymentSupportToolingVersion

    def _setVersionOfDefaultConfigurableEntitiesRepo(self):
        self._defaultConfigurableEntitiesVersion = self.dstHelper.getDefaultConfigurableEntitiesVersion(self._bomFile)
        print 'INFO: DSE Version:', self._defaultConfigurableEntitiesVersion

    def _setVersionOfDeploymentDescriptionsRepo(self):
        self.deploymentDescriptionsVersion = self.dstHelper.getDeploymentTemplateVersion(self._isoContents)

    def _cloneRepositories(self):
        print('INFO: Cloning Repositories')
        repository = Repository()
        repository.cloneRepositoryDefaultConfigurableEntities(self._defaultConfigurableEntitiesVersion)
        repository.cloneRepositoryDeploymentDescriptions(self.deploymentDescriptionsVersion)
        
        print('Finished')

    def _getVersionInformation(self):
        print 'INFO: Retrieving Version Information'
        self._setBomFile()
        self._setVersionOfDeploymentSupportToolingRepo()
        self._setVersionOfDefaultConfigurableEntitiesRepo()
        self._setVersionOfDeploymentDescriptionsRepo()

    def _generateDeploymentDescriptions(self):
        self._downloadDeploymentTemplates()
        self._getVersionInformation()
        self._cloneRepositories()

        print '\nINFO: Downloading DST (Deployment Support Tooling) CLI'
        self.dstHelper.downloadDeploymentSupportTooling(self._deploymentSupportToolingVersion)
        print '\nINFO: Generate Deployment Descriptions using DST (Deployment Support Tooling)'
        self.dstHelper.runDeploymentSupportTooling(self.deploymentDescriptionsVersion, self._deploymentSupportToolingVersion, self._defaultConfigurableEntitiesVersion)

    def _checkSedChangesDone(self):#
        print '\n\nINFO: Download the SED from: ' + 'https://cifwk-oss.lmera.ericsson.se/dmt/clusterBuildArtifact/239/'
        print 'INFO: Make the necessary changes: See: http://confluence-nam.lmera.ericsson.se/display/EBSM/Installation+of+Vapp+with+new+Service+Group+and+Postgres+Database'
        print 'INFO: Upload the SED using the following instructions: http://confluence-nam.lmera.ericsson.se/x/mAxaBg'
        print '\n\nINFO: OPTIONAL: Add the link that is generated by the link into install.properties file'
        print '    e.g. sedurl = https://cifwk-oss.lmera.ericsson.se/static/tmpUploadSnapshot//2016-03-14_10-47-04/Cluster-1130-config.cfg'
        raw_input('\nWhen the SED has been updated press Enter: ')

    def _generateInstallCommand(self):
        print '\n\nINFO: Printing Install Command'
        print 'INFO: Run this command on the server you wish to install'
        print 'INFO: ', self.installHelper.generateInstallCommand(self._enmProductVersion)

    def run(self):
        self._setEnmVersions()
        self._printVersionInformation()
        self._generateDeploymentDescriptions()
        self._checkSedChangesDone()
        self._generateInstallCommand()

class FlushStandardOut(object):
    def __init__(self, standardOut):
        self.standardOut = standardOut
        
    def write(self, x):
        self.standardOut.write(x)
        self.standardOut.flush()


def verifyEnmVersion(enmVersion):
    print "test"
    enmVersionPattern = '[0-9]*\\.[0-9]*'
    validEnmVersionPattern = re.match(enmVersionPattern, enmVersion)
    print validEnmVersionPattern
    if not validEnmVersionPattern:
        print 'ERROR: Invalid ENM Version: ', enmVersion
        usage()
        sys.exit()

def usage():
    print 'Usage: '
    print 'Use default ENM Version 16.7:'
    print '   deploy.py'
    print
    print 'Use a specific ENM Version:'
    print '   deploy.py <Number>.<Number>'
    print '   e.g. deploy.py 16.5'

if __name__ == '__main__':
    enmVersion = '16.11'

    if len(sys.argv) > 1:
        enmVersion = sys.argv[1]
        verifyEnmVersion(enmVersion)
    else:
        print 'INFO: Using Default ENM version ' + enmVersion
        print ''
    
    sys.stdout = FlushStandardOut(sys.stdout)
    
    deploy = Deploy(enmVersion)
    deploy.run()
    

